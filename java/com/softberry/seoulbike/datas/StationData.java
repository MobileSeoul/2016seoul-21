package com.softberry.seoulbike.datas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapView;

import com.softberry.seoulbike.interfaces.IOnMarkerClick;
import com.softberry.seoulbike.views.MarkerOverlay;
import com.softberry.seoulbike.R;

/**
 * Created by parkjs on 2016-10-01.
 */
public class StationData {
    private static Bitmap IMG_MARKER;
    private static Bitmap IMG_SELECTED_MARKER;


    private double coorX;
    private double coorY;
    private String address;
    private String name;
    private String stationNo;
    private int chargoSize;

    private int mTempDistance;

    // TMap custom marker
    private MarkerOverlay mMarker = null;

    public void setCoorX(double val) {
        this.coorX = val;
    }

    public void setCoorY(double val) {
        this.coorY = val;
    }

    public void setAddress(String val) {
        this.address = val;
    }

    public void setName(String val) {
        this.name = val;
    }

    public void setStationNo(String val) {
        this.stationNo = val;
    }

    public void setChargoSize(int val) {
        this.chargoSize = val;
    }

    public String getAddress() {
        return this.address;
    }

    public String getName() {
        return this.name;
    }

    public String getStationNo() {
        return this.stationNo;
    }

    public double getCoorX() {
        return this.coorX;
    }

    public double getCoorY() {
        return this.coorY;
    }

    public int getChargoSize() {
        return this.chargoSize;
    }

    public void setDistance(int distance) {
        mTempDistance = distance;
    }

    public int getDistance() {
        return mTempDistance;
    }

    public Bitmap getImgMarker(Context context) {
        if (IMG_MARKER == null) {
            Bitmap img = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_bike);
            int height = img.getHeight();
            int width = img.getWidth();
            IMG_MARKER = Bitmap.createScaledBitmap(img, (int) (width * 0.15), (int) (height * 0.15), true);
        }
        return IMG_MARKER;
    }

    public Bitmap getSelectImgMarker(Context context) {
        if (IMG_SELECTED_MARKER == null) {
            Bitmap img = BitmapFactory.decodeResource(context.getResources(), R.drawable.select_marker_icon);
            int height = img.getHeight();
            int width = img.getWidth();
            IMG_SELECTED_MARKER = Bitmap.createScaledBitmap(img, (int) (width * 0.15), (int) (height * 0.15), true);
        }
        return IMG_SELECTED_MARKER;
    }

    public MarkerOverlay getMarker(Context context, TMapView mapView, IOnMarkerClick listener) {
        if(mMarker == null) {
            mMarker = new MarkerOverlay(context.getApplicationContext(), mapView, listener);
        }
        mMarker.setID(stationNo);
        mMarker.setIcon(getImgMarker(context));
        mMarker.setTMapPoint(new TMapPoint(coorY, coorX));
        mMarker.setPosition(0.5f, 1);

        return mMarker;
    }
}
