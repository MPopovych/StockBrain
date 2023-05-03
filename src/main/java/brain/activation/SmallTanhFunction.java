package brain.activation;

// for tests
public class SmallTanhFunction extends ActivationFunction {
	private final FastTanhFunction tanh = new FastTanhFunction();
	@Override
	public float apply(float value) {
		return tanh.apply(value * 2.5f);
	}
}
