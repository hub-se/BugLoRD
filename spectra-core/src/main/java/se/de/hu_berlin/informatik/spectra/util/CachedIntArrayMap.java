package se.de.hu_berlin.informatik.spectra.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public class CachedIntArrayMap extends CachedMap<int[]> {

	public CachedIntArrayMap(Path outputZipFile, int cacheSize, String id, boolean deleteAtShutdown) {
		super(outputZipFile, cacheSize, id, deleteAtShutdown);
	}
	
	private static final byte MAGIC_1BYTE      = (byte) 255;
    private static final byte MAGIC_2BYTES     = (byte) 254;
    private static final byte MAGIC_3BYTES     = (byte) 253;
    private static final byte MAGIC_4BYTES     = (byte) 252;  
    private static final int  LOWER_8_BITS     = 0xff;
    private static final int  HIGHER_8_BITS    = 0xff000000;
    private static final int  HIGHER_16_BITS   = 0xffff0000;
    private static final int  HIGHER_24_BITS   = 0xffffff00;

    private void writeInt(final OutputStream out, final int value) throws IOException {
        if ((value & HIGHER_24_BITS) == 0) {
            if (value >= 252)
                out.write(MAGIC_1BYTE);
        } else if ((value & HIGHER_16_BITS) == 0) {
            out.write(MAGIC_2BYTES);
            out.write(value >>> 8);
        } else if ((value & HIGHER_8_BITS) == 0) {
            out.write(MAGIC_3BYTES);
            out.write(value >>> 16);
            out.write(value >>> 8);
        } else {
            out.write(MAGIC_4BYTES);
            out.write(value >>> 24);
            out.write(value >>> 16);
            out.write(value >>> 8);
        }
        out.write(value);
    }
    
    private int readInt(final InputStream in) throws IOException {
        int b0, b1, b2;
        int b3 = in.read();
        switch (b3) {
        case -1:
            throw new EOFException();
        case MAGIC_1BYTE & LOWER_8_BITS:
            b0 = in.read();
            if (b0 < 0)
                throw new EOFException();
            return (b0 & LOWER_8_BITS);
        case MAGIC_2BYTES & LOWER_8_BITS:
            b1 = in.read();
            b0 = in.read();
            if ((b0 | b1) < 0)
                throw new EOFException();
            return ((b1 & LOWER_8_BITS) << 8) 
            		| (b0 & LOWER_8_BITS);
        case MAGIC_3BYTES & LOWER_8_BITS:
            b2 = in.read();
            b1 = in.read();
            b0 = in.read();
            if ((b0 | b1 | b2) < 0)
                throw new EOFException();
            return  ((b2 & LOWER_8_BITS) << 16) 
            		| ((b1 & LOWER_8_BITS) << 8) 
            		| (b0 & LOWER_8_BITS);
        case MAGIC_4BYTES & LOWER_8_BITS:
            b3 = in.read();
            b2 = in.read();
            b1 = in.read();
            b0 = in.read();
            if ((b0 | b1 | b2 | b3) < 0)
                throw new EOFException();
            return ((b3 & LOWER_8_BITS) << 24) 
            		| ((b2 & LOWER_8_BITS) << 16) 
            		| ((b1 & LOWER_8_BITS) << 8) 
            		| (b0 & LOWER_8_BITS);
        default:
            return (b3 & LOWER_8_BITS);
        }
    }

	@Override
	public byte[] toByteArray(int[] array) {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		try (ObjectOutputStream objOut = new ObjectOutputStream(byteOut)) {

			writeInt(objOut, array.length);
			for (int value : array) {
				writeInt(objOut, value);
			}
			objOut.close();
			
			return byteOut.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public int[] fromByteArray(byte[] array) {
		ByteArrayInputStream byteIn = new ByteArrayInputStream(array);
		try (ObjectInputStream objIn = new ObjectInputStream(byteIn)) {
			int length = readInt(objIn);
			int[] result = new int[length];
			
			for (int i = 0; i < length; ++i) {
				result[i] = readInt(objIn);
			}
			
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
