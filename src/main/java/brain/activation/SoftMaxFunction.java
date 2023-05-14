package brain.activation;

import java.util.Arrays;

public class SoftMaxFunction extends ActivationFunction {
	@Override
	public float apply(float value) {
		throw new IllegalStateException();
	}

	@Override
	public void applyTo(float[] from, float[] to) {
		float max = from[0];
		for (float value : from) {
			max = Math.max(value, max);
		}
		float sum = 0;

		for (int i = 0; i < from.length; i++) {
			float value = (float) Math.exp(from[i] - max);
			to[i] = value;
			sum += value;
		}
		if (sum == 0f) {
			Arrays.fill(to, 0f);
			return;
		}
		for (int i = 0; i < from.length; i++) {
			to[i] = from[i] / sum;
		}
	}
}
