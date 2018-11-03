package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data;


public interface LightClassmapListener {
	public void setClazz(Class<?> clazz);

	public void setClazz(String clazz);

	public void setSource(String source);

	public void putLineTouchPoint(int classLine, int counterId,
			String methodName, String methodDescription);

	public void putJumpTouchPoint(int classLine, int trueCounterId,
			int falseCounterId);

	public void putSwitchTouchPoint(int classLine, int maxBranches,
			int... counterIds);
}
