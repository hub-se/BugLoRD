package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Disk buffered Map implementation.
 * 
 * @param <E> 
 * the type of elements held in the map(s)
 */
public class BufferedMap<E> implements Map<Integer, E> {
	
	// keep at most 3 (+1 with the last node) nodes in memory
    private static final int CACHE_SIZE = 3;
    
	private File output;
	private String filePrefix;
	
	private int maxSubMapSize = 500000;
	private Set<Integer> existingNodes = new HashSet<>();

	private Lock lock = new ReentrantLock();
	
	// cache all other nodes, if necessary
	private Map<Integer,Node<E>> cachedNodes = new HashMap<>();
	private List<Integer> cacheSequence = new LinkedList<>();

	private int size = 0;

    public BufferedMap(File outputDir, String filePrefix) {
    	check(outputDir);
		this.output = outputDir;
		this.filePrefix = Objects.requireNonNull(filePrefix);
    }
    
    private void check(File outputDir) {
    	Objects.requireNonNull(outputDir);
		if (outputDir.isFile()) {
			throw new IllegalStateException("Output directory is an existing file: " + outputDir);
		}
		if (!outputDir.exists() && !outputDir.mkdirs()) {
			throw new IllegalStateException("Could not create output directory: " + outputDir);
		}
	}

	public BufferedMap(File output, String filePrefix, int maxSubMapSize) {
    	this(output, filePrefix);
    	this.maxSubMapSize = maxSubMapSize < 1 ? 1 : maxSubMapSize;
    }
	
	public File getOutputDir() {
		return output;
	}
	
	public String getFilePrefix() {
		return filePrefix;
	}

