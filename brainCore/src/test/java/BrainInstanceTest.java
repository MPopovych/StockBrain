import activation.ReLuFunction;
import org.junit.jupiter.api.Test;
import suppliers.RandomRangeSupplier;
import suppliers.RandomSupplier;

import java.util.Arrays;

public class BrainInstanceTest {

    @Test
    public void testSave() {
        BrainBuilder template = BrainBuilder.builder()
                .setFunction(new ReLuFunction())
                .setInitialSupply(RandomRangeSupplier.INSTANCE)
                .addLayer(54)
                .addLayer(40)
                .addLayer(80)
                .addLayer(60)
                .addLayer(40);
        Brain brainSource = template.build();
        Brain brainResult = template.build();

        String signature1 = BrainInstanceManager.getSignature(brainSource);
//        Assert.assertFalse(signature1.isEmpty());

//        System.out.println("SIGNATURE: " + signature1);

        BrainInstanceManager.loadFromSignature(signature1, brainResult);
        String signature2 = BrainInstanceManager.getSignature(brainResult);

//        Assert.assertEquals(signature1, signature2);

        int[] struct1 = brainSource.getStructure();
        int[] struct2 = brainResult.getStructure();
//        Assert.assertEquals(struct1.length, struct2.length);
        for (int i = 0; i < struct1.length; i++) {
//            Assert.assertEquals(struct1[i], struct2[i]);
        }

        float[] input = new float[54];

        for (int i = 0; i < 54; i++) {
            input[i] = RandomSupplier.INSTANCE.supply(0, 0);
        }

        brainSource.setInput(input);
        brainResult.setInput(input);

        float[] result1 = brainSource.calculate();
        float[] result2 = brainResult.calculate();

        System.out.println(" ---- " + Arrays.toString(result1));
        System.out.println(" ---- " + Arrays.toString(result2));
        for (int i = 0; i < result2.length; i++) {
//            Assert.assertEquals(result1[i], result2[i], 0.0f);
        }
    }

    @Test
    public void testSaveBiases() {
        BrainBuilder template = BrainBuilder.builder()
                .setFunction(new ReLuFunction())
                .setInitialSupply(RandomRangeSupplier.INSTANCE)
                .addLayer(54)
                .addLayerBias(40, RandomRangeSupplier.INSTANCE)
                .addLayerBias(80, RandomRangeSupplier.INSTANCE)
                .addLayer(60)
                .addLayer(40);
        Brain brainSource = template.build();
        Brain brainLoaded = template.build();

        String signature1 = BrainInstanceManager.getSignature(brainSource);
//        Assert.assertFalse(signature1.isEmpty());

        System.out.println("SIGNATURE: " + signature1);

        BrainInstanceManager.loadFromSignature(signature1, brainLoaded);
        String signature2 = BrainInstanceManager.getSignature(brainLoaded);

//        Assert.assertEquals(signature1, signature2);

        int[] struct1 = brainSource.getStructure();
        int[] struct2 = brainLoaded.getStructure();
//        Assert.assertEquals(struct1.length, struct2.length);
        for (int i = 0; i < struct1.length; i++) {
//            Assert.assertEquals(struct1[i], struct2[i]);
        }

        float[] input = new float[54];

        for (int i = 0; i < 53; i++) {
            input[i] = RandomSupplier.INSTANCE.supply(0, 0);
        }

        brainSource.setInput(input);
        brainLoaded.setInput(input);

        float[] result1 = brainSource.calculate();
        float[] result2 = brainLoaded.calculate();


        System.out.println(" ---- " + Arrays.toString(result1));
        System.out.println(" ---- " + Arrays.toString(result2));
        for (int i = 0; i < result2.length; i++) {
//            Assert.assertEquals(result1[i], result2[i], 0.0f);
        }
    }

}
