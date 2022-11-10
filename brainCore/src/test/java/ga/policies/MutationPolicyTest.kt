package ga.policies

import assertNotEqualModel
import ga.weights.WeightGenes
import utils.printBlue
import kotlin.test.Test

class MutationPolicyTest {

	@Test
	fun testAdditiveMutationPolicy() {
		val a = WeightGenes("a", floatArrayOf(0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f))
		val d = a.emptyCopy()
		assertNotEqualModel(a, d)

		AdditiveMutationPolicy().mutateWeight(a, d)

		printBlue(d.genes.toList())
	}


}