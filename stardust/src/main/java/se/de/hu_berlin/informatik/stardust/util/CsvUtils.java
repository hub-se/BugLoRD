/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package se.de.hu_berlin.informatik.stardust.util;

import org.junit.Assert;

/**
 * Contains utility methods to better cope with writing CSV files.
 *
 * @author Fabian Keller 'dev@fabian-keller.de'
 */
public final class CsvUtils {

    /**
     * used CSV delimiter
     */
    public static final String CSV_DELIMITER = ";";
    /**
     * used quote character to quote fields
     */
    public static final String CSV_QUOTE = "\"";

    /**
     * Hide constructor, as this is a utility class
     */
    private CsvUtils() {
        super();
    }

    /**
     * Turns a string array into a CSV line.
     *
     * @param parts the columns of the CSV line
     * @return the combined CSV string without trailing newline
     */
    public static String toCsvLine(final String[] parts) {
        final StringBuffer line = new StringBuffer();
        for (final String part : parts) {
            if (line.length() > 0) {
                line.append(CSV_DELIMITER);
            }
            line.append(part.replaceAll(CSV_QUOTE, CSV_QUOTE + CSV_QUOTE));
        }
        return line.toString();
    }
    
    /**
     * Turns a byte array into a CSV line.
     * @param dataArray 
     * the columns of the CSV line
     * @param columnCount
     * the number of columns
     * @return 
     * the combined CSV string to write to a file
     */
    public static String toCsv(final byte[] dataArray, int columnCount) {
        final StringBuffer line = new StringBuffer();
        Assert.assertTrue(dataArray.length % columnCount == 0);
        
        for (int i = 0; i < dataArray.length; ++i) {
            line.append(dataArray[i]);
            if ((i+1) % columnCount == 0) {
            	line.append("\n");            	
            } else {
            	line.append(CSV_DELIMITER);
            }
        }
        return line.toString();
    }
    
    /**
     * Turns a byte array into a CSV line.
     *
     * @param parts the columns of the CSV line
     * @return the combined CSV string without trailing newline
     */
    public static String toCsvLine(final int[] parts) {
        final StringBuffer line = new StringBuffer();
        for (int part : parts) {
            if (line.length() > 0) {
                line.append(CSV_DELIMITER);
            }
            line.append(part);
        }
        return line.toString();
    }
}
