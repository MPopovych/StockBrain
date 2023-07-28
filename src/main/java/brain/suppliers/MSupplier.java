package brain.suppliers;

import java.util.Random;

public class MSupplier implements ValueFiller {

	public static final MSupplier INSTANCE = new MSupplier(new Random(System.currentTimeMillis()));

	private final Random random;

	public MSupplier(Random random) {
		this.random = random;
	}

	@Override
	public float supply(int count, int x, int y) {
		return ((random.nextFloat() * 2f) - 1f) / (count / (float) Math.sqrt(count));
	}

	@Override
	public void fill(float[] array) {
		float m = array.length / (float) Math.sqrt(array.length);
		for (int i = 0; i < array.length; i++) {
			array[i] = ((random.nextFloat() * 2f) - 1f) / m;
		}
	}


}
