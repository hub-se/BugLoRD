package se.de.hu_berlin.informatik.stardust.provider.cobertura;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class LockableProjectData extends ProjectData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8484242021027071646L;
	private boolean locked = false;

	public void lock() {
		locked  = true;
	}
	
	@Override
	public void addClassData(ClassData classData) {
		if (locked) {
			Log.err(this, "Adding class data to locked project data...");
		}
		super.addClassData(classData);
	}

//	@Override
//	public ClassData getClassData(String name) {
//		// TODO Auto-generated method stub
//		return super.getClassData(name);
//	}

	@Override
	public ClassData getOrCreateClassData(String name) {
		if (locked) {
			Log.err(this, "Getting or creating class data in locked project data.");
		}
		lock.lock();
		try {
			ClassData classData = getClassData(name);
			if (classData == null) {
				classData = new MyClassData(name);
				addClassData(classData);
			}
			return classData;
		} finally {
			lock.unlock();
		}
	}

}
