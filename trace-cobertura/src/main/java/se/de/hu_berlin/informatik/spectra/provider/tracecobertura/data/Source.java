package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data;


import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Source {
	private InputStream is;

	//streamOrigin is either a File or a ZipFile
	private Object streamOrigin;

	private static Logger LOGGER = LoggerFactory.getLogger(Source.class);

	public Source(InputStream is, Object streamOrigin) {
		this.is = is;
		this.streamOrigin = streamOrigin;
	}

	public InputStream getInputStream() {
		return is;
	}

	/**
	 * Close the source input stream and the archive if it came from one.
	 * 
	 * This will not throw anything.   Any throwable is caught and a warning is logged.
	 */
	public void close() {
		try {
			is.close();
		} catch (Throwable t) {
			LOGGER.warn("Failure closing input stream for " + getOriginDesc(),
					t);
		}

		if (streamOrigin instanceof ZipFile) {
			try {
				((ZipFile) streamOrigin).close();
			} catch (Throwable t) {
				LOGGER.warn("Failure closing " + getOriginDesc(), t);
			}
		}
	}

	public String getOriginDesc() {
		String ret = "";

		if (streamOrigin instanceof File) {
			ret = "file " + ((File) streamOrigin).getAbsolutePath();
		} else {
			ret = "archive " + ((ZipFile) streamOrigin).getName();
		}
		return ret;
	}
}
