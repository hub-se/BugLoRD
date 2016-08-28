/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import se.de.hu_berlin.informatik.stardust.util.SpectraUtils;
import se.de.hu_berlin.informatik.utils.fileoperations.SearchForFilesOrDirsModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

/**
 * Stores the generated spectra for future usage.
 * 
 * @author SimHigh
 */
public class GenerateSpectraArchive {
	
	/**
	 * @param args
	 * command line arguments
	 */
	public static void main(String[] args) {

		Prop prop = new Prop();
		prop.switchToArchiveMode();
		
		File archiveMainDir = Paths.get(prop.mainDir).toFile();
		
		if (!archiveMainDir.exists()) {
			Log.abort(GenerateSpectraArchive.class, 
					"Archive main directory doesn't exist: '" + prop.mainDir + "'.");
		}
			
		/* #====================================================================================
		 * # load the compressed spectra files and store them in a separate archive folder for
		 * # further usage in the future
		 * #==================================================================================== */
		List<Path> spectraZipFiles = 
				new SearchForFilesOrDirsModule(false, true, "**/ranking/spectraCompressed.zip", false, true)
				.submit(Paths.get(prop.mainDir))
				.getResult();
		
		String spectraArchiveDir = prop.spectraArchiveDir;
		
		//TODO this is for now. In the future, we may just move the specific files...
		for (Path file : spectraZipFiles) {
			Log.out(GenerateSpectraArchive.class, "Processing file '%s'.", file);
			int count = file.getNameCount();
			String filename = file.getName(count-4).toString() + "-" + file.getName(count-3).toString() + ".zip";
			SpectraUtils.saveSpectraToZipFile(
					SpectraUtils.loadSpectraFromZipFile(file),
					Paths.get(spectraArchiveDir, filename),
					true);
		}
		
		Log.out(GenerateSpectraArchive.class, "All done!");
		
	}
	
}
