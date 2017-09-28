package com.softberry.seoulbike.views;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.softberry.seoulbike.R;
import com.skp.Tmap.TMapPOIItem;

import java.util.ArrayList;

public class SearchResultAdapter extends ArrayAdapter<TMapPOIItem> {
    private final String TAG = "SearchResultAdapter";

    private Context mContext;
    private ArrayList<TMapPOIItem> mTmapPOIItems;

    public SearchResultAdapter(Context context, ArrayList<TMapPOIItem> tmapPOIItems) {
        super(context, R.layout.list_search_result_row, tmapPOIItems);

        mContext = context;
        mTmapPOIItems = tmapPOIItems;
    }

    @Override
    public int getCount() {
        return mTmapPOIItems.size();
    }

    @Override
    public TMapPOIItem getItem(int position) {
        return mTmapPOIItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.list_search_result_row, parent, false);
        }

        final TMapPOIItem tmapPOIItem = (TMapPOIItem) getItem(position);

        TextView tvSearchName = (TextView) convertView.findViewById(R.id.tv_search_result_name);
        tvSearchName.setText(tmapPOIItem.getPOIName());

        TextView tvSearchAddress = (TextView) convertView.findViewById(R.id.tv_search_result_address);
        tvSearchAddress.setText(tmapPOIItem.getPOIAddress().replace("null", ""));

        Log.d(TAG, "position: " + position +
                   ", POI Name: " + tmapPOIItem.getPOIName().toString() +
                   ", Address: " + tmapPOIItem.getPOIAddress().replace("null", "") +
                   ", Point: " + tmapPOIItem.getPOIPoint().toString());

        return convertView;
    }

    public void setArrayList(ArrayList<TMapPOIItem> tMapPOIItems) {
        this.mTmapPOIItems = tMapPOIItems;
    }

    public ArrayList<TMapPOIItem> getArrayList() {
        return mTmapPOIItems;
    }
}
