package utils.modelframe

import utils.frames.modelframe.NamedPropGetter
import utils.frames.modelframe.NamedPropModel
import utils.frames.modelframe.nameProp
import kotlin.test.Test
import kotlin.test.assertEquals


class NamedPropTest {

	private val single = NamedPropTestAsset(-1f, -1.0)

	@Test
	fun assetTest() {
		assertEquals(-1.0, NamedPropTestAssetGetter.testValue.get(single))
	}

}

object NamedPropTestAssetGetter : NamedPropGetter<NamedPropTestAsset>() {
	val testValue = nameProp("TEST") { a -> a.testDouble }
}

data class NamedPropTestAsset(val testFloat: Float, val testDouble: Double) : NamedPropModel<NamedPropTestAsset> {

	override fun propGetter(): NamedPropGetter<NamedPropTestAsset> {
		return NamedPropTestAssetGetter
	}
}