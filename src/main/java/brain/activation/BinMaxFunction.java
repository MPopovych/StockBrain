package brain.activation;

public class BinMaxFunction extends ActivationFunction {
	@Override
	public float apply(float value) {
		throw new IllegalStateException();
	}

	@Override
	public void applyTo(float[] from, float[] to) {
		int maxIndex = 0;
		float maxValue = from[0];
		for (int i = 0; i < from.length; i++) {
			float v = from[i];
			if (v > maxValue) {
				maxIndex = i;
				maxValue = v;
			}
		}
		for (int i = 0; i < from.length; i++) {
			if (i == maxIndex) {
				to[i] = 1f;
			} else {
				to[i] = 0f;
			}
		}
	}
}
