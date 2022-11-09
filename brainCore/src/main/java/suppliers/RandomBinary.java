package suppliers;

import java.util.Random;

public class RandomBinary implements ValueSupplier {

    public static final RandomBinary INSTANCE = new RandomBinary(new Random(System.currentTimeMillis()));

    private final Random random;

    public RandomBinary(Random random) {
        this.random = random;
    }

    @Override
    public float supply(int x, int y) {
        return random.nextBoolean() ? 1 : 0;
    }
}
