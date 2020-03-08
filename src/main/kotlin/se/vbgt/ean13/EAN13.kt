package se.vbgt.ean13

import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class EAN13(number: String) {
    private val ean13Number = number.replace(" ", "")

    init {
        require(ean13Number.matches("\\d{13}".toRegex()))
        require(correctCheckDigit(ean13Number))
    }

    fun groups(): String = when (Character.getNumericValue(ean13Number.first())) {
        0 -> "LLLLLLRRRRRR"
        1 -> "LLGLGGRRRRRR"
        2 -> "LLGGLGRRRRRR"
        3 -> "LLGGGLRRRRRR"
        4 -> "LGLLGGRRRRRR"
        5 -> "LGGLLGRRRRRR"
        6 -> "LGGGLLRRRRRR"
        7 -> "LGLGLGRRRRRR"
        8 -> "LGLGGLRRRRRR"
        9 -> "LGGLGLRRRRRR"
        else -> throw InvalidGroupException()
    }

    fun modules(): String = Module(ean13Number, groups()).toString()

    companion object {
        private val rCodeDigitMap = mapOf(0 to "1110010", 1 to "1100110", 2 to "1101100", 3 to "1000010",
            4 to "1011100", 5 to "1001110", 6 to "1010000", 7 to "1000100", 8 to "1001000", 9 to "1110100")

        fun mapModule(group: Char, digit: Char): String = when (group) {
            'R' -> getRCode(digit).getStringRepresentation()
            'L' -> getRCode(digit).invertedRCode().getStringRepresentation()
            'G' -> getRCode(digit).reversed().getStringRepresentation()
            else -> throw InvalidGroupException()
        }

        private fun getRCode(digit: Char): String = rCodeDigitMap[Character.getNumericValue(digit)]
                                                                    ?: error("failed to retrieve r code")
        private fun String.invertedRCode() =
            this.toCharArray().map { if( it == '0') 1 else 0 }.joinToString("")
    }

    fun saveImageTo(path: String) {
        val (image, imageG) = getEmptyImageGraphics(95, 40)
        val (imageQuietZone, imageQuietZoneG) = getEmptyImageGraphics(95, 10)
        modules().forEachIndexed { i, c ->
            if (c == '|') {
                imageG.drawLine(i, 0, i+1, 80)
                if (i in (0..2) || i in (45..49) || i in (93..95))
                    imageQuietZoneG.drawLine(i, 0, i+1, 20)
            }
        }

        val (combined, combinedG) = getEmptyImageGraphics(102, 55)
        combinedG.drawImage(image, 5, 2, null)
        combinedG.drawImage(imageQuietZone, 5, 42, null)

        combinedG.color = Color.BLACK
        combinedG.font = Font("TimesRoman", Font.PLAIN, 10)
        combinedG.drawString(ean13Number.substring(0, 1), 0, 51)
        combinedG.drawString(ean13Number.substring(1, 7).toCharArray().joinToString(""), 12, 51)
        combinedG.drawString(ean13Number.substring(7, 13).toCharArray().joinToString(""), 59, 51)

        ImageIO.write(combined, "png", File(path))
    }

    private fun getEmptyImageGraphics(width: Int, height: Int): Pair<BufferedImage, Graphics> {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = image.graphics
        graphics.fillRect(0, 0, width, height) // white bg
        graphics.color = Color.BLACK // black lines

        return Pair(image, graphics)
    }

    private fun correctCheckDigit(ean13Number: String): Boolean {
        val checkDigit = Character.getNumericValue(ean13Number.last())

        val checkSum = ean13Number
            .map { Character.getNumericValue(it) }
            .dropLast(1)
            .mapIndexed { i, value -> if (i % 2 == 0) value * 1 else value * 3 }
            .sum()

        return (checkSum + checkDigit) % 10 == 0
    }
}

class Module(ean13Number: String, groups: String) {
    private val startMarker = "101".getStringRepresentation()
    private val centerMarker = "01010".getStringRepresentation()
    private val endMarker = "101".getStringRepresentation()
    private val twoToSeven = ean13Number
        .substring(1, 7)
        .toCharArray()
        .mapIndexed{ i, c -> EAN13.mapModule(group = groups[i], digit = c) }
        .joinToString("")

    private val eightToThirteen: String = ean13Number
        .substring(7)
        .toCharArray()
        .joinToString("") { EAN13.mapModule('R', it) }

    override fun toString() = "$startMarker$twoToSeven$centerMarker$eightToThirteen$endMarker"
}


class InvalidGroupException : Exception("first character of the ean13 " +
        "needs to be an integer between 0-9")

fun String.getStringRepresentation() =
    this.replace("0", " ").replace("1", "|")