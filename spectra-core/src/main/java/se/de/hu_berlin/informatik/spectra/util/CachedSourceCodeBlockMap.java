package se.de.hu_berlin.informatik.spectra.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;

public class CachedSourceCodeBlockMap extends CachedMap<SourceCodeBlock> {
    
    public CachedSourceCodeBlockMap(Path outputZipFile, int cacheSize, int entryFileSize, String id, boolean deleteAtShutdown) {
        super(outputZipFile, cacheSize, entryFileSize, id, deleteAtShutdown);
    }
    
    public CachedSourceCodeBlockMap(Path outputZipFile, int cacheSize, String id, boolean deleteAtShutdown) {
        super(outputZipFile, cacheSize, id, deleteAtShutdown);
    }
    
    public CachedSourceCodeBlockMap(Path outputZipFile, String id, boolean deleteAtShutdown) {
        super(outputZipFile, id, deleteAtShutdown);
    }

    @Override
    public byte[] toByteArray(SourceCodeBlock block) {
    	return block.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public SourceCodeBlock fromByteArray(byte[] array) {
        return SourceCodeBlock.getNewBlockFromString(new String(array, StandardCharsets.UTF_8));
    }

}
