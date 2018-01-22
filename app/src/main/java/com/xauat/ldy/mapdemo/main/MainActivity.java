package com.xauat.ldy.mapdemo.main;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;

import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.xauat.ldy.mapdemo.R;
import com.xauat.ldy.mapdemo.bus.BusResultListAdapter;
import com.xauat.ldy.mapdemo.drive.DriveRouteDetailActivity;
import com.xauat.ldy.mapdemo.util.AMapUtil;
import com.xauat.ldy.mapdemo.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import com.xauat.ldy.mapdemo.overlay.*;
import com.xauat.ldy.mapdemo.walk.WalkRouteDetailActivity;

import static android.R.attr.mode;
import static com.amap.api.maps.AMap.MAP_TYPE_NORMAL;


public class MainActivity extends AppCompatActivity implements  View.OnClickListener,PoiSearch.OnPoiSearchListener, TextWatcher,  WeatherSearch.OnWeatherSearchListener, RouteSearch.OnRouteSearchListener {

    private RouteSearch mRouteSearch;
    private DriveRouteResult mDriveRouteResult;
    DrivePath drivePath;
    private final int ROUTE_TYPE_BUS = 1; //公交
    private final int ROUTE_TYPE_DRIVE = 2; //驾车
    private final int ROUTE_TYPE_WALK = 3; //步行
    private WalkRouteResult mWalkRouteResult;
    private WalkRouteOverlay walkRouteOverlay;

    private RelativeLayout mBottomLayout;
    LinearLayout  mdelectlayout;
    private TextView mRotueTimeDes, mRouteDetailDes;
    private BusRouteResult mBusRouteResult;
    private LinearLayout mBusResultLayout;
    private View inflate;
    private Button btn_dirve;
    private Button btn_bus;
    private Button btn_walk;
    private Button cancel;
    Button delect;
    ImageView bus_result_back;
    private Dialog dialog;
    Context thisContext;
    private AMap aMap;//AMap是地图对象
    private MapView mapView;//地图对象
    TextView wendu,fengli,detil;
    InputTipTask inputtiptask;
    List<Tip> list=new ArrayList<Tip>();
    MyLocation myLocation=null;
    private UiSettings settings;//定义一个UiSettings对象
    private ImageButton button1;
    private ProgressDialog progDialog = null;// 搜索时进度条
    private String keyWord;
    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;//搜索
    private PoiSearch.SearchBound searchBound;
    private int currentPage;// 当前页面，从0开始计数
    private PoiResult poiResults; // poi返回的结果
    private AutoCompleteTextView autotext;
    private ListView mBusResultList;
    private LatLonPoint latLonPoint;
    LatLonPoint mStartPoint, mEndPoint;
    LatLng to1;
    private int juli = 1000;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
            mdelectlayout=(LinearLayout)findViewById(R.id.mdelect_loyout);
            mRotueTimeDes = (TextView) findViewById(R.id.firstline);
            mRouteDetailDes = (TextView) findViewById(R.id.secondline);
            wendu=(TextView)findViewById(R.id.textView1);
            fengli=(TextView)findViewById(R.id.textView);
            detil=(TextView)findViewById(R.id.detil);
            button1=(ImageButton)findViewById(R.id.search_button);
            delect=(Button)findViewById(R.id.btn_delect);
            mBusResultLayout = (LinearLayout) findViewById(R.id.bus_result);
            bus_result_back=(ImageView) findViewById(R.id.bus_result_back);
            bus_result_back.setOnClickListener(this);
            delect.setOnClickListener(this);
            mapView = (MapView) findViewById(R.id.map);
            autotext = (AutoCompleteTextView) findViewById(R.id.search_edit);//获取到textview
            autotext=(AutoCompleteTextView) findViewById(R.id.search_edit);
            thisContext=this;

            autotext.setThreshold(1);//设置触发自动提醒的

