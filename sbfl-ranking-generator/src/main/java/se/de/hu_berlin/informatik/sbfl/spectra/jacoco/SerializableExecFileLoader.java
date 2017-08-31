package se.de.hu_berlin.informatik.sbfl.spectra.jacoco;

import java.io.IOException;
import java.io.Serializable;
import org.jacoco.core.tools.ExecFileLoader;

public class SerializableExecFileLoader implements Serializable {

	private static final long serialVersionUID = -3585982447063741516L;

	private ExecFileLoader loader;

	public SerializableExecFileLoader(ExecFileLoader loader) {
		this.loader = loader;
	}

	public ExecFileLoader getExecFileLoader() {
		return loader;
	}

	private void writeObject(java.io.ObjectOutputStream out)
			throws IOException {
		if (loader == null) {
			out.writeByte(0);;
		} else {
			loader.save(out);
		}
	}

	private void readObject(java.io.ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		try {
			loader = new ExecFileLoader();
			loader.load(in);
		} catch (IOException e) {
			loader = null;
		}
	}
}
