package brain.activation;

public class PitFunction implements ActivationFunction {
	@Override
	public float apply(float value) {
		return Math.min((Math.max(Math.max((value * 3f) - 2.5f, value / 3f - 0.5f) + 0.5f, value / 10f)), 1f + value / 10f - 0.1f);
	}
}
