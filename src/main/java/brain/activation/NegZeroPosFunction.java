package brain.activation;

public class NegZeroPosFunction implements ActivationFunction {
	@Override
	public float apply(float value) {
		return value > 0.99 ? 1 : (value < -0.99 ? -1 : 0);
	}
}
