package com.example.travelsharingapp

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.travelsharingapp.utils.ManifestUtils
import com.example.travelsharingapp.utils.TokenProvider
import com.google.android.libraries.places.api.Places
import com.google.firebase.Firebase
import com.google.firebase.initialize

class MainApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(this)
//        Firebase.appCheck.installAppCheckProviderFactory(
//            //PlayIntegrityAppCheckProviderFactory.getInstance(),   // for production env
//            DebugAppCheckProviderFactory.getInstance(),             // for debug env
//        )

        // Places API
        val apiKey = ManifestUtils.getApiKeyFromManifest(this)
        if (!Places.isInitialized() && apiKey != null) {
            Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
            Places.setPlacesAppCheckTokenProvider(TokenProvider())
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .respectCacheHeaders(false) // needed to work with firebase?
            .crossfade(true)
            .build()
    }
}