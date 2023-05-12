package brain.activation;

public class NormPeakFunction extends ActivationFunction {
	@Override
	public float apply(float value) {
		return (float) Math.exp(-Math.pow(value, 2) * 24);
	}
}
