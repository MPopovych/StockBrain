package brain.matrix;

import brain.suppliers.ValueFiller;
import brain.suppliers.ValueSupplier;

public class MatrixF {
	public float[] values;
	public final int width;
	public final int height;

	public MatrixF(int width, // columns
	              int height) { // rows
		this(width, height, null);
	}

	public MatrixF(int width, int height, ValueFiller filler) {
		this.width = width;
		this.height = height;

		if (filler != null) {
			values = new float[height * width];
			filler.fill(values);
		} else {
			values = new float[height * width];
		}
	}
}
