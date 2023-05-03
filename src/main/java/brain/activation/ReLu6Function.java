package brain.activation;

public class ReLu6Function extends ActivationFunction {
	@Override
	public float apply(float value) {
		return Math.min(Math.max(value, 0f), 6f);
	}
}
