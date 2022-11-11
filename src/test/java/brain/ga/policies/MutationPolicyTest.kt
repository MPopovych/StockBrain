package brain.ga.policies

import brain.assertNotEqualModel
import brain.ga.weights.WeightGenes
import brain.utils.printBlueBr
import kotlin.test.Test

class MutationPolicyTest {

	@Test
	fun testAdditiveMutationPolicy() {
		val a = WeightGenes("a", floatArrayOf(0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f))
		val d = a.emptyCopy()
		assertNotEqualModel(a, d)

		AdditiveMutationPolicy().mutateWeight(a, d)

		printBlueBr("testAdditiveMutationPolicy ${d.genes.toList()}")
	}


}