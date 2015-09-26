package com.ricardocasarez.topgamedeals.util;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.ricardocasarez.topgamedeals.data.DealsContract;
import com.ricardocasarez.topgamedeals.util.PollingCheck;

import java.util.Map;
import java.util.Set;

/**
 * Created by ricardo.casarez on 8/19/2015.
 */
public class TestUtils extends AndroidTestCase{
    public static ContentValues createStoreValue() {
        ContentValues value = new ContentValues();
        value.put(DealsContract.StoreEntry.COLUMN_STORE_ID, 1);
        value.put(DealsContract.StoreEntry.COLUMN_STORE_NAME, "steam");
        return value;
    }

    public static ContentValues[] createStoreValues() {
        ContentValues[] values = new ContentValues[2];
        values[0] = createStoreValue();
        values[1] = new ContentValues();
        values[1].put(DealsContract.StoreEntry.COLUMN_STORE_ID, 2);
        values[1].put(DealsContract.StoreEntry.COLUMN_STORE_NAME, "amazon");

        return values;
    }

    public static ContentValues createDealValue() {
        ContentValues deal = new ContentValues();
        deal.put(DealsContract.GameDealEntry.COLUMN_DEAL_ID,
                "nYVugCQlHlAW3qSQnihGJ5cka0G%2FwqSqZKpUolpvhzw%3D");
        deal.put(DealsContract.GameDealEntry.COLUMN_GAME_TITLE, "Civilization Complete Pack");
        deal.put(DealsContract.GameDealEntry.COLUMN_THUMB,
                "http://ecx.images-amazon.com/images/I/51JDgWYEtpL._SL160_.jpg");
        deal.put(DealsContract.GameDealEntry.COLUMN_PRICE, 16.99);
        deal.put(DealsContract.GameDealEntry.COLUMN_OLD_PRICE, 84.97);
        deal.put(DealsContract.GameDealEntry.COLUMN_LAST_CHANGE, 1439712448);
        deal.put(DealsContract.GameDealEntry.COLUMN_STORE_ID, 1);
        deal.put(DealsContract.GameDealEntry.COLUMN_SAVINGS, 90.09);
        deal.put(DealsContract.GameDealEntry.COLUMN_METACRITIC_SCORE, 99);
        deal.put(DealsContract.GameDealEntry.COLUMN_METACRITIC_LINK, "http://wwww.google.com");
        deal.put(DealsContract.GameDealEntry.COLUMN_RELEASE_DATE, 1439712448);

        return deal;
    }

    public static ContentValues[] createDealsValues() {
        ContentValues[] values = new ContentValues[10];
        for (int i=0; i < 10 ; i++) {
            values[i] = new ContentValues();
            values[i].put(DealsContract.GameDealEntry.COLUMN_DEAL_ID, String.valueOf(i));
            values[i].put(DealsContract.GameDealEntry.COLUMN_GAME_TITLE, "Civilization Complete Pack"
                    + String.valueOf(i));
            values[i].put(DealsContract.GameDealEntry.COLUMN_THUMB,
                    "http://ecx.images-amazon.com/images/I/51JDgWYEtpL._SL160_.jpg");
            values[i].put(DealsContract.GameDealEntry.COLUMN_PRICE, 16.99);
            values[i].put(DealsContract.GameDealEntry.COLUMN_OLD_PRICE, 84.97);
            values[i].put(DealsContract.GameDealEntry.COLUMN_LAST_CHANGE, 1439712448);
            values[i].put(DealsContract.GameDealEntry.COLUMN_STORE_ID, 1);
            values[i].put(DealsContract.GameDealEntry.COLUMN_SAVINGS, 90.09);
            values[i].put(DealsContract.GameDealEntry.COLUMN_METACRITIC_SCORE, 99);
            values[i].put(DealsContract.GameDealEntry.COLUMN_METACRITIC_LINK, "http://wwww.google.com");
            values[i].put(DealsContract.GameDealEntry.COLUMN_RELEASE_DATE, 1439712448);

        }
        return values;
    }

    public static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    public static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    public static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    public static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
