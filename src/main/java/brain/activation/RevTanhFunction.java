package brain.activation;

// for tests
public class RevTanhFunction extends ActivationFunction {
	private final FastTanhFunction tanh = new FastTanhFunction();
	@Override
	public float apply(float value) {
		return tanh.apply((value * value * value) * 2) * sign(value);
	}
}
