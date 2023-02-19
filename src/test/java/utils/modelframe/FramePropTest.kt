package utils.modelframe

import utils.frames.modelframe.NamedPropGetter
import utils.frames.modelframe.PropFrameModel
import utils.frames.modelframe.nameProp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull


class FramePropTest {

	private val single = FramePropTestAsset(0.0f, -1.0)

	@Test
	fun assetTest() {
		assertEquals(-1.0, FramePropTestAssetGetter.testValue.get(single))
		assertEquals(-1.0, single.getValueByKey("TEST")?.toDouble())
		assertNull(single.getValueByKey("NOT_EXISTING"))
		single.to2FArray()
	}

}



object FramePropTestAssetGetter : NamedPropGetter<FramePropTestAsset>() {
	val testValue = nameProp("TEST") { a -> a.testDouble }
}

data class FramePropTestAsset(val testFloat: Float, val testDouble: Double) : PropFrameModel<FramePropTestAsset> {
	override fun propGetter(): NamedPropGetter<FramePropTestAsset> {
		return FramePropTestAssetGetter
	}
}