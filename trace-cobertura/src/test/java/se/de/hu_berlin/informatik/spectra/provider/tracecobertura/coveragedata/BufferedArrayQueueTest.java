/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedArrayQueue.Type;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedLongArrayQueue;


/**
 * @author Simon
 *
 */
public class BufferedArrayQueueTest {

	/*
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/*
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/*
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/*
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	private File outputDir = new File("target" + File.separator + "bufferedArrayQueueTest");
	
	/*
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedBufferedArrayQueue#SingleLinkedBufferedArrayQueue(java.io.File, java.lang.String, int)}.
	 */
	@Test
	public void testSingleLinkedBufferedArrayQueueFileStringInt() throws Exception {
		BufferedArrayQueue<Integer> queue = new BufferedArrayQueue<Integer>(outputDir, "test", 5, Type.INTEGER);
		
		for (int i = 0; i < 50; ++i) {
			queue.add(i);
		}
		queue.sleep();
		
//		Thread.sleep(5000);
		Iterator<Integer> iterator = queue.iterator();
		
		int i = 0;
		while (iterator.hasNext()) {
			Assert.assertEquals(i++, iterator.next().intValue());
		}
		
		queue.clear();
	}
	
	/*
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedBufferedArrayQueue#SingleLinkedBufferedArrayQueue(java.io.File, java.lang.String, int)}.
	 */
	@Test
	public void testSingleLinkedBufferedArrayQueueFileStringInt2() throws Exception {
		BufferedArrayQueue<Integer> queue = new BufferedArrayQueue<Integer>(outputDir, "test2", 5, Type.INTEGER);
		
		testIntQueue(queue);
		testIntQueue(queue);
	}

	private void testIntQueue(BufferedArrayQueue<Integer> queue) {
		for (int i = 0; i < 50; ++i) {
			queue.add(i);
		}
		queue.sleep();
		
		Assert.assertEquals(50, queue.size());
		
//		Assert.assertEquals(2, queue.get(2).intValue());
		Assert.assertEquals(30, queue.get(30).intValue());
		
		queue.clear(10);
		Assert.assertEquals(40, queue.size());
		queue.sleep();
		
		queue.clear(13);
		Assert.assertEquals(27, queue.size());
		queue.sleep();
		
		Assert.assertEquals(23, queue.get(0).intValue());
		Assert.assertEquals(25, queue.get(2).intValue());
		Assert.assertEquals(40, queue.get(17).intValue());
		queue.sleep();
		
		Iterator<Integer> iterator = queue.iterator();
		
		int i = 23;
		while (iterator.hasNext()) {
//			System.out.println(iterator.next());
			Assert.assertEquals(i++, iterator.next().intValue());
		}
		queue.sleep();
		
		queue.remove();
		queue.element();
		
		Assert.assertEquals(26, queue.get(2).intValue());
		Assert.assertEquals(41, queue.get(17).intValue());
		queue.sleep();
		
		queue.clear();
		Assert.assertEquals(0, queue.size());
	}
	
	/*
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedBufferedArrayQueue#SingleLinkedBufferedArrayQueue(java.io.File, java.lang.String, int)}.
	 */
	@Test
	public void testSingleLinkedBufferedArrayQueueFileStringInt3() throws Exception {
		BufferedArrayQueue<int[]> queue = new BufferedArrayQueue<int[]>(outputDir, "test3", 5, Type.OTHER);
		
		for (int i = 0; i < 50; ++i) {
			queue.add(new int[] {i});
		}
		
		queue.clear(23);
		
		Iterator<int[]> iterator = queue.iterator();
		
		int i = 23;
		while (iterator.hasNext()) {
			Assert.assertEquals(i++, iterator.next()[0]);
		}
		
		queue.clear();
		
	}
	
	
	
