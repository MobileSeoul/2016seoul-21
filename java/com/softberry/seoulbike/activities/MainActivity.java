package com.softberry.seoulbike.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapMarkerItem;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;
import com.skp.Tmap.TMapView;
import com.softberry.seoulbike.R;
import com.softberry.seoulbike.datas.StationData;
import com.softberry.seoulbike.interfaces.ButtonClickListener;
import com.softberry.seoulbike.interfaces.IOnMarkerClick;
import com.softberry.seoulbike.utils.Const;
import com.softberry.seoulbike.views.SearchResultAdapter;
import com.softberry.seoulbike.views.SearchResultStationAdapter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity implements
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        IOnMarkerClick {

    private final String TAG = "MainActivity";

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    private static final String LOCATION_KEY = "location-key";
    private static final String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates = true;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;

    private ArrayList<StationData> mStationList;

    private TMapView mMapView = null;
    private RelativeLayout mMapLayout;
    private DrawerLayout mDrawerLayout;
    private RelativeLayout mNavigationView;
    private RelativeLayout mDashBoardView;

    // >> navigation mode
    private boolean isNaviStartMode = false;

    private RelativeLayout mLayoutSearch;
    private ToggleButton mBtnMode;

    private TextView mTvStart;
    private TextView mTvFinish;

    private TMapPOIItem mTempPoint = null;
    private TMapPOIItem mStartPoint = null;
    private TMapPOIItem mEndPoint = null;

    private TMapMarkerItem mStartItem;
    private TMapMarkerItem mGoalItem;


    private StationData mStartStation = null;
    private StationData mFinishStation = null;

    private EditText mEditSearch;

    private ListView mSearchResult;
    private SearchResultAdapter mSearchResultAdapter;

    private ListView mStationResult;
    private SearchResultStationAdapter mStationResultAdapter;

    private TMapPolyLine mNaviLine;
    private TMapPolyLine mMyTrackingLine;

    private float mMaxSpeed = 0F;

    // Map Mode Select Station Info Layer
    private LinearLayout mStationInfoLayer;
    private StationData mSelectedMarkerData;
    // << navigation mode

    private TMapData mTmapData;

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String searchWord = s.toString();
            if (searchWord.length() > 0) {
                //Log.d(TAG, "search word = " + searchWord + ", start = " + start + ", before = " + before + ", count = " + count);
                searchAddress(searchWord);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mTmapData = new TMapData();

        initView();
        initData();
        changeMode(false);

        buildGoogleApiClient();

        addCustomMaker();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        checkPlayServices();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    public void onBackPressed() {
        if (mBtnMode.isChecked()) {
            changeMode(false);
        } else {
            new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.title_exit)
                    .setMessage(R.string.exit_notice)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        }
    }

    private void makeMessageDialog(String title, String message) {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void initView() {
        initNavigationModeView();

        mMapLayout = (RelativeLayout) findViewById(R.id.tmap_layout);
        mMapView = new TMapView(getApplicationContext());
        configureMapView();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (RelativeLayout) findViewById(R.id.left_drawer);
        mDashBoardView = (RelativeLayout) findViewById(R.id.navi_dashboard_layer);
        mDashBoardView.setVisibility(View.GONE);

        Button btnMenu = (Button) findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(this);

        LinearLayout sideCallBtn = (LinearLayout) findViewById(R.id.side_call_btn);
        sideCallBtn.setOnClickListener(this);

        LinearLayout sideServiceBtn = (LinearLayout) findViewById(R.id.side_service__btn);
        sideServiceBtn.setOnClickListener(this);

        LinearLayout sidePayBtn = (LinearLayout) findViewById(R.id.side_pay_btn);
        sidePayBtn.setOnClickListener(this);

        LinearLayout sideRentalBtn = (LinearLayout) findViewById(R.id.side_rental_bike);
        sideRentalBtn.setOnClickListener(this);

        LinearLayout sideReturnBtn = (LinearLayout) findViewById(R.id.side_return_bkie);
        sideReturnBtn.setOnClickListener(this);

        LinearLayout sideLinkAppBtn = (LinearLayout) findViewById(R.id.side_seoul_bike_app);
        sideLinkAppBtn.setOnClickListener(this);

        Button startSelectBtn = (Button) findViewById(R.id.start_select_btn);
        startSelectBtn.setOnClickListener(this);
        Button finshSelectBtn = (Button) findViewById(R.id.finish_select_btn);
        finshSelectBtn.setOnClickListener(this);
        Button naviStartBtn = (Button) findViewById(R.id.start_navi_btn);
        naviStartBtn.setOnClickListener(this);
        Button rootCancelBtn = (Button) findViewById(R.id.root_cancel_btn);
        rootCancelBtn.setOnClickListener(this);

//        LinearLayout currentLocationBtn = (LinearLayout) findViewById(R.id.my_location_button);
//        currentLocationBtn.setOnClickListener(this);

        mStationInfoLayer = (LinearLayout) findViewById(R.id.station_info_layer);
        mStationInfoLayer.setVisibility(View.GONE);
    }

    private void initNavigationModeView() {
        mLayoutSearch = (RelativeLayout) findViewById(R.id.layout_find_station);

        mBtnMode = (ToggleButton) findViewById(R.id.btn_mode);
        mBtnMode.setOnClickListener(this);

        Button btnSearch = (Button) findViewById(R.id.btn_search_navi_start);
        btnSearch.setOnClickListener(this);

        Button btnSearchDelete = (Button) findViewById(R.id.btn_search_delete);
        btnSearchDelete.setOnClickListener(this);

        mSearchResultAdapter = new SearchResultAdapter(MainActivity.this, new ArrayList<TMapPOIItem>());
        mSearchResult = (ListView) findViewById(R.id.start_address_list);
        mSearchResult.setAdapter(mSearchResultAdapter);
        mSearchResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mTempPoint = mSearchResultAdapter.getItem(position);
                mEditSearch.setText("");
                mSearchResultAdapter.getArrayList().clear();

                ArrayList<StationData> stationList = searchBikeStation(mTempPoint.getPOIPoint());
                hideKeyBoard(mEditSearch);
                if (stationList.size() > 0) {
                    mSearchResult.setVisibility(View.GONE);
                    mStationResult.setVisibility(View.VISIBLE);

                    mStationResultAdapter.setArrayList(stationList);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mStationResultAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    makeMessageDialog(getResources().getString(R.string.no_station_round_title), getResources().getString(R.string.no_station_round_message));
                }
            }
        });

        mStationResultAdapter = new SearchResultStationAdapter(MainActivity.this, new ArrayList<StationData>());
        mStationResultAdapter.setOnButtonClickListener(new ButtonClickListener() {
            @Override
            public void onButtonClick(View v, int position) {
                StationData station = mStationResultAdapter.getItem(position);

                switch (v.getId()) {
                    case R.id.btn_destination_start:
                        mStartPoint = mTempPoint;
                        setStartStation(station);
                        break;

                    case R.id.btn_destination_finish:
                        mEndPoint = mTempPoint;
                        setFinishStation(station);
                        break;

                    case R.id.btn_find_in_map:
                        changeMode(false);
                        mMapView.setCenterPoint(station.getCoorX(), station.getCoorY());
                        mMapView.getMarkerItem2FromID(station.getStationNo()).setMarkerTouch(true);
                        OnMarkerClick(station.getStationNo());
                        break;
                }

            }
        });

        mStationResult = (ListView) findViewById(R.id.start_station_list);
        mStationResult.setAdapter(mStationResultAdapter);

        mTvStart = (TextView) findViewById(R.id.tv_destination_start);
        mTvFinish = (TextView) findViewById(R.id.tv_destination_finish);

        mEditSearch = (EditText) findViewById(R.id.et_search_start);
        mEditSearch.addTextChangedListener(mTextWatcher);
        mEditSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b == true) {
                    searchAddress(mEditSearch.getText().toString());
                }
            }
        });
    }

    private void initData() {
        mStationList = new ArrayList<StationData>();
        String[] stationNoArray = getResources().getStringArray(R.array.station_no);
        String[] stationNameArray = getResources().getStringArray(R.array.station_name);
        String[] stationAddressArray = getResources().getStringArray(R.array.station_address);
        String[] stationCoorXArray = getResources().getStringArray(R.array.station_x);
        String[] stationCoorYArray = getResources().getStringArray(R.array.station_y);
        String[] stationChargoArray = getResources().getStringArray(R.array.station_size);

        for (int i = 0; i < stationNoArray.length; i++) {
            StationData data = new StationData();
            data.setStationNo(stationNoArray[i]);
            data.setName(stationNameArray[i]);
            data.setCoorX(Double.parseDouble(stationCoorXArray[i]));
            data.setCoorY(Double.parseDouble(stationCoorYArray[i]));
            data.setAddress(stationAddressArray[i]);
            data.setChargoSize(Integer.parseInt(stationChargoArray[i]));
            mStationList.add(data);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btn_menu:
                if (mDrawerLayout.isDrawerOpen(mNavigationView)) {
                    mDrawerLayout.closeDrawer(mNavigationView);
                } else {
                    mDrawerLayout.openDrawer(mNavigationView);
                }
                break;

//            case R.id.my_location_button:
//                updateTMap();
//                break;

            case R.id.btn_mode:
                changeMode(mBtnMode.isChecked());
                break;

            case R.id.start_select_btn:
                setStartStation(mSelectedMarkerData);
                mLayoutSearch.setVisibility(View.VISIBLE);
                break;

            case R.id.finish_select_btn:
                setFinishStation(mSelectedMarkerData);
                mLayoutSearch.setVisibility(View.VISIBLE);
                break;

            case R.id.start_navi_btn:
                mLayoutSearch.setVisibility(View.GONE);
                mStationInfoLayer.setVisibility(View.GONE);
                startNavigation();
                break;

            case R.id.root_cancel_btn:
                isNaviStartMode = false;
                mDashBoardView.setVisibility(View.GONE);
                mMaxSpeed = 0F;
                mMapView.removeAllTMapPolyLine();;
                mMapView.removeTMapPath();

                setStartStation(null);
                setFinishStation(null);
                removeStartGoalPoint();
                addCustomMaker();
                break;

            case R.id.btn_search_navi_start:
                changeMode(false);
                startNavigationSearchMode();

                startPedestrialNevigation();
                int height = BitmapFactory.decodeResource(getResources(), R.drawable.aaaa).getHeight();
                int width = BitmapFactory.decodeResource(getResources(), R.drawable.aaaa).getWidth();
                Bitmap s = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.aaaa), (int) (width * 0.15), (int) (height * 0.15), true);
                Bitmap g = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bbbb), (int) (width * 0.15), (int) (height * 0.15), true);
                mStartItem = new TMapMarkerItem();
                mStartItem.setTMapPoint(mStartPoint.getPOIPoint());
                mStartItem.setIcon(s);
                mGoalItem = new TMapMarkerItem();
                mGoalItem.setTMapPoint(mEndPoint.getPOIPoint());
                mGoalItem.setIcon(g);

                mMapView.addMarkerItem("startItem", mStartItem);
                mMapView.addMarkerItem("goalItem", mGoalItem);
                break;

            case R.id.btn_search_delete:
                clearResult();
                mSearchResult.setVisibility(View.VISIBLE);
                mStationResult.setVisibility(View.GONE);
                removeStartGoalPoint();
                break;

            case R.id.side_call_btn:
                Uri num = Uri.parse("tel:1599-0120");
                intent = new Intent(Intent.ACTION_CALL, num);
                startActivity(intent);
                break;

            case R.id.side_service__btn:
                mDrawerLayout.closeDrawer(mNavigationView);
                intent = new Intent(this, WebViewActivity.class);
                intent.putExtra("title", "서비스 안내");
                intent.putExtra("path", "file:///android_asset/service.html");
                startActivity(intent);
                break;

            case R.id.side_pay_btn:
                mDrawerLayout.closeDrawer(mNavigationView);
                intent = new Intent(this, WebViewActivity.class);
                intent.putExtra("title", "결제 안내");
                intent.putExtra("path", "file:///android_asset/pay.html");
                startActivity(intent);
                break;

            case R.id.side_rental_bike:
                mDrawerLayout.closeDrawer(mNavigationView);
                intent = new Intent(this, WebViewActivity.class);
                intent.putExtra("title", "자전거 대여");
                intent.putExtra("path", "file:///android_asset/rental.html");
                startActivity(intent);
                break;

            case R.id.side_return_bkie:
                mDrawerLayout.closeDrawer(mNavigationView);
                intent = new Intent(this, WebViewActivity.class);
                intent.putExtra("title", "자전거 반납");
                intent.putExtra("path", "file:///android_asset/return.html");
                startActivity(intent);
                break;

            case R.id.side_seoul_bike_app:
                if(hasBikeSeoulApp()){
                    intent = getPackageManager().getLaunchIntentForPackage("com.dki.spb_android");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }else{
                    String url = "https://play.google.com/store/apps/details?id=com.dki.spb_android";
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
                break;
        }
    }

    /**
     * setSKPMapApiKey()에 ApiKey를 입력 한다.
     * setSKPMapBizappId()에 mBizAppID를 입력한다.
     */
    private void configureMapView() {
        mMapView.setSKPMapApiKey(Const.TMAP_APP_KEY);
        mMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        mMapView.setIconVisibility(true);
        mMapView.setZoomLevel(15);
        mMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        //mMapView.setCompassMode(true);
        //mMapView.setTrackingMode(true);

        mMapLayout.addView(mMapView);
    }

    private void removeStartGoalPoint(){
        if(mStartItem != null){
            mMapView.removeMarkerItem("startItem");
            mStartItem = null;
        }
        if(mGoalItem != null){
            mMapView.removeMarkerItem("goalItem");
            mGoalItem = null;
        }
    }
    private void addCustomMaker() {
        for (int i = 0; i < mStationList.size(); i++) {
            mMapView.addMarkerItem2(mStationList.get(i).getStationNo(), mStationList.get(i).getMarker(this, mMapView, this));
        }
    }

    private void removeCustomMarker() {
        for (int i = 0; i < mStationList.size(); i++) {
            mMapView.removeMarkerItem2(mStationList.get(i).getStationNo());
        }
    }

    private void searchAddress(String searchWord) {
        mTmapData.findAllPOI(searchWord, new TMapData.FindAllPOIListenerCallback() {
            @Override
            public void onFindAllPOI(ArrayList<TMapPOIItem> poiItem) {
                ArrayList<TMapPOIItem> seoulPoiItem = new ArrayList<TMapPOIItem>();
                if (poiItem.size() > 0) {
                    for (int i = 0; i < poiItem.size(); i++) {
                        if (poiItem.get(i).getPOIAddress().startsWith("서울")) {
                            seoulPoiItem.add(poiItem.get(i));
                        }
                    }
                }
                mSearchResultAdapter.setArrayList(seoulPoiItem);
                runOnUiThread(new Runnable() {
                    public void run() {
                        mSearchResultAdapter.notifyDataSetChanged();
                        mSearchResult.setVisibility(View.VISIBLE);
                        mStationResult.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private ArrayList<StationData> searchBikeStation(TMapPoint tMapPoint) {
        ArrayList<StationData> resultStationList = new ArrayList<StationData>();
        for (int i = 0; i < mStationList.size(); i++) {
            StationData station = mStationList.get(i);

            TMapPolyLine tpolyline = new TMapPolyLine();
            tpolyline.addLinePoint(tMapPoint);
            tpolyline.addLinePoint(new TMapPoint(station.getCoorY(), station.getCoorX()));
            int distance = (int) tpolyline.getDistance();
            if (distance <= 1000) {
                station.setDistance(distance);
                resultStationList.add(station);
            }
        }

        Collections.sort(resultStationList, new Comparator<StationData>() {
            @Override
            public int compare(StationData t1, StationData t2) {
                return (t1.getDistance() > t2.getDistance()) ? 1 : -1;
            }
        });

        return resultStationList;
    }
    private void startPedestrialNevigation(){
        TMapData starData = new TMapData();
        TMapPoint startP = new TMapPoint(mStartPoint.getPOIPoint().getLatitude(), mStartPoint.getPOIPoint().getLongitude());
        TMapPoint startStationP = new TMapPoint(mStartStation.getCoorY(), mStartStation.getCoorX());
        Log.e("", "PJS hohoho startPedestrialNevigation()" + mStartPoint.getPOIPoint().getLongitude() + "  "  + mStartStation.getCoorX());
        starData.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, startP, startStationP, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine polyLine) {
                polyLine.setID("startLine");
                polyLine.setLineColor(Color.argb(70, 232, 12, 122));
                polyLine.setLineWidth(13);
                mMapView.addTMapPolyLine(polyLine.getID(), polyLine);
                Log.e("", "PJS hohoho startPedestrialNevigation()2" + mStartPoint.getPOIPoint().getLongitude());
            }
        });
        TMapData goalData = new TMapData();
        TMapPoint goalP = new TMapPoint(mEndPoint.getPOIPoint().getLatitude(), mEndPoint.getPOIPoint().getLongitude());
        TMapPoint endStationPoint = new TMapPoint(mFinishStation.getCoorY(), mFinishStation.getCoorX());
        goalData.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, endStationPoint, goalP, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine polyLine) {
                polyLine.setID("goalLine");
                polyLine.setLineColor(Color.argb(70, 232, 12, 122));
                polyLine.setLineWidth(13);
                mMapView.addTMapPolyLine(polyLine.getID(), polyLine);
            }
        });
    }
    private void startNavigation() {
        if (mStartStation == null) {
            makeMessageDialog(getResources().getString(R.string.no_start_station_title), getResources().getString(R.string.no_start_station_message));
        } else if (mFinishStation == null) {
            makeMessageDialog(getResources().getString(R.string.no_end_station_title), getResources().getString(R.string.no_end_station_message));
        } else if (mStartStation == mFinishStation) {
            makeMessageDialog(getResources().getString(R.string.equal_station_title), getResources().getString(R.string.equal_station_message));
        } else {
            mDashBoardView.setVisibility(View.VISIBLE);
            int startIndex = mStartStation.getName().indexOf(".");
            int endIndex = mFinishStation.getName().indexOf(".");
            String dash_start = mStartStation.getName().substring(startIndex + 1);
            String dash_end = mFinishStation.getName().substring(endIndex + 1);
            //((TextView) findViewById(R.id.dash_start_station_text)).setText(mStartStation.getName());
            ((TextView) findViewById(R.id.dash_start_station_text)).setText(dash_start);
            ((TextView) findViewById(R.id.dash_finish_station_text)).setText(dash_end);
            removeCustomMarker();
            TMapData tmapdata = new TMapData();
            TMapPoint startP = new TMapPoint(mStartStation.getCoorY(), mStartStation.getCoorX());
            TMapPoint endP = new TMapPoint(mFinishStation.getCoorY(), mFinishStation.getCoorX());
            tmapdata.findPathDataWithType(TMapData.TMapPathType.BICYCLE_PATH, startP, endP, new TMapData.FindPathDataListenerCallback() {
                @Override
                public void onFindPathData(TMapPolyLine polyLine) {
                    mNaviLine = polyLine;
                    mNaviLine.setID("naviLIne");
                    mNaviLine.setLineColor(Color.rgb(255, 83, 13));
                    mNaviLine.setLineWidth(13);
                    int height = BitmapFactory.decodeResource(getResources(), R.drawable.aaaa).getHeight();
                    int width = BitmapFactory.decodeResource(getResources(), R.drawable.aaaa).getWidth();
                    Bitmap s = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.aaaa), (int) (width * 0.15), (int) (height * 0.15), true);
                    Bitmap g = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bbbb), (int) (width * 0.15), (int) (height * 0.15), true);
                    mMapView.setTMapPathIcon(s, g);
                    mMapView.addTMapPath(polyLine);
                }
            });
            double latSpan = Math.abs(startP.getLatitude() - endP.getLatitude());
            double lonSpan = Math.abs(startP.getLongitude() - endP.getLongitude());
            mMapView.zoomToSpan(latSpan, lonSpan);

            double centerLat = Math.abs((startP.getLatitude() + endP.getLatitude()) / 2);
            double centerLon = Math.abs((startP.getLongitude() + endP.getLongitude()) / 2);
            mMapView.setCenterPoint(centerLon, centerLat);

            isNaviStartMode = true;
            mMyTrackingLine = new TMapPolyLine();
            mMyTrackingLine.setID("myLIne");
            mMyTrackingLine.setLineColor(Color.rgb(36, 179, 109));
            mMyTrackingLine.setLineWidth(12);
        }
    }

    private void startNavigationSearchMode() {
        if (mStartStation == null) {
            makeMessageDialog(getResources().getString(R.string.no_start_station_title), getResources().getString(R.string.no_start_station_message));
        } else if (mFinishStation == null) {
            makeMessageDialog(getResources().getString(R.string.no_end_station_title), getResources().getString(R.string.no_end_station_message));
        } else if (mStartStation == mFinishStation) {
            makeMessageDialog(getResources().getString(R.string.equal_station_title), getResources().getString(R.string.equal_station_message));
        } else {
            mDashBoardView.setVisibility(View.VISIBLE);
            int startIndex = mStartStation.getName().indexOf(".");
            int endIndex = mFinishStation.getName().indexOf(".");
            String dash_start = mStartStation.getName().substring(startIndex + 1);
            String dash_end = mFinishStation.getName().substring(endIndex + 1);
            //((TextView) findViewById(R.id.dash_start_station_text)).setText(mStartStation.getName());
            ((TextView) findViewById(R.id.dash_start_station_text)).setText(dash_start);
            ((TextView) findViewById(R.id.dash_finish_station_text)).setText(dash_end);
            removeCustomMarker();
            TMapData tmapdata = new TMapData();
            TMapPoint startP = new TMapPoint(mStartStation.getCoorY(), mStartStation.getCoorX());
            TMapPoint endP = new TMapPoint(mFinishStation.getCoorY(), mFinishStation.getCoorX());
            tmapdata.findPathDataWithType(TMapData.TMapPathType.BICYCLE_PATH, startP, endP, new TMapData.FindPathDataListenerCallback() {
                @Override
                public void onFindPathData(TMapPolyLine polyLine) {
                    mNaviLine = polyLine;
                    mNaviLine.setID("naviLIne");
                    mNaviLine.setLineColor(Color.rgb(255, 83, 13));
                    mNaviLine.setLineWidth(13);
                    int height = BitmapFactory.decodeResource(getResources(), R.drawable.aaa).getHeight();
                    int width = BitmapFactory.decodeResource(getResources(), R.drawable.aaa).getWidth();
                    Bitmap s = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.aaa), (int) (width * 0.15), (int) (height * 0.15), true);
                    Bitmap g = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bbb), (int) (width * 0.15), (int) (height * 0.15), true);
                    mMapView.setTMapPathIcon(s, g);
                    mMapView.addTMapPath(polyLine);
                }
            });
            double latSpan = Math.abs(startP.getLatitude() - endP.getLatitude());
            double lonSpan = Math.abs(startP.getLongitude() - endP.getLongitude());
            mMapView.zoomToSpan(latSpan, lonSpan);

            double centerLat = Math.abs((startP.getLatitude() + endP.getLatitude()) / 2);
            double centerLon = Math.abs((startP.getLongitude() + endP.getLongitude()) / 2);
            mMapView.setCenterPoint(centerLon, centerLat);

            isNaviStartMode = true;
            mMyTrackingLine = new TMapPolyLine();
            mMyTrackingLine.setID("myLIne");
            mMyTrackingLine.setLineColor(Color.rgb(36, 179, 109));
            mMyTrackingLine.setLineWidth(12);
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                // Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            updateTMap();
        }
    }

    private void updateTMap() {
        if (mCurrentLocation != null) {
            mMapView.setLocationPoint(mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude());
            mMapView.setCenterPoint(mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude());
        }
    }

    private void updateTMap(Location location) {
        if (location != null) {
            mMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
            mMapView.setCenterPoint(location.getLongitude(), location.getLatitude());
        }
    }

    private void updateToLastLocation() {
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateTMap();
        }

        // If the user presses the Start Updates button before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        PendingResult result = LocationServices.SettingsApi.checkLocationSettings(
                mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
