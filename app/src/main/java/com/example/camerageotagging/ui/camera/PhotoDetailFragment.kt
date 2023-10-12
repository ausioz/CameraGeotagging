package com.example.camerageotagging.ui.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.camerageotagging.databinding.FragmentPhotoDetailBinding


class PhotoDetailFragment : Fragment() {
    private var _photoDetailBinding: FragmentPhotoDetailBinding? = null
    private val photoDetailBinding get() = _photoDetailBinding!!
    private val args: PhotoDetailFragmentArgs by navArgs()
    private val exifMetadata by lazy { args.fileMetadata }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _photoDetailBinding = FragmentPhotoDetailBinding.inflate(inflater, container, false)
        return photoDetailBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        _photoDetailBinding?.filenameTV?.text = exifMetadata?.fileName
        _photoDetailBinding?.filePathTV?.text = exifMetadata?.filePath
        _photoDetailBinding?.fileSizeTV?.text = exifMetadata?.fileSize
        _photoDetailBinding?.latitudeValTV?.text = getLatitude()
        _photoDetailBinding?.longitudeValTV?.text = getLongitude()
    }

    private fun getLatitude(): String? {
        return if (exifMetadata?.latitudeRef.equals("S")) {
            "-" + exifMetadata?.latitude
        } else exifMetadata?.latitude
    }

    private fun getLongitude(): String? {
        return if (exifMetadata?.longitudeRef.equals("W")) ({
            (360 - (exifMetadata?.longitude?.toDouble() ?: 0.0))
        }).toString() else {
            exifMetadata?.longitude
        }
    }


}