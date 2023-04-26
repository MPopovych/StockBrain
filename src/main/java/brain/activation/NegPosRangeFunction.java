package brain.activation;

public class NegPosRangeFunction implements ActivationFunction {
	@Override
	public float apply(float value) {
		return Math.max(Math.min(value, -1f), 1f);
	}
}
