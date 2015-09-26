package com.ricardocasarez.topgamedeals;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.ricardocasarez.topgamedeals.model.GameDeal;
import com.ricardocasarez.topgamedeals.view.DealDetailFragment;
import com.ricardocasarez.topgamedeals.view.FragmentInteractionListener;
import com.ricardocasarez.topgamedeals.view.SearchableFragment;

/**
 * Created by ricardo.casarez on 8/26/2015.
 */
public class SearchableActivity extends AppCompatActivity implements FragmentInteractionListener {
    private static final String LOG_TAG = SearchableActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_searchable);

        // set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.activity_searchable_title);
        getSupportActionBar().setElevation(20f);

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }


    /**
     * Handles an Intent that can perform ACTION_VIEW or ACTION_SEARCH.
     * If action is ACTION_VIEW, it will finalize the activity and start DealDetailActivity to show
     * deal details.
     * If action is ACTION_SEARCH, it will search for game deals based on the intent extra
     * SearchManager.QUERY passed by SearchView.
     *
     * @param intent intent to get the action
     */
    public void handleIntent(Intent intent) {
        if (intent != null && intent.getAction() == Intent.ACTION_SEARCH) {
            // add fragment to display search results
            String query = intent.getStringExtra(SearchManager.QUERY);

            SearchableFragment fg = (SearchableFragment) getSupportFragmentManager().findFragmentByTag(SearchableFragment.TAG);

            // add fragment if is not added
            if (fg == null) {
                fg = SearchableFragment.newInstance(query);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fragment_container, fg, SearchableFragment.TAG)
                        .commit();
            }
        } else if (intent != null && intent.getAction() == Intent.ACTION_VIEW) {
            // start DealDetailActivity activity
            Uri data = intent.getData();
            Intent i = new Intent(this, DealDetailActivity.class);
            i.putExtra(DealDetailFragment.EXTRA_GAME_DEAL_URI, data.toString());
            startActivity(i);
            finish();
        } else {
            Log.e(LOG_TAG, "cannot handle the intent action");
        }
    }

    @Override
    public void onGameDealClick(GameDeal deal) {
        MainActivity.openDealDetail(this, deal);
    }
}
