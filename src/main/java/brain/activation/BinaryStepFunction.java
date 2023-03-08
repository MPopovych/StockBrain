package brain.activation;

public class BinaryStepFunction implements ActivationFunction {
	@Override
	public float apply(float value) {
		return value > 0f ? 1f : 0f;
	}
}
