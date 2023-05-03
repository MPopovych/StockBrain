package brain.activation;

// for tests
public class TanhFunction extends ActivationFunction {

	@Override
	public float apply(float value) {
		return (float) Math.tanh(value);
	}
}
