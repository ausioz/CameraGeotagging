package com.example.camerageotagging.ui.camera

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Exifdata(
    val fileName:String?,
    val filePath:String?,
    val fileSize:String?,
    val latitude:String?,
    val longitude:String?,
) : Parcelable
