import activation.ActivationFunction;
import activation.LeakyReLuFunction;
import org.junit.jupiter.api.Test;
import suppliers.RandomRangeSupplier;
import suppliers.ValueSupplier;

public class BenchmarkTest {

    @Test
    public void testRealisticLoad() {
        //these test results will be compared to C++ counterpart

        ActivationFunction function = new LeakyReLuFunction();
        ValueSupplier supplier = RandomRangeSupplier.INSTANCE;

        Brain brain = new Brain(function, supplier);
        brain.append(30);
        brain.appendBias(30, supplier);
        brain.appendBias(30, supplier);
        brain.appendBias(30, supplier);
        brain.append(15);

        float[] input1 = new float[30];
        float[] output = new float[15];

        long start = System.currentTimeMillis();
        for (int i = 0; i < 3000 * 40; i++) {
            testBrain(brain, input1, output);
        }

        long end = System.currentTimeMillis();

        System.out.println("[testRealisticLoad] Elapsed time: " + (end - start) + "ms.");
    }

    @Test
    public void testCompareJava() {
        //these test results will be compared to C++ counterpart

        ActivationFunction function = new LeakyReLuFunction();
        ValueSupplier supplier = RandomRangeSupplier.INSTANCE;

        Brain brain = new Brain(function, supplier);
        brain.append(4);
        brain.appendBias(8000, supplier);
        brain.appendBias(8000, supplier);
        brain.appendBias(8000, supplier);
        brain.append(2);

        float[] input1 = new float[]{0, 0, 0, 0};
        float[] input2 = new float[]{1, 1, 1, 1};
        float[] input3 = new float[]{1, 0, 0, 1};
        float[] input4 = new float[]{1, 1, 0, 0};
        float[] input5 = new float[]{0, 0, 0, 1};
        float[] input6 = new float[]{0, 0, 1, 1};
        float[] output = new float[]{0, 0};

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            testBrain(brain, input1, output);
            testBrain(brain, input2, output);
            testBrain(brain, input3, output);
            testBrain(brain, input4, output);
            testBrain(brain, input5, output);
            testBrain(brain, input6, output);
        }
        long end = System.currentTimeMillis();

        System.out.println("[testCompareJava] Elapsed time: " + (end - start) + "ms.");
    }

    private void testBrain(Brain brain, float[] input, float[] output) {
        brain.setInput(input);
        brain.calculate(output);
    }
}
