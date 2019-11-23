package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.Node.NodeType;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ClassData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ExecutionTraceCollector;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ProjectData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CoberturaStatementEncoding;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.EfficientCompressedIntegerTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.IntTraceIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.ReplaceableCloneableIntIterator;

public class SimpleIntIndexerCompressed implements SequenceIndexerCompressed {

	// mapping: tree indexer sequence ID -> sequence of sub trace IDs
	private int[][] subTraceIdSequences;
	
	// mapping: sub trace ID -> sequence of spectra node IDs
	private EfficientCompressedIntegerTrace[] nodeIdSequences;
	
	
	public SimpleIntIndexerCompressed(int[][] subTraceIdSequences, EfficientCompressedIntegerTrace[] nodeIdSequences) {
		this.subTraceIdSequences = subTraceIdSequences;
		this.nodeIdSequences = nodeIdSequences;
	}
	
	public SimpleIntIndexerCompressed(
			IntArraySequenceIndexer intArraySequenceIndexer, 
			Map<Integer, EfficientCompressedIntegerTrace> idToSubTraceMap, 
			final ISpectra<SourceCodeBlock, ?> lineSpectra, ProjectData projectData) {
		// map counter IDs to line numbers!
		storeSubTraceIdSequences(Objects.requireNonNull(intArraySequenceIndexer));
		// map counter IDs to line numbers!
		mapCounterIdsToSpectraNodeIds(Objects.requireNonNull(idToSubTraceMap), 
				Objects.requireNonNull(lineSpectra), Objects.requireNonNull(projectData));
	}

	private void storeSubTraceIdSequences(IntArraySequenceIndexer intArraySequenceIndexer) {
		this.subTraceIdSequences = new int[intArraySequenceIndexer.getSequences().length][];
		for (int i = 0; i < intArraySequenceIndexer.getSequences().length; i++) {
			Iterator<Integer> sequenceIterator = intArraySequenceIndexer.getSequenceIterator(i);
			int length = intArraySequenceIndexer.getSequenceLength(i);
			
			subTraceIdSequences[i] = new int[length];
			for (int j = 0; j < length; ++j) {
				subTraceIdSequences[i][j] = sequenceIterator.next();
			}
		}
	}

	private void mapCounterIdsToSpectraNodeIds(Map<Integer, EfficientCompressedIntegerTrace> idToSubTraceMap, 
			final ISpectra<SourceCodeBlock, ?> lineSpectra, ProjectData projectData) {
		String[] idToClassNameMap = Objects.requireNonNull(projectData.getIdToClassNameMap());
		
		// indexer.getSequences() will generate all sequences of sub trace IDs that exist in the GS tree
		this.nodeIdSequences = new EfficientCompressedIntegerTrace[idToSubTraceMap.size()+1];

		// id 0 marks an empty sub trace... should not really happen, but just in case it does... :/
		this.nodeIdSequences[0] = null; //new int[] {};
		for (int i = 1; i < idToSubTraceMap.size() + 1; i++) {
			EfficientCompressedIntegerTrace subTrace = idToSubTraceMap.get(i);
			BufferedIntArrayQueue compressedTrace = subTrace.getCompressedTrace();
			ReplaceableCloneableIntIterator sequenceIterator = compressedTrace.iterator();
			
			EfficientCompressedIntegerTrace traceOfNodeIDs = new EfficientCompressedIntegerTrace(
					compressedTrace.getOutputDir(), "idx_" + compressedTrace.getFilePrefix(), 
					compressedTrace.getNodeSize(), ExecutionTraceCollector.MAP_CHUNK_SIZE, true);
			
			while (sequenceIterator.hasNext()) {
				int encodedStatement = sequenceIterator.next();
				int classId = CoberturaStatementEncoding.getClassId(encodedStatement);
				int counterId = CoberturaStatementEncoding.getCounterId(encodedStatement);
				
				//			 Log.out(true, this, "statement: " + Arrays.toString(statement));
				// TODO store the class names with '.' from the beginning, or use the '/' version?
				String classSourceFileName = idToClassNameMap[classId];
				if (classSourceFileName == null) {
					throw new IllegalStateException("No class name found for class ID: " + classId);
				}
				ClassData classData = projectData.getClassData(classSourceFileName.replace('/', '.'));

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
							traceOfNodeIDs.add(nodeIndex);
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
//							if (nodeType.equals(NodeType.NORMAL)) {
							throw new IllegalStateException("Node not found in spectra: "
									+ classData.getSourceFileName() + ":" + lineNumber[0] 
									+ " from counter id " + counterId + throwAddendum);
//							} else {
//								System.err.println("Node not found in spectra: "
//										+ classData.getSourceFileName() + ":" + lineNumber 
//										+ " from counter id " + statement[1] + throwAddendum);
//							}
						}
					} else {
						throw new IllegalStateException("No line number found for counter ID: " + counterId
								+ " in class: " + classData.getName());
					}
				} else {
					throw new IllegalStateException("Class data for '" + classSourceFileName + "' not found.");
				}
			}
			
