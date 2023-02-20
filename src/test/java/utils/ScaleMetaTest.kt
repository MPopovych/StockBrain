package utils

import brain.utils.printGreenBr
import utils.frames.ColumnScaleFilter
import utils.frames.ScaleMetaType
import utils.frames.modelframe.ModelFrame
import utils.frames.modelframe.NamedPropGetter
import utils.frames.modelframe.NamedFrameAsset
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
		"INCR" to ScaleMetaType.Standardize,
		"MODU1" to ScaleMetaType.NormalizeZP,
		"MODU2" to ScaleMetaType.NormalizeNP,
		"UNCH" to ScaleMetaType.None
	)
	private val scaleFilter = ColumnScaleFilter.byTypeMap(testSamples, filterMap)

	@Test
	fun testScaleBuild() {
		for ((key, scale) in scaleFilter) {
			printGreenBr("K: ${key}:${scale.type} mean: ${scale.mean}, std: ${scale.std}, max: ${scale.max}, min: ${scale.min} ")
		}
		val inc = scaleFilter["INCR"] ?: throw IllegalStateException()
		assertEquals(100f, inc.max)
		assertEquals(1f, inc.min)
		assertEquals(50.5f, inc.mean) // n? = (n^2 + n) / 2, n = 100

		val mod1 = scaleFilter["MODU1"] ?: throw IllegalStateException()
		assertEquals(1f, mod1.max)
		assertEquals(0f, mod1.min)
		assertEquals(0.5f, mod1.mean)

		val mod2 = scaleFilter["MODU2"] ?: throw IllegalStateException()
		assertEquals(1f, mod2.max)
		assertEquals(0f, mod2.min)
		assertEquals(0.5f, mod2.mean)

		val unc = scaleFilter["UNCH"] ?: throw IllegalStateException()
		assertEquals(7f, unc.max)
		assertEquals(7f, unc.min)
		assertEquals(7f, unc.mean)
	}

	@Test
	fun testNoneNorm() {
		val unc = scaleFilter["UNCH"] ?: throw IllegalStateException()
		val uncArray = testSamples.getNumberColumn("UNCH") ?: throw IllegalStateException()
		unc.applyToArray(uncArray).forEach { fl ->
			assertEquals(7f, fl)
		}
	}

	@Test
	fun testZPNorm() {
		val mod1 = scaleFilter["MODU1"] ?: throw IllegalStateException()
		val mo1dArray = testSamples.getNumberColumn("MODU1") ?: throw IllegalStateException()
		mod1.applyToArray(mo1dArray).forEachIndexed { index, fl ->
			assert(fl >= -1 && fl <= 1)
		}
	}

	@Test
	fun testNPNorm() {
		val mod2 = scaleFilter["MODU2"] ?: throw IllegalStateException()
		val mo2dArray = testSamples.getNumberColumn("MODU2") ?: throw IllegalStateException()
		mod2.applyToArray(mo2dArray).forEachIndexed { index, fl ->
			assert(fl >= -1 && fl <= 1)
		}
	}

	@Test
	fun testNPStandardize() {
		val incArray = testSamples.getNumberColumn("INCR") ?: throw IllegalStateException()
		val inc = scaleFilter["INCR"] ?: throw IllegalStateException()

		var prev = -Float.MAX_VALUE
		inc.applyToArray(incArray).also {
			printGreenBr(it.toList())
		}.forEach { fl ->
			assert(fl >= -2 && fl <= 2)
			assert(fl > prev)
			prev = fl
		}
	}

	private object ScaleMetaTestGetter : NamedPropGetter<ScaleMetaTestAsset>() {
		val i = nameProp("INCR") { a -> a.increment }
		val m1 = nameProp("MODU1") { a -> a.mod }
		val m2 = nameProp("MODU2") { a -> a.mod }
		val u = nameProp("UNCH") { a -> a.unchanging }
	}

	private data class ScaleMetaTestAsset(
		val id: String,
		val increment: Float,
		val mod: Float,
		val unchanging: Float
	) : NamedFrameAsset<ScaleMetaTestAsset> {
		override fun propGetter(): NamedPropGetter<ScaleMetaTestAsset> {
			return ScaleMetaTestGetter
		}
	}
}