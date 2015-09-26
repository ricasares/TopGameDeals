package com.ricardocasarez.topgamedeals.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Bound service for DealsSyncAdapter.
 * This will allow the sync adapter framework to call onPerformSync().
 */
public class DealsSyncService extends Service {

    // object to use as a thread-safe lock
    private static final Object sSyncAdapterLock = new Object();
    // instance of DealsSyncAdapter as a singleton
    private static DealsSyncAdapter sDealsSyncAdapter = null;

    @Override
    public void onCreate() {
        // Set the sync adapter as syncable
        // Disallow parallel syncs
        synchronized (sSyncAdapterLock) {
            if (sDealsSyncAdapter == null) {
                sDealsSyncAdapter = new DealsSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    /**
     * Returns an object that allows the system to invoke
     * the sync adapter.     *
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        /*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
        return sDealsSyncAdapter.getSyncAdapterBinder();
    }
}
