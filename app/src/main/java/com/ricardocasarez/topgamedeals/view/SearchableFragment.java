package com.ricardocasarez.topgamedeals.view;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ricardocasarez.topgamedeals.MainActivity;
import com.ricardocasarez.topgamedeals.R;
import com.ricardocasarez.topgamedeals.adapters.GameDealRecyclerViewAdapter;
import com.ricardocasarez.topgamedeals.data.DealsContract;
import com.ricardocasarez.topgamedeals.model.GameDeal;
import com.ricardocasarez.topgamedeals.service.DealsSyncAdapter;
import com.ricardocasarez.topgamedeals.utils.HttpRequest;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ricardo.casarez on 9/22/2015.
 */
public class SearchableFragment extends Fragment implements
        GameDealRecyclerViewAdapter.CustomItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = SearchableFragment.class.getSimpleName();
    public static final String TAG = SearchableFragment.class.getSimpleName();

    // Loader used to get store names
    private static final int LOADER_ID = 500;

    public static final String[] COLUMN_PROJECTION = {
            DealsContract.GameDealEntry._ID,
            DealsContract.GameDealEntry.COLUMN_DEAL_ID,
            DealsContract.GameDealEntry.COLUMN_GAME_TITLE,
            DealsContract.GameDealEntry.COLUMN_THUMB,
            DealsContract.GameDealEntry.COLUMN_PRICE,
            DealsContract.GameDealEntry.COLUMN_OLD_PRICE,
            DealsContract.GameDealEntry.COLUMN_METACRITIC_SCORE,
            DealsContract.GameDealEntry.COLUMN_STORE_ID,
            DealsContract.GameDealEntry.COLUMN_LAST_CHANGE,
            DealsContract.GameDealEntry.COLUMN_SAVINGS,
            DealsContract.GameDealEntry.COLUMN_METACRITIC_LINK,
            DealsContract.GameDealEntry.COLUMN_RELEASE_DATE,
            DealsContract.GameDealEntry.COLUMN_GAME_ID,
            DealsContract.StoreEntry.COLUMN_STORE_NAME
    };

    public static final int COL_STORE_NAME = 13;

    // query to search
    private String mSearchQuery;

    // ui
    private AutoFitRecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private TextView mErrorTextView;

    // RecyclerView adapter
    private GameDealRecyclerViewAdapter mGameDealRecyclerViewAdapter;

    // map used to cache store names, <ID, NAME>
    Map<String, String> mStoreNamesMap;

    // interface used to dispatch events into parent activity.
    FragmentInteractionListener mFragmentInteractionListener;

    /**
     * Creates a new instance of SearchableFragment with QUERY argument.
     * @param query term used to search
     * @return instance of SearchableFragment
     */
    public static SearchableFragment newInstance(String query) {
        Bundle args = new Bundle();
        args.putString(SearchManager.QUERY, query);

        SearchableFragment fragment = new SearchableFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            mFragmentInteractionListener = (FragmentInteractionListener) context;
        }catch(ClassCastException e) {
            Log.e(LOG_TAG, "Parent activity must implement FragmentInteractionListener", e);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mSearchQuery = arguments.getString(SearchManager.QUERY);
            getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                        Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_searchable, container, false);

        // set up recycler view
        mRecyclerView = (AutoFitRecyclerView) v.findViewById(R.id.recycler_view_search);
        if (MainActivity.sIsLargeScreen || getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity().getBaseContext(), 1);
            mRecyclerView.setLayoutManager(gridLayoutManager);
        } else {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getBaseContext());
            mRecyclerView.setLayoutManager(linearLayoutManager);
        }

        // set up recycler view adapter
        mGameDealRecyclerViewAdapter = new GameDealRecyclerViewAdapter(getActivity(), null, this);
        mGameDealRecyclerViewAdapter.setItemType(GameDealRecyclerViewAdapter.ITEM_TYPE_DEAL_WITH_STORE_NAME);
        mRecyclerView.setAdapter(mGameDealRecyclerViewAdapter);

        // set up progress bar to show when loading
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);

        mErrorTextView = (TextView) v.findViewById(R.id.textview_search_error);

        return v;
    }

    /**
     * Performs an async HTTP request to search for game deals based on the specified query.
     * @param query game name used to search for deals
     */
    public void getSearchResults(String query) {
        final String DEALS_BASE_API = "http://www.cheapshark.com/api/1.0/deals?";
        final String TITLE_PARAMETER = "title";
        final String SORT_PARAMETER = "sortBy";

        Uri uri = Uri.parse(DEALS_BASE_API).buildUpon()
                .appendQueryParameter(TITLE_PARAMETER, query)
                .appendQueryParameter(SORT_PARAMETER, "Store")
                .build();
        try{
            URL url = new URL(uri.toString());

            HttpRequest.doAsyncHTTPRequest(url, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mErrorTextView != null) {
                                mErrorTextView.setText(R.string.error_connection);
                                mErrorTextView.setVisibility(View.VISIBLE);
                            }
                            if (mProgressBar != null)
                                mProgressBar.setVisibility(View.GONE);
                        }
                    });
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String srtResponse = response.body().string();
                    new GetDataFromJSONTask().execute(srtResponse);
                }
            });
        }catch(Exception e){}
    }

    @Override
    public void onItemClick(View v, int position) {
        Cursor cursor = mGameDealRecyclerViewAdapter.getCursor();

        if (cursor != null && cursor.moveToPosition(position)) {
            GameDeal deal = GameDeal.populateGameDeal(cursor);

            if (mFragmentInteractionListener!= null)
            mFragmentInteractionListener.onGameDealClick(deal);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                DealsContract.StoreEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            mStoreNamesMap = new HashMap<String,String>();
            do{
                mStoreNamesMap.put(data.getString(0), data.getString(1));
            }while(data.moveToNext());
        }
        getSearchResults(mSearchQuery);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * Creates a cursor from the List of ContentValues to populate the RecyclerView adapter.
     */
    class GetDataFromJSONTask extends AsyncTask<String, Void, Cursor> {

        @Override
        protected Cursor doInBackground(String... params) {
            String jsonData = params[0];

            // parse json data and get content values
            List<ContentValues> values = DealsSyncAdapter.getDealsContentValuesFromJSON(jsonData);

            if (values != null) {
                Log.v(LOG_TAG, "got results: " + values.size());

                // Cursor used to display search results using GameDealAdapter
                MatrixCursor cursor = new MatrixCursor(COLUMN_PROJECTION);

                // convert ContentValue to Object[] to add them to MatrixCursor
                long id = 0;
                for (ContentValues value : values) {

                    // get store name
                    String storeId = value.get(DealsContract.GameDealEntry.COLUMN_STORE_ID).toString();
                    String storeName = mStoreNamesMap.get(storeId);

                    Object[] tmp = {
                            id++,
                            value.get(DealsContract.GameDealEntry.ALL_COLUMNS[1]),
                            value.get(DealsContract.GameDealEntry.ALL_COLUMNS[2]),
                            value.get(DealsContract.GameDealEntry.ALL_COLUMNS[3]),
                            value.get(DealsContract.GameDealEntry.ALL_COLUMNS[4]),
                            value.get(DealsContract.GameDealEntry.ALL_COLUMNS[5]),
                            value.get(DealsContract.GameDealEntry.ALL_COLUMNS[6]),
                            value.get(DealsContract.GameDealEntry.ALL_COLUMNS[7]),
                            value.get(DealsContract.GameDealEntry.ALL_COLUMNS[8]),
                            value.get(DealsContract.GameDealEntry.ALL_COLUMNS[9]),
                            value.get(DealsContract.GameDealEntry.ALL_COLUMNS[10]),
                            value.get(DealsContract.GameDealEntry.ALL_COLUMNS[11]),
                            value.get(DealsContract.GameDealEntry.ALL_COLUMNS[12]),
                            storeName
                    };

                    // add row to cursor
                    cursor.addRow(tmp);
                }
                return cursor;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            if (mProgressBar != null)
                mProgressBar.setVisibility(View.GONE);

            mGameDealRecyclerViewAdapter.changeCursor(cursor);

            if (cursor == null || cursor.getCount() <= 0) {
                String error = String.format(getResources().getString(R.string.error_search), mSearchQuery);

                if (mErrorTextView != null) {
                    mErrorTextView.setText(error);
                    mErrorTextView.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
