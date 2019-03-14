package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;


import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.TouchPointListener;

/**
 * Analyzes given method and applies information about all found important places into {@link #classmap}.
 *
 * @author piotr.tabor@gmail.com
 */
public class BuildClassMapTouchPointListener implements TouchPointListener {
	private final ClassMap classmap;

	public BuildClassMapTouchPointListener(ClassMap classMap) {
		this.classmap = classMap;
	}

	public void beforeJump(int eventId, Label label, int currentLine,
			MethodVisitor nextMethodVisitor) {
		classmap.registerNewJump(eventId, currentLine, label);
	}

	public void beforeLabel(int eventId, Label label, int currentLine,
			MethodVisitor mv) {
		classmap.registerNewLabel(eventId, currentLine, label);
	}

	public void afterLineNumber(int eventId, Label label, int currentLine,
			MethodVisitor nextMethodVisitor, String methodName,
			String methodSignature) {
		classmap.registerLineNumber(eventId, currentLine, label, methodName,
				methodSignature);
	}

	public void beforeSwitch(int eventId, Label def, Label[] labels,
			int currentLine, MethodVisitor mv, String conditionType) {
		classmap.registerSwitch(eventId, currentLine, def, labels,
				conditionType);
	}

	public void ignoreLine(int eventId, int currentLine) {
		classmap.unregisterLine(eventId, currentLine);
	}

	// --------------- Not interesting events for analysis ---------------------------
	public void afterJump(int eventId, Label label, int currentLine,
			MethodVisitor nextMethodVisitor) {
	}

	public void afterLabel(int eventId, Label label, int currentLine,
			MethodVisitor mv) {
	}

	public void afterMethodStart(MethodVisitor nextMethodVisitor) {
	}

}
