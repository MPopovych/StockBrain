package matrix;

public class MatrixMath {

	public static void multiply(Matrix a, Matrix b, Matrix destination) {
		//Column major implementation, ijk algorithm
		int thisX = a.width; //right, number of columns
		int thisY = a.height; // down, number of rows
		int targetX = b.width;
		int targetY = b.height;

		if (thisX != targetY) {
			throw new IllegalArgumentException("CONFLICT OF " + thisX + " TARGET " + targetY + ".");
		}

		float value;
		for (int i = 0; i < thisY; i++) {
			for (int j = 0; j < targetX; j++) {
				value = destination.values[j][i];
				for (int k = 0; k < thisX; k++) {
					value += a.values[k][i] * b.values[j][k];
				}
				destination.values[j][i] = value;
			}
		}
	}

	public static void add(Matrix a, Matrix b, Matrix destination) {
		//Column major implementation, ijk algorithm
		int thisX = a.width; //right, number of columns
		int thisY = a.height; // down, number of rows
		int targetX = b.width;
		int targetY = b.height;

		if (thisX != targetX || thisY != targetY) {
			throw new IllegalArgumentException("CONFLICT OF " + thisX + " TARGET " + targetY + ".");
		}

		for (int i = 0; i < thisX; i++) {
			for (int j = 0; j < thisY; j++) {
				destination.values[i][j] = a.values[i][j] + b.values[i][j];
			}
		}
	}

	public static void subtract(Matrix a, Matrix b, Matrix destination) {
		//Column major implementation, ijk algorithm
		int thisX = a.width; //right, number of columns
		int thisY = a.height; // down, number of rows
		int targetX = b.width;
		int targetY = b.height;

		if (thisX != targetX || thisY != targetY) {
			throw new IllegalArgumentException("CONFLICT OF " + thisX + " TARGET " + targetY + ".");
		}

		for (int i = 0; i < thisX; i++) {
			for (int j = 0; j < thisY; j++) {
				destination.values[i][j] = a.values[i][j] - b.values[i][j];
			}
		}
	}

	public static void convolutionSubtract(Matrix a, Matrix destination) {
		//Column major implementation, ijk algorithm
		int thisX = a.width; //right, number of columns
		int thisY = a.height; // down, number of rows
		int targetX = destination.width;
		int targetY = destination.height;

		if (thisX != targetX || targetY != thisY - 1) {
			throw new IllegalArgumentException("shape conflict %s:%s vs %s:%s".formatted(thisX, thisY, targetX, targetY));
		}

		for (int i = 0; i < targetX; i++) {
			for (int j = 0; j < targetY; j++) {
				destination.values[i][j] = a.values[i][j + 1] - a.values[i][j];
			}
		}
	}

	public static void hadamard(Matrix a, Matrix b, Matrix destination) {
		//Column major implementation, ijk algorithm
		int thisX = a.width; //right, number of columns
		int thisY = a.height; // down, number of rows
		int targetX = b.width;
		int targetY = b.height;

		if (thisX != targetX || thisY != targetY) {
			throw new IllegalArgumentException("CONFLICT OF " + thisX + " TARGET " + targetY + ".");
		}

		for (int i = 0; i < thisX; i++) {
			for (int j = 0; j < thisY; j++) {
				destination.values[i][j] = a.values[i][j] * b.values[i][j];
			}
		}
	}

	public static void transfer(Matrix from, Matrix to) {
		//Column major implementation, ijk algorithm
		int thisX = from.width; //right, number of columns
		int thisY = from.height; // down, number of rows
		int targetX = to.width;
		int targetY = to.height;

		if (thisX != targetX || thisY != targetY) {
			throw new IllegalArgumentException("CONFLICT OF " + thisX + " TARGET " + targetY + ".");
		}

		for (int i = 0; i < thisX; i++) {
			if (thisY >= 0) System.arraycopy(from.values[i], 0, to.values[i], 0, thisY);
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
