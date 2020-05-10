package se.de.hu_berlin.informatik.spectra.util;

import java.nio.file.Path;

public class CachedSequiturMap extends CachedMap<byte[]> {

    public CachedSequiturMap(Path outputZipFile, int cacheSize, String id, boolean deleteAtShutdown) {
        super(outputZipFile, cacheSize, id, deleteAtShutdown);
    }

    @Override
    public byte[] toByteArray(byte[] sequence) {
    	return sequence;
    }

    @Override
    public byte[] fromByteArray(byte[] array) {
        return array;
    }

}
