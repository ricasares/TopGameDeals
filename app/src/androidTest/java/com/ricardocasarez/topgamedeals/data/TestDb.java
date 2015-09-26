package com.ricardocasarez.topgamedeals.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.ricardocasarez.topgamedeals.data.DbHelper;
import com.ricardocasarez.topgamedeals.util.TestUtils;

import java.util.HashSet;

/**
 * Created by ricardo.casarez on 8/18/2015.
 */
public class TestDb extends AndroidTestCase {

    public void setUp() {
        deleteDataBase();
    }

    void deleteDataBase() {
        mContext.deleteDatabase(DbHelper.DATABASE_NAME);
    }

    public void testCreateDb() {
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(DealsContract.StoreEntry.TABLE_NAME);

        //  clena db
        mContext.deleteDatabase(DbHelper.DATABASE_NAME);

        //  open writable db
        SQLiteDatabase db = new DbHelper(mContext)
                .getWritableDatabase();
        assertEquals(true, db.isOpen());

        //  are tables created?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: database has not been created correctly", c.moveToFirst());

        do{
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());

        //  if fails database didn't create all the tables
        assertTrue(tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + DealsContract.StoreEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> storeColumnHashSet = new HashSet<String>();
        storeColumnHashSet.add(DealsContract.StoreEntry.COLUMN_STORE_ID);
        storeColumnHashSet.add(DealsContract.StoreEntry.COLUMN_STORE_NAME);

        int columnNameIndex = c.getColumnIndex("name");
        do{
            storeColumnHashSet.remove(c.getString(columnNameIndex));
        }while(c.moveToNext());

        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                storeColumnHashSet.isEmpty());
        c.close();
        db.close();
    }

    public void testStoreTable() {
        insertStore();
    }

    public long insertStore() {
        SQLiteDatabase db = new DbHelper(mContext)
                .getWritableDatabase();

        ContentValues store = TestUtils.createStoreValue();

        long id = db.insert(
                DealsContract.StoreEntry.TABLE_NAME,
                null,
                store);

        assertFalse("could not insert table store " +id, id < 0);

        Cursor c = db.rawQuery("SELECT * from " + DealsContract.StoreEntry.TABLE_NAME, null);

        assertTrue("there is no data inserted into store table", c.moveToFirst());

        TestUtils.validateCurrentRecord("data inserted don't match with original", c, store);

        c.close();
        db.close();

        return id;
    }

    public void testGameDealTable() {
        long store_id = insertStore();

        SQLiteDatabase db = new DbHelper(mContext)
                .getWritableDatabase();

        ContentValues deal = TestUtils.createDealValue();

        long id = db.insert(
                DealsContract.GameDealEntry.TABLE_NAME,
                null,
                deal);

        assertFalse("could not insert into game deal table " + id, id < 0);

        Cursor c = db.rawQuery("SELECT * from " + DealsContract.GameDealEntry.TABLE_NAME, null);

        assertTrue("no records found in table game deal", c.moveToFirst());

        TestUtils.validateCurrentRecord("data in db don't match insertion data", c, deal);

        c.close();
        db.close();
    }


}