//			nodeIdSequences[i] = new int[traceOfNodeIDs.size()];
//			for (int j = 0; j < nodeIdSequences[i].length; ++j) {
//				nodeIdSequences[i][j] = traceOfNodeIDs.remove();
//			}
			
			nodeIdSequences[i] = traceOfNodeIDs;
			
			// delete any stored nodes from disk!
			subTrace.unlock();
			subTrace.clear();
			// sub trace should not be touched anymore...
			subTrace.lock();
			// this would also clear the repetition markers...
//			subTrace.clear();
		}
	}
	
	
//	// for testing purposes
//	public SimpleIntIndexerCompressed(ArraySequenceIndexer<Integer, Integer> subTraceIdSequencesIndexer,
//			ArraySequenceIndexer<Integer, Integer> nodeIdSequencesIndexer) {
//		mapSubTraceIds(Objects.requireNonNull(subTraceIdSequencesIndexer));
//		mapNodeIds(Objects.requireNonNull(nodeIdSequencesIndexer));
//	}
//
//	private void mapSubTraceIds(ArraySequenceIndexer<Integer, Integer> indexer) {
//		this.subTraceIdSequences = new int[indexer.getSequences().length][];
//
//		for (int i = 0; i < indexer.getSequences().length; i++) {
//			Iterator<Integer> sequenceIterator = indexer.getSequenceIterator(i);
//			SingleLinkedArrayQueue<Integer> traceOfNodeIDs = new SingleLinkedArrayQueue<>(100);
//
//			while (sequenceIterator.hasNext()) {
//				Integer statement = sequenceIterator.next();
//
//				traceOfNodeIDs.add(statement);
//			}
//
//			subTraceIdSequences[i] = new int[traceOfNodeIDs.size()];
//			for (int j = 0; j < subTraceIdSequences[i].length; ++j) {
//				subTraceIdSequences[i][j] = traceOfNodeIDs.remove();
//			}
//		}
//
//	}
//	
//	private void mapNodeIds(ArraySequenceIndexer<Integer, Integer> indexer) {
//		this.nodeIdSequences = new int[indexer.getSequences().length][];
//
//		for (int i = 0; i < indexer.getSequences().length; i++) {
//			Iterator<Integer> sequenceIterator = indexer.getSequenceIterator(i);
//			SingleLinkedArrayQueue<Integer> traceOfNodeIDs = new SingleLinkedArrayQueue<>(100);
//
//			while (sequenceIterator.hasNext()) {
//				Integer statement = sequenceIterator.next();
//
//				traceOfNodeIDs.add(statement);
//			}
//
//			nodeIdSequences[i] = new int[traceOfNodeIDs.size()];
//			for (int j = 0; j < nodeIdSequences[i].length; ++j) {
//				nodeIdSequences[i][j] = traceOfNodeIDs.remove();
//			}
//		}
//
//	}
	
