package brain.layers

import brain.layers.impl.DenseLayerImpl
import brain.layers.weights.WeightData
import brain.matrix.Matrix
import brain.matrix.describe
import brain.matrix.get
import brain.suppliers.Suppliers
import brain.utils.printRedBr
import org.junit.jupiter.api.Test

class LayerTest {
	@Test
	fun testDense() {
		val inputData = Matrix.ofLambda(3, 2) { x, y, _ ->
			(x + y).toFloat()
		}

		val weight = Matrix.ofSupply(2, 3, Suppliers.const(0.5f))
		val bias = Matrix.ofSupply(2, 1, Suppliers.Ones)
		val denseWithBias = DenseLayerImpl(
			"", activation = null,
			WeightData("weight", weight, true, true),
			WeightData("bias", bias, true, true),
		)

		val r1 = denseWithBias.propagate(inputData)
		printRedBr(r1.describe())
		assert(r1.shape().width == 2)
		assert(r1.shape().height == 2)

		assert(r1[0, 0] == 2.5f)
		assert(r1[1, 0] == 2.5f)
		assert(r1[0, 1] == 4f)
		assert(r1[1, 1] == 4f)
	}
}