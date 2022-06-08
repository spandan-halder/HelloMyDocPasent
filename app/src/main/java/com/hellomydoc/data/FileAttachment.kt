package com.hellomydoc.data

import android.net.Uri
import java.io.File

data class FileAttachment(
    val uri: Uri?,
    val file: File,
    val name: String,
    val mimeType: String
)
