package com.ricardocasarez.topgamedeals.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ricardocasarez.topgamedeals.MainActivity;
import com.ricardocasarez.topgamedeals.R;
import com.ricardocasarez.topgamedeals.data.DealsContract;
import com.ricardocasarez.topgamedeals.model.GameDeal;
import com.ricardocasarez.topgamedeals.service.DealsAlertService;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Fragment used to display the deals details.
 * For large screens it will be show as dialog.
 */
public class DealDetailFragment extends DialogFragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener
{
    private static final String LOG_TAG = DealDetailFragment.class.getSimpleName();
    public static final String TAG = DealDetailFragment.class.getSimpleName();
    private static final int LOADER_ID = 300;
    private static final int LOADER_ID_STORE = 301;
    private static final int LOADER_ID_ALERT = 302;
    private static final String DATE_FORMAT = "MMMM d, yyyy";
    private static final String TIME_ZONE = "UTC";

    public static final String EXTRA_GAME_DEAL_URI = "deal_uri";
    public static final String EXTRA_PARCELABLE_GAME_DEAL_OBJECT = "parcelable_game";

    private static final String REDIRECT_BASE_API = "http://www.cheapshark.com/redirect?";
    private static final String REDIRECT_DEAL_ID = "dealID";
    private static final String METACRITIC_HOME = "http://www.metacritic.com";

    // ui
    private ImageView mImageView;
    private TextView mGameTitle;
    private TextView mStoreName;
    private TextView mPrice;
    private TextView mOldPrice;
    private TextView mReleaseDate;
    private TextView mSavings;
    private TextView mMetacriticScore;
    private AppCompatButton mMetacriticButton;
    private RatingBar mRatingBar;
    private ProgressBar mProgressBar;
    private LinearLayout mMainLayout;

    // Object used to store information to show
    private GameDeal mGameDeal;

    // current deal uri
    private Uri mDealUri;
    private boolean mHasPriceAlert;

    /**
     * Creates an instance of DealDetailFragment with GameDeal as argument.
     * @param deal GameDeal object used to populate UI
     * @return new Instance of Fragment
     */
    public static DealDetailFragment newInstance(GameDeal deal) {
        DealDetailFragment fragment = new DealDetailFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(EXTRA_PARCELABLE_GAME_DEAL_OBJECT, deal);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // destroy loaders to avoid cache issues when showing fragment as dialog
        getActivity().getSupportLoaderManager().destroyLoader(LOADER_ID);
        getActivity().getSupportLoaderManager().destroyLoader(LOADER_ID_STORE);
        getActivity().getSupportLoaderManager().destroyLoader(LOADER_ID_ALERT);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setHasOptionsMenu(false);
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.deal_detail, container, false);

        // ui
        mGameTitle = (TextView) view.findViewById(R.id.textview_title);
        mStoreName = (TextView) view.findViewById(R.id.textview_store);
        mPrice = (TextView) view.findViewById(R.id.textview_price);
        mOldPrice = (TextView) view.findViewById(R.id.textview_old_price);
        mReleaseDate = (TextView) view.findViewById(R.id.textview_release_date);
        mSavings = (TextView) view.findViewById(R.id.textview_savings);
        mMetacriticScore = (TextView) view.findViewById(R.id.textview_metacritic_score);
        mRatingBar = (RatingBar) view.findViewById(R.id.rating_bar);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mMainLayout = (LinearLayout) view.findViewById(R.id.details_layout);
        // hide content until is loaded
        mMainLayout.setVisibility(View.GONE);

        // set up imageview
        mImageView = (ImageView) view.findViewById(R.id.imageview_art);
        int heightFactor = 5;
        if (MainActivity.sIsLargeScreen || getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            heightFactor = 3;
        }
        int height = getResources().getDisplayMetrics().heightPixels;
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mImageView.getLayoutParams();
        params.height = height / heightFactor;
        mImageView.setLayoutParams(params);

        // button
        AppCompatButton storeButton = (AppCompatButton) view.findViewById(R.id.button_store);
        mMetacriticButton = (AppCompatButton) view.findViewById(R.id.button_metacritic);
        storeButton.setOnClickListener(this);
        mMetacriticButton.setOnClickListener(this);

