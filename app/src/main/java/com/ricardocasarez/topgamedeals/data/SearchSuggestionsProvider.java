package com.ricardocasarez.topgamedeals.data;

import android.app.SearchManager;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by ricardo.casarez on 8/26/2015.
 */
public class SearchSuggestionsProvider extends SearchRecentSuggestionsProvider {
    public static final String AUTHORITY = SearchSuggestionsProvider.class.getName();

    public static final int MODE = DATABASE_MODE_QUERIES;

    public static final String[] COLUMNS = {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
            SearchManager.SUGGEST_COLUMN_SHORTCUT_ID };

    public static final String[] PROJECTION = {
            DealsContract.GameDealEntry._ID,
            DealsContract.GameDealEntry.COLUMN_GAME_TITLE,
            DealsContract.StoreEntry.COLUMN_STORE_NAME
    };

    public static final int PROJECTION_COL_ID = 0;
    public static final int PROJECTION_COL_GAME_TITLE = 1;
    public static final int PROJECTION_COL_STORE_NAME = 2;

    public SearchSuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

    public static Uri buildSearchableUri() {
        /*Uri.Builder builder = new Uri.Builder()
                .appendPath(DealsContract.CONTENT_AUTHORITY)
                .appendPath(DealsContract.PATH_SUGGESTION)
                .appendPath(SearchManager.SUGGEST_URI_PATH_QUERY);*/

        Uri uri = DealsContract.BASE_CONTENT_URI.buildUpon()
                .appendPath(DealsContract.PATH_SUGGESTION)
                .appendPath(SearchManager.SUGGEST_URI_PATH_QUERY)
                .build();

        Log.d("MainActivity", uri.toString());

        return uri;
    }

}
