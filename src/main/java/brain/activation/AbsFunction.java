package brain.activation;

public class AbsFunction extends ActivationFunction {
	@Override
	public float apply(float value) {
		return Math.abs(value);
	}
}
