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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _photoDetailBinding = FragmentPhotoDetailBinding.inflate(inflater, container, false)
        return photoDetailBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        for (i in 0 until args.fileMetadata.size) {
            _photoDetailBinding?.detailTV?.append(args.fileMetadata[i])
            _photoDetailBinding?.detailTV?.append("\n")
        }
    }

}