	/*
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedBufferedArrayQueue#SingleLinkedBufferedArrayQueue(java.io.File, java.lang.String, int)}.
	 */
	@Test
	public void testSingleLinkedBufferedIntArrayQueueFileStringInt() throws Exception {
		BufferedIntArrayQueue queue = new BufferedIntArrayQueue(outputDir, "testInt", 5);
		
		for (int i = 0; i < 50; ++i) {
			queue.add(i);
		}
		Assert.assertEquals(50, queue.size());
		queue.sleep();
		
		queue.clear(100);
		Assert.assertEquals(0, queue.size());
		
		for (int i = 0; i < 50; ++i) {
			queue.add(i);
		}
		queue.sleep();
//		
		queue.clear(6);
		
//		Thread.sleep(5000);
		se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.ReplaceableCloneableIterator iterator = queue.iterator();
		
		int i = 6;
		while (iterator.hasNext()) {
			Assert.assertEquals(i++, iterator.next());
		}
		Assert.assertEquals(50,i);
		queue.clear();
	}
	
	@Test
	public void testSingleLinkedBufferedIntArrayQueueBla() throws Exception {
		BufferedIntArrayQueue queue = new BufferedIntArrayQueue(outputDir, "testIntBla", 5);
		
		for (int i = 0; i < 25; i += 1) {
			for (int k = 0; k <= i + 5; ++k) {
				queue.clear();
				for (int j = 0; j < 25; ++j) {
					queue.add(i);
				}
				queue.sleep();
				queue.clear(k);
				Assert.assertEquals(25-k > 0 ? 25-k : 0, queue.size());
			}
		}
		
		queue.clear();
	}
	
	/*
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedBufferedArrayQueue#SingleLinkedBufferedArrayQueue(java.io.File, java.lang.String, int)}.
	 */
	@Test
	public void testSingleLinkedBufferedIntArrayQueueFileStringInt2() throws Exception {
		BufferedIntArrayQueue queue = new BufferedIntArrayQueue(outputDir, "testInt2", 5);
		
		testIntQueue(queue);
		testIntQueue(queue);
	}
	
	private void testIntQueue(BufferedIntArrayQueue queue) {
		for (int i = 0; i < 50; ++i) {
			queue.add(i);
		}
		queue.sleep();
		
		Assert.assertEquals(50, queue.size());
		
//		Assert.assertEquals(2, queue.get(2).intValue());
		Assert.assertEquals(30, queue.get(30));
		
		queue.clear(10);
		Assert.assertEquals(40, queue.size());
		queue.sleep();
		
		queue.clear(13);
		Assert.assertEquals(27, queue.size());
		queue.sleep();
		
		Assert.assertEquals(23, queue.get(0));
		Assert.assertEquals(25, queue.get(2));
		Assert.assertEquals(40, queue.get(17));
		queue.sleep();
		
		se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.ReplaceableCloneableIterator iterator = queue.iterator();
		
		int i = 23;
		while (iterator.hasNext()) {
//			System.out.println(iterator.next());
			Assert.assertEquals(i++, iterator.next());
		}
		queue.sleep();
		
		queue.remove();
		queue.element();
		
		Assert.assertEquals(26, queue.get(2));
		Assert.assertEquals(41, queue.get(17));
		queue.sleep();
		
		queue.clear();
		Assert.assertEquals(0, queue.size());
	}
	
	/*
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedBufferedArrayQueue#SingleLinkedBufferedArrayQueue(java.io.File, java.lang.String, int)}.
	 */
	@Test
	public void testSingleLinkedBufferedLongArrayQueueFileStringInt() throws Exception {
		BufferedLongArrayQueue queue = new BufferedLongArrayQueue(outputDir, "testLong", 5);
		
		for (int i = 0; i < 50; ++i) {
			queue.add(i);
		}
		Assert.assertEquals(50, queue.size());
		queue.sleep();
		
		queue.clear(100);
		Assert.assertEquals(0, queue.size());
		
		for (int i = 0; i < 50; ++i) {
			queue.add(i);
		}
		queue.sleep();
//		
		queue.clear(6);
		
//		Thread.sleep(5000);
		se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.longs.ReplaceableCloneableIterator iterator = queue.iterator();
		
		int i = 6;
		while (iterator.hasNext()) {
			Assert.assertEquals(i++, iterator.next());
		}
		Assert.assertEquals(50,i);
		
		queue.clear();
	}
	
