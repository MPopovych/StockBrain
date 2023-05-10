package brain.activation;

public class PitFunction extends ActivationFunction {
	@Override
	public float apply(float value) {
		return Math.max(value - 0.333f, Math.min(value + 0.333f, 0));
	}
}
