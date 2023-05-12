package brain.activation;

public abstract class ActivationFunction {

	public abstract float apply(float value);

	public void applyTo(float[] from, float[] to) {
		for (int i = 0; i < from.length; i++) {
			to[i] = apply(from[i]);
		}
	}

	int sign(float value) {
		if (value >= 0) {
			return 1;
		} else {
			return -1;
		}
	}

}
