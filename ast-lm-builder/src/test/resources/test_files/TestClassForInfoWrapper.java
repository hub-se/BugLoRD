package se.de.hu_berlin.informatik.astlmbuilder.parsing.parser;

/**
 * Just a class to load and create info wrapper objects for testing
 */
public class TestClassForInfoWrapper {

	// lets see if they are accessible in some way
	public static final String A_CONSTANT = "Hi";
	public final String A_STRING = "World";
	private int index = 4711;
	
	/**
	 * This is a comment for the main method
	 * @param args
	 */
	public static void main(String[] args) {
		TestClassForInfoWrapper tc = new TestClassForInfoWrapper();
		tc.doAction( args );
	}
	
	public void doAction( String[] args ) {
		doActionInPrivat( args );
	}
	
	private void doActionInPrivat( String[] args ) {
		System.out.println( "This method was called with " + args.length + " arguments" );
		
		// a condition with a nice block
		if ( calcSumFromTo( 10, 20 ) < 150 ) {
			String output = "The sum is lower";
			double something = 15.0;
			boolean b = false;
			
			getSomeParameters( output, something, b);
		}
	}
	
	public void getSomeParameters( String aStr, double aDbl, boolean aB ) {
		if ( aB ) {
			System.out.println( aStr );
		} else {
			System.out.println( aDbl );
		}
	}
	
	private int calcSumFromTo( int aStartIdx, int aEndIdx ) {
		int sum = 0;
		double uselessDoubleWithoutInit;
		boolean uselessBool = false;
		char uselessCharWithSimpleInit = 'x';
		char uselessCharWithComplexInit = "someChars".charAt( 4 );
		long uselessLong = 4711l;
		String uselessString = "siebenundvierzig";
		
		Integer bigSum = new Integer( 47 );
		Double bigDouble = null; // this will be interesting
		Boolean bigBool = new Boolean( true );
		Character bigChar = new Character( 'x' );
		Long bigLong = new Long( 1337l );
		
		int index = 10; // same name/type as the global variable
		
		/**
		 * I never heard of Gauss and need a loop here
		 */
		for( int i = aStartIdx; i <= aEndIdx; ++i ) {
			sum += i;
		}
		
		int belowInt = 4; // this should not appear in the list for the for statement node
		String belowStr = "neverUsed: " + belowInt;
		System.out.println( belowStr );
		
		return sum + lastGloVar;
	}

	// will this be put to the top of the children of the class declaration in the ast?
	private int lastGloVar = 128;
	
}