//	// for testing purposes
//	public SimpleIntIndexer(ArraySequenceIndexer<int[], IntArrayWrapper> indexer) {
//		// map counter IDs to line numbers!
//		map(Objects.requireNonNull(indexer));
//	}
//
//	private void map(ArraySequenceIndexer<int[], IntArrayWrapper> indexer) {
//		this.nodeIdSequences = new int[indexer.getSequences().length][];
//
//		for (int i = 0; i < indexer.getSequences().length; i++) {
//			Iterator<int[]> sequenceIterator = indexer.getSequenceIterator(i);
//			SingleLinkedArrayQueue<Integer> traceOfNodeIDs = new SingleLinkedArrayQueue<>(100);
//			
//			while (sequenceIterator.hasNext()) {
//				int[] statement = sequenceIterator.next();
//				
//				traceOfNodeIDs.add(statement[0]);
//			}
//			
//			nodeIdSequences[i] = new int[traceOfNodeIDs.size()];
//			for (int j = 0; j < nodeIdSequences[i].length; ++j) {
//				nodeIdSequences[i][j] = traceOfNodeIDs.remove();
//			}
//		}
//		
//	}
	
	public int getNodeIndex(final ISpectra<SourceCodeBlock, ?> lineSpectra, String sourceFilePath, int lineNumber, NodeType nodeType) {
		SourceCodeBlock identifier = new SourceCodeBlock(null, sourceFilePath, null, lineNumber, nodeType);
		INode<SourceCodeBlock> node = lineSpectra.getNode(identifier);
		if (node == null) {
			return -1;
		} else {
			return node.getIndex();
		}
	}

	@Override
	public int[][] getSubTraceIdSequences() {
		return subTraceIdSequences;
	}
	
	@Override
	public EfficientCompressedIntegerTrace[] getNodeIdSequences() {
		return nodeIdSequences;
	}
	
	@Override
	public int[] getSubTraceIdSequence(int index) {
		if (index >= subTraceIdSequences.length) {
			return null;
		}
		return subTraceIdSequences[index];
	}
	
	@Override
	public EfficientCompressedIntegerTrace getNodeIdSequence(int subTraceIndex) {
		if (subTraceIndex >= nodeIdSequences.length) {
			return null;
		}
		return nodeIdSequences[subTraceIndex];
	}
	
//	@Override
//	public Map<GSTreeNode,Integer> getEndNodeToSequenceIdMap() {
//		throw new UnsupportedOperationException();
//	}
	

	@Override
	public Iterator<Integer> getFullSequenceIterator(final int index) {
		// will iterate over (potentially) multiple sub traces, indexed
		// by the sequence of sub trace ids in the specified sequence;
		// will return spectra node ids
		return new Iterator<Integer>() {
			private int outerPos = 0;
			IntTraceIterator subTraceIterator;

			public boolean hasNext() {
				if (subTraceIterator != null && subTraceIterator.hasNext()) {
					return true;
				}
				if (index >= subTraceIdSequences.length) {
					throw new IllegalStateException("index out of bounds: " + index);
				}
				if (outerPos < subTraceIdSequences[index].length) {
					if (nodeIdSequences == null) {
						throw new IllegalStateException("meh");
					}
					subTraceIterator = nodeIdSequences[subTraceIdSequences[index][outerPos++]].iterator();
//					System.out.println(nodeIdSequences[subTraceIdSequences[index][outerPos-1]].toString());
					return hasNext();
				}
				return false;
			}

			public Integer next() {
				return subTraceIterator.next();
			}
		};
	}
	
	@Override
	public IntTraceIterator getNodeIdSequenceIterator(final int index) {
		// will iterate over the subtrace with the specified index
		return nodeIdSequences[index].iterator();
	}
	
	@Override
	public Iterator<Integer> getSubTraceIDSequenceIterator(final int index) {
		// will iterate over the subtrace id sequence with the specified index
		return new Iterator<Integer>() {
            private int pos = 0;

            public boolean hasNext() {
               return pos < subTraceIdSequences[index].length;
            }

            public Integer next() {
               return subTraceIdSequences[index][pos++];
            }
        };
	}

	@Override
	public void removeFromSequences(int nodeId) {
		// iterate over all sub traces
		// TODO: sub trace with id 0 is the empty sub trace. Should not exist, regularly
		for (int i = 1; i < nodeIdSequences.length; i++) {
			EfficientCompressedIntegerTrace sequence = nodeIdSequences[i];
			ReplaceableCloneableIntIterator iterator = sequence.baseIterator();
			boolean found = false;
			while (iterator.hasNext()) {
				if (iterator.next() == nodeId) {
					// sequence contains the node at least once
                    found = true;
                    break;
				}
			}
			if (found) {
				// sequence contains the node, so generate a new sequence and replace the old
				IntTraceIterator fullIterator = sequence.iterator();
				BufferedIntArrayQueue compressedTrace = sequence.getCompressedTrace();
				EfficientCompressedIntegerTrace newSequence = new EfficientCompressedIntegerTrace(
						compressedTrace.getOutputDir(), "_" + compressedTrace.getFilePrefix(), 
						compressedTrace.getNodeSize(), compressedTrace.getNodeSize(), true);
				while (fullIterator.hasNext()) {
					int next = fullIterator.next();
					if (next != nodeId) {
						newSequence.add(next);
					}
				}
				nodeIdSequences[i] = newSequence;
			}
		}
	}

//	@Override
//	public int[] getSequenceForEndNode(GSTreeNode endNode) {
//		throw new UnsupportedOperationException();
//	}
	
}
