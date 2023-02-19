package utils.dataframe

import utils.frames.ColumnFilter
import utils.frames.dataframe.DFProcessor
import utils.frames.dataframe.DataFrame
import kotlin.test.Test
import kotlin.test.assertEquals

class DataFrameTest {

	@Test
	fun testDataFrame() {
		val df = DataFrame()
		df.addColumn("ids", arrayOf("0", "1", "2"))
		df.addColumn("test1", arrayOf(1.0, 2.0, 3.0))
		println(df.getHeadString())
		df.addColumn("test2", arrayOf(-1.0, -2.0, -3.0))
		println(df.getHeadString())

		val scale = DFProcessor.buildNPScaleOnDF(df)
		println(scale)

		scale.applyToDf(df)

		println(df.getHeadString())
		scale.save("test_scaler_df")

		val copy = DFProcessor.load("test_scaler_df")
		println(copy)
	}

	@Test
	fun testWindow() {
		val df = DataFrame()
		df.addColumn("ids", arrayOf("0", "1", "2", "3", "4"))
		df.addColumn("test1", arrayOf(1.0, 2.0, 3.0, 4.0, 5.0))
		df.addColumn("test2", arrayOf(-1.0, -2.0, -3.0, -4.0, -5.0))

		val windows = df.window(2)
		assertEquals(4, windows.size)
		windows.forEach {
			println(it.describe())
			it.to2fArray().forEach { s -> println(s.toList()) }
		}
	}

	@Test
	fun testWindowSingle() {
		val df = DataFrame()
		df.addColumn("ids", arrayOf("0", "1", "2", "3", "4"))
		df.addColumn("test1", arrayOf(1.0, 2.0, 3.0, 4.0, 5.0))
		df.addColumn("test2", arrayOf(-1.0, -2.0, -3.0, -4.0, -5.0))

		val windows = df.window(1)
		assertEquals(5, windows.size)
		windows.forEach {
			println(it.describe())
			it.to2fArray().forEach { s -> println(s.toList()) }
		}
	}

	@Test
	fun testWindowGap() {
		val df = DataFrame()
		df.addColumn("ids", arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"))
		df.addColumn("test1", arrayOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0))
		df.addColumn("test2", arrayOf(-1.0, -2.0, -3.0, -4.0, -5.0, -6.0, -7.0, -8.0, -9.0, 10.0))

		val windows = df.window(3, gapSize = 3)
		assertEquals(4, windows.size)
		windows.forEach {
			println(it.describe())
			it.to2fArray().forEach { s -> println(s.toList()) }
		}
	}

	@Test
	fun testFilter() {
		val df = DataFrame()
		df.addColumn("ids", arrayOf("0", "1", "2", "3", "4"))
		df.addColumn("test1", arrayOf(1.0, 2.0, 3.0, 4.0, 5.0))
		df.addColumn("test2", arrayOf(-1.0, -2.0, -3.0, -4.0, -5.0))

		val filter = ColumnFilter(listOf("test1"))
		filter.applyToDf(df)

		println(df.getHeadString())
	}
}