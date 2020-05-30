package se.de.hu_berlin.informatik.spectra.util;

import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.FromString;

import java.util.Map;

public interface Indexable<T> extends FromString<T> {

    public T getOriginalFromIndexedIdentifier(String identifier, Map<Integer, String> map) throws IllegalArgumentException;

    public String getIndexedIdentifier(T original, Map<String, Integer> map);

}
