package com.ricardocasarez.topgamedeals.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.ricardocasarez.topgamedeals.data.DealsContract;

/**
 * Created by ricardo.casarez on 9/17/2015.
 */
public class Store implements Parcelable {
    @SerializedName("storeID")
    int id;
    @SerializedName("storeName")
    String name;

    public Store () {
    }

    protected Store(Parcel in) {
        id = in.readInt();
        name = in.readString();
    }

    public Store(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static final Creator<Store> CREATOR = new Creator<Store>() {
        @Override
        public Store createFromParcel(Parcel in) {
            return new Store(in);
        }

        @Override
        public Store[] newArray(int size) {
            return new Store[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Store populateStore(Cursor cursor) {
        if (cursor != null) {
            String storeID = cursor.getString(DealsContract.StoreEntry.ALL_COL_ID);
            String name = cursor.getString(DealsContract.StoreEntry.ALL_COL_STORE_NAME);

            int id = Integer.parseInt(storeID);
            return new Store(id, name);
        }
        return null;
    }
}
