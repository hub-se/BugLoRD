package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.EfficientCompressedIntegerTrace;

public class CoberturaStatementEncoding {

//	private static final long UPPER_BITMASK = 0xFFFFFFFF00000000L;
	private static final long LOWER_BITMASK = 0x00000000FFFFFFFFL;
	
	private static final int INTEGER_BITS = 32;
	
	// used for encoding (2) cobertura counters/class IDs into a single long value (4 ints -> 1 long)
	// uses 11 bit for the class identifier (max 2048 classes);
	// uses 21 bit for the counter id (max 2,097,152 counters per class);
	public static final int CLASS_ID_BITS = 11;
	public static final int COUNTER_ID_BITS = 21;

	public static final int NORMAL_ID = 0;
	public static final int BRANCH_ID = 1;
	public static final int JUMP_ID = 2;
	public static final int SWITCH_ID = 3;

//	public static long generateUniqueRepresentationForStatement(int classId, int counterId, int specialIndicatorId) {
//		//        32 bits        |       32 bits
//		// class id | counter id | special indicator id
//		return ((((long)classId << COUNTER_ID_BITS) 
//				| counterId) << INTEGER_BITS)
//				| specialIndicatorId;
//	}
//	
//	public static int getClassId(long encodedStatement) {
//		// push everything to the right (fills up with 0s)
//		return (int) (encodedStatement >>> (INTEGER_BITS + COUNTER_ID_BITS));
//	}
//	
//	public static int getCounterId(long encodedStatement) {
//		// push out the class id and then push everything back (fills up with 0s)
//		return (int) ((encodedStatement << CLASS_ID_BITS) >>> (INTEGER_BITS + CLASS_ID_BITS));
//	}
//	
//	public static int getSpecialIndicatorId(long encodedStatement) {
//		// the actual statement information is placed in the first 32 bit that get cut off!
//		return (int) encodedStatement;
//	}

	
	
	public static int generateUniqueRepresentationForStatement(int classId, int counterId) {
		//        32 bits        
		// class id | counter id
		return (classId << COUNTER_ID_BITS) | counterId;
	}
	
	public static int getClassId(int encodedStatement) {
		// push everything to the right (fills up with 0s)
		return (int) (encodedStatement >>> (COUNTER_ID_BITS));
	}
	
	public static int getCounterId(int encodedStatement) {
		// push out the class id and then push everything back (fills up with 0s)
		return (int) ((encodedStatement << CLASS_ID_BITS) >>> (CLASS_ID_BITS));
	}
	
	
	

//	// store the starting and ending statements in a single long value!
//	// should represent a sub trace uniquely!
//	// a counter with id 0 can not occur!
//	public static long generateUniqueRepresentationForSubTrace(BufferedLongArrayQueue subTrace) {
//		if (subTrace.isEmpty()) {
//			return 0;
//		} else if (subTrace.size() == 1) {
//			return subTrace.element() & UPPER_BITMASK;
//		} else {
//			return (subTrace.element() & UPPER_BITMASK) | (subTrace.lastElement() >> INTEGER_BITS);
//		}
//	}
	
	// store the starting and ending statements in a single long value!
	// should represent a sub trace uniquely!
	// a counter with id 0 can not occur!
	public static long generateRepresentationForSubTrace(BufferedIntArrayQueue subTrace) {
		if (subTrace.isEmpty()) {
			return 0;
		} else if (subTrace.size() == 1) {
			return (subTrace.element() & LOWER_BITMASK) << INTEGER_BITS;
		} else {
			return ((subTrace.element() & LOWER_BITMASK) << INTEGER_BITS) | (subTrace.lastElement() & LOWER_BITMASK);
		}
	}
	
//	public static long generateUniqueRepresentationForSubTrace(EfficientCompressedLongTrace subTrace) {
//		if (subTrace.isEmpty()) {
//			return 0;
//		} else if (subTrace.size() == 1) {
//			return subTrace.getFirstElement() & UPPER_BITMASK;
//		} else {
//			return (subTrace.getFirstElement() & UPPER_BITMASK) | (subTrace.getLastElement() >> INTEGER_BITS);
//		}
//	}
	
	public static long generateRepresentationForSubTrace(EfficientCompressedIntegerTrace subTrace) {
		if (subTrace.size() > 1) {
			return ((subTrace.getFirstElement() & LOWER_BITMASK) << INTEGER_BITS) | (subTrace.getLastElement() & LOWER_BITMASK);
		} else if (subTrace.size() == 1) {
			return (subTrace.getFirstElement() & LOWER_BITMASK) << INTEGER_BITS;
		} else {
			return 0;
		}
	}
	
	public static long generateRepresentationForSubTrace(SingleLinkedIntArrayQueue subTrace) {
		if (subTrace.size() > 1) {
			return ((subTrace.peekNoCheck() & LOWER_BITMASK) << INTEGER_BITS) | (subTrace.peekLastNoCheck() & LOWER_BITMASK);
		} else if (subTrace.size() == 1) {
			return (subTrace.peekNoCheck() & LOWER_BITMASK) << INTEGER_BITS;
		} else {
			return 0;
		}
	}
	
	public static long generateUniqueRepresentationForTwoStatements(int encodedStatement1, int encodedStatement2) {
		return ((encodedStatement1 & LOWER_BITMASK) << INTEGER_BITS) | (encodedStatement2 & LOWER_BITMASK);
	}

	public static int getFirstClassId(long encodedSubTrace) {
		return getClassId((int)(encodedSubTrace >>> INTEGER_BITS));
	}
	
	public static int getFirstCounterId(long encodedSubTrace) {
		return getCounterId((int)(encodedSubTrace >>> INTEGER_BITS));
	}
	
	public static int getLastClassId(long encodedSubTrace) {
		return getClassId((int)(encodedSubTrace & LOWER_BITMASK));
	}
	
	public static int getLastCounterId(long encodedSubTrace) {
		return getCounterId((int)(encodedSubTrace & LOWER_BITMASK));
	}

	
	

}
