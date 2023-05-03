package brain.activation;

public class BinMaxFunction extends ActivationFunction {
	@Override
	public float apply(float value) {
		throw new IllegalStateException();
	}

	@Override
	void applyTo(float[] array) {
		int maxIndex = -1;
		float maxValue = Float.MIN_VALUE;
		for (int i = 0; i < array.length; i++) {
			float v = array[i];
			if (v > maxValue) {
				maxIndex = i;
				maxValue = v;
			} else if (v == maxValue) {
				maxIndex = -1;
				maxValue = v;
			}
		}
		for (int i = 0; i < array.length; i++) {
			if (i == maxIndex) {
				array[i] = 1f;
			} else {
				array[i] = 0f;
			}
		}
	}
}
