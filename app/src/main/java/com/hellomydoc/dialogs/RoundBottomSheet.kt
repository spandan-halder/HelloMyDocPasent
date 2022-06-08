package com.hellomydoc.dialogs

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.content.DialogInterface
import com.hellomydoc.R

class RoundBottomSheet(context: Context, private val listener: (()->Unit)?) : BottomSheetDialog(context,R.style.BaseBottomSheetDialog) {
    init {
        setOnDismissListener {
            listener?.invoke()
        }
    }
}