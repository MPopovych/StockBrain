package brain.activation;

public class NegPosRangeFunction extends ActivationFunction {
	@Override
	public float apply(float value) {
		return Math.min(Math.max(value, -1f), 1f);
	}
}
