
import org.junit.jupiter.api.Test;

import java.util.Random;

public class BrainLayerTest {

    @Test
    public void testOutput1() {
        long start = System.currentTimeMillis();

        float[] output = new float[]{114, 114};
        BrainLayer a = new BrainLayer(6, 1, new float[]{1, 2, 0, 7, 0, 7});
        BrainLayer b = new BrainLayer(2, 6, new float[]{4, 6, 6, 7, 0, 7, 4, 6, 6, 7, 0, 7});
        BrainLayer c = new BrainLayer(2, 1);

        c.setToZeroes();
        a.multiply(b, c);

//        for (int i = 0; i < output.length; i++) {
//            Assert.assertEquals(c.getValue(i, 0), output[i], 0.0);
//        }
        c.print();

        long end = System.currentTimeMillis();

        System.out.println("[testOutput1] Elapsed time: " + (end - start) + "ms");
    }

    @Test
    public void testOutput2() {
        long start = System.currentTimeMillis();
        Random random = new Random(start);

        float[] input = new float[5];

        BrainLayer a = new BrainLayer(5, 1);
        BrainLayer b = new BrainLayer(5, 5, new float[]{-1, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0,-1, 0, 0, 0, 0, 0,-1, 0, 0, 0, 0, 0,-1, 0});
        BrainLayer c = new BrainLayer(5, 1);
        for (int j = 0; j < 20; j ++) {
            for (int i = 0; i < input.length; i++) {
                input[i] = random.nextInt(3) - 1;
            }

            a.setValues(input);
            c.setToZeroes();
            a.multiply(b, c);

//            for (int i = 0; i < c.getNodeCount(); i++) {
//                Assert.assertEquals(c.getValue(i, 0), input[i] * -1, 0.0);
//            }
        }

        a.print();
        c.print();

        long end = System.currentTimeMillis();

        System.out.println("[testOutput2] Elapsed time: " + (end - start) + "ms");
    }


    @Test
    public void performanceTest1() {
        long start = System.currentTimeMillis();
        BrainLayer a = new BrainLayer(3, 3, new float[]{1, 1, 1, 2, 2, 2, 3, 3, 3});
        BrainLayer b = new BrainLayer(3, 3, new float[]{1, 1, 1, 2, 2, 2, 3, 3, 3});
        BrainLayer c = new BrainLayer(3, 3, false);

        for (int i = 0; i < 4000000; i++) {
            c.setToZeroes();
            a.multiply(b, c);
        }

        long end = System.currentTimeMillis();

        System.out.println("[performanceTest1] Elapsed time: " + (end - start));
    }

    @Test
    public void performanceTest2() {
        long start = System.currentTimeMillis();

        for (int i = 0; i < 4000000; i++) {
            BrainLayer a = new BrainLayer(2, 3, new float[]{5f, 4f, 6f, 1f, 7f, 7f});
            BrainLayer b = new BrainLayer(1, 2, new float[]{2f, 2f});
            BrainLayer c = new BrainLayer(1, 3);

            c.setToZeroes();
            a.multiply(b, c);
        }

        long end = System.currentTimeMillis();

        System.out.println("[performanceTest2] Elapsed time: " + (end - start));
    }

    @Test
    public void performanceTest3() {
        long start = System.currentTimeMillis();

        for (int i = 0; i < 4000000; i++) {
            BrainLayer a = new BrainLayer(3, 1, new float[]{5f, 4f, 6f});
            BrainLayer b = new BrainLayer(3, 3, new float[]{5f, 4f, 6f, 1f, 7f, 7f, 5f, 4f, 6f});
            BrainLayer c = new BrainLayer(3, 1);

            c.setToZeroes();
            a.multiply(b, c);
        }

        long end = System.currentTimeMillis();

        System.out.println("[performanceTest3] Elapsed time: " + (end - start));
    }

}
