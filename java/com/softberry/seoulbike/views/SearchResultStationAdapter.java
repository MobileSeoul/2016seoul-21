package com.softberry.seoulbike.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.softberry.seoulbike.R;
import com.softberry.seoulbike.datas.StationData;
import com.softberry.seoulbike.interfaces.ButtonClickListener;

import java.util.ArrayList;

/**
 * Created by park shin on 2016-10-04.
 */

public class SearchResultStationAdapter extends ArrayAdapter<StationData> implements View.OnClickListener {

    private final String TAG = "SearchResultStationAdapter";

    private Context mContext;
    private ArrayList<StationData> mStationData;

    private ButtonClickListener mClickListener = null;

    public SearchResultStationAdapter(Context context, ArrayList<StationData> stationDatas) {
        super(context, R.layout.list_search_result_station_row, stationDatas);

        mContext = context;
        mStationData = stationDatas;
    }

    @Override
    public int getCount() {
        return mStationData.size();
    }

    @Override
    public StationData getItem(int position) {
        return mStationData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.list_search_result_station_row, parent, false);
        }

        final StationData stationData = (StationData) getItem(position);

        TextView tvName = (TextView) convertView.findViewById(R.id.tv_search_result_station_name);
        tvName.setText(stationData.getName() + ".");

        TextView tvAddress = (TextView) convertView.findViewById(R.id.tv_search_result_station_address);
        tvAddress.setText(stationData.getAddress());

        TextView tvDistance = (TextView) convertView.findViewById(R.id.tv_search_result_station_distance);
        tvDistance.setText("거리: " + stationData.getDistance() + "m");

        Button btnDestStart = (Button) convertView.findViewById(R.id.btn_destination_start);
        btnDestStart.setTag(position);
        btnDestStart.setOnClickListener(this);

        Button btnDestFinish = (Button) convertView.findViewById(R.id.btn_destination_finish);
        btnDestFinish.setTag(position);
        btnDestFinish.setOnClickListener(this);

        Button btnFindInMap = (Button) convertView.findViewById(R.id.btn_find_in_map);
        btnFindInMap.setTag(position);
        btnFindInMap.setOnClickListener(this);

        return convertView;
    }

    public void setArrayList(ArrayList<StationData> stationDatas) {
        this.mStationData = stationDatas;
    }

    public ArrayList<StationData> getArrayList() {
        return mStationData;
    }

    public void setOnButtonClickListener(ButtonClickListener listener) {
        mClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (mClickListener != null) {
            mClickListener.onButtonClick(v, (Integer) v.getTag());
        }
    }

}
