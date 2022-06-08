package com.hellomydoc

import android.content.Context
import android.graphics.Typeface

object Tyfo {
    fun setDefaultFont(context: Context, staticTypefaceFieldName: String?, fontAssetName: String?) {
        val regular = Typeface.createFromAsset(context.assets, fontAssetName)
        replaceFont(staticTypefaceFieldName, regular)
    }

    internal fun replaceFont(staticTypefaceFieldName: String?, newTypeface: Typeface?) {
        try {
            val staticField = Typeface::class.java.getDeclaredField(staticTypefaceFieldName)
            staticField.isAccessible = true
            staticField[null] = newTypeface
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun overrideFont(context: Context, defaultFontNameToOverride: String?, customFontFileNameInAssets: String?) {
        try {
            val customFontTypeface = Typeface.createFromAsset(context.assets, customFontFileNameInAssets)
            val defaultFontTypefaceField = Typeface::class.java.getDeclaredField(defaultFontNameToOverride)
            defaultFontTypefaceField.isAccessible = true
            defaultFontTypefaceField[null] = customFontTypeface
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}