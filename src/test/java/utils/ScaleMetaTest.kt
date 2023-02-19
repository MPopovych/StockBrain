package utils

import brain.utils.printGreenBr
import utils.frames.ColumnScaleFilter
import utils.frames.ScaleMetaType
import utils.frames.modelframe.ModelFrame
import utils.frames.modelframe.NamedPropGetter
import utils.frames.modelframe.PropFrameModel
import utils.frames.modelframe.nameProp
import kotlin.test.Test
import kotlin.test.assertEquals

class ScaleMetaTest {

	private val testSamples = (1..100).map {
		return@map ScaleMetaTestAsset(
			id = it.toString(),
			increment = it.toFloat(),
			mod = it % 2f,
			unchanging = 7f
		)
	}.let { ModelFrame.from(it) }

	private val filterMap = mapOf(
		"INCR" to ScaleMetaType.NormalizeZP,
		"MODU" to ScaleMetaType.Standardize,
		"UNCH" to ScaleMetaType.None
	)
	private val scaleFilter = ColumnScaleFilter.build(filterMap, testSamples)

	@Test
	fun testScaleBuild() {
		for ((key, scale) in scaleFilter) {
			printGreenBr("K: ${key}:${scale.type} mean: ${scale.mean}, std: ${scale.std}, max: ${scale.max}, min: ${scale.min} ")
		}
		val inc = scaleFilter["INCR"] ?: throw IllegalStateException()
		assertEquals(100f, inc.max)
		assertEquals(1f, inc.min)
		assertEquals(50.5f, inc.mean) // n? = (n^2 + n) / 2, n = 100

		val mod = scaleFilter["MODU"] ?: throw IllegalStateException()
		assertEquals(1f, mod.max)
		assertEquals(0f, mod.min)
		assertEquals(0.5f, mod.mean)

		val unc = scaleFilter["UNCH"] ?: throw IllegalStateException()
		assertEquals(7f, unc.max)
		assertEquals(7f, unc.min)
		assertEquals(7f, unc.mean)

		val incArray = testSamples.getNumberColumn("INCR") ?: throw IllegalStateException()
		val modArray = testSamples.getNumberColumn("MODU") ?: throw IllegalStateException()
		val uncArray = testSamples.getNumberColumn("UNCH") ?: throw IllegalStateException()

		inc.applyToArray(incArray).forEachIndexed { index, fl ->
			assert(fl >= -2 && fl <= 2)
		}

		mod.applyToArray(modArray).forEachIndexed { index, fl ->
			assert(fl >= -1 && fl <= 1)
		}

		unc.applyToArray(uncArray).forEach { fl ->
			assertEquals(7f, fl)
		}
	}

	@Test
	fun testZPNorm() {

	}

	@Test
	fun testNPNorm() {

	}

	@Test
	fun testNPStandardize() {

	}

	private object ScaleMetaTestGetter : NamedPropGetter<ScaleMetaTestAsset>() {
		val i = nameProp("INCR") { a -> a.increment }
		val m = nameProp("MODU") { a -> a.mod }
		val u = nameProp("UNCH") { a -> a.unchanging }
	}

	private data class ScaleMetaTestAsset(
		val id: String,
		val increment: Float,
		val mod: Float,
		val unchanging: Float
	) : PropFrameModel<ScaleMetaTestAsset> {
		override fun propGetter(): NamedPropGetter<ScaleMetaTestAsset> {
			return ScaleMetaTestGetter
		}
	}
}