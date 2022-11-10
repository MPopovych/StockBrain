package matrix;

import org.jetbrains.annotations.NotNull;
import suppliers.OnesSupplier;
import suppliers.ValueSupplier;

import java.nio.ByteBuffer;
import java.util.Base64;

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

	public String readStringData() {
		int count = width * height;
		ByteBuffer buff = ByteBuffer.allocate(count * 4);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				buff.putFloat(values[x][y]); // BIAS VALUES
			}
		}
		return Base64.getEncoder().encodeToString(buff.array());
	}

	public void writeStringData(@NotNull String data) {
		byte[] bytes = Base64.getDecoder().decode(data);
		ByteBuffer buff = ByteBuffer.wrap(bytes);

		int expectedCount = width * height;
		if (bytes.length != expectedCount * 4) {
			throw new IllegalStateException("Mismatch of %s vs %s".formatted(bytes.length, expectedCount * 4));
		}

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				values[x][y] = buff.getFloat();
			}
		}
	}

	public float[] readFloatData() {
		int count = width * height;
		float[] array = new float[count];
		for (int x = 0; x < width; x++) {
			if (height >= 0) System.arraycopy(values[x], 0, array, x * height, height);
		}
		return array;
	}


	public void writeFloatData(@NotNull float[] data) {
		for (int x = 0; x < width; x++) {
			if (height >= 0) System.arraycopy(data, x * height, values[x], 0, height);
		}
	}
}
