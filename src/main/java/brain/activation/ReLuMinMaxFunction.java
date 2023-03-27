package brain.activation;

public class ReLuMinMaxFunction implements ActivationFunction {

	private static final float MAX_VALUE = 3f;
	private static final float MIN_VALUE = 0.3f;

	@Override
	public float apply(float value) {
		return Math.min(Math.max(value, MIN_VALUE), MAX_VALUE);
	}
}
