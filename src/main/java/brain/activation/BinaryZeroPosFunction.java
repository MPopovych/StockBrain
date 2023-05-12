package brain.activation;

public class BinaryZeroPosFunction extends ActivationFunction {
	@Override
	public float apply(float value) {
		return value > 0 ? 1f : 0f;
	}
}
