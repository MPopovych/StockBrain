package matrix;

import suppliers.OnesSupplier;
import suppliers.ValueSupplier;
// a * b = c
// [m, i] * [n, m] = [n, i]
public class Matrix {
	public float[][] values;
	public final int width;
	public final int height;

	public Matrix(int width, // columns
	              int height) { // rows
		this(width, height, OnesSupplier.INSTANCE);
	}

	public Matrix(int width, int height, ValueSupplier supplier) {
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
	}

	public Matrix copy() {
		Matrix m = new Matrix(width, height, null);
		MatrixMath.transfer(this, m);
		return m;
	}
}
