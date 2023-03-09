package brain.suppliers;

import java.util.Random;

public class RandomBinaryZP implements ValueFiller {

	public static final RandomBinaryZP INSTANCE = new RandomBinaryZP(new Random(System.currentTimeMillis()));

	private final Random random;

	public RandomBinaryZP(Random random) {
		this.random = random;
	}

	@Override
	public float supply(int count, int x, int y) {
		return random.nextBoolean() ? 1f : 0f;
	}

	@Override
	public void fill(float[] array) {
		for (int i = 0; i < array.length; i++) {
			array[i] = random.nextBoolean() ? 1f : 0f;
		}
	}
}
