package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CoberturaStatementEncoding;

import java.util.*;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * <p>
 * ProjectData information is typically serialized to a file. An
 * instance of this class records coverage information for a single
 * class that has been instrumented.
 * </p>
 */

@CoverageIgnore
public class ClassData extends CoverageDataContainer
        implements
        Comparable<ClassData> {
    private static final Logger logger = LoggerFactory.getLogger(ClassData.class);
    private static final long serialVersionUID = 5;

//	private Map<String, Set<CoverageData>> coverageMap = new HashMap<>();

    /**
     * Each key is a line number in this class, stored as an Integer object.
     * Each value is information about the line, stored as a LineData object.
     */
    private Map<Integer, LineData> branches = new HashMap<Integer, LineData>();

//	/**
//	 * Each key is a counter Id (array index), stored as an Integer object.
//	 * Each value is the respective line number in the class.
//	 */
//	protected Map<Integer, Integer> counterIdToLineNumberMap = new HashMap<Integer, Integer>();
//
//	public Map<Integer, Integer> getCounterIdToLineNumberMap() {
//		return counterIdToLineNumberMap;
//	}

    protected int[][] counterId2LineNumbers;

    public void setCounterId2LineNumbers(int[][] counterIDs2LineNumbers) {
        this.counterId2LineNumbers = counterIDs2LineNumbers;
    }

    /**
     * @return array that maps counter IDs to actual line numbers;
     * will be set once (and only) during instrumentation and stored in data file
     */
    public int[][] getCounterId2LineNumbers() {
        return counterId2LineNumbers;
    }

//	/**
//	 * Each key is a line number, stored as an Integer object.
//	 * Each value is information about the line, stored as a LineData object.
//	 */
//	private Map<Integer, LineData> lineNumberToLineMap = new HashMap<Integer, LineData>();
//
//	public Map<Integer, LineData> getLineNumberToLineDataMap() {
//		return lineNumberToLineMap;
//	}

    private boolean containsInstrumentationInfo = false;

    private final Set<String> methodNamesAndDescriptors = new HashSet<>();

    private String name = null;

    private String sourceFileName = null;

    private final int classId;

    public int getClassId() {
        return classId;
    }

//	public ClassData() {
//	}

    /**
     * @param name    In the format "net.sourceforge.cobertura.coveragedata.ClassData"
     * @param classId unique id for this class
     */
    public ClassData(String name, int classId) {
        this.classId = classId;
        if (classId > Math.pow(2, CoberturaStatementEncoding.CLASS_ID_BITS) - 1) {
            throw new IllegalStateException("Class ID too high! Encoding error: " + classId);
        }
        if (name == null)
            throw new IllegalArgumentException("Class name must be specified.");
        this.name = name;
    }

    public LineData addLine(int lineNumber, String methodName,
                            String methodDescriptor) {
        lock.lock();
        try {
            LineData lineData = getLineData(lineNumber);
            if (lineData == null) {
                lineData = new LineData(lineNumber);
                // Each key is a line number in this class, stored as an Integer object.
                // Each value is information about the line, stored as a LineData object.
                children.put(lineNumber, lineData);

                // methodName and methodDescriptor can be null when cobertura.ser with
                // no line information was loaded (or was not loaded at all).
                if (methodName != null && methodDescriptor != null) {
                    setMethodNameAndDescriptor(lineData, methodName, methodDescriptor);
                }
            } else if (methodName != null && methodDescriptor != null) {
                if (lineData.getMethodName() == null) {
                    setMethodNameAndDescriptor(lineData, methodName, methodDescriptor);
                }
            }
            return lineData;
        } finally {
            lock.unlock();
        }
    }

    public LineData addLineWithNoMethodName(int lineNumber) {
        lock.lock();
        try {
            LineData lineData = getLineData(lineNumber);
            if (lineData == null) {
                lineData = new LineData(lineNumber);
                // Each key is a line number in this class, stored as an Integer object.
                // Each value is information about the line, stored as a LineData object.
                children.put(lineNumber, lineData);
            }
            return lineData;
        } finally {
            lock.unlock();
        }
    }

    private void setMethodNameAndDescriptor(LineData lineData, String methodName, String methodDescriptor) {
        String methodNameAndDescriptor = methodName + methodDescriptor;
//		Set<CoverageData> set = coverageMap.get(methodNameAndDescriptor);
//		if (set == null) {
//			set = new HashSet<>();
//			coverageMap.put(methodNameAndDescriptor, set);
//		}
//		set.add(lineData);

        methodNamesAndDescriptors.add(methodNameAndDescriptor);
        lineData.setMethodNameAndDescriptor(methodName, methodDescriptor);
    }

    /*
     * This is required because we implement Comparable.
     */
    public int compareTo(ClassData o) {
        if (!o.getClass().equals(ClassData.class))
            return Integer.MAX_VALUE;
        return this.name.compareTo(((ClassData) o).name);
    }

    public boolean containsInstrumentationInfo() {
        lock.lock();
        try {
            return this.containsInstrumentationInfo;
        } finally {
            lock.unlock();
        }
    }

    /*
     * Returns true if the given object is an instance of the
     * ClassData class, and it contains the same data as this
     * class.
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || !(obj.getClass().equals(this.getClass())))
            return false;

        ClassData classData = (ClassData) obj;
        getBothLocks(classData);
        try {
            return super.equals(obj)
                    && this.classId == classData.classId
//					&& this.counterId2LineNumbers.equals(classData.counterId2LineNumbers)
//					&& this.branches.equals(classData.branches)
                    && this.methodNamesAndDescriptors
                    .equals(classData.methodNamesAndDescriptors)
                    && this.name.equals(classData.name)
                    && this.sourceFileName.equals(classData.sourceFileName);
        } finally {
            lock.unlock();
            classData.lock.unlock();
        }
    }

    public String getBaseName() {
        int lastDot = this.name.lastIndexOf('.');
        if (lastDot == -1) {
            return this.name;
        }
        return this.name.substring(lastDot + 1);
    }

//	/*
//	 * @return The branch coverage rate for a particular method.
//	 */
//	public double getBranchCoverageRate(String methodNameAndDescriptor) {
//		int total = 0;
//		int covered = 0;
//
//		lock.lock();
//		try {
//			for (Iterator<LineData> iter = branches.values().iterator(); iter
//					.hasNext();) {
//				LineData next = (LineData) iter.next();
//				if (methodNameAndDescriptor.equals(next.getMethodName()
//						+ next.getMethodDescriptor())) {
//					total += next.getNumberOfValidBranches();
//					covered += next.getNumberOfCoveredBranches();
//				}
//			}
//			if (total == 0)
//				return 1.0;
//			return (double) covered / total;
//		} finally {
//			lock.unlock();
//		}
//	}

//	public Collection<Integer> getBranches() {
//		lock.lock();
//		try {
//			return Collections.unmodifiableCollection(branches.keySet());
//		} finally {
//			lock.unlock();
//		}
//	}

    /**
     * @param lineNumber The source code line number.
     * @return The coverage of the line
     */
    public LineData getLineCoverage(int lineNumber) {
        lock.lock();
        try {
            if (!children.containsKey(lineNumber)) {
                return null;
            }

            return (LineData) children.get(lineNumber);
        } finally {
            lock.unlock();
        }
    }

    /*
     * @return The line coverage rate for particular method
     */
    public double getLineCoverageRate(String methodNameAndDescriptor) {
        int total = 0;
        int hits = 0;

        lock.lock();
        try {
            for (CoverageData coverageData : children.values()) {
                LineData next = (LineData) coverageData;
                if (methodNameAndDescriptor.equals(next.getMethodName()
                        + next.getMethodDescriptor())) {
                    total++;
                    if (next.getHits() > 0) {
                        hits++;
                    }
                }
            }
            if (total == 0)
                return 1d;
            return (double) hits / total;
        } finally {
            lock.unlock();
        }
    }

    public LineData getLineData(int lineNumber) {
        lock.lock();
        try {
            return (LineData) children.get(lineNumber);
        } finally {
            lock.unlock();
        }
    }

    public SortedSet<CoverageData> getLines() {
        lock.lock();
        try {
            return new TreeSet<>(this.children.values());
        } finally {
            lock.unlock();
        }
    }

    public Collection<CoverageData> getLines(String methodNameAndDescriptor) {
        Collection<CoverageData> lines = new HashSet<>();
        lock.lock();
        try {
            for (CoverageData coverageData : children.values()) {
                LineData next = (LineData) coverageData;
                if (methodNameAndDescriptor.equals(next.getMethodName()
                        + next.getMethodDescriptor())) {
                    lines.add(next);
                }
            }
            return lines;
//			Set<CoverageData> set = coverageMap.get(methodNameAndDescriptor);
//			return set == null ? new ArrayList<CoverageData>(0) : set;
        } finally {
            lock.unlock();
        }
    }

    public String getMethodNameAndDescriptor(int lineNumber) {
        lock.lock();
        try {
            LineData next = (LineData) children.get(lineNumber);
            return next.getMethodName() + next.getMethodDescriptor();
        } finally {
            lock.unlock();
        }
    }

    public String getMethodName(int lineNumber) {
        lock.lock();
        try {
            LineData next = (LineData) children.get(lineNumber);
            return next.getMethodName();
        } finally {
            lock.unlock();
        }
    }

    /**
     * @return The method name and descriptor of each method found in the
     * class represented by this instrumentation.
     */
    public Set<String> getMethodNamesAndDescriptors() {
        lock.lock();
        try {
            return methodNamesAndDescriptors;
        } finally {
            lock.unlock();
        }
    }

    public String getName() {
        return name;
    }

//	/**
//	 * @return The number of branches in this class.
//	 */
//	public int getNumberOfValidBranches() {
//		int number = 0;
//		lock.lock();
//		try {
//			for (Iterator<LineData> i = branches.values().iterator(); i
//					.hasNext(); number += ((LineData) i.next())
//					.getNumberOfValidBranches());
//			return number;
//		} finally {
//			lock.unlock();
//		}
//	}
//
//	/**
//	 * see CoverageData#getNumberOfCoveredBranches()
//	 */
//	public int getNumberOfCoveredBranches() {
//		int number = 0;
//		lock.lock();
//		try {
//			for (Iterator<LineData> i = branches.values().iterator(); i
//					.hasNext(); number += ((LineData) i.next())
//					.getNumberOfCoveredBranches());
//			return number;
//		} finally {
//			lock.unlock();
//		}
//	}

    public String getPackageName() {
        int lastDot = this.name.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return this.name.substring(0, lastDot);
    }

    /**
     * Return the name of the file containing this class.  If this
     * class' sourceFileName has not been set (for whatever reason)
     * then this method will attempt to infer the name of the source
     * file using the class name.
     *
     * @return The name of the source file, for example
     * net/sourceforge/cobertura/coveragedata/ClassData.java
     */
    public String getSourceFileName() {
        String baseName;
        lock.lock();
        try {
            if (sourceFileName != null)
                baseName = sourceFileName;
            else {
                baseName = getBaseName();
                int firstDollarSign = baseName.indexOf('$');
                if (firstDollarSign == -1 || firstDollarSign == 0)
                    baseName += ".java";
                else
                    baseName = baseName.substring(0, firstDollarSign) + ".java";
            }

            String packageName = getPackageName();
            if (packageName.equals(""))
                return baseName;
            return packageName.replace('.', '/') + '/' + baseName;
        } finally {
            lock.unlock();
        }
    }

    public int hashCode() {
        return this.name.hashCode();
    }

//	/*
//	 * @return True if the line contains at least one condition jump (branch)
//	 */
//	public boolean hasBranch(int lineNumber) {
//		lock.lock();
//		try {
//			return branches.containsKey(Integer.valueOf(lineNumber));
//		} finally {
//			lock.unlock();
//		}
//	}

    /*
     * Determine if a given line number is a valid line of code.
     *
     * @return True if the line contains executable code.  False
     *         if the line is empty, or a comment, etc.
     */
    public boolean isValidSourceLineNumber(int lineNumber) {
        lock.lock();
        try {
            return children.containsKey(lineNumber);
        } finally {
            lock.unlock();
        }
    }

    public void addLineJump(int lineNumber, int branchNumber) {
        lock.lock();
        try {
            LineData lineData = getLineData(lineNumber);
            if (lineData != null) {
                lineData.addJump(branchNumber);
                this.branches.put(Integer.valueOf(lineNumber), lineData);
            } else {
                logger.error("jump: no line data for line " + lineNumber + ", branch: " + branchNumber);
            }
        } finally {
            lock.unlock();
        }
    }

    public void addLineSwitch(int lineNumber, int switchNumber, int min,
                              int max, int maxBranches) {
        lock.lock();
        try {
            LineData lineData = getLineData(lineNumber);
            if (lineData != null) {
                lineData.addSwitch(switchNumber, min, max, maxBranches);
                this.branches.put(Integer.valueOf(lineNumber), lineData);
            } else {
                logger.error("switch: no line data for line " + lineNumber + ", switch: " + switchNumber);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Merge some existing instrumentation with this instrumentation.
     *
     * @param coverageData Some existing coverage data.
     */
    public void merge(CoverageData coverageData) {
        ClassData classData = (ClassData) coverageData;

        // If objects contain data for different classes then don't merge
        if (!this.getName().equals(classData.getName()))
            return;

        getBothLocks(classData);
        try {
            super.merge(coverageData);

            // We can't just call this.branches.putAll(classData.branches);
            // Why not?  If we did a putAll, then the LineData objects from
            // the coverageData class would overwrite the LineData objects
            // that are already in "this.branches"  And we don't need to
            // update the LineData objects that are already in this.branches
            // because they are shared between this.branches and this.children,
            // so the object hit counts will be moved when we called
            // super.merge() above.
            for (Iterator<Integer> iter = classData.branches.keySet()
                    .iterator(); iter.hasNext(); ) {
                Integer key = iter.next();
                if (!this.branches.containsKey(key)) {
                    this.branches.put(key, classData.branches.get(key));
                }
            }

//			this.coverageMap.putAll(classData.coverageMap);
            if (this.counterId2LineNumbers == null) {
                this.counterId2LineNumbers = classData.counterId2LineNumbers;
            }

            if (this.counterId2LineNumbers != null &&
                    this.counterId2LineNumbers.length > Math.pow(2, CoberturaStatementEncoding.COUNTER_ID_BITS)) {
                throw new IllegalStateException("Counter ID too high! Encoding error: " + (this.counterId2LineNumbers.length - 1));
            }

            if (this.classId > Math.pow(2, CoberturaStatementEncoding.CLASS_ID_BITS) - 1) {
                throw new IllegalStateException("Class ID too high! Encoding error: " + this.classId);
            }

//			System.out.println("max counter ID: " + (this.counterId2LineNumbers.length - 1) + ", class ID: " + this.classId);

//			this.counterIdToLineNumberMap.putAll(classData.counterIdToLineNumberMap);
            this.containsInstrumentationInfo |= classData.containsInstrumentationInfo;
            this.methodNamesAndDescriptors.addAll(classData
                    .getMethodNamesAndDescriptors());
            if (classData.sourceFileName != null)
                this.sourceFileName = classData.sourceFileName;
        } finally {
            lock.unlock();
            classData.lock.unlock();
        }
    }

    public void removeLine(int lineNumber) {
        Integer lineObject = lineNumber;
        lock.lock();
        try {
            children.remove(lineObject);
//			branches.remove(lineObject);
        } finally {
            lock.unlock();
        }
    }

    public void setContainsInstrumentationInfo() {
        lock.lock();
        try {
            this.containsInstrumentationInfo = true;
        } finally {
            lock.unlock();
        }
    }

    public void setSourceFileName(String sourceFileName) {
        lock.lock();
        try {
            this.sourceFileName = sourceFileName;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Increment the number of hits for a particular line of code.
     *
     * @param lineNumber the line of code to increment the number of hits.
     * @param hits       how many times the piece was called
     */
    public void touch(int lineNumber, int hits) {
        lock.lock();
        try {
            LineData lineData = getLineData(lineNumber);
            if (lineData == null)
                lineData = addLine(lineNumber, null, null);
            lineData.touch(hits);
        } finally {
            lock.unlock();
        }
    }

    public void reset() {
        lock.lock();
        try {
            this.methodNamesAndDescriptors.clear();
            this.children.clear();
        } finally {
            lock.unlock();
        }
    }

//	/**
//	 * Increments the number of hits for particular hit counter of particular branch on particular line number.
//	 *
//	 * @param lineNumber   The line of code where the branch is
//	 * @param branchNumber The branch on the line to change the hit counter
//	 * @param branch       The hit counter (true or false)
//	 * @param hits         how many times the piece was called
//	 */
//	public void touchJump(int lineNumber, int branchNumber, boolean branch,
//			int hits) {
//		lock.lock();
//		try {
//			LineData lineData = getLineData(lineNumber);
//			if (lineData == null)
//				lineData = addLine(lineNumber, null, null);
//			lineData.touchJump(branchNumber, branch, hits);
//		} finally {
//			lock.unlock();
//		}
//	}
//
//	/**
//	 * Increments the number of hits for particular hit counter of particular switch branch on particular line number.
//	 *
//	 * @param lineNumber   The line of code where the branch is
//	 * @param switchNumber The switch on the line to change the hit counter
//	 * @param branch       The hit counter
//	 * @param hits         how many times the piece was called
//	 */
//	public void touchSwitch(int lineNumber, int switchNumber, int branch,
//			int hits) {
//		lock.lock();
//		try {
//			LineData lineData = getLineData(lineNumber);
//			if (lineData == null)
//				lineData = addLine(lineNumber, null, null);
//			lineData.touchSwitch(switchNumber, branch, hits);
//		} finally {
//			lock.unlock();
//		}
//	}

}