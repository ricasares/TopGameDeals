package com.ricardocasarez.topgamedeals;


import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ricardocasarez.topgamedeals.adapters.StatePagerAdapter;
import com.ricardocasarez.topgamedeals.data.DealsContract;
import com.ricardocasarez.topgamedeals.model.GameDeal;
import com.ricardocasarez.topgamedeals.service.DealsSyncAdapter;
import com.ricardocasarez.topgamedeals.view.DealDetailFragment;
import com.ricardocasarez.topgamedeals.view.FragmentInteractionListener;


/**
 * Main activity that hosts the ViewPager and TabLayout.
 */
public class MainActivity extends AppCompatActivity implements FragmentInteractionListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String SAVED_STATE_POSITION = "position";
    private static final int LOADER_ID = 100;

    // ui
    private StatePagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    // member used to store selected tab.
    private int mSavedPosition = 0;

    // true if sw600dp
    public static boolean sIsLargeScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // elevation is set to AppBarLayout instead.
        getSupportActionBar().setElevation(0);

        // initialize viewpager adapter with null data
        mSectionsPagerAdapter = new StatePagerAdapter(getSupportFragmentManager(), null);

        // set up viewpager
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // set up tablayout
        mTabLayout = (TabLayout) findViewById(R.id.tablayout);

        sIsLargeScreen = getResources().getBoolean(R.bool.is_large_screen);

        // initialize sync adapter
        DealsSyncAdapter.initializeSyncAdapter(this);

        // start loader to get data from db
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        // restore tab position if application was killed
        if (savedInstanceState != null) {
            mSavedPosition = savedInstanceState.getInt(SAVED_STATE_POSITION);
        }
    }

    @Override

    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // SearchView set up
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save current tab position
        if (mViewPager != null) {
            outState.putInt(SAVED_STATE_POSITION, mViewPager.getCurrentItem());
        }
    }

    /**
     * Initializes TabLayout, set up to use with ViewPager.
     * Restore previous selected index.
     */
    public void setUpTabLayout() {
        if (mTabLayout != null) {
            mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            mTabLayout.setupWithViewPager(mViewPager);
            mTabLayout.setVisibility(View.VISIBLE);

            // restore previous selected tab
            if (mSavedPosition != 0)
                mViewPager.setCurrentItem(mSavedPosition);
        }
    }

    /**
     * Launch DealDetailFragment as a dialog for large screens, or as DealDetailActivity an
     * Activity for normal screens
     * @param activity activity used to get SupportFragmentManager
     * @param deal Object to show in the DealDetailFragment
     */
    public static void openDealDetail(AppCompatActivity activity, GameDeal deal) {
        if (sIsLargeScreen) {
            // show DealDetailFragment as a dialog
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            Fragment prev = activity.getSupportFragmentManager().findFragmentByTag(DealDetailFragment.TAG);
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            DealDetailFragment fragment = DealDetailFragment.newInstance(deal);
            fragment.show(ft, DealDetailFragment.TAG);
        } else {
            // start activity to show DealDetailFragment
            Intent i = new Intent(activity, DealDetailActivity.class);
            i.putExtra(DealDetailFragment.EXTRA_PARCELABLE_GAME_DEAL_OBJECT, deal);
            activity.startActivity(i);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // get store id and store name of all the top deals without duplicated stores to set the tab
        // titles
        if (id == LOADER_ID) {
            return new CursorLoader(this,
                    DealsContract.GameDealEntry.buildGameDealWithStoreUri(),
                    new String[]{
                            "DISTINCT " + DealsContract.GameDealEntry.COLUMN_STORE_ID,
                            DealsContract.StoreEntry.COLUMN_STORE_NAME
                    },
                    DealsContract.GameDealEntry.COLUMN_STORE_ID + " IS NOT NULL) GROUP BY (" +
                            DealsContract.GameDealEntry.COLUMN_STORE_ID,
                    null,
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "onLoadFinished " + loader.getId() + " : " + data.getCount());
        mSectionsPagerAdapter.swapCursor(data);

        // if data is available, set up tab layout
        if (data.getCount() > 0) {
            ProgressBar progress = (ProgressBar) findViewById(R.id.progress_bar);
            progress.setVisibility(View.GONE);

            setUpTabLayout();

            // make error gone
            TextView textViewError = ((TextView) findViewById(R.id.textview_error));
            textViewError.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.v(LOG_TAG, "onLoaderReset " + loader.getId());
        mSectionsPagerAdapter.swapCursor(null);
    }

    @Override
    public void onGameDealClick(GameDeal deal) {
        // handle deal selection
        openDealDetail(this, deal);
    }
}
