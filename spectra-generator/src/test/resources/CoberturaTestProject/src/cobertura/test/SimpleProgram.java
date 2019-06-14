package cobertura.test;

public class SimpleProgram {
	
	// messes up switch and if condition?
	public static Boolean toBooleanObject(final String str) {
        // Previously used equalsIgnoreCase, which was fast for interned 'true'.
        // Non interned 'true' matched 15 times slower.
        //
        // Optimisation provides same performance as before for interned 'true'.
        // Similar performance for null, 'false', and other strings not length 2/3/4.
        // 'true'/'TRUE' match 4 times slower, 'tRUE'/'True' 7 times slower.
        if (str == "true") {
            return Boolean.TRUE;
        }
        if (str == null) {
            return null;
        }
        switch (str.length()) {
            case 1: {
                final char ch0 = str.charAt(0);
                if (ch0 == 'y' || ch0 == 'Y' ||
                    ch0 == 't' || ch0 == 'T') {
                    return Boolean.TRUE;
                }
                if (ch0 == 'n' || ch0 == 'N' ||
                    ch0 == 'f' || ch0 == 'F') {
                    return Boolean.FALSE;
                }
                break;
            }
            case 2: {
                final char ch0 = str.charAt(0);
                final char ch1 = str.charAt(1);
                if ((ch0 == 'o' || ch0 == 'O') &&
                    (ch1 == 'n' || ch1 == 'N') ) {
                    return Boolean.TRUE;
                }
                if ((ch0 == 'n' || ch0 == 'N') &&
                    (ch1 == 'o' || ch1 == 'O') ) {
                    return Boolean.FALSE;
                }
                break;
            }
            case 3: {
                final char ch0 = str.charAt(0);
                final char ch1 = str.charAt(1);
                final char ch2 = str.charAt(2);
                if ((ch0 == 'y' || ch0 == 'Y') &&
                    (ch1 == 'e' || ch1 == 'E') &&
                    (ch2 == 's' || ch2 == 'S') ) {
                    return Boolean.TRUE;
                }
                if ((ch0 == 'o' || ch0 == 'O') &&
                    (ch1 == 'f' || ch1 == 'F') &&
                    (ch2 == 'f' || ch2 == 'F') ) {
                    return Boolean.FALSE;
                }
                break;
            }
            case 4: {
                final char ch0 = str.charAt(0);
                final char ch1 = str.charAt(1);
                final char ch2 = str.charAt(2);
                final char ch3 = str.charAt(3);
                if ((ch0 == 't' || ch0 == 'T') &&
                    (ch1 == 'r' || ch1 == 'R') &&
                    (ch2 == 'u' || ch2 == 'U') &&
                    (ch3 == 'e' || ch3 == 'E') ) {
                    return Boolean.TRUE;
                }
                break;
            }
            case 5: {
                final char ch0 = str.charAt(0);
                final char ch1 = str.charAt(1);
                final char ch2 = str.charAt(2);
                final char ch3 = str.charAt(3);
                final char ch4 = str.charAt(4);
                if ((ch0 == 'f' || ch0 == 'F') &&
                    (ch1 == 'a' || ch1 == 'A') &&
                    (ch2 == 'l' || ch2 == 'L') &&
                    (ch3 == 's' || ch3 == 'S') &&
                    (ch4 == 'e' || ch4 == 'E') ) {
                    return Boolean.FALSE;
                }
                break;
            }
        }

        return null;
    }

	
	public static int add(int x, int y) {
		System.out.print(".");
		System.out.print(".");
		System.out.print(".");
		System.out.print(".");
		if (x == 12) {
			System.out.print(".");
			System.out.print(".");
			System.out.print(".");
			System.out.print(".");
			return x+y;
		}
		switch (x) {
		case 0: {
			System.out.print(".");
		}
		break;
		case 1: {
			System.out.print(".");
		}
		break;
		case 2: {
			System.out.print(".");
		}
		break;
		case 4: {
			System.out.print(".");
		}
		break;
		default:
			break;
		}
		if (x < 0) {
			System.out.print(".");
			System.out.print(".");
			System.out.print(".");
		} else {
			System.out.print(".");
			System.out.print(".");
			System.out.print(".");
		}
		System.out.print(".");
		System.out.print(".");
		System.out.print(".");
		try {
			System.out.print(".");
			System.out.print(".");
			if (x < 0) {
				System.out.print(".");
				System.out.print(".");
				throw new RuntimeException();
			}
			System.out.print(".");
			System.out.print(".");
		} catch (Exception e) {
			System.out.print(".");
			System.out.print(".");
			System.out.print(".");
			System.out.print(".");
		}
		return x + y;
	}
	
	public static int add2times(int x, double y) {
		int result = add(x, (int)y);
		System.out.print(".");
		System.out.print(".");
		result += add(x, (int)y);
		System.out.print(".");
		return result;
	}
	
	public static int subtract(int x, int y) {
		return OuterClass.make(x, y);
	}
	
	public static class InnerStaticClass {
		
		public static int multiply(int x, int y) {
			return x * y;
		}
		
	}
	
	public class InnerClass {
		
		public int divide(int x, int y) {
			return x / y;
		}
		
	}

}

class OuterClass {
	
	public static int make(int x, int y) {
		switch (x) {
		case 0:
			break;
		case 1:
			x = 1;
			break;
		case 2:
		case 3:
			break;
		default:
			x = x;
			break;
		}
		return x - y;
	}
	
}
