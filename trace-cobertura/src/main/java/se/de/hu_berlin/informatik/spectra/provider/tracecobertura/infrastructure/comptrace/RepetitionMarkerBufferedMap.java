package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedMap;

public class RepetitionMarkerBufferedMap extends BufferedMap<int[]> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 761316044011593924L;

	public RepetitionMarkerBufferedMap(File outputDir, String filePrefix, boolean deleteOnExit) {
		super(outputDir, filePrefix, deleteOnExit);
	}

	public RepetitionMarkerBufferedMap(File output, String filePrefix, int maxSubMapSize, boolean deleteOnExit) {
		super(output, filePrefix, maxSubMapSize, deleteOnExit);
	}

	public RepetitionMarkerBufferedMap(File output, String filePrefix, int maxSubMapSize) {
		super(output, filePrefix, maxSubMapSize);
	}

	public RepetitionMarkerBufferedMap(File outputDir, String filePrefix) {
		super(outputDir, filePrefix);
	}
	
	private transient ByteBuffer writeBuffer = null;
	
	private ByteBuffer getFreshBuffer() {
    	// only (lazily) allocate ONE buffer per buffered map object! allocation costs are potentially high...
    	if (writeBuffer == null) {
//    		System.err.println(Runtime.getRuntime().maxMemory() + ", " + (4 * (maxSubMapSize * 3)));
    		writeBuffer = ByteBuffer.allocateDirect(4 * (getMaxSubMapSize() * 3));
    	}
    	writeBuffer.clear();
    	return writeBuffer;
    }
	
	@Override
	protected void store(Node<int[]> node, String filename) {
		try (RandomAccessFile raFile = new RandomAccessFile(filename, "rw")) {
			try (FileChannel file = raFile.getChannel()) {

				ByteBuffer directBuf = getFreshBuffer();
				for (Entry<Integer, int[]> entry : node.getSubMap().entrySet()) {
					directBuf.putInt(entry.getKey());
					directBuf.putInt(entry.getValue()[0]);
					directBuf.putInt(entry.getValue()[1]);
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

	@Override
	protected Node<int[]> load(int storeIndex, String filename) throws IllegalStateException {
		Node<int[]> loadedNode;
		try (FileInputStream in = new FileInputStream(filename)) {
			try (FileChannel file = in.getChannel()) {
				long fileSize = file.size();
				if (fileSize > Integer.MAX_VALUE) {
					throw new UnsupportedOperationException("File size too big!");
				}
				ByteBuffer directBuf = getFreshBuffer();
				file.read(directBuf);
				directBuf.flip();
				
				Map<Integer, int[]> map = new HashMap<>();
				
				
				int count = (int)fileSize/12; // size/4/3
				// fill sub map
				for (int i = 0; i < count; ++i) {
					map.put(directBuf.getInt(), new int[] { directBuf.getInt(), directBuf.getInt()});
				}
				
				loadedNode = new Node<>(storeIndex, map);

				// file can not be removed, due to serialization! TODO
				if (deleteOnExit) {
					new File(filename).deleteOnExit();
				}
			}
		} catch (IOException | IndexOutOfBoundsException e) {
			e.printStackTrace();
			throw new IllegalStateException();
		}
		return loadedNode;
	}
}
