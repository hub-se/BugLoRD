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
		@SuppressWarnings("unchecked")
		SortedSet<PackageData> packagesOther = projectData.getPackages();
		Iterator<PackageData> itPackagesOther = packagesOther.iterator();
		while (itPackagesOther.hasNext()) {
			PackageData packageDataOther = itPackagesOther.next();

			PackageData foundPackageData = null;
			for (PackageData packageData : packages) {
				if (packageData.getName().equals(packageDataOther.getName())) {
					foundPackageData = packageData;
					break;
				}
			}
			if (foundPackageData == null) {
				Log.err(this, "Subtraction: Package '%s' does not exist.", packageDataOther.getName());
				return false;
			}

			// loop over all classes of the package
			@SuppressWarnings("unchecked")
			Collection<SourceFileData> sourceFiles = foundPackageData.getSourceFiles();
			@SuppressWarnings("unchecked")
			Collection<SourceFileData> sourceFilesOther = packageDataOther.getSourceFiles();
			Iterator<SourceFileData> itSourceFilesOther = sourceFilesOther.iterator();
			while (itSourceFilesOther.hasNext()) {
				SourceFileData fileDataOther = itSourceFilesOther.next();

				SourceFileData foundFileData = null;
				for (SourceFileData fileData : sourceFiles) {
					if (fileData.getName().equals(fileDataOther.getName())) {
						foundFileData = fileData;
						break;
					}
				}
				if (foundFileData == null) {
					Log.err(this, "Subtraction: Source file '%s' does not exist.", fileDataOther.getName());
					return false;
				}

				@SuppressWarnings("unchecked")
				SortedSet<ClassData> classes = foundFileData.getClasses();
				@SuppressWarnings("unchecked")
				SortedSet<ClassData> classesOther = fileDataOther.getClasses();
				Iterator<ClassData> itClassesOther = classesOther.iterator();
				while (itClassesOther.hasNext()) {
					ClassData classDataOther = itClassesOther.next();

					ClassData foundClassData = null;
					for (ClassData classData : classes) {
						if (classData.getName().equals(classDataOther.getName())) {
							foundClassData = classData;
							break;
						}
					}
					if (foundClassData == null) {
						Log.err(this, "Subtraction: Class '%s' does not exist.", classDataOther.getName());
						return false;
					}

					// loop over all methods of the class
					SortedSet<String> sortedMethods = new TreeSet<>();
					sortedMethods.addAll(foundClassData.getMethodNamesAndDescriptors());
					SortedSet<String> sortedMethodsOther = new TreeSet<>();
					sortedMethodsOther.addAll(classDataOther.getMethodNamesAndDescriptors());
					Iterator<String> itMethodsOther = sortedMethodsOther.iterator();
					while (itMethodsOther.hasNext()) {
						final String methodNameAndSigOther = itMethodsOther.next();

						String foundMethodNameAndSig = null;
						for (String methodNameAndSig : sortedMethods) {
							if (methodNameAndSig.equals(methodNameAndSigOther)) {
								foundMethodNameAndSig = methodNameAndSig;
								break;
							}
						}
						if (foundMethodNameAndSig == null) {
							Log.err(this, "Subtraction: Method '%s' does not exist.", methodNameAndSigOther);
							return false;
						}

						// loop over all lines of the method
						SortedSet<CoverageData> sortedLines = new TreeSet<>();
						sortedLines.addAll(foundClassData.getLines(foundMethodNameAndSig));
						SortedSet<CoverageData> sortedLinesOther = new TreeSet<>();
						sortedLinesOther.addAll(classDataOther.getLines(methodNameAndSigOther));
						Iterator<CoverageData> itLinesOther = sortedLinesOther.iterator();
						while (itLinesOther.hasNext()) {
							LineWrapper lineDataOther = new LineWrapper(itLinesOther.next());
							if (!lineDataOther.isCovered()) {
								continue;
							}

							LineWrapper foundLineData = null;
							for (CoverageData coverageData : sortedLines) {
								LineWrapper lineWrapper = new LineWrapper(coverageData);
								if (lineWrapper.getLineNumber() == lineDataOther.getLineNumber()) {
									foundLineData = lineWrapper;
									break;
								}
							}
							if (foundLineData == null) {
								Log.err(this, "Subtraction: Line '%s' does not exist in method '%s'.", lineDataOther.getLineNumber(), methodNameAndSigOther);
								return false;
							}

							if (foundLineData.getHits() - lineDataOther.getHits() < 0) {
								Log.err(this, "Subtraction: line hits would be negative after subtraction for method '%s', line %d.", methodNameAndSigOther, lineDataOther.getLineNumber());
								return false;
							}

							if (!foundLineData.setHits(foundLineData.getHits() - lineDataOther.getHits())) {
								Log.err(this, "Subtraction: line hits could not be set for method '%s', line %d.", methodNameAndSigOther, lineDataOther.getLineNumber());
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
	
	public static boolean containsSameCoverage(ProjectData projectData, ProjectData OtherProjectData) {
		//it should not be the same object
		if (projectData == OtherProjectData) {
			return false;
		}
		// loop over all packages
		@SuppressWarnings("unchecked")
		SortedSet<PackageData> packages = projectData.getPackages();
		Iterator<PackageData> itPackages = packages.iterator();
		@SuppressWarnings("unchecked")
		SortedSet<PackageData> packagesOther = OtherProjectData.getPackages();
		Iterator<PackageData> itPackagesOther = packagesOther.iterator();
		if (packages.size() != packagesOther.size()) {
//			Log.err(this, "Unequal amount of stored packages.");
			return false;
		}
		while (itPackages.hasNext()) {
			PackageData packageData = itPackages.next();
			PackageData packageDataOther = itPackagesOther.next();

			if (!packageData.getName().equals(packageDataOther.getName())) {
//				Log.err(this, "Package names don't match.");
				return false;
			}

			// loop over all classes of the package
			@SuppressWarnings("unchecked")
			Collection<SourceFileData> sourceFiles = packageData.getSourceFiles();
			Iterator<SourceFileData> itSourceFiles = sourceFiles.iterator();
			@SuppressWarnings("unchecked")
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
				@SuppressWarnings("unchecked")
				SortedSet<ClassData> classes = fileData.getClasses();
				Iterator<ClassData> itClasses = classes.iterator();
				@SuppressWarnings("unchecked")
				SortedSet<ClassData> classesOther = fileDataOther.getClasses();
				Iterator<ClassData> itClassesOther = classesOther.iterator();
				if (classes.size() != classesOther.size()) {
//					Log.err(this, "Unequal amount of stored classes for file '%s'.", fileData.getName());
					return false;
				}
				while (itClasses.hasNext()) {
					ClassData classData = itClasses.next();
					ClassData classDataOther = itClassesOther.next();

					if (!classData.getName().equals(classDataOther.getName())) {
//						Log.err(this, "Class names don't match for file '%s'.", fileData.getName());
						return false;
					}
					if (!classData.getSourceFileName().equals(classDataOther.getSourceFileName())) {
//						Log.err(this, "Source file names don't match for file '%s'.", fileData.getName());
						return false;
					}

					// loop over all methods of the class
					SortedSet<String> sortedMethods = new TreeSet<>();
					sortedMethods.addAll(classData.getMethodNamesAndDescriptors());
					Iterator<String> itMethods = sortedMethods.iterator();
					SortedSet<String> sortedMethodsOther = new TreeSet<>();
					sortedMethodsOther.addAll(classDataOther.getMethodNamesAndDescriptors());
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
						SortedSet<CoverageData> sortedLines = new TreeSet<>();
						sortedLines.addAll(classData.getLines(methodNameAndSig));
						Iterator<CoverageData> itLines = sortedLines.iterator();
						SortedSet<CoverageData> sortedLinesOther = new TreeSet<>();
						sortedLinesOther.addAll(classDataOther.getLines(methodNameAndSigOther));
						Iterator<CoverageData> itLinesOther = sortedLinesOther.iterator();
						if (sortedLines.size() != sortedLinesOther.size()) {
//							Log.err(this, "Unequal amount of stored lines for method '%s'.", methodNameAndSig);
							return false;
						}
						while (itLines.hasNext()) {
							LineWrapper lineData = new LineWrapper(itLines.next());
							LineWrapper lineDataOther = new LineWrapper(itLinesOther.next());

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
