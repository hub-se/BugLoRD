package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentBufferedMap<E> extends BufferedMap<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5273626486422999710L;

	public ConcurrentBufferedMap(File outputDir, String filePrefix, boolean deleteOnExit) {
		super(outputDir, filePrefix, deleteOnExit);
	}

	public ConcurrentBufferedMap(File output, String filePrefix, int maxSubMapSize, boolean deleteOnExit) {
		super(output, filePrefix, maxSubMapSize, deleteOnExit);
	}

	public ConcurrentBufferedMap(File output, String filePrefix, int maxSubMapSize) {
		super(output, filePrefix, maxSubMapSize);
	}

	public ConcurrentBufferedMap(File outputDir, String filePrefix) {
		super(outputDir, filePrefix);
	}

	protected transient Lock lock = new ReentrantLock();
	
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
	public void clear() {
		lock.lock();
		try {
			super.clear();	
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
	public boolean containsKey(Object key) {
		lock.lock();
		try {
			return super.containsKey(key);	
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean containsValue(Object value) {
		lock.lock();
		try {
			return super.containsValue(value);	
		} finally {
			lock.unlock();
		}
	}

	@Override
	public E get(Object key) {
		lock.lock();
		try {
			return super.get(key);	
		} finally {
			lock.unlock();
		}
	}

	@Override
	public E put(Integer key, E value) {
		lock.lock();
		try {
			return super.put(key, value);	
		} finally {
			lock.unlock();
		}
	}

	@Override
	public E remove(Object key) {
		lock.lock();
		try {
			return super.remove(key);	
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends E> m) {
		lock.lock();
		try {
			super.putAll(m);	
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Set<Integer> keySet() {
		lock.lock();
		try {
			return super.keySet();	
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Collection<E> values() {
		lock.lock();
		try {
			return super.values();	
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Set<Entry<Integer, E>> entrySet() {
		lock.lock();
		try {
			return super.entrySet();	
		} finally {
			lock.unlock();
		}
	}

	@Override
	protected Node<E> load(int storeIndex, String filename) throws IllegalStateException {
		lock.lock();
		try {
			return super.load(storeIndex, filename);
		} finally {
			lock.unlock();
		}
	}

	@Override
	protected void cacheNode(Node<E> node) {
		lock.lock();
		try {
			super.cacheNode(node);
		} finally {
			lock.unlock();
		}
	}

	@Override
	protected void store(Node<E> node, String filename) {
		lock.lock();
		try {
			super.store(node, filename);
		} finally {
			lock.unlock();
		}
	}

}
