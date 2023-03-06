package brain.matrix;

import brain.suppliers.ValueSupplier;
import brain.suppliers.ZeroSupplier;
import org.jetbrains.annotations.NotNull;

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
		this(width, height, null);
	}

	public Matrix(int width, int height, ValueSupplier supplier) {
		this.width = width;
		this.height = height;

		if (supplier != null) {
			values = new float[height][];
			for (int y = 0; y < height; y++) {
				values[y] = new float[width];
				for (int x = 0; x < width; x++) {
					values[y][x] = supplier.supply(height * width, x, y);
				}
			}
		} else {
			values = new float[height][];
			for (int y = 0; y < height; y++) {
				values[y] = new float[width];
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
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				buff.putFloat(values[y][x]);
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

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				values[y][x] = buff.getFloat();
			}
		}
	}

	public float[] readFloatData() {
		int count = width * height;
		float[] array = new float[count];
		for (int y = 0; y < height; y++) {
			System.arraycopy(values[y], 0, array, y * width, width);
		}
		return array;
	}


	public void writeFloatData(@NotNull float[] data) {
		for (int y = 0; y < height; y++) {
			System.arraycopy(data, y * width, values[y], 0, width);
		}
	}
}
