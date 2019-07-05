/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Queue;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Decodes...
 * 
 * @author Simon Heiden
 */
public class SpecialCompressedByteArrayToIntArrayQueueProcessor extends AbstractProcessor<String,Queue<int[]>> {
	
	// same buffer that is used in zip utils
	private static final int BUFER_SIZE = 4096;
	private byte[] buffer = new byte[BUFER_SIZE];
		
	public static final int DELIMITER = 1;
	public static final int TOTAL_END_MARKER = 0;
	
	private byte usedBits;
	private int sequenceLength;
	private int arrayPos;

	private boolean containsZero;
	private ZipFileWrapper zipFileWrapper;
	private Queue<int[]> result;
	
	public SpecialCompressedByteArrayToIntArrayQueueProcessor(ZipFileWrapper zipFileWrapper, 
			int sequenceLength, boolean containsZero, Queue<int[]> result) {
		super();
		this.containsZero = containsZero;
		this.zipFileWrapper = zipFileWrapper;
		this.result = result;
		this.sequenceLength = sequenceLength;
	}
	
	public SpecialCompressedByteArrayToIntArrayQueueProcessor(ZipFileWrapper zipFileWrapper, 
			boolean containsZero, Queue<int[]> result) {
		this(zipFileWrapper, 0, containsZero, result);
	}
	
	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public Queue<int[]> processItem(String fileName) {
		
		ZipInputStream inputStream = null;
		try {
			inputStream = zipFileWrapper.uncheckedGetAsStream(fileName);

			int len = getNextBytesFromInputStream(inputStream);
			readHeader(len);

			boolean atTotalEnd = false;

			byte currentByte = 0;
			int currentInt = 0;
			byte remainingBits = 0;
			byte bitsLeft = 0;
			int[] currentSequence = null;

			int intCounter = 0;

			//get all the encoded integers
			while (arrayPos < len) {
				//for each number, the number of bits to get is equal
				bitsLeft = usedBits;
				//if no bits remain to get from the current byte, then get the next one from the array
				if (remainingBits == 0) {
					currentByte = buffer[arrayPos];
					remainingBits = 8;
				}

				//if intCounter is zero, then we are at the start of a new sequence
				if (intCounter == 0) {
					if (currentSequence != null) {
						result.add(currentSequence);
					}
					currentSequence = null;
//					currentSequence = new ArrayList<>();
				}

				//as long as bits are still needed, get them from the array
				while (bitsLeft > 0) {
					if (bitsLeft > remainingBits) {
						currentInt = (currentInt << remainingBits) | (currentByte & 0xFF ) >>> (8 - remainingBits);
						bitsLeft -= remainingBits;
						//					remainingBits = 0;
						++arrayPos;
						if (arrayPos >= len) {
							len = getNextBytesFromInputStream(inputStream);
							arrayPos = 0;
						}
						currentByte = buffer[arrayPos];
						remainingBits = 8;
					} else { //bitsLeft <= remainingBits
						currentInt = (currentInt << bitsLeft) | (currentByte & 0xFF ) >>> (8 - bitsLeft);
						currentByte = (byte) (currentByte << bitsLeft);
						remainingBits -= bitsLeft;
						bitsLeft = 0;
					}
				}

				if (currentInt == TOTAL_END_MARKER) {
					atTotalEnd = true;
					break;
				} else {
					if (sequenceLength == 0) {
						if (currentInt == DELIMITER) {
							//reset the counter (start of new sequence)
							intCounter = 0;
						} else {
							if (currentSequence == null) {
								// first stored integer is always the size of the map to be constructed later
								currentSequence = new int[3*(containsZero ? currentInt-2 : currentInt-1)];
							} else {
								//add the next integer to the current sequence
								currentSequence[intCounter-1] = (containsZero ? currentInt-2 : currentInt-1);
							}
							++intCounter;
						}
					} else {
						if (currentSequence == null) {
							// first stored integer is always the size of the map to be constructed later
							currentSequence = new int[3*(containsZero ? currentInt-1 : currentInt)];
						} else {
							//add the next integer to the current sequence
							currentSequence[intCounter-1] = (containsZero ? currentInt-1 : currentInt);
						}
						++intCounter;
						//if the sequence ends here, reset the counter
						if (intCounter >= sequenceLength) {
							intCounter = 0;
						}
					}
				}
				//reset the current integer to all zeroes
				currentInt = 0;

				//if no bits remain in the current byte, then update the array position for the next step
				if (remainingBits == 0) {
					++arrayPos;
				}
				if (arrayPos >= len) {
					len = getNextBytesFromInputStream(inputStream);
					arrayPos = 0;
				}
			}

			if (!atTotalEnd) {
				Log.abort(this, "No total end marker was read!");
			}

			return result;
		} catch (ZipException e) {
			Log.abort(this, e, "Could not get input stream from file %s.", fileName);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		
		return null;
	}

	private void readHeader(int len) {
		// header should be 5 bytes:
		// | number of bits used for one element (1 byte) | sequence length (4 bytes) - 0 for delimiter mode |
		if (len < 5) {
			Log.abort(this, "Could not read header from input stream.");
		}
		usedBits = buffer[0];
		
		byte[] smallArray = { buffer[1], buffer[2], buffer[3], buffer[4] };
		ByteBuffer b = ByteBuffer.wrap(smallArray);
		//b.order(ByteOrder.BIG_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.
		sequenceLength = b.getInt();
		
		arrayPos = 5;
	}
	
	// returns the length of available bytes
	private int getNextBytesFromInputStream(InputStream is) {
		try {
			return is.read(buffer);
		} catch (IOException e) {
			Log.abort(this, e, "Could not read bytes from stream.");
			return -1;
		}
	}
	
}
