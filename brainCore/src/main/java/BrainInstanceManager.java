import java.nio.ByteBuffer;
import java.util.Base64;

public class BrainInstanceManager {

    /**
     * [struct_length][struct_array]
     * + (L * [layer_type][bias_length][bias_values])
     * + (W * [weight_length][weight_values])
     */


    private interface LayerTypes {
        int NORMAL = 0x01 << 31;
        int BIASED = 0x01 << 1;
    }

    public static String getSignature(Brain brain) {
        int biasSum = 0;
        int normalLayerCount = brain.getLayerCount();
        for (int i = 0; i < normalLayerCount; i++) {
            BrainLayer layer = brain.getLayer(i);
            if (layer.isBiased()) {
                biasSum += layer.getNodeCount();
            }
        }

        int weightSum = 0;
        int weightLayerCount = brain.getWeightLayerCount();
        for (int i = 0; i < weightLayerCount; i++) {
            weightSum += brain.getWeightLayer(i).getNodeCount();
        }

        int[] structure = brain.getStructure();

        ByteBuffer buff = ByteBuffer.allocate(4 // struct len
                + structure.length * 4          // struct array
                + (weightSum * 4                // for weight values
                + weightLayerCount * 4)         // for sizes of weight layers
                + (normalLayerCount * 4         // for layer type masks
                + normalLayerCount * 4          // for bias value count (0 for non biased)
                + biasSum * 4));                // for bias values

        buff.putInt(structure.length); // STRUCTURE SIZE
        for (int count : structure) {
            buff.putInt(count); // STRUCTURE ELEMENTS
        }

        // BIASES
        for (int i = 0; i < normalLayerCount; i++) {
            BrainLayer layer = brain.getLayer(i);
            int biasNodeCount = layer.getNodeCount();

            int layerType = LayerTypes.NORMAL;
            if (layer.isBiased()) {
                layerType |= LayerTypes.BIASED;
            }

            buff.putInt(layerType); // LAYER TYPE

            if (!layer.isBiased()) {
                buff.putInt(0);         //BIAS COUNT
                continue;
            } else {
                buff.putInt(biasNodeCount); //BIAS COUNT
            }

            for (int x = 0; x < layer.getWidth(); x++) {
                for (int y = 0; y < layer.getHeight(); y++) {
                    buff.putFloat(layer.getBiasValue(x, y)); // BIAS VALUES
                }
            }
        }

        // WEIGHTS
        for (int i = 0; i < weightLayerCount; i++) {
            BrainLayer layer = brain.getWeightLayer(i);
            int weightCount = layer.getNodeCount();
            buff.putInt(weightCount); // WEIGHT COUNT

            for (int x = 0; x < layer.getWidth(); x++) {
                for (int y = 0; y < layer.getHeight(); y++) {
                    buff.putFloat(layer.getValue(x, y)); // WEIGHT VALUES
                }
            }
        }

        return Base64.getEncoder().encodeToString(buff.array());
    }

    public static int[] loadSignature(String signature, Brain brain) {
        ByteBuffer buff = ByteBuffer.wrap(Base64.getDecoder().decode(signature));

        int structureSize = buff.getInt(); // STRUCTURE SIZE
        int[] structure = new int[structureSize];
        for (int i = 0; i < structureSize; i++) {
            structure[i] = buff.getInt(); // STRUCTURE ELEMENTS
        }
        return structure;
    }

    public static Brain loadFromSignature(String signature, Brain brain) {
        ByteBuffer buff = ByteBuffer.wrap(Base64.getDecoder().decode(signature));

        int structureSize = buff.getInt(); // STRUCTURE SIZE
        int[] brainStructure = brain.getStructure();
        for (int i = 0; i < structureSize; i++) {
            if (brainStructure[i] != buff.getInt()) {
                throw new IllegalStateException("MISMATCH OF STRUCTURE");
            }; // STRUCTURE ELEMENTS
        }

        int layerIndex = 0;
        while (buff.hasRemaining()) {
            int layerType = buff.getInt(); // LAYER TYPE
            if ((layerType & LayerTypes.NORMAL) == 0) {
                throw new IllegalStateException("Failed to verify layer type");
            }

            int biasCount = buff.getInt(); // BIAS COUNT

            BrainLayer layer = brain.getLayer(layerIndex);

            float[] values = new float[biasCount];
            for (int i = 0; i < biasCount; i++) {
                values[i] = buff.getFloat(); // BIAS VALUES
            }

            if (values.length > 0) {
                layer.setBiasValues(values);
            }
            layerIndex++;
            if (layerIndex >= structureSize) {
                break;
            }
        }

        int weightLayerIndex = 0;
        while (buff.hasRemaining()) {
            int weightCount = buff.getInt(); // WEIGHT COUNT

            BrainLayer layer = brain.getWeightLayer(weightLayerIndex);

            float[] values = new float[weightCount];
            for (int i = 0; i < weightCount; i++) {
                values[i] = buff.getFloat(); // WEIGHT VALUES
            }

            layer.setValues(values);
            weightLayerIndex++;
        }

        return brain;
    }
}