            autotext.addTextChangedListener(this);//添加监听
            //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，实现地图生命周期管理
            mapView.onCreate(savedInstanceState);
            button1.setOnClickListener(this);
            if (aMap == null) {
                aMap = mapView.getMap();
                //设置显示定位按钮 并且可以点击
                settings = aMap.getUiSettings();
                aMap.showIndoorMap(true);
                myLocation=new MyLocation(getApplicationContext(),aMap);
                settings.setZoomControlsEnabled(false);//显示缩放按钮
                settings.setCompassEnabled(true); //显示指南针
                aMap.setLocationSource(myLocation);//设置了定位的监听,这里要实现LocationSource接口
                settings.setScaleControlsEnabled(true);//控制比例尺控件是否显示
                settings.setMyLocationButtonEnabled(true);// 是否显示定位按钮
                aMap.setMyLocationEnabled(true);//显示定位层并且可以触发定位,默认是flase
                settings.setAllGesturesEnabled (true);//设置所有手势例如，缩放，滑动，旋转等。
            }
            autotext.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String serch = (String) parent.getItemAtPosition(position);
                    autotext.setText(serch);
                    search();
                }
            });

            // 定义 Marker 点击事件监听
            AMap.OnMarkerClickListener markerClickListener = new AMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    //获取marker点的的经纬度值
                    to1= marker.getPosition();
                    mEndPoint=new LatLonPoint(to1.latitude,to1.longitude);
                    //显示路线规划弹框
                    dialog = new Dialog(thisContext,R.style.ActionSheetDialogStyle);
                    inflate = LayoutInflater.from(thisContext).inflate(R.layout.pop_item, null);
                    btn_dirve = (Button) inflate.findViewById(R.id.dirve_item);
                    btn_walk = (Button) inflate.findViewById(R.id.walk_item);
                    btn_bus= (Button) inflate.findViewById(R.id.bus_item);
                    cancel = (Button) inflate.findViewById(R.id.btn_cancel);
                    btn_dirve.setOnClickListener(MainActivity.this);
                    btn_walk.setOnClickListener(MainActivity.this);
                    btn_bus.setOnClickListener(MainActivity.this);
                    cancel.setOnClickListener( new View.OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    dialog.setContentView(inflate);
                    Window dialogWindow = dialog.getWindow();
                    dialogWindow.setGravity( Gravity.BOTTOM);
                    WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                    lp.x = 0; // 新位置X坐标
                    lp.y = -20; // 新位置Y坐标
                    lp.width = (int) getResources().getDisplayMetrics().widthPixels; // 宽度
                    dialogWindow.setAttributes(lp);
                    dialog.show();
                    return false;
                }
            };
            // 绑定 Marker 被点击事件
            aMap.setOnMarkerClickListener(markerClickListener);

            //开始定位
           myLocation.location();


            //天气开始查询
            WeatherSearchQuery mquery = new WeatherSearchQuery("西安市",WeatherSearchQuery.WEATHER_TYPE_LIVE);
            WeatherSearch mweathersearch=new WeatherSearch(this);
            mweathersearch.setOnWeatherSearchListener(this);
            mweathersearch.setQuery(mquery);
            mweathersearch.searchWeatherAsyn(); //异步搜索

            mBusResultList = (ListView) findViewById(R.id.bus_result_list);
        }


    @Override
        protected void onDestroy() {
            super.onDestroy();
            //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
            mapView.onDestroy();
        }
        @Override
        protected void onResume() {
            super.onResume();
            //在activity执行onResume时执行mMapView.onResume ()，实现地图生命周期管理
            mapView.onResume();
        }
        @Override
        protected void onPause() {
            super.onPause();
            //在activity执行onPause时执行mMapView.onPause ()，实现地图生命周期管理
            mapView.onPause();

        }
        @Override
        protected void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，实现地图生命周期管理
            mapView.onSaveInstanceState(outState);
        }



    //实现地图模式切换
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //新建的xml文件
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        //根据不同的id点击不同按钮控制activity需要做的事件
        switch (item.getItemId())
        {
            case R.id.dh:
                aMap.setMapType(AMap.MAP_TYPE_NAVI);
                break;
            case R.id.yj:
                //夜景地图显示
                aMap.setMapType(AMap.MAP_TYPE_NIGHT);
                break;
            case R.id. pt:
                //事件
                aMap.setMapType(MAP_TYPE_NORMAL);
                break;
            case R.id. wx:
                //卫星地图
                aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
                break;
        }
        return true;
    }


    long firstTime = 0;
    //双击退出
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK){
            long secondTime = System.currentTimeMillis();
            if (secondTime-firstTime>2000){
                Toast.makeText(this,"再按一次退出",Toast.LENGTH_SHORT).show();
                firstTime = System.currentTimeMillis();
                return true;
            }else {
                finish();
            }
        }
        return super.onKeyUp(keyCode, event);
    }
    //兼容性考虑
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
            Log.d("=========", "横屏");
        }else {
            Log.d("=========", "竖屏");
        }
    }


    /**
     * 点击搜索按钮
     */
    private void search(){
        keyWord = autotext.getText().toString();
        Log.i("---", keyWord);
        if ("".equals(keyWord)){
            Toast.makeText(MainActivity.this, "请输入搜索关键字",Toast.LENGTH_SHORT).show();
            return;
        }else {
            doSearchQuery();
        }
    }
    /**
     * 搜索操作
     */
    private void doSearchQuery() {
        showProgressDialog();
        currentPage = 0;
        //第一个参数表示搜索字符串，第索类二个参数表示poi搜型，第三个参数表示poi搜索区域（空字符串代表全国）
        query = new PoiSearch.Query(keyWord,"","");
        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页
        poiSearch = new PoiSearch(this,query);
        poiSearch.setOnPoiSearchListener(this);//设置回调数据的监听器
        //点附近2000米内的搜索结果
        if (latLonPoint!=null){
            searchBound = new PoiSearch.SearchBound(latLonPoint,juli);
            poiSearch.setBound(searchBound);
        }
        poiSearch.searchPOIAsyn();//开始搜索
    }
    /**
     * 显示进度框
     */
    private void showProgressDialog() {

        if (progDialog == null)
            progDialog = new ProgressDialog(thisContext);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索...");
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.search_button:
                if (!autotext.getText().toString().equals("")) {
                    Log.i("---", "搜索操作");
                    search();
                }
                break;
            case R.id.dirve_item:
                dialog.dismiss();
                mRouteSearch = new RouteSearch(thisContext);
                mRouteSearch.setRouteSearchListener(this);
                searchRouteResult(ROUTE_TYPE_DRIVE);  //传入类型为驾车
                mapView.setVisibility(View.VISIBLE);
                break;
            case R.id.walk_item:
                dialog.dismiss();
                mRouteSearch = new RouteSearch(thisContext);
                mRouteSearch.setRouteSearchListener(this);
                searchRouteResult(ROUTE_TYPE_WALK); //传入类型为步行
                mapView.setVisibility(View.VISIBLE);
                break;
            case R.id.bus_item:
                dialog.dismiss();
                mRouteSearch = new RouteSearch(thisContext);
                mRouteSearch.setRouteSearchListener(this);
                searchRouteResult(ROUTE_TYPE_BUS);
                mBusResultLayout.setVisibility(View.VISIBLE);
                mapView.setVisibility(View.GONE);
               break;
            case R.id.btn_cancel:
                dialog.dismiss();
                break;
            case R.id.btn_delect:
                aMap.clear();
                mBottomLayout.setVisibility(View.GONE);
                mdelectlayout.setVisibility(View.GONE);
                break;
            case R.id.bus_result_back:
                aMap.getMapScreenMarkers().clear();
                mBusResultLayout.setVisibility(View.GONE);
                mapView.setVisibility(View.VISIBLE);

        }
    }
    /**
     * poi没有搜索到数据，返回一些推荐城市的信息
     */
    private void showSuggestCity(List<SuggestionCity> cities) {
        String infomation = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
        Toast.makeText(MainActivity.this, infomation,Toast.LENGTH_SHORT).show();

    }


    /**
     * POI信息查询回调方法
     */
    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        dissmissProgressDialog();// 隐藏对话框
        if (i == 1000) {
            Log.i("---","查询结果:"+i);
            if (poiResult != null && poiResult.getQuery() != null) {// 搜索poi的结果
                if (poiResult.getQuery().equals(query)) {// 是否是同一条
                    poiResults = poiResult;
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
                    if (poiItems != null && poiItems.size() > 0) {
                       // aMap.clear();
                        aMap.getMapScreenMarkers().clear();// 清理之前的图标
                         PoiOverlay poiOverlay = new PoiOverlay(aMap, poiItems);//根据给定的参数来构造一个PoiOverlay的新对象。通过此构造函数创建Poi图层。
                        poiOverlay.removeFromMap();//将PoiOverlay从地图中移除。去掉PoiOverlay上所有的Marker。
                        poiOverlay.addToMap();//将PoiOverlay加入到地图中。添加Marker到地图中。
                        poiOverlay.zoomToSpan();//移动镜头到当前的视角
                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        showSuggestCity(suggestionCities);
                    } else {
                        Toast.makeText(MainActivity.this, "未找到结果",Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(MainActivity.this, "该距离内没有找到结果",Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.i("---","查询结果:"+i);
            Toast.makeText(MainActivity.this, "异常代码---"+i,Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }





    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if (s.length() <= 0) {
            return;
        }else {
            //高德地图的输入的自动提示，代码在后面
            inputtiptask= InputTipTask.getInstance(this);
            inputtiptask.setAdapter(autotext).searchTips(s.toString().trim(), "");
        }


    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

//天气的回掉函数
    @Override
    public void onWeatherLiveSearched(LocalWeatherLiveResult weatherLiveResult, int i) {

        if (i== 1000) {
            if (weatherLiveResult != null & weatherLiveResult.getLiveResult() != null) {
                LocalWeatherLive weatherlive = weatherLiveResult.getLiveResult();
                wendu.setText("  西安温度:"+weatherlive.getTemperature() + "°");
                fengli.setText("  "+weatherlive.getWindDirection()+"风 "+weatherlive.getWindPower()+"级");
            }
        }
    }

    @Override
    public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {

    }


    /**
     * 开始搜索路径规划方案
     */
    private void searchRouteResult(int routeType) {
        mStartPoint = myLocation.getFrom();
        if (mStartPoint == null) {
            ToastUtil.show(thisContext, "起点未设置");
            return;
        }
        if (mEndPoint == null) {
            ToastUtil.show(thisContext, "终点未设置");
        }
        showProgressDialog();
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                mStartPoint, mEndPoint);
        if (routeType == ROUTE_TYPE_BUS) {// 公交路径规划
                RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(fromAndTo, RouteSearch.BusLeaseWalk,
                    "029", 0);// 第一个参数表示路径规划的起点和终点，第二个参数表示公交查询模式，第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算
            mRouteSearch.calculateBusRouteAsyn(query);// 异步路径规划公交模式查询
        }
        else if (routeType == ROUTE_TYPE_DRIVE) {// 驾车路径规划
            RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, mode, null,
                    null, "");// 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
            mRouteSearch.calculateDriveRouteAsyn(query);// 异步路径规划驾车模式查询
        }
        else if (routeType == ROUTE_TYPE_WALK) {// 步行路径规划
            RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo);
            mRouteSearch.calculateWalkRouteAsyn(query);// 异步路径规划步行模式查询
        }
    }








//公交方式的回调函数
    @Override
    public void onBusRouteSearched(BusRouteResult result, int errorCode) {
        Toast.makeText(thisContext,"fdf",Toast.LENGTH_SHORT);
        dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == 1000) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mBusRouteResult = result;
                    BusResultListAdapter mBusResultListAdapter = new BusResultListAdapter(this.getApplicationContext(), mBusRouteResult);
                    mBusResultList.setAdapter(mBusResultListAdapter);
                } else if (result != null && result.getPaths() == null) {
                    ToastUtil.show(thisContext, R.string.no_result);
                }
            } else {
                ToastUtil.show(thisContext, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }

    }
//驾车方式的回调函数
    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int errorCode) {
        dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == 1000) {
            if (driveRouteResult != null && driveRouteResult.getPaths() != null) {
                if (driveRouteResult.getPaths().size() > 0) {
                    mDriveRouteResult = driveRouteResult;
                    drivePath = mDriveRouteResult.getPaths()
                            .get(0);
                    DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
                            thisContext, aMap, drivePath,
                            mDriveRouteResult.getStartPos(),
                            mDriveRouteResult.getTargetPos(), null);
                    drivingRouteOverlay.setNodeIconVisibility(false);//设置节点marker是否显示
                    drivingRouteOverlay.setIsColorfulline(true);//是否用颜色展示交通拥堵情况，默认true
                    drivingRouteOverlay.removeFromMap();
                    drivingRouteOverlay.addToMap();
                    drivingRouteOverlay.zoomToSpan();
                    int dis = (int) drivePath.getDistance();
                    int dur = (int) drivePath.getDuration();
                    String des = AMapUtil.getFriendlyTime(dur)+"("+ AMapUtil.getFriendlyLength(dis)+")";
                    mRotueTimeDes.setText(des);
                    mBottomLayout.setVisibility(View.VISIBLE);
                    mRouteDetailDes.setVisibility(View.VISIBLE);
                    mdelectlayout.setVisibility(View.VISIBLE);
                    int taxiCost = (int) mDriveRouteResult.getTaxiCost();
                    mRouteDetailDes.setText("打车约"+taxiCost+"元");
                    detil.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getApplicationContext(),
                                    DriveRouteDetailActivity.class);
                            intent.putExtra("drive_path", drivePath);
                            intent.putExtra("drive_result",
                                    mDriveRouteResult);
                            startActivity(intent);
                        }
                    });
                } else if (driveRouteResult != null && driveRouteResult.getPaths() == null) {
                    ToastUtil.show(thisContext, R.string.no_result);
                }

            } else {
                ToastUtil.show(thisContext, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }

    }


