package brain.activation;

// for tests
public class SigmoidFunction implements ActivationFunction {
	@Override
	public float apply(float value) {
		return (1.0f / (1f + (float) Math.exp(-value)));
	}
}
