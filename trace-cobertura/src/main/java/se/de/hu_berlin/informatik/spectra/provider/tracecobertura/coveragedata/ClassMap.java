package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;


import org.objectweb.asm.Label;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.JumpTouchPointDescriptor;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.LineTouchPointDescriptor;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.SwitchTouchPointDescriptor;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.TouchPointDescriptor;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>This class is a container for informations gathered during class analyzing done by {@link BuildClassMapClassVisitor}.</p>
 *
 * @author piotr.tabor@gmail.com
 */
public class ClassMap {
//	private static final Logger logger = LoggerFactory
//			.getLogger(ClassMap.class);
	/**
	 * Simple name of source-file that was used to generate that value
	 */
	private String source;

	/**
	 * We map every eventId that is connected to instruction that created touch-point to the touch-point
	 */
	private final Map<Integer, TouchPointDescriptor> eventId2touchPointDescriptor = new HashMap<>();

	/**
	 * Contains map of label into set of {@link JumpTouchPointDescriptor} or {@link SwitchTouchPointDescriptor} that the label could be destination of
	 * 
	 * <p>The labels used here are {@link Label} created during {@link BuildClassMapClassVisitor} pass. Don't try to compare it with labels created by other instrumentation passes.
	 * Instead you should use eventId and {@link #eventId2label} to get the label created in the first pass and lookup using the label.</p>
	 */
	private final Map<Label, Set<TouchPointDescriptor>> label2sourcePoints = new HashMap<>();

	/**
	 * Maps eventId to code label from BuildClassMapClassInstrumenter pass
	 */
	private final Map<Integer, Label> eventId2label = new HashMap<>();

	/**
	 * List of line numbers (not lineIds) of lines that are not allowed to contain touch-point. This
	 * lines was probably excluded from coverage using 'ignore' stuff.
	 */
	private final Set<Integer> blockedLines = new HashSet<>();

	/**
	 * List of touch-points stored in given line.
	 */
	private final SortedMap<Integer, List<TouchPointDescriptor>> line2touchPoints = new TreeMap<>();

	/**
	 * Set of eventIds that has bean already registered.
	 */
	private final Set<Integer> alreadyRegisteredEvents = new HashSet<>();

	/*from duplicate to origin*/
	private final Map<Label, Label> labelDuplicates2orginMap = new HashMap<>();
	private final Map<Label, Set<Label>> labelDuplicates2duplicateMap = new HashMap<>();

	private String className;

	private int maxCounterId = 0;

	private final int classId;
	
