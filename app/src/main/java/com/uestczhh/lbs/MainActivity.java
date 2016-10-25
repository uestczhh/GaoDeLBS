package com.uestczhh.lbs;

import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements LocationSource {

    private MapView mapview;
    private AMap aMap;
    private Marker locationMarker;
    private UiSettings uiSettings;
    private AMapLocationClient locationClient;
    private AMapLocationClientOption locationClientOption;
    private OnLocationChangedListener changedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapview = (MapView) findViewById(R.id.mapview);
        mapview.onCreate(savedInstanceState);
        setMap();
        //定位精度设置
        locationClientOption = new AMapLocationClientOption();
        locationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //启动定位
        locationClient.startLocation();

    }

    private void setMap() {
        //获取amap实例
        aMap = mapview.getMap();
        aMap.setMyLocationEnabled(true);// 可触发定位并显示定位层
        aMap.setLocationSource(this);//设置了定位的监听服务
//        aMap.setOnCameraChangeListener(cameraChangeListener);//视图切换

        //控件的交互
        uiSettings = aMap.getUiSettings();
        uiSettings.setCompassEnabled(true);//罗盘
        uiSettings.setMyLocationButtonEnabled(true); // 显示默认的定位按钮

        //设置定位结果监听
        locationClient = new AMapLocationClient(getApplicationContext());
        locationClient.setLocationListener(aMapLocationListener);
    }

    /**
     * 定位返回结果
     */
    private AMapLocationListener aMapLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    Log.e("AMapLocation", aMapLocation.getCity());
                    double lat = aMapLocation.getLatitude();
                    double lon = aMapLocation.getLongitude();
                    LatLng latLng = new LatLng(lat, lon);

                    //位置描述
                    String desc = "";
                    Bundle locBundle = aMapLocation.getExtras();
                    if (locBundle != null) {
                        desc = locBundle.getString("desc");
                    }
                    //描绘定位点
                    addMarker(latLng, desc);
                    //显示定位框
                    locationMarker.showInfoWindow();
                    //焦点转移到定位处
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));//缩放范围3-19
                    //定位结束，停止定位服务
                    deactivate();
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError", "location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                }
            }
        }
    };

    /**
     * 描绘定位点
     *
     * @param latLng
     * @param desc
     */
    private void addMarker(LatLng latLng, String desc) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("当前位置");
        markerOptions.snippet(desc);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker());
        locationMarker = aMap.addMarker(markerOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapview.onPause();
        locationClient.stopLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapview.onDestroy();
        locationClient.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapview.onSaveInstanceState(outState);
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        changedListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        changedListener = null;
        //定位结束停止监听
        locationClient.unRegisterLocationListener(aMapLocationListener);
        aMapLocationListener = null;
        Log.e("deactivate", "deactivate");
    }
}
