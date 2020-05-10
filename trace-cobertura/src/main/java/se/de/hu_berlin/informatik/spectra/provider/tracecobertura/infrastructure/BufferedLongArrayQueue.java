package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.Function;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.longs.ReplaceableCloneableIterator;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

/**
 * Simple single linked queue implementation using fixed/variable size array nodes.
 */
public class BufferedLongArrayQueue implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1103705721017209751L;

    // keep at most 3 (+1 with the last node) nodes in memory
    private static final int CACHE_SIZE = 3;

    private static final int ARRAY_SIZE = 1000;

    private int arrayLength = ARRAY_SIZE;

    public int getArrayLength() {
        return arrayLength;
    }

    private volatile long size = 0;

    private File output;
    private String filePrefix;
    private volatile int firstStoreIndex = 0;
    private volatile int currentStoreIndex = -1;
    private volatile int lastStoreIndex = -1;

    private volatile int firstNodeSize = 0;

    private volatile transient Node lastNode;

    // cache all other nodes, if necessary
    private transient Map<Integer, Node> cachedNodes = null;
    private transient List<Integer> cacheSequence = null;

    private transient boolean deleteOnExit;

    // keep a node that may be reused
    private transient Node reusableNode = null;

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
        stream.writeLong(size);
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
//		System.out.println(super.toString() + " sleep: " + cachedNodes.keySet() + ", last: " + (lastStoreIndex+1));
        if (cachedNodes != null) {
            for (Node node : cachedNodes.values()) {
                // stores cached nodes, if modified (i.e., if elements were added/removed)
                if (node.modified) {
                    store(node);
                }
                node.cleanup();
            }
            cachedNodes = null;
            cacheSequence = null;
        }

        // store the last node, too
        if (lastNode != null && lastNode.modified) {
            store(lastNode);
        }
        lastNode = null;
        writeBuffer = null;
        reusableNode = null;
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
        size = stream.readLong();
        arrayLength = stream.readInt();

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

    private void initialize() {
        writeBuffer = null;
        firstStoreIndex = 0;
        lastStoreIndex = -1;
        if (lastNode != null) {
//			for (int j = lastNode.startIndex; j < lastNode.endIndex; ++j) {
//				lastNode.items[j] = null;
//			}
            delete(lastNode.storeIndex);
//			lastNode = null;
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

        final Node newNode = reusableNode == null ? new Node(e, arrayLength, ++currentStoreIndex) : reusableNode.recycle(e, ++currentStoreIndex);
        reusableNode = null;
        lastNode = newNode;

        ++size;
    }

    private transient ByteBuffer writeBuffer = null;

    private ByteBuffer getFreshBuffer() {
        // only (lazily) allocate ONE buffer per buffered queue object! allocation costs are potentially high...
        if (writeBuffer == null) {
            writeBuffer = ByteBuffer.allocateDirect(8 * arrayLength + 8);
        }
        writeBuffer.clear();
        return writeBuffer;
    }

    private void store(Node node) {
//    	System.out.println(super.toString() + " store: " + node.storeIndex + ", " + cachedNodes.keySet() + ", last: " + (lastStoreIndex+1));
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
                    directBuf.putLong(node.items[i]);
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
    }

    private void putInCache(int storeIndex, Node node) {
        // last node?
        if (storeIndex > lastStoreIndex) {
            lastNode = node;
            return;
        }

        if (cachedNodes == null) {
            cachedNodes = new HashMap<>((int) (((float) CACHE_SIZE / 0.7F) + 1), 0.7F);
            cacheSequence = new LinkedList<>();
        } else if (cachedNodes.containsKey(storeIndex)) {
            // already in the cache?
            return;
        }

//    	System.out.println(super.toString() + " cache: " + storeIndex + ", " + cachedNodes.keySet() + ", last: " + (lastStoreIndex+1));
        // remove a cached node if the cache is full
        if (cachedNodes.size() >= CACHE_SIZE) {
            // remove the node from the cache that has been added first
            uncache(cacheSequence.remove(0));
        }
        cachedNodes.put(storeIndex, node);
        cacheSequence.add(storeIndex);
    }

    // should check for modifications and possibly write to the disk
    private void uncache(int storeIndex) {
        if (cachedNodes == null) {
            return;
        }
        Node node = cachedNodes.remove(storeIndex);
        if (node != null) {
            if (node.modified) {
//    			System.out.println(super.toString() + " uncache: " + storeIndex + ", " + cachedNodes.keySet() + ", last: " + (lastStoreIndex+1));
                store(node);
            }
            reusableNode = node.reset();
        }
    }

    /**
     * Removes the first node, if it only contains one item.
     * Decreases the size variable.
     */
    private long unlinkFirst(Node f) {
        // assert f == first && f != null;
        final long element = f.items[f.startIndex];
//        final Node<E> next = f.next;
        if (storedNodeExists()) {
            // there exists a stored node on disk;
//            f.items = null; // help GC
            uncacheAndDelete(firstStoreIndex);
            ++firstStoreIndex;
            --size;
            if (storedNodeExists()) {
                firstNodeSize = arrayLength;
            } else {
                firstNodeSize = (int) size;
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
        uncacheNoStore(storeIndex);
        delete(storeIndex);
    }

    private void uncacheNoStore(int storeIndex) {
        if (cachedNodes == null) {
            return;
        }
        if (cachedNodes.containsKey(storeIndex)) {
//    		System.out.println(super.toString() + " uncache: " + storeIndex + ", " + cachedNodes.keySet() + ", last: " + (lastStoreIndex+1));
            Node node = cachedNodes.get(storeIndex);
            reusableNode = node.reset();
            cachedNodes.remove(storeIndex);
            cacheSequence.remove((Integer) storeIndex);
        }
    }

    private void delete(int storeIndex) {
        String filename = getFileName(storeIndex);
        // stored node should be deleted
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
    }

    private String getFileName(int storeIndex) {
        return output.getAbsolutePath() + File.separator + filePrefix + "-" + storeIndex + ".rry";
    }

    private Node loadFirst() {
        if (storedNodeExists()) {
            return load(firstStoreIndex);
        } else {
            return loadLast();
        }
    }

    private Node loadLast() {
        if (lastNode == null) {
//			System.out.println(super.toString() + " load: " + (lastStoreIndex+1) + ", " + cachedNodes.keySet() + ", last: " + (lastStoreIndex+1));
            // might be loaded from cache...
            lastNode = load(lastStoreIndex + 1);
            return lastNode;
        } else {
            return lastNode;
        }
    }

    private Node load(int storeIndex) {
        if (lastNode != null && storeIndex == lastNode.storeIndex) {
            return lastNode;
        }

        // only cache nodes that are not the last node
        if (cachedNodes != null && cachedNodes.containsKey(storeIndex)) {
            // already cached
            return cachedNodes.get(storeIndex);
        }

        String filename = getFileName(storeIndex);
        if (!(new File(filename).exists())) {
            return null;
        }

//		System.out.println(super.toString() + " load: " + storeIndex + ", " + cachedNodes.keySet() + ", last: " + (lastStoreIndex+1));
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

                if (reusableNode == null) {
//					int arrayLength = (int)fileSize/8 - 2;
                    // actually only load an array of the size that's necessary;
                    // will be extended if there are new elements that are added
                    long[] items = new long[endIndex];
                    for (int i = startIndex; i < endIndex; ++i) {
                        items[i] = directBuf.getLong();
                    }

                    loadedNode = new Node(items, startIndex, endIndex, storeIndex, arrayLength);
                } else {
                    long[] items = reusableNode.items;
                    if (endIndex > items.length) {
                        items = new long[endIndex];
                    }
                    for (int i = startIndex; i < endIndex; ++i) {
                        items[i] = directBuf.getLong();
                    }

                    loadedNode = reusableNode.recycle(items, startIndex, endIndex, storeIndex);
                    reusableNode = null;
                }

                // file can not be removed, due to serialization! TODO
                if (deleteOnExit) {
                    new File(filename).deleteOnExit();
                }
            }
        } catch (IOException | IndexOutOfBoundsException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }

        if (storeIndex <= lastStoreIndex) {
            putInCache(storeIndex, loadedNode);
        } else {
//			System.out.println(super.toString() + " load: " + storeIndex + ", " + cachedNodes.keySet() + ", last: " + (lastStoreIndex+1));
            lastNode = loadedNode;
        }
        return loadedNode;
    }


    public long size() {
        return size;
    }

    public boolean add(long e) {
        if (locked) {
            throw new IllegalStateException("Tried to add value " + e + " while being locked.");
        }

        loadLast();
        if (lastNode != null && lastNode.hasFreeSpace()) {
            lastNode.add(e);
            ++size;
            if (firstStoreIndex == lastNode.storeIndex) {
                ++firstNodeSize;
            }
//    		System.out.println(e + " -> added " + lastNode.storeIndex + ", " + lastNode.endIndex);
        } else {
            linkLast(e);
//    		System.out.println(e + " -> linked " + lastNode.storeIndex + ", " + lastNode.endIndex);
        }
        return true;
    }

    private void clearCache() {
        if (cachedNodes == null) {
            return;
        }
//    	System.out.println(super.toString() + " clear cache, " + cachedNodes.keySet() + ", last: " + (lastStoreIndex+1));
        for (Node node : cachedNodes.values()) {
            node.cleanup();
        }
        cachedNodes = null;
        cacheSequence = null;
        reusableNode = null;
    }

    public void clear() {
        if (locked) {
            throw new IllegalStateException("Tried to clear queue while being locked.");
        }

        clearCache();
        // delete possibly stored nodes
        for (; storedNodeExists(); ++firstStoreIndex) {
            delete(firstStoreIndex);
        }
        // delete potentially stored last node
        delete(lastStoreIndex + 1);
        lastNode = null;

        initialize();
    }

    private boolean storedNodeExists() {
        return firstStoreIndex <= lastStoreIndex;
    }

    /**
     * Same as {@link #clear()}, but only removes the first {@code count} elements.
     *
     * @param count the number of elements to clear from the list
     */
    public void clear(long count) {
        if (count >= size) {
            clear();
            return;
        }
        if (locked) {
            throw new IllegalStateException("Tried to clear queue while being locked.");
        }

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
            x.clear((int) count);
            firstNodeSize -= count;
        }

        size -= count;
    }

    /**
     * Same as {@link #clear()}, but only removes the last {@code count} elements.
     *
     * @param count the number of elements to clear from the list
     */
    public void clearLast(long count) {
        if (count >= size) {
            clear();
            return;
        }
        if (locked) {
            throw new IllegalStateException("Tried to clear queue while being locked.");
        }

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

        // ensure that the last node is not cached
        loadLast();
        removeFromCache(lastNode.storeIndex);

        // still elements left to clear?
        if (count > 0) {
            // it holds that count < arrayLength
            lastNode.clearLast((int) count);
            if (!storedNodeExists()) {
                // no node left on disk, so continue with the last node
                firstNodeSize = lastNode.endIndex - lastNode.startIndex;
            }
        }

        size -= count;
    }

    private void removeFromCache(int storeIndex) {
        if (cachedNodes == null) {
            return;
        }
        if (cachedNodes.containsKey(storeIndex)) {
//    		System.out.println(super.toString() + " uncache: " + storeIndex + ", " + cachedNodes.keySet() + ", last: " + (lastStoreIndex+1));
            cachedNodes.remove(storeIndex);
            cacheSequence.remove((Integer) storeIndex);
        }
    }

    /**
     * Same as {@link #clear()}, but removes {@code count} elements,
     * starting from {@code startIndex}.
     *
     * @param startIndex the index to start clearing from
     * @param count      the number of elements to clear from the list
     */
    public void clearFrom(long startIndex, long count) {
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

        int startNodeIndex = -1;
        int startItemIndex = (int) startIndex;
        Node startNode = null;
        // we can compute the store index using the size of the 
        // first node and the constant size of each array node
        if (startIndex < firstNodeSize) {
            startNode = loadFirst();
            if (startNode == null || startNode.startIndex >= startNode.endIndex)
                throw new NoSuchElementException();
            startNodeIndex = startNode.storeIndex;
        } else {
            int i = (int) (startIndex - firstNodeSize);
            startNodeIndex = firstStoreIndex + 1 + (i / arrayLength);
            startItemIndex = i % arrayLength;
            startNode = load(startNodeIndex);
        }

        long endItemIndex = startIndex + count;

        // shift the remaining elements to the left
        MyBufferedIterator iterator = iterator(endItemIndex);
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
    }


    public long lastElement() {
        final Node f = loadLast();
        if (f == null || f.startIndex >= f.endIndex)
            if (f == null)
                throw new NoSuchElementException("size: " + size);
            else
                throw new NoSuchElementException("startindex: " + (f.startIndex) + ", endindex: " + (f.endIndex) + ", size: " + size);
        return f.items[f.endIndex - 1];
    }

    // Queue operations

