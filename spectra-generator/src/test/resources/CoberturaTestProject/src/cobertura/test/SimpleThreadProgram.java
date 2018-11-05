package cobertura.test;

public class SimpleThreadProgram {
	
	public static void runOneThread() {
		Thread thread = new Thread(new MyThread());
		try {
			thread.start();
			thread.join();
		} catch (InterruptedException e) {
			// do nothing
		}
	}
	
	public static void runTwoThreads() {
		System.out.println("Main Thread ID: " + String.valueOf(Thread.currentThread().getId()));
		Thread thread = new Thread(new MyWaitingThread());
		Thread thread2 = new Thread(new MyThread());
		try {
			System.out.println("Waiting Thread ID: " + String.valueOf(thread.getId()));
			System.out.println("Thread ID: " + String.valueOf(thread2.getId()));
			thread.start();
			thread2.start();
			thread.join();
			thread2.join();
		} catch (InterruptedException e) {
			// do nothing
		}
	}

	public static class MyThread implements Runnable {

		@Override
		public void run() {
			System.out.println("actual Thread ID: " + String.valueOf(Thread.currentThread().getId()));
			SimpleProgram.add(2,SimpleProgram.subtract(10,3));
			
			SimpleProgram.InnerStaticClass.multiply(10,3);
		}
		
	}
	
	public static class MyWaitingThread implements Runnable {

		@Override
		public void run() {
			System.out.println("actual Waiting Thread ID: " + String.valueOf(Thread.currentThread().getId()));
			SimpleProgram.add(2,SimpleProgram.subtract(10,3));
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// do nothing
			}
			SimpleProgram.InnerStaticClass.multiply(10,3);
		}
		
	}
	
}
