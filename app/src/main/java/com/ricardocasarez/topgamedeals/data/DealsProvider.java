package com.ricardocasarez.topgamedeals.data;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ricardo.casarez on 8/18/2015.
 */
public class DealsProvider extends ContentProvider{

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DbHelper mOpenHelper;

    static final int STORE = 100;
    static final int STORE_ID = 101;
    static final int DEAL = 200;
    static final int DEAL_ID = 201;
    static final int DEAL_WITH_STORE = 202;
    static final int DEAL_SUGGESTION = 300;
    static final int ALERT = 400;
    static final int ALERT_GAME_ID = 401;

    private static final SQLiteQueryBuilder sDealsWithStoreQueryBuilder;

    static {
        sDealsWithStoreQueryBuilder = new SQLiteQueryBuilder();

        sDealsWithStoreQueryBuilder.setTables(
                DealsContract.GameDealEntry.TABLE_NAME + " INNER JOIN " +
                        DealsContract.StoreEntry.TABLE_NAME +
                        " ON " + DealsContract.GameDealEntry.TABLE_NAME +
                        "." + DealsContract.GameDealEntry.COLUMN_STORE_ID +
                        " = " + DealsContract.StoreEntry.TABLE_NAME +
                        "." + DealsContract.StoreEntry.COLUMN_STORE_ID
        );
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case STORE:
                cursor = mOpenHelper.getReadableDatabase().query(
                        DealsContract.StoreEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case STORE_ID: {
                long id = ContentUris.parseId(uri);
                selection = DealsContract.StoreEntry.COLUMN_STORE_ID + " = ?";
                selectionArgs = new String[]{String.valueOf(id)};
                cursor = mOpenHelper.getReadableDatabase().query(
                        DealsContract.StoreEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case DEAL:
                cursor = mOpenHelper.getReadableDatabase().query(
                        DealsContract.GameDealEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            case DEAL_ID: {
                long id = ContentUris.parseId(uri);
                selection = DealsContract.GameDealEntry._ID + " = ?";
                selectionArgs = new String[]{String.valueOf(id)};
                cursor = mOpenHelper.getReadableDatabase().query(
                        DealsContract.GameDealEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case DEAL_WITH_STORE: {
                cursor = sDealsWithStoreQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case DEAL_SUGGESTION: {

                String query = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selectionArgs[0])) {
                    return null;
                }

                String like = "%" + selectionArgs[0] + "%";
                String[] myArgs = {like};

                Cursor data = sDealsWithStoreQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        SearchSuggestionsProvider.PROJECTION,
                        DealsContract.GameDealEntry.COLUMN_GAME_TITLE + " like ?",
                        myArgs,
                        null,
                        null,
                        sortOrder
                );

                MatrixCursor suggestionCursor = new MatrixCursor(SearchSuggestionsProvider.COLUMNS);
                if (data != null && data.moveToFirst()) {
                    do {
                        int id = data.getInt(SearchSuggestionsProvider.PROJECTION_COL_ID);
                        String gameName = data.getString(SearchSuggestionsProvider.PROJECTION_COL_GAME_TITLE);
                        String storeName = data.getString(SearchSuggestionsProvider.PROJECTION_COL_STORE_NAME);
                        String dealID = String.valueOf(id);

                        Object[] tmp = {
                                id,         // _ID
                                gameName,   // SUGGEST_COLUMN_TEXT_1
                                storeName,  // SUGGEST_COLUMN_TEXT_2
                                dealID,     // SUGGEST_COLUMN_INTENT_DATA_ID
                                SearchManager.SUGGEST_NEVER_MAKE_SHORTCUT   //SUGGEST_COLUMN_SHORTCUT_ID
                        };

                        suggestionCursor.addRow(tmp);
                    }while (data.moveToNext());
                    data.close();
                }
                cursor = suggestionCursor;
                break;
            }
            case ALERT:{
                cursor = mOpenHelper.getReadableDatabase().query(
                        DealsContract.AlertsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case ALERT_GAME_ID:
            {
                long id = ContentUris.parseId(uri);
                selection = DealsContract.AlertsEntry.COLUMN_GAME_ID + " = ?";
                selectionArgs = new String[]{String.valueOf(id)};
                cursor = mOpenHelper.getReadableDatabase().query(
                        DealsContract.GameDealEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case STORE:
                return DealsContract.StoreEntry.CONTENT_TYPE;
            case STORE_ID:
                return DealsContract.StoreEntry.CONTENT_ITEM_TYPE;
            case DEAL:
                return DealsContract.GameDealEntry.CONTENT_TYPE;
            case DEAL_ID:
                return DealsContract.GameDealEntry.CONTENT_ITEM_TYPE;
            case DEAL_WITH_STORE:
                return DealsContract.GameDealEntry.CONTENT_TYPE;
            case DEAL_SUGGESTION:
                return SearchManager.SUGGEST_MIME_TYPE;
            case ALERT:
                return DealsContract.AlertsEntry.CONTENT_TYPE;
            case ALERT_GAME_ID:
                return DealsContract.AlertsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case STORE: {
                long id = db.insert(DealsContract.StoreEntry.TABLE_NAME, null, values);
                if (id > 0)
                    returnUri = DealsContract.StoreEntry.buildStoreUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case DEAL: {
                long id = db.insert(DealsContract.GameDealEntry.TABLE_NAME, null, values);
                if (id > 0)
                    returnUri = DealsContract.GameDealEntry.buildGameDealUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);

                break;
            }
            case ALERT: {
                long id = db.insert(DealsContract.AlertsEntry.TABLE_NAME, null, values);
                if (id > 0)
                    returnUri = DealsContract.AlertsEntry.buildAlertUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int rowsDeleted = 0;
        if (selection == null) selection="1";
        switch (match) {
            case STORE: {
                rowsDeleted = db.delete(DealsContract.StoreEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            }
            case DEAL: {
                rowsDeleted = db.delete(DealsContract.GameDealEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            }
            case ALERT: {
                rowsDeleted = db.delete(DealsContract.AlertsEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted !=0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int rowsUpdated = 0;
        if (selection == null) selection = "1";
        switch (match) {
            case STORE:
                rowsUpdated = db.update(DealsContract.StoreEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case DEAL:
                rowsUpdated = db.update(DealsContract.GameDealEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case ALERT:
                rowsUpdated = db.update(DealsContract.AlertsEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int returnCount = 0;
        switch (match) {
            case STORE: {
                db.beginTransaction();
                try{
                    for (ContentValues value : values) {
                        long id = db.insert(DealsContract.StoreEntry.TABLE_NAME, null, value);
                        if (id >= 0)
                            returnCount++;
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;

            }
            case DEAL: {
                db.beginTransaction();
                try{
                    for (ContentValues value : values) {
                        long id = db.insert(DealsContract.GameDealEntry.TABLE_NAME, null, value);
                        if (id >= 0)
                            returnCount++;
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            }
            default:
                return super.bulkInsert(uri, values);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
    }

    static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        String authority = DealsContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, DealsContract.PATH_STORE, STORE);
        uriMatcher.addURI(authority, DealsContract.PATH_STORE + "/#", STORE_ID);
        uriMatcher.addURI(authority, DealsContract.PATH_GAME_DEAL, DEAL);
        uriMatcher.addURI(authority, DealsContract.PATH_GAME_DEAL + "/#" , DEAL_ID);

        uriMatcher.addURI(authority, DealsContract.PATH_GAME_DEAL  + "/" +
                DealsContract.PATH_STORE , DEAL_WITH_STORE);

        uriMatcher.addURI(authority, DealsContract.PATH_SUGGESTION + "/" +
                SearchManager.SUGGEST_URI_PATH_QUERY, DEAL_SUGGESTION);

        uriMatcher.addURI(authority, DealsContract.PATH_ALERTS, ALERT);
        uriMatcher.addURI(authority, DealsContract.PATH_ALERTS + "/#", ALERT_GAME_ID);

        return uriMatcher;
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
