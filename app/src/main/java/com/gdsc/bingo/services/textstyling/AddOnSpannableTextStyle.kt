package com.gdsc.bingo.services.textstyling

import android.graphics.Color
import android.graphics.Typeface
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AlignmentSpan
import android.text.style.QuoteSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import kotlin.math.max

class AddOnSpannableTextStyle {

    fun convertHtmlWithOrderedList(html: String): SpannableStringBuilder {
        val spanned = SpannableStringBuilder()
        var orderListCounter = 1
        var unorderedListCounter = 1
        val openTags = mutableListOf<String>()
        val nestedTagStack = mutableListOf<String>()

        val ksoupHtmlHandler = KsoupHtmlHandler
            .Builder()
            .onOpenTag { name, attributes, isImplied ->
                if (name == "br") {
                    return@onOpenTag
                }

                openTags.add(name)
                nestedTagStack.add(name)
                when (name) {
                    "ol" -> {
                        // Reset the counter for each new ordered list
                        orderListCounter = 1
                    }

                    "ul" -> {
                        // Reset the counter for each new unordered list
                        unorderedListCounter = 1
                    }

                    "li" -> {
                        // Check the parent tag to determine if it's an ordered list or unordered list
                        if (openTags.getOrNull(openTags.lastIndex - 1) == "ol") {
                            // Prepend the counter and a dot to each list item in an ordered list
                            spanned.append("$orderListCounter. ")
                            orderListCounter++
                        } else if (openTags.getOrNull(openTags.lastIndex - 1) == "ul") {
                            // Prepend a bullet point to each list item in an unordered list
                            spanned.append("• ")
                            unorderedListCounter++
                        }
                    }
                    "p", "div" -> {
                        val align = attributes["style"]
                        Log.d("AddOnSpannableTextStyle", "get Align $align")
                        if (align != null) {
                            openTags.add("$name-$align")
                        }
                    }
                }
            }
            .onCloseTag { name, isImplied ->
                openTags.removeLastOrNull()

                val currentStack = nestedTagStack.removeLastOrNull()

                currentStack?.let {
                    if (currentStack != name) {
                        nestedTagStack.add(currentStack)
                        return@onCloseTag
                    }

                    if (nestedTagStack.contains("div").not()
                        && nestedTagStack.contains("li").not()) {
                        spanned.append("\n")
                    } else if (currentStack == "li") {
                        spanned.append("\n")
                    }
                }


            }
            .onText { text ->
                // Append the text of each list item to the SpannableStringBuilder
                spanned.append(text)

                // Apply the appropriate styling based on the parent tag
                val start = max(0, spanned.length - text.length)
                val end = max(start, spanned.length)

                openTags.forEach { tag ->

                    when (tag) {
                        "h1" -> {
                            spanned.setSpan(
                                StyleSpan(Typeface.BOLD),
                                start,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            spanned.setSpan(
                                RelativeSizeSpan(2f),
                                start,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }

                        "h2" -> {
                            spanned.setSpan(
                                StyleSpan(Typeface.BOLD),
                                start,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            spanned.setSpan(
                                RelativeSizeSpan(1.5f),
                                start,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }

                        "h3" -> {
                            spanned.setSpan(
                                StyleSpan(Typeface.BOLD),
                                start,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            spanned.setSpan(
                                RelativeSizeSpan(1.17f),
                                start,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }

                        "h4" -> {
                            spanned.setSpan(
                                StyleSpan(Typeface.BOLD),
                                start,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            spanned.setSpan(
                                RelativeSizeSpan(1.12f),
                                start,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }

                        "h5" -> {
                            spanned.setSpan(
                                StyleSpan(Typeface.BOLD),
                                start,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            spanned.setSpan(
                                RelativeSizeSpan(1f),
                                start,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }

                        "h6" -> {
                            spanned.setSpan(
                                StyleSpan(Typeface.BOLD),
                                start,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            spanned.setSpan(
                                RelativeSizeSpan(0.83f),
                                start,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }

                        "b" -> {
                            spanned.setSpan(
                                StyleSpan(Typeface.BOLD),
                                start,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }

                        "i" -> {
                            spanned.setSpan(
                                StyleSpan(Typeface.ITALIC),
                                start,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }

                        "u" -> {
                            spanned.setSpan(
                                UnderlineSpan(),
                                start,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }

                        "blockquote" -> {
                            spanned.setSpan(
                                QuoteSpan(Color.LTGRAY),
                                start,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }

                        else -> {
                            Log.d("AddOnSpannableTextStyle", tag)
                            if (tag.startsWith("div-text")) {
                                val align = when (tag.split(":").last().trim().dropLast(1)) {
                                    "left" -> Layout.Alignment.ALIGN_NORMAL
                                    "center" -> Layout.Alignment.ALIGN_CENTER
                                    "right" -> Layout.Alignment.ALIGN_OPPOSITE
                                    else -> null
                                }
                                if (align != null) {
                                    spanned.setSpan(
                                        AlignmentSpan.Standard(align),
                                        start,
                                        end,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                }
                            }
                        }
                    }
                }
            }
            .build()

        val ksoupHtmlParser = KsoupHtmlParser(handler = ksoupHtmlHandler)
        ksoupHtmlParser.write(html)
        ksoupHtmlParser.end()

        return spanned
    }

}