//    public int peek() {
//        final Node f = loadFirst();
//        return ((f == null) ? null : (f.startIndex < f.endIndex ? f.items[f.startIndex] : null));
//    }

    public long element() {
        final Node f = loadFirst();
        if (f == null || f.startIndex >= f.endIndex)
            if (f == null)
                throw new NoSuchElementException("size: " + size);
            else
                throw new NoSuchElementException("startindex: " + (f.startIndex) + ", endindex: " + (f.endIndex) + ", size: " + size);
        return f.items[f.startIndex];
    }

//    public int poll() {
//        final Node f = loadFirst();
//        return (f == null) ? null : (f.startIndex < f.endIndex ? removeFirst(f) : null);
//    }

    public long remove() {
        if (locked) {
            throw new IllegalStateException("Tried to remove element while being locked.");
        }

        final Node f = loadFirst();
        if (f == null || f.startIndex >= f.endIndex)
            if (f == null)
                throw new NoSuchElementException("size: " + size);
            else
                throw new NoSuchElementException("startindex: " + (f.startIndex) + ", endindex: " + (f.endIndex) + ", size: " + size);
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

    public boolean offer(long e) {
        return add(e);
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public MyBufferedIterator iterator() {
        return new MyBufferedIterator();
    }

    public MyBufferedIterator iterator(final long position) {
        return new MyBufferedIterator(position);
    }

    public final class MyBufferedIterator implements ReplaceableCloneableIterator {

        int storeIndex;
        int index;

        MyBufferedIterator(long i) {
            setToPosition(i);
        }

        public void setToPosition(long i) {
            // we can compute the store index using the size of the 
            // first node and the constant size of each array node
            if (i < firstNodeSize) {
                storeIndex = firstStoreIndex;
                Node node = load(storeIndex);
                index = (int) (i + node.startIndex);
            } else {
                i -= firstNodeSize;
                storeIndex = (int) (firstStoreIndex + 1 + (i / arrayLength));
                index = (int) (i % arrayLength);
            }
        }

        MyBufferedIterator() {
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
        private MyBufferedIterator(MyBufferedIterator iterator) {
            storeIndex = iterator.storeIndex;
            index = iterator.index;
        }

        public MyBufferedIterator clone() {
            return new MyBufferedIterator(this);
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
        public long processNextAndReplaceWithResult(Function<Long, Long> function) {
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
        /**
         *
         */
        private static final long serialVersionUID = -7985304543765441098L;

        private volatile transient boolean modified = false;

        // index to store/load this node
        private volatile int storeIndex;

        private long[] items;
        // points to first actual item slot
        private volatile int startIndex = 0;
        // points to last free slot
        private volatile int endIndex = 1;

        private int arrayLength;
        // pointer to next array node

        Node(long element, int arrayLength, int storeIndex) {
            this.arrayLength = arrayLength;
            if (arrayLength < 1) {
                throw new IllegalStateException();
            }
            this.items = new long[arrayLength];
            items[0] = element;
            this.storeIndex = storeIndex;
            // ensure that new nodes are stored (if not empty)
            this.modified = true;
        }

        public Node recycle(long[] items, int startIndex, int endIndex, int storeIndex) {
//			System.out.println("node load recycle " + this.storeIndex + " -> " + storeIndex);
            this.items = items;
            this.endIndex = endIndex;
            this.startIndex = startIndex;
            this.storeIndex = storeIndex;
            this.modified = false;
            return this;
        }

        public Node reset() {
            // prepare for possible recycling
            // nothing to do here
            return this;
        }

        public Node recycle(long element, int storeIndex) {
//			System.out.println("node recycle " + this.storeIndex + " -> " + storeIndex);
            startIndex = 0;
            endIndex = 1;
            items[0] = element;
            this.storeIndex = storeIndex;
            // ensure that new nodes are stored (if not empty)
            this.modified = true;
            return this;
        }

//		Node(int element, int arrayLength, int[] items, int storeIndex) {
//			this.arrayLength = arrayLength;
//			if (arrayLength < 1) {
//        		throw new IllegalStateException();
//        	}
//            this.items = items;
//            items[0] = element;
//			this.storeIndex = storeIndex;
//			// ensure that new nodes are stored (if not empty)
//        	this.modified = true;
//		}

        public void clearLast(int count) {
            this.endIndex -= count;
            this.modified = true;
        }

        public void clear(int count) {
            this.startIndex += count;
            this.modified = true;
        }

        public long[] cleanup() {
//			System.out.println("node cleanup " + this.storeIndex);
            long[] temp = this.items;
            this.items = null;
            return temp;
        }

        public int size() {
            return endIndex - startIndex;
        }

        public Node(long[] items, int startIndex, int endIndex, int storeIndex, int arrayLength) {
            this.items = items;
            this.endIndex = endIndex;
            this.startIndex = startIndex;
            this.storeIndex = storeIndex;
            this.arrayLength = arrayLength;
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
            if (items.length <= endIndex) {
                extendArray();
            }
            items[endIndex++] = e;
        }

        private void extendArray() {
            long[] temp = items;
            items = new long[arrayLength];
            System.arraycopy(temp, startIndex, items, startIndex, temp.length - startIndex);
        }

        // checks whether an element can be added to the node's array
        boolean hasFreeSpace() {
            return endIndex < arrayLength;
        }

        public long get(int i) {
            return items[i + startIndex];
        }

        public void set(int i, long value) {
            this.modified = true;
            if (items.length <= i + startIndex) {
                extendArray();
            }
            items[i + startIndex] = value;
        }

        public void trim() {
            // reduce size of item array to remove empty space
            if (items.length > endIndex) {
                long[] temp = items;
                items = new long[endIndex];
                System.arraycopy(temp, startIndex, items, startIndex, endIndex - startIndex);
            }
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

    public long get(long i) {
        // we can compute the store index using the size of the 
        // first node and the constant size of each array node
        if (i < firstNodeSize) {
            final Node f = loadFirst();
            if (f == null || f.startIndex >= f.endIndex)
                throw new NoSuchElementException();
            return f.get((int) i);
        } else {
            i -= firstNodeSize;
            int storeIndex = (int) (firstStoreIndex + 1 + (i / arrayLength));
            int itemIndex = (int) (i % arrayLength);
            final Node f = load(storeIndex);
            if (f == null)
                throw new NoSuchElementException("node null!: " + storeIndex + ", first node size: " + firstNodeSize + ", index: " + (i + firstNodeSize) + ", size: " + size);
            if (itemIndex >= f.endIndex)
                throw new NoSuchElementException("index: " + (i + firstNodeSize) + ", size: " + size);
            return f.get(itemIndex);
        }
    }

    private void set(long i, long value) {
        if (locked) {
            throw new IllegalStateException("Tried to set value at index " + i + " to " + value + " while being locked.");
        }
        // we can compute the store index using the size of the 
        // first node and the constant size of each array node
        if (i < firstNodeSize) {
            final Node f = loadFirst();
            if (f == null || f.startIndex >= f.endIndex)
                throw new NoSuchElementException();
            f.set((int) i, value);
        } else {
            i -= firstNodeSize;
            int storeIndex = (int) (firstStoreIndex + 1 + (i / arrayLength));
            int itemIndex = (int) (i % arrayLength);
            final Node f = load(storeIndex);
            if (f == null || itemIndex >= f.endIndex)
                throw new NoSuchElementException();
            f.set(itemIndex, value);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[ ");
        ReplaceableCloneableIterator iterator = iterator();
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

    public long getAndReplaceWith(long i, Function<Long, Long> function) {
        long previous = get(i);
        set(i, function.apply(previous));
        return previous;
    }

    public void deleteOnExit() {
        deleteOnExit = true;
    }

    public void trim() {
//		System.out.println(super.toString() + " trim: " + cachedNodes.keySet() + ", last: " + (lastStoreIndex+1));
        if (cachedNodes != null) {
            for (Node node : cachedNodes.values()) {
                node.trim();
            }
        }
        if (lastNode != null) {
            lastNode.trim();
        }
    }

}
