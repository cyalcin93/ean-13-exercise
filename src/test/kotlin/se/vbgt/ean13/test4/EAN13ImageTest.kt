package se.vbgt.ean13.test4

import org.junit.jupiter.api.Test
import se.vbgt.ean13.EAN13

private const val directoryPath = "src/test/resources"

class EAN13ImageTest {

    @Test
    fun `save image 1`() {
        EAN13("1234567890128").saveImageTo("$directoryPath/image1.png")
    }

    @Test
    fun `ben n jerry cookie dough`() {
        EAN13("0076840600021").saveImageTo("$directoryPath/cookiedough.png")
    }


}