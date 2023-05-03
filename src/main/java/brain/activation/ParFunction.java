package brain.activation;

public class ParFunction extends ActivationFunction {

	@Override
	public float apply(float value) {
		if (value >= 0) {
			return value;
		}

		return value / 3;
	}
}
