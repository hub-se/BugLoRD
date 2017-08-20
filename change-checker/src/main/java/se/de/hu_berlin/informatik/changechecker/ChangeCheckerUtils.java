package se.de.hu_berlin.informatik.changechecker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.SignificanceLevel;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.Delta.TYPE;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper.ModificationType;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.files.processors.FileToStringListReader;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;

public class ChangeCheckerUtils {

	/**
	 * Compares the given files and returns a list of changes with information
	 * about all discovered changes, including line numbers, types and
	 * significance level.
	 * @param left
	 * the first file
	 * @param right
	 * the second file
	 * @return a list of changes, or null if an error occurred
	 */
	public static List<ChangeWrapper> checkForChanges(File left, File right) {

		List<Delta<String>> deltas = getDeltas(left, right);
		if (deltas == null) {
			return null;
		}

		CompilationUnit compilationUnit = getCompilationUnitFromFile(left);
		if (compilationUnit == null) {
			return null;
		}
		String className = compilationUnit.getPackage().getName().getFullyQualifiedName() + "."
				+ FileUtils.getFileNameWithoutExtension(left.getName());

		List<ChangeWrapper> lines = getChangeWrappers(left, right, className, deltas);
		if (lines == null) {
			return null;
		}

		List<Integer> allDeltaPositions = getLinesFromDeltas(deltas);
		updateChangeWrappersWithDeltas(
				className, new FileToStringListReader().submit(left.toPath()).getResult(), lines, allDeltaPositions);

		return lines;
	}

