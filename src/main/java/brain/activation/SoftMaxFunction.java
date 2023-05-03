package brain.activation;

public class SoftMaxFunction extends ActivationFunction {
	@Override
	public float apply(float value) {
		throw new IllegalStateException();
	}

	@Override
	void applyTo(float[] array) {
		for (int i = 0; i < array.length; i++) {
			array[i] = (float) Math.exp(array[i]);
		}
		float sum = 0;
		for (float v : array) {
			sum += v;
		}
		for (int i = 0; i < array.length; i++) {
			array[i] = array[i] / sum;
		}
	}
}