//                final LocationSettingsStates = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location requests here.
                        updateToLastLocation();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Log.d(TAG, "onConnectionSuspended()");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mCurrentLocation == null) {
            updateTMap(location);
        }
        mCurrentLocation = location;

        if (mMaxSpeed < location.getSpeed()) {
            mMaxSpeed = location.getSpeed();
        }

        if (isNaviStartMode) {
            mMapView.setLocationPoint(mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude());

            TMapPoint point = new TMapPoint(location.getLatitude(), location.getLongitude());
            mMyTrackingLine.addLinePoint(point);
            mMapView.addTMapPolyLine(mMyTrackingLine.getID(), mMyTrackingLine);

            ((TextView) findViewById(R.id.current_speed_text)).setText(String.valueOf(location.getSpeed()) + " km/h");
            ((TextView) findViewById(R.id.max_speed_text)).setText(String.valueOf(mMaxSpeed) + " km/h");
        }
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //Log.e(TAG, "onConnectionFailed()");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        updateToLastLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    public void OnMarkerClick(String id) {
        StationData data = null;
        for (int i = 0; i < mStationList.size(); i++) {
            if (mStationList.get(i).getStationNo().equals(id)) {
                data = mStationList.get(i);
                break;
            }
        }
        if (mStationInfoLayer.getVisibility() == View.VISIBLE) {
            if (data != null) {
                if (mSelectedMarkerData.getStationNo().equals(data.getStationNo())) {
                    mStationInfoLayer.setVisibility(View.GONE);
                    mMapView.getMarkerItem2FromID(mSelectedMarkerData.getStationNo()).setIcon(mSelectedMarkerData.getImgMarker(this));
                } else {
                    mMapView.getMarkerItem2FromID(mSelectedMarkerData.getStationNo()).setIcon(mSelectedMarkerData.getImgMarker(this));
                    mSelectedMarkerData = data;
                    setSelectMarkerInfo(mSelectedMarkerData);
                    mMapView.getMarkerItem2FromID(mSelectedMarkerData.getStationNo()).setIcon(mSelectedMarkerData.getSelectImgMarker(this));
                }
            }
        } else {
            mStationInfoLayer.setVisibility(View.VISIBLE);
            mSelectedMarkerData = data;
            setSelectMarkerInfo(mSelectedMarkerData);
            mMapView.getMarkerItem2FromID(mSelectedMarkerData.getStationNo()).setIcon(mSelectedMarkerData.getSelectImgMarker(this));

        }
    }

    private void setSelectMarkerInfo(StationData data) {
        ((TextView) findViewById(R.id.tv_station_name)).setText(data.getName());
        ((TextView) findViewById(R.id.tv_station_address)).setText(data.getAddress());
        ((TextView) findViewById(R.id.tv_station_chargo)).setText(String.valueOf(data.getChargoSize()) + "대 거치가능");
    }

    private void changeMode(boolean searchMode) {
        if (searchMode == true) {
            setStartStation(mStartStation);
            setFinishStation(mFinishStation);

            mBtnMode.setChecked(true);

            mLayoutSearch.setVisibility(View.VISIBLE);
            mSearchResult.setVisibility(View.VISIBLE);
            mStationInfoLayer.setVisibility(View.GONE);

            LinearLayout searchButton = (LinearLayout) findViewById(R.id.layout_search_button);
            searchButton.setVisibility(View.VISIBLE);

            LinearLayout searchEdit = (LinearLayout) findViewById(R.id.layout_search);
            searchEdit.setVisibility(View.VISIBLE);
        } else {
            mBtnMode.setChecked(false);

            mLayoutSearch.setVisibility(View.GONE);
            mSearchResult.setVisibility(View.GONE);
            mStationResult.setVisibility(View.GONE);

            LinearLayout searchButton = (LinearLayout) findViewById(R.id.layout_search_button);
            searchButton.setVisibility(View.INVISIBLE);

            LinearLayout searchEdit = (LinearLayout) findViewById(R.id.layout_search);
            searchEdit.setVisibility(View.GONE);

            hideKeyBoard(mEditSearch);
        }
    }

    private void clearResult() {
        setStartStation(null);
        setFinishStation(null);

        mSearchResultAdapter.getArrayList().clear();
        mStationResultAdapter.getArrayList().clear();

        mEditSearch.setText("");
    }

    private void setStartStation(StationData station) {
        if (station != null) {
            mStartStation = station;
            mTvStart.setText(station.getName());
        } else {
            mStartStation = null;
            mTvStart.setText("");
        }
        mStationResultAdapter.getArrayList().clear();
        mStationResultAdapter.notifyDataSetChanged();
    }

    private void setFinishStation(StationData station) {
        if (station != null) {
            mFinishStation = station;
            mTvFinish.setText(station.getName());
        } else {
            mFinishStation = null;
            mTvFinish.setText("");
        }
        mStationResultAdapter.getArrayList().clear();
        mStationResultAdapter.notifyDataSetChanged();
    }

    private void hideKeyBoard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private boolean hasBikeSeoulApp() {
        boolean isExist = false;

        PackageManager pkgMgr = getPackageManager();
        List<ResolveInfo> mApps;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = pkgMgr.queryIntentActivities(mainIntent, 0);

        try {
            for (int i = 0; i < mApps.size(); i++) {
                if(mApps.get(i).activityInfo.packageName.startsWith("com.dki.spb_android")){
                    isExist = true;
                    break;
                }
            }
        }
        catch (Exception e) {
            isExist = false;
        }
        return isExist;
    }
}
