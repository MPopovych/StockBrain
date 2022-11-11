package suppliers;

import java.util.Random;

public class RandomNegZeroPosSupplier implements ValueSupplier {

    public static final RandomNegZeroPosSupplier INSTANCE = new RandomNegZeroPosSupplier(new Random(System.currentTimeMillis()));

    private final Random random;

    public RandomNegZeroPosSupplier(Random random) {
        this.random = random;
    }

    @Override
    public float supply(int x, int y) {
        return random.nextInt(3) - 1;
    }
}
