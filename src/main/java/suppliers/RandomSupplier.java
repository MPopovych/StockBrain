package suppliers;

import java.util.Random;

public class RandomSupplier implements ValueSupplier {

	public static final RandomSupplier INSTANCE = new RandomSupplier(new Random(System.currentTimeMillis()));

	private final Random random;

	public RandomSupplier(Random random) {
		this.random = random;
	}

	@Override
	public float supply(int x, int y) {
		return random.nextFloat();
	}
}
