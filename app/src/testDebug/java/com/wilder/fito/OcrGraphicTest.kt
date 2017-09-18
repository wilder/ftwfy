package com.wilder.fito

import junit.framework.Assert
import junit.framework.TestCase
import org.junit.Test

/**
 * Created by Wilder on 17/09/17.
 */
class OcrGraphicTest : TestCase(){

    @Test
    fun test_whenTextToFindIsSurroundedByExtraText_TrimReturnsLeftAndRightWidthToBeRemoved() {
        val characterSize = 2f
        var leftRight = trim(characterSize, matched = "haha oi meu nome é wilder haha", textToFind = "oi meu nome é wilder")
        Assert.assertEquals(leftRight[0], "haha ".length * characterSize)
        Assert.assertEquals(leftRight[1], " haha".length * characterSize)
    }

    @Test
    fun test_whenMatchedTextHasExtraTextInLeft_RightIsZero() {
        val sizePerText = 4f
        var leftRight = trim(sizePerText, matched = "haha oi meu nome é wilder", textToFind = "oi meu nome é wilder")
        Assert.assertEquals(leftRight[0], "haha ".length * sizePerText)
        Assert.assertEquals(leftRight[1], 0f)
    }

    @Test
    fun test_whenMatchedTextHasExtraTextInRight_LeftIsZero() {
        val sizePerText = 2f
        var leftRight = trim(sizePerText, matched = "I have extra text on right ahahah", textToFind = "I have extra text on right")
        Assert.assertEquals(leftRight[0], 0f)
        Assert.assertEquals(leftRight[1], " ahahah".length * sizePerText)
    }

    @Test
    fun test_whenMatchedTextHasNoExtraText_LeftAndRightAreZero() {
        val sizePerText = 3f
        var leftRight = trim(sizePerText, matched = "I have extra text on right", textToFind = "I have extra text on right")
        Assert.assertEquals(leftRight[0], 0f)
        Assert.assertEquals(leftRight[1], 0f)
    }

}