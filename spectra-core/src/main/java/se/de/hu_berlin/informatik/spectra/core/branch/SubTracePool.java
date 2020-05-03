package se.de.hu_berlin.informatik.spectra.core.branch;

import se.de.hu_berlin.informatik.spectra.core.*;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ClassData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ProjectData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CoberturaStatementEncoding;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.SingleLinkedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.util.CachedIntArrayMap;
import se.de.hu_berlin.informatik.spectra.util.CachedMap;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SubTracePool {

    private ProjectData projectData;

    public SubTracePool(Path tempOutputDir) {
        this.existingSubTraces = new CachedIntArrayMap(tempOutputDir.resolve("nodeIdSequences.zip"),
                0, SpectraFileUtils.NODE_ID_SEQUENCES_DIR, true);
    }

    // maps encoded sub trace representations (long) to unique integer IDs
    private final Map<Long, Integer> idToSubtraceIdMap = new HashMap<>();

    // maps unique sub trace IDs (int) to sequences of spectra node IDs
    private final CachedMap<int[]> existingSubTraces;

    private int currentId = 0;

    public int addSubTraceSequence(SingleLinkedIntArrayQueue subTraceToCheck, ISpectra<SourceCodeBlock, ?> lineSpectra) {
        // get a representation id for the subtrace (unique for sub traces that start and end within the same method!)
        long subTraceId = CoberturaStatementEncoding.generateRepresentationForSubTrace(subTraceToCheck);
        Integer id = idToSubtraceIdMap.get(subTraceId);

        if (id == null) {
            // first time seeing this sub trace!
            // starts with id 1
            id = ++currentId;

            // add id to the map
            idToSubtraceIdMap.put(subTraceId, id);

            int subTraceLength = subTraceToCheck.size();
            // using integer arrays
            int[] subTrace = new int[subTraceLength];
            int j = 0;
            while (!subTraceToCheck.isEmpty()) {
                // convert to the actual spectra node indices
                subTrace[j++] = getNodeIndexForCounter(subTraceToCheck.removeNoAutoBoxing(), lineSpectra);
            }
            // add sub trace to the list of existing sub traces (together with the id)
            existingSubTraces.put(id, subTrace);
        }

        return id;
    }

    public int getID(SingleLinkedIntArrayQueue subTrace, ISpectra<SourceCodeBlock, ?> lineSpectra) {
        // get a representation id for the subtrace (unique for sub traces that start and end within the same method!)
        long subTraceId = CoberturaStatementEncoding.generateRepresentationForSubTrace(subTrace);
        Integer id = idToSubtraceIdMap.get(subTraceId);
        if (id == null) {
            id = addSubTraceSequence(subTrace, lineSpectra);
        }
        return id;
    }


    private int getNodeIndexForCounter(int encodedStatement, ISpectra<SourceCodeBlock, ?> lineSpectra) {
        int classId = CoberturaStatementEncoding.getClassId(encodedStatement);
        int counterId = CoberturaStatementEncoding.getCounterId(encodedStatement);

        //			 Log.out(true, this, "statement: " + Arrays.toString(statement));
        // TODO store the class names with '.' from the beginning, or use the '/' version?
        String classSourceFileName = projectData.getIdToClassNameMap()[classId];
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

            Node.NodeType nodeType = Node.NodeType.NORMAL;
            switch (specialIndicatorId) {
                case CoberturaStatementEncoding.SWITCH_ID:
                    nodeType = Node.NodeType.SWITCH_BRANCH;
                    break;
                case CoberturaStatementEncoding.BRANCH_ID:
                    nodeType = Node.NodeType.FALSE_BRANCH;
                    break;
                case CoberturaStatementEncoding.JUMP_ID:
                    nodeType = Node.NodeType.TRUE_BRANCH;
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

    private int getNodeIndex(final ISpectra<SourceCodeBlock, ?> lineSpectra, String sourceFilePath, int lineNumber, Node.NodeType nodeType) {
        SourceCodeBlock identifier = new SourceCodeBlock(null, sourceFilePath, null, lineNumber, nodeType);
        INode<SourceCodeBlock> node = lineSpectra.getNode(identifier);
        if (node == null) {
            return -1;
        } else {
            return node.getIndex();
        }
    }

    public CachedMap<int[]> getExistingSubTraces() {
        return existingSubTraces;
    }

	public void setProjectData(ProjectData projectData) {
		this.projectData = projectData;
	}
}
