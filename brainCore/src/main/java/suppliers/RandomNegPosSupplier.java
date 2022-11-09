package suppliers;

import java.util.Random;

public class RandomNegPosSupplier implements ValueSupplier {

    public static final RandomNegPosSupplier INSTANCE = new RandomNegPosSupplier(new Random(System.currentTimeMillis()));

    private final Random random;

    public RandomNegPosSupplier(Random random) {
        this.random = random;
    }

    @Override
    public float supply(int x, int y) {
        return random.nextBoolean() ? 1 : -1;
    }
}