//步行方式的回调函数
    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
        dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mWalkRouteResult = result;
                    final WalkPath walkPath = mWalkRouteResult.getPaths()
                            .get(0);
                    if (walkRouteOverlay != null){
                        walkRouteOverlay.removeFromMap();
                    }
                    walkRouteOverlay = new WalkRouteOverlay(
                            this, aMap, walkPath,
                            mWalkRouteResult.getStartPos(),
                            mWalkRouteResult.getTargetPos());
                    walkRouteOverlay.addToMap();
                    walkRouteOverlay.zoomToSpan();
                    int dis = (int) walkPath.getDistance();
                    int dur = (int) walkPath.getDuration();
                    String des = AMapUtil.getFriendlyTime(dur)+"("+AMapUtil.getFriendlyLength(dis)+")";
                    mRotueTimeDes.setText(des);
                    mBottomLayout.setVisibility(View.VISIBLE);
                    mRouteDetailDes.setVisibility(View.VISIBLE);
                    mdelectlayout.setVisibility(View.VISIBLE);
                    detil.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getApplicationContext(),
                                    WalkRouteDetailActivity.class);
                            intent.putExtra("walk_path", walkPath);
                            intent.putExtra("walk_result",
                                    mWalkRouteResult);
                            startActivity(intent);
                        }
                    });
                } else if (result != null && result.getPaths() == null) {
                    ToastUtil.show(thisContext, R.string.no_result);
                }
            } else {
                ToastUtil.show(thisContext, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }

    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }

}


