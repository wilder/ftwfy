package com.wilder.fito

/**
 * Created by Wilder on 17/09/17.
 */
/**
 * returns the width of the extra text at the right or left of the textToFindText
 */
fun trim(characterSize: Float, matched: String, textToFind: String) : List<Float> {
    var left = 0
    var j = 0
    for (i in 0 until matched.length) {
        if (i == textToFind.length) {
            return listOf(characterSize*left, characterSize*matched.substring(i+left).length)
        }
        if(matched[i].equals(textToFind[j])) {
            j += 1
        } else {
            left += 1
            j = 0
        }
    }
    return listOf(0f, 0f)
}