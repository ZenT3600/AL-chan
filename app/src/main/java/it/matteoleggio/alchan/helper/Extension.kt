package it.matteoleggio.alchan.helper

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.updatePadding
import com.google.gson.reflect.TypeToken
import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.data.response.FuzzyDate
import it.matteoleggio.alchan.helper.enums.AppColorTheme
import it.matteoleggio.alchan.helper.enums.BrowsePage
import it.matteoleggio.alchan.helper.pojo.InitialPadding
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import it.matteoleggio.alchan.helper.utils.DialogUtility
import it.matteoleggio.alchan.helper.utils.Utility
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


fun Activity.changeStatusBarColor(color: Int) {
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = color
}

fun Double.trimTrailingZero(): String {
    val bigDecimal = BigDecimal(this.toString()).stripTrailingZeros().toPlainString()
    return if (bigDecimal == "0.0") {
        "0"
    } else {
        bigDecimal
    }
}

fun Double.roundToOneDecimal(): String {
    val format = DecimalFormat("#.#")
    format.decimalFormatSymbols = DecimalFormatSymbols.getInstance().also { it.decimalSeparator = '.' }
    return format.format(this)
}

fun Double.roundToTwoDecimal(): String {
    val format = DecimalFormat("#.##")
    format.decimalFormatSymbols = DecimalFormatSymbols.getInstance().also { it.decimalSeparator = '.' }
    return format.format(this)
}

fun Int.secondsToDateTime(): String {
    val dateFormat = SimpleDateFormat(Constant.DATE_TIME_FORMAT, Locale.US)
    val date = Date(this * 1000L)
    return dateFormat.format(date)
}

fun Int.secondsToDate(): String {
    val dateFormat = SimpleDateFormat(Constant.DEFAULT_DATE_FORMAT, Locale.US)
    val date = Date(this * 1000L)
    return dateFormat.format(date)
}

fun FuzzyDate?.toMillis(): Long? {
    if (this?.year == null || month == null || day == null) {
        return null
    }

    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, year!!)
    calendar.set(Calendar.MONTH, month!! - 1)
    calendar.set(Calendar.DAY_OF_MONTH, day!!)

    return calendar.timeInMillis
}

fun String?.replaceUnderscore(): String {
    return this?.replace("_", " ") ?: ""
}

inline fun <reified T> genericType() = object: TypeToken<T>() {}.type

fun FuzzyDate?.toStringDateFormat(): String {
    if (this?.year == null || month == null || day == null) {
        return "-"
    }

    val dateFormat = SimpleDateFormat(Constant.DEFAULT_DATE_FORMAT, Locale.US)
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this.toMillis()!!

    return dateFormat.format(calendar.time)
}

fun Int.toHex(): String {
    return String.format("#%06X", 0xFFFFFF and this)
}

fun Long.toAlphaHex(): String {
    return String.format("#%08X", 0xFFFFFFFF and this)
}

fun String?.setRegularPlural(count: Int?): String {
    if (this == null) return ""
    if (count == null || abs(count) == 1) return this

    // TODO: add more rules

    return if (endsWith("ay") || endsWith("ey") || endsWith("iy") || endsWith("oy") || endsWith("uy")) {
        this + "s"
    } else if (endsWith("y")) {
        replace(Regex("y$"), "ies")
    } else if (endsWith("s") || endsWith("x") || endsWith("z") || endsWith("ch") || endsWith("sh")) {
        this + "es"
    } else {
        this + "s"
    }
}