	private String getFileName(int storeIndex) {
		return output.getAbsolutePath() + File.separator + filePrefix + "-" + storeIndex + ".rry";
	}

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
    	lock.lock();
    	try {
    		cachedNodes.clear();
    		cacheSequence.clear();
    		// delete possibly stored nodes
    		for (Integer storeIndex : existingNodes) {
				delete(storeIndex);
			}
    		existingNodes.clear();
    		size = 0;
    	} finally {
    		lock.unlock();
		}
    }


	private void delete(Integer storeIndex) {
		lock.lock();
		try {
			String filename = getFileName(storeIndex);
//			if (cachedNodes.containsKey(storeIndex)) {
//				cachedNodes.remove(storeIndex);
//				cacheSequence.remove((Object)storeIndex);
//			}
			existingNodes.remove(storeIndex);
			// stored node should be deleted
			new File(filename).delete();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	protected void finalize() throws Throwable {
		this.clear();
		super.finalize();
	}


	@Override
	public boolean containsKey(Object key) {
		if (key == null) {
			return false;
		}
		Integer index = (Integer) key;
		int storeIndex = getStoreindex(index);
		Node<E> node = getNode(storeIndex);
		return node == null ? false : node.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E get(Object key) {
		if (key == null) {
			return null;
		}
		Integer index = (Integer) key;
		int storeIndex = getStoreindex(index);
		Node<E> node = getNode(storeIndex);
		return node == null ? null : node.get(key);
	}

	@Override
	public E put(Integer key, E value) {
		if (value == null) {
			throw new IllegalStateException("Must not add null values to buffered map.");
		}
		int storeIndex = getStoreindex(key);
		Node<E> node = getOrCreateNode(storeIndex);
		E element = node.put(key, value);
		if (element == null) {
			++size;
		}
		return element;
	}

	@Override
	public E remove(Object key) {
		if (key == null) {
			return null;
		}
		Integer index = (Integer) key;
		int storeIndex = getStoreindex(index);
		Node<E> node = getNode(storeIndex);
		if (node == null) {
			return null;
		} else {
			E element = node.remove(key);
			if (element != null) {
				--size;
				if (node.isEmpty()) {
					delete(node.storeIndex);
				}
			}
			return element;
		}
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends E> m) {
		for (java.util.Map.Entry<? extends Integer, ? extends E> entry : m.entrySet()) {
			this.put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public Set<Integer> keySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<E> values() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<java.util.Map.Entry<Integer, E>> entrySet() {
		throw new UnsupportedOperationException();
	}
	
	
	
	
	private int getStoreindex(Integer index) {
		return index / maxSubMapSize;
	}
	
	private Node<E> getOrCreateNode(Integer key) {
		Node<E> node = getNode(key);
		if (node == null) {
			node = createNewNode(key);
		}
		return node;
	}
	
	private Node<E> createNewNode(Integer key) {
		Node<E> node = new Node<>(key);
		existingNodes.add(key);
		cacheNode(node);
		return node;
	}
	
	private Node<E> load(int storeIndex) {
		if (!existingNodes.contains(storeIndex)) {
			return null;
		}
		lock.lock();
		try {
			if (cachedNodes.containsKey(storeIndex)) {
				// already cached
				return cachedNodes.get(storeIndex);
			}
			
			String filename = getFileName(storeIndex);
			Node<E> loadedNode;
			try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filename))) {
				@SuppressWarnings("unchecked")
				Map<Integer, E> map = (Map<Integer, E>)inputStream.readObject();
				loadedNode = new Node<E>(storeIndex, map);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw new IllegalStateException();
			} catch (IOException e) {
				e.printStackTrace();
				throw new IllegalStateException();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new IllegalStateException();
			}
			
			cacheNode(loadedNode);
			return loadedNode;
		} finally {
			lock.unlock();
		}
	}

	private void cacheNode(Node<E> node) {
		lock.lock();
		try {
			// cache the node
			if (cachedNodes.size() >= CACHE_SIZE) {
				// remove the node from the cache that has been added first
				Node<E> uncachedNode = cachedNodes.remove(cacheSequence.remove(0));
				store(uncachedNode);
			}
			cachedNodes.put(node.storeIndex, node);
			cacheSequence.add(node.storeIndex);
		} finally {
			lock.unlock();
		}
	}

	private Node<E> getNode(int storeIndex) {
		return load(storeIndex);
	}

	private void store(Node<E> node) {
		if (node == null || node.isEmpty()) {
			return;
		}
		String filename = getFileName(node.storeIndex);
		try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filename))) {
			outputStream.writeObject(node.subMap);
			// file can be removed on exit!? TODO
			new File(filename).deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	protected static class Node<E> implements Map<Integer, E>{
        
		Map<Integer,E> subMap;

        // index to store/load this node
        private int storeIndex;

        Node(int storeIndex) {
        	this.storeIndex = storeIndex;
        	this.subMap = new HashMap<>();
        }

		public Node(int storeIndex, Map<Integer, E> map) {
			this.storeIndex = storeIndex;
        	this.subMap = map;
		}

		@Override
		public int size() {
			return subMap.size();
		}

		@Override
		public boolean isEmpty() {
			return subMap.isEmpty();
		}

		@Override
		public boolean containsKey(Object key) {
			return subMap.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			return subMap.containsValue(value);
		}

		@Override
		public E get(Object key) {
			return subMap.get(key);
		}

		@Override
		public E put(Integer key, E value) {
			return subMap.put(key, value);
		}

		@Override
		public E remove(Object key) {
			return subMap.remove(key);
		}

		@Override
		public void putAll(Map<? extends Integer, ? extends E> m) {
			subMap.putAll(m);
		}

		@Override
		public void clear() {
			subMap.clear();
		}

		@Override
		public Set<Integer> keySet() {
			return subMap.keySet();
		}

		@Override
		public Collection<E> values() {
			return subMap.values();
		}

		@Override
		public Set<java.util.Map.Entry<Integer, E>> entrySet() {
			return subMap.entrySet();
		}
    }
	
	public Iterator<java.util.Map.Entry<Integer,E>> entrySetIterator() {
		return new MyBufferedIterator();
	}
	
	protected final class MyBufferedIterator implements Iterator<java.util.Map.Entry<Integer,E>> {

		Iterator<Integer> storeIndexIterator;
		Integer currentStoreIndex = null;
		Iterator<java.util.Map.Entry<Integer,E>> entrySetIterator;
		
		MyBufferedIterator() {
			storeIndexIterator = existingNodes.iterator();
		}
				
		@Override
		public boolean hasNext() {
			return check();
		}

		private boolean check() {
			while (entrySetIterator == null || !entrySetIterator.hasNext()) {
				if (storeIndexIterator.hasNext()) {
					Node<E> node = load(storeIndexIterator.next());
					if (node == null) {
						throw new IllegalStateException();
					}
					entrySetIterator = node.entrySet().iterator();
				} else {
					return false;
				}
			}
			return entrySetIterator != null && entrySetIterator.hasNext();
		}

		@Override
		public java.util.Map.Entry<Integer, E> next() {
			check();
			return entrySetIterator.next();
		}
	}
}
