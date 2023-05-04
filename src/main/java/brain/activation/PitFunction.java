package brain.activation;

public class PitFunction extends ActivationFunction {
	@Override
	public float apply(float value) {
		return Math.max(value - 1, Math.min(value + 1, 0));
	}
}
