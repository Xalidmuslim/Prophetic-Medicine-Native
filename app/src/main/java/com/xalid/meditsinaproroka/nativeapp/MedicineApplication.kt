package com.xalid.meditsinaproroka.nativeapp

import android.app.Application

/**
 * Starts parsing bundled book.json before the first Activity frame.
 * The existing caches and in-memory reader data remain unchanged.
 */
class MedicineApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MedicineRuntimeWarmup.preload(this)
    }
}
