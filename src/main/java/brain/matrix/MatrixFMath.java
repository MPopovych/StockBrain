package brain.matrix;

import java.util.Set;

public class MatrixFMath {

	public static void checkSameDimensions(MatrixF... matrices) {
		MatrixF first = matrices[0];
		int width = first.width;
		int height = first.height;
		for (MatrixF m : matrices) {
			if (m.width != width || m.height != height) {
				throw new IllegalArgumentException("shape conflict %s:%s vs %s:%s".formatted(width, height, m.width, m.height));
			}
		}
	}

	public static void multiply(MatrixF a, MatrixF b, MatrixF d) {
		// Row major implementation, ijk algorithm
		if (a.width != b.height) {
			throw new IllegalArgumentException("Matrix input dimensions are not compatible for multiplication" +
					"a: [%s:%s], b[%s:%s]".formatted(a.height, a.width, b.height, b.width));
		}

		if (a.height != d.height || b.width != d.width) {
			throw new IllegalArgumentException(("Matrix destination dimensions are not compatible for multiplication" +
					"has to be: dw[%s vs final %s], dh[%s vs final %s]").formatted(b.width, d.width, a.height, d.height));
		}

		if (d.width * d.height > 225) {
			multiplyBig(a, b, d, 64);
			return;
		}

		int m = a.height;
		int f = d.width;
		int g = a.width;
		int h = b.width;
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < h; j++) {
				float sum = 0;
				for (int k = 0; k < g; k++) {
					sum += a.values[i * g + k] * b.values[k * h + j];
				}
				d.values[i * f + j] = sum;
			}
		}
	}

	private static void multiplyBig(MatrixF a, MatrixF b, MatrixF d, int blockSize) {
		int m = a.height;
		int n = a.width;
		int p = b.width;
		int g = d.width;

		for (int i = 0; i < m; i += blockSize) {
			for (int j = 0; j < p; j += blockSize) {
				for (int k = 0; k < n; k += blockSize) {
					// perform block matrix multiplication
					for (int ii = i; ii < Math.min(i + blockSize, m); ii++) {
						for (int jj = j; jj < Math.min(j + blockSize, p); jj++) {
							float s = 0;
							for (int kk = k; kk < Math.min(k + blockSize, n); kk++) {
								s += a.values[ii * n + kk] * b.values[kk * p + jj];
							}
							d.values[ii * g + jj] += s;
						}
					}
				}
			}
		}
	}

	public static void multiply(MatrixF a, float m, MatrixF d) {
		// Row major implementation, ijk algorithm
		if (a.height != d.height || a.width != d.width) {
			throw new IllegalArgumentException("Matrix dimensions are not compatible for multiplication.");
		}

		for (int y = 0; y < a.height; y++) {
			for (int x = 0; x < a.width; x++) {
				d.values[y * d.width + x] = a.values[y * d.width + x] * m;
			}
		}
	}

	public static void add(MatrixF a, MatrixF b, MatrixF d) {
		int thisX = a.width; // right, number of columns
		int thisY = a.height; // down, number of rows

		checkSameDimensions(a, b, d);

		for (int y = 0; y < thisY; y++) {
			for (int x = 0; x < thisX; x++) {
				d.values[y * d.width + x] = a.values[y * a.width + x] + b.values[y * b.width + x];
			}
		}
	}

	public static void add(MatrixF a, MatrixF d, float constant) {
		int thisX = a.width; // right, number of columns
		int thisY = a.height; // down, number of rows

		checkSameDimensions(a, d);

		for (int y = 0; y < thisY; y++) {
			for (int x = 0; x < thisX; x++) {
				d.values[y * d.width + x] = a.values[y * a.width + x] + constant;
			}
		}
	}

	public static void convolutionSubtract(MatrixF a, MatrixF d) {
		int thisX = a.width; // right, number of columns
		int thisY = a.height; // down, number of rows
		int targetX = d.width;
		int targetY = d.height;

		if (targetX != thisX || targetY != thisY - 1) {
			throw new IllegalArgumentException("shape conflict %s:%s vs %s:%s".formatted(thisX, thisY, targetX, targetY));
		}

		for (int y = 0; y < targetY; y++) {
			for (int x = 0; x < targetX; x++) {
				d.values[y * d.width + x] = a.values[(y + 1) * a.width + x] - a.values[y * a.width + x];
			}
		}
	}

	public static void convolutionFlatten(MatrixF a, MatrixF destination) {
		int thisX = a.width; // right, number of columns
		int thisY = a.height; // down, number of rows
		int targetX = destination.width;
		int targetY = destination.height;

		if (targetY != 1 || targetX != thisX * thisY) {
			throw new IllegalArgumentException("shape conflict %s:%s vs %s:%s".formatted(thisX, thisY, targetX, targetY));
		}

		int pending = 0;
		for (int y = 0; y < thisY; y++) {
			for (int x = 0; x < thisX; x++) {
				destination.values[pending++] = a.values[y * a.width + x];
			}
		}
	}

	public static void hadamard(MatrixF a, MatrixF b, MatrixF d) {
		int thisX = a.width; // right, number of columns
		int thisY = a.height; // down, number of rows

		checkSameDimensions(a, b, d);

		for (int y = 0; y < thisY; y++) {
			for (int x = 0; x < thisX; x++) {
				d.values[y * d.width + x] = a.values[y * a.width + x] * b.values[y * b.width + x];
			}
		}
	}

	public static void addSingleToEveryRow(MatrixF a, MatrixF single, MatrixF d) {
		int thisX = a.width; // right, number of columns
		int thisY = a.height; // down, number of rows

		checkSameDimensions(a, d);
		if (single.height != 1 || single.width != thisX) {
			throw new IllegalArgumentException("shape conflict %s:%s vs %s:%s".formatted(thisX, thisY, single.width, single.height));
		}

		for (int y = 0; y < thisY; y++) {
			for (int x = 0; x < thisX; x++) {
				d.values[y * d.width + x] = a.values[y * a.width + x] + single.values[x];
			}
		}
	}

	public static void hadamardSingleRow(MatrixF a, MatrixF single, MatrixF d) {
		int thisX = a.width; // right, number of columns
		int thisY = a.height; // down, number of rows

		checkSameDimensions(a, d);
		if (single.height != 1 || single.width != thisX) {
			throw new IllegalArgumentException("shape conflict %s:%s vs %s:%s".formatted(thisX, thisY, single.width, single.height));
		}

		for (int y = 0; y < thisY; y++) {
			for (int x = 0; x < thisX; x++) {
				d.values[y * d.width + x] = a.values[y * a.width + x] * single.values[x];
			}
		}
	}

	public static void transfer(MatrixF from, MatrixF to) {
		int thisX = from.width; // right, number of columns
		int thisY = from.height; // down, number of rows

		checkSameDimensions(from, to);

		System.arraycopy(from.values, 0, to.values, 0, thisX * thisY);
	}

	public static void transferWithFilter(MatrixF from, MatrixF to, Set<Integer> filter) {
		int thisX = from.width; // right, number of columns
		int thisY = from.height; // down, number of rows

		if (thisY != to.height || to.width != filter.size()) {
			throw new IllegalArgumentException("shape conflict %s:%s vs %s:%s".formatted(thisX, thisY, to.width, to.height));
		}

		int pendingX = 0;
		for (int x = 0; x < thisX; x++) {
			if (!filter.contains(x)) {
				continue;
			}
			for (int y = 0; y < thisY; y++) {
				to.values[y * to.width + pendingX] = from.values[y * from.width + x];
			}
			pendingX++;
		}
	}

	public static void transferEndRange(MatrixF from, MatrixF to, int maskStart, int maskEnd) {
		int thisX = from.width; // right, number of columns
		int thisY = from.height; // down, number of rows
		int targetX = to.width;
		int targetY = to.height;
		int destHeight = thisY - maskEnd - maskStart;

		if (thisX != targetX || destHeight != targetY) {
			throw new IllegalArgumentException("shape conflict %s:%s vs %s:%s".formatted(thisX, thisY, targetX, targetY));
		}

		for (int y = maskStart; y < thisY - maskEnd; y++) {
			System.arraycopy(from.values, y * from.width, to.values, (y - maskStart) * from.width, targetX);
		}
	}

	public static void transferSingleRow(Matrix from, Matrix to, int row, int destRow) {
		int thisX = from.width; // right, number of columns
		int thisY = from.height; // down, number of rows
		int targetX = to.width;
		int targetY = to.height;

		if (thisX != targetX || row >= thisY || destRow >= targetY) {
			throw new IllegalArgumentException("shape conflict %s:%s vs %s:%s".formatted(thisX, thisY, targetX, targetY));
		}

		System.arraycopy(from.values[row], 0, to.values[destRow], 0, targetX);
	}

	public static void flush(MatrixF d) {
		//Column major implementation, ijk algorithm
		int thisX = d.width; //right, number of columns
		int thisY = d.height; // down, number of rows

		for (int xy = 0; xy < thisY * thisX; xy++) {
			d.values[xy] = 0f;
		}
	}
}