fun String?.handleSpoilerAndLink(context: Context, urlAction: (BrowsePage, Int) -> Unit): SpannableString {
    if (this == null) return SpannableString(context.getString(R.string.no_description))

    val spoilerRegex = "(?<=<span class='markdown_spoiler'><span>).+?(?=<\\/span><\\/span>)".toRegex()
    val urlRegex = "(?<=<a href=\").+?(?=\">)".toRegex()
    val linkRegex =  "(?<=\">).+?(?=<\\/a>)".toRegex()

    val spoilerTag = "[Spoiler]"

    // find all spoilers
    val spoilerList = spoilerRegex.findAll(this).toList()

    // replace spoilers into [Spoiler]
    val spoilerDescription = replace(spoilerRegex, spoilerTag)

    // find all links
    val urlList = urlRegex.findAll(spoilerDescription).toList()
    val linkList = linkRegex.findAll(spoilerDescription).toList()

    // convert html format to spannable string
    val spanned = HtmlCompat.fromHtml(spoilerDescription, HtmlCompat.FROM_HTML_MODE_LEGACY)
    val spannableString = SpannableString(spanned.toString())

    // handle spannable string for spoilers
    var findSpoilerIndex = spanned.indexOf(spoilerTag)
    var counter = 0
    while (findSpoilerIndex != -1) {
        val clickableSpan = object : ClickableSpan() {
            val internalCounter = counter
            override fun onClick(widget: View) {
                DialogUtility.showInfoDialog(context, HtmlCompat.fromHtml(spoilerList[internalCounter].value, HtmlCompat.FROM_HTML_MODE_LEGACY).toString())
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color =  AndroidUtility.getResValueFromRefAttr(context, R.attr.themeSecondaryColor)
            }
        }

        spannableString.setSpan(clickableSpan, findSpoilerIndex, findSpoilerIndex + spoilerTag.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        counter += 1
        findSpoilerIndex = spanned.indexOf(spoilerTag, findSpoilerIndex + spoilerTag.length)
    }

    // handle spannable string for links
    if (!linkList.isNullOrEmpty()) {
        counter = 0
        var findLinkIndex = spanned.indexOf(linkList[counter].value)

        while (findLinkIndex != -1) {
            val clickableSpan = object : ClickableSpan() {
                val internalCounter = counter

                override fun onClick(widget: View) {
                    val url = urlList[internalCounter].value
                    if (url.contains(Constant.ANILIST_URL) && url.contains(Regex("((anime)|(manga)|(character)|(staff)|(studio))/[0-9]+"))) {
                        val urlSplit = url.substringAfter(Constant.ANILIST_URL).split("/")
                        urlAction(BrowsePage.valueOf(urlSplit[0].toUpperCase()), urlSplit[1].toInt())
                    } else {
                        CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(urlList[internalCounter].value))
                    }
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                    ds.color =  AndroidUtility.getResValueFromRefAttr(context, R.attr.themeSecondaryColor)
                }
            }

            spannableString.setSpan(clickableSpan, findLinkIndex, findLinkIndex + linkList[counter].value.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            counter += 1
            findLinkIndex = if (counter < linkList.size) {
                spanned.indexOf(linkList[counter].value, findLinkIndex + linkList[counter - 1].value.length)
            } else {
                -1
            }
        }
    }

    return spannableString
}

private fun recordInitialPadding(view: View) = InitialPadding(
    view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom
)

fun View.doOnApplyWindowInsets(f: (View, WindowInsets, InitialPadding) -> Unit) {
    val initialPadding = recordInitialPadding(this)
    setOnApplyWindowInsetsListener { v, insets ->
        f(v, insets, initialPadding)
        insets
    }
    requestApplyInsets()
}

fun View.updateAllPadding(windowInsets: WindowInsets, initialPadding: InitialPadding) {
    if (windowInsets.systemWindowInsetTop != 0 ||
        windowInsets.systemWindowInsetBottom != 0 ||
        windowInsets.systemWindowInsetLeft != 0 ||
        windowInsets.systemWindowInsetRight != 0
    ) {
        updatePadding(
            left = initialPadding.left + windowInsets.systemWindowInsetLeft,
            top = initialPadding.top + windowInsets.systemWindowInsetTop,
            right = initialPadding.right + windowInsets.systemWindowInsetRight,
            bottom = initialPadding.bottom + windowInsets.systemWindowInsetBottom
        )
    }
}

fun View.updateTopPadding(windowInsets: WindowInsets, initialPadding: InitialPadding) {
    if (windowInsets.systemWindowInsetTop != 0 ||
        windowInsets.systemWindowInsetBottom != 0 ||
        windowInsets.systemWindowInsetLeft != 0 ||
        windowInsets.systemWindowInsetRight != 0
    ) {
        updatePadding(top = initialPadding.top + windowInsets.systemWindowInsetTop)
    }
}

fun View.updateBottomPadding(windowInsets: WindowInsets, initialPadding: InitialPadding) {
    if (windowInsets.systemWindowInsetTop != 0 ||
        windowInsets.systemWindowInsetBottom != 0 ||
        windowInsets.systemWindowInsetLeft != 0 ||
        windowInsets.systemWindowInsetRight != 0
    ) {
        updatePadding(bottom = initialPadding.bottom + windowInsets.systemWindowInsetBottom)
    }
}

fun View.updateSidePadding(windowInsets: WindowInsets, initialPadding: InitialPadding) {
    if (windowInsets.systemWindowInsetTop != 0 ||
        windowInsets.systemWindowInsetBottom != 0 ||
        windowInsets.systemWindowInsetLeft != 0 ||
        windowInsets.systemWindowInsetRight != 0
    ) {
        updatePadding(
            left = initialPadding.left + windowInsets.systemWindowInsetLeft,
            right = initialPadding.right + windowInsets.systemWindowInsetRight
        )
    }
}
