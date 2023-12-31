package com.example.camerageotagging.ui.camera

import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.camerageotagging.R
import com.example.camerageotagging.databinding.FragmentGalleryBinding
import com.example.camerageotagging.utils.MediaStoreFile
import com.example.camerageotagging.utils.MediaStoreUtils
import com.example.camerageotagging.utils.padWithDisplayCutout
import com.example.camerageotagging.utils.showImmersive
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch


/** Fragment used to present the user with a gallery of photos taken */
class GalleryFragment internal constructor() : Fragment() {

    /** Android ViewBinding */
    private var _fragmentGalleryBinding: FragmentGalleryBinding? = null

    private val fragmentGalleryBinding get() = _fragmentGalleryBinding!!

    /** AndroidX navigation arguments */
    private val args: GalleryFragmentArgs by navArgs()

    private var mediaList: MutableList<MediaStoreFile> = mutableListOf()
    private var hasMediaItems = CompletableDeferred<Boolean>()

    /** Adapter class used to present a fragment containing one photo or video as a page */
    inner class MediaPagerAdapter(
        fm: FragmentManager, private var mediaList: MutableList<MediaStoreFile>
    ) : FragmentStateAdapter(fm, lifecycle) {
        override fun getItemCount(): Int = mediaList.size
        override fun createFragment(position: Int): Fragment =
            PhotoFragment.create(mediaList[position])

        override fun getItemId(position: Int): Long {
            return mediaList[position].id
        }

        override fun containsItem(itemId: Long): Boolean {
            return null != mediaList.firstOrNull { it.id == itemId }
        }

        fun setMediaListAndNotify(mediaList: MutableList<MediaStoreFile>) {
            this.mediaList = mediaList
            notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            // Get images this app has access to from MediaStore
            mediaList = MediaStoreUtils(requireContext()).getImages()
            (fragmentGalleryBinding.photoViewPager.adapter as MediaPagerAdapter).setMediaListAndNotify(
                mediaList
            )
            hasMediaItems.complete(mediaList.isNotEmpty())
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _fragmentGalleryBinding = FragmentGalleryBinding.inflate(inflater, container, false)
        return fragmentGalleryBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            fragmentGalleryBinding.deleteButton.isEnabled = hasMediaItems.await()
            fragmentGalleryBinding.detailButton.isEnabled = hasMediaItems.await()
        }

        // Populate the ViewPager and implement a cache of two media items
        fragmentGalleryBinding.photoViewPager.apply {
            offscreenPageLimit = 2
            adapter = MediaPagerAdapter(childFragmentManager, mediaList)
        }

        // Make sure that the cutout "safe area" avoids the screen notch if any
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Use extension method to pad "inside" view containing UI using display cutout's bounds
            fragmentGalleryBinding.cutoutSafeArea.padWithDisplayCutout()
        }

        // Handle back button press
        fragmentGalleryBinding.backButton.setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.fragment_container).navigateUp()
        }

        // Handle share button press
//        fragmentGalleryBinding.shareButton.setOnClickListener {
//
//            mediaList.getOrNull(fragmentGalleryBinding.photoViewPager.currentItem)
//                ?.let { mediaStoreFile ->
//                    val mediaFile = mediaStoreFile.file
//                    // Create a sharing intent
//                    val intent = Intent().apply {
//                        // Infer media type from file extension
//                        val mediaType = MimeTypeMap.getSingleton()
//                            .getMimeTypeFromExtension(mediaFile.extension)
//                        // Set the appropriate intent extra, type, action and flags
//                        type = mediaType
//                        action = Intent.ACTION_SEND
//                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//                        putExtra(Intent.EXTRA_STREAM, mediaStoreFile.uri)
//                    }
//
//
//
//                    // Launch the intent letting the user choose which app to share with
//                    startActivity(Intent.createChooser(intent, "Share using"))
//                }
//        }

        // Handle delete button press
        fragmentGalleryBinding.deleteButton.setOnClickListener {

            mediaList.getOrNull(fragmentGalleryBinding.photoViewPager.currentItem)
                ?.let { mediaStoreFile ->
                    val mediaFile = mediaStoreFile.file

                    AlertDialog.Builder(view.context, android.R.style.Theme_Material_Dialog)
                        .setTitle("Confirm Delete").setMessage("Are you sure?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.ok) { _, _ ->

                            // Delete current photo
                            mediaFile.delete()

                            // Send relevant broadcast to notify other apps of deletion
                            MediaScannerConnection.scanFile(
                                view.context, arrayOf(mediaFile.absolutePath), null, null
                            )

                            // Notify our view pager
                            mediaList.removeAt(fragmentGalleryBinding.photoViewPager.currentItem)
                            fragmentGalleryBinding.photoViewPager.adapter?.notifyDataSetChanged()

                            // If all photos have been deleted, return to camera
                            if (mediaList.isEmpty()) {
                                Navigation.findNavController(
                                    requireActivity(), R.id.fragment_container
                                ).navigateUp()
                            }

                        }.setNegativeButton(android.R.string.cancel, null).create().showImmersive()
                }
        }


        fragmentGalleryBinding.detailButton.setOnClickListener {

            mediaList.getOrNull(fragmentGalleryBinding.photoViewPager.currentItem)
                ?.let { mediaStoreFile ->
                    val exif = ExifInterface(mediaStoreFile.file)
                    val fileName = mediaStoreFile.file.name
                    val filePath = mediaStoreFile.file.path
                    val fileSize = Formatter.formatShortFileSize(
                        requireContext(), mediaStoreFile.file.length()
                    )
                    val latlong = exif.latLong

                    //to pass the data to PhotoDetailFragment
                    val latitude: Double? = latlong?.get(0)
                    val longitude: Double? = latlong?.get(1)

                    Navigation.findNavController(requireActivity(), R.id.fragment_container)
                        .navigate(
                            GalleryFragmentDirections.actionGalleryFragmentToPhotoDetailFragment(
                                Exifdata(
                                    fileName,
                                    filePath,
                                    fileSize,
                                    latitude.toString(),
                                    longitude.toString(),
                                )
                            )
                        )
                }
        }

    }

    override fun onDestroyView() {
        _fragmentGalleryBinding = null
        super.onDestroyView()
    }
}