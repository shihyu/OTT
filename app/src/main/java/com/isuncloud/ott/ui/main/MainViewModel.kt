package com.isuncloud.ott.ui

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.isuncloud.ott.repository.model.app.AppItem
import com.isuncloud.ott.ui.base.BaseAndroidViewModel
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(app: Application) : BaseAndroidViewModel(app) {

    companion object {
        private const val COLLECTION_PATH_OTT = "OTT"
        private const val COLLECTION_PATH_RATINGS = "Ratings"
        private const val COLLECTION_PATH_DEVICES = "Devices"
    }

    private var sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

    var isClickApp = false

    private lateinit var startDate: Date
    private lateinit var appId: String
    private lateinit var ratingId: String

    @SuppressLint("StaticFieldLeak")
    private val applicationContext = getApplication<Application>().applicationContext

    private val compositeDisposable by lazy { CompositeDisposable() }

    lateinit var db: FirebaseFirestore

    override fun onCleared() {
        compositeDisposable.clear()
        compositeDisposable.dispose()
    }

    init {
        setupFirestore()
    }

    private fun setupFirestore() {
        db = FirebaseFirestore.getInstance()
    }

    fun enterApp(item: AppItem) {
        startDate = Date()
        var ratingsMap = hashMapOf<String, Any>()
        ratingsMap["APPSTime"] = sdf.format(startDate)

        val appItemMap = hashMapOf<String, Any>()
        appItemMap["APPName"] = item.appName

        var devicesMap = hashMapOf<String, Any>()
        devicesMap["DeviceId"] = Build.SERIAL

        db.collection(COLLECTION_PATH_OTT)
                .document(item.appId)
                .set(appItemMap)
                .addOnSuccessListener {
                    appId = item.appId
                    Timber.d("data written successfully!")
                }
                .addOnFailureListener {
                    Timber.d("data written fail!")
                }

        db.collection(COLLECTION_PATH_OTT)
                .document(item.appId)
                .collection(COLLECTION_PATH_DEVICES)
                .add(devicesMap)
                .addOnSuccessListener {
                    ratingId = it.id
                    Timber.d("data written successfully!")
                }
                .addOnFailureListener {
                    Timber.d("data written fail!")
                }

        db.collection(COLLECTION_PATH_OTT)
                .document(item.appId)
                .collection(COLLECTION_PATH_RATINGS)
                .add(ratingsMap)
                .addOnSuccessListener {
                    ratingId = it.id
                    Timber.d("data written successfully!")
                }
                .addOnFailureListener {
                    Timber.d("data written fail!")
                }
    }

    fun exitApp() {
        val endDate = Date()
        val duration = (endDate.time - startDate.time) / 1000

        val ratingsMap = hashMapOf<String, Any>()
        ratingsMap["APPETime"] = sdf.format(endDate)
        ratingsMap["APPrunduration"] = duration

        db.collection(COLLECTION_PATH_OTT)
                .document(appId)
                .collection(COLLECTION_PATH_RATINGS)
                .document(ratingId)
                .set(ratingsMap, SetOptions.merge())
                .addOnSuccessListener {
                    Timber.d("data written successfully!")
                }
                .addOnFailureListener {
                    Timber.d("data written fail!")
                }
    }

}