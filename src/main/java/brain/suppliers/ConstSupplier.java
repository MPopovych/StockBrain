package brain.suppliers;

import java.util.Arrays;

public class ConstSupplier implements ValueFiller {

	private final float value;

	public ConstSupplier(float value) {
		this.value = value;
	}

	@Override
	public float supply(int count, int x, int y) {
		return value;
	}

	@Override
	public void fill(float[] array) {
		Arrays.fill(array, value);
	}
}
