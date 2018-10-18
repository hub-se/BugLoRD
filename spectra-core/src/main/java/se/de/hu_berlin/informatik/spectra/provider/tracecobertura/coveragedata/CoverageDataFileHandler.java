package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import net.sourceforge.cobertura.CoverageIgnore;
import net.sourceforge.cobertura.util.ConfigurationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * This contains methods used for reading and writing the
 * "cobertura.ser" file.
 */
@CoverageIgnore
public abstract class CoverageDataFileHandler {
	private static final Logger logger = LoggerFactory
			.getLogger(CoverageDataFileHandler.class);
	private static File defaultFile = null;

	public static File getDefaultDataFile() {
		// return cached defaultFile
		if (defaultFile != null) {
			return defaultFile;
		}

		// load and cache datafile configuration
		ConfigurationUtil config = new ConfigurationUtil();
		defaultFile = new File(config.getDatafile());

		return defaultFile;
	}

	public static TraceProjectData loadCoverageData(File dataFile) {
		InputStream is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(dataFile), 16384);
			return loadCoverageData(is);
		} catch (IOException e) {
			logger.error("Cobertura: Error reading file "
					+ dataFile.getAbsolutePath() + ": "
					+ e.getLocalizedMessage(), e);
			return null;
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					logger.error("Cobertura: Error closing file "
							+ dataFile.getAbsolutePath() + ": "
							+ e.getLocalizedMessage(), e);
				}
		}
	}
	private static TraceProjectData loadCoverageData(InputStream dataFile)
			throws IOException {
		ObjectInputStream objects = null;

		try {
			objects = new ObjectInputStream(dataFile);
			TraceProjectData projectData = (TraceProjectData) objects.readObject();
			logger.info("Cobertura: Loaded information on "
					+ projectData.getNumberOfClasses() + " classes.");

			return projectData;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Cobertura: Error reading from object stream.", e);
			return null;
		} finally {
			if (objects != null) {
				try {
					objects.close();
				} catch (IOException e) {
					logger.error("Cobertura: Error closing object stream.");
				}
			}
		}
	}

	public static void saveCoverageData(TraceProjectData projectData, File dataFile) {
		FileOutputStream os = null;

		try {
			File dataDir = dataFile.getParentFile();
			if ((dataDir != null) && !dataDir.exists()) {
				dataDir.mkdirs();
			}
			os = new FileOutputStream(dataFile);
			saveCoverageData(projectData, os);
		} catch (IOException e) {
			logger.error("Cobertura: Error writing file "
					+ dataFile.getAbsolutePath(), e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					logger.error("Cobertura: Error closing file "
							+ dataFile.getAbsolutePath(), e);
				}
			}
		}
	}

	private static void saveCoverageData(TraceProjectData projectData,
			OutputStream dataFile) {
		ObjectOutputStream objects = null;

		try {
			objects = new ObjectOutputStream(dataFile);
			objects.writeObject(projectData);
			logger.info("Cobertura: Saved information on "
					+ projectData.getNumberOfClasses() + " classes.");
		} catch (IOException e) {
			logger.error("Cobertura: Error writing to object stream.", e);
		} finally {
			if (objects != null) {
				try {
					objects.close();
				} catch (IOException e) {
					logger.error("Cobertura: Error closing object stream.", e);
				}
			}
		}
	}
}

