package com.ricardocasarez.topgamedeals.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.ricardocasarez.topgamedeals.data.DealsContract.StoreEntry;
import com.ricardocasarez.topgamedeals.data.DealsContract.GameDealEntry;
import com.ricardocasarez.topgamedeals.data.DealsContract.AlertsEntry;

/**
 * Created by ricardo.casarez on 8/18/2015.
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "gamedeals.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // stores
        final String SQL_CREATE_STORE_TABLE = "CREATE TABLE " + StoreEntry.TABLE_NAME + " ( " +
                StoreEntry.COLUMN_STORE_ID + " INTEGER PRIMARY KEY, " +
                StoreEntry.COLUMN_STORE_NAME + " TEXT NOT NULL " +
                ");";

        // deals
        final String SQL_CREATE_DEAL_TABLE = "CREATE TABLE " + GameDealEntry.TABLE_NAME + " ( " +
                GameDealEntry._ID + " INTEGER PRIMARY KEY, " +
                GameDealEntry.COLUMN_STORE_ID + " INTEGER NOT NULL," +
                GameDealEntry.COLUMN_DEAL_ID + " TEXT NOT NULL, " +
                GameDealEntry.COLUMN_GAME_TITLE + " TEXT NOT NULL, " +
                GameDealEntry.COLUMN_PRICE + " REAL NOT NULL, " +
                GameDealEntry.COLUMN_OLD_PRICE + " REAL NOT NULL, " +
                GameDealEntry.COLUMN_THUMB + " TEXT NOT NULL, " +
                GameDealEntry.COLUMN_LAST_CHANGE + " INTEGER, " +
                GameDealEntry.COLUMN_SAVINGS + " REAL NOT NULL, "+
                GameDealEntry.COLUMN_METACRITIC_SCORE + " REAL NOT NULL, " +
                GameDealEntry.COLUMN_METACRITIC_LINK + " TEXT, " +
                GameDealEntry.COLUMN_RELEASE_DATE + " INTEGER NOT NULL, " +
                GameDealEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                GameDealEntry.COLUMN_GAME_ID + " INTEGER NOT NULL, " +
                " FOREIGN KEY (" + GameDealEntry.COLUMN_STORE_ID + ") REFERENCES " +
                StoreEntry.TABLE_NAME + " (" + StoreEntry.COLUMN_STORE_ID + ")," +
                " UNIQUE (" + GameDealEntry.COLUMN_DEAL_ID + ") ON CONFLICT REPLACE " +
                ");";

        // alerts
        final String SQL_CREATE_ALERT_TABLE = "CREATE TABLE " + AlertsEntry.TABLE_NAME + " ( " +
                AlertsEntry._ID + " INTEGER PRIMARY KEY, " +
                AlertsEntry.COLUMN_GAME_ID + " INTEGER NOT NULL," +
                AlertsEntry.COLUMN_EMAIL + " TEXT NOT NULL," +
                AlertsEntry.COLUMN_PRICE + " REAL NOT NULL);";

        db.execSQL(SQL_CREATE_STORE_TABLE);
        db.execSQL(SQL_CREATE_DEAL_TABLE);
        db.execSQL(SQL_CREATE_ALERT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + StoreEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + GameDealEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AlertsEntry.TABLE_NAME);
        onCreate(db);
    }
}
