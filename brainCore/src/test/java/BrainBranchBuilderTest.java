import activation.Functions;
import activation.ReLuFunction;
import org.junit.jupiter.api.Test;
import suppliers.RandomRangeSupplier;
import suppliers.RandomSupplier;

import java.util.Arrays;

public class BrainBranchBuilderTest {

    @Test
    public void testBranchCopy() {
        BrainBuilder brainBuilder = BrainBuilder.builder()
                .setFunction(Functions.LeReLu)
                .setInitialSupply(RandomRangeSupplier.INSTANCE)
                .addLayer(54)
                .addLayer(40)
                .addLayer(80)
                .addLayer(60)
                .addLayer(40);

        Brain destination = brainBuilder.build();
        Brain source = brainBuilder.build();

        Brain target = brainBuilder
                .branchDestination(destination)
                .copy(source);

//        Assert.assertEquals(destination, target);
//        Assert.assertNotEquals(source, destination);

        int[] struct1 = destination.getStructure();
        int[] struct2 = source.getStructure();
//        Assert.assertEquals(struct1.length, struct2.length);
        for (int i = 0; i < struct1.length; i++) {
//            Assert.assertEquals(struct1[i], struct2[i]);
        }

        float[] input = new float[54];

        for (int i = 0; i < 54; i++) {
            input[i] = RandomSupplier.INSTANCE.supply(0, 0);
        }

        destination.setInput(input);
        source.setInput(input);

        float[] result1 = destination.calculate();
        float[] result2 = source.calculate();

        System.out.println(" ---- " + Arrays.toString(result1));
        System.out.println(" ---- " + Arrays.toString(result2));
        for (int i = 0; i < result2.length; i++) {
//            Assert.assertEquals(result1[i], result2[i], 0.0f);
        }
    }

    @Test
    public void testBranchBreeding() {
        BrainBuilder brainBuilder = BrainBuilder.builder()
                .setFunction(new ReLuFunction())
                .setInitialSupply(RandomRangeSupplier.INSTANCE)
                .addLayer(54)
                .addLayer(40)
                .addLayer(80)
                .addLayer(60)
                .addLayer(40);

        Brain destination = brainBuilder.build();
        Brain source1 = brainBuilder.build();
        Brain source2 = brainBuilder.build();

        Brain target = brainBuilder
                .branchDestination(destination)
                .produceChild(source1, source2);

//        Assert.assertEquals(destination, target);

        int[] struct1 = destination.getStructure();
        int[] struct2 = source1.getStructure();
//        Assert.assertEquals(struct1.length, struct2.length);
        for (int i = 0; i < struct1.length; i++) {
//            Assert.assertEquals(struct1[i], struct2[i]);
        }
    }

}
