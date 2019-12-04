package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.Function;

/**
 * Simple single linked queue implementation using fixed/variable size array nodes.
 */
public class ConcurrentBufferedArrayQueue<E> extends BufferedArrayQueue<E> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -42217897543622740L;

	public ConcurrentBufferedArrayQueue(File outputDir, String filePrefix, boolean deleteOnExit,
			Type serializationType) {
		super(outputDir, filePrefix, deleteOnExit, serializationType);
	}

	public ConcurrentBufferedArrayQueue(File output, String filePrefix, int nodeArrayLength, boolean deleteOnExit,
			Type serializationType) {
		super(output, filePrefix, nodeArrayLength, deleteOnExit, serializationType);
	}

	public ConcurrentBufferedArrayQueue(File output, String filePrefix, int nodeArrayLength, Type serializationType) {
		super(output, filePrefix, nodeArrayLength, serializationType);
	}

	public ConcurrentBufferedArrayQueue(File outputDir, String filePrefix, Type serializationType) {
		super(outputDir, filePrefix, serializationType);
	}

	private transient Lock lock = new ReentrantLock();
	
	@Override
	public void sleep() {
		lock.lock();
		try {
			super.sleep();
		} finally {
			lock.unlock();
		}
	}
    
	@Override
    public int size() {
    	lock.lock();
    	try {
    		return super.size();
    	} finally {
    		lock.unlock();
    	}
    }

	@Override
    public boolean add(E e) {
    	lock.lock();
    	try {
    		return super.add(e);
    	} finally {
    		lock.unlock();
    	}
    }

	@Override
    public void clear() {
    	lock.lock();
    	try {
    		super.clear();
    	} finally {
    		lock.unlock();
		}
    }

	@Override
    public void clear(long count) {
    	lock.lock();
    	try {
    		super.clear();
    	} finally {
    		lock.unlock();
		}
    }

	@Override
    public void clearLast(long count) {
    	lock.lock();
    	try {
    		super.clearLast(count);
    	} finally {
    		lock.unlock();
		}
    }

	@Override
    public void clearFrom(long startIndex, long count) {
    	lock.lock();
    	try {
    		super.clearFrom(startIndex, count);
    	} finally {
    		lock.unlock();
		}
    }

	@Override
    public E lastElement() {
    	lock.lock();
    	try {
    		return super.lastElement();
    	} finally {
    		lock.unlock();
    	}
    }
    
    // Queue operations

	@Override
    public E element() {
    	lock.lock();
    	try {
    		return super.element();
    	} finally {
    		lock.unlock();
    	}
    }

	@Override
    public E remove() {
    	lock.lock();
    	try {
    		return super.remove();
    	} finally {
    		lock.unlock();
    	}
    }
    
	@Override
	public boolean isEmpty() {
		lock.lock();
    	try {
    		return super.isEmpty();
    	} finally {
			lock.unlock();
		}
	}


	@Override
	public void lock() {
		lock.lock();
    	try {
    		super.lock();
    	} finally {
			lock.unlock();
		}
	}

	@Override
	public void unlock() {
		lock.lock();
    	try {
    		super.unlock();
    	} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean offer(E e) {
		lock.lock();
    	try {
    		return super.offer(e);
    	} finally {
			lock.unlock();
		}
	}

	@Override
	public E get(long i) {
		lock.lock();
    	try {
    		return super.get(i);
    	} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean isDeleteOnExit() {
		lock.lock();
    	try {
    		return super.isDeleteOnExit();
    	} finally {
			lock.unlock();
		}
	}

	@Override
	public E getAndReplaceWith(long i, Function<E, E> function) {
		lock.lock();
    	try {
    		return super.getAndReplaceWith(i, function);
    	} finally {
    		lock.unlock();
    	}
	}

	@Override
	public void deleteOnExit() {
		lock.lock();
		try {
			super.deleteOnExit();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public long longSize() {
		lock.lock();
		try {
			return super.longSize();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public E peekLast() {
		lock.lock();
		try {
			return super.peekLast();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public E peek() {
		lock.lock();
		try {
			return super.peek();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public E poll() {
		lock.lock();
		try {
			return super.poll();
		} finally {
			lock.unlock();
		}
	}

}
