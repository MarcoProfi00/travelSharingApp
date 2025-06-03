package com.example.travelsharingapp.utils

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.travelsharingapp.CameraActivity

data class ImagePickerActions(
    val launchCamera: () -> Unit,
    val launchGallerySingle: () -> Unit,
    val launchGalleryMultiple: () -> Unit
)

@Composable
fun rememberImagePickerActions(
    onImageSelected: (Uri?) -> Unit,
    onImagesSelected: (List<Uri>) -> Unit = {}
): ImagePickerActions {

    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                onImageSelected(result.data?.data)
            } else {
                onImageSelected(null)
            }
        }
    )

    val gallerySingleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(it, flags)
                } catch (_: SecurityException) {
                    Log.e("ImagePicker", "Could not get persistable permission for single image URI: $it")
                }
            }
            onImageSelected(uri)
        }
    )

    val galleryMultipleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris: List<Uri> ->
            val grantedUris = uris.mapNotNull { uri ->
                try {
                    val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, flags)
                    uri
                } catch (_: SecurityException) {
                    Log.e("ImagePicker", "Could not get persistable permission for multiple image URI: $uri")
                    null
                }
            }

            onImagesSelected(grantedUris)
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val intent = Intent(context, CameraActivity::class.java)
                cameraLauncher.launch(intent)
            } else {
                Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val actions = remember {
        ImagePickerActions(
            launchCamera = {
                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                        val intent = Intent(context, CameraActivity::class.java)
                        cameraLauncher.launch(intent)
                    }
                    else -> {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            },
            launchGallerySingle = {
                gallerySingleLauncher.launch("image/*")
            },
            launchGalleryMultiple = {
                galleryMultipleLauncher.launch("image/*")
            }
        )
    }

    return actions
}