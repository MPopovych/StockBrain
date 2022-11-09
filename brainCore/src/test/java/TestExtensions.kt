import matrix.Matrix
import java.util.*

fun assertEqual(a: Matrix, b: Matrix) {
	assert(Arrays.deepEquals(a.values, b.values))
}