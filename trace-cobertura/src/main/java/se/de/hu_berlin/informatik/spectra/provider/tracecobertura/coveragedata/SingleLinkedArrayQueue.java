package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * Simple single linked queue implementation using fixed/variable size array nodes.
 * 
 * @param <E> 
 * the type of elements held in the queue
 */
public class SingleLinkedArrayQueue<E> extends AbstractQueue<E> implements Queue<E> {
	
	private static final int ARRAY_SIZE = 1000;
	
	private int arrayLength = ARRAY_SIZE;
	private int size = 0;
    private Node<E> first;
    private Node<E> last;

    /**
     * Constructs an empty queue.
     */
    public SingleLinkedArrayQueue() {
    }
    
    public SingleLinkedArrayQueue(int nodeArrayLength) {
    	this.arrayLength = nodeArrayLength;
    }

    public SingleLinkedArrayQueue(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    public NodePointer<E> getLastNodePointer() {
		return new NodePointer<>(last);
	}
    
    /**
     * Creates a new node if the last node is null or full.
     * Increases the size variable.
     */
    private void linkLast(E e) {
        final Node<E> l = last;
        final Node<E> newNode = new Node<>(e, arrayLength);
        last = newNode;
        if (l == null)
            first = newNode;
        else
            l.next = newNode;
        ++size;
    }

    /**
     * Removes the first node, if it only contains one item.
     * Decreases the size variable.
     */
    private E unlinkFirst(Node<E> f) {
        // assert f == first && f != null;
        @SuppressWarnings("unchecked")
		final E element = (E) f.items[f.startIndex];
        final Node<E> next = f.next;
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
        Iterator<E> iterator = this.iterator();
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
    public boolean add(E e) {
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
        for (Node<E> x = first; x != null; ) {
            Node<E> next = x.next;
            for (int j = x.startIndex; j < x.endIndex; ++j) {
            	x.items[j] = null;
            	++x.startIndex;
            }
            x.items = null;
            x.next = null;
            x = next;
        }
        first = last = null;
        size = 0;
    }
    
    /**
     * Same as {@link #clear()}, but only removes the first {@code count} elements.
     * 
     * @param count
     * the number of elements to clear from the list
     */
    public void clear(int count) {
        // Clearing all of the links between nodes is "unnecessary", but:
        // - helps a generational GC if the discarded nodes inhabit
        //   more than one generation
        // - is sure to free memory even if there is a reachable Iterator
    	int i = 0;
    	Node<E> x = first;
        while (x != null && i < count) {
            Node<E> next = x.next;
            for (int j = x.startIndex; j < x.endIndex && i < count; ++j, ++i) {
            	x.items[j] = null;
            	++x.startIndex;
            }
            if (x.startIndex >= x.endIndex) {
            	x.items = null;
            	x.next = null;
            	x = next;
            } else if (i >= count) {
            	break;
			}
        }
        size -= count;
        first = x;
        if (x == null) {
        	last = null;
        	size = 0;
        }
    }

    // Queue operations

    @SuppressWarnings("unchecked")
	@Override
    public E peek() {
        final Node<E> f = first;
        return ((f == null) ? null : (E) f.items[f.startIndex]);
    }

    @SuppressWarnings("unchecked")
	@Override
    public E element() {
    	final Node<E> f = first;
        if (f == null)
            throw new NoSuchElementException();
        return (E) f.items[f.startIndex];
    }

    @Override
    public E poll() {
        final Node<E> f = first;
        return (f == null) ? null : removeFirst(f);
    }

    @Override
    public E remove() {
    	final Node<E> f = first;
        if (f == null)
            throw new NoSuchElementException();
        return removeFirst(f);
    }
    
    /**
     * Removes the first element.
     */
    private E removeFirst(Node<E> f) {
    	if (f.startIndex < f.endIndex - 1) {
    		--size;
    		return f.remove();
    	} else {
    		return unlinkFirst(f);	
		}
    }

    @Override
    public boolean offer(E e) {
        return add(e);
    }

	static class Node<E> {
        Object[] items;
        // points to first actual item slot
        int startIndex = 0;
        // points to last free slot
        int endIndex = 1;
        // pointer to next array node
        Node<E> next;

		Node(E element, int arrayLength) {
        	if (arrayLength < 1) {
        		throw new IllegalStateException();
        	}
            this.items = new Object[arrayLength];
            items[0] = element;
        }
        
        // removes the first element
        public E remove() {
        	@SuppressWarnings("unchecked")
			E temp = (E) items[startIndex];
        	items[startIndex++] = null;
			return temp;
		}

        // adds an element to the end
		public void add(E e) {
			items[endIndex++] = e;
		}

		// checks whether an element can be added to the node's array
		boolean hasFreeSpace() {
        	return endIndex < items.length;
        }
    }
    
    static class NodePointer<E> {
        // pointer to the array node
        final Node<E> node;
        // index to the last added element (atm)
        final int index;

        NodePointer(Node<E> node) {
            this.node = node;
            this.index = node.endIndex - 1;
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
	public Iterator<E> iterator() {
		return new MyIterator();
	}
	
	public Iterator<E> iterator(final NodePointer<E> start) {
		return new MyIterator(start);
	}
	
	private final class MyIterator implements Iterator<E> {

		Node<E> currentNode;
		int index;

		private MyIterator(NodePointer<E> start) {
			currentNode = start.node;
			index = start.index;
		}
		
		private MyIterator() {
			currentNode = first;
			index = first == null ? 0 : first.startIndex;
		}

		@Override
		public boolean hasNext() {
			return currentNode != null && index < currentNode.endIndex;
		}

		@Override
		public E next() {
			@SuppressWarnings("unchecked")
			E temp = (E) currentNode.items[index++];
			if (index >= currentNode.endIndex) {
				currentNode = currentNode.next;
				index = 0;
			}
			return temp;
		}
	}

}