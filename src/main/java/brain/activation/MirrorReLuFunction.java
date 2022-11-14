package brain.activation;

public class MirrorReLuFunction implements ActivationFunction {
	@Override
	public float apply(float value) {
		if (value > 1f) return value - 1f;
		if (value < -1f) return value + 1f;
		return 0f;
	}
}
