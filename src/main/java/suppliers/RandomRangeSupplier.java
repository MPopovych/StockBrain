package suppliers;

import java.util.Random;

public class RandomRangeSupplier implements ValueSupplier {

    public static final RandomRangeSupplier INSTANCE = new RandomRangeSupplier(new Random(System.currentTimeMillis()));

    private final Random random;

    public RandomRangeSupplier(Random random) {
        this.random = random;
    }

    @Override
    public float supply(int x, int y) {
        return random.nextFloat() * 2f - 1f;
    }
}
