package se.de.hu_berlin.informatik.stardust.provider.tracecobertura.coveragedata;


import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * This class represents an archive within an archive.
 *
 * @author John Lewis
 */
public class Archive {

	private byte[] bytes;
	private boolean modified;
	private CoberturaFile file;

	/**
	 * Create an object that holds a buffer to an archive that is within a parent archive.
	 *
	 * @param file  The parent archive on the hard drive that holds the child archive.
	 * @param bytes The contents of the child archive.
	 */
	Archive(CoberturaFile file, byte[] bytes) {
		this.bytes = bytes;
		this.file = file;
	}

	/**
	 * Return an input stream for the contents of this archive (the child).
	 *
	 * @return An InputStream for the contents.
	 */
	InputStream getInputStream() {
		return new ByteArrayInputStream(this.bytes);
	}

	/**
	 * Set this archive's bytes after they have been modified via instrumentation.
	 *
	 * @param bytes The new contents of the archive (instrumented).
	 */
	void setModifiedBytes(byte[] bytes) {
		this.bytes = bytes;
		this.modified = true;
	}

	/**
	 * Return true if this archive has been modified (instrumented).
	 *
	 * @return true if modified.
	 */
	boolean isModified() {
		return modified;
	}

	/**
	 * Return the contents of this archive.
	 *
	 * @return A byte array with the contents of this archive.
	 */
	byte[] getBytes() {
		return this.bytes;
	}

	/**
	 * Returns the parent archive that contains this archive.
	 *
	 * @return A CoberturaFile representing the parent archive.
	 */
	CoberturaFile getCoberturaFile() {
		return this.file;
	}
}

