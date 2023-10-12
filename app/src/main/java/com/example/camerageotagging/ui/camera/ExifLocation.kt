package com.example.camerageotagging.ui.camera

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExifLocation(
    val fileName:String?,
    val filePath:String?,
    val fileSize:String?,
    val latitude:String?,
    val latitudeRef:String?,
    val longitude:String?,
    val longitudeRef:String?
) : Parcelable
