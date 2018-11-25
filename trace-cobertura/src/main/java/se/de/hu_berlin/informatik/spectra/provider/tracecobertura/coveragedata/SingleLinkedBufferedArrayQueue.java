package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple single linked queue implementation using fixed/variable size array nodes.
 * 
 * @param <E> 
 * the type of elements held in the queue
 */
public class SingleLinkedBufferedArrayQueue<E> extends SingleLinkedArrayQueue<E> {
	
	// keep at most 3 (+1 with the last node) nodes in memory
    private static final int CACHE_SIZE = 3;
    
	private File output;
	private String filePrefix;
	private int firstStoreIndex = 0;
	private int currentStoreIndex = -1;
	private int lastStoreIndex = -1;
	
	private int firstNodeSize = 0;
	
	// keep the last node out of the cache at all times
	private Node<E> lastNode;
	
	private Lock lock = new ReentrantLock();
	
	// cache all other nodes, if necessary
	private Map<Integer,Node<E>> cachedNodes = new HashMap<>();
	private List<Integer> cacheSequence = new LinkedList<>();

	@SuppressWarnings("unused")
	private SingleLinkedBufferedArrayQueue() {
		// prevent instantiation
	}
	
	@SuppressWarnings("unused")
	private SingleLinkedBufferedArrayQueue(int nodeArrayLength) {
		// prevent instantiation
	}
	
