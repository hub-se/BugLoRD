package se.de.hu_berlin.informatik.sbfl.spectra;

import java.util.List;

public interface TestProject {

	public String getProjectMainDir();
	
	public String getSrcDir();
	
	public String getBinDir();
	
	public String getBinTestDir();
	
	public String getTestCP();
	
	public String getTestClassListPath();
	
	public List<String> getFailingTests();
	
}
