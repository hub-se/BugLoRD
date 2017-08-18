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
		int sum = 0; // 0
		double uselessDoubleWithoutInit; // 1
		boolean uselessBool = false; // 2
		char uselessCharWithSimpleInit = 'x'; // 3
		char uselessCharWithComplexInit = "someChars".charAt( 4 ); // 4
		long uselessLong = 4711l; // 5
		String uselessString = "siebenundvierzig"; // 6
		
		Integer bigSum = new Integer( 47 ); // 7
		Double bigDouble = null; // 8
		Boolean bigBool = new Boolean( true ); // 9
		Character bigChar = new Character( 'x' ); // 10
		Long bigLong = new Long( 1337l ); // 11
		
		int index = 10; // 12 same name/type as the global variable
		
		/**
		 * I never heard of Gauss and need a loop here
		 */
		for( int i = aStartIdx; i <= aEndIdx; ++i ) { // 13
			sum += i;
		}
		
		int belowInt = 4; // 14 this should not appear in the list for the for statement node
		String belowStr = "neverUsed: " + belowInt; // 15
		System.out.println( belowStr ); // 16
		
		for( int i = 0; i < 100; ++i ) { // 17
			String outerLoop = "outerForLoop"; // 17.0
			while( i < 100 ) { // 17.1
				String innerWhileLoop = "innerWhileLoop"; // 17.1.0
				for( byte b : innerWhileLoop.getBytes() ) { // 17.1.1
					int mostInnerLoop = b; // 17.1.1.0
					System.out.println( mostInnerLoop ); // 17.1.1.1
				}
				String innerWhileLoopEnd = "innerWhileLoopEnd"; // 17.1.2
				System.out.println( innerWhileLoop + innerWhileLoopEnd); // 17.1.3
			}

			String outerLoopEnd = "outerForLoopEnd"; // 17.2
			System.out.println( outerLoop + outerLoopEnd ); // 17.3
		}
		
		long time = System.currentTimeMillis(); // 18
		System.out.println( time ); // 19
		
		return sum + lastGloVar; // 20
	}

	// will this be put to the top of the children of the class declaration in the ast?
	private int lastGloVar = 128;
	
}
