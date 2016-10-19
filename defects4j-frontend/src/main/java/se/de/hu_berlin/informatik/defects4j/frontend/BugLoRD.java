package se.de.hu_berlin.informatik.defects4j.frontend;

import java.io.File;
import java.util.Properties;

import se.de.hu_berlin.informatik.utils.properties.PropertyLoader;
import se.de.hu_berlin.informatik.utils.properties.PropertyTemplate;

public final class BugLoRD {
	
	public static enum BugLoRDProperties implements PropertyTemplate {
		SRILM_DIR("srilm_dir", "/path/to/../SRILM-1.7.1/bin",
				"path to SRILM (not necessarily needed to run experiments with Defects4J)"),
		KEN_LM_DIR("kenlm_dir", "/path/to/../kenlm/bin",
				"path to the (altered) kenLM version (needed for the computation of LM rankings)"),

		RANKING_PERCENTAGES("ranking_percentages", "0 2 4 6 8 10 12 14 16 18 20 22 24 26 28 30 "
				+ "32 34 36 38 40 42 44 46 48 50 52 54 56 58 60 62 64 66 68 70 72 74 76 78 80 82 84 86 88 90 92 94 96 98 100",
				"specify percentages with which to combine the SBFL and LM rankings (0 means 0% SBFL ranking, 100 means 100%)",
				"ranking_percentages=0 5 10 15 20 25 30 35 40 45 50 55 60 65 70 75 80 85 90 95 100",
				"ranking_percentages=0 1 2 3 4 5 10 50 90 95 96 97 98 99 100"),

		LOCALIZERS("localizers", "Op2",
				"you can specify different SBFL ranking metrics at this point. This ensures that",
				"only rankings for these metrics will get generated or plotted when running the",
				"respecting tools. This way, you don't have to give every tool this list of",
				"ranking metrics as an option.",
				"localizers=Op2 GP13 Tarantula Ochiai Jaccard RussellRao",
				"localizers=Op2 GP13 Tarantula Ochiai Jaccard Ample Anderberg ArithmeticMean "
				+ "Cohen Dice Euclid Fleiss GeometricMean Goodman Hamann Hamming HarmonicMean "
				+ "Kulczynski1 Kulczynski2 M1 M2 Ochiai2 Overlap RogersTanimoto Rogot1 Rogot2 "
				+ "RussellRao Scott SimpleMatching Sokal SorensenDice Wong1 Wong2 Wong3 Zoltar"),
				
		GLOBAL_LM_BINARY("global_lm_binary", "/path/to/../some_language_model.kenlm.binary",
				"specify the path to the LM binary that shall be used");

		final private String[] descriptionLines;
		final private String identifier;
		final private String placeHolder;
		
		private String value = null;
		
		BugLoRDProperties(String identifier, String placeHolder, String... descriptionLines) {
			this.identifier = identifier;
			this.placeHolder = placeHolder;
			this.descriptionLines = descriptionLines;
		}

		@Override public String getPropertyIdentifier() { return identifier; }
		@Override public String getPlaceHolder() { return placeHolder; }
		@Override public String[] getHelpfulDescription() { return descriptionLines; }

		@Override public void setPropertyValue(String value) { this.value = value; }
		@Override public String getValue() { return value; }
	}

	public final static String SEP = File.separator;
	
	public final static String PROP_FILE_NAME = "BugLoRDProperties.ini";
	
	private static Properties props = PropertyLoader.loadProperties(new File(BugLoRD.PROP_FILE_NAME), BugLoRDProperties.class);
	
	//suppress default constructor (class should not be instantiated)
	private BugLoRD() {
		throw new AssertionError();
	}
	
	public static Properties getProperties() {
		return props;
	}
	
	public static String getValueOf(BugLoRDProperties property) {
		return property.getValue();
	}
	
	public static String getSRILMMakeBatchCountsExecutable() {
		return getValueOf(BugLoRDProperties.SRILM_DIR) + SEP + "make-batch-counts";
	}
	
	public static String getSRILMMergeBatchCountsExecutable() {
		return getValueOf(BugLoRDProperties.SRILM_DIR) + SEP + "merge-batch-counts";
	}
	
	public static String getSRILMMakeBigLMExecutable() {
		return getValueOf(BugLoRDProperties.SRILM_DIR) + SEP + "make-big-lm";
	}
	
	public static String getKenLMBinaryExecutable() {
		return getValueOf(BugLoRDProperties.KEN_LM_DIR) + SEP + "build_binary";
	}
	
	public static String getKenLMQueryExecutable() {
		return getValueOf(BugLoRDProperties.KEN_LM_DIR) + SEP + "query";
	}
	
}
