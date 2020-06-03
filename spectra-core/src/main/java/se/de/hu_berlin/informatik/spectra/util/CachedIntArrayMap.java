package se.de.hu_berlin.informatik.spectra.util;

import java.io.*;
import java.nio.file.Path;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.DataInput;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.output.DataOutput;

public class CachedIntArrayMap extends CachedMap<int[]> {

    public CachedIntArrayMap(Path outputZipFile, int cacheSize, int entryFileSize, String id, boolean deleteAtShutdown) {
        super(outputZipFile, cacheSize, entryFileSize, id, deleteAtShutdown);
    }
    
    public CachedIntArrayMap(Path outputZipFile, int cacheSize, String id, boolean deleteAtShutdown) {
        super(outputZipFile, cacheSize, id, deleteAtShutdown);
    }
    
    public CachedIntArrayMap(Path outputZipFile, String id, boolean deleteAtShutdown) {
        super(outputZipFile, id, deleteAtShutdown);
    }

    @Override
    public byte[] toByteArray(int[] array) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try (ObjectOutputStream objOut = new ObjectOutputStream(byteOut)) {

            DataOutput.writeInt(objOut, array.length);
            for (int value : array) {
            	DataOutput.writeInt(objOut, value);
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
            int length = DataInput.readInt(objIn);
            int[] result = new int[length];

            for (int i = 0; i < length; ++i) {
                result[i] = DataInput.readInt(objIn);
            }

            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
