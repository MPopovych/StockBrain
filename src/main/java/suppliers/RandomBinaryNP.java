package suppliers;

import java.util.Random;

public class RandomBinaryNP implements ValueSupplier {

	public static final RandomBinaryNP INSTANCE = new RandomBinaryNP(new Random(System.currentTimeMillis()));

	private final Random random;

	public RandomBinaryNP(Random random) {
		this.random = random;
	}

	@Override
	public float supply(int x, int y) {
		return random.nextBoolean() ? -1f : 1f;
	}
}