        // handle arguments extras
        handleAction(getArguments());

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        getContext().getContentResolver().unregisterContentObserver(mAlertContentObserver);
    }

    public void onResume() {
        super.onResume();

        getContext().getContentResolver().registerContentObserver(
                DealsContract.AlertsEntry.CONTENT_URI, false, mAlertContentObserver);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // clean menu
        menu.clear();

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_details, menu);
        if (mHasPriceAlert) {
            menu.removeItem(R.id.action_add_alert);
        }

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider
        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        mShareActionProvider.setShareIntent(createShareIntent());

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_alert) {
            showAddAlertDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        if (mHasPriceAlert) {
            menu.removeItem(R.id.action_add_alert);
        }
        super.onPrepareOptionsMenu(menu);
    }

    /**
     * Determines if is necessary to query data base for additional data.
     * @param deal current data
     * @return true if additional information is required
     */
    private boolean isAdditionalInfoRequired(GameDeal deal) {
        return (deal == null || deal.getReleaseDate() <= 0 || TextUtils.isEmpty(deal.getMetacriticLink()));
    }

    /**
     * Handles fragment argument.
     * If argument EXTRA_PARCELABLE_GAME_DEAL_OBJECT is not null, db will be query for additional
     * data. If not UI will be refreshed.
     * If argument EXTRA_GAME_DEAL_URI, query db to get additional data.
     * @param arguments Fragment arguments. Expected to get EXTRA_PARCELABLE_GAME_DEAL_OBJECT or
     *                  EXTRA_PARCELABLE_GAME_DEAL_OBJECT
     */
    public void handleAction(Bundle arguments) {
        if (arguments != null) {
            mGameDeal = arguments.getParcelable(EXTRA_PARCELABLE_GAME_DEAL_OBJECT);

            if (mGameDeal != null) {
                if (isAdditionalInfoRequired(mGameDeal)) {
                    // additional data is required to display, lets query to db with the id uri
                    mDealUri = DealsContract.GameDealEntry.buildGameDealUri(mGameDeal.getId());
                    getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
                } else {
                    // query store name
                    getActivity().getSupportLoaderManager().initLoader(LOADER_ID_STORE, null, this);
                    getActivity().getSupportLoaderManager().initLoader(LOADER_ID_ALERT, null, this);
                }
            } else{
                // comes from search suggestion, query to db
                mDealUri = Uri.parse(arguments.getString(EXTRA_PARCELABLE_GAME_DEAL_OBJECT));
                getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
            }
        }
    }

    /**
     * Shows an AlertDialog to add price alerts.
     * User need to input email and desired price for the alert.
     * Email will be remembered for the nex time.
     */
    public void showAddAlertDialog() {
        // get dialog view
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View textEntryView = factory.inflate(R.layout.dialog_add_alert, null);

        // get pref email
        String registeredEmail = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.pref_email_edit_key), getString(R.string.pref_email_edit_default));

        if (!TextUtils.isEmpty(registeredEmail)) {
            EditText emailTextView = ((EditText) textEntryView.findViewById(R.id.edit_text_add_alert_email));
            emailTextView.setText(registeredEmail);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()/*, R.style.AppCompatAlertDialogStyle*/);
        builder.setTitle(R.string.add_alert_title);
        builder.setMessage(R.string.add_alert_msg);
        builder.setPositiveButton(R.string.add_alert_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = ((EditText) textEntryView.findViewById(R.id.edit_text_add_alert_email))
                        .getEditableText().toString();
                String price = ((EditText) textEntryView.findViewById(R.id.edit_text_add_alert_price))
                        .getEditableText().toString();

                Intent service = new Intent(getActivity(), DealsAlertService.class);
                service.putExtra(DealsAlertService.EXTRA_ALERT_EMAIL, email);
                service.putExtra(DealsAlertService.EXTRA_ALERT_GAME_ID, String.valueOf(mGameDeal.getGameID()));
                service.putExtra(DealsAlertService.EXTRA_ALERT_PRICE, price);
                service.putExtra(DealsAlertService.EXTRA_ALERT_ACTION, DealsAlertService.ACTION_SET);
                getActivity().startService(service);
            }
        });
        builder.setNegativeButton(R.string.add_alert_cancel, null);

        builder.setView(textEntryView);

        builder.show();
    }

    /**
     * Set values to user interface using GameDeal object.
     * @param deal object used to pupulate UI
     */
    public void setUiData(GameDeal deal) {
        if (deal != null) {

            // load image url into ImageView
            Picasso.with(getActivity()).load(deal.getThumbUrl()).into(mImageView);

            // Game title
            String title = deal.getGameTitle();//data.getString(COL_GAME_TITLE);
            mGameTitle.setText(title);

            // Rating
            int metacriticScore = deal.getMetacriticScore();
            float raiting = metacriticScore > 0 ?  metacriticScore/20 : 0;
            mRatingBar.setRating(raiting);

            // price
            float price = deal.getSalePrice();
            String formattedPrice = String.format(getString(R.string.format_price), price);
            mPrice.setText(formattedPrice);

            // old price
            if (mGameDeal.getSavings() > 0) {
                float oldPrice = deal.getNormalPrice();
                String formattedOldPrice = String.format(getString(R.string.format_price), oldPrice);
                mOldPrice.setVisibility(View.VISIBLE);
                mOldPrice.setText(formattedOldPrice);
                mOldPrice.setPaintFlags(
                        mOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                mOldPrice.setVisibility(View.INVISIBLE);
            }

            // release date
            long dateMilliseconds = deal.getReleaseDate() * 1000l;
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));

            String formattedDate = sdf.format(new Date(dateMilliseconds));
            String releaseDate = String.format(getResources().getString(R.string.release_date), formattedDate);
            mReleaseDate.setText(releaseDate);

            // metracritic score
            String score = String.format(getString(R.string.metacritic_score), deal.getMetacriticScore());
            mMetacriticScore.setText(score);

            // savings
            int savings = (int) deal.getSavings();
            if (savings > 0) {
                String fmtSavings = String.format(getResources().getString(R.string.format_savings), savings);
                mSavings.setVisibility(View.VISIBLE);
                mSavings.setText(fmtSavings);
            } else {
                mSavings.setVisibility(View.INVISIBLE);
            }

            // disable button if link is unavailable
            if (TextUtils.isEmpty(mGameDeal.getMetacriticLink())) {
                mMetacriticButton.setVisibility(View.GONE);
            } else
                mMetacriticButton.setVisibility(View.VISIBLE);
            if (mProgressBar != null)
                mProgressBar.setVisibility(View.GONE);
            if (mMainLayout != null) {
                mMainLayout.setVisibility(View.VISIBLE);
            }

        }
    }

    /**
     * Creates a share intent.
     * @return  Intent with the follow format:
     *          GAME_TITLE XX% off
     *          Store link of the deal
     */
    public Intent createShareIntent() {
        if (mGameDeal== null)
            return null;

        int savings = (int) mGameDeal.getSavings();
        String formattedSavings = String.format(getResources().getString(R.string.format_savings), savings);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        String content = mGameDeal.getGameTitle() + " " + formattedSavings + "\n" + REDIRECT_BASE_API + REDIRECT_DEAL_ID + mGameDeal.getDealID();
        shareIntent.putExtra(Intent.EXTRA_TEXT, content);

        return shareIntent;
    }


    ////////////////////////////// LoaderCallbacks
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader "+id);
        if (id == LOADER_ID) {
            // query db additional data
            return new CursorLoader(getActivity(),
                    mDealUri,
                    DealsContract.GameDealEntry.ALL_COLUMNS,
                    null,
                    null,
                    null);
        } else if (id == LOADER_ID_STORE) {
            // query store data
            return new CursorLoader(getActivity(),
                    DealsContract.StoreEntry.buildStoreUri(mGameDeal.getStoreID()),
                    new String[]{DealsContract.StoreEntry.COLUMN_STORE_NAME},
                    null,
                    null,
                    null
            );
        } else if (id == LOADER_ID_ALERT) {
            Log.v(LOG_TAG, "LOADER_ID_ALERT:"+String.valueOf(mGameDeal.getGameID()));
            // query alert for this game
            return new CursorLoader(getActivity(),
                    DealsContract.AlertsEntry.CONTENT_URI,
                    null,
                    DealsContract.AlertsEntry.COLUMN_GAME_ID + " = ?",
                    new String[]{String.valueOf(mGameDeal.getGameID())},
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "onLoadFinished "+loader.getId());
        int id = loader.getId();
        if (id == LOADER_ID) {
            if (data != null && data.moveToFirst()) {
                mGameDeal = GameDeal.populateGameDeal(data);

                // now query store name
                getActivity().getSupportLoaderManager().initLoader(LOADER_ID_STORE, null, this);
                getActivity().getSupportLoaderManager().initLoader(LOADER_ID_ALERT, null, this);
            }
        } else if (id == LOADER_ID_STORE) {
            if (data != null && data.moveToFirst()) {
                String mStore = data.getString(0); //COLUMN_STORE_NAME
                mStoreName.setText(mStore);

                // update ui
                setUiData(mGameDeal);
            }
        } else if (id == LOADER_ID_ALERT) {
            if (data != null && data.getCount() > 0) {
                //update options menu
                mHasPriceAlert = true;
                getActivity().supportInvalidateOptionsMenu();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    ////////////////////////////// View.OnClickListener
    @Override
    public void onClick(View v) {
        int id = v.getId();
        Uri uri = null;

        if (id == R.id.button_store) {
            uri = Uri.parse(REDIRECT_BASE_API + "dealID=" + mGameDeal.getDealID());
        } else if (id == R.id.button_metacritic) {
            uri = Uri.parse(METACRITIC_HOME + mGameDeal.getMetacriticLink());
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else
            Toast.makeText(getActivity(), R.string.intent_error, Toast.LENGTH_SHORT).show();
    }

    // Observer used to listent for Alert table changes
    final ContentObserver mAlertContentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            //update toolbar
            mHasPriceAlert = true;
            getActivity().supportInvalidateOptionsMenu();
        }
    };
}