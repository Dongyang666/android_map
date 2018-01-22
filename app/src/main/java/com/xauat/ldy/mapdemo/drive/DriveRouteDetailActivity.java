package com.xauat.ldy.mapdemo.drive;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.xauat.ldy.mapdemo.R;
import com.xauat.ldy.mapdemo.drive.DriveSegmentListAdapter;
import com.xauat.ldy.mapdemo.util.AMapUtil;


public class DriveRouteDetailActivity extends Activity {
	private DrivePath mDrivePath;
	private DriveRouteResult mDriveRouteResult;
	private TextView mTitle, mTitleDriveRoute, mDesDriveRoute;
	private ListView mDriveSegmentList;
	private DriveSegmentListAdapter mDriveSegmentListAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_detail);

		getIntentData();
		init();
	}

	private void init() {
		mTitle = (TextView) findViewById(R.id.title_center);
		mTitleDriveRoute = (TextView) findViewById(R.id.firstline);
		mDesDriveRoute = (TextView) findViewById(R.id.secondline);
		mTitle.setText("驾车路线详情");
		String dur = AMapUtil.getFriendlyTime((int) mDrivePath.getDuration()); //返回驾车规划方案的限行结果
		String dis = AMapUtil.getFriendlyLength((int) mDrivePath
				.getDistance());//返回此方案中的收费道路的总长度，单位米
		mTitleDriveRoute.setText(dur + "(" + dis + ")");
		int taxiCost = (int) mDriveRouteResult.getTaxiCost(); //返回从起点到终点打车的费用，单位元。
		mDesDriveRoute.setText("打车约"+taxiCost+"元");
		mDesDriveRoute.setVisibility(View.VISIBLE);
		configureListView();
	}

	private void configureListView() {
		mDriveSegmentList = (ListView) findViewById(R.id.bus_segment_list);
		mDriveSegmentListAdapter = new DriveSegmentListAdapter(
				this.getApplicationContext(), mDrivePath.getSteps());//驾车规划方案的路段列表
		mDriveSegmentList.setAdapter(mDriveSegmentListAdapter);
	}

	private void getIntentData() {
		Intent intent = getIntent();
		if (intent == null) {
			return;
		}
		mDrivePath = intent.getParcelableExtra("drive_path");
		mDriveRouteResult = intent.getParcelableExtra("drive_result");
	}

	public void onBackClick(View view) {
		this.finish();
	}
}
