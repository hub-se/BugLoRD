package se.de.hu_berlin.informatik.astlmbuilder.tests;

/**
 * This class is mainly for testing purposes and does not really contain
 * functionality because it only exists to be parsed
 */
public class TestSource {

	public static final int 	CONSTANT_INT = 0;
	public static final double 	CONSTANT_DOUBLE = 0.0;
	public static final long 	CONSTANT_LONG = 0l;
	public static final String  CONSTANT_STR = "0";
	
	public TestSource( int aArg, String aName ) {
		super();
	}
	
	/**
	 * The static entry method
	 * @param args
	 * command line arguments
	 */
	public static void main(String[] args) {
		TestSource ts = new TestSource( -1, "Testing" );
		ts.doAction();
	}
	
	/**
	 * The non static entry
	 */
	public void doAction() {
		System.out.println( "Test Source started." );
		
		doSomethingUseless( 10, 2, "Hello" );
		
		System.out.println( "Test Source finished" );
	}
	
	/**
	 * Very important
	 * @param aFirstArg
	 * @param aSecondArg
	 * @param aName
	 * @return
	 */
	private double[] doSomethingUseless( double aFirstArg, int aSecondArg, String aName ) {
		double[] result = new double[ aSecondArg ];
		
		for( int i = 1; i <= aSecondArg; ++i ) {
			result[ aSecondArg - i ] = Math.pow( i, aSecondArg );
			System.out.println( result[ aSecondArg - i ]);
		}
		
		return result;
	}
	
	/**
	 * This will be intense
	 */
	protected void complicated() {
		int lowerBnd = 0;
		int upperBnd = 100;
		int checkBnd = upperBnd;
		int rnd = getRndInt( upperBnd );
		
		checkBnd = getMedianValue( lowerBnd, upperBnd );
		if( rnd < checkBnd ) {
			checkBnd = getMedianValue( lowerBnd, checkBnd );
			if ( rnd < checkBnd ) {
				checkBnd = (int) (upperBnd*0.1);
				if ( rnd < checkBnd ) {
					printBnd( lowerBnd, checkBnd, rnd );
				} else {
					printBnd( lowerBnd, checkBnd, rnd );
				}
			} else {
				
			}
		} else {
			if( rnd < getMedianValue( checkBnd, upperBnd ) ) {
				if( rnd < upperBnd ) {
					printBnd( lowerBnd, upperBnd, rnd );
				} else {
					System.out.println( "impossible" );
				}
			} else {
				
			}
		}
		
		// some comments
		InnerClass ic = new InnerClass();
		ic.doPublicAction();
		ic.doPrivateAction();
		ic.callTheOtherMethods( 10 );
		
		System.out.println( "This was complex" );
	}
	
	private int getRndInt( int aRange ) {
		return (int) (Math.random() * aRange);
	}
	
	private int getMedianValue( int aLowerBnd, int aUpperBnd ) {
		return (int) ((aLowerBnd + aUpperBnd )*0.5);
	}
	
	private void printBnd( int lower, int upper, int value ) {
		System.out.println( "Between " + lower + " and " + upper + " (" + value + ")");
	}
	
	/**
	 * Inner classes are a thing
	 */
	private class InnerClass {
		private int innerInteger = 0;
		private boolean innerBoolean = true;
		private String innerString = ":)";
		
		/**
		 * Just a method in an inner class
		 */
		public void doPublicAction() {
			if( innerBoolean ) {
				System.out.println( "Hello public inner action!" );
			}
		}
		
		/**
		 * Just a method in an inner class
		 */
		private void doPrivateAction() {
			System.out.println( "Hello private inner action! " + innerString );
		}
		
		/**
		 * Calls the inner public and private method
		 * @param howMany
		 */
		public void callTheOtherMethods( int howMany ) {
			for( int i = innerInteger; i < howMany; ++i ) {
				doPublicAction();
				doPrivateAction();
				System.out.println( "Looping for fun " + Math.min( i,  howMany) );
			}
		}
	}

}
