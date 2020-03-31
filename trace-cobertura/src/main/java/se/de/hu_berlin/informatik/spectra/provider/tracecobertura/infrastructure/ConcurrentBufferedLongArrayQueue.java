package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.Function;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple single linked queue implementation using fixed/variable size array nodes.
 */
public class ConcurrentBufferedLongArrayQueue extends BufferedLongArrayQueue implements Serializable {


	/**
	 *
	 */
	private static final long serialVersionUID = -5800736473476096186L;
	private transient Lock lock = new ReentrantLock();


	public ConcurrentBufferedLongArrayQueue(File outputDir, String filePrefix, boolean deleteOnExit) {
		super(outputDir, filePrefix, deleteOnExit);
	}


	public ConcurrentBufferedLongArrayQueue(File output, String filePrefix, int nodeArrayLength, boolean deleteOnExit) {
		super(output, filePrefix, nodeArrayLength, deleteOnExit);
	}


	public ConcurrentBufferedLongArrayQueue(File output, String filePrefix, int nodeArrayLength) {
		super(output, filePrefix, nodeArrayLength);
	}

	public ConcurrentBufferedLongArrayQueue(File outputDir, String filePrefix) {
		super(outputDir, filePrefix);
	}

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
	public boolean add(long e) {
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
	public long lastElement() {
		lock.lock();
		try {
			return super.lastElement();
		} finally {
			lock.unlock();
		}
	}

	// Queue operations

	@Override
	public long element() {
		lock.lock();
		try {
			return super.element();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public long remove() {
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
	public boolean offer(long e) {
		lock.lock();
		try {
			return super.offer(e);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public long get(long i) {
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
	public long getAndReplaceWith(long i, Function<Long, Long> function) {
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
