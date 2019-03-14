package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Detects duplicates in a single analyzed method of ASM code. Applies found information about
 * duplicates into {@link #duplicatesCollector} structure.
 * 
 * See {@link DetectDuplicatedCodeClassVisitor} about reasons for the class.
 *
 * @author piotr.tabor@gmail.com
 */
public class DetectDuplicatedCodeMethodVisitor
		extends
			ContextMethodAwareMethodAdapter {
//	private final Logger logger = LoggerFactory
//			.getLogger(DetectDuplicatedCodeClassVisitor.class);
	/**
	 * Map of (lineNumber -> (list of pairs: lineId,{@link CodeFootstamp} for the lineId))).
	 */
	private final Map<Integer, List<LineIdWithCodeFootstamp>> line2label2codefootstamp = new LinkedHashMap<>();

	/**
	 * Map of (lineNumber -> (duplicate lineId -> origin lineId)). This structure is filled with a new reasults at the {@link #visitEnd()} method.
	 */
	private final Map<Integer, Map<Integer, Integer>> duplicatesCollector;

	/**
	 * Represents pair of lineId and {@link CodeFootstamp} connected with that {@link #lineId}
	 */
	private static class LineIdWithCodeFootstamp {
		private final Integer lineId;
		private final CodeFootstamp footstamp;

		public LineIdWithCodeFootstamp(Integer lineId, CodeFootstamp footstamp) {
			super();
			this.lineId = lineId;
			this.footstamp = footstamp;
		}
	}

	/**
	 * {@link CodeFootstamp} of currently analyzed block of code. We will append to that variable
	 * events that we will see in the current block of code.
	 */
	private CodeFootstamp currentLineFootstamp;

	public DetectDuplicatedCodeMethodVisitor(MethodVisitor mv,
			Map<Integer, Map<Integer, Integer>> duplicatesCollector,
			String className, String methodName, String methodSignature,
			AtomicInteger lineIdGenerator) {
		super(mv, className, methodName, methodSignature, lineIdGenerator);
		this.duplicatesCollector = duplicatesCollector;
	}

	//---------------- Visit event and put it into footstamp methods ----------------------	

	/**
	 * <p>Starts a new block and assigns a new {@link #currentLineFootstamp}.</p>
	 * 
	 * <p>Put's the new {@link LineIdWithCodeFootstamp} into {@link #line2label2codefootstamp}</p>
	 */
	@Override
	public void visitLineNumber(int lineNumber, Label label) {
		super.visitLineNumber(lineNumber, label);

		currentLineFootstamp = new CodeFootstamp();
		List<LineIdWithCodeFootstamp> footstamps = (line2label2codefootstamp
				.get(lineNumber));
		if (footstamps == null) {
			footstamps = new LinkedList<>();
			line2label2codefootstamp.put(lineNumber, footstamps);
		}
		footstamps.add(new LineIdWithCodeFootstamp(lastLineId,
				currentLineFootstamp));
	}

	@Override
	public void visitLabel(Label label) {
		if (currentLineFootstamp != null) {
			currentLineFootstamp.visitLabel(label);
		}
		super.visitLabel(label);
	}

	@Override
	public void visitFieldInsn(int access, String name, String description,
			String signature) {
		if (currentLineFootstamp != null) {
			currentLineFootstamp.visitFieldInsn(access, name, description,
					signature);
		}
		super.visitFieldInsn(access, name, description, signature);
	}

	@Override
	public void visitInsn(int opCode) {
		if (currentLineFootstamp != null) {
			currentLineFootstamp.visitInsn(opCode);
		}
		super.visitInsn(opCode);
	}

	@Override
	public void visitIntInsn(int opCode, int variable) {
		if (currentLineFootstamp != null) {
			currentLineFootstamp.visitIntInsn(opCode, variable);
		}
		super.visitIntInsn(opCode, variable);
	}

	@Override
	public void visitIincInsn(int opCode, int variable) {
		if (currentLineFootstamp != null) {
			currentLineFootstamp.visitIintInsn(opCode, variable);
		}
		super.visitIincInsn(opCode, variable);
	}

	@Override
	public void visitJumpInsn(int opCode, Label label) {
		if (currentLineFootstamp != null) {
			currentLineFootstamp.visitJumpInsn(opCode, label);
		}
		super.visitJumpInsn(opCode, label);
	}

	@Override
	public void visitLdcInsn(Object obj) {
		if (currentLineFootstamp != null) {
			currentLineFootstamp.visitLdcInsn(obj);
		}
		super.visitLdcInsn(obj);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void visitMethodInsn(int opCode, String className,
			String methodName, String description) {
		if (currentLineFootstamp != null) {
			currentLineFootstamp.visitMethodInsn(opCode, className, methodName,
					description);
		}
		super.visitMethodInsn(opCode, className, methodName, description);
	}

	@Override
	public void visitMultiANewArrayInsn(String type, int arg1) {
		if (currentLineFootstamp != null) {
			currentLineFootstamp.visitMultiANewArrayInsn(type, arg1);
		}
		super.visitMultiANewArrayInsn(type, arg1);
	}

	@Override
	public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
		if (currentLineFootstamp != null) {
			currentLineFootstamp.visitLookupSwitchInsn(arg0, arg1, arg2);
		}
		super.visitLookupSwitchInsn(arg0, arg1, arg2);
	}

	@Override
	public void visitTableSwitchInsn(int arg0, int arg1, Label arg2,
			Label... arg3) {
		if (currentLineFootstamp != null) {
			currentLineFootstamp.visitTableSwitchInsn(arg0, arg1, arg2, arg3);
		}
		super.visitTableSwitchInsn(arg0, arg1, arg2, (Label[])arg3);
	}

	@Override
	public void visitEnd() {
		super.visitEnd();
		putDuplicatedLinesIntoMap(duplicatesCollector);
	}

	/**
	 * Analyzes (compare) all footstamps stored in {@link #line2label2codefootstamp} and add found duplicated
	 * into {@link #duplicatesCollector}
	 * 
	 * @param res
	 * map
	 */
	public void putDuplicatedLinesIntoMap(
			Map<Integer, Map<Integer, Integer>> res) {
		for (Map.Entry<Integer, List<LineIdWithCodeFootstamp>> l : line2label2codefootstamp
				.entrySet()) {
			Map<Integer, Integer> r = putDuplicates(l.getValue());
			if (r != null) {
				res.put(l.getKey(), r);
			}

//			if (logger.isDebugEnabled()) {
//				for (LineIdWithCodeFootstamp pair : l.getValue()) {
//					logger.debug("SIGNATURE:" + l.getKey() + ":" + pair.lineId
//							+ ":" + pair.footstamp);
//				}
//			}
		}
	}

	/**
	 * <p>Analyzes (compares) a list of duplicates connected to the line with the same number and
	 * add found duplicated into {@link #duplicatesCollector}</p>
	 *
	 * @param listOfFootstamps - list of footstamps connected with a source-code line with the same number
	 *
	 * @return A map (duplicate lineId -> origin lineId ) of duplicates found in the listOfFootstams, or NULL
	 *         if no such duplicates has been found.
	 */
	private Map<Integer, Integer> putDuplicates(
			List<LineIdWithCodeFootstamp> listOfFootstamps) {
		Map<CodeFootstamp, Integer> reversedMap = new HashMap<>();
		Map<Integer, Integer> result = new HashMap<>();
		for (LineIdWithCodeFootstamp lcf : listOfFootstamps) {
			lcf.footstamp.finalize();
			if (lcf.footstamp.isMeaningful()) {
				Integer found = reversedMap.get(lcf.footstamp);
				if (found != null) {
					result.put(lcf.lineId, found);
				} else {
					reversedMap.put(lcf.footstamp, lcf.lineId);
				}
			}
		}
		return result.size() > 0 ? result : null;
	}

}