	private static List<ChangeWrapper> getChangeWrappers(File left, File right, String className,
			List<Delta<String>> deltas) {
		List<SourceCodeChange> changes = getChangesWithChangeDistiller(left, right);

		CompilationUnit compilationUnit = getCompilationUnitFromFile(left);
		if (compilationUnit == null) {
			return null;
		}

		CompilationUnit compilationUnitRevised = getCompilationUnitFromFile(right);
		if (compilationUnitRevised == null) {
			return null;
		}

		Map<Integer, TYPE> fineDeltas = new HashMap<>();
		Map<Integer, Integer> linesInserted = new HashMap<>();
		for (Delta<String> delta : deltas) {
			int pos = delta.getOriginal().getPosition() + 1; // == lineNumber-1
																// + 1
			Log.out(ChangeChecker.class, "delta: " + (pos) + 
					(delta.getType() == TYPE.INSERT ? 
							", insert + " + delta.getRevised().getLines().size() : 
								(delta.getType() == TYPE.CHANGE ?
										", change + " + (delta.getRevised().getLines().size() - delta.getOriginal().getLines().size()) :
											", delete - " + (delta.getOriginal().getLines().size()))));
//			 Log.out(ChangeChecker.class, "" + pos + ", " + delta.getRevised().getLines().size());
//			 for (String line : delta.getRevised().getLines()) {
//				 Log.out(ChangeChecker.class, "\t" + line);
//			 }
			
			// we insert lines AFTER the specified position!
			if (delta.getType() == TYPE.INSERT) {
				linesInserted.put(pos - 1, delta.getRevised().getLines().size());
				fineDeltas.put(pos, TYPE.INSERT);
			} else if (delta.getType() == TYPE.CHANGE) {
				//TODO: create an insert delta the line after the "real" changes?
				linesInserted.put(pos + delta.getOriginal().getLines().size() - 1, 
						delta.getRevised().getLines().size() - delta.getOriginal().getLines().size());
				for (int i = 0; i < delta.getOriginal().getLines().size(); ++i) {
					fineDeltas.put(pos + i, TYPE.CHANGE);
				}
			} else { //TODO: deletes?
				linesInserted.put(pos - 1, -delta.getOriginal().getLines().size());
				for (int i = 0; i < delta.getOriginal().getLines().size(); ++i) {
					fineDeltas.put(pos + i, TYPE.DELETE);
				}
			}
		}

		Map<Integer, Integer> sortedInsertions = Misc.sortByKey(linesInserted);
		
//		List<SourceCodeEntity> inserts = new ArrayList<>();
//		List<SourceCodeEntity> updates = new ArrayList<>();
//		for (SourceCodeChange change : changes) {
//			if (change instanceof Insert) {
//				inserts.add(change.getChangedEntity());
//			} if (change instanceof Move) {
//				inserts.add(((Move) change).getNewEntity());
//				updates.add(change.getChangedEntity());
//			} else {
//				updates.add(change.getChangedEntity());
//			}
//		}

		List<String> leftLines = new FileToStringListReader().submit(left.toPath()).getResult();
		List<String> rightLines = new FileToStringListReader().submit(right.toPath()).getResult();

		List<ChangeWrapper> lines = new ArrayList<>();
		if (changes != null) {
			for (SourceCodeChange change : changes) {

				// see Javadocs for more information
				SourceCodeEntity parent = change.getParentEntity();
				SourceCodeEntity entity = change.getChangedEntity();

				ChangeType type = change.getChangeType();

				int parentStart = compilationUnit.getLineNumber(parent.getStartPosition());
				int parentEnd = compilationUnit.getLineNumber(parent.getEndPosition());
				int start = compilationUnit.getLineNumber(entity.getStartPosition());
				int end = compilationUnit.getLineNumber(entity.getEndPosition());

				ModificationType modification_type;

				if (change instanceof Insert) {
					// Insert insert = (Insert) change;

					// what if parent was inserted?
					if (compilationUnit.getLineNumber(parentStart) < 0) {
						parentStart = compilationUnitRevised.getLineNumber(parent.getStartPosition());
						parentEnd = compilationUnitRevised.getLineNumber(parent.getEndPosition());
						int linesInsertedBefore = computeInsertedLineCount(sortedInsertions, parentStart); //TODO check for correctness
						parentStart -= linesInsertedBefore;
						parentEnd -= linesInsertedBefore;
					}

					start = compilationUnitRevised.getLineNumber(entity.getStartPosition());
//					end = compilationUnitRevised.getLineNumber(entity.getEndPosition());
					int linesInsertedBefore = computeInsertedLineCount(sortedInsertions, start);
					start -= linesInsertedBefore;
					// start is now the line BEFORE the inserted element, if it is inserted after the line
					// OR the actual line of the element, if it was only a partly insertion
					// inserted elements should only correspond to one line in
					// the original source code
					end = start;
					
					Log.out(ChangeChecker.class, "%d, %d, lines inserted: %d", start, end, linesInsertedBefore);
					
					switch (type) {
//					case ADDITIONAL_CLASS:
//						break;
//					case ADDITIONAL_FUNCTIONALITY:
//						break;
//					case ADDITIONAL_OBJECT_STATE:
//						break;
//					case ALTERNATIVE_PART_DELETE:
//						break;
//					case ALTERNATIVE_PART_INSERT:
//						break;
//					case RETURN_TYPE_INSERT:
//						start = compilationUnitRevised.getLineNumber(entity.getStartPosition()-1);
//						start -= linesInsertedBefore;
//						end = start;
//						break;
//					case PARAMETER_INSERT:
//						// simply take the parent range, since there exist problems when inserting a
//						// parameter that spans over multiple lines (only the line with the var name is given...)
//						start = parentStart;
//						end = parentEnd;
//						break;
//					case PARENT_CLASS_INSERT:
//						break;
//					case PARENT_INTERFACE_INSERT:
//						break;
//					case STATEMENT_INSERT:
//						if (start != compilationUnitRevised.getLineNumber(entity.getStartPosition() - 1)) {
//							start = getNearestActualLineBeforePos(leftLines, start, true);
//						}
//						end = getNearestActualLineAfterPos(leftLines, start, true);
//						break;
					default:
						break;
					}
					
					// check if the whole line or only a part was inserted by comparing the two files
					if (checkIfWholeLineChanged(leftLines, rightLines, start, linesInsertedBefore)) {
						// skip comments and whitespaces before the line
						start = getNearestActualLineBeforePos(leftLines, start+1, true)-1;
					}
//					end = start;
					// if inside of a comment or at a blank line, get the nearest actual line
					end = getNearestActualLineAfterPos(leftLines, start+1, true);

					modification_type = ModificationType.INSERT;

				} else if (change instanceof Move) {
					Move move = (Move) change;

					{
						modification_type = getModificationType(type, ModificationType.DELETE);

						lines.add(
								new ChangeWrapper(className, entity,
										parentStart, parentEnd, start, end, entity.getType(), change.getChangeType(),
										change.getSignificanceLevel(), modification_type));
					}

					parent = move.getNewParentEntity();
					entity = move.getNewEntity();

					parentStart = compilationUnitRevised.getLineNumber(parent.getStartPosition());
					parentEnd = compilationUnitRevised.getLineNumber(parent.getEndPosition());
					int linesInsertedBefore = computeInsertedLineCount(sortedInsertions, parentStart);
					parentStart -= linesInsertedBefore;
					parentEnd -= linesInsertedBefore;

					start = compilationUnitRevised.getLineNumber(entity.getStartPosition());
					end = compilationUnitRevised.getLineNumber(entity.getEndPosition());
					linesInsertedBefore = computeInsertedLineCount(sortedInsertions, start);
					start -= linesInsertedBefore;
					// inserted elements should only correspond to one line in
					// the original source code?
					end = start;
					// end -= linesInsertedBefore;
					
					start = getNearestActualLineBeforePos(leftLines, start, true);
					end = getNearestActualLineAfterPos(leftLines, start, true);

					modification_type = ModificationType.INSERT;

				} else if (change instanceof Update) {
					// Update update = (Update) change;

					// what if parent was inserted?
					if (compilationUnit.getLineNumber(parentStart) < 0) {
						parentStart = compilationUnitRevised.getLineNumber(parent.getStartPosition());
						parentEnd = compilationUnitRevised.getLineNumber(parent.getEndPosition());
						int linesInsertedBefore = computeInsertedLineCount(sortedInsertions, parentStart);
						parentStart -= linesInsertedBefore;
						parentEnd -= linesInsertedBefore;
					}

					modification_type = ModificationType.CHANGE;

				} else if (change instanceof Delete) {
					// Delete delete = (Delete) change;
					modification_type = ModificationType.DELETE;

				} else {
					modification_type = ModificationType.NO_SEMANTIC_CHANGE;
				}

				modification_type = getModificationType(type, modification_type);

				lines.add(
						new ChangeWrapper(className, entity, parentStart, parentEnd, start, end, entity.getType(),
								change.getChangeType(), change.getSignificanceLevel(), modification_type));
			}
		}

		return lines;
	}