	public ClassMap(int classId) {
		this.classId = classId;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void registerNewJump(int eventId, int currentLine,
			Label destinationLabel) {
		if (alreadyRegisteredEvents.add(eventId)) {
//			logger.debug(className + ":" + currentLine + ": Registering JUMP ("
//					+ eventId + ") to " + destinationLabel);
			JumpTouchPointDescriptor descriptor = new JumpTouchPointDescriptor(
					eventId, currentLine/*,destinationLabel*/);
			eventId2touchPointDescriptor.put(eventId, descriptor);
			getOrCreateSourcePoints(destinationLabel).add(descriptor);
			getOrCreateLineTouchPoints(currentLine).add(descriptor);
		} else {
//			logger.debug(className + ":" + currentLine
//					+ ": NOT registering (already done) JUMP (" + eventId
//					+ ") to " + destinationLabel);
		}
	}

	private List<TouchPointDescriptor> getOrCreateLineTouchPoints(
			int currentLine) {
		List<TouchPointDescriptor> res = line2touchPoints.get(currentLine);
		if (res == null) {
			res = new LinkedList<>();
			line2touchPoints.put(currentLine, res);
		}
		return res;
	}

	private Set<TouchPointDescriptor> getOrCreateSourcePoints(Label label) {
		Set<TouchPointDescriptor> res = label2sourcePoints.get(label);
		if (res == null) {
			res = new HashSet<>();
			label2sourcePoints.put(label, res);
		}
		return res;
	}

	public void registerNewLabel(int eventId, int currentLine, Label label) {
//		logger.debug(className + ":" + currentLine + ": Registering label ("
//				+ eventId + ") " + label);
		if (alreadyRegisteredEvents.add(eventId)) {
			eventId2label.put(eventId, label);
			putIntoDuplicatesMaps(label, label);
		} else {
			putIntoDuplicatesMaps(label, eventId2label.get(eventId));
		}
	}

	public void putIntoDuplicatesMaps(Label label, Label orgin) {
		labelDuplicates2orginMap.put(label, orgin); //For coherence
		Set<Label> list = labelDuplicates2duplicateMap.get(orgin);
		if (list == null) {
			list = new HashSet<>();
			labelDuplicates2duplicateMap.put(orgin, list);
		}
		list.add(label);
	}

	public void registerLineNumber(int eventId, int currentLine, Label label,
			String methodName, String methodSignature) {
//		logger.debug(className + ":" + currentLine + ": Registering line ("
//				+ eventId + ") " + label);
		if (alreadyRegisteredEvents.add(eventId)) {
			if (!blockedLines.contains(currentLine)) {
				LineTouchPointDescriptor line = new LineTouchPointDescriptor(
						eventId, currentLine, methodName, methodSignature);
				eventId2label.put(eventId, label);
				eventId2touchPointDescriptor.put(eventId, line);
				getOrCreateLineTouchPoints(currentLine).add(line);
			}
		}
	}

	public void unregisterLine(int eventId, int currentLine) {
		if (alreadyRegisteredEvents.add(eventId)) {
			blockedLines.add(currentLine);
			List<TouchPointDescriptor> res = line2touchPoints.get(currentLine);
			if (res != null) {
				Iterator<TouchPointDescriptor> iter = res.iterator();
				while (iter.hasNext()) {
					TouchPointDescriptor desc = iter.next();
					if (desc instanceof LineTouchPointDescriptor) {
						iter.remove();
						eventId2touchPointDescriptor.remove(desc.getEventId());
						eventId2label.remove(desc.getEventId());
					}
				}
			}
		}
	}

	public void registerSwitch(int eventId, int currentLine, Label def,
			Label[] labels, String conditionType) {
		if (alreadyRegisteredEvents.add(eventId)) {
			SwitchTouchPointDescriptor swi = new SwitchTouchPointDescriptor(
					eventId, currentLine, def, labels, conditionType);
			eventId2touchPointDescriptor.put(eventId, swi);
			getOrCreateLineTouchPoints(currentLine).add(swi);
			getOrCreateSourcePoints(def).add(swi);
			for (Label l : labels) {
				//				System.out.println("Registering label to switch:"+l);
				getOrCreateSourcePoints(l).add(swi);
			}
		}
	}

	//======================= data retrieval =====================================================	

	public Integer getCounterIdForFalseBranchJump(int eventId) {
		if (eventId2touchPointDescriptor.get(eventId) instanceof JumpTouchPointDescriptor) {
			JumpTouchPointDescriptor jumpTouchPointDescriptor = (JumpTouchPointDescriptor) eventId2touchPointDescriptor
					.get(eventId);
			if (jumpTouchPointDescriptor != null) {
				return jumpTouchPointDescriptor.getCounterIdForTrue();
			}
		}
		return null;
	}

	public Integer getCounterIdForTrueBranchJump(int eventId) {
		if (eventId2touchPointDescriptor.get(eventId) instanceof JumpTouchPointDescriptor) {
			JumpTouchPointDescriptor jumpTouchPointDescriptor = (JumpTouchPointDescriptor) eventId2touchPointDescriptor
					.get(eventId);
			if (jumpTouchPointDescriptor != null) {
				return jumpTouchPointDescriptor.getCounterIdForFalse();
			}
		}
		return null;
	}

	public boolean isJumpDestinationLabel(int eventId) {
		Label label_local = eventId2label.get(eventId);
//		logger.debug("Label found for eventId:" + eventId + ":" + label_local);
		if (labelDuplicates2duplicateMap.containsKey(label_local)) {
			for (Label label : labelDuplicates2duplicateMap.get(label_local)) {
				if (label != null) {
					Set<TouchPointDescriptor> res = label2sourcePoints
							.get(label);
//					logger
//							.debug("label2sourcePoints.get(" + label + "):"
//									+ res);
					if (res != null) {
						for (TouchPointDescriptor r : res) {
							if (r instanceof JumpTouchPointDescriptor) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	public Integer getCounterIdForSwitch(int eventId) {
		if (eventId2touchPointDescriptor.get(eventId) instanceof SwitchTouchPointDescriptor) {
			SwitchTouchPointDescriptor point = (SwitchTouchPointDescriptor) eventId2touchPointDescriptor
					.get(eventId);
			if (point != null) {
				return point.getCounterId();
			}
		}
		return null;
	}

	public Integer getCounterIdForLineEventId(int eventId) {
		if (eventId2touchPointDescriptor.get(eventId) instanceof LineTouchPointDescriptor) {
			LineTouchPointDescriptor point = (LineTouchPointDescriptor) eventId2touchPointDescriptor
					.get(eventId);
			if (point != null) {
				return point.getCounterId();
			}
		}
		return null;
	}

	/**
	 * Returns map:   switchCounterId to counterId
	 *
	 * @param labelEventId id
	 *
	 * @return map:   switchCounterId to counterId
	 */
	public Map<Integer, Integer> getBranchLabelDescriptorsForLabelEvent(
			int labelEventId) {
		Label label_local = eventId2label.get(labelEventId);
		if (label_local != null) {
			if (labelDuplicates2duplicateMap.containsKey(label_local)) {
				for (Label label : labelDuplicates2duplicateMap
						.get(label_local)) {
					Set<TouchPointDescriptor> list = label2sourcePoints
							.get(label);
					if (list != null) {
						Map<Integer, Integer> res = new HashMap<>();
						for (TouchPointDescriptor r : list) {
							if (r instanceof SwitchTouchPointDescriptor) {
								SwitchTouchPointDescriptor swi = (SwitchTouchPointDescriptor) r;
								res.put(swi.getCounterId(), swi
										.getCounterIdForLabel(label));
							}
						}
						return res;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Iterates over all touch-points created during class analysis and assigns
	 * hit-counter identifiers to each of the touchpoint (some of them needs mode then one
	 * hit-counter).
	 * 
	 * This class assign hit-counter ids to each touch-point and upgrades maxCounterId to
	 * reflect the greatest assigned Id.
	 * @return 
	 * an array that maps counter IDs to actual line numbers
	 */
	public int[] assignCounterIds() {
		AtomicInteger idGenerator = new AtomicInteger(0);
		for (List<TouchPointDescriptor> tpd : line2touchPoints.values()) {
			for (TouchPointDescriptor t : tpd) {
				t.assignCounters(idGenerator);
			}
		}
		maxCounterId = idGenerator.get();
		
		int[] result = new int[maxCounterId+1];
		// set to -1
		for (int i = 0; i < result.length; i++) {
			result[i] = -1;
		}
		
		// map counter IDs to actual line numbers
		for (TouchPointDescriptor tpd : getTouchPointsInLineOrder()) {
			if (tpd instanceof LineTouchPointDescriptor) {
				result[((LineTouchPointDescriptor) tpd).getCounterId()] = tpd.getLineNumber();
			} else if (tpd instanceof JumpTouchPointDescriptor) {
				result[((JumpTouchPointDescriptor) tpd).getCounterIdForTrue()] = tpd.getLineNumber();
				result[((JumpTouchPointDescriptor) tpd).getCounterIdForFalse()] = tpd.getLineNumber();
			} else if (tpd instanceof SwitchTouchPointDescriptor) {
				result[((SwitchTouchPointDescriptor) tpd).getCounterId()] = tpd.getLineNumber();
				for (int id : ((SwitchTouchPointDescriptor) tpd).getCountersForLabels()) {
					result[id] = tpd.getLineNumber();
				}
			}
		}
		
		return result;
	}

	public int getMaxCounterId() {
		return maxCounterId;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getSource() {
		return source;
	}

	public List<TouchPointDescriptor> getTouchPointsInLineOrder() {
		LinkedList<TouchPointDescriptor> res = new LinkedList<>();
		for (List<TouchPointDescriptor> tpd : line2touchPoints.values()) {
			for (TouchPointDescriptor t : tpd) {
				if (t instanceof LineTouchPointDescriptor) {
					res.add(t);
				}
			}
			for (TouchPointDescriptor t : tpd) {
				if (!(t instanceof LineTouchPointDescriptor)) {
					res.add(t);
				}
			}
		}
		return res;
	}

	/*
	 * Upgrades {@link ProjectData} to contain all information found in class during class instrumentation.
	 * 
	 * <p>I don't like the idea of creating ser file during the instrumentation, but we need to do it,
	 * to be compatible with tools that expect that (such as cobertura-maven-plugin)</p>
	 *
	 * @param projectData
	 */
	public ClassData applyOnProjectData(ProjectData projectData,
			boolean instrumented) {
		ClassData classData = projectData.getOrCreateClassData(className
				.replace('/', '.'), classId);
		if (source != null) {
			classData.setSourceFileName(source);
		}
		if (instrumented) {
//			classData.counterIdToLineNumberMap = new HashMap<>();
			classData.setContainsInstrumentationInfo();
			int lastLine = 0;
//			int jumpsInLine = 0;
//			int toucesInLine = 0;

			for (TouchPointDescriptor tpd : getTouchPointsInLineOrder()) {
				if (tpd.getLineNumber() != lastLine) {
//					jumpsInLine = 0;
//					toucesInLine = 0;
					lastLine = tpd.getLineNumber();
				}
				if (tpd instanceof LineTouchPointDescriptor) {
					classData.addLine(tpd.getLineNumber(),
							((LineTouchPointDescriptor) tpd).getMethodName(),
							((LineTouchPointDescriptor) tpd).getMethodSignature());
				} else if (tpd instanceof JumpTouchPointDescriptor) {
//					classData.addLineJump(tpd.getLineNumber(), jumpsInLine++);
				} else if (tpd instanceof SwitchTouchPointDescriptor) {
//					int countersCnt = ((SwitchTouchPointDescriptor) tpd)
//							.getCountersForLabelsCnt();
//					//TODO(ptab): instead of Integer.MAX_VALUE should be length of Enum.
//					classData.addLineSwitch(tpd.getLineNumber(),
//							toucesInLine++, 0, countersCnt - 2,
//							Integer.MAX_VALUE);
				}
			}
		}
		return classData;
	}

	public int getClassId() {
		return classId;
	}

}
