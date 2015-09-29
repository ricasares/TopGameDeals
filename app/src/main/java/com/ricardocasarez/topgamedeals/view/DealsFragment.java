package com.ricardocasarez.topgamedeals.view;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.ricardocasarez.topgamedeals.MainActivity;
import com.ricardocasarez.topgamedeals.R;
import com.ricardocasarez.topgamedeals.adapters.GameDealRecyclerViewAdapter;
import com.ricardocasarez.topgamedeals.data.DealsContract;
import com.ricardocasarez.topgamedeals.model.GameDeal;

/**
 * Fragment used to display deals as a list or grid.
 */
public class DealsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        GameDealRecyclerViewAdapter.CustomItemClickListener {

    private static final String LOG_TAG = DealsFragment.class.getSimpleName();
    public static final String FRAGMENT_DEALS_ARGUMENT_STORE_ID = "args_top_frag_position";
    private static final int LOADER_ID = 200;

    // Projection of information retrieved from db
    static final String[] GAME_DEAL_COLUMNS = {
            DealsContract.GameDealEntry.TABLE_NAME + "." + DealsContract.GameDealEntry._ID,
            DealsContract.GameDealEntry.COLUMN_DEAL_ID,
            DealsContract.GameDealEntry.COLUMN_GAME_TITLE,
            DealsContract.GameDealEntry.COLUMN_THUMB,
            DealsContract.GameDealEntry.COLUMN_PRICE,
            DealsContract.GameDealEntry.COLUMN_OLD_PRICE,
            DealsContract.GameDealEntry.COLUMN_METACRITIC_SCORE
    };

    // Projection index
    public static final int COL_ID = 0;
    public static final int COL_DEAL_ID = 1;
    public static final int COL_GAME_TITLE = 2;
    public static final int COL_THUMB_URL = 3;
    public static final int COL_PRICE = 4;
    public static final int COL_OLD_PRICE = 5;
    public static final int COL_METACRITIC_SCORE = 6;

    // Store ID used to filter deals by store
    private int mStoreID = 0;

    // recycler view adapter
    GameDealRecyclerViewAdapter mGameAdapter;

    // ui
    AutoFitRecyclerView mRecyclerView;
    ProgressBar mProgressBar;

    // interface used to dispatch events into parent activity.
    FragmentInteractionListener mFragmentInteractionListener;

    /**
     * Creates a new instance of DealsFragment with arguments
     * @param storeID id of the store
     * @return instance of fragment with store id as argument
     */
    public static DealsFragment newInstance(int storeID) {
        Bundle arguments = new Bundle();
        arguments.putInt(FRAGMENT_DEALS_ARGUMENT_STORE_ID, storeID);

        DealsFragment f = new DealsFragment();
        f.setArguments(arguments);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mFragmentInteractionListener = (FragmentInteractionListener) context;
        }catch(ClassCastException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get store id
        mStoreID = getArguments() != null ?
                getArguments().getInt(FRAGMENT_DEALS_ARGUMENT_STORE_ID) : 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deals, container, false);

        // RecyclerView setup
        mRecyclerView = (AutoFitRecyclerView) view.findViewById(R.id.recycler_view);

        if (MainActivity.sIsLargeScreen || getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity().getBaseContext(), 1);
            mRecyclerView.setLayoutManager(gridLayoutManager);
        } else {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getBaseContext());
            mRecyclerView.setLayoutManager(linearLayoutManager);
        }

        // RecyclerView adapter setup, data is initialized as null
        mGameAdapter = new GameDealRecyclerViewAdapter(getActivity(), null, this);
        mRecyclerView.setAdapter(mGameAdapter);

        mProgressBar = (ProgressBar) view.findViewById(R.id.fragment_deals_progress_bar);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    /*
    ----------- GameDealAdapter.CustomItemClickListener
     */
    @Override
    public void onItemClick(View v, int position) {
        Cursor cursor = mGameAdapter.getCursor();

        if (cursor.moveToPosition(position)) {
            GameDeal deal = new GameDeal();
            deal.setId(cursor.getInt(COL_ID));
            deal.setDealID(cursor.getString(COL_DEAL_ID));
            deal.setGameTitle(cursor.getString(COL_GAME_TITLE));
            deal.setThumbUrl(cursor.getString(COL_THUMB_URL));
            deal.setSalePrice(cursor.getFloat(COL_PRICE));
            deal.setNormalPrice(cursor.getFloat(COL_OLD_PRICE));
            deal.setMetacriticScore(cursor.getInt(COL_METACRITIC_SCORE));
            deal.setStoreID(mStoreID);

            if (mFragmentInteractionListener != null)
                mFragmentInteractionListener.onGameDealClick(deal);
        } else
            Log.w(LOG_TAG, "Cursor could not move to position:"+position);
    }


    /*
    ----------- LoaderManager.LoaderCallbacks
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID) {
            return new CursorLoader(getActivity(),
                    DealsContract.GameDealEntry.CONTENT_URI,
                    GAME_DEAL_COLUMNS,
                    DealsContract.GameDealEntry.COLUMN_STORE_ID + " = ?",
                    new String[]{String.valueOf(mStoreID)},
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "onLoadFinished "+loader.getId());

        mProgressBar.setVisibility(View.GONE);
        mGameAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.v(LOG_TAG, "onLoaderReset "+loader.getId());
        mGameAdapter.changeCursor(null);
    }

}
