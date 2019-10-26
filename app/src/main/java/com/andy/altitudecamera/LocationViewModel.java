package com.andy.altitudecamera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.andy.altitudecamera.constant.CameraConstant;
import com.andy.altitudecamera.helper.CameraHelper;

import java.util.Date;

public class LocationViewModel implements AMapLocationListener
{

	private TextView valueAltitude;

	private TextView valueLongitude;

	private TextView valueLatitude;

	private TextView valuePlace;

	private TextView valueCity;

	private TextView valueCountry;

	private TextView valueProvince;

	private TextView valueDateTime;

	private TextView valueGPS;

	private Activity activity;

	private MapView mMapView;

	private AMap aMap;

	private LocationManager locationManager = null;

	private ALtitudeCameraLocationListener gpsLocationlistener = new ALtitudeCameraLocationListener();

	// 创建Handler对象，用于主线程更新界面
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{

			super.handleMessage(msg);

			Parcelable parcelable = msg.getData().getParcelable(CameraConstant.LOCATION);
			if (parcelable instanceof AMapLocation)
			{
				LocationViewModel.this.updateLocationView((AMapLocation) parcelable);
			}
			else
			{
				LocationViewModel.this.updateAltitudeView((Location) parcelable);
			}
		}
	};

	class ALtitudeCameraLocationListener implements LocationListener
	{

		boolean validation = false;

		public ALtitudeCameraLocationListener()
		{
		}

		@Override
		public void onLocationChanged(Location location)
		{
			if (location.getLatitude() == 0.0 && location.getLongitude() == 0.0)
			{
				return;
			}

			if (!this.validation)
			{
				this.validation = true;
				Log.d(CameraConstant.TAG, "Got First location.");
			}

			if (this.validation)
			{
				Log.d(CameraConstant.TAG, "The New Location is Longitude : " + location.getLongitude()
						+ " , Latitude : " + location.getLatitude());
			}

			Bundle data = new Bundle();
			data.putParcelable(CameraConstant.LOCATION, location);

			Message message = new Message();
			message.setData(data);

			LocationViewModel.this.handler.sendMessage(message);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
			switch (status)
			{
			case LocationProvider.OUT_OF_SERVICE:
				this.validation = false;
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				this.validation = false;
				break;
			case LocationProvider.AVAILABLE:
				this.validation = true;
				break;
			}
		}

		@Override
		public void onProviderEnabled(String provider)
		{
			Log.d(CameraConstant.TAG, "Provider support : " + provider);
			this.validation = true;
		}

		@Override
		public void onProviderDisabled(String provider)
		{
			Log.d(CameraConstant.TAG, "Provider not support : " + provider);
			this.validation = false;
		}

	}

	public LocationViewModel(Activity activity)
	{
		this.activity = activity;
	}

	public boolean isGPSProviderEnable()
	{
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	@SuppressLint("MissingPermission")
	public void initialize(Bundle savedInstanceState)
	{
		valueAltitude = (TextView) this.activity.findViewById(R.id.valueAltitude);
		valueLongitude = (TextView) this.activity.findViewById(R.id.valueLongitude);
		valueLatitude = (TextView) this.activity.findViewById(R.id.valueLatitude);
		valueCountry = (TextView) this.activity.findViewById(R.id.valueCountry);
		valueProvince = (TextView) this.activity.findViewById(R.id.valueProvince);
		valueCity = (TextView) this.activity.findViewById(R.id.valueCity);
		valuePlace = (TextView) this.activity.findViewById(R.id.valuePlace);
		valueDateTime = (TextView) this.activity.findViewById(R.id.valueDateTime);
		valueGPS = (TextView) this.activity.findViewById(R.id.valueGPS);

		locationManager = (LocationManager) this.activity.getSystemService(Context.LOCATION_SERVICE);
		if (!isGPSProviderEnable())
		{
			Toast.makeText(this.activity, this.activity.getString(R.string.message_open_location_service),
					Toast.LENGTH_SHORT).show();
			Intent locationSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			locationSettingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.activity.startActivity(locationSettingIntent);
		}
		else
		{
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, gpsLocationlistener);
		}

		// 获取地图控件引用
		mMapView = (MapView) this.activity.findViewById(R.id.map);
		// 在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
		mMapView.onCreate(savedInstanceState);
		// 初始化地图控制器对象
		if (aMap == null)
		{
			aMap = mMapView.getMap();
		}

		aMap.setMapLanguage(AMap.CHINESE);
		aMap.setMyLocationEnabled(true);
		aMap.setMyLocationStyle(CameraHelper.createMyLocationStyle());
		aMap.moveCamera(CameraUpdateFactory.zoomTo(14));

		AMapLocationClient mLocationClient = new AMapLocationClient(this.activity);
		mLocationClient.setLocationListener(LocationViewModel.this);
		mLocationClient.setLocationOption(CameraHelper.createAMapLocationClientOption());
		mLocationClient.startLocation();

	}

	public void onDestroy()
	{
		mMapView.onDestroy();
		locationManager.removeUpdates(gpsLocationlistener);
	}

	public void onResume()
	{
		mMapView.onResume();
	}

	public void onPause()
	{
		mMapView.onPause();
	}

	public void onSaveInstanceState(Bundle outState)
	{
		mMapView.onSaveInstanceState(outState);
	}

	@Override
	public void onLocationChanged(AMapLocation location)
	{
		Bundle data = new Bundle();
		data.putParcelable(CameraConstant.LOCATION, location);

		Message message = new Message();
		message.setData(data);

		this.handler.sendMessage(message);
	}

	protected void updateAltitudeView(Location location)
	{
		if (location != null)
		{
			valueAltitude.setText(String.valueOf(location.getAltitude()));
		}
	}

	public void updateLocationView(AMapLocation location)
	{
		if (location != null)
		{
			if (location.getErrorCode() != 0)
			{
				return;
			}

			if (location.getLongitude() > 0)
			{
				valueLongitude.setText(String.valueOf(location.getLongitude()).concat("°E"));
			}
			else if (location.getLongitude() < 0)
			{
				valueLongitude.setText(String.valueOf(Math.abs(location.getLongitude())).concat("°W"));
			}
			else
			{
				valueLongitude.setText("0");
			}

			if (location.getLatitude() > 0)
			{
				valueLatitude.setText(String.valueOf(location.getLatitude()).concat("°N"));
			}
			else if (location.getLatitude() < 0)
			{
				valueLatitude.setText(String.valueOf(Math.abs(location.getLatitude())).concat("°S"));
			}
			else
			{
				valueLatitude.setText("0");
			}

			valueCountry.setText(location.getCountry());
			valueProvince.setText(location.getProvince());
			valueCity.setText(location.getCity());
			valuePlace.setText(location.getAddress());

			valueDateTime.setText(CameraHelper.formatDate(new Date()));
			valueGPS.setText(CameraHelper.formatDate(location.getTime(), true).concat(" +0000"));
		}
	}

}