    public SingleLinkedBufferedArrayQueue(File outputDir, String filePrefix) {
    	check(outputDir);
		this.output = outputDir;
		this.filePrefix = Objects.requireNonNull(filePrefix);
		initialize();
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

	public SingleLinkedBufferedArrayQueue(File output, String filePrefix, int nodeArrayLength) {
    	this(output, filePrefix);
    	this.arrayLength = nodeArrayLength < 1 ? 1 : nodeArrayLength;
    }
	
	private void initialize() {
		firstStoreIndex = 0;
		lastStoreIndex = -1;
		if (lastNode != null) {
			lastNode.storeIndex = 0;
			currentStoreIndex = 0;
		} else {
			currentStoreIndex = -1;
		}
		firstNodeSize = 0;
		size = 0;
	}
	
	
	public File getOutputDir() {
		return output;
	}
	
	public String getFilePrefix() {
		return filePrefix;
	}

    public NodePointer<E> getLastNodePointer() {
		return new NodePointer<>(lastNode);
	}
    
    /**
     * Creates a new node if the last node is null or full.
     * Increases the size variable.
     * Stores full nodes to the disk.
     */
    @Override
    protected void linkLast(E e) {
        final Node<E> l = lastNode;
        final Node<E> newNode = new Node<>(e, arrayLength, ++currentStoreIndex);
        lastNode = newNode;
        if (l == null) {
        	// no nodes did exist, previously
            lastNode = newNode;
            ++firstNodeSize;
        } else {
        	// we don't actually use pointers!
//            l.next = newNode;
        	store(l);
        	// we can remove the node now from memory
        	l.items = null;
        	l.next = null;
        }
        ++size;
    }

    private void store(Node<E> node) {
		++lastStoreIndex;
		String filename = getFileName(lastStoreIndex);
		try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filename))) {
			outputStream.writeObject(node.items);
			outputStream.writeInt(node.startIndex);
			// file can be removed on exit!? TODO
			new File(filename).deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
     * Removes the first node, if it only contains one item.
     * Decreases the size variable.
     * 
     */
    @Override
    protected E unlinkFirst(SingleLinkedArrayQueue.Node<E> f) {
        // assert f == first && f != null;
        @SuppressWarnings("unchecked")
		final E element = (E) f.items[f.startIndex];
//        final Node<E> next = f.next;
        if (storedNodeExists()) {
        	// there exists a stored node on disk;
            f.items = null;
            f.next = null; // help GC
        	uncacheAndDelete(firstStoreIndex);
        	++firstStoreIndex;
            --size;
            if (storedNodeExists()) {
            	firstNodeSize = arrayLength;
            } else {
            	firstNodeSize = size;
            }
        } else {
        	// there exists only the last node
        	// keep one node/array to avoid having to allocate a new one
        	lastNode.startIndex = 0;
        	lastNode.endIndex = 0;
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
				cacheSequence.remove((Object)storeIndex);
			}
			// stored node should be deleted
			new File(filename).delete();
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
	
	private Node<E> loadFirst() {
		if (storedNodeExists()) {
			return load(firstStoreIndex);
		} else {
			return lastNode;
		}
	}
	
	private Node<E> load(int storeIndex) {
		if (storeIndex == lastNode.storeIndex) {
			return lastNode;
		}
		if (storeIndex > lastNode.storeIndex) {
			return null;
		}
		lock.lock();
		try {
			if (cachedNodes.containsKey(storeIndex)) {
				// already cached
				return cachedNodes.get(storeIndex);
			}
			// cache the node
			if (cachedNodes.size() >= CACHE_SIZE) {
				// remove the node from the cache that has been added first
				cachedNodes.remove(cacheSequence.remove(0));
			}
			String filename = getFileName(storeIndex);
			Node<E> loadedNode;
			try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filename))) {
				Object[] items = (Object[])inputStream.readObject();
				int startIndex = inputStream.readInt();
				loadedNode = new Node<E>(items, startIndex, storeIndex);
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
			cachedNodes.put(storeIndex, loadedNode);
			cacheSequence.add(storeIndex);
			return loadedNode;
		} finally {
			lock.unlock();
		}
	}

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean add(E e) {
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

    @Override
    public void clear() {
    	lock.lock();
    	try {
    		cachedNodes.clear();
    		cacheSequence.clear();
    		// delete possibly stored nodes
    		for (; storedNodeExists(); ++firstStoreIndex) {
    			uncacheAndDelete(firstStoreIndex);
    		}
    		// empty last (and now also first) node
    		if (lastNode != null) {
    			for (int j = lastNode.startIndex; j < lastNode.endIndex; ++j) {
    				lastNode.items[j] = null;
    			}
    			// keep one node/array to avoid having to allocate a new one
    			lastNode.startIndex = 0;
    			lastNode.endIndex = 0;
    		}
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
    @Override
    public void clear(int count) {
    	lock.lock();
    	try {
    		int i = 0;
    		Node<E> x = null;
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
    				x.items[j] = null;
    				++x.startIndex;
    				--firstNodeSize;
    			}
    			if (x.startIndex >= x.endIndex) {
    				if (storedNodeExists()) {
    					uncacheAndDelete(firstStoreIndex);
    					++firstStoreIndex;

    					x.items = null;
    					x.next = null;
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
    					// keep one node/array to avoid having to allocate a new one
    					x.startIndex = 0;
    					x.endIndex = 0;
    					initialize();
    					break;
    				}
    			} else if (i >= count) {
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

    // Queue operations

    @SuppressWarnings("unchecked")
	@Override
    public E peek() {
        final Node<E> f = loadFirst();
        return ((f == null) ? null : (f.startIndex < f.endIndex ? (E) f.items[f.startIndex] : null));
    }

	@SuppressWarnings("unchecked")
	@Override
    public E element() {
    	final Node<E> f = loadFirst();
        if (f == null || f.startIndex >= f.endIndex)
            throw new NoSuchElementException();
        return (E) f.items[f.startIndex];
    }

    @Override
    public E poll() {
        final Node<E> f = loadFirst();
        return (f == null) ? null : (f.startIndex < f.endIndex ? removeFirst(f) : null);
    }

    @Override
    public E remove() {
    	final Node<E> f = loadFirst();
        if (f == null || f.startIndex >= f.endIndex)
            throw new NoSuchElementException();
        return removeFirst(f);
    }
    
    /*
     * Removes the first element.
     */
    protected E removeFirst(Node<E> f) {
    	if (f.startIndex < f.endIndex - 1) {
    		--size;
    		--firstNodeSize;
    		return f.remove();
    	} else {
    		return unlinkFirst(f);	
		}
    }

    @Override
    public boolean offer(E e) {
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

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public CloneableIterator<E> iterator() {
		return new MyBufferedIterator();
	}
	
	public Iterator<E> iterator(final NodePointer<E> start) {
		return new MyBufferedIterator(start);
	}
	
	public Iterator<E> iterator(final int position) {
		return new MyBufferedIterator(position);
	}

	protected final class MyBufferedIterator implements CloneableIterator<E> {

		int storeIndex;
		int index;

		MyBufferedIterator(NodePointer<E> start) {
			storeIndex = start.storeIndex;
			index = start.index;
		}
		
		MyBufferedIterator(int i) {
			// we can compute the store index using the size of the 
			// first node and the constant size of each array node
			if (i < firstNodeSize) {
				storeIndex = firstStoreIndex;
				index = i;
			} else {
				i -= firstNodeSize;
				storeIndex = firstStoreIndex + 1 + (i / arrayLength);
				index = i % arrayLength;
			}
		}
		
		MyBufferedIterator() {
			if (storedNodeExists()) {
				storeIndex = firstStoreIndex;
				Node<E> node = load(storeIndex);
				index = node.startIndex;
			} else if (lastNode != null) {
				storeIndex = lastNode.storeIndex;
				index = lastNode.startIndex;
			} else {
				storeIndex = -1;
				index = -1;
			}
		}
		
		// clone constructor
		private MyBufferedIterator(MyBufferedIterator iterator) {
			storeIndex = iterator.storeIndex;
			index = iterator.index;
		}

		public MyBufferedIterator clone() {
			return new MyBufferedIterator(this);
		}
		
		@Override
		public boolean hasNext() {
			if (storeIndex < 0) {
				return false;
			}
			Node<E> node = load(storeIndex);
			return node != null && index < node.endIndex;
		}

		@Override
		public E next() {
			Node<E> currentNode = load(storeIndex);
			@SuppressWarnings("unchecked")
			E temp = (E) currentNode.items[index++];
			if (index >= currentNode.endIndex) {
				if (storeIndex < lastStoreIndex) {
					++storeIndex;
				} else if (lastNode != null) {
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
		public E peek() {
			Node<E> currentNode = load(storeIndex);
			@SuppressWarnings("unchecked")
			E temp = (E) currentNode.items[index];
			return temp;
		}
	}

	protected static class Node<E> extends SingleLinkedArrayQueue.Node<E> {
		// index to store/load this node
		private int storeIndex;

		Node(E element, int arrayLength, int storeIndex) {
			super(element, arrayLength);
			this.storeIndex = storeIndex;
		}

		public Node(Object[] items, int startIndex, int storeIndex) {
			super(items);
			this.startIndex = startIndex;
			this.storeIndex = storeIndex;
		}

	}

	protected static class NodePointer<E> extends SingleLinkedArrayQueue.NodePointer<E> {
        // node store index to get/restore the node
        final int storeIndex;

        NodePointer(Node<E> node) {
        	super(node);
        	if (node != null) {
        		this.storeIndex = node.storeIndex;
        	} else {
        		this.storeIndex = -1;
        	}
        }
    }
	
	@Override
	protected void finalize() throws Throwable {
		this.clear();
		super.finalize();
	}

	public E get(int i) {
		// we can compute the store index using the size of the 
		// first node and the constant size of each array node
		if (i < firstNodeSize) {
			final Node<E> f = loadFirst();
	        if (f == null || f.startIndex >= f.endIndex)
	            throw new NoSuchElementException();
	        return f.get(i);
		}
		i -= firstNodeSize;
		int storeIndex = firstStoreIndex + 1 + (i / arrayLength);
		int itemIndex = i % arrayLength;
		final Node<E> f = load(storeIndex);
        if (f == null || itemIndex >= f.endIndex)
            throw new NoSuchElementException();
        return f.get(itemIndex);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("[ ");
		Iterator<E> iterator = this.iterator();
		while (iterator.hasNext()) {
			builder.append(String.valueOf(iterator.next())).append(", ");
		}
		builder.setLength(builder.length() > 2 ? builder.length() - 2 : builder.length());
		builder.append(" ]");
		return builder.toString();
	}
	
}
