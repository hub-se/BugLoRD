package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
public class BufferedMap<E> implements Map<Integer, E>, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3786896457427890598L;

	// keep at most 3 nodes in memory
    private static final int CACHE_SIZE = 3;
    
	private File output;
	private String filePrefix;
	
	private int maxSubMapSize = 500000;
	
	public int getMaxSubMapSize() {
		return maxSubMapSize;
	}

	private int size = 0;
	
	protected Set<Integer> existingNodes = new HashSet<>();

	
	protected transient Lock lock = new ReentrantLock();
	
	// cache all other nodes, if necessary
	protected transient Map<Integer,Node<E>> cachedNodes = new HashMap<>();
	private transient List<Integer> cacheSequence = new LinkedList<>();

	protected transient boolean deleteOnExit;

	
	
	private void writeObject(java.io.ObjectOutputStream stream)
            throws IOException {
		sleep();
        stream.writeObject(output);
        stream.writeObject(filePrefix);
        stream.writeObject(existingNodes);
        stream.writeInt(maxSubMapSize);
        stream.writeInt(size);
    }

	public void sleep() {
		lock.lock();
		try {
			for (Node<E> node : cachedNodes.values()) {
				// stores nodes, if nodes were modified
				// empty nodes are removed from the set of existing nodes
				store(node);
			}
			cachedNodes.clear();
			cacheSequence.clear();
		} finally {
			lock.unlock();
		}
	}

    @SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        output = (File) stream.readObject();
        filePrefix = (String) stream.readObject();
        existingNodes = (Set<Integer>) stream.readObject();
        maxSubMapSize = stream.readInt();
        size = stream.readInt();
        
        lock = new ReentrantLock();
        cachedNodes = new HashMap<>();
        cacheSequence = new LinkedList<>();
        // always delete files from deserialized object!? TODO
        deleteOnExit = true;
    }

    public BufferedMap(File outputDir, String filePrefix, boolean deleteOnExit) {
    	this.deleteOnExit = deleteOnExit;
		check(outputDir);
		this.output = outputDir;
		this.filePrefix = Objects.requireNonNull(filePrefix);
    }
    
    public BufferedMap(File outputDir, String filePrefix) {
    	this(outputDir, filePrefix, true);
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

	public BufferedMap(File output, String filePrefix, int maxSubMapSize, boolean deleteOnExit) {
    	this(output, filePrefix, deleteOnExit);
    	this.maxSubMapSize = maxSubMapSize < 1 ? 1 : maxSubMapSize;
    }
	
	public BufferedMap(File output, String filePrefix, int maxSubMapSize) {
    	this(output, filePrefix, maxSubMapSize, true);
    }
	
	public File getOutputDir() {
		return output;
	}
	
	public String getFilePrefix() {
		return filePrefix;
	}

	protected String getFileName(int storeIndex) {
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
		// due to serialization, we must not delete nodes! TODO
		if (deleteOnExit) {
			this.clear();
		}
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
		return node != null && node.containsKey(key);
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
					existingNodes.remove(node.storeIndex);
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
	
	protected Node<E> load(int storeIndex) {
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
				loadedNode = new Node<>(storeIndex, map);
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
				throw new IllegalStateException();
			}

			cacheNode(loadedNode);
			return loadedNode;
		} finally {
			lock.unlock();
		}
	}

	protected void cacheNode(Node<E> node) {
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

	protected void store(Node<E> node) {
		if (node == null) {
			return;
		}
		if (node.isEmpty()) {
			existingNodes.remove(node.storeIndex);
			return;
		}
		if (node.modified) {
			String filename = getFileName(node.storeIndex);
			try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filename))) {
				outputStream.writeObject(node.subMap);
				// do not delete on exit, due to serialization! TODO
				if (deleteOnExit) {
					new File(filename).deleteOnExit();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	public static class Node<E> implements Map<Integer, E> {
        
		private transient boolean modified = false;
		private final Map<Integer,E> subMap;

        // index to store/load this node
		private final int storeIndex;

        
		public boolean isModified() {
			return modified;
		}

		
		public Map<Integer, E> getSubMap() {
			return subMap;
		}

		
		public int getStoreIndex() {
			return storeIndex;
		}

		public Node(int storeIndex) {
        	this.storeIndex = storeIndex;
        	this.subMap = new HashMap<>();
        	// ensure that new nodes are stored (if not empty)
        	this.modified = true;
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
			modified = true;
			return subMap.put(key, value);
		}

		@Override
		public E remove(Object key) {
			E e = subMap.remove(key);
			if (e != null) {
				modified = true;
			}
			return e;
		}

		@Override
		public void putAll(Map<? extends Integer, ? extends E> m) {
			modified = true;
			subMap.putAll(m);
		}

		@Override
		public void clear() {
			if (!subMap.isEmpty()) {
				modified = true;
			}
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
	
	private final class MyBufferedIterator implements Iterator<java.util.Map.Entry<Integer,E>> {

		private final Iterator<Integer> storeIndexIterator;
		private Iterator<java.util.Map.Entry<Integer,E>> entrySetIterator;
		
		public MyBufferedIterator() {
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
			return true;
		}

		@Override
		public java.util.Map.Entry<Integer, E> next() {
			check();
			return entrySetIterator.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	public boolean isDeleteOnExit() {
		return deleteOnExit;
	}
	
	public void deleteOnExit() {
		deleteOnExit = true;
	}
	
}
