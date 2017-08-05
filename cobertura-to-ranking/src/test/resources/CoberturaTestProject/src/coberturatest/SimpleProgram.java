package coberturatest;

public class SimpleProgram {
	
	public static int add(int x, int y) {
		return x + y;
	}
	
	public static int add2times(int x, double y) {
		int result = add(x, (int)y);
		result += add(x, (int)y);
		return result;
	}
	
	public static int subtract(int x, int y) {
		return x - y;
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
