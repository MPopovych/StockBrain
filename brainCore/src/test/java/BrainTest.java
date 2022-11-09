import activation.ReLuFunction;
import org.junit.jupiter.api.Test;
import suppliers.RandomRangeSupplier;
import suppliers.RandomSupplier;
import suppliers.ConstSupplier;

import java.util.Arrays;

public class BrainTest {

    @Test
    public void test() {
        long start = System.currentTimeMillis();

        int[] structureRef = new int[]{53, 200, 80, 50, 100, 50, 5};

        BrainBuilder brainBuilder = BrainBuilder.builder()
                .setFunction(new ReLuFunction())
                .setInitialSupply(RandomRangeSupplier.INSTANCE);

        for (int count : structureRef) {
            brainBuilder.addLayer(count);
        }

        Brain brain = brainBuilder.build();

        System.out.println("struct ---");
        int[] struct = brain.getStructure();
        System.out.println(Arrays.toString(struct));
        System.out.println(" --- end struct");

//        Assert.assertEquals(struct.length, structureRef.length);
//        for (int i = 0; i < struct.length; i++) {
//            Assert.assertEquals(struct[i], structureRef[i]);
//        }

//        Assert.assertEquals(brain.getLayerCount(), structureRef.length);
//        Assert.assertEquals(brain.getWeightLayerCount(), structureRef.length - 1);

        BrainLayer first = brain.getLayer(0);

//        Assert.assertNotNull(first);
//        Assert.assertEquals(53, first.getNodeCount());

        BrainLayer firstWeight = brain.getWeightLayer(0);
//        Assert.assertNotNull(firstWeight);
//        Assert.assertEquals(53 * 200, firstWeight.getNodeCount());

        for (int i = 0; i < 10; i++) {
            brain.setInput(RandomSupplier.INSTANCE);
            float[] result = brain.calculate();

            System.out.print("result " + i + "--- ");
            for (float v : result) {
                System.out.print(v + " ");
            }
            System.out.println(" --- end result");
        }

        long end = System.currentTimeMillis();

        System.out.println("Elapsed time: " + (end - start));
    }

    @Test
    public void testConsistency() {
        long start = System.currentTimeMillis();

        Brain brain = BrainBuilder.builder()
                .setFunction(new ReLuFunction())
                .setInitialSupply(RandomRangeSupplier.INSTANCE)
                .addLayer(53)
                .addLayer(200)
                .addLayer(80)
                .addLayer(50)
                .addLayer(100)
                .addLayer(50)
                .addLayer(5)
                .build();

        float[] input = new float[53];

        for (int i = 0; i < 53; i++) {
            input[i] = RandomSupplier.INSTANCE.supply(0, 0);
        }

        brain.setInput(input);
        float[] result1 = brain.calculate();
        float[] result2 = brain.calculate();

//        for (int i = 0; i < result2.length; i++) {
//            Assert.assertEquals(result1[i], result2[i], 0.0f);
//        }

        long end = System.currentTimeMillis();

        System.out.println("[testConsistency] Elapsed time: " + (end - start));
    }

    @Test
    public void testSpeed() {
        long start = System.currentTimeMillis();

        Brain brain = BrainBuilder.builder()
                .setFunction(new ReLuFunction())
                .setInitialSupply(RandomRangeSupplier.INSTANCE)
                .addLayer(53)
                .addLayer(200)
                .addLayer(80)
                .addLayer(50)
                .addLayer(100)
                .addLayer(50)
                .addLayer(5)
                .build();

        for (int i = 0; i < 10; i++) {
            brain.setInput(RandomSupplier.INSTANCE);
            float[] result = brain.calculate();
//
//            System.out.print("result " + i + "--- ");
//            for (float v : result) {
//                System.out.print(v + " ");
//            }
//            System.out.println(" --- end result");
        }

        long end = System.currentTimeMillis();

        System.out.println("[testSpeed] Elapsed time: " + (end - start));
    }

    @Test
    public void testMerge() {
        long start = System.currentTimeMillis();

        BrainBuilder brainBuilder = BrainBuilder.builder()
                .setFunction(new ReLuFunction())
                .addLayer(3)
                .addLayer(5)
                .addLayer(2);

        Brain brain1 = brainBuilder
                .setInitialSupply(new ConstSupplier(3))
                .build();
        Brain brain2 = brainBuilder
                .setInitialSupply(new ConstSupplier(4))
                .build();

        int[] struct1 = brain1.getStructure();
        int[] struct2 = brain2.getStructure();

//        Assert.assertEquals(struct1.length, struct2.length);
        for (int i = 0; i < struct1.length; i++) {
//            Assert.assertEquals(struct1[i], struct2[i]);
        }

        Brain child = brainBuilder.produceChild(brain1, brain2);

        int[] struct3 = child.getStructure();

//        Assert.assertEquals(struct1.length, struct3.length);
        for (int i = 0; i < struct1.length; i++) {
//            Assert.assertEquals(struct1[i], struct3[i]);
        }

        for (int i = 0; i < child.getWeightLayerCount(); i++) {
            System.out.println("PARENT 1:");
            brain1.getWeightLayer(i).print();
            System.out.println("PARENT 2:");
            brain2.getWeightLayer(i).print();
            System.out.println("CHILD:");
            child.getWeightLayer(i).print();
        }

        long end = System.currentTimeMillis();

        System.out.println("[testMerge] Elapsed time: " + (end - start));
    }

    @Test
    public void testValueMerge() {
        long start = System.currentTimeMillis();

        BrainBuilder brainBuilder = BrainBuilder.builder()
                .setFunction(new ReLuFunction())
                .setInitialSupply(RandomRangeSupplier.INSTANCE)
                .addLayer(53)
                .addLayer(50)
                .addLayer(50)
                .addLayer(5);

        Brain brain1 = brainBuilder
                .build();
        Brain brain2 = brainBuilder
                .build();

        Brain brain3 = brainBuilder.produceChild(brain1, brain2);

        float[] input = new float[53];

        for (int i = 0; i < 53; i++) {
            input[i] = RandomSupplier.INSTANCE.supply(0, 0);
        }

        System.out.println(Arrays.toString(input));

        for (int i = 0; i < 1; i++) {
            brain1.setInput(input);
            brain2.setInput(input);
            brain3.setInput(input);
            float[] result1 = brain1.calculate();
            float[] result2 = brain2.calculate();
            float[] result3 = brain3.calculate();

            brain1.getLayer(0).print();
            brain2.getLayer(0).print();
            brain3.getLayer(0).print();

            System.out.print("result 1" + "--- ");
            for (float item : result1) {
                System.out.print(item + " ");
            }
            System.out.println(" --- end result");

            System.out.print("result 2" + "--- ");
            for (float value : result2) {
                System.out.print(value + " ");
            }
            System.out.println(" --- end result");

            System.out.print("result 3" + "--- ");
            for (float v : result3) {
                System.out.print(v + " ");
            }
            System.out.println(" --- end result");
        }

        long end = System.currentTimeMillis();

        System.out.println("[testValueMerge] Elapsed time: " + (end - start));
    }
}
