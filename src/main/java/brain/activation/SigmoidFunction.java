package brain.activation;

// for tests
public class SigmoidFunction implements ActivationFunction {
	@Override
	public float apply(float value) {
		return (float) (1.0 / (1 + Math.exp(-value)));
	}
}
