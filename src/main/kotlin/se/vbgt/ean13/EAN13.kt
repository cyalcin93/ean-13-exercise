package se.vbgt.ean13

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
            else -> "not valid" // beautiful... not
        }

        private fun getRCode(digit: Char): String = rCodeDigitMap[Character.getNumericValue(digit)]
                                                                    ?: error("failed to retrieve r code")
        private fun String.invertedRCode() =
            this.toCharArray().map { if( it == '0') 1 else 0 }.joinToString("")
    }

    fun saveImageTo(path: String): Unit = TODO("Bonus")

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