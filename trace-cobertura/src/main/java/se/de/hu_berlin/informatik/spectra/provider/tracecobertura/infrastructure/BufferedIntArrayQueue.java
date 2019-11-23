package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.Function;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.ReplaceableCloneableIntIterator;

/**
 * Simple single linked queue implementation using fixed/variable size array nodes.
 */
public class BufferedIntArrayQueue implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1777403971930917719L;

	// keep at most 3 (+1 with the last node) nodes in memory
    private static final int CACHE_SIZE = 3;
    
    private static final int ARRAY_SIZE = 1000;
	
	private int arrayLength = ARRAY_SIZE;
	
	public int getArrayLength() {
		return arrayLength;
	}

	private volatile int size = 0;
	
	private File output;
	private String filePrefix;
	private volatile int firstStoreIndex = 0;
	private volatile int currentStoreIndex = -1;
	private volatile int lastStoreIndex = -1;
	
	private volatile int firstNodeSize = 0;
	
	private volatile transient Node lastNode;
	
	private transient Lock lock = new ReentrantLock();
	
	// cache all other nodes, if necessary
	private transient Map<Integer,Node> cachedNodes = new HashMap<>();
	private transient List<Integer> cacheSequence = new LinkedList<>();

	private transient boolean deleteOnExit;
	
	private void writeObject(java.io.ObjectOutputStream stream)
            throws IOException {
		sleep();
        stream.writeObject(output);
        stream.writeObject(filePrefix);
//        stream.writeObject(lastNode);
        stream.writeInt(firstStoreIndex);
        stream.writeInt(currentStoreIndex);
        stream.writeInt(lastStoreIndex);
        stream.writeInt(firstNodeSize);
        stream.writeInt(size);
        stream.writeInt(arrayLength);
    }
	
	private volatile transient boolean locked = false;
	
	public void lock() {
		this.locked = true;
	}
	
	public void unlock() {
		this.locked = false;
	}

	// stores all nodes on disk
	public void sleep() {
		lock.lock();
		try {
			for (Node node : cachedNodes.values()) {
				// stores cached nodes, if modified (i.e., if elements were added/removed)
				if (node.modified) {
					store(node);
				}
			}
			cachedNodes.clear();
			cacheSequence.clear();
			// store the last node, too
			if (lastNode != null && lastNode.modified) {
				store(lastNode);
				lastNode = null;
			}
		} finally {
			lock.unlock();
		}
	}

	private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        output = (File) stream.readObject();
        filePrefix = (String) stream.readObject();
