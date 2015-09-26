package com.ricardocasarez.topgamedeals.data;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import com.ricardocasarez.topgamedeals.data.DealsContract.GameDealEntry;
import com.ricardocasarez.topgamedeals.data.DealsContract.StoreEntry;
import com.ricardocasarez.topgamedeals.util.TestUtils;

/**
 * Created by ricardo.casarez on 8/19/2015.
 */
public class TestProvider extends AndroidTestCase {
    public static final String LOG_TAG = TestProvider.class.getSimpleName();
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                StoreEntry.CONTENT_URI,
                null,
                null
        );

        mContext.getContentResolver().delete(
                GameDealEntry.CONTENT_URI,
                null,
                null
        );

        Cursor c = mContext.getContentResolver().query(
                StoreEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertEquals("Error: Records not deleted from Store table during delete", 0, c.getCount());
        c.close();

        c = mContext.getContentResolver().query(
                GameDealEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from GameDeals table during delete", 0, c.getCount());
        c.close();
    }

    public void deleteAllRecordsFromDB() {
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(StoreEntry.TABLE_NAME, null, null);
        db.delete(GameDealEntry.TABLE_NAME, null, null);
        db.close();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        deleteAllRecordsFromProvider();
    }

    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        ComponentName component = new ComponentName(mContext.getPackageName(),
                DealsProvider.class.getName());

        try {
            ProviderInfo providerInfo = pm.getProviderInfo(component, 0);

            assertEquals("Error: DealsProvider registered with authority: " + providerInfo.authority +
                    " instead of authority: " + DealsContract.CONTENT_AUTHORITY,
                    providerInfo.authority, DealsContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e){
            assertTrue("Error: DealsProvider not registered at " + mContext.getPackageName(), false);
        }
    }

    public void testGetType() {
        String type = mContext.getContentResolver().getType(StoreEntry.CONTENT_URI);
        assertEquals(type, StoreEntry.CONTENT_TYPE);

        type = mContext.getContentResolver().getType(StoreEntry.buildStoreUri(0));
        assertEquals(type, StoreEntry.CONTENT_ITEM_TYPE);

        type = mContext.getContentResolver().getType(GameDealEntry.CONTENT_URI);
        assertEquals(type, GameDealEntry.CONTENT_TYPE);

        type = mContext.getContentResolver().getType(GameDealEntry.buildGameDealUri(0));
        assertEquals(type, GameDealEntry.CONTENT_ITEM_TYPE);

        type = mContext.getContentResolver().getType(GameDealEntry.buildGameDealWithStoreUri());
        assertEquals(type, GameDealEntry.CONTENT_TYPE);

        type = mContext.getContentResolver().getType(SearchSuggestionsProvider.buildSearchableUri());
        assertEquals(type, SearchManager.SUGGEST_MIME_TYPE);
    }

    public void testStoreQuery() {
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues store = TestUtils.createStoreValue();
        long id = db.insert(StoreEntry.TABLE_NAME, null, store);

        assertTrue("Unable to insert into Store database", id != -1);

        db.close();

        Cursor c = mContext.getContentResolver().query(StoreEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        TestUtils.validateCursor("testStoreQuery", c, store);

        if ( Build.VERSION.SDK_INT >= 19 ) {
            assertEquals("Error: Store Query did not properly set NotificationUri",
                    c.getNotificationUri(), StoreEntry.CONTENT_URI);
        }
    }

    public void testDealQuery() {
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues deal = TestUtils.createDealValue();
        long id = db.insert(GameDealEntry.TABLE_NAME,
                null,
                deal);

        assertTrue(id != -1);

        db.close();

        Cursor c = mContext.getContentResolver().query(
                GameDealEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtils.validateCursor("testDealQuery", c, deal);

        if ( Build.VERSION.SDK_INT >= 19 ) {
            assertEquals("Error: Store Query did not properly set NotificationUri",
                    c.getNotificationUri(), GameDealEntry.CONTENT_URI);
        }

    }

    public void testUpdateStore() {
        ContentValues values = TestUtils.createStoreValue();

        Uri storeUri = mContext.getContentResolver().insert(
                StoreEntry.CONTENT_URI,
                values
        );
        long store_id = ContentUris.parseId(storeUri);
        assertTrue(store_id != -1);
        Log.d(LOG_TAG, "New row id: " + store_id);

        ContentValues updatedValues = new ContentValues();
        updatedValues.put(DealsContract.StoreEntry.COLUMN_STORE_ID, store_id);
        updatedValues.put(DealsContract.StoreEntry.COLUMN_STORE_NAME, "amazon");

        // Create a cursor
        Cursor storeCursor = mContext.getContentResolver().query(StoreEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        // create observer to verify that content provider is notifying the cursor
        TestUtils.TestContentObserver tco = TestUtils.getTestContentObserver();
        storeCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                StoreEntry.CONTENT_URI,
                updatedValues,
                StoreEntry.COLUMN_STORE_ID + "= ?",
                new String[]{String.valueOf(store_id)}
        );
        assertEquals(count, 1);

        // Test to make sure our observer is called
        tco.waitForNotificationOrFail();

        storeCursor.unregisterContentObserver(tco);
        storeCursor.close();

        Cursor c = mContext.getContentResolver().query(
                StoreEntry.CONTENT_URI,
                null,
                StoreEntry.COLUMN_STORE_ID + " = " + store_id,
                null,
                null
        );

        TestUtils.validateCursor("testUpdateStore.  Error validating location entry update.",
                c, updatedValues);

        c.close();
    }



    public void testInsertReadProvider() {
        // -----------------------------------------
        // test store

        // creates default store values for insert
        ContentValues storeValues = TestUtils.createStoreValue();

        // Register content observer for insert
        TestUtils.TestContentObserver observer = TestUtils.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(StoreEntry.CONTENT_URI, true, observer);

        // insert into db
        Uri storeUri = mContext.getContentResolver().insert(
                StoreEntry.CONTENT_URI,
                storeValues
        );

        // test if observer was notified
        observer.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(observer);

        // verify id of the record inserted
        long storeId = ContentUris.parseId(storeUri);
        assertTrue(storeId != -1);

        // validate that the data exist in db
        Cursor cursorStores = mContext.getContentResolver().query(
                StoreEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtils.validateCursor("testInsertReadProvider. Error validating StoreEntry.",
                cursorStores, storeValues);

        // -----------------------------------------
        // test game deal
        // create default values for game deal insert
        ContentValues dealValues = TestUtils.createDealValue();

        // register observer
        observer = TestUtils.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(GameDealEntry.CONTENT_URI, true, observer);

        // insert game offer into db
        Uri dealInsert = mContext.getContentResolver().insert(GameDealEntry.CONTENT_URI, dealValues);

        // validate insert id
        long dealId = ContentUris.parseId(dealInsert);
        assertTrue(dealId != -1);

        // test if observer was notified and unregister observer
        observer.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(observer);

        // verify that data actually exist
        Cursor cursorDeals = mContext.getContentResolver().query(GameDealEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtils.validateCursor("testInsertReadProvider. Error validating GameDealEntry.",
                cursorDeals, dealValues);

        // -----------------------------------------
        // test inner join
        dealValues.putAll(storeValues);
        cursorDeals = mContext.getContentResolver().query(
                GameDealEntry.buildGameDealWithStoreUri(),
                null,
                null,
                null,
                null
        );

        TestUtils.validateCursor("testInsertReadProvider. Error validating GameDealEntry join.",
                cursorDeals, dealValues);

    }

    public void testDeleteRecords() {
        testInsertReadProvider();

        // register content observer for store table
        TestUtils.TestContentObserver storeObserver = TestUtils.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(StoreEntry.CONTENT_URI, true, storeObserver);

        // register content observer for deals table
        TestUtils.TestContentObserver dealsObserver = TestUtils.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(GameDealEntry.CONTENT_URI, true, dealsObserver);

        deleteAllRecordsFromProvider();

        storeObserver.waitForNotificationOrFail();
        dealsObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(storeObserver);
        mContext.getContentResolver().unregisterContentObserver(dealsObserver);
    }

    public void testBulkInsert() {
        ContentValues storeValues = TestUtils.createStoreValue();

        Uri storeUri = mContext.getContentResolver().insert(StoreEntry.CONTENT_URI, storeValues);
        long storeID = ContentUris.parseId(storeUri);

        assertTrue(storeID != -1);

        Cursor cursor = mContext.getContentResolver().query(
                StoreEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtils.validateCursor("testBulkInsert. Error validating StoreEntry.",
                cursor, storeValues);

        final ContentValues[] dealsValues = TestUtils.createDealsValues();
        final int NUM_INSERT_RECORDS = dealsValues.length;

        // Register a content observer for our bulk insert.
        TestUtils.TestContentObserver dealsObserver = TestUtils.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(GameDealEntry.CONTENT_URI, true, dealsObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(GameDealEntry.CONTENT_URI, dealsValues);

        dealsObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(dealsObserver);

        assertEquals(insertCount, NUM_INSERT_RECORDS);

        cursor = mContext.getContentResolver().query(
                GameDealEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        assertEquals(cursor.getCount(), NUM_INSERT_RECORDS);

        cursor.moveToFirst();
        for (int x = 0; x < NUM_INSERT_RECORDS; x++, cursor.moveToNext()) {
            TestUtils.validateCurrentRecord("testBulkInsert.  Error validating Game deal " + x,
                    cursor, dealsValues[x]);
        }
        cursor.close();

        // test by id
        Cursor cursorDealById = mContext.getContentResolver().query(
                GameDealEntry.buildGameDealUri(2),
                null,
                null,
                null,
                null);

        assertTrue(cursorDealById.moveToFirst());
        assertEquals(cursorDealById.getCount(), 1);

        TestUtils.validateCursor("",cursorDealById, dealsValues[1]);
    }
}
