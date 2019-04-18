package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.TouchPointListener;

import java.util.Map;

/**
 * Inject code provided by {@link #codeProvider} into the instrumented method's body. Injects code that
 * is responsible for incrementing counters. Mapping of places into counters is provided by {@link #classMap}.
 *
 * @author piotr.tabor@gmail.com
 */
public class InjectCodeTouchPointListener implements TouchPointListener {
	private final static Logger logger = LoggerFactory
			.getLogger(InjectCodeTouchPointListener.class);
	/**
	 * Component that is responsible for generation of the snippets
	 */
	private final CodeProvider codeProvider;

	/**
	 * Source of mapping from place (eventId) into counterId that is incremented if the place is touched
	 */
	private final ClassMap classMap;

	private int lastJumpIdVariableIndex;

	public InjectCodeTouchPointListener(ClassMap classMap,
			CodeProvider codeProvider) {
		this.classMap = classMap;
		this.codeProvider = codeProvider;
	}

	/*
	 * Before jump, we will store into 'internal variable' the counterId of a 'false' branch of the JUMP
	 */
	public void beforeJump(int eventId, Label label, int currentLine,
			MethodVisitor nextMethodVisitor) {
		logger.debug("Before jump:" + currentLine + "(" + eventId + ") to: "
				+ label);
		Integer jumpFalseCounterId = classMap.getCounterIdForFalseBranchJump(eventId);
		if (jumpFalseCounterId != null) {
			logger.debug("jump false counter:" + jumpFalseCounterId + ", " + currentLine + "(" + eventId + ") to: "
					+ label);
			codeProvider.generateCodeThatSetsJumpCounterIdVariable(
					nextMethodVisitor, jumpFalseCounterId,
					lastJumpIdVariableIndex);
		}
	}

	/*
	 * After jump, we will increment counterId for the 'true' branch of the JUMP. (successful jump to given label)
	 * Then we set internal variable to ZERO to avoid fake interpretation (another incrementation)
	 * 
	 * If we would not reset the jump counter, it would probably increment the true branch counter again,
	 * where otherwise it would have increased the stored false branch counter... (see: before jump)
	 */
	public void afterJump(int eventId, Label label, int currentLine,
			MethodVisitor nextMethodVisitor) {
		logger.debug("After jump:" + currentLine + "(" + eventId + ") to: "
				+ label);
		Integer jumpTrueCounterId = classMap.getCounterIdForTrueBranchJump(eventId);
		if (jumpTrueCounterId != null) {
			logger.debug("jump true counter:" + jumpTrueCounterId + ", " + currentLine + "(" + eventId + ") to: "
					+ label);
			codeProvider.generateCodeThatIncrementsCoberturaCounterAfterJump(
					nextMethodVisitor, jumpTrueCounterId, classMap
							.getClassName(), classMap.getClassId());
			codeProvider.generateCodeThatZeroJumpCounterIdVariable(
					nextMethodVisitor, lastJumpIdVariableIndex);
		}
	}

	/*
	 * Before switch we set the internal variable to a special counterId connected with the switch. This counterId is not
	 * connected with any branch of the switch.
	 */
	public void beforeSwitch(int eventId, Label def, Label[] labels,
			int currentLine, MethodVisitor mv, String conditionType) {
		Integer switchCounterId = classMap.getCounterIdForSwitch(eventId);
		if (switchCounterId != null) {
			codeProvider.generateCodeThatSetsJumpCounterIdVariable(mv,
					switchCounterId, lastJumpIdVariableIndex);
		}
	}

	/*
	 * <p>If the label is JUMP destination, we will increment 
	 * the counter stored inside the 'internal variable'. 
	 * This way we are incrementing the 'false' branch of the condition. </p>
	 * 
	 * <p>If the label is SWITCH destination, we check all switch 
	 * instructions that have targets in the label we generate
	 * code that checks if the 'internal variable' is equal to id 
	 * of considered switch and if so increments counterId connected to the switch.
	 */
	public void afterLabel(int eventId, Label label, int currentLine,
			MethodVisitor mv) {
//		logger.debug("Looking for jumps going to event(" + eventId + "):"
//				+ label + " ");
		if (classMap.isJumpDestinationLabel(eventId)) {
			codeProvider
					.generateCodeThatIncrementsCoberturaCounterFromInternalVariable(
							mv, lastJumpIdVariableIndex, 
							classMap.getClassName(), classMap.getClassId());
		}

		Map<Integer, Integer> branchTouchPoints = classMap
				.getBranchLabelDescriptorsForLabelEvent(eventId);
		if (branchTouchPoints != null) {
			/*map of counterId of a switch into counterId of the branch of the switch*/
			for (Map.Entry<Integer, Integer> entry : branchTouchPoints
					.entrySet()) {
				codeProvider
						.generateCodeThatIncrementsCoberturaCounterIfVariableEqualsAndCleanVariable(
								mv, entry.getKey(), entry.getValue(),
								lastJumpIdVariableIndex, 
								classMap.getClassName(), classMap.getClassId());
			}
		}

		if (classMap.isJumpDestinationLabel(eventId)) {
			codeProvider.generateCodeThatZeroJumpCounterIdVariable(mv,
					lastJumpIdVariableIndex);
		}
	}

	/*
	 * After every 'linenumber' instruction we increments counter connected with the line number.
	 */
	public void afterLineNumber(int eventId, Label label, int currentLine,
			MethodVisitor nextMethodVisitor, String methodName,
			String methodSignature) {
		Integer lineCounterId = classMap.getCounterIdForLineEventId(eventId);
		// TODO when can this be null?
		if (lineCounterId != null) {
			codeProvider.generateCodeThatIncrementsCoberturaCounter(
					nextMethodVisitor, lineCounterId, 
					classMap.getClassName(), classMap.getClassId());
		}
	}

	/*
	 * At the start of every method we initiates the 'internal variable' with zero.
	 */
	public void afterMethodStart(MethodVisitor nextMethodVisitor) {
		// TODO: setup counter? answer: probably not...!
		codeProvider.generateCodeThatZeroJumpCounterIdVariable(
				nextMethodVisitor, lastJumpIdVariableIndex);
	}

	// ------------------- ignored events -------------------------------	

	public void beforeLabel(int eventId, Label label, int currentLine,
			MethodVisitor mv) {
	}

	public void ignoreLine(int eventId, int currentLine) {
	}

	// ------------------- getters and setters --------------------------	

	/*
	 * Index of 'internal variable'. Should be detected by {@link ShiftVariableMethodAdapter#calculateFirstStackVariable(int, String)}.
	 */
	public void setLastJumpIdVariableIndex(int lastJumpIdVariableIndex) {
		this.lastJumpIdVariableIndex = lastJumpIdVariableIndex;
	}

}

