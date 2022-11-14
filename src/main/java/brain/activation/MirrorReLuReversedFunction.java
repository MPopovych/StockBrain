package brain.activation;

public class MirrorReLuReversedFunction implements ActivationFunction {
	@Override
	public float apply(float value) {
		if (value > 1f) return 0f;
		if (value < -1f) return 0f;
		return value;
	}
}
