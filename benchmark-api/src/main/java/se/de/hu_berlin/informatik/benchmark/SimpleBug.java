package se.de.hu_berlin.informatik.benchmark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.benchmark.Bug;
import se.de.hu_berlin.informatik.benchmark.FaultLocations;
import se.de.hu_berlin.informatik.benchmark.FileWithFaultLocations;

/**
 * Holds all faulty files for a bug
 */
public class SimpleBug implements Bug {
    
    /** all files containing real fault locations for this bug */
    private final Map<String, FileWithFaultLocations> files = new HashMap<>();

    /**
     * Returns all files
     *
     * @return the files
     */
    @Override
    public List<FileWithFaultLocations> getFaultyFiles() {
        return new ArrayList<>(this.files.values());
    }

    /**
     * Check if filename exists
     *
     * @param filename
     *            to check
     * @return true if exists, false otherwise
     */
    @Override
    public boolean hasFile(final String filename) {
        return this.files.containsKey(filename);
    }

	@Override
	public void addFile(FileWithFaultLocations file) {
		this.files.put(file.getFileName(), file);
	}

	@Override
	public void addFile(String file) {
		this.files.put(file, null);
	}

	@Override
	public FaultLocations getFaultLocations(String file) throws IllegalStateException {
		FileWithFaultLocations locations = this.files.get(file);
		if (locations == null) {
			throw new IllegalStateException("No fault locations stored for file '" + file + "'.");
		}
		return locations.getFaultLocations();
	}

	@Override
	public boolean hasFaultLocations(String file) {
		return this.files.get(file) != null;
	}

	@Override
	public FileWithFaultLocations getFile(String file) {
		return this.files.get(file);
	}

}
