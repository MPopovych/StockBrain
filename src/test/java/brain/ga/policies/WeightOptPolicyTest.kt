package brain.ga.policies

import brain.assertEqualModel
import brain.assertNotEqualModel
import brain.ga.weights.WeightGenes
import brain.utils.printBlueBr
import brain.utils.printRedBr
import kotlin.test.Test

class WeightOptPolicyTest {

	@Test
	fun testDiscreteOptPolicy() {
		val a = WeightGenes("a", floatArrayOf(0.1f, 0.2f, 0.6f, 0.7f, 0.9f, 0.3f, 0.66f))
		val d = a.copy()
		assertEqualModel(a, d)

		DiscreteOptPolicy().optimise(d)

		printBlueBr("was ${a.genes.toList()}")
		printRedBr("became ${d.genes.toList()}")
	}

	@Test
	fun testNoiseOptPolicy() {
		val a = WeightGenes("a", floatArrayOf(0.1f, 0.2f, 0.6f, 0.7f, 0.9f, 0.3f, 0.66f))
		val d = a.copy()
		assertEqualModel(a, d)

		NoiseOptPolicy().optimise(d)

		printBlueBr("was ${a.genes.toList()}")
		printRedBr("became ${d.genes.toList()}")
	}

	@Test
	fun testOutlierOptPolicy() {
		val a = WeightGenes("a", floatArrayOf(0.1f, 0.2f, 0.6f, 0.7f, -0.9f, 0.3f, 0.66f))
		val d = a.copy()
		assertEqualModel(a, d)

		OutlierOptPolicy().optimise(d)

		printBlueBr("was ${a.genes.toList()}")
		printRedBr("became ${d.genes.toList()}")
	}

}