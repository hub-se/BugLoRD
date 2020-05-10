package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageData;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;


public class LockableProjectData extends ProjectData {

    /**
     *
     */
    private static final long serialVersionUID = 8484242021027071646L;
//	private boolean locked = false;
//
//	public void lock() {
//		locked  = true;
//	}
//	
//	@Override
//	public void addClassData(ClassData classData) {
//		if (locked) {
//			return;
//		}
//		super.addClassData(classData);
//	}
//
//	@Override
//	public ClassData getOrCreateClassData(String name) {
//		if (locked) {
//			return null;
//		}
//		lock.lock();
//		try {
//			ClassData classData = getClassData(name);
//			if (classData == null) {
//				classData = new ClassData(name);
//				addClassData(classData);
//			}
//			return classData;
//		} finally {
//			lock.unlock();
//		}
//	}

    public boolean subtract(ProjectData projectData) {
        // loop over all packages
        SortedSet<CoverageData> packages = this.getPackages();
        SortedSet<CoverageData> packagesOther = projectData.getPackages();
        for (CoverageData coverageData3 : packagesOther) {
            PackageData packageDataOther = (PackageData) coverageData3;

            PackageData foundPackageData = null;
            for (CoverageData packageDataCov : packages) {
                PackageData packageData = (PackageData) packageDataCov;
                if (packageData.getName().equals(packageDataOther.getName())) {
                    foundPackageData = packageData;
                    break;
                }
            }
            if (foundPackageData == null) {
//				Log.err(this, "Subtraction: Package '%s' does not exist.", packageDataOther.getName());
                return false;
            }

            // loop over all classes of the package
            Collection<SourceFileData> sourceFiles = foundPackageData.getSourceFiles();
            Collection<SourceFileData> sourceFilesOther = packageDataOther.getSourceFiles();
            for (SourceFileData fileDataOther : sourceFilesOther) {
                SourceFileData foundFileData = null;
                for (SourceFileData fileData : sourceFiles) {
                    if (fileData.getName().equals(fileDataOther.getName())) {
                        foundFileData = fileData;
                        break;
                    }
                }
                if (foundFileData == null) {
//					Log.err(this, "Subtraction: Source file '%s' does not exist.", fileDataOther.getName());
                    return false;
                }

                SortedSet<CoverageData> classes = foundFileData.getClasses();
                SortedSet<CoverageData> classesOther = fileDataOther.getClasses();
                for (CoverageData coverageData2 : classesOther) {
                    ClassData classDataOther = (ClassData) coverageData2;

                    ClassData foundClassData = null;
                    for (CoverageData classDataCov : classes) {
                        ClassData classData = (ClassData) classDataCov;
                        if (classData.getName().equals(classDataOther.getName())) {
                            foundClassData = classData;
                            break;
                        }
                    }
                    if (foundClassData == null) {
//						Log.err(this, "Subtraction: Class '%s' does not exist.", classDataOther.getName());
                        return false;
                    }

                    // loop over all methods of the class
                    SortedSet<String> sortedMethods = new TreeSet<>(foundClassData.getMethodNamesAndDescriptors());
                    SortedSet<String> sortedMethodsOther = new TreeSet<>(classDataOther.getMethodNamesAndDescriptors());
                    for (String methodNameAndSigOther : sortedMethodsOther) {
                        String foundMethodNameAndSig = null;
                        for (String methodNameAndSig : sortedMethods) {
                            if (methodNameAndSig.equals(methodNameAndSigOther)) {
                                foundMethodNameAndSig = methodNameAndSig;
                                break;
                            }
                        }
                        if (foundMethodNameAndSig == null) {
//							Log.err(this, "Subtraction: Method '%s' does not exist.", methodNameAndSigOther);
                            return false;
                        }

                        // loop over all lines of the method
                        SortedSet<CoverageData> sortedLines = new TreeSet<>(foundClassData.getLines(foundMethodNameAndSig));
                        SortedSet<CoverageData> sortedLinesOther = new TreeSet<>(classDataOther.getLines(methodNameAndSigOther));
                        for (CoverageData coverageData1 : sortedLinesOther) {
                            LineData lineDataOther = (LineData) coverageData1;
                            if (!lineDataOther.isCovered()) {
                                continue;
                            }

                            LineData foundLineData = null;
                            for (CoverageData coverageData : sortedLines) {
                                LineData lineWrapper = (LineData) coverageData;
                                if (lineWrapper.getLineNumber() == lineDataOther.getLineNumber()) {
                                    foundLineData = lineWrapper;
                                    break;
                                }
                            }
                            if (foundLineData == null) {
//								Log.err(this, "Subtraction: Line '%s' does not exist in method '%s'.", lineDataOther.getLineNumber(), methodNameAndSigOther);
                                return false;
                            }

                            if (foundLineData.getHits() - lineDataOther.getHits() < 0) {
//								Log.err(this, "Subtraction: line hits would be negative after subtraction for method '%s', line %d.", methodNameAndSigOther, lineDataOther.getLineNumber());
                                return false;
                            }

                            if (!foundLineData.setHits(foundLineData.getHits() - lineDataOther.getHits())) {
//								Log.err(this, "Subtraction: line hits could not be set for method '%s', line %d.", methodNameAndSigOther, lineDataOther.getLineNumber());
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public static String projectDataToString(ProjectData projectData, boolean onlyUseCovered) {
        StringBuilder builder = new StringBuilder();

        // loop over all packages
        SortedSet<CoverageData> packages = projectData.getPackages();
        for (CoverageData aPackage : packages) {
            boolean packageWasCovered = false;
            PackageData packageData = (PackageData) aPackage;
            StringBuilder nextPackage = new StringBuilder(packageData.getName() + System.lineSeparator());

            // loop over all classes of the package
            Collection<SourceFileData> sourceFiles = packageData.getSourceFiles();
            for (SourceFileData sourceFile : sourceFiles) {
                boolean fileWasCovered = false;
                SourceFileData fileData = sourceFile;
                StringBuilder nextFile = new StringBuilder("  " + fileData.getName() + System.lineSeparator());

                SortedSet<CoverageData> classes = fileData.getClasses();
                for (CoverageData aClass : classes) {
                    boolean classWasCovered = false;
                    ClassData classData = (ClassData) aClass;
                    StringBuilder nextClass = new StringBuilder("    " + classData.getName() + System.lineSeparator());

                    // loop over all methods of the class
                    SortedSet<String> sortedMethods = new TreeSet<>(classData.getMethodNamesAndDescriptors());
                    for (String sortedMethod : sortedMethods) {
                        boolean methodWasCovered = false;
                        final String methodNameAndSig = sortedMethod;
                        StringBuilder nextMethod = new StringBuilder("      " + methodNameAndSig + System.lineSeparator());

                        // loop over all lines of the method
                        SortedSet<CoverageData> sortedLines = new TreeSet<>(classData.getLines(methodNameAndSig));
                        Iterator<CoverageData> itLines = sortedLines.iterator();
                        nextMethod.append("       ");
                        while (itLines.hasNext()) {
                            LineData lineData = (LineData) itLines.next();
                            if (!onlyUseCovered || lineData.isCovered()) {
                                methodWasCovered = true;
                                nextMethod.append(" ").append(lineData.getLineNumber()).append("(").append(lineData.getHits()).append(")");
                            }
                        }
                        nextMethod.append(System.lineSeparator());
                        if (methodWasCovered) {
                            classWasCovered = true;
                            nextClass.append(nextMethod);
                        }
                    }
                    if (classWasCovered) {
                        fileWasCovered = true;
                        nextFile.append(nextClass);
                    }
                }
                if (fileWasCovered) {
                    packageWasCovered = true;
                    nextPackage.append(nextFile);
                }
            }
            if (packageWasCovered) {
                builder.append(nextPackage);
            }
        }
        return builder.toString();
    }

    public static boolean containsSameCoverage(ProjectData projectData, ProjectData OtherProjectData) {
        //it should not be the same object
        if (projectData == OtherProjectData) {
            return false;
        }
        // loop over all packages
        SortedSet<CoverageData> packages = projectData.getPackages();
        Iterator<CoverageData> itPackages = packages.iterator();
        SortedSet<CoverageData> packagesOther = OtherProjectData.getPackages();
        Iterator<CoverageData> itPackagesOther = packagesOther.iterator();
        if (packages.size() != packagesOther.size()) {
//			Log.err(this, "Unequal amount of stored packages.");
            return false;
        }
        while (itPackages.hasNext()) {
            PackageData packageData = (PackageData) itPackages.next();
            PackageData packageDataOther = (PackageData) itPackagesOther.next();

            if (!packageData.getName().equals(packageDataOther.getName())) {
//				Log.err(this, "Package names don't match.");
                return false;
            }

            // loop over all classes of the package
            Collection<SourceFileData> sourceFiles = packageData.getSourceFiles();
            Iterator<SourceFileData> itSourceFiles = sourceFiles.iterator();
            Collection<SourceFileData> sourceFilesOther = packageDataOther.getSourceFiles();
            Iterator<SourceFileData> itSourceFilesOther = sourceFilesOther.iterator();
            if (sourceFiles.size() != sourceFilesOther.size()) {
//				Log.err(this, "Unequal amount of stored source files for package '%s'.", packageData.getName());
                return false;
            }
            while (itSourceFiles.hasNext()) {
                SourceFileData fileData = itSourceFiles.next();
                SourceFileData fileDataOther = itSourceFilesOther.next();

                if (!fileData.getName().equals(fileDataOther.getName())) {
//					Log.err(this, "Source file names don't match for package '%s'.", packageData.getName());
                    return false;
                }

                SortedSet<CoverageData> classes = fileData.getClasses();
                Iterator<CoverageData> itClasses = classes.iterator();
                SortedSet<CoverageData> classesOther = fileDataOther.getClasses();
                Iterator<CoverageData> itClassesOther = classesOther.iterator();
                if (classes.size() != classesOther.size()) {
//					Log.err(this, "Unequal amount of stored classes for file '%s'.", fileData.getName());
                    return false;
                }
                while (itClasses.hasNext()) {
                    ClassData classData = (ClassData) itClasses.next();
                    ClassData classDataOther = (ClassData) itClassesOther.next();

                    if (!classData.getName().equals(classDataOther.getName())) {
//						Log.err(this, "Class names don't match for file '%s'.", fileData.getName());
                        return false;
                    }
                    if (!classData.getSourceFileName().equals(classDataOther.getSourceFileName())) {
//						Log.err(this, "Source file names don't match for file '%s'.", fileData.getName());
                        return false;
                    }

                    // loop over all methods of the class
                    SortedSet<String> sortedMethods = new TreeSet<>(classData.getMethodNamesAndDescriptors());
                    Iterator<String> itMethods = sortedMethods.iterator();
                    SortedSet<String> sortedMethodsOther = new TreeSet<>(classDataOther.getMethodNamesAndDescriptors());
                    Iterator<String> itMethodsOther = sortedMethodsOther.iterator();
                    if (sortedMethods.size() != sortedMethodsOther.size()) {
//						Log.err(this, "Unequal amount of stored methods for class '%s'.", classData.getName());
                        return false;
                    }
                    while (itMethods.hasNext()) {
                        final String methodNameAndSig = itMethods.next();
                        final String methodNameAndSigOther = itMethodsOther.next();
                        if (!methodNameAndSig.equals(methodNameAndSigOther)) {
//							Log.err(this, "Methods don't match for class '%s'.", classData.getName());
                            return false;
                        }

                        // loop over all lines of the method
                        SortedSet<CoverageData> sortedLines = new TreeSet<>(classData.getLines(methodNameAndSig));
                        Iterator<CoverageData> itLines = sortedLines.iterator();
                        SortedSet<CoverageData> sortedLinesOther = new TreeSet<>(classDataOther.getLines(methodNameAndSigOther));
                        Iterator<CoverageData> itLinesOther = sortedLinesOther.iterator();
                        if (sortedLines.size() != sortedLinesOther.size()) {
//							Log.err(this, "Unequal amount of stored lines for method '%s'.", methodNameAndSig);
                            return false;
                        }
                        while (itLines.hasNext()) {
                            LineData lineData = (LineData) itLines.next();
                            LineData lineDataOther = (LineData) itLinesOther.next();

                            if (lineData.getLineNumber() != lineDataOther.getLineNumber()) {
//								Log.err(this, "Line numbers don't match for method '%s'.", methodNameAndSig);
                                return false;
                            }

                            if (lineData.isCovered() != lineDataOther.isCovered()) {
//								Log.err(this, "Coverage doesn't match for method '%s', line %d.", methodNameAndSig, lineData.getLineNumber());
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean containsCoveredLines(ProjectData projectData) {
        // loop over all packages
        for (CoverageData coverageData2 : projectData.getPackages()) {
            PackageData packageData = (PackageData) coverageData2;

            // loop over all classes of the package
            for (SourceFileData sourceFileData : packageData.getSourceFiles()) {
                for (CoverageData coverageData1 : sourceFileData.getClasses()) {
                    ClassData classData = (ClassData) coverageData1;

                    // loop over all methods of the class
                    for (String methodNameAndSig : classData.getMethodNamesAndDescriptors()) {
                        // loop over all lines of the method
                        for (CoverageData coverageData : classData.getLines(methodNameAndSig)) {
                            LineData lineData = (LineData) coverageData;

                            if (lineData.isCovered()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean resetLines(ProjectData projectData) {
        // loop over all packages
        for (CoverageData coverageData2 : projectData.getPackages()) {
            PackageData packageData = (PackageData) coverageData2;

            // loop over all classes of the package
            for (SourceFileData sourceFileData : packageData.getSourceFiles()) {
                for (CoverageData coverageData1 : sourceFileData.getClasses()) {
                    ClassData classData = (ClassData) coverageData1;

                    // loop over all methods of the class
                    for (String methodNameAndSig : classData.getMethodNamesAndDescriptors()) {
                        // loop over all lines of the method
                        for (CoverageData coverageData : classData.getLines(methodNameAndSig)) {
                            LineData lineData = (LineData) coverageData;
                            lineData.setHits(0);
                        }
                    }
                }
            }
        }
        return false;
    }

}
