package com.softberry.seoulbike.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.softberry.seoulbike.R;
import com.softberry.seoulbike.datas.StationData;

import java.util.List;

/**
 * Created by parkjs on 2016-10-01.
 */
public class StationListAdapter extends BaseAdapter {

    private Context mContext;
    private List<StationData> mItems;


    public StationListAdapter(Context context, List<StationData> items){
        mContext = context;
        mItems = items;
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mItems.size();
    }

    @Override
    public StationData getItem(int position) {
        // TODO Auto-generated method stub
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.station_list_adapter, null);
            holder = new ViewHolder();
            holder.nameText = (TextView) convertView.findViewById(R.id.adapter_station_name);
            holder.addressText = (TextView) convertView.findViewById(R.id.adapter_station_address);
            holder.chargoText = (TextView) convertView.findViewById(R.id.adapter_station_chargo);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final StationData item = mItems.get(position);
        String name = item.getName();
        String chargo = String.valueOf(item.getChargoSize()) +"대 보관가능";
        holder.nameText.setText(name);
        holder.addressText.setText(item.getAddress());
        holder.chargoText.setText(chargo);
        return convertView;
    }

    private class ViewHolder{
        public TextView nameText;
        public TextView addressText;
        public TextView chargoText;
    }

}