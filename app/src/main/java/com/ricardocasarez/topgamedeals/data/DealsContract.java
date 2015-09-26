package com.ricardocasarez.topgamedeals.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ricardo.casarez on 8/18/2015.
 */
public class DealsContract {

    public static final String CONTENT_AUTHORITY = "com.ricardocasarez.topgamedeals";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // provider supported paths
    public static final String PATH_STORE = "store";
    public static final String PATH_GAME_DEAL = "deal";
    public static final String PATH_SUGGESTION = "suggestion";
    public static final String PATH_ALERTS = "alerts";

    public static final class StoreEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_STORE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STORE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STORE;

        public static final String TABLE_NAME = "store";

        public static final String COLUMN_STORE_ID = "store_id";
        public static final String COLUMN_STORE_NAME = "store_name";

        /**
         * Builds a store URI with the specified id appended.
         * @param id    a store id to search in deals table.
         * @return      The store URI with the id appended.
         */
        public static Uri buildStoreUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final String[] ALL_COLUMNS = {
                COLUMN_STORE_ID,
                COLUMN_STORE_NAME
        };

        public static final int ALL_COL_ID = 0;
        public static final int ALL_COL_STORE_NAME = 1;

    }

    public static final class GameDealEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_GAME_DEAL).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GAME_DEAL;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GAME_DEAL;

        public static final String TABLE_NAME = "game_deal";

        public static final String COLUMN_GAME_TITLE = "game_title";
        public static final String COLUMN_DEAL_ID = "deal_id";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_OLD_PRICE = "old_price";
        public static final String COLUMN_STORE_ID = "deal_store_id";
        public static final String COLUMN_LAST_CHANGE = "lastChange";
        public static final String COLUMN_THUMB = "thumb";
        public static final String COLUMN_SAVINGS = "savings";
        public static final String COLUMN_METACRITIC_SCORE = "metacriticScore";
        public static final String COLUMN_METACRITIC_LINK = "metacriticLink";
        public static final String COLUMN_RELEASE_DATE = "releaseDate";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_GAME_ID = "game_id";


        /**
         * Builds a deal URI with the specified id appended.
         * @param id    a deal id to search in deals table.
         * @return      The deal URI with the id appended.
         */
        public static Uri buildGameDealUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        /**
         * Builds a deal URI with store path appended.
         * @return      An URI to query an inner join of deals and store table.
         */
        public static Uri buildGameDealWithStoreUri() {

            //Uri uri = CONTENT_URI.buildUpon().appendPath(StoreEntry.COLUMN_STORE_ID).build();
            Uri uri = CONTENT_URI.buildUpon().appendPath(PATH_STORE).build();
            return uri;
        }

        public static final String[] ALL_COLUMNS = {
                _ID,
                COLUMN_DEAL_ID,
                COLUMN_GAME_TITLE,
                COLUMN_THUMB,
                COLUMN_PRICE,
                COLUMN_OLD_PRICE,
                COLUMN_METACRITIC_SCORE,
                COLUMN_STORE_ID,
                COLUMN_LAST_CHANGE,
                COLUMN_SAVINGS,
                COLUMN_METACRITIC_LINK,
                COLUMN_RELEASE_DATE,
                COLUMN_GAME_ID
        };

        // keep column index to faster access to data
        public static final int ALL_COL_ID = 0;
        public static final int ALL_COL_DEAL_ID = 1;
        public static final int ALL_COL_GAME_TITLE = 2;
        public static final int ALL_COL_THUMB = 3;
        public static final int ALL_COL_PRICE = 4;
        public static final int ALL_COL_OLD_PRICE = 5;
        public static final int ALL_COL_METACRITIC_SCORE = 6;
        public static final int ALL_COL_STORE_ID = 7;
        public static final int ALL_COL_LAST_CHANGE = 8;
        public static final int ALL_COL_SAVINGS = 9;
        public static final int ALL_COL_METACRITIC_LINK = 10;
        public static final int ALL_COL_RELEASE_DATE = 11;
        public static final int ALL_COL_GAME_ID = 12;
    }

    public static final class AlertsEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ALERTS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ALERTS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ALERTS;

        public static final String TABLE_NAME = "alerts";

        public static final String COLUMN_GAME_ID = "game_id";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_EMAIL = "email";

        /**
         * Builds a store URI with the specified id appended.
         * @param id    a store id to search in deals table.
         * @return      The store URI with the id appended.
         */
        public static Uri buildAlertUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
