

import activation.Functions;
import org.junit.jupiter.api.Test;
import suppliers.RandomNegZeroPosSupplier;
import suppliers.ZeroSupplier;

import java.util.Arrays;
import java.util.Random;

public class BrainTrainingTest {

    private static final int REVERSE_BRAIN_COUNT = 30;
    private static final int REVERSE_IO_COUNT = 12;
    private static final int REVERSE_L2_COUNT = REVERSE_IO_COUNT;
    private static final int REVERSE_L3_COUNT = REVERSE_IO_COUNT;
    private static final int REVERSE_L4_COUNT = REVERSE_IO_COUNT;
    private static final int REVERSE_TEST_COUNT = 1;

    /**
     * This test is just a brute force test
     */
    @Test
    public void testReverse() {
        int testCount = REVERSE_TEST_COUNT;
        long total = 0;

        long start = System.currentTimeMillis();
        for (int i = 0; i < testCount; i++) {
            total += testReverseOnce();
        }
        long end = System.currentTimeMillis();
        System.out.println("[testReverseOnce] Avg time: " + total / testCount + "ms.");
        System.out.println("[testReverse] Elapsed time: " + (end - start) + "ms.");
    }


    @Test
    public void testReverseDataSet() {
        int testCount = REVERSE_TEST_COUNT;
        long total = 0;

        long start = System.currentTimeMillis();
        for (int i = 0; i < testCount; i++) {
            total += testReverseDataSetOnce();
        }
        long end = System.currentTimeMillis();
        System.out.println("[testReverseOnce] Avg time: " + total / testCount + "ms.");
        System.out.println("[testReverse] Elapsed time: " + (end - start) + "ms.");
    }

    public long testReverseOnce() {
        Random random = new Random(System.currentTimeMillis());
        Brain[] brainPool = new Brain[REVERSE_BRAIN_COUNT];
        int[] brainResults = new int[REVERSE_BRAIN_COUNT];

        BrainBuilder template = BrainBuilder.builder()
                .setFunction(Functions.NegZeroPos)
                .addLayer(REVERSE_IO_COUNT)
                .addLayerBias(REVERSE_L2_COUNT, RandomNegZeroPosSupplier.INSTANCE)
                .addLayerBias(REVERSE_L3_COUNT, RandomNegZeroPosSupplier.INSTANCE)
                .addLayer(REVERSE_L4_COUNT, Functions.NegPos)
                .addLayer(REVERSE_IO_COUNT, Functions.NegZeroPos);

        for (int i = 0; i < brainPool.length; i++) {
            brainPool[i] = template.build();
        }

        float[] input = new float[REVERSE_IO_COUNT];
        for (int i = 0; i < input.length; i++) {
            input[i] = random.nextBoolean() ? -1f : 1f;
        }
        System.out.println("INPUT ---- " + Arrays.toString(input));

        float[] output = new float[REVERSE_IO_COUNT];
        int bestGlobalResult = 0;

        long start = System.currentTimeMillis();
        long iteration = 0;
        while (bestGlobalResult < REVERSE_IO_COUNT) {
            //iterate brains and get results
            for (int b = 0; b < brainPool.length; b++) {
                Brain brain = brainPool[b];
                brain.setInput(input);
                brain.calculate(output);

                int matched = 0;
                for (int r = 0; r < input.length; r++) {
                    if (input[r] == output[r] * -1) {
                        matched++;
                    }
                }
                brainResults[b] = matched;
            }

            //find 1 best result
            int bestIndex = 0;
            int secondBestIndex = 0;
            int bestResult = 0;
            for (int b = 0; b < brainResults.length; b++) {
                if (brainResults[b] > bestResult) {
                    bestIndex = b;
                    bestResult = brainResults[b];
                    if (bestResult > bestGlobalResult) {
                        bestGlobalResult = bestResult;
                    }
                } else if (brainResults[b] == bestResult) {
                    secondBestIndex = b;
                }
            }
            if (bestGlobalResult == REVERSE_IO_COUNT) break;

            Brain bestBrain = brainPool[bestIndex];
            int lastNotBest = 0;
            for (int b = 0; b < brainPool.length; b++) {
                if (bestIndex == b) {
                    continue;
                }

                template.branchDestination(brainPool[b])
                        .copy(bestBrain, REVERSE_IO_COUNT, bestGlobalResult); //the closer to the goal - the less mutation
                if (secondBestIndex != b) {
                    lastNotBest = b;
                }
            }

            // this one to ensure a good start with low mutation, so called fresh blood
            brainPool[lastNotBest] = template.build();
            if (iteration % 5000 == 0) {
                System.out.println("[test iterate] Best so far: " + bestGlobalResult + " best now: " + bestResult);
            }
            iteration++;
        }
        long end = System.currentTimeMillis();
        return (end - start);
    }

