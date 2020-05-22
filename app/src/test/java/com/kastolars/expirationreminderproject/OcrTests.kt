package com.kastolars.expirationreminderproject

import android.content.res.Resources
import android.graphics.Rect
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@Config(manifest = Config.NONE)
@RunWith(AndroidJUnit4::class)
class OcrTests {

    private val captureRegex =
        Regex(".*([0-9]{1,2}|[a-zA-Z]{3,})\\W{0,2}[0-9]{1,2}\\W{0,2}[0-9]{2,4}.*")
    private val cleanRegex = Regex("([0-9]{1,2}|[a-zA-Z]{3,})\\W{0,2}[0-9]{1,2}\\W{0,2}[0-9]{2,4}")

    inner class TestCase(val input: Any, val expected: Any)

    @Test
    fun testCapture() {
        val testStrings = arrayOf(
            "05-14-20",
            "JUN 05 2020 A 18:3",
            "by:05/22/20",
            "05 19 2020",
            "May 10, 2020"
        )

        for (s: String in testStrings) {
            assertTrue(captureRegex.matches(s))
        }
    }

    @Test
    fun testExtractClean() {
        val testCases = arrayOf(
            TestCase("05-14-20", "05-14-20"),
            TestCase("JUN 05 2020 A 18:3", "JUN 05 2020"),
            TestCase("by:05/22/20", "05/22/20"),
            TestCase("05 19 2020", "05 19 2020"),
            TestCase("May 10, 2020", "May 10, 2020")
        )

        for (tc: TestCase in testCases) {
            val match = cleanRegex.find(tc.input as CharSequence)!!
            assertEquals(tc.expected, match.value)
        }
    }

    @Test
    fun testSplit() {
        val testCases = arrayOf(
            TestCase("05-14-20", listOf("05", "14", "20")),
            TestCase("JUN 05 2020", listOf("JUN", "05", "2020")),
            TestCase("05/22/20", listOf("05", "22", "20")),
            TestCase("05 19 2020", listOf("05", "19", "2020")),
            TestCase("May 10, 2020", listOf("May", "10", "2020"))
        )

        for (tc: TestCase in testCases) {
            val splits = (tc.input as CharSequence).split(Regex("\\W{1,2}"))
            assertEquals(splits, tc.expected as List<*>)
        }
    }

    @Test
    fun testToDate() {
        val testCases = arrayOf(
            TestCase("05-14-20", listOf(4, 14, 2020)),
            TestCase("JUN 05 2020", listOf(5, 5, 2020)),
            TestCase("05/22/20", listOf(4, 22, 2020)),
            TestCase("05 19 2020", listOf(4, 19, 2020)),
            TestCase("May 10, 2020", listOf(4, 10, 2020))
        )
        val months = arrayOf(
            "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug",
            "sep", "oct", "nov", "dec"
        )
        val cal = Calendar.getInstance(TimeZone.getDefault())
        for (tc: TestCase in testCases) {
            var year: Int? = null
            var month: Int? = null
            var dayOfTheMonth: Int? = null
            val splits = (tc.input as CharSequence).split(Regex("\\W"))
            for (i in splits.indices) {
                if (splits[i].matches(Regex("[a-zA-z]+"))) {
                    for (j in 0..12) {
                        if (months[j].matches(Regex(splits[i], RegexOption.IGNORE_CASE))) {
                            month = j
                            break
                        }
                    }
                } else {
                    try {
                        val num = splits[i].toInt()
                        if (num > 31) {
                            year = num
                        } else {
                            when {
                                month == null -> {
                                    month = num - 1
                                }
                                dayOfTheMonth == null -> {
                                    dayOfTheMonth = num
                                }
                                year == null -> {
                                    year = 2000 + num
                                }
                            }
                        }
                    } catch (e: NumberFormatException) {
                        continue
                    }
                }
            }
            val actual = arrayOf(month, dayOfTheMonth, year).toList()
            assertEquals(tc.expected, actual)
        }
    }

    @Test
    fun testBoxes(){
        val deviceWidth = Resources.getSystem().displayMetrics.widthPixels
        val deviceHeight = Resources.getSystem().displayMetrics.heightPixels
        val left = deviceWidth / 6
        val top = (deviceHeight / 2) - 200
        val right = left + (deviceWidth / 6) * 4
        val bottom = top + 200
        val captureBox = Rect(left, top, right, bottom)
    }
}