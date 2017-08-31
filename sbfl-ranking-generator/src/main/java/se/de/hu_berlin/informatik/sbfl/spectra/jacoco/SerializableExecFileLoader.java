package se.de.hu_berlin.informatik.sbfl.spectra.jacoco;

import java.io.IOException;
import java.io.Serializable;
import org.jacoco.core.tools.ExecFileLoader;

public class SerializableExecFileLoader implements Serializable {
	
	private transient final static byte NULL_DUMMY = 0;
	private transient final static byte NOT_NULL_DUMMY = 1;
	
	private static final long serialVersionUID = -3585982447063741516L;

	private transient ExecFileLoader loader;

	public SerializableExecFileLoader(ExecFileLoader loader) {
		this.loader = loader;
	}

	public ExecFileLoader getExecFileLoader() {
		return loader;
	}

	private void writeObject(java.io.ObjectOutputStream out)
			throws IOException {
		if (loader == null) {
			out.writeByte(NULL_DUMMY);
		} else {
			out.writeByte(NOT_NULL_DUMMY);
			loader.save(out);
		}
		out.flush();
	}

	private void readObject(java.io.ObjectInputStream in)
			throws IOException, ClassNotFoundException {
			if (in.readByte() == NULL_DUMMY) {
				loader = null;
			} else {
				loader = new ExecFileLoader();
				loader.load(in);
			}
	}

	@Override
	public int hashCode() {
		return loader.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (loader == null) {
			return loader == obj;
		} else {
			return loader.equals(obj);
		}
	}
	 
}
