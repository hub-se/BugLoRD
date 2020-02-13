package se.de.hu_berlin.informatik.spectra.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.zip.ZipException;

import se.de.hu_berlin.informatik.utils.compression.ziputils.MoveNamedByteArraysBetweenZipFilesProcessor;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileWrapper;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;

public abstract class CachedMap<T> implements Map<Integer,T> {

	private static final String MAP_EXTENSION = ".map";
	
	private Set<Integer> keys = new HashSet<>();
	private Map<Integer, T> cache = new HashMap<>();
	private Queue<Integer> cacheOrder = new LinkedList<>();
	private final int cacheSize;
	
	private int size = 0;
	private final ZipFileWrapper zipFile;
	private String directory;

	public CachedMap(Path zipFilePath, int cacheSize, String id, boolean deleteAtShutdown) {
		this.cacheSize = cacheSize;
		this.directory = id + "/.";
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
	
	private void tryToLoadMapContents() {
		try {
			List<String> list = zipFile.getFileHeadersContainingString(directory);
			for (String fileName : list) {
				String[] split = fileName.split("\\.");
				if (split.length < 3) {
					throw new IllegalStateException("Illegal file name: " + fileName);
				}
				keys.add(Integer.valueOf(split[split.length-2]));
				++size;
			}
			
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	abstract public byte[] toByteArray(T value);
	abstract public T fromByteArray(byte[] value);
	
	
	private void store(int key, T value) {
		zipFile.addArray(toByteArray(value), getFileName(key));
	}
	
	private T load(int key) {
		try {
			return fromByteArray(zipFile.uncheckedGet(getFileName(key)));
		} catch (ZipException e) {
			e.printStackTrace();
			throw new IllegalStateException("Could not get zip entry: " + getFileName(key));
		}
	}
	
	private String getFileName(int key) {
		return directory + key + MAP_EXTENSION;
	}

	public boolean moveMapContentsTo(Path otherZipFile) {
		MoveNamedByteArraysBetweenZipFilesProcessor mover = 
				new MoveNamedByteArraysBetweenZipFilesProcessor(zipFile.getzipFilePath(), otherZipFile);
		
		boolean result = true;
		for (int key : keys) {
			String fileName = getFileName(key);
			result &= mover.submit(new Pair<>(fileName, fileName)).getResult();
		}
		
		return result;
	}
	
	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size <= 0;
	}

	@Override
	public boolean containsKey(Object key) {
		return keys.contains(key);
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T get(Object key) {
		if (keys.contains(key)) {
			if (cache.containsKey(key)) {
				return cache.get(key);
			}
			
			// load from zip file, if not in cache
			T value = load((Integer) key);
			// put in cache
			if (cacheSize > 0) {
				putInCache((Integer) key ,value);
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
		if (!keys.contains(key)) {
			++size;
			// add the key
			keys.add(key);
		} else {
			// retrieving values from the zip file closes the input stream
//			throw new IllegalStateException("Cannot store another entry for key: " + key);
		}
		
		// store the value in the zip file
		store(key, value);
		
		return null;
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
		for (int key : keys) {
			toRemove.add(getFileName(key));
		}
		zipFile.removeEntries(toRemove);
		keys.clear();
		cache.clear();
		cacheOrder.clear();
		size = 0;
	}

	@Override
	public Set<Integer> keySet() {
		return Collections.unmodifiableSet(keys);
	}
	
	
	@Override
	public T remove(Object key) {
		T item = get(key);
		if (keys.contains(key)) {
			--size;
			// remove the key
			keys.remove(key);
			// remove from zip file (rewrites the zip file...)
			zipFile.removeEntries(Collections.singletonList(getFileName((Integer) key)));
		}
		return item;
	}

	@Override
	public Collection<T> values() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Entry<Integer, T>> entrySet() {
		throw new UnsupportedOperationException();
	}

}
