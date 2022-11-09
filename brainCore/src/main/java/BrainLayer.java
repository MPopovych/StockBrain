import suppliers.ValueSupplier;
import suppliers.ZeroSupplier;

public class BrainLayer {

    private final int width;
    private final int height;
    float[][] values;
    float[][] bias = null;

    public BrainLayer(int width, int height) {
        this(width, height, ZeroSupplier.INSTANCE, null);
    }

    public BrainLayer(int width, int height, boolean biased) {
        this(width, height, ZeroSupplier.INSTANCE, null);
    }

    public BrainLayer(int width, int height, ValueSupplier supplier){
        this(width, height, supplier, null);
    }
    public BrainLayer(int width, int height, ValueSupplier supplier, ValueSupplier bSupplier) {
        this.width = width;
        this.height = height;

        if (supplier != null) {
            values = new float[width][];
            for (int x = 0; x < width; x++) {
                values[x] = new float[height];
                for (int y = 0; y < height; y++) {
                    values[x][y] = supplier.supply(x, y);
                }
            }
        } else {
            values = new float[width][];
            for (int x = 0; x < width; x++) {
                values[x] = new float[height];
            }
        }

        if (bSupplier != null) {
            bias = new float[width][];
            for (int x = 0; x < width; x++) {
                bias[x] = new float[height];
                for (int y = 0; y < height; y++) {
                    bias[x][y] = bSupplier.supply(x, y);;
                }
            }
        }
    }

    public BrainLayer(int width, int height, float[] source) {
        this(width, height, source, null);
    }

    public BrainLayer(int width, int height, float[] source, float[] bSource) {
        this.width = width;
        this.height = height;

        values = new float[width][];
        for (int x = 0; x < width; x++) {
            values[x] = new float[height];
            for (int y = 0; y < height; y++) {
                values[x][y] = source[x * height + y];
            }
        }
        if (bSource != null) {
            bias = new float[width][];
            for (int x = 0; x < width; x++) {
                bias[x] = new float[height];
                for (int y = 0; y < height; y++) {
                    bias[x][y] = bSource[x * height + y];
                }
            }
        }
    }

    public void multiply(float[][] target, float[][] destination) {
        //Column major implementation, ijk algorithm
        int thisX = values.length; //right, number of columns
        int thisY = values[0].length; // down, number of rows
        int targetX = target.length;
        int targetY = target[0].length;

        if (thisX != targetY) {
            throw new IllegalArgumentException("CONFLICT OF " + thisX + " TARGET " + targetY + ".");
        }

        float value;
        for (int i = 0; i < thisY; i++) {
            for (int j = 0; j < targetX; j++) {
                value = destination[j][i];
                for (int k = 0; k < thisX; k++) {
                    value += values[k][i] * target[j][k];
                }
                destination[j][i] = value;
            }
        }
    }

    public void multiply(BrainLayer target, BrainLayer destination) {
        multiply(target.values, destination.values);
    }

    void setValue(int x, int y, float value) {
        values[x][y] = value;
    }

    void setValue(int x, int y, ValueSupplier supplier) {
        values[x][y] = supplier.supply(x, y);
    }

    public float getValue(int x, int y) {
        return values[x][y];
    }

    public void setValues(ValueSupplier supplier) {
        for (int x = 0; x < values.length; x++) {
            for (int y = 0; y < values[0].length; y++) {
                values[x][y] = supplier.supply(x, y);
            }
        }
    }

    public void setValues(float[] source) {
        for (int x = 0; x < values.length; x++) {
            for (int y = 0; y < values[0].length; y++) {
                values[x][y] = source[x * height + y];
            }
        }
    }

    public void setToZeroes() {
        for (int x = 0; x < values.length; x++) {
            for (int y = 0; y < values[0].length; y++) {
                values[x][y] = 0;
            }
        }
    }

    public void setToBias() {
        if (bias == null) {
            return;
        }
        for (int x = 0; x < values.length; x++) {
            for (int y = 0; y < values[0].length; y++) {
                values[x][y] = bias[x][y];
            }
        }
    }

    public float getBiasValue(int x, int y) {
        if (bias == null) {
            return 0;
        }
        return bias[x][y];
    }

    public void setBiasValue(int x, int y, float value) {
        if (bias == null) {
            return;
        }
        bias[x][y] = value;
    }

    public void setBiasValues(ValueSupplier supplier) {
        if (bias == null) {
            bias = new float[width][];
            for (int x = 0; x < width; x++) {
                bias[x] = new float[height];
                for (int y = 0; y < height; y++) {
                    bias[x][y] = supplier.supply(x, y);
                }
            }
            return;
        }
        for (int x = 0; x < bias.length; x++) {
            for (int y = 0; y < bias[0].length; y++) {
                bias[x][y] = supplier.supply(x, y);
            }
        }
    }

    public void setBiasValues(float[] source) {
        if (bias == null) {
            bias = new float[width][];
            for (int x = 0; x < width; x++) {
                bias[x] = new float[height];
                for (int y = 0; y < height; y++) {
                    bias[x][y] = source[x * height + y];
                }
            }
            return;
        }
        for (int x = 0; x < bias.length; x++) {
            for (int y = 0; y < bias[0].length; y++) {
                bias[x][y] = source[x * height + y];
            }
        }
    }


    public void createBiasZeroed() {
        if (bias != null) {
            return;
        }

        bias = new float[width][];
        for (int x = 0; x < width; x++) {
            bias[x] = new float[height];
        }
    }

    public boolean isBiased() {
        return bias != null;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getNodeCount() {
        return width * height;
    }

    public void print() {
        for (int y = 0; y < values[0].length; y++) {
            for (int x = 0; x < values.length; x++) {
                System.out.print(values[x][y] + " ");
            }
            System.out.println();
        }
    }

    public void printBias() {
        if (bias == null) {
            return;
        }
        for (int y = 0; y < bias[0].length; y++) {
            for (int x = 0; x < bias.length; x++) {
                System.out.print(bias[x][y] + " ");
            }
            System.out.println();
        }
    }

}
