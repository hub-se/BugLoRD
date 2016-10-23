package se.de.hu_berlin.informatik.benchmark;

import java.util.List;


public interface Bug {
	
	/**
     * Adds a faulty file to this bug.
     * @param faultLocations
     * a file with fault locations
     * @throws UnsupportedOperationException
     * if this method is not supported
     */
    public void addFile(FileWithFaultLocations faultLocationsFile) throws UnsupportedOperationException;
    
    /**
     * Adds a faulty file to this bug.
     * @param file
     * to add
     */
    public void addFile(final String file);

    /**
     * Adds fault locations to the given file.
     * @param file
     * to add
     * @param faultLocations
     * the location of faults in the given file
     * returns true if successful, false otherwise
     */
    default public boolean addFaultLocationsToFile(final String file, FaultLocations faultLocations) {
    	FileWithFaultLocations fileWithFaultLocations = getFile(file);
    	if (fileWithFaultLocations == null) {
    		return false;
    	}
    	return fileWithFaultLocations.setFaultLocations(faultLocations);
    }
    
    /**
     * Returns all faulty files.
     * @return
     * the files
     */
    public List<FileWithFaultLocations> getFaultyFiles();

    /**
     * Check if filename exists in the faulty files.
     * @param file
     * to check
     * @return
     * true if exists, false otherwise
     */
    public boolean hasFile(final String file);

    default public boolean hasFaultLocations(final String file) {
    	if (hasFile(file)) {
    		return getFile(file).hasFaultLocations();
    	} else {
    		return false;
    	}
    }
    
    /**
     * Get fault locations of given file.
     * @param file
     * to get
     * @return
     * fault locations, or null if none exists
     */
    default public FaultLocations getFaultLocations(final String file) {
    	if (hasFile(file)) {
    		return getFile(file).getFaultLocations();
    	} else {
    		return null;
    	}
    }
    
    /**
     * Get fault locations of given file.
     * @param file
     * to get
     * @return
     * file
     */
    public FileWithFaultLocations getFile(final String file);
	
}
