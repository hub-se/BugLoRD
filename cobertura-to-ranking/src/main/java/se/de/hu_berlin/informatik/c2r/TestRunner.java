package se.de.hu_berlin.informatik.c2r;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

public class TestRunner extends Runner {
	
	private String clazz;
	private String method;

	public TestRunner(String clazz, String method) {
		super();
		this.clazz = clazz;
		this.method = method;
	}

	@Override
	public Description getDescription() {
		return Description.createTestDescription(clazz, method);
	}

	@Override
	public void run(RunNotifier notifier) {
		// TODO Auto-generated method stub
		
	}
	
	

}
