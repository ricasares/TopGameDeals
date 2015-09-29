package com.ricardocasarez.topgamedeals.view;

import com.ricardocasarez.topgamedeals.model.GameDeal;

/**
 * Interface to send events from Fragment to parent Activity.
 */
public interface FragmentInteractionListener {
    void onGameDealClick(GameDeal deal);
}
