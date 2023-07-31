package brain.activation;

// for tests
public class SmallSigmoidFunction extends ActivationFunction {
	@Override
	public float apply(float value) {
		return (3 * value) / (1f + Math.abs(3 + value));
	}
}
