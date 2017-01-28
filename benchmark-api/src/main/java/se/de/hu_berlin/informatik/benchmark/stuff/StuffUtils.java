package se.de.hu_berlin.informatik.benchmark.stuff;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import se.de.hu_berlin.informatik.utils.fileoperations.ListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.fileoperations.csv.CSVUtils;

public class StuffUtils {
	
	public static <T> List<T>[] drawFromArrayIntoNBuckets(final T[] array, int n) {
		return drawFromArrayIntoNBuckets(array, n, null);
	}

	public static <T> List<T>[] drawFromArrayIntoNBuckets(final T[] array, int n, Long seed) {
		Random ran;
		if (seed == null) {
			ran = new Random();
		} else {
			ran = new Random(seed);
		}
		
		Set<Integer> usedPositions = new HashSet<>();
		
		@SuppressWarnings("unchecked")
		List<T>[] buckets = (List<T>[]) new List<?>[n];
		int numberOfElments = array.length;
		
		for (int i = 0; i < n; ++i) {
			buckets[i] = new ArrayList<>((numberOfElments / n) + 1);
		}
		
		int bucketCounter = 0;
		for (int count = 0; count < numberOfElments; ++count) {
			//get an element from the array and put it in the current bucket
			T element = null;
			while (element == null) {
				int nextNumber = ran.nextInt(numberOfElments);
				if (!usedPositions.contains(nextNumber)) {
					element = array[nextNumber];
					usedPositions.add(nextNumber);
				}
			}
			buckets[bucketCounter].add(element);
			++bucketCounter;
			if (bucketCounter >= n) {
				bucketCounter = 0;
			}
		}
		
		return buckets;
	}

	public static <T> void generateFileFromBuckets(List<T>[] buckets, Function<T,String> StringSupplier, Path outputFile) {
		List<String[]> csvLines = new ArrayList<>();
		int maxLength = 0;
		for (List<T> bucket : buckets) {
			maxLength = maxLength < bucket.size() ? bucket.size() : maxLength;
		}
		
		for (List<T> bucket : buckets) {
			String[] bucketArray = new String[maxLength];
			Iterator<T> bucketIterator = bucket.iterator();
			int i = 0;
			while (bucketIterator.hasNext()) {
				bucketArray[i] = StringSupplier.apply(bucketIterator.next());
				++i;
			}
			while (i < maxLength) {
				bucketArray[i] = "";
				++i;
			}
			csvLines.add(bucketArray);
		}
		
		new ListToFileWriterModule<List<String>>(outputFile, true)
		.submit(CSVUtils.toMirroredCsv(csvLines));
	}
	
	public static <T> List<T>[] getBucketsFromFile(Path bucketFile, Function<String, T> elementSupplier) {
		List<String[]> lines = CSVUtils.readCSVFileToListOfStringArrays(bucketFile, true);
		@SuppressWarnings("unchecked")
		List<T>[] buckets = (List<T>[]) new List<?>[lines.size()];
		
		Iterator<String[]> lineIterator = lines.iterator();
		int i = 0;
		while(lineIterator.hasNext()) {
			String[] line = lineIterator.next();
			List<T> bucket = new ArrayList<>();
			for (String element : line) {
				bucket.add(elementSupplier.apply(element));
			}
			buckets[i] = bucket;
			++i;
		}
		
		return buckets;
	}
	
}
