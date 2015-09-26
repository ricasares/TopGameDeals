package com.ricardocasarez.topgamedeals.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.ricardocasarez.topgamedeals.data.DealsContract;

import java.util.List;

/**
 * Created by ricardo.casarez on 8/25/2015.
 */
public class GameDeal implements Parcelable {

    private int id;
    @SerializedName("title")
    private String gameTitle;
    private String metacriticLink;
    private String dealID;
    private int storeID;
    private int gameID;
    private float salePrice;
    private float normalPrice;
    private float savings;
    private int metacriticScore;
    private long releaseDate;
    private long lastChange;
    private float dealRating;
    @SerializedName("thumb")
    private String thumbUrl;

    public GameDeal() {

    }

    public GameDeal(String dealID, String gameTitle, float salePrice, float normalPrice, float savings,
                    String defaultThumbUrl, String metacriticLink, int metacriticScore,
                    long releaseDate) {
        this.dealID = dealID;
        this.gameTitle = gameTitle;
        this.salePrice = salePrice;
        this.normalPrice = normalPrice;
        this.savings = savings;
        this.thumbUrl = defaultThumbUrl;
        this.metacriticLink = metacriticLink;
        this.metacriticScore = metacriticScore;
        this.releaseDate = releaseDate;
        //this.store = store;
    }

    protected GameDeal(Parcel in) {
        id = in.readInt();
        dealID = in.readString();
        gameTitle = in.readString();
        thumbUrl = in.readString();
        salePrice = in.readFloat();
        normalPrice = in.readFloat();
        metacriticScore = in.readInt();

        if (in.dataAvail() > 0)
            metacriticLink = in.readString();
        if (in.dataAvail() > 0)
            savings = in.readFloat();
        if (in.dataAvail() > 0)
            releaseDate = in.readLong();
        if (in.dataAvail() > 0)
            storeID = in.readInt();
        if (in.dataAvail() > 0)
            gameID = in.readInt();
    }

    public static final Creator<GameDeal> CREATOR = new Creator<GameDeal>() {
        @Override
        public GameDeal createFromParcel(Parcel in) {
            return new GameDeal(in);
        }

        @Override
        public GameDeal[] newArray(int size) {
            return new GameDeal[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(dealID);
        dest.writeString(gameTitle);
        dest.writeString(thumbUrl);
        dest.writeFloat(salePrice);
        dest.writeFloat(normalPrice);
        dest.writeInt(metacriticScore);

        dest.writeString(metacriticLink);

        dest.writeFloat(savings);
        dest.writeLong(releaseDate);

        dest.writeInt(storeID);
        dest.writeInt(gameID);
    }

    public String getGameTitle() {
        return gameTitle;
    }

    public void setGameTitle(String gameTitle) {
        this.gameTitle = gameTitle;
    }

    public String getMetacriticLink() {
        return metacriticLink;
    }

    public void setMetacriticLink(String metacriticLink) {
        this.metacriticLink = metacriticLink;
    }

    public String getDealID() {
        return dealID;
    }

    public void setDealID(String dealID) {
        this.dealID = dealID;
    }

    public int getStoreID() {
        return storeID;
    }

    public void setStoreID(int storeID) {
        this.storeID = storeID;
    }

    public int getGameID() {
        return gameID;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    public float getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(float salePrice) {
        this.salePrice = salePrice;
    }

    public float getNormalPrice() {
        return normalPrice;
    }

    public void setNormalPrice(float normalPrice) {
        this.normalPrice = normalPrice;
    }

    public float getSavings() {
        return savings;
    }

    public void setSavings(float savings) {
        this.savings = savings;
    }

    public int getMetacriticScore() {
        return metacriticScore;
    }

    public void setMetacriticScore(int metacriticScore) {
        this.metacriticScore = metacriticScore;
    }

    public long getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(long releaseDate) {
        this.releaseDate = releaseDate;
    }

    public long getLastChange() {
        return lastChange;
    }

    public void setLastChange(long lastChange) {
        this.lastChange = lastChange;
    }

    public float getDealRating() {
        return dealRating;
    }

    public void setDealRating(float dealRating) {
        this.dealRating = dealRating;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static GameDeal populateGameDeal(Cursor cursor) {
        if (cursor != null) {
            GameDeal deal = new GameDeal();
            deal.setId(cursor.getInt(DealsContract.GameDealEntry.ALL_COL_ID));
            deal.setDealID(cursor.getString(DealsContract.GameDealEntry.ALL_COL_DEAL_ID));
            deal.setGameTitle(cursor.getString(DealsContract.GameDealEntry.ALL_COL_GAME_TITLE));
            deal.setThumbUrl(cursor.getString(DealsContract.GameDealEntry.ALL_COL_THUMB));
            deal.setSalePrice(cursor.getFloat(DealsContract.GameDealEntry.ALL_COL_PRICE));
            deal.setNormalPrice(cursor.getFloat(DealsContract.GameDealEntry.ALL_COL_OLD_PRICE));
            deal.setMetacriticScore(cursor.getInt(DealsContract.GameDealEntry.ALL_COL_METACRITIC_SCORE));

            // optional columns
            if (cursor.getColumnCount() > DealsContract.GameDealEntry.ALL_COL_METACRITIC_SCORE) {
                deal.setMetacriticLink(cursor.getString(DealsContract.GameDealEntry.ALL_COL_METACRITIC_LINK));
                deal.setSavings(cursor.getFloat(DealsContract.GameDealEntry.ALL_COL_SAVINGS));
                deal.setReleaseDate(cursor.getLong(DealsContract.GameDealEntry.ALL_COL_RELEASE_DATE));
                deal.setGameID(cursor.getInt(DealsContract.GameDealEntry.ALL_COL_GAME_ID));
                deal.setStoreID(cursor.getInt(DealsContract.GameDealEntry.ALL_COL_STORE_ID));
            }

            return deal;
        }
        return null;
    }
}
