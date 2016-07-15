package se.de.hu_berlin.informatik.stardust.util;

import java.nio.file.Path;
import java.util.List;

import se.de.hu_berlin.informatik.stardust.traces.IMutableTrace;
import se.de.hu_berlin.informatik.stardust.traces.INode;
import se.de.hu_berlin.informatik.stardust.traces.ISpectra;
import se.de.hu_berlin.informatik.stardust.traces.ITrace;
import se.de.hu_berlin.informatik.stardust.traces.Spectra;
import se.de.hu_berlin.informatik.utils.compression.ByteArrayToCompressedByteArrayModule;
import se.de.hu_berlin.informatik.utils.compression.CompressedByteArrayToByteArrayModule;
import se.de.hu_berlin.informatik.utils.compression.ziputils.AddByteArrayToZipFileModule;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ReadZipFileModule;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;

/**
 * Helper class to save and load spectra objects.
 * 
 * @author Simon
 *
 */
public class SpectraUtils {

	private static final String IDENTIFIER_DELIMITER = "\t";
	
	/**
	 * Saves a Spectra object to hard drive.
	 * @param spectra
	 * the Spectra object to save
	 * @param output
	 * the output path to the zip file to be created
	 * @param compress
	 * whether or not to use an additional compression procedure apart from zipping
	 */
	public static <T extends CharSequence> void saveSpectraToZipFile(ISpectra<T> spectra, Path output, boolean compress) {
		StringBuffer buffer = new StringBuffer();
		byte[] involvement = new byte[spectra.getTraces().size()*(spectra.getNodes().size()+1)];
		
		if (involvement.length == 0) {
			Misc.err("Can not save empty spectra...");
			return;
		}	
		
		List<INode<T>> nodes = spectra.getNodes();
		
		//store the identifiers (order is important)
		for (INode<T> node : nodes) {
			//add the identifier to the list of identifiers (has to be stored with the table)
			buffer.append(node.getIdentifier() + IDENTIFIER_DELIMITER);
		}
		if (buffer.length() > 0) {
			buffer.deleteCharAt(buffer.length()-1);
		}
		
		int byteCounter = -1;
		
		//iterate through the traces
		for (ITrace<T> trace : spectra.getTraces()) {
			//the first element is a flag that marks successful traces with '1'
			if (trace.isSuccessful()) {
				involvement[++byteCounter] = 1;
			} else {
				involvement[++byteCounter] = 0;
			}
			//the following elements are flags that mark the trace's involvement with nodes with '1'
			for (INode<T> node : nodes) {
				if (trace.isInvolved(node)) {
					involvement[++byteCounter] = 1;
				} else {
					involvement[++byteCounter] = 0;
				}
			}
		}
		
		byte[] status = { 0 };
		if (compress) {
			involvement = new ByteArrayToCompressedByteArrayModule(1, nodes.size()+1).submit(involvement).getResultFromCollectedItems();
			status[0] = 1;
		}
		
		//now, we have a list of identifiers and the involvement table
		//so add them to the output zip file
		new AddByteArrayToZipFileModule(output, true)
		.submit(buffer.toString().getBytes()) //0.bin
		.submit(involvement) //1.bin
		.submit(status); //2.bin
	}
	
	/**
	 * Loads a Spectra object from a zip file. This method is deprecated...
	 * @param zipFilePath
	 * the path to the zip file containing the Spectra object
	 * @param isCompressed
	 * whether the Spectra has been compressed
	 * @return
	 * the loaded Spectra object
	 */
	public static ISpectra<String> loadSpectraFromZipFile(Path zipFilePath, boolean isCompressed) {
		ZipFileWrapper zip = new ReadZipFileModule().submit(zipFilePath).getResult();
		
		//parse the file containing the identifiers
		String[] identifiers = new String(zip.checkedGet(0)).split(IDENTIFIER_DELIMITER);
		
		//parse the file containing the involvement table
		byte[] involvementTable = zip.checkedGet(1);
		
		//for backwards functionality... TODO: throw this whole method away...
		if (isCompressed) {
			involvementTable = new CompressedByteArrayToByteArrayModule().submit(involvementTable).getResult();
		}
		
		//create a new spectra
		Spectra<String> spectra = new Spectra<>();
		
		int tablePosition = -1;
		//iterate over the involvement table and fill the spectra object with traces
		while (tablePosition+1 < involvementTable.length) {
			//the first element is always the 'successful' flag
			IMutableTrace<String> trace = spectra.addTrace(involvementTable[++tablePosition] == 1);

			for (int i = 0; i < identifiers.length; ++i) {
				trace.setInvolvement(identifiers[i], involvementTable[++tablePosition] == 1);
			}
		}
		
		return spectra;
	}
	
	
	/**
	 * Loads a Spectra object from a zip file.
	 * @param zipFilePath
	 * the path to the zip file containing the Spectra object
	 * @return
	 * the loaded Spectra object
	 */
	public static ISpectra<String> loadSpectraFromZipFile(Path zipFilePath) {
		ZipFileWrapper zip = new ReadZipFileModule().submit(zipFilePath).getResult();
		
		//parse the file containing the identifiers
		String[] identifiers = new String(zip.checkedGet(0)).split(IDENTIFIER_DELIMITER);
		
		//parse the file containing the involvement table
		byte[] involvementTable = zip.checkedGet(1);
		
		//parse the status byte (0 -> uncompressed, 1 -> compressed)
		byte[] status = zip.checkedGet(2);

		//check if we have a compressed byte array at hand
		if (status[0] == 1) {
			involvementTable = new CompressedByteArrayToByteArrayModule().submit(involvementTable).getResult();
		}
		
		//create a new spectra
		Spectra<String> spectra = new Spectra<>();
		
		int tablePosition = -1;
		//iterate over the involvement table and fill the spectra object with traces
		while (tablePosition+1 < involvementTable.length) {
			//the first element is always the 'successful' flag
			IMutableTrace<String> trace = spectra.addTrace(involvementTable[++tablePosition] == 1);

			for (int i = 0; i < identifiers.length; ++i) {
				trace.setInvolvement(identifiers[i], involvementTable[++tablePosition] == 1);
			}
		}
		
		return spectra;
	}
}
