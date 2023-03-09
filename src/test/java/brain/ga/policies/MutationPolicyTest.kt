package brain.ga.policies

import brain.assertEqualModel
import brain.assertNotEqualModel
import brain.ga.weights.WeightGenes
import brain.utils.printBlueBr
import brain.utils.printRedBr
import kotlin.test.Test

class MutationPolicyTest {

	@Test
	fun testAdditiveMutationPolicy() {
		val a = WeightGenes("a", floatArrayOf(0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f))
		val d = a.emptyCopy()
		assertNotEqualModel(a, d)

		repeat(10) {
			AdditiveMutationPolicy(fraction = 0.5).mutateWeight(a, d)
		}

		printBlueBr("testAdditiveMutationPolicy ${d.genes.toList()}")
		assertNotEqualModel(a, d)
	}

	@Test
	fun testAdditiveMutationPolicyLowChance() {
		val a = WeightGenes("a", floatArrayOf(0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f))
		val ref = a.copy()
		assertEqualModel(a, ref)

		repeat(1000) {
			AdditiveMutationPolicy(fraction = 0.05).mutateWeight(a, a)
		}

		printBlueBr("testAdditiveMutationPolicy ${ref.genes.toList()}")
		printRedBr("testAdditiveMutationPolicy ${a.genes.toList()}")
		assertNotEqualModel(a, ref)
	}



}