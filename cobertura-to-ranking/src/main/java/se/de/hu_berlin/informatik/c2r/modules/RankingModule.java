/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import se.de.hu_berlin.informatik.stardust.localizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.stardust.localizer.Ranking;
import se.de.hu_berlin.informatik.stardust.traces.ISpectra;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AModule;

/**
 * Computes rankings for all coverage data stored in the 
 * input spectra and saves multiple ranking files for
 * various SBFL formulae to the hard drive.
 * 
 * @author Simon Heiden
 */
public class RankingModule extends AModule<ISpectra<String>, Object> {

	private String outputdir;
	private List<Class<?>> localizers;
	
	/**
	 * @param outputdir
	 * path to the output directory
	 * @param localizers
	 * a list of Cobertura localizer identifiers
	 */
	public RankingModule(String outputdir, String... localizers) {
		super(true);
		this.outputdir = outputdir;
		this.localizers = new ArrayList<>(localizers.length);
		
		//check if the given localizers can be found and abort in the negative case
		for (int i = 0; i < localizers.length; ++i) {
			String className = localizers[i].substring(0, 1).toUpperCase() + localizers[i].substring(1);
			 try {
				this.localizers.add(Class.forName("se.de.hu_berlin.informatik.stardust.localizer.sbfl." + className));
			} catch (ClassNotFoundException e) {
				Misc.abort(this, "Could not find class '%s'.", "se.de.hu_berlin.informatik.stardust.localizer.sbfl." + className);
			}
		}
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public Object processItem(ISpectra<String> spectra) {
		
		for (Class<?> localizer : localizers) {
			String className = localizer.getSimpleName();
			System.out.println("...calculating " + className + " ranking.");
			try {
				generateRanking(spectra, 
						(IFaultLocalizer<String>) localizer.getConstructor().newInstance(), 
						className.toLowerCase());
			} catch (InstantiationException e) {
				Misc.err(this, e, "Could not instantiate class '%s'.", className);
			} catch (IllegalAccessException e) {
				Misc.err(this, e, "Illegal access of class '%s'.", className);
			} catch (ClassCastException e) {
				Misc.err(this, e, "Class '%s' is not of right type.", className);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * Generates and saves a specific SBFL ranking. 
	 * @param spectra
	 * Cobertura line spectra
	 * @param localizer
	 * provides specific SBFL formulae
	 * @param subfolder
	 * name of a subfolder to be used
	 */
	private void generateRanking(ISpectra<String> spectra, final IFaultLocalizer<String> localizer, final String subfolder) {
		try {
			final Ranking<String> ranking = localizer.localize(spectra);
			Paths.get(outputdir + File.separator + subfolder).toFile().mkdirs();
			ranking.save(outputdir + File.separator + subfolder + File.separator + "ranking.rnk");
		} catch (Exception e) {
			Misc.err(this, e, "Could not save ranking in '%s'.", outputdir + File.separator + subfolder + File.separator + "ranking.rnk");
		}
	}

}
