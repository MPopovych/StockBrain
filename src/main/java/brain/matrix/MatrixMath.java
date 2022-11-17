package brain.matrix;

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
		//Column major implementation, ijk algorithm

		int aW = a.width;
		int aH = a.height;
		int bW = b.width;
		int bH = b.height;
		int dW = d.width;
		int dH = d.height;

		if (aW != bH || aH != dH || bW != dW) {
			throw new IllegalArgumentException("shape conflict %s:%s vs %s:%s vs %s:%s".formatted(aW, aH, bW, bH, dW, dH));
		}

		float value;
		for (int i = 0; i < aH; i++) {
			for (int j = 0; j < bW; j++) {
				value = d.values[j][i];
				for (int k = 0; k < aW; k++) {
					value += a.values[k][i] * b.values[j][k];
				}
				d.values[j][i] = value;
			}
		}
	}

	public static void add(Matrix a, Matrix b, Matrix destination) {
		int thisX = a.width; // right, number of columns
		int thisY = a.height; // down, number of rows

		checkSameDimensions(a, b, destination);

		for (int i = 0; i < thisX; i++) {
			for (int j = 0; j < thisY; j++) {
				destination.values[i][j] = a.values[i][j] + b.values[i][j];
			}
		}
	}

	public static void subtract(Matrix a, Matrix b, Matrix destination) {
		int thisX = a.width; // right, number of columns
		int thisY = a.height; // down, number of rows

		checkSameDimensions(a, b, destination);

		for (int i = 0; i < thisX; i++) {
			for (int j = 0; j < thisY; j++) {
				destination.values[i][j] = a.values[i][j] - b.values[i][j];
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

		for (int i = 0; i < targetX; i++) {
			for (int j = 0; j < targetY; j++) {
				destination.values[i][j] = a.values[i][j + 1] - a.values[i][j];
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
		for (int j = 0; j < thisY; j++) {
			for (int i = 0; i < thisX; i++) {
				destination.values[pending++][0] = a.values[i][j];
			}
		}
	}

	public static void hadamard(Matrix a, Matrix b, Matrix destination) {
		int thisX = a.width; // right, number of columns
		int thisY = a.height; // down, number of rows

		checkSameDimensions(a, b, destination);

		for (int i = 0; i < thisX; i++) {
			for (int j = 0; j < thisY; j++) {
				destination.values[i][j] = a.values[i][j] * b.values[i][j];
			}
		}
	}

	public static void transfer(Matrix from, Matrix to) {
		int thisX = from.width; // right, number of columns
		int thisY = from.height; // down, number of rows

		checkSameDimensions(from, to);

		for (int x = 0; x < thisX; x++) {
			System.arraycopy(from.values[x], 0, to.values[x], 0, thisY);
		}
	}

	public static void transferArrayToMatrixRoot(float[] from, Matrix to) {
		int targetX = to.width;
		int targetY = to.height;

		if (from.length != targetX || targetY != 1) {
			throw new IllegalArgumentException("shape conflict %s:%s vs %s:%s".formatted(1, from.length, targetX, targetY));
		}

		for (int x = 0; x < targetX; x++) {
			to.values[x][0] = from[x];
		}
	}

	public static void transferMatrixRootToArray(Matrix from, float[] to) {
		int thisX = from.width; // right, number of columns
		int thisY = from.height; // down, number of rows

		if (thisX != to.length || thisY != 1) {
			throw new IllegalArgumentException("shape conflict %s:%s vs %s:%s".formatted(1, to.length, thisX, thisY));
		}

		for (int x = 0; x < thisX; x++) {
			to[x] = from.values[x][0];
		}
	}

	public static void transferHeightRange(Matrix from, Matrix to, int maskStart, int maskEnd) {
		int thisX = from.width; // right, number of columns
		int thisY = from.height; // down, number of rows
		int targetX = to.width;
		int targetY = to.height;
		int destHeight = thisY - maskEnd - maskStart;

		if (thisX != targetX || destHeight != targetY) {
			throw new IllegalArgumentException("shape conflict %s:%s vs %s:%s".formatted(thisX, thisY, targetX, targetY));
		}

		for (int x = 0; x < thisX; x++) {
			System.arraycopy(from.values[x], maskStart, to.values[x], 0, destHeight);
		}
	}

	public static void flush(Matrix d) {
		//Column major implementation, ijk algorithm
		int thisX = d.width; //right, number of columns
		int thisY = d.height; // down, number of rows

		for (int i = 0; i < thisX; i++) {
			for (int j = 0; j < thisY; j++) {
				d.values[i][j] = 0f;
			}
		}
	}
}
