package com.example.data

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseService {
    private const val TAG = "FirebaseService"

    val isFirebaseAvailable: Boolean by lazy {
        try {
            val app = FirebaseApp.getInstance()
            app != null
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Firebase is not initialized (likely missing google-services.json): ${e.message}")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check Firebase status: ${e.message}")
            false
        }
    }

    val auth: FirebaseAuth?
        get() = if (isFirebaseAvailable) FirebaseAuth.getInstance() else null

    val firestore: FirebaseFirestore?
        get() = if (isFirebaseAvailable) FirebaseFirestore.getInstance() else null
}
