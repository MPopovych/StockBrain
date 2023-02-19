package utils.modelframe

import utils.frames.ColumnFilter
import utils.frames.ColumnScaleFilter
import utils.frames.ScaleMetaType
import utils.frames.modelframe.ModelFrame
import utils.frames.modelframe.NamedPropGetter
import utils.frames.modelframe.PropFrameModel
import utils.frames.modelframe.nameProp
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModelFrameTest {

	private val testSamples = (1..99).map {
		val pos = it.toDouble()
		val neg = it * -1.0
		ModelFrameTestAsset(id = it.toString(), pos = pos, neg = neg, Random.nextDouble(neg, pos))
	}.let { ModelFrame.from(it) }

	private val testSampleSingle = testSamples.first()

	private val filterMap = mapOf(
		"P" to ScaleMetaType.None,
		"R" to ScaleMetaType.None
	)
	private val scaleFilter = ColumnScaleFilter.build(filterMap, testSamples)

	@Test
	fun testToArray() {
		val array = testSampleSingle.to2FArray()
		assertEquals(3, array.size)
		assertEquals(1f, array[0])
		assertEquals(-1f, array[1])
		assertTrue {  array[2] in (-1.0 .. 1.0) }
	}

	@Test
	fun testToArrayWithFilter() {
		val array = testSampleSingle.to2FArray(scaleFilter)
		assertEquals(2, array.size)
		assertEquals(1f, array[0])
		assertTrue {  array[1] in (-1.0 .. 1.0) }
	}

	private object ModelFrameTestGetter: NamedPropGetter<ModelFrameTestAsset>() {
		val p = nameProp("P") { a -> a.pos }
		val n = nameProp("N") { a -> a.neg }
		val r = nameProp("R") { a -> a.random }
	}

	private data class ModelFrameTestAsset(
		val id: String,
		val pos: Double,
		val neg: Double,
		val random: Double
	): PropFrameModel<ModelFrameTestAsset> {
		override fun propGetter(): NamedPropGetter<ModelFrameTestAsset> {
			return ModelFrameTestGetter
		}
	}

}