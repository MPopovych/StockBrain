package brain.activation;

public class BinaryStepFunction extends ActivationFunction {
	@Override
	public float apply(float value) {
		return value >= 0.5f ? 1f : 0f;
	}
}
