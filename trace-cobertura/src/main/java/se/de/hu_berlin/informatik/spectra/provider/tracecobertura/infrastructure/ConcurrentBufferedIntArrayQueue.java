package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.Function;

/**
 * Simple single linked queue implementation using fixed/variable size array nodes.
 */
public class ConcurrentBufferedIntArrayQueue extends BufferedIntArrayQueue implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -5334620325979323897L;

	public ConcurrentBufferedIntArrayQueue(File outputDir, String filePrefix, boolean deleteOnExit) {
		super(outputDir, filePrefix, deleteOnExit);
	}


	public ConcurrentBufferedIntArrayQueue(File output, String filePrefix, int nodeArrayLength, boolean deleteOnExit) {
		super(output, filePrefix, nodeArrayLength, deleteOnExit);
	}


	public ConcurrentBufferedIntArrayQueue(File output, String filePrefix, int nodeArrayLength) {
		super(output, filePrefix, nodeArrayLength);
	}


	public ConcurrentBufferedIntArrayQueue(File outputDir, String filePrefix) {
		super(outputDir, filePrefix);
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
    public long size() {
    	lock.lock();
    	try {
    		return super.size();
    	} finally {
    		lock.unlock();
    	}
    }

	@Override
    public boolean add(int e) {
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
    public int lastElement() {
    	lock.lock();
    	try {
    		return super.lastElement();
    	} finally {
    		lock.unlock();
    	}
    }
    
    // Queue operations

	@Override
    public int element() {
    	lock.lock();
    	try {
    		return super.element();
    	} finally {
    		lock.unlock();
    	}
    }

	@Override
    public int remove() {
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
	public boolean offer(int e) {
		lock.lock();
    	try {
    		return super.offer(e);
    	} finally {
			lock.unlock();
		}
	}

	@Override
	public int get(long i) {
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
	public int getAndReplaceWith(long i, Function<Integer, Integer> function) {
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

}
