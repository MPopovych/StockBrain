package brain.activation;

public class ReLuMinMaxFunction implements ActivationFunction {
	@Override
	public float apply(float value) {
		return Math.min(Math.max(value, 0f), 1f);
	}
}