	/*
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedBufferedArrayQueue#SingleLinkedBufferedArrayQueue(java.io.File, java.lang.String, int)}.
	 */
	@Test
	public void testSingleLinkedBufferedLongArrayQueueFileStringInt2() throws Exception {
		BufferedLongArrayQueue queue = new BufferedLongArrayQueue(outputDir, "testLong2", 5);
		
		testLongQueue(queue);
		testLongQueue(queue);
	}
	
	private void testLongQueue(BufferedLongArrayQueue queue) {
		for (int i = 0; i < 50; ++i) {
			queue.add(i);
		}
		queue.sleep();
		
		Assert.assertEquals(50, queue.size());
		
//		Assert.assertEquals(2, queue.get(2).intValue());
		Assert.assertEquals(30, queue.get(30));
		
		queue.clear(10);
		Assert.assertEquals(40, queue.size());
		queue.sleep();
		
		queue.clear(13);
		Assert.assertEquals(27, queue.size());
		queue.sleep();
		
		Assert.assertEquals(23, queue.get(0));
		Assert.assertEquals(25, queue.get(2));
		Assert.assertEquals(40, queue.get(17));
		queue.sleep();
		
		se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.longs.ReplaceableCloneableIterator iterator = queue.iterator();
		
		int i = 23;
		while (iterator.hasNext()) {
//			System.out.println(iterator.next());
			Assert.assertEquals(i++, iterator.next());
		}
		queue.sleep();
		
		queue.remove();
		queue.element();
		
		Assert.assertEquals(26, queue.get(2));
		Assert.assertEquals(41, queue.get(17));
		queue.sleep();
		
		queue.clear();
		Assert.assertEquals(0, queue.size());
	}
	
	private static final int NUM_INTS = 500000;
	private static final int NUM_REPS = 100;

	@Test
	public void testTime() {
		outputDir.mkdirs();

		int[] ints = new int[NUM_INTS];
		Random r = new Random();
		for (int i=0; i<NUM_INTS; i++) {
			ints[i] = r.nextInt();
		}
		//		  time("DataOutputStream", new IntWriter() {
		//		    public void write(int[] ints) {
		//		      storeDO(ints);
		//		    }
		//		  }, ints);

//		time("ObjectOutputStreamWrite", new IntWriter() {
//			public void write(int[] ints) {
//				for (int i = 0; i < NUM_REPS; ++i) {
//					storeOO(ints, "oo.out");
//				}
//			}
//		}, ints);
//		time("ObjectOutputStreamRead", new IntWriter() {
//			public void write(int[] ints) {
//				for (int i = 0; i < NUM_REPS; ++i) {
//					readOO(ints, "oo.out");
//				}
//			}
//		}, ints);

		for (int j = 0; j < 2; ++j) {
		time("FileChannel1write", new IntWriter() {
			public void write(int[] ints) {
				for (int i = 0; i < NUM_REPS; ++i) {
					storeFC(ints, "fc.out");
//					readFC(ints, "fc.out");
					
				}
			}
		}, ints);
//		time("FileChannel1read", new IntWriter() {
//			public void write(int[] ints) {
//				for (int i = 0; i < NUM_REPS; ++i) {
//					readFC(ints, "fc.out");
//				}
//			}
//		}, ints);
		
		time("FileChannel4write", new IntWriter() {
			public void write(int[] ints) {
				for (int i = 0; i < NUM_REPS; ++i) {
					storeFC4(ints, "fc4.out");
//					readFC(ints, "fc.out");
					
				}
			}
		}, ints);
//		time("FileChannel1read", new IntWriter() {
//			public void write(int[] ints) {
//				for (int i = 0; i < NUM_REPS; ++i) {
//					readFC(ints, "fc.out");
//				}
//			}
//		}, ints);
		}
		
//		// seems to have trouble deleting the created file after termination
//		time("FileChannel2write", new IntWriter() {
//			public void write(int[] ints) {
//				for (int i = 0; i < NUM_REPS; ++i) {
//					storeFC2(ints, "fc2.out");
////					readFC2(ints, "fc2.out");
//				}
//			}
//		}, ints);
////		time("FileChannel2read", new IntWriter() {
////			public void write(int[] ints) {
////				for (int i = 0; i < NUM_REPS; ++i) {
////					readFC2(ints, "fc2.out");
////				}
////			}
////		}, ints);
//		
//		time("FileChannel3write", new IntWriter() {
//			public void write(int[] ints) {
//				for (int i = 0; i < NUM_REPS; ++i) {
//					storeFC3(ints, "fc3.out");
////					readFC3(ints, "fc3.out");
//				}
//			}
//		}, ints);
////		time("FileChannel3read", new IntWriter() {
////			public void write(int[] ints) {
////				for (int i = 0; i < NUM_REPS; ++i) {
////					readFC3(ints, "fc3.out");
////				}
////			}
////		}, ints);
	}

