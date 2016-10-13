/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

import se.de.hu_berlin.informatik.utils.tm.modules.stringprocessor.StringProcessor;

/**
 * Takes lines with format "path:line# 'a|c|d'" and builds a title
 * for the plot from them.
 * 
 * @author Simon Heiden
 */
public class TitleBuilder implements StringProcessor<String> {

	private String prefix;
	private int appends = 0;
	private int changes = 0;
	private int deletes = 0;
	private int totalLines = 0;
	
	/**
	 * Creates a new {@link TitleBuilder} object with the given parameters.
	 * @param prefix
	 * should be either "ranked" or "unranked"
	 */
	public TitleBuilder(String prefix) {
		this.prefix = prefix;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.modules.stringprocessor.IStringProcessor#process(java.lang.String)
	 */
	public boolean process(String line) {
		++totalLines;
		switch (line.charAt(line.length()-1)) {
		case 'a':
			++appends;
			break;
		case 'c':
			++changes;
			break;
		case 'd':
			++deletes;
			break;
		default:
			break;
		}
		return true;
	}

	/**
	 * @return 
	 * the built title
	 */
	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.modules.stringprocessor.IStringProcessor#getResult()
	 */
	public String getResult() {
		return prefix + " modified lines: " + totalLines + " (a:" + appends + ", c:" + changes + ", d:" + deletes + ")";
	}

}
