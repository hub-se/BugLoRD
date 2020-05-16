package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import de.hammacher.util.Pair;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;


/**
 * Disk buffered Map implementation.
 *
 * @param <E> the type of elements held in the map(s)
 */
@CoverageIgnore
public class BufferedMap<E> implements Map<Integer, E>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3786896457427890598L;

    // keep at most 4 nodes in memory
    private static final int CACHE_SIZE = 4;

    private File output;
    private String filePrefix;

    private int maxSubMapSize = 500000;

    public int getMaxSubMapSize() {
        return maxSubMapSize;
    }

    private int size = 0;

    protected Set<Integer> existingNodes = new HashSet<>();

    private transient int lastStoreIndex = -1;

    protected transient Node<E> reusableNode = null;


    // cache all other nodes, if necessary
    protected transient Map<Integer, Node<E>> cachedNodes = null;
    private transient List<Integer> cacheSequence = null;

    protected transient boolean deleteOnExit;

	// if this is true, always store nodes, since added map 
	// elements may change AFTER they have been added to the map!
	private boolean addedElementsMayBeChange = false;

	private transient Node<E> lastObtainedNode;

	
	public Pair<Integer, Integer> calculateMinAndMaxNodeSizeStats() {
		int minSize = Integer.MAX_VALUE;
		int maxSize = 0;
		Iterator<Map<Integer, E>> subMapIterator = subMapIterator();
		while (subMapIterator.hasNext()) {
			Map<Integer, E> next = subMapIterator.next();
			minSize = Math.min(minSize, next.size());
			maxSize = Math.max(maxSize, next.size());
		}
		
		return new Pair<>(minSize, maxSize);
	}
	
	public double getAverageNodeSizeStats() {
		if (existingNodes.isEmpty()) {
			return Double.NaN;
		} else {
			return (double)size / (double)existingNodes.size();
		}
	}
	
	public String getStats() {
		Pair<Integer, Integer> pair = calculateMinAndMaxNodeSizeStats();
		return String.format("#entries: %,d, #files: %,d, entry cap: %,d, min/max/avg #entries per file: %,d/%,d/%.2f", 
				size, existingNodes.size(), maxSubMapSize, pair.getFirst(), pair.getSecond(), getAverageNodeSizeStats());
	}

    private void writeObject(java.io.ObjectOutputStream stream)
            throws IOException {
        sleep();
        stream.writeObject(output);
        stream.writeObject(filePrefix);
        stream.writeObject(existingNodes);
        stream.writeInt(maxSubMapSize);
        stream.writeInt(size);
        stream.writeBoolean(addedElementsMayBeChange);
    }

    public void sleep() {
        if (cachedNodes == null) {
            return;
        }
        for (Node<E> node : cachedNodes.values()) {
            // stores nodes, if nodes were modified
            // empty nodes are removed from the set of existing nodes
            sleep(node);
        }
        cachedNodes = null;
        cacheSequence = null;
        reusableNode = null;
    }

    private void sleep(Node<E> node) {
        store(node);
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        output = (File) stream.readObject();
        filePrefix = (String) stream.readObject();
        existingNodes = (Set<Integer>) stream.readObject();
        maxSubMapSize = stream.readInt();
        size = stream.readInt();
        addedElementsMayBeChange = stream.readBoolean();

//        cachedNodes = new HashMap<>();
//        cacheSequence = new LinkedList<>();
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
    
    public BufferedMap(File output, String filePrefix, int maxSubMapSize, boolean deleteOnExit, boolean addedElementsMayBeChange) {
        this(output, filePrefix, maxSubMapSize, deleteOnExit);
        this.addedElementsMayBeChange = addedElementsMayBeChange;
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
        clearCache();
        // delete possibly stored nodes
        for (Integer storeIndex : existingNodes) {
            delete(storeIndex);
        }
        existingNodes.clear();
        size = 0;
    }


    private void clearCache() {
        if (cachedNodes == null) {
            return;
        }
        for (Node<E> node : cachedNodes.values()) {
            node.clear();
        }
        cachedNodes = null;
        cacheSequence = null;
        reusableNode = null;
    }

    private void delete(Integer storeIndex) {
        String filename = getFileName(storeIndex);
        // stored node should be deleted
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
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
        this.lastObtainedNode = node;
        return node == null ? null : node.get(key);
    }

    @Override
    public E put(Integer key, E value) {
        if (value == null) {
            throw new IllegalStateException("Must not add null values to buffered map.");
        }
        int storeIndex = getStoreindex(key);

        // write last written node to disk if we go to the next one
        if (lastStoreIndex > -1 && storeIndex != lastStoreIndex) {
            Node<E> node = getNode(lastStoreIndex);
            if (node != null) {
                uncache(lastStoreIndex);
//				sleep(node);
            }
        }
        lastStoreIndex = storeIndex;

        Node<E> node = getOrCreateNode(storeIndex);
        E element = node.put(key, value);
        if (element == null) {
            ++size;
        }
        return element;
    }

    // should check for modifications and possibly write to the disk
    private void uncache(int storeIndex) {
        if (cachedNodes == null) {
            return;
        }
        Node<E> node = cachedNodes.remove(storeIndex);
        cacheSequence.remove((Integer) storeIndex);
        store(node);
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
                    uncacheNoStore(node.storeIndex);
                    delete(node.storeIndex);
                    existingNodes.remove(node.storeIndex);
                }
            }
            return element;
        }
    }

    private void uncacheNoStore(int storeIndex) {
        if (cachedNodes == null) {
            return;
        }
        cachedNodes.remove(storeIndex);
        cacheSequence.remove((Integer) storeIndex);
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
    	lastObtainedNode = null;
        Node<E> node = reusableNode == null ?
                new Node<E>(key, maxSubMapSize) :
                reusableNode.recycle(key);
        reusableNode = null;
        existingNodes.add(key);
        cacheNode(node);
        return node;
    }

    private Node<E> load(int storeIndex) {
        if (!existingNodes.contains(storeIndex)) {
            return null;
        }

        if (cachedNodes != null && cachedNodes.containsKey(storeIndex)) {
            // already cached
            return cachedNodes.get(storeIndex);
        }

        String filename = getFileName(storeIndex);

        Node<E> loadedNode = load(storeIndex, filename);

        cacheNode(loadedNode);
        return loadedNode;
    }

    protected Node<E> load(int storeIndex, String filename) throws IllegalStateException {
    	lastObtainedNode = null;
        Node<E> loadedNode;
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filename))) {
            @SuppressWarnings("unchecked")
            Map<Integer, E> map = (Map<Integer, E>) inputStream.readObject();
            loadedNode = new Node<>(storeIndex, map);
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
        return loadedNode;
    }

    protected void cacheNode(Node<E> node) {
        if (cachedNodes == null) {
            cachedNodes = new HashMap<>((int) (((float) CACHE_SIZE / 0.7F) + 1), 0.7F);
            cacheSequence = new LinkedList<>();
        }
        // cache the node
        if (cachedNodes.size() >= CACHE_SIZE) {
            // remove the node from the cache that has been added first
            Node<E> uncachedNode = cachedNodes.remove(cacheSequence.remove(0));
            store(uncachedNode);
        }
        cachedNodes.put(node.storeIndex, node);
        cacheSequence.add(node.storeIndex);
    }

    private Node<E> getNode(int storeIndex) {
        return load(storeIndex);
    }

    private void store(Node<E> node) {
        if (node == null) {
            return;
        }

        if (node.isEmpty()) {
            delete(node.storeIndex);
            existingNodes.remove(node.storeIndex);
            reusableNode = node.reset();
            return;
        } else if (node.modified || addedElementsMayBeChange) {
            String filename = getFileName(node.storeIndex);
            store(node, filename);
            node.modified = false;
            reusableNode = node.reset();
        }
    }

    protected void store(Node<E> node, String filename) {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filename))) {
            outputStream.writeObject(node.subMap);
            // do not delete on exit, due to serialization! TODO
            if (deleteOnExit) {
                new File(filename).deleteOnExit();
            }
        } catch (IOException e) {
            try {
				Files.deleteIfExists(Paths.get(filename));
				try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filename))) {
		            outputStream.writeObject(node.subMap);
		            // do not delete on exit, due to serialization! TODO
		            if (deleteOnExit) {
		                new File(filename).deleteOnExit();
		            }
		        }
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        }
    }


    public static class Node<E> implements Map<Integer, E> {

        private transient boolean modified = false;
        private Map<Integer, E> subMap;

        // index to store/load this node
        private int storeIndex;


        public boolean isModified() {
            return modified;
        }


        public Map<Integer, E> getSubMap() {
            return subMap;
        }


        public int getStoreIndex() {
            return storeIndex;
        }

        public Node(int storeIndex, int mapSize) {
            this.storeIndex = storeIndex;
            // ((float)s / loadFactor) + 1.0F
            this.subMap = new HashMap<>((int) (((float) mapSize / 0.7F) + 1), 0.7F);
            // ensure that new nodes are stored (if not empty)
            this.modified = true;
        }

        public Node(int storeIndex, Map<Integer, E> map) {
            this.storeIndex = storeIndex;
            this.subMap = map;
        }

        public Node<E> recycle(int storeIndex) {
//			System.out.println("map recycle " + this.storeIndex + " -> " + storeIndex);
            this.storeIndex = storeIndex;
            // ensure that new nodes are stored (if not empty)
            this.modified = true;
            return this;
        }

        public Node<E> recycle(int storeIndex, Map<Integer, E> subMap) {
//			System.out.println("map load recycle " + this.storeIndex + " -> " + storeIndex);
            this.storeIndex = storeIndex;
            this.subMap = subMap;
            this.modified = false;
            return this;
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
            cleanup();
        }

        public Node<E> reset() {
            // prepare for possible recycling
            subMap.clear();
            return this;
        }

        public Map<Integer, E> cleanup() {
            if (!subMap.isEmpty()) {
                modified = true;
            }
            subMap.clear();
            Map<Integer, E> temp = subMap;
            subMap = null;
            return temp;
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

    public Iterator<Map<Integer, E>> subMapIterator() {
        return new NodeIterator();
    }
    
    public Iterator<java.util.Map.Entry<Integer, E>> entrySetIterator() {
        return new MyBufferedIterator(false);
    }

    public Iterator<java.util.Map.Entry<Integer, E>> entrySetIterator(boolean sortNodeEntries) {
        return new MyBufferedIterator(sortNodeEntries);
    }

    private final class MyBufferedIterator implements Iterator<java.util.Map.Entry<Integer, E>> {

        private class KeyComp implements Comparator<Entry<Integer, E>> {
            @Override
            public int compare(Entry<Integer, E> o1, Entry<Integer, E> o2) {
                return Integer.compare(o1.getKey(), o2.getKey());
            }
        }

        private final NodeIterator nodeIterator;
        private Iterator<java.util.Map.Entry<Integer, E>> entrySetIterator;
        private boolean sortNodeEntries;

        public MyBufferedIterator(boolean sortNodeEntries) {
            this.sortNodeEntries = sortNodeEntries;
            this.nodeIterator = new NodeIterator();
        }

        @Override
        public boolean hasNext() {
            return check();
        }

        private boolean check() {
            while (entrySetIterator == null || !entrySetIterator.hasNext()) {
                if (nodeIterator.hasNext()) {
                	Map<Integer, E> node = nodeIterator.next();
                    if (sortNodeEntries) {
                        List<Entry<Integer, E>> list = new ArrayList<>(node.entrySet());
                        Collections.sort(list, new KeyComp());
                        entrySetIterator = list.iterator();
                    } else {
                        entrySetIterator = node.entrySet().iterator();
                    }
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
    
    private final class NodeIterator implements Iterator<Map<Integer, E>> {

    	private final Iterator<Integer> storeIndexIterator;
    	
    	public NodeIterator() {
    		List<Integer> list = new ArrayList<>(existingNodes);
            Collections.sort(list);
            storeIndexIterator = list.iterator();
		}
    	
		@Override
		public boolean hasNext() {
			return storeIndexIterator.hasNext();
		}

		@Override
		public Map<Integer, E> next() {
			Node<E> node = load(storeIndexIterator.next());
        	if (node == null) {
        		throw new IllegalStateException();
        	}
			return node.getSubMap();
		}
    	
    }

    public boolean isDeleteOnExit() {
        return deleteOnExit;
    }

    public void deleteOnExit() {
        deleteOnExit = true;
    }

	public void setLastObtainedNodeToModified() {
		if (lastObtainedNode != null) {
			lastObtainedNode.modified = true;
		}
	}

}
