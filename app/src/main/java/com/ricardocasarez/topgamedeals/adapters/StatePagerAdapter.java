package com.ricardocasarez.topgamedeals.adapters;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.ricardocasarez.topgamedeals.data.DealsContract;
import com.ricardocasarez.topgamedeals.view.DealsFragment;

/**
 * Adapter to show the store titles in TabLayout and generates Fragments to show deals for each store.
 */
public class StatePagerAdapter extends FragmentStatePagerAdapter {

    // data source
    private Cursor mCursor;

    public StatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public StatePagerAdapter(FragmentManager fm, Cursor cursor) {
        super(fm);
        mCursor = cursor;
    }

    @Override
    public Fragment getItem(int i) {
        if (mCursor == null || !mCursor.moveToPosition(i))
            return null;

        // get store id and pass it to new fragment as argument
        int storeId = mCursor.getInt(DealsContract.StoreEntry.ALL_COL_ID);
        return DealsFragment.newInstance(storeId);
    }

    @Override
    public int getCount() {
        if (mCursor == null)
            return 0;
        else
            return mCursor.getCount();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (!mCursor.moveToPosition(position))
            return null;

        // get store name
        return mCursor.getString(DealsContract.StoreEntry.ALL_COL_STORE_NAME);
    }

    /**
     * Changes current cursor data and notifies that data has changed.
     * @param newCursor     Cursor with two columns {store_id, store_name}.
     */
    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    /**
     * Returns current data cursor
     * @return  Cursor with two columns {store_id, store_name}.
     */
    public Cursor getCursor() {
        return mCursor;
    }
}