	interface IntWriter {
		void write(int[] ints);
	}
	
	private static void time(String name, IntWriter writer, int[] ints) {
		long start = System.nanoTime();
		writer.write(ints);
		long end = System.nanoTime();
		double ms = (end - start) / 1000000d;
		System.out.printf("%s wrote %,d ints in %,.3f ms%n", name, ints.length, ms);
	}

	private void storeOO(int[] ints, String filename) {
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outputDir.getAbsolutePath() + File.separator + filename))) {
			out.writeObject(ints);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
//		new File(outputDir.getAbsolutePath() + File.separator + filename).deleteOnExit();
	}
	
	private void readOO(int[] ints, String filename) {
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(outputDir.getAbsolutePath() + File.separator + filename))) {
			int[] items = (int[]) in.readObject();
			
			for (int i = 0; i < items.length; ++i) {
				Assert.assertEquals(ints[i], items[i]);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new File(outputDir.getAbsolutePath() + File.separator + filename).deleteOnExit();
	}

	//		private void storeDO(int[] ints) {
	//		  DataOutputStream out = null;
	//		  try {
	//		    out = new DataOutputStream(new FileOutputStream(outputDir.getAbsolutePath() + File.separator + "data.out"));
	//		    for (int anInt : ints) {
	//		      out.write(anInt);
	//		    }
	//		  } catch (IOException e) {
	//		    throw new RuntimeException(e);
	//		  } finally {
	//		    safeClose(out);
	//		  }
	//		}

	private void storeFC(int[] ints, String filename) {
		new File(outputDir.getAbsolutePath() + File.separator + filename).delete();
		try (FileOutputStream out = new FileOutputStream(outputDir.getAbsolutePath() + File.separator + filename)) {
			try (FileChannel file = out.getChannel()) {
				ByteBuffer directBuf = ByteBuffer.allocateDirect(4 * ints.length);
				for (int i : ints) {
					directBuf.putInt(i);
				}
				directBuf.flip();
				file.write(directBuf);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
//		new File(outputDir.getAbsolutePath() + File.separator + filename).deleteOnExit();
	}
	
	private void storeFC4(int[] ints, String filename) {
		new File(outputDir.getAbsolutePath() + File.separator + filename).delete();
		try (RandomAccessFile raFile = new RandomAccessFile(outputDir.getAbsolutePath() + File.separator + filename, "rw")) {
			try (FileChannel file = raFile.getChannel()) {
				ByteBuffer directBuf = ByteBuffer.allocateDirect(4 * ints.length);
				for (int i : ints) {
					directBuf.putInt(i);
				}
				directBuf.flip();
				file.write(directBuf);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
//		new File(outputDir.getAbsolutePath() + File.separator + filename).deleteOnExit();
	}
	
	private void readFC(int[] ints, String filename) {
		try (FileInputStream in = new FileInputStream(outputDir.getAbsolutePath() + File.separator + filename)) {
			try (FileChannel file = in.getChannel()) {
				ByteBuffer directBuf = ByteBuffer.allocateDirect(4 * ints.length);
				file.read(directBuf);
				directBuf.flip();
				
				int arrayLength = directBuf.capacity()/4;
				int[] items = new int[arrayLength];
				for (int i = 0; i < arrayLength; ++i) {
					items[i] = directBuf.getInt();
				}
				
				for (int i = 0; i < arrayLength; ++i) {
					Assert.assertEquals(ints[i], items[i]);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		new File(outputDir.getAbsolutePath() + File.separator + filename).delete();
	}
	
	private void storeFC3(int[] ints, String filename) {
		new File(outputDir.getAbsolutePath() + File.separator + filename).delete();
		try (FileOutputStream out = new FileOutputStream(outputDir.getAbsolutePath() + File.separator + filename)) {
			try (FileChannel file = out.getChannel()) {
				ByteBuffer directBuf = ByteBuffer.allocate(4 * ints.length);
				byte[] backingArray = directBuf.array();
				int j = -1;
				for (int i = 0; i < ints.length; ++i) {
					int value = ints[i];
					backingArray[++j] = (byte)(value >> 24);
//					System.out.println(backingArray[j]);
					backingArray[++j] = (byte)(value >> 16);
//					System.out.println(backingArray[j]);
					backingArray[++j] = (byte)(value >> 8);
//					System.out.println(backingArray[j]);
					backingArray[++j] = (byte)value;
//					System.out.println(backingArray[j]);
				}
//				directBuf.flip();
				directBuf.limit(directBuf.capacity());
				file.write(directBuf);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
//		new File(outputDir.getAbsolutePath() + File.separator + filename).deleteOnExit();
	}
	
	private void readFC3(int[] ints, String filename) {
		try (FileInputStream in = new FileInputStream(outputDir.getAbsolutePath() + File.separator + filename)) {
			try (FileChannel file = in.getChannel()) {
				ByteBuffer directBuf = ByteBuffer.allocate(4 * ints.length);
				file.read(directBuf);
//				directBuf.flip();
				
				int arrayLength = directBuf.capacity()/4;
				byte[] backingArray = directBuf.array();
				int j = -1;
				int[] items = new int[arrayLength];
				for (int i = 0; i < arrayLength; ++i) {
//					items[i] = directBuf.getInt();
					items[i] = ((((int)backingArray[++j] & 0xff) << 24) |
			                (((int)backingArray[++j] & 0xff) << 16) |
							(((int)backingArray[++j] & 0xff) <<  8) |
							(((int)backingArray[++j] & 0xff)));
				}
				
				for (int i = 0; i < arrayLength; ++i) {
					Assert.assertEquals(ints[i], items[i]);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		new File(outputDir.getAbsolutePath() + File.separator + filename).delete();
	}

	private void storeFC2(int[] ints, String filename) {
		new File(outputDir.getAbsolutePath() + File.separator + filename).delete();
		try (RandomAccessFile raFile = new RandomAccessFile(outputDir.getAbsolutePath() + File.separator + filename, "rw")) {
			try (FileChannel file = raFile.getChannel()) {
				MappedByteBuffer buf = file.map(FileChannel.MapMode.READ_WRITE, 0, 4 * ints.length);
//				System.out.println(file.size() + ", " + (4*ints.length));
				for (int i : ints) {
					buf.putInt(i);
				}
				buf.force();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
//		new File(outputDir.getAbsolutePath() + File.separator + filename).deleteOnExit();
	}
	
	private void readFC2(int[] ints, String filename) {
		try (RandomAccessFile raFile = new RandomAccessFile(outputDir.getAbsolutePath() + File.separator + filename, "r")) {
			try (FileChannel file = raFile.getChannel()) {
				MappedByteBuffer directBuf = file.map(FileChannel.MapMode.READ_ONLY, 0, file.size());
				
//				System.out.println(file.size());
				int arrayLength = (int)(file.size()/4);
				int[] items = new int[arrayLength];
				for (int i = 0; i < arrayLength; ++i) {
					items[i] = directBuf.getInt();
				}
				
				for (int i = 0; i < arrayLength; ++i) {
					Assert.assertEquals(ints[i], items[i]);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		new File(outputDir.getAbsolutePath() + File.separator + filename).delete();
	}

}
