package brain.suppliers;

import java.util.Random;

public class RandomBinaryNP implements ValueFiller {

	public static final RandomBinaryNP INSTANCE = new RandomBinaryNP(new Random(System.currentTimeMillis()));

	private final Random random;

	public RandomBinaryNP(Random random) {
		this.random = random;
	}

	@Override
	public float supply(int count, int x, int y) {
		return random.nextBoolean() ? -1f : 1f;
	}

	@Override
	public void fill(float[] array) {
		for (int i = 0; i < array.length; i++) {
			array[i] = random.nextBoolean() ? -1f : 1f;
		}
	}
}
