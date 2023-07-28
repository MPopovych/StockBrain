package brain.ga.policies

import brain.assertNotEqualModel
import brain.ga.weights.WeightGenes
import brain.utils.printBlueBr
import kotlin.test.Test

class CrossOverPolicyTest {

	@Test
	fun testSinglePoint() {
		val a = WeightGenes("a", floatArrayOf(0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f), 1, 7, 0)
		val b = WeightGenes("a", floatArrayOf(0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f), 1, 7, 0)
		val d = a.emptyCopy()
		assertNotEqualModel(a, d)

		SinglePointCrossOver().crossWeight(a, b, d)

		printBlueBr("testSinglePoint ${d.genes.toList()}")
	}

	@Test
	fun testUniform() {
		val a = WeightGenes("a", floatArrayOf(0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f), 1, 7, 0)
		val b = WeightGenes("a", floatArrayOf(0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f), 1, 7, 0)
		val d = a.emptyCopy()
		assertNotEqualModel(a, d)

		UniformCrossOver().crossWeight(a, b, d)

		printBlueBr("testUniform ${d.genes.toList()}")
	}

}