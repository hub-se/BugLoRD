package cobertura.test;

public class SimpleProgram {
	
	public static int add(int x, int y) {
		if (x < 0) {
			return x + y;
		} else {
			return y + x;
		}
	}
	
	public static int add2times(int x, double y) {
		int result = add(x, (int)y);
		result += add(x, (int)y);
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
