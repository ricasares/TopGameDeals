package com.ricardocasarez.topgamedeals.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;

/**
 * Abstract class of RecyclerView.Adapter that contains it's data in a cursor.
 */
public abstract class RecyclerViewAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>{

    private Cursor mCursor;
    private Context mContext;

    private DataSetObserver mDataSetObserver;

    final static String COLUMN_ID = "_id";
    int mColumnIdIndex;

    public RecyclerViewAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;

        mDataSetObserver = new NotifyingDataSetObserver();
        if (mCursor != null) {
            mColumnIdIndex = mCursor.getColumnIndex(COLUMN_ID);
            mCursor.registerDataSetObserver(mDataSetObserver);
        }
    }

    public abstract void onBindViewHolder(VH viewHolder, Cursor cursor);

    @Override
    public void onBindViewHolder(VH holder, int position) {
        if (mCursor == null || mCursor.getColumnIndex(COLUMN_ID) < 0)
            throw new IllegalStateException("onBindViewHolder should be called when data is available");
        if (!mCursor.moveToPosition(position))
            throw new IllegalStateException("couldn't move cursor to position " + position);
        onBindViewHolder(holder, mCursor);
    }

    @Override
    public int getItemCount() {
        if (mCursor == null)    return 0;
        return mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        if (mCursor == null || !mCursor.moveToPosition(position))
            return 0;

        return mCursor.getLong(mColumnIdIndex);
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }

    public void changeCursor(Cursor newCursor) {
        Cursor old = swapCursor(newCursor);
        if (old != null)
            old.close();
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (mCursor == newCursor)
            return null;

        final Cursor oldCursor = mCursor;

        if (oldCursor != null && mDataSetObserver != null)
            oldCursor.unregisterDataSetObserver(mDataSetObserver);

        mCursor = newCursor;
        if (mCursor != null) {
            if (mDataSetObserver != null)
                mCursor.registerDataSetObserver(mDataSetObserver);
            mColumnIdIndex = mCursor.getColumnIndexOrThrow(COLUMN_ID);
            notifyDataSetChanged();
        } else {
            mColumnIdIndex = -1;
            notifyDataSetChanged();
        }
        return oldCursor;
    }

    public Context getContext() {
        return mContext;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();

            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();

            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
    }
}
