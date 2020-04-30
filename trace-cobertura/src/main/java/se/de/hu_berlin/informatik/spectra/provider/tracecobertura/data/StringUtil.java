package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data;

import java.text.NumberFormat;

/**
 * Abstract, not to be instantiated utility class for String functions.
 *
 * @author Jeremy Thomerson
 */
public abstract class StringUtil {

    /**
     * <p>
     * Replaces all instances of "replace" with "with" from the "original"
     * string.
     * </p>
     *
     * <p>
     * NOTE: it is known that a similar function is included in jdk 1.4 as replaceAll(),
     * but is written here so as to allow backward compatibility to users using SDK's
     * prior to 1.4
     * </p>
     *
     * @param original The original string to do replacement on.
     * @param replace  The string to replace.
     * @param with     The string to replace "replace" with.
     * @return The replaced string.
     */
    public static String replaceAll(String original, String replace, String with) {
        if (original == null) {
            return null;
        }

        final int len = replace.length();
        StringBuilder sb = new StringBuilder(original.length());
        int start = 0;
        int found = -1;

        while ((found = original.indexOf(replace, start)) != -1) {
            sb.append(original, start, found);
            sb.append(with);
            start = found + len;
        }

        sb.append(original.substring(start));
        return sb.toString();
    }

    /**
     * Takes a double and turns it into a percent string.
     * Ex.  0.5 turns into 50%
     *
     * @param value input value
     * @return corresponding percent string
     */
    public static String getPercentValue(double value) {
        //moved from HTMLReport.getPercentValue()
        value = Math.floor(value * 100) / 100; //to represent 199 covered lines from 200 as 99% covered, not 100 %
        return NumberFormat.getPercentInstance().format(value);
    }

}
