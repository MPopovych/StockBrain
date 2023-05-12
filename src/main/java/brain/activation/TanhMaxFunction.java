package brain.activation;

public class TanhMaxFunction extends ActivationFunction {

	private final SoftMaxFunction softMaxFunction = new SoftMaxFunction();

	@Override
	public float apply(float value) {
		throw new IllegalStateException();
	}

	@Override
	public void applyTo(float[] from, float[] to) {
		softMaxFunction.applyTo(from, to);
		for (int i = 0; i < to.length; i++) {
			to[i] = (to[i] * 2f) - 1f;
		}
	}
}
