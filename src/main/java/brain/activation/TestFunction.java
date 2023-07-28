package brain.activation;

import static java.lang.Math.max;
import static java.lang.Math.min;

// for tests
public class TestFunction extends ActivationFunction {
	//	private final RevTanhFunction tanh = new RevTanhFunction();
	@Override
	public float apply(float x) {
		// tanh(x - tanh(x)) * 3
		return max(min(max(x - 0.5f, min(x + 0.5f, 0)), 2f), -2f) + x * 0.2f;
//		return tanh.apply(x) * 2f;
	}
}
