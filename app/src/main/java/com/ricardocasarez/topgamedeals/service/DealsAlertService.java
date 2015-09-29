package com.ricardocasarez.topgamedeals.service;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.ricardocasarez.topgamedeals.R;
import com.ricardocasarez.topgamedeals.data.DealsContract;
import com.ricardocasarez.topgamedeals.utils.HttpRequest;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.URL;

/**
 * Service used to subscribe and un-subscribe to price alerts.
 */
public class DealsAlertService extends IntentService{
    private static final String LOG_TAG = DealsAlertService.class.getSimpleName();

    public static final String ACTION_SET = "set";
    public static final String ACTION_DELETE = "delete";
    public static final String EXTRA_ALERT_EMAIL = "extra_email";
    public static final String EXTRA_ALERT_ACTION = "extra_action";
    public static final String EXTRA_ALERT_PRICE = "extra_price";
    public static final String EXTRA_ALERT_GAME_ID = "extra_game_id";

    public DealsAlertService() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String BASE_ALERT_API = "http://www.cheapshark.com/api/1.0/alerts?";
        final String PARAMETER_ACTION = "action";
        final String PARAMETER_EMAIL = "email";
        final String PARAMETER_PRICE = "price";
        final String PARAMETER_GAME_ID = "game_id";

        String email = intent.getStringExtra(EXTRA_ALERT_EMAIL);
        String action = intent.getStringExtra(EXTRA_ALERT_ACTION);
        String price = intent.getStringExtra(EXTRA_ALERT_PRICE);
        String gameId = intent.getStringExtra(EXTRA_ALERT_GAME_ID);

        Uri.Builder uri = Uri.parse(BASE_ALERT_API).buildUpon();
        uri.appendQueryParameter(PARAMETER_ACTION, action);
        uri.appendQueryParameter(PARAMETER_EMAIL, email);
        uri.appendQueryParameter(PARAMETER_GAME_ID, gameId);
        if (price != null)
            uri.appendQueryParameter(PARAMETER_PRICE, price);

        try {
            URL url = new URL(uri.toString());
            Response response = HttpRequest.doHTTPRequest(url);

            // add alert to db
            if (response.isSuccessful()) {
                if (action.equals(ACTION_SET)) {
                    // add alert to db
                    ContentValues values = new ContentValues();
                    values.put(DealsContract.AlertsEntry.COLUMN_GAME_ID, gameId);
                    values.put(DealsContract.AlertsEntry.COLUMN_PRICE, price);
                    values.put(DealsContract.AlertsEntry.COLUMN_EMAIL, email);

                    Uri insertUri = this.getContentResolver().insert(DealsContract.AlertsEntry.CONTENT_URI, values);
                    long id = ContentUris.parseId(insertUri);

                    // set preference if it wasn't set
                    String registeredEmail = PreferenceManager.getDefaultSharedPreferences(this)
                            .getString(getString(R.string.pref_email_edit_key), getString(R.string.pref_email_edit_default));

                    if (TextUtils.isEmpty(registeredEmail)) {
                        PreferenceManager.getDefaultSharedPreferences(this).edit()
                                .putString(getString(R.string.pref_email_edit_key), email)
                                .commit();
                    }

                    Log.d(LOG_TAG, "Added price alert for:\n" +
                            email + "\n" +
                            gameId + "\n" +
                            price + "\n" + id);

                    showToast();
                } else {
                    // delete from db
                    this.getContentResolver().delete(
                            DealsContract.AlertsEntry.CONTENT_URI,
                            DealsContract.AlertsEntry.COLUMN_GAME_ID + " = ?",
                            new String[]{gameId});

                    Log.d(LOG_TAG, "Deleted price alert for:\n" +
                            email + "\n" +
                            gameId + "\n" +
                            price);
                }
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    public void showToast() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(DealsAlertService.this, R.string.add_alert_confirmation, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Clear all price alerts from db and un-subscribe from cheapshark.
     * Execute code in worker thread.
     *
     * @param context Context instance.
     */
    public static void clearAndUnsubscribePrceAlerts(final Context context) {

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Cursor cursor = context.getContentResolver().query(DealsContract.AlertsEntry.CONTENT_URI,
                        DealsContract.AlertsEntry.ALL_COLUMNS,
                        null,
                        null,
                        null);

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        String email = cursor.getString(DealsContract.AlertsEntry.ALL_COL_EMAIL);
                        String gameId = cursor.getString(DealsContract.AlertsEntry.ALL_COL_GAME_ID);

                        Intent intent = new Intent(context, DealsAlertService.class);
                        intent.putExtra(EXTRA_ALERT_GAME_ID, gameId);
                        intent.putExtra(EXTRA_ALERT_EMAIL, email);
                        intent.putExtra(EXTRA_ALERT_ACTION, ACTION_DELETE);
                        context.startService(intent);

                    }while(cursor.moveToNext());
                    cursor.close();
                }
            }
        };
        performOnBackgroundThread(runnable);
    }

    /**
     * Executes the network requests on a separate thread.
     *
     * @param runnable The runnable instance containing network mOperations to
     *        be executed.
     */
    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }
}
