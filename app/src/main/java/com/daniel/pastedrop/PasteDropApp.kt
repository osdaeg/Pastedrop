package com.daniel.pastedrop

import android.app.Application
import com.daniel.pastedrop.sync.NetworkMonitor
import com.daniel.pastedrop.sync.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PasteDropApp : Application() {

    @Inject lateinit var networkMonitor: NetworkMonitor

    override fun onCreate() {
        super.onCreate()
        networkMonitor.startWatching()
        SyncWorker.enqueuePeriodic(this)
    }
}
