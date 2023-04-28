package brain.activation;

public class NormPeakFunction implements ActivationFunction {
	@Override
	public float apply(float value) {
		return Math.max(0f, (-value * 6f) * (value - 1f));
	}
}
