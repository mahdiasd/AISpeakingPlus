package ir.aispeaking.sharedui.ui.extension

fun String.isPersian(): Boolean {
    val persianCharacters = arrayOf(
        'ا', 'ب', 'پ', 'ت', 'ث', 'ج', 'چ', 'ح', 'خ', 'د', 'ذ', 'ر', 'ز', 'ژ', 'س', 'ش', 'ص', 'ض', 'ط', 'ظ',
        'ع', 'غ', 'ف', 'ق', 'ک', 'گ', 'ل', 'م', 'ن', 'و', 'ه', 'ی', 'آ', 'ء', 'ؤ', 'ئ'
    )
    this.forEach {
        if (it in persianCharacters) return true
    }
    return false
}

fun String.isSpaceDetect(): Boolean {
    return this.contains(" ")
}