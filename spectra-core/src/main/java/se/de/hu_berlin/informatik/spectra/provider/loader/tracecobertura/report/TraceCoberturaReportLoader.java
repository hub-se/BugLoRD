/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.provider.loader.tracecobertura.report;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.Node.NodeType;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.branch.SubTracePool;
import se.de.hu_berlin.informatik.spectra.core.branch.SubTraceSequencePool;
import se.de.hu_berlin.informatik.spectra.core.traces.RawIntTraceCollector;
import se.de.hu_berlin.informatik.spectra.core.traces.SimpleIntIndexerCompressed;
import se.de.hu_berlin.informatik.spectra.provider.loader.AbstractCoverageDataLoader;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.*;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CoberturaStatementEncoding;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.SingleLinkedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.SequiturUtils;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.InputSequence;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.InputSequence.TraceIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.SharedInputGrammar;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.output.OutputSequence;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.output.SharedOutputGrammar;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.report.TraceCoberturaReportWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public abstract class TraceCoberturaReportLoader<K extends ITrace<SourceCodeBlock>>
        extends AbstractCoverageDataLoader<SourceCodeBlock, K, TraceCoberturaReportWrapper> {

    private static final int LARGE_STEP = 100000000;
    private static final int SMALL_STEP = 1000000;
    private final RawIntTraceCollector traceCollector;
    private ProjectData projectData;

    // stores instances of all *sequences of* sub traces that have been seen so far;
    // the sequences are max length sequences of sub traces with no branches in the sequence
    // (branches are only allowed at the start or the end of the sequence!)
    private SubTraceSequencePool subTraceSequencePool;

    // stores instances of all sub traces that have been seen so far;
    // branches are only allowed at the start or the end of a sub trace!
    private SubTracePool subTracePool;

    public TraceCoberturaReportLoader(Path tempOutputDir) {
        traceCollector = new RawIntTraceCollector(tempOutputDir);
        subTracePool = new SubTracePool(tempOutputDir);
        subTraceSequencePool = new SubTraceSequencePool(tempOutputDir);
    }

    private int traceCount = 0;

    SharedOutputGrammar sharedExecutionTraceGrammar = new SharedOutputGrammar();
    SharedOutputGrammar sharedSubTraceGrammar = new SharedOutputGrammar();

    private final boolean sharedExe = true;
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
            this.subTracePool.setProjectData(projectData);
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


//            	SharedInputGrammar sharedInputGrammar = SequiturUtils.getInputGrammarFromByteArray(projectData.getExecutionTraces().getSecond());
                // convert execution traces from statement sequences to sequences of sub traces
            	List<Pair<Long, byte[]>> executionTracesWithSubTraces = new ArrayList<>(projectData.getExecutionTraces().size());
                for (Iterator<Pair<Long, byte[]>> iterator = projectData.getExecutionTraces().iterator(); iterator.hasNext(); ) {
                    Pair<Long, byte[]> entry = iterator.next();
                    iterator.remove();

                    byte[] mappedExecutionTrace = generateSubTraceExecutionTrace(entry.getSecond(), projectData, lineSpectra);

//				System.out.println(String.format("grammar size: %,d", SpectraFileUtils.convertToByteArray(sharedExecutionTraceGrammar).length/4));

                    executionTracesWithSubTraces.add(new Pair<>(entry.getFirst(), mappedExecutionTrace));
                }
                projectData.addExecutionTraces(executionTracesWithSubTraces);


                int threadId = -1;
                for (Iterator<Pair<Long, byte[]>> iterator = projectData.getExecutionTraces().iterator(); iterator.hasNext(); ) {
                    Pair<Long, byte[]> entry = iterator.next();
                    ++threadId;


                    boolean printTraces =
                            false;
//						true;
                    if (printTraces) {
                        // only for testing... the arrays in a trace are composed of [ class id, counter id].
                        String[] idToClassNameMap = projectData.getIdToClassNameMap();
                        Log.out(true, this, "Thread: " + entry.getFirst());
                        // iterate over executed statements in the trace

                        SharedInputGrammar inExecutionTraceGrammar = SequiturUtils.convertToInputGrammar(sharedExe ? sharedExecutionTraceGrammar : null);

                        InputSequence inputSequence = SequiturUtils.getInputSequenceFromByteArray(entry.getSecond(), inExecutionTraceGrammar);
                        TraceIterator traceIterator = inputSequence.iterator();

//		            SharedInputGrammar inSubTraceGrammar = SequiturUtils.convertToInputGrammar(sharedSubTraceGrammar);

                        while (traceIterator.hasNext()) {
                            int subTraceId = traceIterator.next();
                            int[] subTrace;
                            if (subTraceId == 0) {
                                subTrace = null;
                            } else {
//							subTrace = InputSequence.readFrom(new ObjectInputStream(new ByteArrayInputStream(existingSubTraces.get(subTraceId))), inSubTraceGrammar);
                                // TODO might run into issues due to rewriting zip files, etc...
                                subTrace = (int[]) subTracePool.getExistingSubTraces().get(subTraceId);
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
                    traceCollector.addRawTraceToPool(traceCount, threadId, entry.getSecond());

                }

            }

            return true;
        } catch (Throwable e) {
            Log.err(this, e, "Exception thrown for test '%s'.", testId);
            return false;
        }
    }

    private byte[] generateSubTraceExecutionTrace(byte[] trace, ProjectData projectData, 
    		ISpectra<SourceCodeBlock, K> lineSpectra) throws ClassNotFoundException, IOException {
        OutputSequence resultTrace = sharedExe ? new OutputSequence(sharedExecutionTraceGrammar) : new OutputSequence();
        String[] idToClassNameMap = projectData.getIdToClassNameMap();
        // iterate over trace and generate new trace based on seen sub traces
        // iterate over executed statements in the trace

        TraceIterator traceIterator = SequiturUtils.getInputSequenceFromByteArray(trace).iterator();

        SingleLinkedIntArrayQueue currentSubTrace = new SingleLinkedIntArrayQueue(15);
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

            // check if the current statement indicates the start of a new sub trace;
            // this is currently done when entering a catch block or entering a method
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

        // process remaining sub trace sequence
        if (currentSubTraceSequence != null) {
            addCurrentSubTraceSequenceToTrace(resultTrace);
        }

        byte[] bytes = SequiturUtils.convertToByteArray(resultTrace, !sharedExe);

        if (!sharedExe) {
            System.out.println(String.format("%n#sub traces: %,d -> %,d (%.2f%%)",
                    counter, bytes.length / 4, -100.00 + 100.0 * (double) (bytes.length / 4) / (double) counter));
        }

        return bytes;
    }

    SingleLinkedIntArrayQueue currentSubTraceSequence = null;

    private SingleLinkedIntArrayQueue processLastSubTrace(
            OutputSequence resultTrace, SingleLinkedIntArrayQueue currentSubTrace,
            int lastNodeType, String[] idToClassNameMap, ISpectra<SourceCodeBlock, K> lineSpectra) {
        Integer lastExecutedStatement = null;
        // remember a directly preceding branch, if any
        if (lastNodeType != CoberturaStatementEncoding.NORMAL_ID) {
            // last processed statement was some sort of branch
            lastExecutedStatement = currentSubTrace.peekLastNoCheck();
        }

        // add current sub trace to pool, if necessary;
        // otherwise just gets the respective sub trace id!
        int id = subTracePool.addSubTraceSequence(currentSubTrace, lineSpectra);
        // reuse the sub trace; the sub trace pool should keep no reference!
        currentSubTrace.clear();

        // we'll add the current sub trace id to the current sequence of sub traces;
        // if there is none, we have to create a new one!
        if (currentSubTraceSequence == null) {
            currentSubTraceSequence = new SingleLinkedIntArrayQueue(30);
        }
        // add the sub trace id to sub trace sequence
        currentSubTraceSequence.add(id);

        if (lastExecutedStatement != null) {
            // add the last executed statement (some branch)
            // to the new sub trace
            currentSubTrace.add(lastExecutedStatement);

            // after a branch, add the current sequence ID to the execution trace!
            addCurrentSubTraceSequenceToTrace(resultTrace);
        }

        // return sub trace for reuse (wouldn't be really necessary)
        return currentSubTrace;
    }

    private void addCurrentSubTraceSequenceToTrace(OutputSequence resultTrace) {
        // since the last statement was a branch, this ends the current sequence of sub traces, too
        int sequenceId = subTraceSequencePool.addSubTraceSequence(currentSubTraceSequence);
        // add the sequence ID to the execution trace!
        resultTrace.append(sequenceId);
        // reset the current sequence
        currentSubTraceSequence = null;
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
                    sharedExe ? sharedExecutionTraceGrammar : null, subTracePool, subTraceSequencePool);
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
