package brain.suppliers;

import java.util.Random;

public class RandomRangeSupplier implements ValueFiller {

	public static final RandomRangeSupplier INSTANCE = new RandomRangeSupplier(new Random(System.currentTimeMillis()));

	private final Random random;

	public RandomRangeSupplier(Random random) {
		this.random = random;
	}

	@Override
	public float supply(int count, int x, int y) {
		return random.nextFloat() * 2f - 1f;
	}

	@Override
	public void fill(float[] array) {
		for (int i = 0; i < array.length; i++) {
			array[i] = random.nextFloat() * 2f - 1f;
		}
	}
}
