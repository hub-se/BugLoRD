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

/**
 * Simple single linked queue implementation using fixed/variable size array nodes.
 */
public class BufferedLongArrayQueue implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9027487159232780112L;

	// keep at most 2 (+1 with the last node) nodes in memory
    private static final int CACHE_SIZE = 2;
    
    private static final int ARRAY_SIZE = 1000;
	
	protected int arrayLength = ARRAY_SIZE;
	private int size = 0;
	
	private File output;
	private String filePrefix;
	private int firstStoreIndex = 0;
	private int currentStoreIndex = -1;
	private int lastStoreIndex = -1;
	
	private int firstNodeSize = 0;
	
	private transient Node lastNode;
	
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
    
    public BufferedLongArrayQueue(File outputDir, String filePrefix, boolean deleteOnExit) {
    	this.deleteOnExit = deleteOnExit;
		check(outputDir);
		this.output = outputDir;
		this.filePrefix = Objects.requireNonNull(filePrefix);
		initialize();
    }
    
    public BufferedLongArrayQueue(File outputDir, String filePrefix) {
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

	public BufferedLongArrayQueue(File output, String filePrefix, int nodeArrayLength, boolean deleteOnExit) {
    	this(output, filePrefix, deleteOnExit);
    	this.arrayLength = nodeArrayLength < 1 ? 1 : nodeArrayLength;
    }
	
	public BufferedLongArrayQueue(File output, String filePrefix, int nodeArrayLength) {
    	this(output, filePrefix, nodeArrayLength, true);
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
    private void linkLast(long e) {
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
        	store(l);
        	// we can now remove the node from memory
        	l.items = null;
        }
        ++size;
    }
    
    private void store(Node node) {
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
				
				ByteBuffer directBuf = ByteBuffer.allocateDirect(8 * node.items.length + 8);
				directBuf.putInt(node.startIndex);
				directBuf.putInt(node.endIndex);
				for (long i : node.items) {
					directBuf.putLong(i);
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

	/**
     * Removes the first node, if it only contains one item.
     * Decreases the size variable.
     * 
     */
    private long unlinkFirst(Node f) {
        // assert f == first && f != null;
		final long element = f.items[f.startIndex];
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
		String filename = getFileName(storeIndex);
		if (!(new File(filename).exists())) {
			return null;
		}
		
		lock.lock();
		try {
			// only cache nodes that are not the last node
			if (storeIndex <= lastStoreIndex) {
				if (cachedNodes.containsKey(storeIndex)) {
					// already cached
					return cachedNodes.get(storeIndex);
				}
				// cache the node
				if (cachedNodes.size() >= CACHE_SIZE) {
					// remove the node from the cache that has been added first
					uncache(cacheSequence.remove(0));
				}
			}
			
			Node loadedNode;
			try (FileInputStream in = new FileInputStream(filename)) {
				try (FileChannel file = in.getChannel()) {
					long fileSize = file.size();
					if (fileSize > Integer.MAX_VALUE) {
						throw new UnsupportedOperationException("File size too big!");
					}
					ByteBuffer directBuf = ByteBuffer.allocateDirect((int) fileSize);
					file.read(directBuf);
					directBuf.flip();
					
					int startIndex = directBuf.getInt();
					int endIndex = directBuf.getInt();
					
					int arrayLength = (int)(fileSize-8)/8;
					long[] items = new long[arrayLength];
					for (int i = 0; i < arrayLength; ++i) {
						items[i] = directBuf.getLong();
					}
					
					loadedNode = new Node(items, startIndex, endIndex, storeIndex);
					
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
				cachedNodes.put(storeIndex, loadedNode);
				cacheSequence.add(storeIndex);
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
        return size;
    }

    public boolean add(long e) {
    	loadLast();
    	if (lastNode != null && lastNode.hasFreeSpace()) {
    		lastNode.add(e);
    		++size;
    		if (firstStoreIndex == lastNode.storeIndex) {
    			++firstNodeSize;
    		}
    	} else {
    		linkLast(e);	
		}
        return true;
    }

    public void clear() {
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
		return firstStoreIndex <= lastStoreIndex;
	}
    
    /**
     * Same as {@link #clear()}, but only removes the first {@code count} elements.
     * 
     * @param count
     * the number of elements to clear from the list
     */
    public void clear(int count) {
    	lock.lock();
    	try {
    		int i = 0;
    		Node x = null;
    		if (storedNodeExists()) {
    			// do not load uncached nodes, if possible
    			while (count >= firstNodeSize) {
    				if (cachedNodes.containsKey(firstStoreIndex)) {
    					x = load(firstStoreIndex);
    					break;
    				} else {
    					// we can just delete the file without loading the node
    					uncacheAndDelete(firstStoreIndex);
    					++firstStoreIndex;
    					count -= firstNodeSize;
    					size -= firstNodeSize;
    					// still a node on disk?
    					if (storedNodeExists()) {
    						firstNodeSize = arrayLength;
    						// check next node from disk
    						if (cachedNodes.containsKey(firstStoreIndex)) {
    							x = load(firstStoreIndex);
    							break;
    						}

    					} else {
    						loadLast();
    						// no node left on disk, so continue with the last node
    						firstNodeSize = lastNode.endIndex - lastNode.startIndex;
    						x = lastNode;
    						break;
    					}
    				}
    			}

    			if (count < firstNodeSize) {
    				x = load(firstStoreIndex);
    			}
    		} else {
    			x = lastNode;
    		}
    		while (x != null && i < count) {
    			for (int j = x.startIndex; j < x.endIndex && i < count; ++j, ++i) {
//    				System.out.println(x.storeIndex + ", start: " + x.startIndex + ", next: " + x.items[j]);
//    				x.items[j] = null;
    				++x.startIndex;
    				--firstNodeSize;
    			}
    			if (x.startIndex >= x.endIndex) {
    				if (storedNodeExists()) {
    					uncacheAndDelete(firstStoreIndex);
    					++firstStoreIndex;

    					x.items = null;
    					// still a node on disk?
    					if (storedNodeExists()) {
    						// load next node from disk
    						x = load(firstStoreIndex);
    						firstNodeSize = arrayLength;
    					} else {
    						// no node left on disk, so continue with the last node
    						x = lastNode;
    						firstNodeSize = lastNode.endIndex - lastNode.startIndex;
    					}
    				} else if (x == lastNode) {
//    					// keep one node/array to avoid having to allocate a new one
//    					x.startIndex = 0;
//    					x.endIndex = 0;
    					initialize();
    					break;
    				}
    			} else if (i >= count) {
    				x.modified = true;
    				break;
    			}
    		}
    		size -= count;
    		if (size < 0) {
    			size = 0;
    		}
    	} finally {
    		lock.unlock();
		}
    }

//	public long peekLast() {
//        final Node f = loadLast();
//        return ((f == null) ? null : (f.startIndex < f.endIndex ? f.items[f.endIndex-1] : null));
//    }
    
    public long lastElement() {
    	final Node f = loadLast();
        if (f == null || f.startIndex >= f.endIndex)
            throw new NoSuchElementException();
        return f.items[f.endIndex-1];
    }
    
    // Queue operations

//    public long peek() {
//        final Node f = loadFirst();
//        return ((f == null) ? null : (f.startIndex < f.endIndex ? f.items[f.startIndex] : null));
//    }

    public long element() {
    	if (size == 0) {
    		throw new IllegalStateException("size is 0");
    	}
    	final Node f = loadFirst();
        if (f == null || f.startIndex >= f.endIndex)
            throw new NoSuchElementException();
        return f.items[f.startIndex];
    }

//    public long poll() {
//        final Node f = loadFirst();
//        return (f == null) ? null : (f.startIndex < f.endIndex ? removeFirst(f) : null);
//    }

    public long remove() {
    	final Node f = loadFirst();
        if (f == null || f.startIndex >= f.endIndex)
            throw new NoSuchElementException();
        return removeFirst(f);
    }
    
    /*
     * Removes the first element.
     */
    private long removeFirst(Node f) {
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
		return size == 0;
	}

	public ReplaceableCloneableLongIterator iterator() {
		return new MyBufferedLongIterator();
	}
	
	public ReplaceableCloneableLongIterator iterator(final int position) {
		return new MyBufferedLongIterator(position);
	}

	private final class MyBufferedLongIterator implements ReplaceableCloneableLongIterator {

		int storeIndex;
		int index;

		MyBufferedLongIterator(int i) {
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
		
		MyBufferedLongIterator() {
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
		private MyBufferedLongIterator(MyBufferedLongIterator iterator) {
			storeIndex = iterator.storeIndex;
			index = iterator.index;
		}

		public MyBufferedLongIterator clone() {
			return new MyBufferedLongIterator(this);
		}
		
		public boolean hasNext() {
			if (storeIndex < 0) {
				return false;
			}
			Node node = load(storeIndex);
			return node != null && index < node.endIndex;
		}

		public long next() {
			Node currentNode = load(storeIndex);
			long temp = currentNode.items[index++];
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
		public long processNextAndReplaceWithResult(Function<Long,Long> function) {
			Node currentNode = load(storeIndex);
			long temp = currentNode.items[index];
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

		public long peek() {
			Node currentNode = load(storeIndex);
			return currentNode.items[index];
		}

//		public void remove() {
//			throw new UnsupportedOperationException();
//		}
	}

	private static class Node implements Serializable {
		private transient boolean modified = false;

		/**
		 * 
		 */
		private static final long serialVersionUID = 2511188925909568960L;

		// index to store/load this node
		private int storeIndex;
		
		private long[] items;
        // points to first actual item slot
        private int startIndex = 0;
        // points to last free slot
        private int endIndex = 1;
        // pointer to next array node

		Node(long element, int arrayLength, int storeIndex) {
			if (arrayLength < 1) {
        		throw new IllegalStateException();
        	}
            this.items = new long[arrayLength];
            items[0] = element;
			this.storeIndex = storeIndex;
			// ensure that new nodes are stored (if not empty)
        	this.modified = true;
		}

		public Node(long[] items, int startIndex, int endIndex, int storeIndex) {
			this.items = items;
			this.endIndex = endIndex;
			this.startIndex = startIndex;
			this.storeIndex = storeIndex;
		}
		
		// removes the first element
        public long remove() {
        	this.modified = true;
//			int temp = items[startIndex];
//        	items[startIndex++] = 0;
			return items[startIndex++];
		}

        // adds an element to the end
		public void add(long e) {
			this.modified = true;
			items[endIndex++] = e;
		}

		// checks whether an element can be added to the node's array
		boolean hasFreeSpace() {
        	return endIndex < items.length;
        }

		public long get(int i) {
			return items[i+startIndex];
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

	public long get(int i) {
		// we can compute the store index using the size of the 
		// first node and the constant size of each array node
		if (i < firstNodeSize) {
			final Node f = loadFirst();
	        if (f == null || f.startIndex >= f.endIndex)
	            throw new NoSuchElementException();
	        return f.get(i);
		}
		i -= firstNodeSize;
		int storeIndex = firstStoreIndex + 1 + (i / arrayLength);
		int itemIndex = i % arrayLength;
		final Node f = load(storeIndex);
        if (f == null || itemIndex >= f.endIndex)
            throw new NoSuchElementException();
        return f.get(itemIndex);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("[ ");
		ReplaceableCloneableLongIterator iterator = iterator();
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
	
}
