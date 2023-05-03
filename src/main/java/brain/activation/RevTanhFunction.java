package brain.activation;

// for tests
public class RevTanhFunction extends ActivationFunction {
	private final FastTanhFunction tanh = new FastTanhFunction();
	@Override
	public float apply(float value) {
		return tanh.apply((value / 2.5f) * (value / 2.5f)) * (Math.abs(value) / value);
	}
}
