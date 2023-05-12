package brain.activation;

public class NPCap extends ActivationFunction {
	@Override
	public float apply(float value) {
		return Math.min(Math.max(value, -2f), 2f);
	}
}
