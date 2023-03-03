package brain.suppliers;

import java.util.Random;

public class HESupplier implements ValueSupplier {

	public static final HESupplier INSTANCE = new HESupplier(new Random(System.currentTimeMillis()));

	private final Random random;

	public HESupplier(Random random) {
		this.random = random;
	}

	@Override
	public float supply(int count, int x, int y) {

		return (random.nextFloat() * 2f - 1f) * (float) Math.sqrt(2f / count) ;
	}
}
