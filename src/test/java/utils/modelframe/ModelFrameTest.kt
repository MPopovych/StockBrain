package utils.modelframe

import brain.utils.printBlueBr
import brain.utils.printGreenBr
import brain.utils.printRedBr
import utils.frames.ColumnScaleFilter
import utils.frames.ScaleMetaType
import utils.frames.modelframe.ModelFrame
import utils.frames.modelframe.NamedPropGetter
import utils.frames.modelframe.NamedFrameAsset
import utils.frames.modelframe.nameProp
import kotlin.random.Random
import kotlin.test.*

class ModelFrameTest {

	private val testSamples = (1..99).map {
		val pos = it.toDouble()
		val neg = it * -1.0
		ModelFrameTestAssetFrameData(id = it.toString(), pos = pos, neg = neg, Random.nextDouble(neg, pos))
	}.let { ModelFrame.from(it) }

	private val testSampleSingle = testSamples.first()

	private val filterMap = mapOf(
		"P" to ScaleMetaType.None,
		"R" to ScaleMetaType.None
	)
	private val scaleFilter = ColumnScaleFilter.byTypeMap(testSamples, filterMap)

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
		val array = testSampleSingle.to2FArray(scaleFilter.keys)
		assertEquals(2, array.size)
		assertEquals(1f, array[0])
		assertTrue {  array[1] in (-1.0 .. 1.0) }
	}

	@Test
	fun testWindowBack() {
		val lastIndex = testSamples.size - 1
		printBlueBr("Last index: $lastIndex")
		val lastWindow = testSamples.getBackWindow(lastIndex, windowSize = 2)
		assertNotNull(lastWindow)

		printGreenBr(lastWindow.describe())
		val lastArray = lastWindow.to2fArray()
		assertEquals(2, lastArray.size)
		printRedBr(lastArray.map { it.toList() })
		assertEquals(99f, lastArray[1][0])
		assertEquals(98f, lastArray[0][0])
		assertEquals(-99f, lastArray[1][1])
		assertEquals(-98f, lastArray[0][1])

		val firstWindow = testSamples.getBackWindow(0, windowSize = 2)
		assertNull(firstWindow)

		val secondWindow = testSamples.getBackWindow(1, windowSize = 2)
		assertNotNull(secondWindow)
	}

	@Test
	fun testWindowForward() {
		val windowList = testSamples.windowList(windowSize = 2)
		val lastWindow = windowList.last()
		assertNotNull(lastWindow)

		printGreenBr(lastWindow.describe())
		val lastArray = lastWindow.to2fArray()
		assertEquals(2, lastArray.size)
		printRedBr(lastArray.map { it.toList() })
		assertEquals(99f, lastArray[1][0])
		assertEquals(98f, lastArray[0][0])
		assertEquals(-99f, lastArray[1][1])
		assertEquals(-98f, lastArray[0][1])

		val firstWindow = windowList.first()
		assertNotNull(firstWindow)
		val firstArray = firstWindow.to2fArray()
		assertEquals(2, firstArray.size)
		printRedBr(firstArray.map { it.toList() })
		assertEquals(2f, firstArray[1][0])
		assertEquals(1f, firstArray[0][0])
		assertEquals(-2f, firstArray[1][1])
		assertEquals(-1f, firstArray[0][1])
	}

	private object ModelFrameTestGetter: NamedPropGetter<ModelFrameTestAssetFrameData>() {
		val p = nameProp("P") { a -> a.pos }
		val n = nameProp("N") { a -> a.neg }
		val r = nameProp("R") { a -> a.random }
	}

	private data class ModelFrameTestAssetFrameData(
		val id: String,
		val pos: Double,
		val neg: Double,
		val random: Double
	): NamedFrameAsset<ModelFrameTestAssetFrameData> {
		override fun propGetter(): NamedPropGetter<ModelFrameTestAssetFrameData> {
			return ModelFrameTestGetter
		}
	}

}