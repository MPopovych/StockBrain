package brain.activation;

public class LeakyReLuFunction extends ActivationFunction {
	private static final float LEAK_VALUE = 0.01f;

	@Override
	public float apply(float value) {
		if (value >= 0) {
			return value;
		}

		return LEAK_VALUE * value;
	}
}
