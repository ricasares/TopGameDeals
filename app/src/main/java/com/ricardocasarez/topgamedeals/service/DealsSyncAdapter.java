package com.ricardocasarez.topgamedeals.service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ricardocasarez.topgamedeals.DealDetailActivity;
import com.ricardocasarez.topgamedeals.R;
import com.ricardocasarez.topgamedeals.data.DealsContract;
import com.ricardocasarez.topgamedeals.model.GameDeal;
import com.ricardocasarez.topgamedeals.model.Store;
import com.ricardocasarez.topgamedeals.utils.HttpRequest;
import com.ricardocasarez.topgamedeals.view.DealDetailFragment;
import com.squareup.okhttp.Response;

import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Handle the transfer of data between server and app
 * using the Android sync adapter framework.
 */
public class DealsSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOG_TAG = DealsSyncAdapter.class.getSimpleName();

    // Interval at which to sync with the deals, in milliseconds.
    // 60 seconds (1 minute) * 360 = 6 hours
    public static final int SYNC_INTERVAL = 60 * 360;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    // notifications
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int DEALS_NOTIFICATION_ID = 3654;

    public DealsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    /**
     * Gets json data from www.cheapshark.com, concentrates all network tasks to conserve power.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync");

        final String STORES_BASE_API = "http://www.cheapshark.com/api/1.0/stores";
        final String DEALS_BASE_API = "http://www.cheapshark.com/api/1.0/deals?";
        final String DEAL_PARAMETER_AAA = "AAA";
        final String DEAL_PARAMETER_META = "metacritic";

        try{
            // get store data
            URL url = new URL(STORES_BASE_API);
            Response storesResponse = HttpRequest.doHTTPRequest(url);
            if (storesResponse != null && storesResponse.isSuccessful()) {
                getStoreDataFromJson(storesResponse.body().string());
            } else {
                return;
            }


            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            // get AAA setting
            boolean isAAA = sharedPref.getBoolean(getContext().getString(R.string.pref_aaa_key),
                    Boolean.parseBoolean(getContext().getString(R.string.pref_aaa_default)));

            boolean useMetacriticFiler = sharedPref.getBoolean(getContext().getString(R.string.pref_metacritic_key),
                    Boolean.valueOf(getContext().getString(R.string.pref_metacritic_default)));

            String metacriticScore = null;
            if (useMetacriticFiler) {
                metacriticScore = sharedPref.getString(getContext().getString(R.string.pref_metacritic_edit_key),
                        getContext().getString(R.string.pref_metacritic_edit_default));
            }

            Uri.Builder builder = Uri.parse(DEALS_BASE_API).buildUpon();
            if (isAAA)
                builder.appendQueryParameter(DEAL_PARAMETER_AAA, "1");

            if (metacriticScore != null) {
                builder.appendQueryParameter(DEAL_PARAMETER_META, metacriticScore);
            }

            // get top deals data
            url = new URL(builder.build().toString());
            Log.i(LOG_TAG, "request deals:" + url);
            Response dealsResponse = HttpRequest.doHTTPRequest(url);
            if (dealsResponse != null && dealsResponse.isSuccessful()) {
                getDealsFromJSON(dealsResponse.body().string());
            }
        } catch (IOException | JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    /**
     * Creates a new dummy account for the sync adapter.
     * @param context The application context
     * @return Account
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {
            /*
             * Add the account and account type, no password or user data
             * If successful, return the Account object, otherwise report an error.
             */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);

        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        DealsSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Parses json deals data and return in a contentValues array,
     * @param jsonData  deal json data from cheapshark api.
     * @return          A vector of ContentValues.
     */
    public static List<ContentValues> getDealsContentValuesFromJSON(String jsonData) {
        // parse and serialize into GameDeal List
        Gson gson = new Gson();
        Type dealListType = new TypeToken<List<GameDeal>>(){}.getType();
        List<GameDeal> dealsList = gson.fromJson(jsonData, dealListType);

        if (dealsList != null) {
            int size = dealsList.size();
            ArrayList<ContentValues> contentValues = new ArrayList<>();

            // get time
            Time time = new Time();
            time.setToNow();
            int julianDay = Time.getJulianDay(System.currentTimeMillis(), time.gmtoff);
            long dateTime = time.setJulianDay(julianDay);

            for (int i = 0; i < size; i++) {
                ContentValues value = new ContentValues();
                GameDeal deal = dealsList.get(i);

                value.put(DealsContract.GameDealEntry.COLUMN_GAME_TITLE, deal.getGameTitle());
                value.put(DealsContract.GameDealEntry.COLUMN_DEAL_ID, deal.getDealID());
                value.put(DealsContract.GameDealEntry.COLUMN_STORE_ID, deal.getStoreID());
                value.put(DealsContract.GameDealEntry.COLUMN_PRICE, deal.getSalePrice());
                value.put(DealsContract.GameDealEntry.COLUMN_OLD_PRICE, deal.getNormalPrice());
                value.put(DealsContract.GameDealEntry.COLUMN_LAST_CHANGE, deal.getLastChange());
                value.put(DealsContract.GameDealEntry.COLUMN_THUMB, deal.getThumbUrl());
                value.put(DealsContract.GameDealEntry.COLUMN_SAVINGS, deal.getSavings());
                value.put(DealsContract.GameDealEntry.COLUMN_METACRITIC_LINK, deal.getMetacriticLink());
                value.put(DealsContract.GameDealEntry.COLUMN_METACRITIC_SCORE, deal.getMetacriticScore());
                value.put(DealsContract.GameDealEntry.COLUMN_RELEASE_DATE, deal.getReleaseDate());
                value.put(DealsContract.GameDealEntry.COLUMN_DATE, dateTime);
                value.put(DealsContract.GameDealEntry.COLUMN_GAME_ID, deal.getGameID());

                contentValues.add(value);
            }
            return contentValues;
        }
        return null;
    }

    public void getDealsFromJSON(String jsonData) {
        List<ContentValues> contentValues = getDealsContentValuesFromJSON(jsonData);

        if (contentValues != null) {
            // insert new deals
            int total = getContext().getContentResolver().bulkInsert(
                    DealsContract.GameDealEntry.CONTENT_URI,
                    contentValues.toArray(new ContentValues[contentValues.size()]));

            Log.i(LOG_TAG, "bulkInsert " + total + " of " + contentValues.size() + " into table "
                    + DealsContract.GameDealEntry.TABLE_NAME);

            if (total > 0) {
                notifyDeals();
            }

            // delete 1 day old deals
            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            total = getContext().getContentResolver().delete(
                    DealsContract.GameDealEntry.CONTENT_URI,
                    DealsContract.GameDealEntry.COLUMN_DATE + " <= ?",
                    new String[]{Long.toString(dayTime.setJulianDay(julianStartDay-1))}
            );
            Log.i(LOG_TAG, "deleted " + total + " from table " + DealsContract.GameDealEntry.TABLE_NAME);
        }
    }

    /**
     * Parse json data and insert into store table
     * @param jsonData          store json data from cheapshark api.
     * @throws JSONException
     */
    public void getStoreDataFromJson(String jsonData) throws JSONException {
        // parse and serialize json into Store list
        Gson gson = new Gson();
        Type storeListType = new TypeToken<List<Store>>(){}.getType();
        List<Store> storesList = gson.fromJson(jsonData, storeListType);

        if (storesList != null) {
            int size = storesList.size();
            ArrayList<ContentValues> contentValues = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                Store store = storesList.get(i);
                int storeID = store.getId();
                String storeName = store.getName();

                ContentValues value = new ContentValues();
                value.put(DealsContract.StoreEntry.COLUMN_STORE_ID, storeID);
                value.put(DealsContract.StoreEntry.COLUMN_STORE_NAME, storeName);
                contentValues.add(value);
            }

            //bulk insert into db
            int total = getContext().getContentResolver().bulkInsert(
                    DealsContract.StoreEntry.CONTENT_URI,
                    contentValues.toArray(new ContentValues[contentValues.size()]));

            Log.i(LOG_TAG, "bulkInsert " + total + " of " + contentValues.size() + " into table "
                    + DealsContract.StoreEntry.TABLE_NAME);
        }
    }

    /**
     * Notify when a lower price has found for a proce alert.
     * For now lower prices are only searched in top deals in db.     *
     */
    public void notifyDeals() {
        Context context = getContext();

        //checking the last update and notify
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String lastNotificationKey = context.getString(R.string.pref_last_notification);
        long lastSync = prefs.getLong(lastNotificationKey, 0);

        boolean displayNotification = prefs.getBoolean(context.getString(R.string.pref_notifications_key),
                Boolean.parseBoolean(context.getString(R.string.pref_notifications_default)));

        if (displayNotification) {
            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
                Log.i(LOG_TAG, "searching for price alerts to notify");

                // query for price alerts, get only column id and price
                Cursor alertsCursor = context.getContentResolver().query(
                        DealsContract.AlertsEntry.CONTENT_URI,
                        new String[]{
                                DealsContract.AlertsEntry.COLUMN_GAME_ID,
                                DealsContract.AlertsEntry.COLUMN_PRICE},
                        null, //selection
                        null, //selection args
                        null  //sort by
                );

                if (alertsCursor != null && alertsCursor.moveToFirst()) {
                    do {
                        String gameId = alertsCursor.getString(0);    //COLUMN_GAME_ID
                        float price = alertsCursor.getFloat(1);   //COLUMN_PRICE

                        // get the matching deals with price alert and lower price
                        Cursor dealsCursor = context.getContentResolver().query(
                                DealsContract.GameDealEntry.CONTENT_URI,
                                new String[]{
                                        DealsContract.GameDealEntry.COLUMN_DEAL_ID,
                                        DealsContract.GameDealEntry.COLUMN_GAME_TITLE,
                                        DealsContract.GameDealEntry.COLUMN_PRICE,
                                        DealsContract.GameDealEntry.COLUMN_THUMB,
                                        DealsContract.GameDealEntry._ID,

                                },
                                DealsContract.GameDealEntry.COLUMN_GAME_ID + " = ? AND " +
                                        DealsContract.GameDealEntry.COLUMN_PRICE + " < ?",
                                new String[]{gameId, String.valueOf(price)},
                                null // sort by
                        );
                        if (dealsCursor != null && dealsCursor.moveToFirst()) {
                            // notify deal
                            GameDeal deal = new GameDeal();
                            deal.setDealID(dealsCursor.getString(0)); //COLUMN_DEAL_ID
                            deal.setGameTitle(dealsCursor.getString(1)); //COLUMN_GAME_TITLE
                            deal.setSalePrice(dealsCursor.getFloat(2));  //COLUMN_PRICE
                            deal.setThumbUrl(dealsCursor.getString(3)); //COLUMN_THUMB
                            deal.setId(dealsCursor.getInt(4)); //_ID
                            prepareNotification(deal);

                            dealsCursor.close();
                        }
                    } while (alertsCursor.moveToNext());
                    alertsCursor.close();
                } else
                    Log.w(LOG_TAG, "cannot find any alert in db");
            }
        }
    }

    /**
     * Prepare all data needed for to build the notification.
     * Title, content and Bitmap
     * @param deal
     */
    public void prepareNotification(final GameDeal deal) {
        Log.i(LOG_TAG, "prepare notification for " + deal.getGameTitle() + " : " + deal.getSalePrice());

        final String title = getContext().getString(R.string.app_name);
        final String contentText = "Price alert for: " + deal.getGameTitle() + "\n" +
                "new price: " + deal.getSalePrice();

        // for now we build the notification without large icon
        // load bitmap TODO

        buildNotification(title, contentText, deal, null);
    }

    /**
     * Builds and send the notification with the provided data
     * @param title Title of the notification
     * @param content Body of the notification
     * @param deal  GameDeal to be passed as extra.
     * @param icon if null large icon won't be set.
     */
    public void buildNotification(String title, String content, GameDeal deal, Bitmap icon) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext())
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher);

        if (icon != null)
            builder.setLargeIcon(icon);

        Intent intent = new Intent(getContext(), DealDetailActivity.class);
        intent.putExtra(DealDetailFragment.EXTRA_PARCELABLE_GAME_DEAL_OBJECT, deal);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        builder.setContentIntent(resultPendingIntent);

        NotificationManager manager = (NotificationManager) getContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(DEALS_NOTIFICATION_ID, builder.build());

        //refreshing last sync
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        String lastNotificationKey = getContext().getString(R.string.pref_last_notification);
        editor.putLong(lastNotificationKey, System.currentTimeMillis());
        editor.commit();
    }
}
