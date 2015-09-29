package com.ricardocasarez.topgamedeals.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.ricardocasarez.topgamedeals.R;
import com.ricardocasarez.topgamedeals.view.DealsFragment;
import com.ricardocasarez.topgamedeals.view.SearchableFragment;
import com.squareup.picasso.Picasso;

/**
 * RecyclerView Adapter set the data into views to show the deals.
 */
public class GameDealRecyclerViewAdapter extends RecyclerViewAdapter<GameDealRecyclerViewAdapter.ViewHolder> {
    // Adapter support two types of item view.
    public static final int ITEM_TYPE_DEAL_WITH_RATING = 0;
    public static final int ITEM_TYPE_DEAL_WITH_STORE_NAME = 1;

    // Custom click listener used to dispatch click events.
    CustomItemClickListener mItemClickListener;

    // By default item type with rating bar.
    private int mItemType = ITEM_TYPE_DEAL_WITH_RATING;

    public GameDealRecyclerViewAdapter(Context context, Cursor cursor, CustomItemClickListener listener) {
        super(context, cursor);
        mItemClickListener = listener;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        // icon
        String url = cursor.getString(DealsFragment.COL_THUMB_URL);
        Picasso.with(getContext()).load(url).into(viewHolder.iconView);

        // game name
        viewHolder.textViewName.setText(cursor.getString(DealsFragment.COL_GAME_TITLE));

        if (mItemType == ITEM_TYPE_DEAL_WITH_STORE_NAME) {
            // hide rating bar, show store name
            viewHolder.ratingBar.setVisibility(View.GONE);
            viewHolder.textViewStore.setVisibility(View.VISIBLE);

            String store = cursor.getString(SearchableFragment.COL_STORE_NAME);
            viewHolder.textViewStore.setText(store);
        } else {
            // hide store name, show rating bar
            viewHolder.ratingBar.setVisibility(View.VISIBLE);
            viewHolder.textViewStore.setVisibility(View.GONE);

            // get rating
            int score = cursor.getInt(DealsFragment.COL_METACRITIC_SCORE);
            if (score > 0)
                viewHolder.ratingBar.setRating(score / 20);
            else
                viewHolder.ratingBar.setRating(score);
        }

        // price
        float price = cursor.getFloat(DealsFragment.COL_PRICE);
        String formattedPrice = String.format(getContext().getString(R.string.format_price), price);
        viewHolder.textViewPrice.setText(formattedPrice);

        // old price
        float oldPrice = cursor.getFloat(DealsFragment.COL_OLD_PRICE);
        String formattedOldPrice = String.format(getContext().getString(R.string.format_price), oldPrice);
        viewHolder.textViewOldPrice.setText(formattedOldPrice);
        viewHolder.textViewOldPrice.setPaintFlags(
        viewHolder.textViewOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_deal_item_layout, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClickListener.onItemClick(v, viewHolder.getAdapterPosition());
            }
        });

        return viewHolder;
    }

    /**
     * Sets the current type of the view to show.
     * @param type  Type must be either ITEM_TYPE_DEAL_WITH_RATING or ITEM_TYPE_DEAL_WITH_STORE_NAME
     *              If value is any other, type ITEM_TYPE_DEAL_WITH_RATING will be used by default
     */
    public void setItemType(int type) {
        mItemType = type;
    }

    /**
     * Cache of the children views for recycler view
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView iconView;
        public final TextView textViewName;
        public final TextView textViewStore;
        public final TextView textViewPrice;
        public final TextView textViewOldPrice;
        public final RatingBar ratingBar;


        public ViewHolder(View view) {
            super(view);
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            textViewName = (TextView) view.findViewById(R.id.list_item_text);
            textViewStore = (TextView) view.findViewById(R.id.list_item_text2);
            textViewPrice = (TextView) view.findViewById(R.id.list_item_price);
            textViewOldPrice = (TextView) view.findViewById(R.id.list_item_old_price);
            ratingBar = (RatingBar) view.findViewById(R.id.rating_bar);
        }
    }

    /**
     * Public interface to callback click events.
     */
    public interface CustomItemClickListener {
        void onItemClick(View v, int position);
    }
}
