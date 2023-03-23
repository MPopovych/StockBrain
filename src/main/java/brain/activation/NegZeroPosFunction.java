package brain.activation;

public class NegZeroPosFunction implements ActivationFunction {
	@Override
	public float apply(float value) {
		return value >= 1 ? 1 : (value <= -1 ? -1 : 0);
	}
}
