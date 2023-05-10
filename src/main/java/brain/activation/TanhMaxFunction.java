package brain.activation;

public class TanhMaxFunction extends ActivationFunction {
	@Override
	public float apply(float value) {
		throw new IllegalStateException();
	}

	@Override
	void applyTo(float[] array) {
		float max = Float.MIN_VALUE;
		for (float value : array) {
			max = Math.max(value, max);
		}
		float sum = 0;
		for (int i = 0; i < array.length; i++) {
			float value = (float) Math.exp(array[i] - max);
			array[i] = value;
			sum += value;
		}
		if (sum == 0f) {
			sum = 0.00001f;
		}
		for (int i = 0; i < array.length; i++) {
			array[i] = ((array[i] / sum) * 2f) - 1f;
		}
	}
}
