/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.util;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedMap;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Special processer to directly populate repetition marker maps from a compressed byte array.
 *
 * @author Simon Heiden
 */
public class CompressedByteArrayToRepetitionMarkerMapListProcessor extends AbstractProcessor<String, List<BufferedMap<int[]>>> {

    public static final int DELIMITER = 0;
    // same buffer that is used in zip utils
    private static final int BUFFER_SIZE = 4096;
    private byte[] buffer = new byte[BUFFER_SIZE];

    private byte usedBits;
    private int arrayPos;

    private boolean containsZero;
    private ZipFileWrapper zipFileWrapper;
    private Supplier<BufferedMap<int[]>> supplier;

    public CompressedByteArrayToRepetitionMarkerMapListProcessor(ZipFileWrapper zipFileWrapper,
                                                                 boolean containsZero, Supplier<BufferedMap<int[]>> supplier) {
        super();
        this.containsZero = containsZero;
        this.zipFileWrapper = zipFileWrapper;
        this.supplier = supplier;
    }

    /* (non-Javadoc)
     * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
     */
    @Override
    public List<BufferedMap<int[]>> processItem(String fileName) {

        InputStream inputStream = null;
        try (ZipFile zipFile = new ZipFile(zipFileWrapper.getzipFilePath().toFile())) {
            ZipEntry entry = zipFile.getEntry(fileName);
            inputStream = zipFile.getInputStream(entry);

            int len = getNextBytesFromInputStream(inputStream);
            readHeader(len);

            boolean atTotalEnd = false;

            byte currentByte = 0;
            int currentInt = 0;

            int[] currentMarker = new int[2];
            int currentIndex = 0;

            byte remainingBits = 0;
            byte bitsLeft = 0;
            BufferedMap<int[]> currentMap = null;
            List<BufferedMap<int[]>> result = new ArrayList<>();

            int intCounter = 0;

            boolean lastElementWasDelimiter = false;

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
                    if (currentMap != null) {
                        result.add(currentMap);
                    }
                    currentMap = null;
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
                        currentInt = (currentInt << bitsLeft) | (currentByte & 0xFF) >>> (8 - bitsLeft);
                        currentByte = (byte) (currentByte << bitsLeft);
                        remainingBits -= bitsLeft;
                        bitsLeft = 0;
                    }
                }

                if (currentInt == DELIMITER && lastElementWasDelimiter) {
                    atTotalEnd = true;
                    break;
                } else {
                    if (currentInt == DELIMITER) {
                        lastElementWasDelimiter = true;
                        //reset the counter (start of new sequence)
                        intCounter = 0;
                    } else {
                        lastElementWasDelimiter = false;
                        if (currentMap == null) {
                            currentMap = supplier.get();
                        }

                        // add/process the next integer
                        // assumes that the sequence of storing key/value pairs is:
                        // [repetition length, repetition count], start index
                        if (currentIndex < 2) {
                            currentMarker[currentIndex++] = (containsZero ? currentInt - 1 : currentInt);
                        } else {
//							System.out.println((containsZero ? currentInt-1 : currentInt) + " -> [" + currentMarker[0] + ", " + currentMarker[1] + "]");
                            currentMap.put(containsZero ? currentInt - 1 : currentInt, currentMarker);
                            currentMarker = new int[2];
                            currentIndex = 0;
                        }

                        ++intCounter;
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
		} catch (IOException e) {
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
        // header should be 1 byte:
        // | number of bits used for one element (1 byte) |
        if (len < 1) {
            Log.abort(this, "Could not read header from input stream.");
        }
        usedBits = buffer[0];

        arrayPos = 1;
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
