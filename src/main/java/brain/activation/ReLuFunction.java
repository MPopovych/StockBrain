package brain.activation;

public class ReLuFunction extends ActivationFunction {
	private static final float MIN_VALUE = 0f;

	@Override
	public float apply(float value) {
		return Math.max(value, MIN_VALUE);
	}
}
