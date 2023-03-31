package brain.matrix;

import java.util.Arrays;
import java.util.Set;

public class MatrixMath {

	public static void checkSameDimensions(Matrix... matrices) {
		Matrix first = matrices[0];
		int width = first.width;
		int height = first.height;
		for (Matrix m : matrices) {
			if (m.width != width || m.height != height) {
				throw new IllegalArgumentException("shape conflict %s:%s vs %s:%s".formatted(width, height, m.width, m.height));
			}
		}
	}

	public static void multiply(Matrix a, Matrix b, Matrix d) {
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

		final int m = a.height;
		final int g = a.width;
		final int h = b.width;
		final float[][] aa = a.values;
		final float[][] bb = b.values;
		final float[][] dd = d.values;
		for (int i = 0; i < m; i++) {
			final float[] ddi = dd[i];
			for (int j = 0; j < h; j++) {
				float sum = 0;
				final float[] aai = aa[i];
				for (int k = 0; k < g; k++) {
					sum += aai[k] * bb[k][j];
				}
				ddi[j] = sum;
			}
		}
	}


	private static void multiplyBig(Matrix a, Matrix b, Matrix d, int blockSize) {
		final int m = a.height;
		final int n = a.width;
		final int p = b.width;

		MatrixMath.flush(d);

		final float[][] aa = a.values;
		final float[][] bb = b.values;
		final float[][] dd = d.values;
		for (int i = 0; i < m; i += blockSize) {
			for (int j = 0; j < p; j += blockSize) {
				for (int k = 0; k < n; k += blockSize) {
					// perform block matrix multiplication
					final int capI = Math.min(i + blockSize, m);
					final int capJ = Math.min(j + blockSize, p);
					for (int ii = i; ii < capI; ii++) {
						for (int jj = j; jj < capJ; jj++) {
							float s = 0;
							final int capK = Math.min(k + blockSize, n);
							for (int kk = k; kk < capK; kk++) {
								s += aa[ii][kk] * bb[kk][jj];
							}
							dd[ii][jj] += s;
						}
					}
				}
			}
		}
	}

	public static void disperseFeature(Matrix a, Matrix b, Matrix d, int units) {
		// Row major implementation, ijk algorithm
		if (a.height != d.height || b.height != 1) {
			throw new IllegalArgumentException("Matrix input dimensions are not compatible for dispersion" +
					"a: [%s:%s], b[%s:%s]".formatted(a.height, a.width, b.height, b.width));
		}

		if (d.width != a.width * units || d.width != b.width) {
			throw new IllegalArgumentException(("Matrix destination dimensions are not compatible for multiplication" +
					"has to be: dw[%s vs final %s], dh[%s vs final %s]").formatted(b.width, d.width, a.height, d.height));
		}

		final int m = a.height;
		final int g = a.width;
		final float[][] aa = a.values;
		final float[][] bb = b.values;
		final float[][] dd = d.values;
		for (int y = 0; y < m; y++) {
			for (int x = 0; x < g; x++) {
				for (int u = 0; u < units; u++) {
					dd[y][x * units + u] = aa[y][x] + bb[0][x * units + u];
				}
			}
		}
	}

	public static void multiply(Matrix a, float m, Matrix d) {
		// Row major implementation, ijk algorithm
		if (a.height != d.height || a.width != d.width) {
			throw new IllegalArgumentException("Matrix dimensions are not compatible for multiplication.");
		}

		for (int y = 0; y < a.height; y++) {
			for (int x = 0; x < a.width; x++) {
				d.values[y][x] = a.values[y][x] * m;
			}
		}
	}

	public static void add(Matrix a, Matrix b, Matrix d) {
		int thisX = a.width; // right, number of columns
		int thisY = a.height; // down, number of rows

		checkSameDimensions(a, b, d);

		for (int y = 0; y < thisY; y++) {
			for (int x = 0; x < thisX; x++) {
				d.values[y][x] = a.values[y][x] + b.values[y][x];
			}
		}
	}

	public static void add(Matrix a, Matrix d, float constant) {
		int thisX = a.width; // right, number of columns
		int thisY = a.height; // down, number of rows

		checkSameDimensions(a, d);

		for (int y = 0; y < thisY; y++) {
			for (int x = 0; x < thisX; x++) {
				d.values[y][x] = a.values[y][x] + constant;
			}
		}
	}

	public static void constantSub(float constant, Matrix a, Matrix d) {
		int thisX = a.width; // right, number of columns
		int thisY = a.height; // down, number of rows

		checkSameDimensions(a, d);

		for (int y = 0; y < thisY; y++) {
			for (int x = 0; x < thisX; x++) {
				d.values[y][x] = constant - a.values[y][x];
			}
		}
	}

