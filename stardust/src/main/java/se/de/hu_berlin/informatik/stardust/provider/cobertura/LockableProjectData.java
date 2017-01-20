package se.de.hu_berlin.informatik.stardust.provider.cobertura;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageData;
import net.sourceforge.cobertura.coveragedata.PackageData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.SourceFileData;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class LockableProjectData extends ProjectData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8484242021027071646L;
	private boolean locked = false;

	public void lock() {
		locked  = true;
	}
	
	@Override
	public void addClassData(ClassData classData) {
		if (locked) {
			Log.err(this, "Adding class data to locked project data...");
		}
		super.addClassData(classData);
	}

	@Override
	public ClassData getOrCreateClassData(String name) {
		if (locked) {
			Log.err(this, "Getting or creating class data in locked project data.");
		}
		lock.lock();
		try {
			ClassData classData = getClassData(name);
			if (classData == null) {
				classData = new MyClassData(name);
				addClassData(classData);
			}
			return classData;
		} finally {
			lock.unlock();
		}
	}
	
	public boolean subtract(ProjectData projectData) {
				// loop over all packages
				@SuppressWarnings("unchecked")
				SortedSet<PackageData> packages = this.getPackages();
				Iterator<PackageData> itPackages = packages.iterator();
				@SuppressWarnings("unchecked")
				SortedSet<PackageData> packagesLast = projectData.getPackages();
				Iterator<PackageData> itPackagesLast = packagesLast.iterator();
				if (packages.size() != packagesLast.size()) {
					Log.err(this, "Subtraction: Unequal amount of stored packages.");
					return false;
				}
				while (itPackages.hasNext()) {
					PackageData packageData = itPackages.next();
					PackageData packageDataLast = itPackagesLast.next();

					if (!packageData.getName().equals(packageDataLast.getName())) {
						Log.err(this, "Subtraction: Package names don't match.");
						return false;
					}

					// loop over all classes of the package
					@SuppressWarnings("unchecked")
					Collection<SourceFileData> sourceFiles = packageData.getSourceFiles();
					Iterator<SourceFileData> itSourceFiles = sourceFiles.iterator();
					@SuppressWarnings("unchecked")
					Collection<SourceFileData> sourceFilesLast = packageDataLast.getSourceFiles();
					Iterator<SourceFileData> itSourceFilesLast = sourceFilesLast.iterator();
					if (sourceFiles.size() != sourceFilesLast.size()) {
						Log.err(this, "Subtraction: Unequal amount of stored source files for package '%s'.", packageData.getName());
						return false;
					}
					while (itSourceFiles.hasNext()) {
						SourceFileData fileData = itSourceFiles.next();
						SourceFileData fileDataLast = itSourceFilesLast.next();

						if (!fileData.getName().equals(fileDataLast.getName())) {
							Log.err(this, "Subtraction: Source file names don't match for package '%s'.", packageData.getName());
							return false;
						}
						@SuppressWarnings("unchecked")
						SortedSet<ClassData> classes = fileData.getClasses();
						Iterator<ClassData> itClasses = classes.iterator();
						@SuppressWarnings("unchecked")
						SortedSet<ClassData> classesLast = fileDataLast.getClasses();
						Iterator<ClassData> itClassesLast = classesLast.iterator();
						if (classes.size() != classesLast.size()) {
							Log.err(this, "Subtraction: Unequal amount of stored classes for file '%s'.", fileData.getName());
							return false;
						}
						while (itClasses.hasNext()) {
							ClassData classData = itClasses.next();
							ClassData classDataLast = itClassesLast.next();

							if (!classData.getName().equals(classDataLast.getName())) {
								Log.err(this, "Subtraction: Class names don't match for file '%s'.", fileData.getName());
								return false;
							}
							if (!classData.getSourceFileName().equals(classDataLast.getSourceFileName())) {
								Log.err(this, "Subtraction: Source file names don't match for file '%s'.", fileData.getName());
								return false;
							}

							// loop over all methods of the class
							SortedSet<String> sortedMethods = new TreeSet<>();
							sortedMethods.addAll(classData.getMethodNamesAndDescriptors());
							Iterator<String> itMethods = sortedMethods.iterator();
							SortedSet<String> sortedMethodsLast = new TreeSet<>();
							sortedMethodsLast.addAll(classDataLast.getMethodNamesAndDescriptors());
							Iterator<String> itMethodsLast = sortedMethodsLast.iterator();
							if (sortedMethods.size() != sortedMethodsLast.size()) {
								Log.err(this, "Subtraction: Unequal amount of stored methods for class '%s'.", classData.getName());
								return false;
							}
							while (itMethods.hasNext()) {
								final String methodNameAndSig = itMethods.next();
								final String methodNameAndSigLast = itMethodsLast.next();
								if (!methodNameAndSig.equals(methodNameAndSigLast)) {
									Log.err(this, "Subtraction: Methods don't match for class '%s'.", classData.getName());
									return false;
								}

								// loop over all lines of the method
								SortedSet<CoverageData> sortedLines = new TreeSet<>();
								sortedLines.addAll(classData.getLines(methodNameAndSig));
								Iterator<CoverageData> itLines = sortedLines.iterator();
								SortedSet<CoverageData> sortedLinesLast = new TreeSet<>();
								sortedLinesLast.addAll(classDataLast.getLines(methodNameAndSigLast));
								Iterator<CoverageData> itLinesLast = sortedLinesLast.iterator();
								if (sortedLines.size() != sortedLinesLast.size()) {
									Log.err(this, "Subtraction: Unequal amount of stored lines for method '%s'.", methodNameAndSig);
									return false;
								}
								while (itLines.hasNext()) {
									LineWrapper lineData = new LineWrapper(itLines.next());
									LineWrapper lineDataLast = new LineWrapper(itLinesLast.next());

									if (lineData.getLineNumber() != lineDataLast.getLineNumber()) {
										Log.err(this, "Subtraction: Line numbers don't match for method '%s'.", methodNameAndSig);
										return false;
									}
									
									if (lineData.getHits() - lineDataLast.getHits() < 0) {
										Log.err(this, "Subtraction: line hits would be negative after subtraction for method '%s', line %d.", methodNameAndSig, lineData.getLineNumber());
										return false;
									}
									
									if (!lineData.setHits(lineData.getHits() - lineDataLast.getHits())) {
										Log.err(this, "Subtraction: line hits could not be set for method '%s', line %d.", methodNameAndSig, lineData.getLineNumber());
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
		@SuppressWarnings("unchecked")
		SortedSet<PackageData> packages = projectData.getPackages();
		Iterator<PackageData> itPackages = packages.iterator();
		while (itPackages.hasNext()) {
			boolean packageWasCovered = false;
			PackageData packageData = itPackages.next();
			String nextPackage = packageData.getName() + System.lineSeparator();

			// loop over all classes of the package
			@SuppressWarnings("unchecked")
			Collection<SourceFileData> sourceFiles = packageData.getSourceFiles();
			Iterator<SourceFileData> itSourceFiles = sourceFiles.iterator();
			while (itSourceFiles.hasNext()) {
				boolean fileWasCovered = false;
				SourceFileData fileData = itSourceFiles.next();
				String nextFile = "  " + fileData.getName() + System.lineSeparator();
				
				@SuppressWarnings("unchecked")
				SortedSet<ClassData> classes = fileData.getClasses();
				Iterator<ClassData> itClasses = classes.iterator();
				while (itClasses.hasNext()) {
					boolean classWasCovered = false;
					ClassData classData = itClasses.next();
					String nextClass = "    " + classData.getName() + System.lineSeparator();

					// loop over all methods of the class
					SortedSet<String> sortedMethods = new TreeSet<>();
					sortedMethods.addAll(classData.getMethodNamesAndDescriptors());
					Iterator<String> itMethods = sortedMethods.iterator();
					while (itMethods.hasNext()) {
						boolean methodWasCovered = false;
						final String methodNameAndSig = itMethods.next();
						String nextMethod = "      " + methodNameAndSig + System.lineSeparator();

						// loop over all lines of the method
						SortedSet<CoverageData> sortedLines = new TreeSet<>();
						sortedLines.addAll(classData.getLines(methodNameAndSig));
						Iterator<CoverageData> itLines = sortedLines.iterator();
						nextMethod += "       ";
						while (itLines.hasNext()) {
							LineWrapper lineData = new LineWrapper(itLines.next());
							if (!onlyUseCovered || lineData.isCovered()) {
								methodWasCovered = true;
								nextMethod += " " + lineData.getLineNumber() + "(" + lineData.getHits() + ")";
							}
						}
						nextMethod += System.lineSeparator();
						if (methodWasCovered) {
							classWasCovered = true;
							nextClass += nextMethod;
						}
					}
					if (classWasCovered) {
						fileWasCovered = true;
						nextFile += nextClass;
					}
				}
				if (fileWasCovered) {
					packageWasCovered = true;
					nextPackage += nextFile;
				}
			}
			if (packageWasCovered) {
				builder.append(nextPackage);
			}
		}
		return builder.toString();
	}
	
	public static boolean containsSameCoverage(ProjectData projectData2, ProjectData lastProjectData) {
		//it should not be the same object
		if (projectData2 == lastProjectData) {
			return false;
		}
		// loop over all packages
		@SuppressWarnings("unchecked")
		SortedSet<PackageData> packages = projectData2.getPackages();
		Iterator<PackageData> itPackages = packages.iterator();
		@SuppressWarnings("unchecked")
		SortedSet<PackageData> packagesLast = lastProjectData.getPackages();
		Iterator<PackageData> itPackagesLast = packagesLast.iterator();
		if (packages.size() != packagesLast.size()) {
//			Log.err(this, "Unequal amount of stored packages.");
			return false;
		}
		while (itPackages.hasNext()) {
			PackageData packageData = itPackages.next();
			PackageData packageDataLast = itPackagesLast.next();

			if (!packageData.getName().equals(packageDataLast.getName())) {
//				Log.err(this, "Package names don't match.");
				return false;
			}

			// loop over all classes of the package
			@SuppressWarnings("unchecked")
			Collection<SourceFileData> sourceFiles = packageData.getSourceFiles();
			Iterator<SourceFileData> itSourceFiles = sourceFiles.iterator();
			@SuppressWarnings("unchecked")
			Collection<SourceFileData> sourceFilesLast = packageDataLast.getSourceFiles();
			Iterator<SourceFileData> itSourceFilesLast = sourceFilesLast.iterator();
			if (sourceFiles.size() != sourceFilesLast.size()) {
//				Log.err(this, "Unequal amount of stored source files for package '%s'.", packageData.getName());
				return false;
			}
			while (itSourceFiles.hasNext()) {
				SourceFileData fileData = itSourceFiles.next();
				SourceFileData fileDataLast = itSourceFilesLast.next();

				if (!fileData.getName().equals(fileDataLast.getName())) {
//					Log.err(this, "Source file names don't match for package '%s'.", packageData.getName());
					return false;
				}
				@SuppressWarnings("unchecked")
				SortedSet<ClassData> classes = fileData.getClasses();
				Iterator<ClassData> itClasses = classes.iterator();
				@SuppressWarnings("unchecked")
				SortedSet<ClassData> classesLast = fileDataLast.getClasses();
				Iterator<ClassData> itClassesLast = classesLast.iterator();
				if (classes.size() != classesLast.size()) {
//					Log.err(this, "Unequal amount of stored classes for file '%s'.", fileData.getName());
					return false;
				}
				while (itClasses.hasNext()) {
					ClassData classData = itClasses.next();
					ClassData classDataLast = itClassesLast.next();

					if (!classData.getName().equals(classDataLast.getName())) {
//						Log.err(this, "Class names don't match for file '%s'.", fileData.getName());
						return false;
					}
					if (!classData.getSourceFileName().equals(classDataLast.getSourceFileName())) {
//						Log.err(this, "Source file names don't match for file '%s'.", fileData.getName());
						return false;
					}

					// loop over all methods of the class
					SortedSet<String> sortedMethods = new TreeSet<>();
					sortedMethods.addAll(classData.getMethodNamesAndDescriptors());
					Iterator<String> itMethods = sortedMethods.iterator();
					SortedSet<String> sortedMethodsLast = new TreeSet<>();
					sortedMethodsLast.addAll(classDataLast.getMethodNamesAndDescriptors());
					Iterator<String> itMethodsLast = sortedMethodsLast.iterator();
					if (sortedMethods.size() != sortedMethodsLast.size()) {
//						Log.err(this, "Unequal amount of stored methods for class '%s'.", classData.getName());
						return false;
					}
					while (itMethods.hasNext()) {
						final String methodNameAndSig = itMethods.next();
						final String methodNameAndSigLast = itMethodsLast.next();
						if (!methodNameAndSig.equals(methodNameAndSigLast)) {
//							Log.err(this, "Methods don't match for class '%s'.", classData.getName());
							return false;
						}

						// loop over all lines of the method
						SortedSet<CoverageData> sortedLines = new TreeSet<>();
						sortedLines.addAll(classData.getLines(methodNameAndSig));
						Iterator<CoverageData> itLines = sortedLines.iterator();
						SortedSet<CoverageData> sortedLinesLast = new TreeSet<>();
						sortedLinesLast.addAll(classDataLast.getLines(methodNameAndSigLast));
						Iterator<CoverageData> itLinesLast = sortedLinesLast.iterator();
						if (sortedLines.size() != sortedLinesLast.size()) {
//							Log.err(this, "Unequal amount of stored lines for method '%s'.", methodNameAndSig);
							return false;
						}
						while (itLines.hasNext()) {
							LineWrapper lineData = new LineWrapper(itLines.next());
							LineWrapper lineDataLast = new LineWrapper(itLinesLast.next());

							if (lineData.getLineNumber() != lineDataLast.getLineNumber()) {
//								Log.err(this, "Line numbers don't match for method '%s'.", methodNameAndSig);
								return false;
							}
							
							if (lineData.isCovered() != lineDataLast.isCovered()) {
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
        @SuppressWarnings("unchecked")
		Iterator<PackageData> itPackages = projectData.getPackages().iterator();
		while (itPackages.hasNext()) {
			PackageData packageData = itPackages.next();

			// loop over all classes of the package
			@SuppressWarnings("unchecked")
			Iterator<SourceFileData> itSourceFiles = packageData.getSourceFiles().iterator();
			while (itSourceFiles.hasNext()) {
				@SuppressWarnings("unchecked")
				Iterator<ClassData> itClasses = itSourceFiles.next().getClasses().iterator();
				while (itClasses.hasNext()) {
					ClassData classData = itClasses.next();

	                // loop over all methods of the class
	        		Iterator<String> itMethods = classData.getMethodNamesAndDescriptors().iterator();
	        		while (itMethods.hasNext()) {
	        			final String methodNameAndSig = itMethods.next();

	                    // loop over all lines of the method
	            		Iterator<CoverageData> itLines = classData.getLines(methodNameAndSig).iterator();
	            		while (itLines.hasNext()) {
	            			LineWrapper lineData = new LineWrapper(itLines.next());
	            			
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
	
}
