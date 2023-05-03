package brain.activation;

public class BinaryRangeFunction extends ActivationFunction {
	@Override
	public float apply(float value) {
		return value > 0f ? value <= 1f ? 1f : 0f : 0f;
	}
}