	public static void add(Matrix d, float constant) {
		final int thisX = d.width; // right, number of columns
		final int thisY = d.height; // down, number of rows

		for (int y = 0; y < thisY; y++) {
			for (int x = 0; x < thisX; x++) {
				d.values[y][x] += constant;
			}
		}
	}

	public static void subtract(Matrix a, Matrix b, Matrix destination) {
		int thisX = a.width; // right, number of columns
		int thisY = a.height; // down, number of rows

		checkSameDimensions(a, b, destination);
		for (int y = 0; y < thisY; y++) {
			for (int x = 0; x < thisX; x++) {
				destination.values[y][x] = a.values[y][x] - b.values[y][x];
			}
		}
	}

	public static void convolutionSubtract(Matrix a, Matrix destination) {
		int thisX = a.width; // right, number of columns
		int thisY = a.height; // down, number of rows
		int targetX = destination.width;
		int targetY = destination.height;

		if (targetX != thisX || targetY != thisY - 1) {
			throw new IllegalArgumentException("shape conflict %s:%s vs %s:%s".formatted(thisX, thisY, targetX, targetY));
		}

		for (int y = 0; y < targetY; y++) {
			for (int x = 0; x < targetX; x++) {
				destination.values[y][x] = a.values[y + 1][x] - a.values[y][x];
			}
		}
	}

	public static void convolutionFlatten(Matrix a, Matrix destination) {
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
				destination.values[0][pending++] = a.values[y][x];
			}
		}
	}

	public static void hadamard(Matrix a, Matrix b, Matrix destination) {
		int thisX = a.width; // right, number of columns
		int thisY = a.height; // down, number of rows

		checkSameDimensions(a, b, destination);

		for (int y = 0; y < thisY; y++) {
			for (int x = 0; x < thisX; x++) {
				destination.values[y][x] = a.values[y][x] * b.values[y][x];
			}
		}
	}

	public static void addSingleToEveryRow(Matrix a, Matrix single, Matrix destination) {
		int thisX = a.width; // right, number of columns
		int thisY = a.height; // down, number of rows

		checkSameDimensions(a, destination);
		if (single.height != 1 || single.width != thisX) {
			throw new IllegalArgumentException("shape conflict %s:%s vs %s:%s".formatted(thisX, thisY, single.width, single.height));
		}

		for (int y = 0; y < thisY; y++) {
			for (int x = 0; x < thisX; x++) {
				destination.values[y][x] = a.values[y][x] + single.values[0][x];
			}
		}
	}

	public static void hadamardSingleRow(Matrix a, Matrix single, Matrix destination) {
		int thisX = a.width; // right, number of columns
		int thisY = a.height; // down, number of rows

		checkSameDimensions(a, destination);
		if (single.height != 1 || single.width != thisX) {
			throw new IllegalArgumentException("shape conflict %s:%s vs %s:%s".formatted(thisX, thisY, single.width, single.height));
		}

		for (int y = 0; y < thisY; y++) {
			for (int x = 0; x < thisX; x++) {
				destination.values[y][x] = a.values[y][x] * single.values[0][x];
			}
		}
	}

	public static void transfer(Matrix from, Matrix to) {
		int thisX = from.width; // right, number of columns
		int thisY = from.height; // down, number of rows

		checkSameDimensions(from, to);

		for (int y = 0; y < thisY; y++) {
			System.arraycopy(from.values[y], 0, to.values[y], 0, thisX);
		}
	}

	public static void transferWithFilter(Matrix from, Matrix to, Set<Integer> filter) {
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
				to.values[y][pendingX] = from.values[y][x];
			}
			pendingX++;
		}
	}

	public static void transferEndRange(Matrix from, Matrix to, int maskStart, int maskEnd) {
		int thisX = from.width; // right, number of columns
		int thisY = from.height; // down, number of rows
		int targetX = to.width;
		int targetY = to.height;
		int destHeight = thisY - maskEnd - maskStart;

		if (thisX != targetX || destHeight != targetY) {
			throw new IllegalArgumentException("shape conflict %s:%s vs %s:%s".formatted(thisX, thisY, targetX, targetY));
		}

		for (int y = maskStart; y < thisY - maskEnd; y++) {
			System.arraycopy(from.values[y], 0, to.values[y - maskStart], 0, targetX);
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

	public static void flush(Matrix d) {
		//Column major implementation, ijk algorithm
		int thisY = d.height; // down, number of rows

		for (int y = 0; y < thisY; y++) {
			Arrays.fill(d.values[y], 0f);
		}
	}
}
