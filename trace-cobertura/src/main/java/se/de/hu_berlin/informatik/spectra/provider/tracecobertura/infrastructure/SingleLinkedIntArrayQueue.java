package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;

/**
 * Simple single linked queue implementation using fixed/variable size array nodes.
 */
@CoverageIgnore
public class SingleLinkedIntArrayQueue extends AbstractQueue<Integer> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2315461230998072689L;

    private static final int ARRAY_SIZE = 1000;

    private int arrayLength = ARRAY_SIZE;
    private int size = 0;
    private Node first;
    private Node last;

    /**
     * Constructs an empty queue.
     */
    public SingleLinkedIntArrayQueue() {
    }

    public SingleLinkedIntArrayQueue(int nodeArrayLength) {
        this.arrayLength = nodeArrayLength;
    }

    public SingleLinkedIntArrayQueue(Collection<? extends Integer> c) {
        this();
        addAll(c);
    }

    public NodePointer getLastNodePointer() {
        return new NodePointer(last);
    }

    /*
     * Creates a new node if the last node is null or full.
     * Increases the size variable.
     */
    private void linkLast(int e) {
        final Node l = last;
        final Node newNode = new Node(e, arrayLength);
        last = newNode;
        if (l == null)
            first = newNode;
        else
            l.next = newNode;
        ++size;
    }

    /*
     * Removes the first node, if it only contains one item.
     * Decreases the size variable.
     */
    private int unlinkFirst(Node f) {
        // assert f == first && f != null;
        final int element = f.items[f.startIndex];
        final Node next = f.next;
        f.items = null;
        f.next = null; // help GC
        first = next;
        if (next == null)
            last = null;
        --size;
        return element;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    public int indexOf(Object o) {
        int index = 0;
        Iterator<Integer> iterator = this.iterator();
        if (o == null) {
            while (iterator.hasNext()) {
                if (iterator.next() == null)
                    return index;
                index++;
            }
        } else {
            while (iterator.hasNext()) {
                if (o.equals(iterator.next()))
                    return index;
                index++;
            }
        }
        return -1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean add(Integer e) {
        if (last != null && last.hasFreeSpace()) {
            last.add(e);
            ++size;
        } else {
            linkLast(e);
        }
        return true;
    }

    public boolean addNoAutoBoxing(int e) {
        if (last != null && last.hasFreeSpace()) {
            last.add(e);
            ++size;
        } else {
            linkLast(e);
        }
        return true;
    }

    @Override
    public void clear() {
        // Clearing all of the links between nodes is "unnecessary", but:
        // - helps a generational GC if the discarded nodes inhabit
        //   more than one generation
        // - is sure to free memory even if there is a reachable Iterator
        for (Node x = first; x != null; ) {
            Node next = x.next;
//            for (int j = x.startIndex; j < x.endIndex; ++j) {
//            	x.items[j] = null;
//            }
            if (x == last) {
                // keep one node/array to avoid having to allocate a new one
                x.startIndex = 0;
                x.endIndex = 0;
                break;
            } else {
                x.items = null;
                x.next = null;
                x = next;
            }
        }
        first = last;
        size = 0;
    }

    /**
     * Same as {@link #clear()}, but only removes the first {@code count} elements.
     *
     * @param count the number of elements to clear from the list
     */
    public void clear(int count) {
        // Clearing all of the links between nodes is "unnecessary", but:
        // - helps a generational GC if the discarded nodes inhabit
        //   more than one generation
        // - is sure to free memory even if there is a reachable Iterator
        int i = 0;
        Node x = first;
        while (x != null && i < count) {
            Node next = x.next;
            for (int j = x.startIndex; j < x.endIndex && i < count; ++j, ++i) {
//            	x.items[j] = null;
                ++x.startIndex;
            }
            if (x.startIndex >= x.endIndex) {
                if (x == last) {
                    // keep one node/array to avoid having to allocate a new one
                    x.startIndex = 0;
                    x.endIndex = 0;
                    break;
                } else {
                    x.items = null;
                    x.next = null;
                    x = next;
                }
            } else if (i >= count) {
                break;
            }
        }
        size -= count;
        if (size < 0) {
            size = 0;
        }
        first = x;
        if (x == null) {
            last = null;
        }
    }

    public int peekLastNoCheck() {
        return last.items[last.endIndex - 1];
    }

    public int peekNoCheck() {
        return first.items[first.startIndex];
    }

    public Integer peekLast() {
        final Node f = last;
        return ((f == null) ? null : (f.startIndex < f.endIndex ? f.items[f.endIndex - 1] : null));
    }

    // Queue operations

    @Override
    public Integer peek() {
        final Node f = first;
        return ((f == null) ? null : (f.startIndex < f.endIndex ? f.items[f.startIndex] : null));
    }

    @Override
    public Integer element() {
        final Node f = first;
        if (f == null || f.startIndex >= f.endIndex)
            throw new NoSuchElementException();
        return f.items[f.startIndex];
    }

    @Override
    public Integer poll() {
        final Node f = first;
        return (f == null) ? null : (f.startIndex < f.endIndex ? removeFirst(f) : null);
    }

    @Override
    public Integer remove() {
        final Node f = first;
        if (f == null || f.startIndex >= f.endIndex)
            throw new NoSuchElementException();
        return removeFirst(f);
    }

    public int removeNoAutoBoxing() {
        final Node f = first;
        if (f == null || f.startIndex >= f.endIndex)
            throw new NoSuchElementException();
        return removeFirst(f);
    }

    /*
     * Removes the first element.
     */
    private int removeFirst(Node f) {
        if (f.startIndex < f.endIndex - 1) {
            --size;
            return f.remove();
        } else {
            return unlinkFirst(f);
        }
    }

    @Override
    public boolean offer(Integer e) {
        return add(e);
    }

    private static class Node implements Serializable {

        /**
         *
         */
        private static final long serialVersionUID = -3387052246825592890L;

        int[] items;
        // points to first actual item slot
        int startIndex = 0;
        // points to last free slot
        int endIndex = 1;
        // pointer to next array node
        Node next;

        Node(int element, int arrayLength) {
            if (arrayLength < 1) {
                throw new IllegalStateException();
            }
            this.items = new int[arrayLength];
            items[0] = element;
        }

        // removes the first element
        public int remove() {
            return items[startIndex++];
        }

        // adds an element to the end
        public void add(int e) {
            items[endIndex++] = e;
        }

        // checks whether an element can be added to the node's array
        boolean hasFreeSpace() {
            return endIndex < items.length;
        }

        @SuppressWarnings("unused")
        public int get(int i) {
            return items[i + startIndex];
        }

        public void trim() {
            // reduce size of item array to remove empty space
            if (items.length > endIndex) {
                int[] temp = items;
                items = new int[endIndex];
                System.arraycopy(temp, startIndex, items, startIndex, endIndex - startIndex);
            }
        }
    }

    private static class NodePointer {
        // pointer to the array node
        final Node node;
        // index to the last added element (atm)
        final int index;

        NodePointer(Node node) {
            this.node = node;
            if (node != null) {
                this.index = node.endIndex - 1;
            } else {
                this.index = -1;
            }
        }
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
    public Iterator<Integer> iterator() {
        return new MyIterator();
    }

    public MyIterator iterator2() {
        return new MyIterator();
    }

    public Iterator<Integer> iterator(final NodePointer start) {
        return new MyIterator(start);
    }

    public MyIterator iterator2(final NodePointer start) {
        return new MyIterator(start);
    }

    public final class MyIterator implements Iterator<Integer> {

        Node currentNode;
        int index;

        MyIterator(NodePointer start) {
            currentNode = start.node;
            index = start.index;
        }

        MyIterator() {
            currentNode = first;
            index = first == null ? 0 : first.startIndex;
        }

        @Override
        public boolean hasNext() {
            return currentNode != null && index < currentNode.endIndex;
        }

        @Override
        public Integer next() {
            int temp = currentNode.items[index++];
            if (index >= currentNode.endIndex) {
                currentNode = currentNode.next;
                index = 0;
            }
            return temp;
        }
        
        public int peek() {
             return currentNode.items[index];
        }

        public int nextNoAutoBoxing() {
            int temp = currentNode.items[index++];
            if (index >= currentNode.endIndex) {
                currentNode = currentNode.next;
                index = 0;
            }
            return temp;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[ ");
        for (Integer e : this) {
            builder.append(e).append(", ");
        }
        builder.setLength(builder.length() > 2 ? builder.length() - 2 : builder.length());
        builder.append(" ]");
        return builder.toString();
    }

    public void trim() {
        if (last != null) {
            last.trim();
        }
    }

}
