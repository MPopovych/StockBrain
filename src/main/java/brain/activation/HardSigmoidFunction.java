package brain.activation;

// for tests
public class HardSigmoidFunction implements ActivationFunction {
	@Override
	public float apply(float value) {
		if (value < -2.5f) return 0f;
		if (value > 2.5f) return 1f;
		return 0.2f * value + 0.5f;
	}
}