    public long testReverseDataSetOnce() {
        Random random = new Random(System.currentTimeMillis());
        Brain[] brainPool = new Brain[REVERSE_BRAIN_COUNT];
        Brain[] cache1 = new Brain[REVERSE_BRAIN_COUNT];
        Brain[] cache2 = new Brain[REVERSE_BRAIN_COUNT];

        int[] brainResults = new int[REVERSE_BRAIN_COUNT];

        BrainBuilder template = BrainBuilder.builder()
                .setFunction(Functions.NegZeroPos)
                .setInitialSupply(ZeroSupplier.INSTANCE)
//                .setInitialSupply(RandomNegZeroPosSupplier.INSTANCE) // this should take a little bit longer
                .setMutationSupply(RandomNegZeroPosSupplier.INSTANCE)
                .addLayer(REVERSE_IO_COUNT)
                .addLayer(REVERSE_IO_COUNT);

        for (int i = 0; i < brainPool.length; i++) {
            cache1[i] = template.build();
            cache2[i] = template.build();
        }

        brainPool = cache1;

        float[] input = new float[REVERSE_IO_COUNT];
        float[] negatedInput = new float[REVERSE_IO_COUNT];
        for (int i = 0; i < input.length; i++) {
            input[i] = random.nextBoolean() ? -1f : 1f;
            negatedInput[i] = input[i] * -1;
        }

        System.out.println("INPUT  ---- " + Arrays.toString(input));
        System.out.println("INPUT REVERSE ---- " + Arrays.toString(negatedInput));

        float[] output = new float[REVERSE_IO_COUNT];
        float bestGlobalResult = 0;

        int validationRepeat = 50;
        long start = System.currentTimeMillis();
        long iteration = 0;
        float resultSum = 0;
        while (bestGlobalResult < REVERSE_IO_COUNT) {
            //clear previous
            Arrays.fill(brainResults, 0);

            //iterate brains on dataset and sum results
            for (int d = 0; d < validationRepeat; d++) {
                for (int i = 0; i < input.length; i++) {
                    input[i] = random.nextBoolean() ? 1f : -1f;
                    negatedInput[i] = input[i] * -1;
                }

                for (int b = 0; b < brainPool.length; b++) {
                    Brain brain = brainPool[b];
                    brain.setInput(input);
                    brain.calculate(output);

                    int matched = 0;
                    for (int r = 0; r < input.length; r++) {
                        if (negatedInput[r] == output[r]) {
                            matched++;
                        }
                    }
                    brainResults[b] += matched;
                }
            }

            //find 1 best result
            int bestIndex = 0;
            float bestResult = 0;
            for (int b = 0; b < brainResults.length; b++) {
                int avgResult = brainResults[b];
                if (avgResult > bestResult) {
                    bestIndex = b;
                    bestResult = avgResult;
                    float avg = bestResult / validationRepeat;
                    if (avg > bestGlobalResult) {
                        bestGlobalResult = avg;
                    }
                }
            }
            resultSum += bestResult / validationRepeat;


            //second best
            int secondBestIndex = 0;
            int secondBestResult = 0;
            for (int b = 0; b < brainResults.length; b++) {
                if (b == bestIndex) continue;

                int avgResult = brainResults[b];
                if (avgResult >= secondBestResult) {
                    secondBestIndex = b;
                    secondBestResult = avgResult;
                }
            }

            Brain bestBrain = brainPool[bestIndex];
            Brain secondBestBrain = brainPool[secondBestIndex];

            if (bestResult / validationRepeat == REVERSE_IO_COUNT) {
                bestBrain.getWeightLayer(0).print();
            }

            //swap
            brainPool = (brainPool == cache1) ? cache2 : cache1;
            for (int b = 0; b < brainPool.length; b++) {
                if (bestIndex == b) {
                    brainPool[b] = bestBrain;
                    continue;
                }
                if (secondBestIndex == b) {
                    brainPool[b] = secondBestBrain;
                    continue;
                }

                template.branchDestination(brainPool[b])
//                        .copy(bestBrain, REVERSE_IO_COUNT * 3, bestGlobalResult * 2); //the closer to the goal - the less mutation
                        .produceChildUnsafe(bestBrain, secondBestBrain, 5, 1);
            }

            int iterationSkip = 2500;
            if (iteration % iterationSkip == 0) {
                System.out.println("[test iterate] Best so far: " + bestGlobalResult + " avg: " + resultSum / iterationSkip + " best: " + bestResult / validationRepeat);
                resultSum = 0;
            }

            iteration++;
        }
        long end = System.currentTimeMillis();
        return (end - start);
    }

}
