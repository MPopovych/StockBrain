package brain.activation;

public class ReLuMinMaxFunction implements ActivationFunction {

	private static final float EPS = 0.1f;
	private static final float MAX_VALUE = 1f + EPS;
	private static final float MIN_VALUE = 0f - EPS;

	@Override
	public float apply(float value) {
		return Math.min(Math.max(value, MIN_VALUE), MAX_VALUE);
	}
}
