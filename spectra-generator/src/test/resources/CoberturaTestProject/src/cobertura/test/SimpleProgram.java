package cobertura.test;

public class SimpleProgram {
	
	public static int add(int x, int y) {
		System.out.print(".");
		System.out.print(".");
		System.out.print(".");
		System.out.print(".");
		if (x < 0) {
			System.out.print(".");
			System.out.print(".");
			System.out.print(".");
			System.out.print(".");
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
