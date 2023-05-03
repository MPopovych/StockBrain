package brain.activation;

public class ReverseReLuFunction extends ActivationFunction {
	@Override
	public float apply(float value) {
		return Math.max(-value, 0);
	}
}