	private static boolean checkIfWholeLineChanged(List<String> leftLines, List<String> rightLines, int start,
			int linesInsertedBefore) {
		String leftString = Misc.replaceWhitespacesInString(leftLines.get(start-1), "");
		String rightString = Misc.replaceWhitespacesInString(rightLines.get(start-1+linesInsertedBefore), "");
		Log.out(null, leftString);
		Log.out(null, rightString);
		// check if the beginning of the line changed
		// => the insertion affects the whole line (probably...)
		if (leftString.length() > 0 && rightString.length() > 0
				&& leftString.charAt(0) != rightString.charAt(0)) {
			return true;
		} else {
			return false;
		}
	}

	private static int computeInsertedLineCount(Map<Integer, Integer> sortedInsertions, int lineInRevisedVersion) {
		int linesInsertedBefore = 0;
		for (Entry<Integer, Integer> entry : sortedInsertions.entrySet()) {
			if (entry.getKey() + linesInsertedBefore + entry.getValue() <= lineInRevisedVersion) {
				// mapped line no. + already inserted lines + lines inserted after mapped line <= specified line no.
				linesInsertedBefore += entry.getValue();
			} else if (entry.getKey() + linesInsertedBefore < lineInRevisedVersion) {
				// mapped line no. + already inserted lines < specified line no.
				linesInsertedBefore += lineInRevisedVersion - (entry.getKey() + linesInsertedBefore);
			} else {
				// mapped line no. + already inserted lines >= specified line no.
				break;
			}
		}
		return linesInsertedBefore;
	}

