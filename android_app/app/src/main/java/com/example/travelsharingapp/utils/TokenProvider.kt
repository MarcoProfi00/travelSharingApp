package com.example.travelsharingapp.utils

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.libraries.places.api.auth.PlacesAppCheckTokenProvider
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.google.firebase.appcheck.AppCheckToken
import com.google.firebase.appcheck.FirebaseAppCheck

internal class TokenProvider : PlacesAppCheckTokenProvider {
    override fun fetchAppCheckToken(): ListenableFuture<String?> {
        val future: SettableFuture<String?> = SettableFuture.create()
        FirebaseAppCheck.getInstance()
            .getAppCheckToken(false)
            .addOnSuccessListener(
                OnSuccessListener { appCheckToken: AppCheckToken? ->
                    future.set(appCheckToken!!.token)
                })
            .addOnFailureListener(
                OnFailureListener { ex: Exception? ->
                    if (ex != null) {
                        future.setException(ex)
                    }
                })

        return future
    }
}