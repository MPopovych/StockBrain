package brain.suppliers;

import java.util.Random;

public class HESupplier implements ValueFiller {

	public static final HESupplier INSTANCE = new HESupplier(new Random(System.currentTimeMillis()));

	private final Random random;

	public HESupplier(Random random) {
		this.random = random;
	}

	@Override
	public float supply(int count, int x, int y) {
		return (float) random.nextGaussian();
	}

	@Override
	public void fill(float[] array) {
		for (int i = 0; i < array.length; i++) {
			array[i] = (float) random.nextGaussian() / (2 * array.length);
		}
	}


}
