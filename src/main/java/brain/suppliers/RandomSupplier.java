package brain.suppliers;

import java.util.Random;

public class RandomSupplier implements ValueFiller {

	public static final RandomSupplier INSTANCE = new RandomSupplier(new Random(System.currentTimeMillis()));

	private final Random random;

	public RandomSupplier(Random random) {
		this.random = random;
	}

	@Override
	public float supply(int count, int x, int y) {
		return random.nextFloat();
	}

	@Override
	public void fill(float[] array) {
		for (int i = 0; i < array.length; i++) {
			array[i] = random.nextFloat();
		}
	}
}
