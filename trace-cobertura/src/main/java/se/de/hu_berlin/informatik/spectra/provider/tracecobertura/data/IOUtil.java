package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Helper class with useful I/O operations.
 *
 * @author Grzegorz Lukasik
 */
public abstract class IOUtil {

	/**
	 * Copies bytes from input stream into the output stream.  Stops
	 * when the input stream read method returns -1.  Does not close
	 * the streams.
	 * 
	 * @param in
	 * input
	 * @param out
	 * output
	 *
	 * @throws IOException          If either passed stream will throw IOException.
	 * @throws NullPointerException If either passed stream is null.
	 */
	public static void copyStream(InputStream in, OutputStream out)
			throws IOException {
		// NullPointerException is explicity thrown to guarantee expected behaviour
		if (in == null || out == null)
			throw new NullPointerException();

		int el;
		byte[] buffer = new byte[1 << 15];
		while ((el = in.read(buffer)) != -1) {
			out.write(buffer, 0, el);
		}
	}

	/**
	 * Returns an array that contains values read from the
	 * given input stream.
	 * 
	 * @param in
	 * input
	 * @return
	 * byte array
	 *
	 * @throws NullPointerException If null stream is passed.
	 * @throws IOException          If either passed stream will throw IOException.
	 */
	public static byte[] createByteArrayFromInputStream(InputStream in)
			throws IOException {
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		copyStream(in, byteArray);
		return byteArray.toByteArray();
	}

	/**
	 * Moves a file from one location to other.
	 *
	 * @param sourceFile
	 * input
	 * @param destinationFile
	 * output
	 * 
	 * @throws IOException          If IO exception occur during moving.
	 * @throws NullPointerException If either passed file is null.
	 */
	public static void moveFile(File sourceFile, File destinationFile)
			throws IOException {
		if (destinationFile.exists()) {
			destinationFile.delete();
		}

		// Move file using File method if possible
		boolean succesfulMove = sourceFile.renameTo(destinationFile);
		if (succesfulMove)
			return;

		// Copy file from source to destination
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(sourceFile);
			out = new FileOutputStream(destinationFile);
			copyStream(in, out);
		} finally {
			in = closeInputStream(in);
			out = closeOutputStream(out);
		}

		// Remove source file
		sourceFile.delete();
	}

	/**
	 * Closes an input stream.
	 *
	 * @param in The stream to close.
	 *
	 * @return null unless an exception was thrown while closing, else
	 *         returns the stream
	 */
	public static InputStream closeInputStream(InputStream in) {
		if (in != null) {
			try {
				in.close();
				in = null;
			} catch (IOException e) {
				System.err.println("Cobertura: Error closing input stream.");
				e.printStackTrace();
			}
		}
		return in;
	}

	/**
	 * Closes an output stream.
	 *
	 * @param out The stream to close.
	 *
	 * @return null unless an exception was thrown while closing, else
	 *         returns the stream.
	 */
	public static OutputStream closeOutputStream(OutputStream out) {
		if (out != null) {
			try {
				out.close();
				out = null;
			} catch (IOException e) {
				System.err.println("Cobertura: Error closing output stream.");
				e.printStackTrace();
			}
		}
		return out;
	}

	public static PrintWriter getPrintWriter(File file)
			throws UnsupportedEncodingException, FileNotFoundException {
		Writer osWriter = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(file), StandardCharsets.UTF_8), 16384);
		PrintWriter pw = new PrintWriter(osWriter, false);
		return pw;
	}

}
