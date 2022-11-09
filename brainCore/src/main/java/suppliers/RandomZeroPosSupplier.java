package suppliers;

import java.util.Random;

public class RandomZeroPosSupplier implements ValueSupplier {

    public static final RandomZeroPosSupplier INSTANCE = new RandomZeroPosSupplier(new Random(System.currentTimeMillis()));

    private final Random random;

    public RandomZeroPosSupplier(Random random) {
        this.random = random;
    }

    @Override
    public float supply(int x, int y) {
        return random.nextBoolean() ? 1 : 0;
    }
}
