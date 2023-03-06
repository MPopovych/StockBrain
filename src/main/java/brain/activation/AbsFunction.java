package brain.activation;

public class AbsFunction implements ActivationFunction {
	@Override
	public float apply(float value) {
		return Math.abs(value);
	}
}
