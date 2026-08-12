package com.abdulwaheed.smartelectricitypredictor.util

import com.google.firebase.firestore.FirebaseFirestoreException

object FirestoreApplianceErrorHandler {
    fun getErrorMessage(exception: Throwable): String = when {
        exception is FirebaseFirestoreException &&
            exception.code == FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "You do not have permission to access your appliances."
        exception is FirebaseFirestoreException &&
            exception.code == FirebaseFirestoreException.Code.UNAVAILABLE ->
            "Appliance service is unavailable. Check your connection and try again."
        exception is FirebaseFirestoreException &&
            exception.code == FirebaseFirestoreException.Code.UNAUTHENTICATED ->
            "Your session has expired. Please sign in again."
        else -> "Unable to manage your appliances. Please try again."
    }
}
