package se.de.hu_berlin.informatik.spectra.util;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.SingleLinkedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.DataInput;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.output.DataOutput;
import se.de.hu_berlin.informatik.utils.compression.ziputils.MoveNamedByteArraysBetweenZipFilesProcessor;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileWrapper;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipException;

public abstract class CachedMap<T> implements Map<Integer, T> {

    private static final String INDEX_DIRECTORY = "index";
	private static final String MAP_EXTENSION = ".map";
//    private static final String INDEX_EXTENSION = ".idx";

    private Map<Integer, CachemapFileEntry> storedEntries = new HashMap<>();
    private Map<Integer, T> newEntries = new HashMap<>();
    
    private Map<Integer, T> cache = new HashMap<>();
    private Queue<Integer> cacheOrder = new SingleLinkedIntArrayQueue(50);
    private int cacheSize;

    private AtomicInteger idGen = new AtomicInteger(0);
    private final ZipFileWrapper zipFile;
    private String directory;
	private int zipEntryFileSize = 100;
	
	boolean oldFormat = false;

    public CachedMap(Path zipFilePath, int cacheSize, String id, boolean deleteAtShutdown) {
        this.cacheSize = cacheSize;
        this.directory = id;
        this.zipFile = ZipFileWrapper.getZipFileWrapper(zipFilePath);

        if (zipFilePath.toFile().exists()) {
            // (try to) load map contents from existing zip file
            tryToLoadMapContents();
        }

        if (deleteAtShutdown) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    FileUtils.delete(zipFilePath);
                }
            });
        }
    }
    
    public void setCacheSize(int size) {
    	this.cacheSize = size;
    	while (cache.size() > cacheSize) {
    		cache.remove(cacheOrder.remove());
    	}
    }

    private void tryToLoadMapContents() {
        try {
        	int highestUsedIndex = -1;
        	String pattern = directory + "/" + INDEX_DIRECTORY;
//        	System.err.println("searching for: " + pattern);
			List<String> list = zipFile.getFileHeadersStartingWithString(pattern);
        	if (!list.isEmpty()) {
        		// new format
        		for (String fileName : list) {
        			// ...directory/.index/.xy
        			int dotIndex = fileName.lastIndexOf('.');
        			if (dotIndex < 0) {
        				throw new IllegalStateException("Illegal file name: " + fileName);
        			}
        			Integer fileIndex = Integer.valueOf(fileName.substring(dotIndex + 1));
        			highestUsedIndex = Math.max(highestUsedIndex, fileIndex);
        			byte[] indexFile = zipFile.uncheckedGet(fileName);
            		// index should contain a list of keys and where to find the values (key, offset, length)
            		ByteArrayInputStream byteIn = new ByteArrayInputStream(indexFile);
            		
//            		System.err.println(fileName);
            		while (byteIn.available() > 0) {
            			int key = DataInput.readInt(byteIn);
            			long offset = DataInput.readLong(byteIn);
            			int length = DataInput.readInt(byteIn);
            			storedEntries.put(key, new CachemapFileEntry(fileIndex, offset, length));
            		}
        		}
        	} else {
//        		System.err.println("old format map: " + zipFile.getzipFilePath() + "/" + directory);
        		oldFormat = true;
        		// old format (one entry per file)
        		list = zipFile.getFileHeadersStartingWithString(directory + "/");
        		for (String fileName : list) {
        			// old format files have an extension
        			String temp = fileName.substring(0, fileName.length()-4);
        			int dotIndex = temp.lastIndexOf('.');
        			if (dotIndex < 0) {
        				throw new IllegalStateException("Illegal file name: " + fileName);
        			}
        			Integer fileIndex = Integer.valueOf(temp.substring(dotIndex + 1));
        			highestUsedIndex = Math.max(highestUsedIndex, fileIndex);
        			storedEntries.put(fileIndex, new CachemapFileEntry(fileIndex, 0, (int) zipFile.getEntrySize(fileName)));
        		}
        	}
        	
        	idGen.set(highestUsedIndex+1);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    abstract public byte[] toByteArray(T value);

    abstract public T fromByteArray(byte[] value);

//    private String getFileName(int key) {
//        return directory + "/." + key + MAP_EXTENSION;
//    }
    
    private String getFileName(int key, String directory) {
        return directory + "/." + key;
    }
    
//    private String getIndexFileName(int key) {
//        return directory + "/" + INDEX_DIRECTORY + "/." + key + INDEX_EXTENSION;
//    }
    
    private String getIndexFileName(int key, String directory) {
        return directory + "/" + INDEX_DIRECTORY + "/." + key;
    }

	public boolean moveMapContentsTo(Path otherZipFile) {
    	return moveMapContentsTo(otherZipFile, directory);
    }

    public boolean moveMapContentsTo(Path otherZipFile, String directory) {
    	storeNewEntries(zipFile, this.directory, idGen, storedEntries, newEntries);
    	if (oldFormat) {
    		AtomicInteger idGen = new AtomicInteger(0);
    		// store in new format in target zip file
    		ZipFileWrapper zipFile = ZipFileWrapper.getZipFileWrapper(otherZipFile);
    		for (Integer key : keySet()) {
				T element = get(key);
				addNewEntry(key, element, zipFile, directory, idGen, null);
			}
    		storeNewEntries(zipFile, directory, idGen, null, newEntries);
    		return true;
    	} else {
    		MoveNamedByteArraysBetweenZipFilesProcessor mover =
    				new MoveNamedByteArraysBetweenZipFilesProcessor(zipFile.getzipFilePath(), otherZipFile);

    		boolean result = true;
    		for (int i = 0; i < idGen.get(); ++i) {
//    			System.err.println("moving: " + getFileName(i, directory));
    			result &= mover.submit(new Pair<>(getIndexFileName(i, this.directory), getIndexFileName(i, directory))).getResult();
    			result &= mover.submit(new Pair<>(getFileName(i, this.directory), getFileName(i, directory))).getResult();
    		}

    		return result;
    	}
    }
    
    private void storeNewEntries(ZipFileWrapper zipFile, String directory, 
    		AtomicInteger idGen, Map<Integer, CachemapFileEntry> storedEntries, 
    		Map<Integer, T> newEntries) {
    	if (!newEntries.isEmpty()) {
    		int nextIndex = idGen.getAndIncrement();
    		
    		try {
    			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    			ByteArrayOutputStream byteOutIndex = new ByteArrayOutputStream();

    			long offset = 0;
    			for (Entry<Integer, T> entry : newEntries.entrySet()) {
    				byte[] array = toByteArray(entry.getValue());
    				// index file contains list of entries: (key, offset, length)
    				DataOutput.writeInt(byteOutIndex, entry.getKey());
    				DataOutput.writeLong(byteOutIndex, offset);
    				DataOutput.writeInt(byteOutIndex, array.length);
//    				System.err.println(entry.getKey() + ", " + offset + ", " + array.length);

    				if (storedEntries != null) {
    					storedEntries.put(entry.getKey(), new CachemapFileEntry(nextIndex, offset, array.length));
    				}
    				offset += array.length;

    				// contents file contains list of entries (byte[])
    				byteOut.write(array);
    			}

    			byteOut.flush();
    			byteOutIndex.flush();

    			// store in zip file
				byte[] byteArray = byteOutIndex.toByteArray();
//				System.err.println("byteArray length: " + byteArray.length);
				zipFile.addArray(byteArray, getIndexFileName(nextIndex, directory));
				byte[] byteArray2 = byteOut.toByteArray();
//				System.err.println("byteArray2 length: " + byteArray2.length);
				zipFile.addArray(byteArray2, getFileName(nextIndex, directory));
    		}catch (Exception e) {
				throw new IllegalStateException(e);
			}
            
    		newEntries.clear();
    	}
	}
    
    private T load(int key) {
    	CachemapFileEntry fileEntry = storedEntries.get(key);
        String fileName = getFileName(fileEntry.fileIndex, directory);
        if (oldFormat) {
        	fileName += MAP_EXTENSION;
        }
		try {
//        	System.err.println("loading zip entry: " + getFileName(key, directory) + ", " + fileEntry.offset + ", " + fileEntry.length);
            return fromByteArray(zipFile.uncheckedGet(fileName, fileEntry.offset, fileEntry.length));
        } catch (ZipException e) {
            e.printStackTrace();
            throw new IllegalStateException("Could not get zip entry: " + zipFile.getzipFilePath() + "/" + fileName);
        } catch (Exception e) {
        	System.err.println("loading zip entry: " + fileName + ", " + fileEntry.offset + ", " + fileEntry.length);
        	e.printStackTrace();
            throw new IllegalStateException("Could not get zip entry: " + zipFile.getzipFilePath() + "/" + fileName);
		}
    }

    @Override
    public int size() {
        return storedEntries.size() + newEntries.size();
    }

    @Override
    public boolean isEmpty() {
        return size() <= 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return storedEntries.containsKey(key) || newEntries.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T get(Object key) {
        if (containsKey(key)) {
            if (cache.containsKey(key)) {
                return cache.get(key);
            }
            if (newEntries.containsKey(key)) {
                return newEntries.get(key);
            }

            // load from zip file, if not cached
            T value = load((Integer) key);
            // put in cache
            if (cacheSize > 0) {
                putInCache((Integer) key, value);
            }

            return value;
        } else {
            return null;
        }
    }

    private void putInCache(int key, T value) {
        if (cache.size() >= cacheSize) {
            cache.remove(cacheOrder.remove());
        }
        cache.put(key, value);
        cacheOrder.add(key);
    }

    @Override
    public T put(Integer key, T value) {
        if (storedEntries.containsKey(key)) {
        	return replaceStoredEntry(key, value);
        } else {
            return addNewEntry(key, value, zipFile, directory, idGen, storedEntries);
        }
    }

    // this rewrites the entire zip archive... avoid, if possible!
    private T replaceStoredEntry(Integer key, T value) {
		// removes the files associated with the entry from the zip file and stores new entries
    	CachemapFileEntry fileEntry = storedEntries.get(key);
//    	System.err.println("replacing key " + key + " in zip entry " + getFileName(fileEntry.fileIndex, directory));
    	// retrieve all entries in the same file
    	Map<Integer, T> newEntries = new HashMap<>();
    	for (Entry<Integer, CachemapFileEntry> entry : storedEntries.entrySet()) {
			if (entry.getValue().fileIndex == fileEntry.fileIndex) {
				newEntries.put(entry.getKey(), load(entry.getKey()));
			}
		}
    	// add actual new entry
    	T previous = value == null ? newEntries.remove(key) : newEntries.put(key, value);
    	
    	// remove old file entries
    	Collection<String> toDelete = new ArrayList<>(2);
    	toDelete.add(getFileName(fileEntry.fileIndex, directory));
    	toDelete.add(getIndexFileName(fileEntry.fileIndex, directory));
    	zipFile.removeEntries(toDelete);
    	
    	// this should store all the "new" entries and overwrite the entries in the storedEntries map
    	storeNewEntries(zipFile, directory, new AtomicInteger(fileEntry.fileIndex), storedEntries, newEntries);
    	
    	if (value == null) {
    		storedEntries.remove(key);
    	}
    	
    	// return previous entry
    	return previous;
	}

	private T addNewEntry(Integer key, T value, ZipFileWrapper zipFile, String directory, 
    		AtomicInteger idGen, Map<Integer, CachemapFileEntry> storedEntries) {
		T previous = newEntries.put(key, value);
		if (newEntries.size() >= zipEntryFileSize) {
			storeNewEntries(zipFile, directory, idGen, storedEntries, newEntries);
		}
		return previous;
	}

	@Override
	public T remove(Object key) {
		if (storedEntries.containsKey(key)) {
			return replaceStoredEntry((Integer) key, null);
		} else {
			return null;
		}
	}

	@Override
    public void putAll(Map<? extends Integer, ? extends T> m) {
        for (Entry<? extends Integer, ? extends T> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        Set<String> toRemove = new HashSet<>();
        for (Entry<Integer, CachemapFileEntry> entry : storedEntries.entrySet()) {
            toRemove.add(getFileName(entry.getValue().fileIndex, directory));
            toRemove.add(getIndexFileName(entry.getValue().fileIndex, directory));
        }
        zipFile.removeEntries(toRemove);
        newEntries.clear();
        storedEntries.clear();
        cache.clear();
        cacheOrder.clear();
    }

    @Override
    public Set<Integer> keySet() {
    	// TODO this should not be used extensively while building the map...
    	storeNewEntries(zipFile, directory, idGen, storedEntries, newEntries);
        return storedEntries.keySet();
    }

    @Override
    public Collection<T> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entry<Integer, T>> entrySet() {
        throw new UnsupportedOperationException();
    }

    private static class CachemapFileEntry {
    	final int fileIndex;
    	final long offset;
    	final int length;
    	
		public CachemapFileEntry(int fileIndex, long offset, int length) {
			super();
			this.fileIndex = fileIndex;
			this.offset = offset;
			this.length = length;
		}
    	
    	
    }
}
