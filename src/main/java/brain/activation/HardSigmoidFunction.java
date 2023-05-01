package brain.activation;

// for tests
public class HardSigmoidFunction implements ActivationFunction {
	@Override
	public float apply(float value) {
		return (value / (1 + Math.abs(value)) + 1f) * 0.5f;
	}
}
