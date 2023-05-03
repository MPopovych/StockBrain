package brain.activation;

public abstract class ActivationFunction {

	abstract float apply(float value);

	void applyTo(float[] array) {
		for (int i = 0; i < array.length; i++) {
			array[i] = apply(array[i]);
		}
	}

}
