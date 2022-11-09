import activation.ActivationFunction;
import suppliers.ValueSupplier;

public class Brain {

    private boolean debug = false;
    private int layerCount = 0;

    private ActivationFunction function;
    private final ValueSupplier supplier;
    private BrainLayer[] brainLayers = new BrainLayer[0];
    private ActivationFunction[] functions = new ActivationFunction[0];

    Brain(ActivationFunction function, ValueSupplier baseSupplier) {
        this.function = function;
        this.supplier = baseSupplier;
    }

    public int[] getStructure() {
        int[] struct = new int[(brainLayers.length + 1) / 2];

        for (int i = 0; i < struct.length; i++) {
            struct[i] = brainLayers[i * 2].getWidth();
        }
        return struct;
    }

    public BrainLayer getWeightLayer(int index) {
        return brainLayers[index * 2 + 1];
    }

    public BrainLayer getLayer(int index) {
        return brainLayers[index * 2];
    }

    public int getLayerCount() {
        return layerCount;
    }

    public int getWeightLayerCount() {
        return (brainLayers.length - 1) / 2;
    }

    void setDebug(boolean debug) {
        this.debug = debug;
    }

    void append(int count) {
        append(count, null, null, null);
    }

    void append(int count, ValueSupplier supplier) {
        append(count, null, supplier, null);
    }

    void appendBias(int count, ValueSupplier biasSupplier) {
        append(count,null, null, biasSupplier);
    }

    void append(int count, ActivationFunction function, ValueSupplier supplier, ValueSupplier bSupplier) {
        layerCount++;

        ValueSupplier supply = supplier == null ? this.supplier : supplier;

        BrainLayer weightLayer = null;
        if (brainLayers.length > 0) {
            BrainLayer layer = brainLayers[brainLayers.length - 1];
            weightLayer = new BrainLayer(count, layer.getWidth(), supply); // weight layer
        }

        BrainLayer nextLayer = new BrainLayer(count, 1, null, bSupplier); // hidden layer

        int newSize = brainLayers.length + 1 + (weightLayer == null ? 0 : 1);

        BrainLayer[] oldArray = brainLayers;
        brainLayers = new BrainLayer[newSize];
        System.arraycopy(oldArray, 0, brainLayers, 0, oldArray.length);
        if (weightLayer != null) {
            brainLayers[oldArray.length] = weightLayer;
        }
        brainLayers[brainLayers.length - 1] = nextLayer;

        ActivationFunction[] layers = functions;
        functions = new ActivationFunction[layerCount];
        System.arraycopy(layers, 0, functions, 0, layers.length);
        functions[layers.length] = (function == null) ? this.function : function;
    }

    ActivationFunction getFunction() {
        return this.function;
    }

    void setFunction(ActivationFunction function) {
        this.function = function;
    }

    public void setInput(float[] values) {
        if (brainLayers.length <= 0) {
            throw new IllegalStateException("NO LAYERS");
        }

        BrainLayer first = brainLayers[0];
        if (first.getNodeCount() != values.length) {
            throw new IllegalStateException("MISMATCH OF NODE: " + first.getNodeCount() + " INPUT: " + values.length);
        }

        first.setValues(values);
    }

    public void setInput(ValueSupplier supplier) {
        if (brainLayers.length <= 0) {
            throw new IllegalStateException("NO LAYERS");
        }

        BrainLayer first = brainLayers[0];
        for (int i = 0; i < first.getHeight(); i++) {
            first.values[0][i] = supplier.supply(0, i);
        }
    }

    public float[] calculate() {
        float[] result = new float[brainLayers[brainLayers.length - 1].getWidth()];
        return calculate(result);
    }

    public float[] calculate(float[] outDest) {
        int layer;
        int weight = 0;

        for (int i = 0; i < layerCount - 1; i++) {
            layer = i * 2;
            weight = layer + 1;

            if (debug) {
                System.out.println("Layer " + i + " values");
                brainLayers[layer].print();
                System.out.println("Layer " + i + " biases");
                brainLayers[layer].printBias();
                System.out.println("Layer " + i + " weights");
                brainLayers[weight].print();
            }

            BrainLayer target = brainLayers[weight + 1];
            if (weight + 1 == brainLayers.length || target.bias == null) {
                target.setToZeroes();
            } else {
                target.setToBias();
            }
            brainLayers[layer].multiply(brainLayers[weight], target);

            ActivationFunction func = functions[i + 1];
            for (int j = 0; j < target.getWidth(); j++) {
                target.values[j][0] = func.apply(target.values[j][0]);
            }
        }

        BrainLayer last = brainLayers[weight + 1];
        for (int i = 0; i < outDest.length; i++) {
            outDest[i] = last.values[i][0];
        }

        return outDest;
    }

}
