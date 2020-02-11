/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.provider.loader.tracecobertura.report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.Node.NodeType;
import se.de.hu_berlin.informatik.spectra.core.traces.RawIntTraceCollector;
import se.de.hu_berlin.informatik.spectra.core.traces.SimpleIntIndexerCompressed;
import se.de.hu_berlin.informatik.spectra.provider.loader.AbstractCoverageDataLoader;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.AbstractCodeProvider;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ClassData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ExecutionTraceCollector;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.JumpData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.LineData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.PackageData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SourceFileData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SwitchData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CoberturaStatementEncoding;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.SingleLinkedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.SequiturUtils;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.InputSequence;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.InputSequence.TraceIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.SharedInputGrammar;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.output.OutputSequence;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.output.SharedOutputGrammar;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ProjectData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.report.TraceCoberturaReportWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public abstract class TraceCoberturaReportLoader<K extends ITrace<SourceCodeBlock>>
		extends AbstractCoverageDataLoader<SourceCodeBlock, K, TraceCoberturaReportWrapper> {

    private static final int LARGE_STEP = 10000000;
	private static final int SMALL_STEP = 100000;
	private final RawIntTraceCollector traceCollector;
	private ProjectData projectData;
	
	private Map<Long, Integer> idToSubtraceIdMap = new HashMap<>();
	private Map<Integer, int[]> existingSubTraces = new HashMap<>();

	public TraceCoberturaReportLoader(Path tempOutputDir) {
		traceCollector = new RawIntTraceCollector(tempOutputDir);
	}
	
	private int traceCount = 0;
	private int currentId = 0;
	
	SharedOutputGrammar sharedExecutionTraceGrammar = new SharedOutputGrammar();
	SharedOutputGrammar sharedSubTraceGrammar = new SharedOutputGrammar();
	
	private final boolean sharedExe = false;
//	private final boolean sharedSub = false;

	
	@Override
	public boolean loadSingleCoverageData(ISpectra<SourceCodeBlock, K> lineSpectra, final TraceCoberturaReportWrapper reportWrapper,
			final boolean fullSpectra) {
		if (reportWrapper == null || reportWrapper.getReport() == null) {
			return false;
		}

		
		K trace = null;

		ProjectData projectData = reportWrapper.getReport().getProjectData();
		if (projectData == null) {
			return false;
		} else if (this.projectData == null) {
			this.projectData = projectData;
		}

		String testId;
		if (reportWrapper.getIdentifier() == null) {
			testId = String.valueOf(++traceCount);
		} else {
			++traceCount;
			testId = reportWrapper.getIdentifier();
		}
		
		try {
		trace = lineSpectra.addTrace(testId, traceCount, reportWrapper.isSuccessful());
		
		if (projectData.isReset()) {
			Log.warn(this, "Test '%s' produced no coverage.", testId);
			return true;
		}
		
		boolean coveredLines = false;
		
//		Log.out(true, this, "Processing Test: " + reportWrapper.getIdentifier());
		
		// loop over all packages
		for (CoverageData coverageData2 : projectData.getPackages()) {
			PackageData packageData = (PackageData) coverageData2;
			final String packageName = packageData.getName();

			onNewPackage(packageName, trace);

			// loop over all classes of the package
			for (SourceFileData sourceFileData : packageData.getSourceFiles()) {
				for (CoverageData coverageData1 : sourceFileData.getClasses()) {
					ClassData classData = (ClassData) coverageData1;
					// TODO: use actual class name!?
					final String actualClassName = classData.getName();
					final String sourceFilePath = classData.getSourceFileName();

					onNewClass(packageName, sourceFilePath, trace);

					// loop over all methods of the class
					// SortedSet<String> sortedMethods = new TreeSet<>();
					// sortedMethods.addAll(classData.getMethodNamesAndDescriptors());
					for (String methodNameAndSig : classData.getMethodNamesAndDescriptors()) {
						// String name = methodNameAndSig.substring(0,
						// methodNameAndSig.indexOf('('));
						// String signature =
						// methodNameAndSig.substring(methodNameAndSig.indexOf('('));

						final String methodIdentifier = String.format("%s:%s", actualClassName, methodNameAndSig);

						onNewMethod(packageName, sourceFilePath, methodIdentifier, trace);

						// loop over all lines of the method
						// SortedSet<CoverageData> sortedLines = new
						// TreeSet<>();
						// sortedLines.addAll(classData.getLines(methodNameAndSig));
						for (CoverageData coverageData : classData.getLines(methodNameAndSig)) {
							LineData lineData = (LineData) coverageData;

							// set node involvement
							SourceCodeBlock lineIdentifier = getIdentifier(
									packageName, sourceFilePath, methodNameAndSig, lineData.getLineNumber(), NodeType.NORMAL);

							long hits = lineData.getHits();
							if (hits > 0) {
								coveredLines = true;
							}
							onNewLine(
									packageName, sourceFilePath, methodIdentifier, lineIdentifier, lineSpectra, trace,
									fullSpectra, hits);
							
							if (lineData.hasBranch()) {
								int conditionSize = lineData.getConditionSize();
								for (int index = 0; index < conditionSize; ++index) {
									Object branchData = lineData.getConditionData(index);
									if (branchData == null) {
										continue;
									} else if (branchData instanceof JumpData) {
										JumpData jumpData = (JumpData) branchData;
										
										// add nodes for false branches (the counters point at the "wrong" boolean)
										SourceCodeBlock lineIdentifier2 = getIdentifier(
												packageName, sourceFilePath, methodNameAndSig, lineData.getLineNumber(), NodeType.FALSE_BRANCH);
//										Log.out(this, "%s, F: %d", lineIdentifier.toString(), jumpData.getTrueHits());
										onNewLine(
												packageName, sourceFilePath, methodIdentifier, lineIdentifier2, lineSpectra, trace,
												fullSpectra, jumpData.getTrueHits());
										
										// add nodes for true branches (the counters point at the "wrong" boolean)
										SourceCodeBlock lineIdentifier3 = getIdentifier(
												packageName, sourceFilePath, methodNameAndSig, lineData.getLineNumber(), NodeType.TRUE_BRANCH);
//										Log.out(this, "%s, T: %d", lineIdentifier.toString(), jumpData.getFalseHits());
										onNewLine(
												packageName, sourceFilePath, methodIdentifier, lineIdentifier3, lineSpectra, trace,
												fullSpectra, jumpData.getFalseHits());
									} else if (branchData instanceof SwitchData) {
										SwitchData switchData = (SwitchData) branchData;
										
										// add nodes for default branch of switch statements
										SourceCodeBlock lineIdentifier4 = getIdentifier(
												packageName, sourceFilePath, methodNameAndSig, lineData.getLineNumber(), NodeType.SWITCH_BRANCH);
//										Log.out(this, "%s, SD: %d", lineIdentifier.toString(), switchData.getDefaultHits());
//										onNewLine(
//												packageName, sourceFilePath, methodIdentifier, lineIdentifier, lineSpectra, trace,
//												fullSpectra, switchData.getDefaultHits());
										
										long switchBranchHits = 0;
										for (int i = 0; i < switchData.getNumberOfValidBranches(); ++i) {
											switchBranchHits += (switchData.getHits(i) < 0 ? 0 : switchData.getHits(i));
										}
										
//										// add nodes for default branch of switch statements
//										lineIdentifier = getIdentifier(
//												packageName, sourceFilePath, methodNameAndSig, lineData.getLineNumber(), NodeType.SWITCH_BRANCH);
//										Log.out(this, "%s, SH: %d, SD: %d", lineIdentifier4.toString(), switchBranchHits, switchData.getDefaultHits());
//										onNewLine(
//												packageName, sourceFilePath, methodIdentifier, lineIdentifier, lineSpectra, trace,
//												fullSpectra, switchBranchHits);
										
										onNewLine(
												packageName, sourceFilePath, methodIdentifier, lineIdentifier4, lineSpectra, trace,
												fullSpectra, switchBranchHits + switchData.getDefaultHits());
									}
								}
							}
						}

						onLeavingMethod(packageName, sourceFilePath, methodIdentifier, lineSpectra, trace);
					}

					onLeavingClass(packageName, sourceFilePath, lineSpectra, trace);
				}
			}

			onLeavingPackage(packageName, lineSpectra, trace);
		}
		
		trace.sleep();

		if (!coveredLines) {
			Log.warn(this, "Test '%s' covered no lines.", testId);
			if (projectData.getExecutionTraces() == null) {
				Log.err(this, "Execution trace is null for test '%s'.", testId);
				return false;
			}
			if (!projectData.getExecutionTraces().isEmpty()) {
				Log.err(this, "Execution trace for test '%s' is NOT empty.", testId);
				return false;
			}
			return true;
		}
		
		if (projectData.getExecutionTraces() == null) {
			Log.err(this, "Execution trace is null for test '%s'.", testId);
			return false;
		}

		if (projectData.getExecutionTraces().isEmpty()) {
			Log.warn(this, "No execution trace for test '%s'.", testId);
		} else {
			// TODO debug output
			// for (Object classData : projectData.getClasses()) {
			// Log.out(true, this, ((MyClassData)classData).getName());
			// }
//			Log.out(true, this, "Trace: " + reportWrapper.getIdentifier());
			
			
			// convert execution traces from statement sequences to sequences of sub traces
			Map<Long, byte[]> executionTracesWithSubTraces = new HashMap<>();
			for (Iterator<Entry<Long, byte[]>> iterator = projectData.getExecutionTraces().entrySet().iterator(); iterator.hasNext();) {
				Entry<Long, byte[]> entry = iterator.next();
				
				byte[] mappedExecutionTrace = generateSubTraceExecutionTrace(entry.getValue(), projectData, lineSpectra);
				
//				System.out.println(String.format("grammar size: %,d", SpectraFileUtils.convertToByteArray(sharedExecutionTraceGrammar).length/4));
				
				executionTracesWithSubTraces.put(entry.getKey(), mappedExecutionTrace);
			}
			projectData.addExecutionTraces(executionTracesWithSubTraces);
			
			
			int threadId = -1;
			for (Iterator<Entry<Long, byte[]>> iterator = projectData.getExecutionTraces().entrySet().iterator(); iterator.hasNext();) {
				Entry<Long, byte[]> entry = iterator.next();
				++threadId;
				
				
				boolean printTraces = 
						false;
//						true;
				if (printTraces) {
					// only for testing... the arrays in a trace are composed of [ class id, counter id].
					String[] idToClassNameMap = projectData.getIdToClassNameMap();
					Log.out(true, this, "Thread: " + entry.getKey());
					// iterate over executed statements in the trace

					SharedInputGrammar inExecutionTraceGrammar = SequiturUtils.convertToInputGrammar(sharedExe ? sharedExecutionTraceGrammar : null);
					
					InputSequence inputSequence = SequiturUtils.getInputSequenceFromByteArray(entry.getValue(), inExecutionTraceGrammar);
					TraceIterator traceIterator = inputSequence.iterator();
		            
//		            SharedInputGrammar inSubTraceGrammar = SequiturUtils.convertToInputGrammar(sharedSubTraceGrammar);
			        
					while (traceIterator.hasNext()) {
						int subTraceId = traceIterator.next();
						int[] subTrace = null;
						if (subTraceId == 0) {
							subTrace = null;
						} else {
//							subTrace = InputSequence.readFrom(new ObjectInputStream(new ByteArrayInputStream(existingSubTraces.get(subTraceId))), inSubTraceGrammar);
							subTrace = existingSubTraces.get(subTraceId);
							if (subTrace == null) {
								Log.err(this, "No sub trace found for ID: " + subTraceId);
								return false;
							}
						}
						if (subTrace == null) {
							continue;
						}
						Log.out(true, this, "sub trace ID: " + subTraceId + ", length: " + subTrace.length);

						Iterator<Integer> longTraceIterator = Arrays.stream(subTrace).iterator();
						while (longTraceIterator.hasNext()) {
							int statement = longTraceIterator.next();
//							if (CoberturaStatementEncoding.getClassId(statement) != 142) {
//								continue;
//							}
//							Log.out(true, this, "statement: " + Arrays.toString(statement));
							// TODO store the class names with '.' from the beginning, or use the '/' version?
							String classSourceFileName = idToClassNameMap[CoberturaStatementEncoding.getClassId(statement)];
							if (classSourceFileName == null) {
								//						throw new IllegalStateException("No class name found for class ID: " + statement[0]);
								Log.err(this, "No class name found for class ID: " + CoberturaStatementEncoding.getClassId(statement));
								return false;
							}
//							if (!classSourceFileName.contains("FastDateParser")) {
//								continue;
//							}
							ClassData classData = projectData.getClassData(classSourceFileName);

							if (classData != null) {
								if (classData.getCounterId2LineNumbers() == null) {
									Log.err(this, "No counter ID to line number map for class " + classSourceFileName);
									return false;
								}
								int[] lineNumber = classData.getCounterId2LineNumbers()[CoberturaStatementEncoding.getCounterId(statement)];
//								if (lineNumber != 398 && lineNumber != 399) {
//									continue;
//								}

								NodeType nodeType = NodeType.NORMAL;
								// these following lines print out the execution trace
								String addendum = "";
								if (lineNumber[1] != CoberturaStatementEncoding.NORMAL_ID) {
									switch (lineNumber[1]) {
									case CoberturaStatementEncoding.BRANCH_ID:
										addendum = " (from branch/'false' branch)";
										nodeType = NodeType.FALSE_BRANCH;
										break;
									case CoberturaStatementEncoding.JUMP_ID:
										addendum = " (after jump/'true' branch)";
										nodeType = NodeType.TRUE_BRANCH;
										break;
									case CoberturaStatementEncoding.SWITCH_ID:
										addendum = " (after switch label)";
										nodeType = NodeType.SWITCH_BRANCH;
										break;
									default:
										addendum = " (unknown)";
									}
								}
								Log.out(true, this, classSourceFileName + ":" + classData.getMethodName(lineNumber[0]) +
										", counter ID " + CoberturaStatementEncoding.getCounterId(statement) +
										", line " + (lineNumber[0] < 0 ? "(not set)" : String.valueOf(lineNumber[0])) +
										addendum);

								// the array is initially set to -1 to indicate counter IDs that were not set, if any
								if (lineNumber[0] >= 0) {
									int nodeIndex = getNodeIndex(classData.getSourceFileName(), lineNumber[0], nodeType);
									//								nodeIndex = 1;
									if (nodeIndex >= 0) {
										// everything's fine; the below statement is from an earlier version
										//traceOfNodeIDs.add(nodeIndex);
									} else {
										// node index not correct...
										String throwAddendum = "";
										if (lineNumber[1] != CoberturaStatementEncoding.NORMAL_ID) {
											switch (lineNumber[1]) {
											case CoberturaStatementEncoding.BRANCH_ID:
												throwAddendum = " (from branch/'false' branch)";
												break;
											case CoberturaStatementEncoding.JUMP_ID:
												throwAddendum = " (after jump/'true' branch)";
												break;
											case CoberturaStatementEncoding.SWITCH_ID:
												throwAddendum = " (after switch label)";
												break;
											default:
												throwAddendum = " (unknown)";
											}
										}
										Log.err(this, "Node not found in spectra: "
												+ classData.getSourceFileName() + ":" + lineNumber[0] 
												+ " from counter id " + CoberturaStatementEncoding.getCounterId(statement) + throwAddendum);
										//									return false;
									}
//								} else if (statement.length <= 2 || statement[2] != 0) {
//									// disregard counter ID 0 if it comes from an internal variable (fake jump?!)
//									// this should actually not be an issue anymore!
//									//							throw new IllegalStateException("No line number found for counter ID: " + counterId
//									//									+ " in class: " + classData.getName());
//									Log.err(this, "No line number found for counter ID: " + CoberturaStatementEncoding.getCounterId(statement)
//									+ " in class: " + classData.getName());
//									return false;
								} else {
									Log.err(this, "No line number found for counter ID: " + CoberturaStatementEncoding.getCounterId(statement)
									+ " in class: " + classData.getName());
									return false;
									//							// we have to add a dummy node here to not mess up the repetition markers
									//							traceOfNodeIDs.add(-1);
									//							Log.out(this, "Ignoring counter ID: " + statement[1]
									//									+ " in class: " + classData.getName());
								}
							} else {
								throw new IllegalStateException("Class data for '" + classSourceFileName + "' not found.");
							}
						}
					}
					System.out.flush();
					//Thread.sleep(1000);
					// testing done!....
				}
				
				// processed and done with...
				iterator.remove();
				
				// collect the raw trace for future compression, etc.
				// this will, among others, extract common sequences for added traces
				traceCollector.addRawTraceToPool(traceCount, threadId, entry.getValue());
				
			}
			
		}
		
		return true;
		} catch (Throwable e) {
			Log.err(this, e, "Exception thrown for test '%s'.", testId);
			return false;
		}
	}

	private byte[] generateSubTraceExecutionTrace(byte[] trace, 
			ProjectData projectData, ISpectra<SourceCodeBlock, K> lineSpectra) throws ClassNotFoundException, IOException {
		OutputSequence resultTrace = sharedExe ? new OutputSequence(sharedExecutionTraceGrammar) : new OutputSequence();
		String[] idToClassNameMap = projectData.getIdToClassNameMap();
		// iterate over trace and generate new trace based on seen sub traces
		// iterate over executed statements in the trace

		ByteArrayInputStream byteIn = new ByteArrayInputStream(trace);
        ObjectInputStream objIn = new ObjectInputStream(byteIn);
        TraceIterator traceIterator = InputSequence.readFrom(objIn).iterator();
		
		SingleLinkedIntArrayQueue currentSubTrace = new SingleLinkedIntArrayQueue(100);
		int lastMethod = -1;
		int lastClass = -1;
		int lastNodeType = CoberturaStatementEncoding.NORMAL_ID;
		long counter = 0;
		while (traceIterator.hasNext()) {
//			++counter;
//			if (counter % 100000 == 0)
//				System.out.print('.');
//			if (counter % 10000000 == 0)
//				System.out.println(String.format("%,d", counter));
			
			int statement = traceIterator.next();
			
			// check if the current statement indicates the start of a new sub trace
			if (statement == ExecutionTraceCollector.NEW_SUBTRACE_ID) {
				// cut the trace **before** each catch block entry or new method start
				if (!currentSubTrace.isEmpty()) {
					currentSubTrace = processLastSubTrace(resultTrace, currentSubTrace, lastNodeType, idToClassNameMap, lineSpectra);
					
					if (++counter % SMALL_STEP == 0) {
						System.out.print('.');
						if (counter % LARGE_STEP == 0)
							System.out.println(String.format("%,d", counter));
					}
				}
				
				while (statement == ExecutionTraceCollector.NEW_SUBTRACE_ID) {
					// skip the indicator
					if (traceIterator.hasNext()) {
						statement = traceIterator.next();
					} else {
						break;
					}
				}
				
				if (statement == ExecutionTraceCollector.NEW_SUBTRACE_ID) {
					// we're at the end of the trace!
					break;
				}
			}

//			Log.out(true, this, "statement: " + Arrays.toString(statement));
			// TODO store the class names with '.' from the beginning, or use the '/' version?
			int classId = CoberturaStatementEncoding.getClassId(statement);
			int counterId = CoberturaStatementEncoding.getCounterId(statement);
			
			ClassData classData = projectData.getClassData(classId);

			if (classData != null) {
				
				if (counterId == AbstractCodeProvider.FAKE_COUNTER_ID) {
					// this marks a fake jump! we should not be here...
					throw new IllegalStateException("Illegal counter ID 0 in class " + classId + ". (" + classData.getName() + ")");
				}

				int[] lineNumber = classData.getCounterId2LineNumbers()[counterId];

				// check if we switched to a different class than before
				if (classId == lastClass) {
					// check if we switched to a different method than we were in before;
					// this should allow for the sub traces to be uniquely determined by first and last statement
					int currentMethod = lineNumber[2] < 0 ? lastMethod : lineNumber[2];
					if (currentMethod != lastMethod && !currentSubTrace.isEmpty()) {
						// cut the trace after each change in methods
						currentSubTrace = processLastSubTrace(resultTrace, currentSubTrace, lastNodeType, idToClassNameMap, lineSpectra);
						
						if (++counter % SMALL_STEP == 0) {
							System.out.print('.');
							if (counter % LARGE_STEP == 0)
								System.out.println(String.format("%,d", counter));
						}
					}
					lastMethod = currentMethod;
				} else {
					// change in classes!
					if (!currentSubTrace.isEmpty()) {
						// cut the trace after each change in classes
						currentSubTrace = processLastSubTrace(resultTrace, currentSubTrace, lastNodeType, idToClassNameMap, lineSpectra);
						
						if (++counter % SMALL_STEP == 0) {
							System.out.print('.');
							if (counter % LARGE_STEP == 0)
								System.out.println(String.format("%,d", counter));
						}
					}
					lastMethod = lineNumber[2];
					lastClass = classId;
				}
				
				// add the current statement to the current sub trace
				currentSubTrace.addNoAutoBoxing(statement);

				
				lastNodeType = lineNumber[1];
				
				if (lastNodeType != CoberturaStatementEncoding.NORMAL_ID && !currentSubTrace.isEmpty()) {
					// cut the trace after each branching statement
					currentSubTrace = processLastSubTrace(resultTrace, currentSubTrace, lastNodeType, idToClassNameMap, lineSpectra);
					
					if (++counter % SMALL_STEP == 0) {
						System.out.print('.');
						if (counter % LARGE_STEP == 0)
							System.out.println(String.format("%,d", counter));
					}
				}

			} else {
				throw new IllegalStateException("Class data for class '" + classId + "' not found.");
			}
		}
		
		// process any remaining statements
		if (!currentSubTrace.isEmpty()) {
			currentSubTrace = processLastSubTrace(resultTrace, currentSubTrace, lastNodeType, idToClassNameMap, lineSpectra);
			currentSubTrace.clear();
		}

		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
		resultTrace.writeOut(objOut, !sharedExe);
		objOut.close();
		byte[] bytes = byteOut.toByteArray();
		
		if (!sharedExe) {
			System.out.println(String.format("%n#sub traces: %,d -> %,d (%.2f%%)", 
					counter, bytes.length/4, -100.00+100.0*(double)(bytes.length/4)/(double)counter));
		}
		
		return bytes;
	}
	
	
	private SingleLinkedIntArrayQueue processLastSubTrace(
			OutputSequence resultTrace, SingleLinkedIntArrayQueue currentSubTrace, 
			int lastNodeType, String[] idToClassNameMap, ISpectra<SourceCodeBlock, K> lineSpectra) {
		// get a representation id for the subtrace (unique for sub traces that start and end within the same method!)
		long subTraceId = CoberturaStatementEncoding.generateRepresentationForSubTrace(currentSubTrace);
		Integer id = idToSubtraceIdMap.get(subTraceId);

//		// check if the current sub trace has been seen before and, if so, get the respective id
//		for (EfficientCompressedIntegerTrace subTrace : subTraces) {
//			if (sameSubTrace(subTrace, currentSubTrace)) {
//				id = subtraceToIdMap.get(subTrace);
//				break;
//			}
//		}
		
		int subTraceLength = currentSubTrace.size();
		
		Integer lastExecutedStatement = null;
		if (lastNodeType != CoberturaStatementEncoding.NORMAL_ID) {
			lastExecutedStatement = currentSubTrace.peekLastNoCheck();
		}
		
		if (id == null) {
			// first time seeing this sub trace!
			// starts with id 1
			id = ++currentId;

			// add id to the map
			idToSubtraceIdMap.put(subTraceId, id);
			
//			// using sequitur sequences
//			OutputSequence subTrace = sharedSub ? new OutputSequence(sharedSubTraceGrammar) : new OutputSequence();
//			while (!currentSubTrace.isEmpty()) {
//				// convert to the actual spectra node indices
//				subTrace.append(getNodeIndexForCounter(currentSubTrace.removeNoAutoBoxing(), idToClassNameMap, lineSpectra));
//			}
//			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
//			try (ObjectOutputStream objOut= new ObjectOutputStream(byteOut)) {
//				subTrace.writeOut(objOut, !sharedSub);
//				objOut.close();
//				
//				byte[] bytes = byteOut.toByteArray();
//				
//				if (!sharedSub) {
//					System.out.println(String.format("#sub trace: %,d -> %,d", subTraceLength, bytes.length/4));
//				}
//				
//				// add sub trace to the list of existing sub traces (together with the id)
//				existingSubTraces.put(id, bytes);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			// using integer arrays
			int[] subTrace = new int[subTraceLength];
			int j = 0;
			while (!currentSubTrace.isEmpty()) {
				// convert to the actual spectra node indices
				subTrace[j++] = getNodeIndexForCounter(currentSubTrace.removeNoAutoBoxing(), idToClassNameMap, lineSpectra);
			}
			// add sub trace to the list of existing sub traces (together with the id)
			existingSubTraces.put(id, subTrace);
			
			currentSubTrace.clear();
		} else {
			// already seen, so reuse the sub trace
			currentSubTrace.clear();
//			currentSubTrace = getNewSubTrace(trace);
		}
		
		if (lastExecutedStatement != null) {
			// add the last executed statement (usually a branch)
			// to the new sub trace
			currentSubTrace.add(lastExecutedStatement);
		}
		
		resultTrace.append(id);
		return currentSubTrace;
	}
	
	
	private int getNodeIndexForCounter(int encodedStatement, String[] idToClassNameMap, ISpectra<SourceCodeBlock, K> lineSpectra) {
		int classId = CoberturaStatementEncoding.getClassId(encodedStatement);
		int counterId = CoberturaStatementEncoding.getCounterId(encodedStatement);
		
		//			 Log.out(true, this, "statement: " + Arrays.toString(statement));
		// TODO store the class names with '.' from the beginning, or use the '/' version?
		String classSourceFileName = idToClassNameMap[classId];
		if (classSourceFileName == null) {
			throw new IllegalStateException("No class name found for class ID: " + classId);
		}
		ClassData classData = projectData.getClassData(classSourceFileName);

		if (classData != null) {
			if (classData.getCounterId2LineNumbers() == null) {
				throw new IllegalStateException("No counter ID to line number map for class " + classSourceFileName);
			}
			int[] lineNumber = classData.getCounterId2LineNumbers()[counterId];
			int specialIndicatorId = lineNumber[1];

			//				// these following lines print out the execution trace
			//				String addendum = "";
			//				if (statement.length > 2) {
			//					switch (statement[2]) {
			//					case 0:
			//						addendum = " (from branch)";
			//						break;
			//					case 1:
			//						addendum = " (after jump)";
			//						break;
			//					case 2:
			//						addendum = " (after switch label)";
			//						break;
			//					default:
			//						addendum = " (unknown)";
			//					}
			//				}
			//				Log.out(true, this, classSourceFileName + ", counter  ID " + statement[1] +
			//						", line " + (lineNumber < 0 ? "(not set)" : String.valueOf(lineNumber)) +
			//						addendum);
			
			NodeType nodeType = NodeType.NORMAL;
			switch (specialIndicatorId) {
			case CoberturaStatementEncoding.SWITCH_ID:
				nodeType = NodeType.SWITCH_BRANCH;
				break;
			case CoberturaStatementEncoding.BRANCH_ID:
				nodeType = NodeType.FALSE_BRANCH;
				break;
			case CoberturaStatementEncoding.JUMP_ID:
				nodeType = NodeType.TRUE_BRANCH;
				break;
			default:
				// ignore switch statements...
			}

			// the array is initially set to -1 to indicate counter IDs that were not set, if any
			if (lineNumber[0] >= 0) {
				int nodeIndex = getNodeIndex(lineSpectra, classData.getSourceFileName(), lineNumber[0], nodeType);
				if (nodeIndex >= 0) {
					return nodeIndex;
				} else {
					String throwAddendum = "";
					switch (specialIndicatorId) {
					case CoberturaStatementEncoding.BRANCH_ID:
						throwAddendum = " (from branch/'false' branch)";
						break;
					case CoberturaStatementEncoding.JUMP_ID:
						throwAddendum = " (after jump/'true' branch)";
						break;
					case CoberturaStatementEncoding.SWITCH_ID:
						throwAddendum = " (after switch label)";
						break;
					default:
						// ?
					}
//					if (nodeType.equals(NodeType.NORMAL)) {
					throw new IllegalStateException("Node not found in spectra: "
							+ classData.getSourceFileName() + ":" + lineNumber[0] 
							+ " from counter id " + counterId + throwAddendum);
//					} else {
//						System.err.println("Node not found in spectra: "
//								+ classData.getSourceFileName() + ":" + lineNumber 
//								+ " from counter id " + statement[1] + throwAddendum);
//					}
				}
			} else {
				throw new IllegalStateException("No line number found for counter ID: " + counterId
						+ " in class: " + classData.getName());
			}
		} else {
			throw new IllegalStateException("Class data for '" + classSourceFileName + "' not found.");
		}
	}
	
	private int getNodeIndex(final ISpectra<SourceCodeBlock, ?> lineSpectra, String sourceFilePath, int lineNumber, NodeType nodeType) {
		SourceCodeBlock identifier = new SourceCodeBlock(null, sourceFilePath, null, lineNumber, nodeType);
		INode<SourceCodeBlock> node = lineSpectra.getNode(identifier);
		if (node == null) {
			return -1;
		} else {
			return node.getIndex();
		}
	}
	

//	private boolean sameSubTrace(EfficientCompressedIntegerTrace subTrace,
//			EfficientCompressedIntegerTrace currentSubTrace) {
//		if (subTrace.size() != currentSubTrace.size() || 
//				subTrace.getCompressedTrace().size() != currentSubTrace.getCompressedTrace().size()) {
//			return false;
//		}
//		IntTraceIterator iterator = subTrace.iterator();
//		IntTraceIterator iterator2 = currentSubTrace.iterator();
//		while (iterator.hasNext()) {
//			if (iterator.next() != iterator2.next()) {
//				return false;
//			}
//		}
//		return true;
//	}

//	private OutputSequence getNewSubTrace() {
//		return sharedSub ? new OutputSequence(sharedSubTraceGrammar) : new OutputSequence();
//	}

	public void addExecutionTracesToSpectra(ISpectra<SourceCodeBlock, ? super K> spectra) {
		// add trace collector to the spectra
		spectra.setRawTraceCollector(traceCollector);
				
		// gets called after ALL tests have been processed
		Log.out(TraceCoberturaReportLoader.class, "Setting up indexer...");
		
		// generate mapping from statements to spectra nodes
		SimpleIntIndexerCompressed simpleIndexer = null;
		try {
//			simpleIndexer = new SimpleIntIndexerCompressed(
//					sharedExe ? sharedExecutionTraceGrammar : null, 
//							existingSubTraces, sharedSub ? sharedSubTraceGrammar : null);
			simpleIndexer = new SimpleIntIndexerCompressed(
					sharedExe ? sharedExecutionTraceGrammar : null, existingSubTraces);
		} catch (IOException e) {
			Log.abort(TraceCoberturaReportLoader.class, e, "Error setting up indexer");
		}
		
		// store the indexer with the spectra
		spectra.setIndexer(simpleIndexer);
		
		
		
//		// generate the execution traces for each test case and add them to the spectra;
//		// this needs to be done AFTER all tests have been executed
//		for (ITrace<?> trace : spectra.getTraces()) {
//			// generate execution traces from collected raw traces
//			List<ExecutionTrace> executionTraces = traceCollector.getExecutionTraces(trace.getIndex());
//			if (executionTraces != null) {
//				// add those traces to the test case
//				for (ExecutionTrace executionTrace : executionTraces) {
//					trace.addExecutionTrace(executionTrace);
//				}
//			}
//		}
//		
//		// remove temporary zip file containing the raw traces
//		try {
//			traceCollector.finalize();
//		} catch (Throwable e) {
//			// meh...
//		}
	}
	
}