//        lastNode = (Node<E>) stream.readObject();
        firstStoreIndex = stream.readInt();
        currentStoreIndex = stream.readInt();
        lastStoreIndex = stream.readInt();
        firstNodeSize = stream.readInt();
        size = stream.readInt();
        arrayLength = stream.readInt();
        
        lock = new ReentrantLock();
        cachedNodes = new HashMap<>();
        cacheSequence = new LinkedList<>();
        // always delete files from deserialized object TODO
        deleteOnExit = true;
    }
    
    public BufferedIntArrayQueue(File outputDir, String filePrefix, boolean deleteOnExit) {
    	this.deleteOnExit = deleteOnExit;
		check(outputDir);
		this.output = outputDir;
		this.filePrefix = Objects.requireNonNull(filePrefix);
		initialize();
    }
    
    private void initialize() {
		firstStoreIndex = 0;
		lastStoreIndex = -1;
		if (lastNode != null) {
//			for (int j = lastNode.startIndex; j < lastNode.endIndex; ++j) {
//				lastNode.items[j] = null;
//			}
			uncacheAndDelete(lastNode.storeIndex);
//			lastNode = null;
		}
		if (lastNode != null) {
			lastNode.modified = false;
			lastNode.storeIndex = 0;
			lastNode.startIndex = 0;
			lastNode.endIndex = 0;
			currentStoreIndex = 0;
		} else {
			currentStoreIndex = -1;
		}
		firstNodeSize = 0;
		size = 0;
	}
    
    public BufferedIntArrayQueue(File outputDir, String filePrefix) {
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

	public BufferedIntArrayQueue(File output, String filePrefix, int nodeArrayLength, boolean deleteOnExit) {
    	this(output, filePrefix, deleteOnExit);
    	this.arrayLength = nodeArrayLength < 1 ? 1 : nodeArrayLength;
    }
	
	public BufferedIntArrayQueue(File output, String filePrefix, int nodeArrayLength) {
    	this(output, filePrefix, nodeArrayLength, true);
    }
	
	public int getNodeSize() {
		return arrayLength;
	}
	
	public File getOutputDir() {
		return output;
	}
	
	public String getFilePrefix() {
		return filePrefix;
	}

    /**
     * Creates a new node if the last node is null or full.
     * Increases the size variable.
     * Stores full nodes to the disk.
     */
    private void linkLast(int e) {
        final Node l = loadLast();
        final Node newNode = new Node(e, arrayLength, ++currentStoreIndex);
        lastNode = newNode;
        if (l == null) {
        	// no nodes did exist, previously
			++firstNodeSize;
        } else {
        	// we don't actually use pointers!
//            l.next = newNode;
        	++lastStoreIndex;
        	putInCache(l.storeIndex, l);
//        	store(l);
//        	// we can now remove the node from memory
//        	l.items = null;
        }
        ++size;
    }
    
    private transient ByteBuffer writeBuffer = null;
    
    private ByteBuffer getFreshBuffer() {
    	// only (lazily) allocate ONE buffer per buffered queue object! allocation costs are potentially high...
    	if (writeBuffer == null) {
    		writeBuffer = ByteBuffer.allocateDirect(4 * arrayLength + 8);
    	}
    	writeBuffer.clear();
    	return writeBuffer;
    }
    
    private void store(Node node) {
//    	System.out.println("imem: " + Runtime.getRuntime().freeMemory());
		String filename = getFileName(node.storeIndex);
		// apparently, this is faster than using an ObjectOutputStream...
		try (RandomAccessFile raFile = new RandomAccessFile(filename, "rw")) {
			try (FileChannel file = raFile.getChannel()) {
//				ByteBuffer buf = file.map(FileChannel.MapMode.READ_WRITE, 0, 4 * node.items.length + 8);
//				buf.putInt(node.startIndex);
//				buf.putInt(node.endIndex);
//				for (int i : node.items) {
//					buf.putInt(i);
//				}
				
				ByteBuffer directBuf = getFreshBuffer();
				directBuf.putInt(node.startIndex);
				directBuf.putInt(node.endIndex);
				for (int i = node.startIndex; i < node.endIndex; ++i) {
					directBuf.putInt(node.items[i]);
				}
				directBuf.flip();
				file.write(directBuf);

				// file can not be removed, due to serialization! TODO
				if (deleteOnExit) {
					new File(filename).deleteOnExit();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filename))) {
//			outputStream.writeObject(node.items);
//			
//			outputStream.writeInt(node.startIndex);
//			outputStream.writeInt(node.endIndex);
//			
//			// file can not be removed, due to serialization! TODO
//			if (deleteOnExit) {
//				new File(filename).deleteOnExit();
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
    
    private void putInCache(int storeIndex, Node node) {
    	lock.lock();
    	try {
    		// already in the cache?
    		if (cachedNodes.containsKey(storeIndex)) {
    			return;
    		}
    		// remove a cached node if the cache is full
    		if (cachedNodes.size() >= CACHE_SIZE) {
    			// remove the node from the cache that has been added first
    			uncache(cacheSequence.remove(0));
    		}
    		cachedNodes.put(storeIndex, node);
    		cacheSequence.add(storeIndex);
    	} finally {
    		lock.unlock();
    	}
	}

	/**
     * Removes the first node, if it only contains one item.
     * Decreases the size variable.
     * 
     */
    private int unlinkFirst(Node f) {
        // assert f == first && f != null;
		final int element = f.items[f.startIndex];
//        final Node<E> next = f.next;
        if (storedNodeExists()) {
        	// there exists a stored node on disk;
            f.items = null; // help GC
        	uncacheAndDelete(firstStoreIndex);
        	++firstStoreIndex;
            --size;
            if (storedNodeExists()) {
            	firstNodeSize = arrayLength;
            } else {
            	firstNodeSize = size;
            }
        } else {
//        	// there exists only the last node
//        	// keep one node/array to avoid having to allocate a new one
//        	lastNode.startIndex = 0;
//        	lastNode.endIndex = 0;
        	// no further nodes
        	initialize();
        }
        return element;
    }

	private void uncacheAndDelete(int storeIndex) {
		lock.lock();
		try {
			String filename = getFileName(storeIndex);
			if (cachedNodes.containsKey(storeIndex)) {
				cachedNodes.remove(storeIndex);
				cacheSequence.remove((Integer)storeIndex);
			}
			// stored node should be deleted
			File file = new File(filename);
			if (file.exists()) {
				file.delete();
			}
		} finally {
			lock.unlock();
		}
	}

	private String getFileName(int storeIndex) {
		return output.getAbsolutePath() + File.separator + filePrefix + "-" + storeIndex + ".rry";
	}

//	private Node<E> load() {
//		String filename = getFileName(firstStoreIndex);
//		if (cachedNodes.containsKey(firstStoreIndex)) {
//			// node was cached, previously
//			Node<E> node = cachedNodes.get(firstStoreIndex);
//			cacheSequence.remove(firstStoreIndex);
//			// stored node can/should be deleted
//			new File(filename).delete();
//			++firstStoreIndex;
//			return node;
//		} else {
//			Node<E> loadedNode;
//			try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filename))) {
//				Object[] items = (Object[])inputStream.readObject();
//				loadedNode = new Node<E>(items);
//				// stored node can/should be deleted
//				new File(filename).delete();
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//				throw new IllegalStateException();
//			} catch (IOException e) {
//				e.printStackTrace();
//				throw new IllegalStateException();
//			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
//				throw new IllegalStateException();
//			}
//			++firstStoreIndex;
//			return loadedNode;
//		}
//	}
	
	private Node loadFirst() {
		if (storedNodeExists()) {
			return load(firstStoreIndex);
		} else {
			return loadLast();
		}
	}
	
	private Node loadLast() {
		if (lastNode == null) {
			lastNode = load(lastStoreIndex+1);
			return lastNode;
		} else {
			return lastNode;
		}
	}
	
	private Node load(int storeIndex) {
		if (lastNode != null && storeIndex == lastNode.storeIndex) {
			return lastNode;
		}
//		if (storeIndex > lastStoreIndex) {
//			return null;
//		}
		
		lock.lock();
		try {
			// only cache nodes that are not the last node
//			if (storeIndex <= lastStoreIndex) {
			if (cachedNodes.containsKey(storeIndex)) {
				// already cached
				return cachedNodes.get(storeIndex);
			}
//			}
			
			String filename = getFileName(storeIndex);
			if (!(new File(filename).exists())) {
				return null;
//				throw new IllegalStateException("Node " + storeIndex + " was neither found in the cache or as a file.");
			}
			
			Node loadedNode;
			try (FileInputStream in = new FileInputStream(filename)) {
				try (FileChannel file = in.getChannel()) {
					long fileSize = file.size();
					if (fileSize > Integer.MAX_VALUE) {
						throw new UnsupportedOperationException("File size too big!");
					}
					ByteBuffer directBuf = getFreshBuffer();
					file.read(directBuf);
					directBuf.flip();
					
					int startIndex = directBuf.getInt();
					int endIndex = directBuf.getInt();
					
//					int arrayLength = (int)fileSize/4 - 2;
					// actually only load an array of the size that's necessary;
					// will be extended if there are new elements that are added
					int[] items = new int[endIndex];
					for (int i = startIndex; i < endIndex; ++i) {
						items[i] = directBuf.getInt();
					}
					
					loadedNode = new Node(items, startIndex, endIndex, storeIndex, arrayLength);
					
					// file can not be removed, due to serialization! TODO
					if (deleteOnExit) {
						new File(filename).deleteOnExit();
					}
				}
			} catch (IOException | IndexOutOfBoundsException e) {
				e.printStackTrace();
				throw new IllegalStateException();
			}
//			try (RandomAccessFile raFile = new RandomAccessFile(filename, "r")) {
//				try (FileChannel file = raFile.getChannel()) {
//					long fileSize = file.size();
//					ByteBuffer buf = file.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
//					int startIndex = buf.getInt();
//					int endIndex = buf.getInt();
//
//					int arrayLength = (int) (fileSize/4 - 2);
//					int[] items = new int[arrayLength];
//					for (int i = 0; i < arrayLength; ++i) {
//						items[i] = buf.getInt();
//					}
//
//					loadedNode = new Node(items, startIndex, endIndex, storeIndex);
//				} 
//			} catch (IOException | IndexOutOfBoundsException e) {
//				e.printStackTrace();
//				throw new IllegalStateException();
//			}
			
//			try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filename))) {
//				int[] items = (int[]) inputStream.readObject();
//				
//				
//				
//				loadedNode = new Node(items, startIndex, endIndex, storeIndex);
//			} catch (ClassNotFoundException | IOException e) {
//				e.printStackTrace();
//				throw new IllegalStateException();
//			}
			
			if (storeIndex <= lastStoreIndex) {
				putInCache(storeIndex, loadedNode);
			}
			return loadedNode;
		} finally {
			lock.unlock();
		}
	}

	// should check for modifications and possibly write to the disk
    private void uncache(int storeIndex) {
    	lock.lock();
    	try {
    		Node node = cachedNodes.remove(storeIndex);
    		if (node != null) {
    			if (node.modified) {
    				store(node);
    			}
    		}
    	} finally {
    		lock.unlock();
    	}
	}

    public int size() {
    	lock.lock();
    	try {
    		return size;
    	} finally {
    		lock.unlock();
    	}
    }

    public boolean add(int e) {
    	if (locked) {
    		throw new IllegalStateException("Tried to add value " + e + " while being locked.");
    	}
    	lock.lock();
    	try {
    		loadLast();
    		if (lastNode != null && lastNode.hasFreeSpace()) {
    			lastNode.add(e);
    			++size;
    			if (firstStoreIndex == lastNode.storeIndex) {
    				++firstNodeSize;
    			}
//    			System.out.println(e + " -> added " + lastNode.storeIndex + ", " + lastNode.endIndex);
    		} else {
    			linkLast(e);
//    			System.out.println(e + " -> linked " + lastNode.storeIndex + ", " + lastNode.endIndex);
    		}
    		return true;
    	} finally {
    		lock.unlock();
    	}
    }

    public void clear() {
    	if (locked) {
    		throw new IllegalStateException("Tried to clear queue while being locked.");
    	}
    	lock.lock();
    	try {
    		cachedNodes.clear();
    		cacheSequence.clear();
    		// delete possibly stored nodes
    		for (; storedNodeExists(); ++firstStoreIndex) {
    			uncacheAndDelete(firstStoreIndex);
    		}
//    		// empty last (and now also first) node
//    		if (lastNode != null) {
//    			for (int j = lastNode.startIndex; j < lastNode.endIndex; ++j) {
//    				lastNode.items[j] = null;
//    			}
//    			// keep one node/array to avoid having to allocate a new one
//    			lastNode.startIndex = 0;
//    			lastNode.endIndex = 0;
//    		}
    		initialize();
    	} finally {
    		lock.unlock();
		}
    }

	private boolean storedNodeExists() {
		lock.lock();
		try {
			return firstStoreIndex <= lastStoreIndex;
		} finally {
			lock.unlock();
		}
	}
    
    /**
     * Same as {@link #clear()}, but only removes the first {@code count} elements.
     * 
     * @param count
     * the number of elements to clear from the list
     */
    public void clear(int count) {
    	if (count >= size) {
			clear();
			return;
		}
    	if (locked) {
    		throw new IllegalStateException("Tried to clear queue while being locked.");
    	}
    	lock.lock();
    	try {
    		Node x = null;
    		if (storedNodeExists()) {
    			// do not load nodes, if possible
    			while (count >= firstNodeSize) {
    				// we can just delete the file without looking at the node
    				uncacheAndDelete(firstStoreIndex);
    				++firstStoreIndex;
    				count -= firstNodeSize;
    				size -= firstNodeSize;
    				// still a node on disk?
    				if (storedNodeExists()) {
    					firstNodeSize = arrayLength;
    				} else {
    					break;
    				}
    			}

    			// still elements left to clear?
    			if (count > 0) {
    				if (storedNodeExists()) {
    					x = load(firstStoreIndex);
    				} else {
    					loadLast();
    					// no node left on disk, so continue with the last node
    					firstNodeSize = lastNode.endIndex - lastNode.startIndex;
    					x = lastNode;
    				}
    			}
    		} else {
    			loadLast();
    			x = lastNode;
    		}
    		
    		if (count > 0 && x != null) {
    			// remove elements from first node
    			x.startIndex += count;
    			firstNodeSize -= count;
    			x.modified = true;
    		}
    		
    		size -= count;
    	} finally {
    		lock.unlock();
		}
    }
    
    /**
     * Same as {@link #clear()}, but only removes the last {@code count} elements.
     * 
     * @param count
     * the number of elements to clear from the list
     */
    public void clearLast(int count) {
    	if (count >= size) {
			clear();
			return;
		}
    	if (locked) {
    		throw new IllegalStateException("Tried to clear queue while being locked.");
    	}
    	lock.lock();
    	try {
    		loadLast();
    		int removedElements = lastNode.size();
			int storeIndex = lastNode.storeIndex;

			if (count >= removedElements) {
				// we can delete the last node's file
				uncacheAndDelete(storeIndex);
				--lastStoreIndex;
				--currentStoreIndex;
				count -= removedElements;
				size -= removedElements;
				lastNode = null;
			}

			// check if we can delete entire nodes without actually loading the respective nodes
			while (count >= arrayLength) {
				// we can just delete the respective file
				uncacheAndDelete(--storeIndex);
				--lastStoreIndex;
				--currentStoreIndex;
				count -= arrayLength;
				size -= arrayLength;
			}

			// still elements left to clear?
			if (count > 0) {
				// it holds that count < arrayLength
				loadLast();
	    		lastNode.modified = true;
	    		lastNode.endIndex -= count;
				if (!storedNodeExists()) {
					// no node left on disk, so continue with the last node
					firstNodeSize = lastNode.endIndex - lastNode.startIndex;
				}
			}

    		size -= count;
    	} finally {
    		lock.unlock();
		}
    }
    
    /**
     * Same as {@link #clear()}, but removes {@code count} elements, 
     * starting from {@code startIndex}.
     * 
     * @param startIndex
     * the index to start clearing from
     * @param count
     * the number of elements to clear from the list
     */
    public void clearFrom(int startIndex, int count) {
    	if (count == 0) {
    		return;
    	}
    	if (count >= size) {
			clear();
			return;
		}
    	if (startIndex + count >= size) {
			clearLast(size - startIndex);
			return;
		}
    	if (locked) {
    		throw new IllegalStateException("Tried to clear queue while being locked.");
    	}
    	lock.lock();
    	try {
    		int startNodeIndex = -1;
    		int startItemIndex = startIndex;
    		Node startNode = null;
    		// we can compute the store index using the size of the 
    		// first node and the constant size of each array node
    		if (startIndex < firstNodeSize) {
    			startNode = loadFirst();
    	        if (startNode == null || startNode.startIndex >= startNode.endIndex)
    	            throw new NoSuchElementException();
    	        startNodeIndex = startNode.storeIndex;
    		} else {
    			int i = startIndex - firstNodeSize;
    			startNodeIndex = firstStoreIndex + 1 + (i / arrayLength);
    			startItemIndex = i % arrayLength;
    			startNode = load(startNodeIndex);
    		}

    		int endItemIndex = startIndex + count;

    		// shift the remaining elements to the left
    		MyBufferedIntIterator iterator = iterator(endItemIndex);
    		while (iterator.hasNext()) {
    			// since we keep a reference to the node here, we might make changes to it,
    			// but we might also have remove the node from the cache earlier
    			startNode.set(startItemIndex++, iterator.next());
    			if (startNode.startIndex + startItemIndex >= startNode.endIndex) {
    				// ensure that the node is in the cache after being modified
    				putInCache(startNode.storeIndex, startNode);
    				startItemIndex = 0;
    				startNode = load(++startNodeIndex);
    			}
    		}
    		// ensure that the node is in the cache after being modified
			putInCache(startNode.storeIndex, startNode);
    		
    		// now just remove the duplicate entries at the end
    		clearLast(count);
    		
//    		System.out.println();
//    		MyBufferedIntIterator iterator2 = iterator(0);
//    		while (iterator2.hasNext()) {
//    			System.out.print(iterator2.next() + ",");
//    		}
//    		System.out.println();
    		
    	} finally {
    		lock.unlock();
		}
    }

//	public int peekLast() {
//        final Node f = loadLast();
//        return ((f == null) ? null : (f.startIndex < f.endIndex ? f.items[f.endIndex-1] : null));
//    }
    
    public int lastElement() {
    	lock.lock();
    	try {
    		final Node f = loadLast();
    		if (f == null || f.startIndex >= f.endIndex)
    			if (f== null) 
    				throw new NoSuchElementException("size: " + size);
    			else
    				throw new NoSuchElementException("startindex: " + (f.startIndex) + ", endindex: " + (f.endIndex) + ", size: " + size);
    		return f.items[f.endIndex-1];
    	} finally {
    		lock.unlock();
    	}
    }
    
    // Queue operations

//    public int peek() {
//        final Node f = loadFirst();
//        return ((f == null) ? null : (f.startIndex < f.endIndex ? f.items[f.startIndex] : null));
//    }

    public int element() {
    	lock.lock();
    	try {
    		final Node f = loadFirst();
    		if (f == null || f.startIndex >= f.endIndex)
    			if (f== null) 
    				throw new NoSuchElementException("size: " + size);
    			else
    				throw new NoSuchElementException("startindex: " + (f.startIndex) + ", endindex: " + (f.endIndex) + ", size: " + size);
    		return f.items[f.startIndex];
    	} finally {
    		lock.unlock();
    	}
    }

//    public int poll() {
//        final Node f = loadFirst();
//        return (f == null) ? null : (f.startIndex < f.endIndex ? removeFirst(f) : null);
//    }

    public int remove() {
    	if (locked) {
			throw new IllegalStateException("Tried to remove element while being locked.");
		}
    	lock.lock();
    	try {
    		final Node f = loadFirst();
    		if (f == null || f.startIndex >= f.endIndex)
    			if (f== null) 
    				throw new NoSuchElementException("size: " + size);
    			else
    				throw new NoSuchElementException("startindex: " + (f.startIndex) + ", endindex: " + (f.endIndex) + ", size: " + size);
    		return removeFirst(f);
    	} finally {
    		lock.unlock();
    	}
    }
    
    /*
     * Removes the first element.
     */
    private int removeFirst(Node f) {
    	if (f.startIndex < f.endIndex - 1) {
    		--size;
    		--firstNodeSize;
    		return f.remove();
    	} else {
    		return unlinkFirst(f);	
		}
    }

    public boolean offer(int e) {
        return add(e);
    }

//    @Override
//    public Object[] toArray() {
//        Object[] result = new Object[size];
//        int i = 0;
//        for (Node<E> x = first; x != null; x = x.next)
//            result[i++] = x.item;
//        return result;
//    }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    public <T> T[] toArray(T[] a) {
//        if (a.length < size)
//            a = (T[])java.lang.reflect.Array.newInstance(
//                                a.getClass().getComponentType(), size);
//        int i = 0;
//        Object[] result = a;
//        for (Node<E> x = first; x != null; x = x.next)
//            result[i++] = x.item;
//
//        if (a.length > size)
//            a[size] = null;
//
//        return a;
//    }

	public boolean isEmpty() {
		lock.lock();
    	try {
    		return size == 0;
    	} finally {
			lock.unlock();
		}
	}

	public MyBufferedIntIterator iterator() {
		return new MyBufferedIntIterator();
	}
	
	public MyBufferedIntIterator iterator(final int position) {
		return new MyBufferedIntIterator(position);
	}

	public final class MyBufferedIntIterator implements ReplaceableCloneableIntIterator {

		int storeIndex;
		int index;

		MyBufferedIntIterator(int i) {
			setToPosition(i);
		}
		
		public void setToPosition(int i) {
			// we can compute the store index using the size of the 
			// first node and the constant size of each array node
			if (i < firstNodeSize) {
				storeIndex = firstStoreIndex;
				Node node = load(storeIndex);
				index = i+node.startIndex;
			} else {
				i -= firstNodeSize;
				storeIndex = firstStoreIndex + 1 + (i / arrayLength);
				index = i % arrayLength;
			}
		}
		
		MyBufferedIntIterator() {
			if (storedNodeExists()) {
				storeIndex = firstStoreIndex;
				Node node = load(storeIndex);
				index = node.startIndex;
			} else if (loadLast() != null) {
				storeIndex = lastNode.storeIndex;
				index = lastNode.startIndex;
			} else {
				storeIndex = -1;
				index = -1;
			}
		}
		
		// clone constructor
		private MyBufferedIntIterator(MyBufferedIntIterator iterator) {
			storeIndex = iterator.storeIndex;
			index = iterator.index;
		}

		public MyBufferedIntIterator clone() {
			return new MyBufferedIntIterator(this);
		}
		
		public boolean hasNext() {
			if (storeIndex < 0) {
				return false;
			}
			Node node = load(storeIndex);
			return node != null && index < node.endIndex;
		}

		public int next() {
			Node currentNode = load(storeIndex);
			int temp = currentNode.items[index++];
			if (index >= currentNode.endIndex) {
				if (storeIndex < lastStoreIndex) {
					++storeIndex;
				} else if (loadLast() != null) {
					if (storeIndex >= lastNode.storeIndex) {
						// already at the last node
						storeIndex = -1;
					} else {
						// process the last node next
						storeIndex = lastNode.storeIndex;
					}
				} else {
					// should really not happen...
					storeIndex = -1;
				}
				index = 0;
			}
			return temp;
		}
		
		@Override
		public int processNextAndReplaceWithResult(Function<Integer,Integer> function) {
			Node currentNode = load(storeIndex);
			int temp = currentNode.items[index];
			// replace item with function's result; otherwise the same as next()
			currentNode.items[index] = function.apply(temp);
			index++;
			if (index >= currentNode.endIndex) {
				if (storeIndex < lastStoreIndex) {
					++storeIndex;
				} else if (loadLast() != null) {
					if (storeIndex >= lastNode.storeIndex) {
						// already at the last node
						storeIndex = -1;
					} else {
						// process the last node next
						storeIndex = lastNode.storeIndex;
					}
				} else {
					// should really not happen...
					storeIndex = -1;
				}
				index = 0;
			}
			return temp;
		}

		public int peek() {
			Node currentNode = load(storeIndex);
			return currentNode.items[index];
		}

//		public void remove() {
//			throw new UnsupportedOperationException();
//		}
	}

	private static class Node implements Serializable {
		private volatile transient boolean modified = false;

		/**
		 * 
		 */
		private static final long serialVersionUID = 2511188925909568960L;

		// index to store/load this node
		private volatile int storeIndex;
		
		private int[] items;
        // points to first actual item slot
        private volatile int startIndex = 0;
        // points to last free slot
        private volatile int endIndex = 1;

		private int arrayLength;
        // pointer to next array node

		Node(int element, int arrayLength, int storeIndex) {
			this.arrayLength = arrayLength;
			if (arrayLength < 1) {
        		throw new IllegalStateException();
        	}
            this.items = new int[arrayLength];
            items[0] = element;
			this.storeIndex = storeIndex;
			// ensure that new nodes are stored (if not empty)
        	this.modified = true;
		}

		public int size() {
			return endIndex - startIndex;
		}

		public Node(int[] items, int startIndex, int endIndex, int storeIndex, int arrayLength) {
			this.items = items;
			this.endIndex = endIndex;
			this.startIndex = startIndex;
			this.storeIndex = storeIndex;
			this.arrayLength = arrayLength;
		}
		
		// removes the first element
        public int remove() {
        	this.modified = true;
//			int temp = items[startIndex];
//        	items[startIndex++] = 0;
			return items[startIndex++];
		}

        // adds an element to the end
		public void add(int e) {
			this.modified = true;
			if (items.length < arrayLength) {
				extendArray();
			}
			items[endIndex++] = e;
		}

		private void extendArray() {
			int[] temp = items;
			items = new int[arrayLength];
			System.arraycopy(temp, startIndex, items, startIndex, temp.length - startIndex);
		}

		// checks whether an element can be added to the node's array
		boolean hasFreeSpace() {
        	return endIndex < arrayLength;
        }

		public int get(int i) {
			return items[i+startIndex];
		}
		
		public void set(int i, int value) {
			this.modified = true;
			if (items.length <= i + startIndex) {
				extendArray();
			}
			items[i+startIndex] = value;
		}

	}
	
	@Override
	public void finalize() throws Throwable {
		// due to serialization, we need to rely on the user to clear queues manually TODO
		if (deleteOnExit) {
			this.clear();
		}
		super.finalize();
	}

	public int get(int i) {
		// we can compute the store index using the size of the 
		// first node and the constant size of each array node
		if (i < firstNodeSize) {
			final Node f = loadFirst();
	        if (f == null || f.startIndex >= f.endIndex)
	            throw new NoSuchElementException();
	        return f.get(i);
		} else {
			i -= firstNodeSize;
			int storeIndex = firstStoreIndex + 1 + (i / arrayLength);
			int itemIndex = i % arrayLength;
			final Node f = load(storeIndex);
			if (f == null || itemIndex >= f.endIndex)
				throw new NoSuchElementException("index: " + (i + firstNodeSize) + ", size: " + size);
			return f.get(itemIndex);
		}
	}
	
	private void set(int i, int value) {
		if (locked) {
    		throw new IllegalStateException("Tried to set value at index " + i + " to " + value + " while being locked.");
    	}
		// we can compute the store index using the size of the 
		// first node and the constant size of each array node
		if (i < firstNodeSize) {
			final Node f = loadFirst();
	        if (f == null || f.startIndex >= f.endIndex)
	            throw new NoSuchElementException();
	        f.set(i, value);
		} else {
			i -= firstNodeSize;
			int storeIndex = firstStoreIndex + 1 + (i / arrayLength);
			int itemIndex = i % arrayLength;
			final Node f = load(storeIndex);
			if (f == null || itemIndex >= f.endIndex)
				throw new NoSuchElementException();
			f.set(itemIndex, value);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("[ ");
		ReplaceableCloneableIntIterator iterator = iterator();
		while (iterator.hasNext()) {
			builder.append(iterator.next()).append(", ");
		}
		builder.setLength(builder.length() > 2 ? builder.length() - 2 : builder.length());
		builder.append(" ]");
		return builder.toString();
	}

	public boolean isDeleteOnExit() {
		return deleteOnExit;
	}

	public int getAndReplaceWith(int i, Function<Integer, Integer> function) {
		int previous = get(i);
		set(i, function.apply(previous));
		return previous;
	}
	
	public void deleteOnExit() {
		deleteOnExit = true;
	}
	
}
