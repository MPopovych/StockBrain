package suppliers;

import java.util.Random;

public class RandomBinaryZP implements ValueSupplier {

    public static final RandomBinaryZP INSTANCE = new RandomBinaryZP(new Random(System.currentTimeMillis()));

    private final Random random;

    public RandomBinaryZP(Random random) {
        this.random = random;
    }

    @Override
    public float supply(int x, int y) {
        return random.nextBoolean() ? 1 : 0;
    }
}
