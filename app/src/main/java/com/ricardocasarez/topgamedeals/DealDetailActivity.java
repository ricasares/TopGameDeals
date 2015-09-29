package com.ricardocasarez.topgamedeals;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.ricardocasarez.topgamedeals.model.GameDeal;
import com.ricardocasarez.topgamedeals.view.DealDetailFragment;

/**
 * Activity that hosts DealDetailFragment to show deal details.
 */
public class DealDetailActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal_detail);

        // set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.activity_deal_detail_title);

        // find fragment
        DealDetailFragment fragment = (DealDetailFragment) getSupportFragmentManager()
                .findFragmentByTag(DealDetailFragment.TAG);

        // add it if dont exist
        if (fragment == null) {
            // create fragment
            fragment = new DealDetailFragment();

            // get extras from intent and forward as fragment arguments
            GameDeal deal = getIntent().getParcelableExtra(DealDetailFragment.EXTRA_PARCELABLE_GAME_DEAL_OBJECT);
            String dealURI = getIntent().getStringExtra(DealDetailFragment.EXTRA_GAME_DEAL_URI);

            Bundle args = new Bundle();
            if (deal != null) {
                args.putParcelable(DealDetailFragment.EXTRA_PARCELABLE_GAME_DEAL_OBJECT, deal);

            } else if (dealURI != null) {
                args.putString(DealDetailFragment.EXTRA_GAME_DEAL_URI, dealURI);
            }
            fragment.setArguments(args);

            // add fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.deal_detail_fragment_container, fragment, DealDetailFragment.TAG)
                    .commit();
        }
    }
}
