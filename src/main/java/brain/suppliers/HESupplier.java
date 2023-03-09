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
		return (random.nextFloat() * 2f - 1f) * (float) Math.sqrt(2f / count) ;
	}

	@Override
	public void fill(float[] array) {
		float m = (float) Math.sqrt(2f / array.length);
		for (int i = 0; i < array.length; i++) {
			array[i] = (random.nextFloat() * 2f - 1f) * m;
		}
	}


}