	private static CompilationUnit getCompilationUnitFromFile(File left) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);

		// Parse the class as a compilation unit.
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		try {
			// give your java source here as char array
			parser.setSource(FileUtils.readFile2CharArray(left.toString()));
		} catch (IOException e) {
			Log.err(ChangeChecker.class, e, "Could not parse source file '%s'.", left);
			return null;
		}
		parser.setResolveBindings(true);

		// Return the compiled class as a compilation unit
		CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
		return compilationUnit;
	}

	// private static boolean parentIsAChangeOrInsideOfChange(SourceCodeEntity
	// parent, List<SourceCodeChange> changes) {
	// int start = parent.getStartPosition();
	// int end = parent.getEndPosition();
	// for (SourceCodeChange change : changes) {
	// if (change.getChangedEntity().getStartPosition() <= start
	// && change.getChangedEntity().getEndPosition() >= end) {
	// return true;
	// }
	// }
	// return false;
	// }

	

	private static List<SourceCodeChange> getChangesWithChangeDistiller(File left, File right) {
		FileDistiller distiller = ChangeDistiller.createFileDistiller(Language.JAVA);
		try {
			distiller.extractClassifiedSourceCodeChanges(left, right);
		} catch (Exception e) {
			/*
			 * An exception most likely indicates a bug in ChangeDistiller.
			 * Please file a bug report at
			 * https://bitbucket.org/sealuzh/tools-changedistiller/issues and
			 * attach the full stack trace along with the two files that you
			 * tried to distill.
			 */
			Log.err(ChangeChecker.class, "Error while change distilling. " + e.getMessage());
			return null;
		}

		List<SourceCodeChange> changes = distiller.getSourceCodeChanges();
		return changes;
	}

	private static List<Delta<String>> getDeltas(File left, File right) {
		List<String> leftList = new FileToStringListReader().submit(left.toPath()).getResult();
		List<String> rightList = new FileToStringListReader().submit(right.toPath()).getResult();

		Patch<String> patch = DiffUtils.diff(leftList, rightList);

		List<Delta<String>> deltas = patch.getDeltas();
		if (deltas == null) {
			Log.err(ChangeChecker.class, "Error getting deltas.");
			return null;
		}

		return deltas;
	}

	private static List<Integer> getLinesFromDeltas(List<Delta<String>> deltas) {
		List<Integer> allDeltaPositions = new ArrayList<>();
		for (Delta<String> delta : deltas) {
			// int pos = delta.getType() == TYPE.INSERT ?
			// delta.getRevised().getPosition() + 1 :
			// delta.getOriginal().getPosition() + 1; //== lineNumber-1 + 1
			int pos = delta.getOriginal().getPosition() + 1; // == lineNumber-1
																// + 1
			// Log.out(ChangeChecker.class, "" + pos);

			int lineCount;
			if (delta.getType() == TYPE.INSERT) {
				// line count would be 0, since the lines don't exist in the
				// original
				// and can't use the revised chunk, since we can't count all the
				// inserted lines...
				allDeltaPositions.add(pos - 1);
			} else {
				lineCount = delta.getOriginal().getLines().size();
				for (int i = 0; i < lineCount; ++i) {
					allDeltaPositions.add(pos + i);
				}
				if (delta.getRevised().getLines().size() > delta.getOriginal().getLines().size()) {
					// add a delta for the inserted lines
					allDeltaPositions.add(pos + lineCount);
				}
			}
		}
		return allDeltaPositions;
	}

	private static void updateChangeWrappersWithDeltas(String className, List<String> lines,
			List<ChangeWrapper> changes, List<Integer> deltas) {
		for (ChangeWrapper change : changes) {

			List<Integer> matchingDeltas = getIncludedDeltas(lines, changes, deltas, change);

			if (matchingDeltas.isEmpty()) {
				continue;
			}

			change.setDeltas(matchingDeltas);
		}

		if (!deltas.isEmpty()) {
			List<Integer> matchingDeltas = new ArrayList<>();
			for (int pos : deltas) {
				//only skip whitespaces here
				matchingDeltas.add(getNearestActualLineAfterPos(lines, pos, false));
			}
			changes.add(
					new ChangeWrapper(className, null, 1, lines.size(), 1, lines.size(), matchingDeltas, null, null,
							SignificanceLevel.NONE, ModificationType.NO_SEMANTIC_CHANGE));
		}
	}

	private static List<Integer> getIncludedDeltas(List<String> lines, List<ChangeWrapper> changes,
			List<Integer> deltas, ChangeWrapper change) {
		List<Integer> matchingDeltas = new ArrayList<>();
		for (Iterator<Integer> iterator = deltas.iterator(); iterator.hasNext();) {
			int lineNo = iterator.next();
			if (change.getStart() <= lineNo && lineNo <= change.getEnd()) {
				if (positionIsOnLowestLevel(changes, change, lineNo)) {
					// skip whitespaces and comment sections
					matchingDeltas.add(getNearestActualLineAfterPos(lines, lineNo, true));

					iterator.remove();
				}
			}
		}
		return matchingDeltas;
	}
	
	private static boolean positionIsOnLowestLevel(List<ChangeWrapper> changes, ChangeWrapper currentChange, int lineNo) {
		SourceCodeEntity currentEntity = currentChange.getEntity();
		for (ChangeWrapper change : changes) {
			if (change == currentChange) {
				continue;
			}
			// is the line in the change?
			if (change.getStart() <= lineNo && lineNo <= change.getEnd()) {
				SourceCodeEntity entity = change.getEntity();
				if ((currentEntity.getStartPosition() < entity.getStartPosition() 
						&& entity.getEndPosition() <= currentEntity.getEndPosition())
						|| (currentEntity.getStartPosition() <= entity.getStartPosition() 
								&& entity.getEndPosition() < currentEntity.getEndPosition())) {
					return false;
				}
			}
//			// is the line in the change?
//			if (change.getStart() <= lineNo && lineNo <= change.getEnd()) {
//				if ((currentChange.getStart() < change.getStart() && change.getEnd() <= currentChange.getEnd())
//						|| (currentChange.getStart() <= change.getStart()
//								&& change.getEnd() < currentChange.getEnd())) {
//					return false;
//				}
//			}
		}
		return true;
	}

	private static int getNearestActualLineAfterPos(List<String> lines, 
			int pos, boolean skipComments) {
//		Log.out(ChangeChecker.class, "original pos: %d", pos);
		//skip empty lines and comments...
		boolean foundActualLine = false;
		boolean isInsideOfCommentBlock = false;
		int realPos = pos;
		if (isInsideOfCommentBlock(lines, realPos)) {
			isInsideOfCommentBlock = true;
		}
		while (!foundActualLine) {
			String currentLine = lines.get(realPos - 1);
			if (realPos > lines.size()) {
				break;
			} else if (currentLine.matches("[\\s]*")) { // skip whitespaces
				++realPos;
			} else if (skipComments) {
				if (currentLine.matches("^[\\s]*//.*")) { // skip line comments
					++realPos;
				} else if (isInsideOfCommentBlock || // inside of block comment
						currentLine.matches("^[\\s]*/\\*.*")) { // comment block start at beginning of line
					isInsideOfCommentBlock = true;
					if (currentLine.matches(".*\\*/[\\s]*$")) { // comment block ended at ending of line
						isInsideOfCommentBlock = false;
						// skipping this will be an error if we have a construct like "/*...*/ code /*...*/",
						// so we test if there is a pattern like "...*/.../*..." in the line.
						// this is still not perfect... 
						if (currentLine.matches(".*\\*/.*[^\\s]+.*/\\*.*")) {
							foundActualLine = true;
						} else { // skip the line
							++realPos;
						}
					} else if (currentLine.matches(".*\\*/.*$")) { // comment block ended with some other elements left
						foundActualLine = true;
					} else { // comment did not end yet...
						++realPos;
					}
				} else {
					foundActualLine = true;
				}
			} else {
				foundActualLine = true;
			}
		}
		if (foundActualLine) {
			return realPos;
		} else {
			return pos;
		}
	}
	
	private static int getNearestActualLineBeforePos(List<String> lines, 
			int pos, boolean skipComments) {
//		Log.out(ChangeChecker.class, "original pos: %d", pos);
		//skip empty lines and comments...
		boolean foundActualLine = false;
		boolean isInsideOfCommentBlock = false;
		int realPos = pos - 1;
		if (isInsideOfCommentBlock(lines, realPos)) {
			isInsideOfCommentBlock = true;
		}
		while (!foundActualLine) {
			if (realPos <= 1) {
				break;
			}
			String currentLine = lines.get(realPos - 1);
			if (currentLine.matches("[\\s]*")) { // skip whitespaces
				--realPos;
			} else if (skipComments) {
				if (currentLine.matches("^[\\s]*/\\*.*")) { // comment block start at beginning of line
					isInsideOfCommentBlock = false;
					--realPos;
				} else if (currentLine.matches("^.*/\\*.*")) { // comment block starts somewhere else in the line
					// skipping this will be an error if we have a construct like "/*...*/ /*...*/",
					// so we test if there is a pattern like "...*/.../*..." in the line.
					// this is still not perfect... 
					if (currentLine.matches(".*\\*/[\\s]*/\\*.*")) { // skip the line
						isInsideOfCommentBlock = true;
						--realPos;
					} else { //there are elements in between the comments
						foundActualLine = true;
					}
				} else if (isInsideOfCommentBlock) { // still inside of comment block...
					--realPos;
				} else if (currentLine.matches(".*\\*/[\\s]*$")) { // comment block ended at ending of line
					isInsideOfCommentBlock = true;
					--realPos;
				} else { // (possible: comment block ended), some other elements left
					foundActualLine = true;
				}
			} else {
				foundActualLine = true;
			}
		}
		return realPos + 1;
	}

	private static boolean isInsideOfCommentBlock(List<String> lines, int realPos) {
		boolean insideOfBlockComment = false;
		boolean insideOfString = false;
		boolean readSlash = false;
		boolean readBackSlash = false;
		boolean readStar = false;
		int counter = 0;
		for (String line : lines) {
			++counter;
			if (counter == realPos) {
				if (insideOfBlockComment) {
					return true;
				} else {
					return false;
				}
//				// maybe inside a line comment?
//				if (line.matches("^[\\s]*//.*")) {
//					return true;
//				}
			}
			for (int i = 0; i < line.length(); ++i) {
				if (insideOfBlockComment) {
					if (line.charAt(i) == '*') {
						readStar = true;
					} else if (readStar && line.charAt(i) == '/') {
						readStar = false;
						insideOfBlockComment = false;
					} else {
						readStar = false;
					}
				} else if (insideOfString) {
					if (line.charAt(i) == '\\') {
						readBackSlash = true;
					} else if (line.charAt(i) == '"' && !readBackSlash) {
						insideOfString = false;
						readBackSlash = false;
					} else {
						readBackSlash = false;
					}
				} else if (readSlash) {
					if (line.charAt(i) == '/') {
						// line comment
						readSlash = false;
						break;
					} else if (line.charAt(i) == '"') {
						insideOfString = true;
						readSlash = false;
					} else if (line.charAt(i) == '*') {
						insideOfBlockComment = true;
						readSlash = false;
					} else {
						readSlash = false;
					}
				} else if (line.charAt(i) == '/') {
					readSlash = true;
				} else if (line.charAt(i) == '"') {
					insideOfString = true;
				}
			}
		}
		return false;
	}

	private static ModificationType getModificationType(ChangeType type, ModificationType oldType) {
		ModificationType modification_type = oldType;
		switch (type) {
		// case ADDITIONAL_CLASS:
		// case ADDITIONAL_FUNCTIONALITY:
		// case ADDITIONAL_OBJECT_STATE:
		// case ALTERNATIVE_PART_INSERT:
		// case PARAMETER_INSERT:
		// case PARENT_CLASS_INSERT:
		// case PARENT_INTERFACE_INSERT:
		// case RETURN_TYPE_INSERT:
		// case STATEMENT_INSERT:
		// modification_type = ModificationType.INSERT;
		// break;
		//
		// case ALTERNATIVE_PART_DELETE:
		// case PARAMETER_DELETE:
		// case PARENT_CLASS_DELETE:
		// case PARENT_INTERFACE_DELETE:
		// case REMOVED_CLASS:
		// case REMOVED_FUNCTIONALITY:
		// case REMOVED_OBJECT_STATE:
		// case RETURN_TYPE_DELETE:
		// case STATEMENT_DELETE:
		// modification_type = ModificationType.DELETE;
		// break;
		//
		// case UNCLASSIFIED_CHANGE:
		// case ATTRIBUTE_TYPE_CHANGE:
		// case CONDITION_EXPRESSION_CHANGE:
		// case DECREASING_ACCESSIBILITY_CHANGE:
		// case INCREASING_ACCESSIBILITY_CHANGE:
		// case PARAMETER_ORDERING_CHANGE:
		// case PARAMETER_TYPE_CHANGE:
		// case PARENT_CLASS_CHANGE:
		// case PARENT_INTERFACE_CHANGE:
		// case RETURN_TYPE_CHANGE:
		// case STATEMENT_ORDERING_CHANGE:
		// case STATEMENT_PARENT_CHANGE:
		// case STATEMENT_UPDATE:
		// modification_type = ModificationType.CHANGE;
		// break;

		case COMMENT_INSERT:
		case DOC_INSERT:
		case COMMENT_DELETE:
		case DOC_DELETE:
		case REMOVING_ATTRIBUTE_MODIFIABILITY:
		case REMOVING_CLASS_DERIVABILITY:
		case REMOVING_METHOD_OVERRIDABILITY:
//		case METHOD_RENAMING:
//		case CLASS_RENAMING:
//		case ATTRIBUTE_RENAMING:
//		case PARAMETER_RENAMING:
		case ADDING_ATTRIBUTE_MODIFIABILITY:
		case ADDING_CLASS_DERIVABILITY:
		case ADDING_METHOD_OVERRIDABILITY:
		case COMMENT_MOVE:
		case COMMENT_UPDATE:
		case DOC_UPDATE:
			modification_type = ModificationType.NO_SEMANTIC_CHANGE;
			break;
		default:
			// modification_type = ModificationType.NO_SEMANTIC_CHANGE;
			break;
		}
		return modification_type;
	}
	
}
