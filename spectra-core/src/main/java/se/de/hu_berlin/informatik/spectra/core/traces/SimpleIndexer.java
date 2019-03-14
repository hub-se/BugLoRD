package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ClassData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.IntArrayWrapper;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ProjectData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedArrayQueue;

public class SimpleIndexer implements SequenceIndexer {

	// array of all existing sequences
	private int[][] sequences;
	
	
	public SimpleIndexer(int[][] sequences) {
		this.sequences = sequences;
	}
	
	public SimpleIndexer(ArraySequenceIndexer<int[], IntArrayWrapper> indexer, 
			final ISpectra<SourceCodeBlock, ?> lineSpectra, ProjectData projectData) {
		// map counter IDs to line numbers!
		map(Objects.requireNonNull(indexer), 
				Objects.requireNonNull(lineSpectra), Objects.requireNonNull(projectData));
	}

	private void map(ArraySequenceIndexer<int[], IntArrayWrapper> indexer, 
			final ISpectra<SourceCodeBlock, ?> lineSpectra, ProjectData projectData) {
		String[] idToClassNameMap = Objects.requireNonNull(projectData.getIdToClassNameMap());
		
		this.sequences = new int[indexer.getSequences().length][];

		for (int i = 0; i < indexer.getSequences().length; i++) {
			Iterator<int[]> sequenceIterator = indexer.getSequenceIterator(i);
			SingleLinkedArrayQueue<Integer> traceOfNodeIDs = new SingleLinkedArrayQueue<>(100);
			
			while (sequenceIterator.hasNext()) {
				int[] statement = sequenceIterator.next();
				//			 Log.out(true, this, "statement: " + Arrays.toString(statement));
				// TODO store the class names with '.' from the beginning, or use the '/' version?
				String classSourceFileName = idToClassNameMap[statement[0]];
				if (classSourceFileName == null) {
					throw new IllegalStateException("No class name found for class ID: " + statement[0]);
				}
				ClassData classData = projectData.getClassData(classSourceFileName.replace('/', '.'));

				if (classData != null) {
					if (classData.getCounterId2LineNumbers() == null) {
						throw new IllegalStateException("No counter ID to line number map for class " + classSourceFileName);
					}
					int lineNumber = classData.getCounterId2LineNumbers()[statement[1]];

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

					// the array is initially set to -1 to indicate counter IDs that were not set, if any
					if (lineNumber >= 0) {
						int nodeIndex = getNodeIndex(lineSpectra, classData.getSourceFileName(), lineNumber);
						if (nodeIndex >= 0) {
							traceOfNodeIDs.add(nodeIndex);
						} else {
							String throwAddendum = "";
							if (statement.length > 2) {
								switch (statement[2]) {
								case 0:
									throwAddendum = " (from branch)";
									break;
								case 1:
									throwAddendum = " (after jump)";
									break;
								case 2:
									throwAddendum = " (after switch label)";
									break;
								default:
									throwAddendum = " (unknown)";
								}
							}
							throw new IllegalStateException("Node not found in spectra: "
									+ classData.getSourceFileName() + ":" + lineNumber 
									+ " from counter id " + statement[1] + throwAddendum);
						}
					} else if (statement.length <= 2 || statement[2] != 0) {
						// disregard counter ID 0 if it comes from an internal variable (fake jump?!)
						// this should actually not be an issue anymore!
						throw new IllegalStateException("No line number found for counter ID: " + statement[1]
								+ " in class: " + classData.getName());
					} else {
						throw new IllegalStateException("No line number found for counter ID: " + statement[1]
								+ " in class: " + classData.getName());
					}
				} else {
					throw new IllegalStateException("Class data for '" + classSourceFileName + "' not found.");
				}
			}
			
			sequences[i] = new int[traceOfNodeIDs.size()];
			for (int j = 0; j < sequences[i].length; ++j) {
				sequences[i][j] = traceOfNodeIDs.remove();
			}
		}
	}
	
	
	// for testing purposes
	public SimpleIndexer(ArraySequenceIndexer<int[], IntArrayWrapper> indexer) {
		// map counter IDs to line numbers!
		map(Objects.requireNonNull(indexer));
	}

	private void map(ArraySequenceIndexer<int[], IntArrayWrapper> indexer) {
		this.sequences = new int[indexer.getSequences().length][];

		for (int i = 0; i < indexer.getSequences().length; i++) {
			Iterator<int[]> sequenceIterator = indexer.getSequenceIterator(i);
			SingleLinkedArrayQueue<Integer> traceOfNodeIDs = new SingleLinkedArrayQueue<>(100);
			
			while (sequenceIterator.hasNext()) {
				int[] statement = sequenceIterator.next();
				
				traceOfNodeIDs.add(statement[0]);
			}
			
			sequences[i] = new int[traceOfNodeIDs.size()];
			for (int j = 0; j < sequences[i].length; ++j) {
				sequences[i][j] = traceOfNodeIDs.remove();
			}
		}
		
	}
	
	public int getNodeIndex(final ISpectra<SourceCodeBlock, ?> lineSpectra, String sourceFilePath, int lineNumber) {
		SourceCodeBlock identifier = new SourceCodeBlock(null, sourceFilePath, null, lineNumber);
		INode<SourceCodeBlock> node = lineSpectra.getNode(identifier);
		if (node == null) {
			return -1;
		} else {
			return node.getIndex();
		}
	}

	@Override
	public int[][] getMappedSequences() {
		return sequences;
	}
	
	@Override
	public int[] getSequence(int index) {
		if (index >= sequences.length) {
			return new int[] {};
		}
		return sequences[index];
	}
	
//	@Override
//	public Map<GSTreeNode,Integer> getEndNodeToSequenceIdMap() {
//		throw new UnsupportedOperationException();
//	}
	
	@Override
	public int getSequenceIdForEndNode(GSTreeNode endNode) {
		throw new UnsupportedOperationException();
	}

	@Override
	public GSTreeNode[][] getSequences() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Integer> getSequenceIterator(final int index) {
		return new Iterator<Integer>() {
            private int pos = 0;

            public boolean hasNext() {
               return pos < sequences[index].length;
            }

            public Integer next() {
               return sequences[index][pos++];
            }
        };
	}

	@Override
	public void removeFromSequences(int element) {
		// iterate over all sequences
		for (int i = 0; i < sequences.length; i++) {
			int[] sequence = sequences[i];
			boolean found = false;
            for (int i2 : sequence) {
                if (i2 == element) {
                    // sequence contains the node at least once
                    found = true;
                    break;
                }
            }
			if (found) {
				// sequence contains the node, so generate a new sequence and replace the old
				List<Integer> newSequence = new ArrayList<>(sequence.length - 1);
                for (int i1 : sequence) {
                    if (i1 != element) {
                        newSequence.add(i1);
                    }
                }
				sequences[i] = newSequence.stream().mapToInt(k -> k).toArray();
			}
		}
	}
	
//	@Override
//	public int[] getSequenceForEndNode(GSTreeNode endNode) {
//		throw new UnsupportedOperationException();
//	}
	
}
