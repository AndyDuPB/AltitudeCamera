package com.andy.altitudecamera.helper;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.view.Display;
import android.view.View;

import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.andy.altitudecamera.constant.CameraConstant;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@SuppressWarnings("deprecation")
public class CameraHelper
{
	public static String formatDate(Date date, SimpleDateFormat dateformat)
	{
		return formatDate(date.getTime(), dateformat);
	}

	public static String formatDate(long datetime, SimpleDateFormat dateformat)
	{
		dateformat.setTimeZone(TimeZone.getDefault());
		return dateformat.format(datetime);
	}

	public static String formatDate(Date date)
	{
		return formatDate(date.getTime());
	}

	public static String formatDate(Date date, boolean gmt)
	{
		return formatDate(date.getTime(), gmt);
	}

	public static String formatDate(long date)
	{
		return formatDate(date, false);
	}

	public static String formatDate(long date, boolean gmt)
	{
		if (gmt)
		{
			CameraConstant.SIMPLE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone(CameraConstant.GMT_TIME_ZONE));
		}
		else
		{
			CameraConstant.SIMPLE_DATE_FORMAT.setTimeZone(TimeZone.getDefault());
		}

		return CameraConstant.SIMPLE_DATE_FORMAT.format(date);
	}

	public static String getAltitudeCameraBaseDir()
	{
		File directory;

		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{
			directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		}
		else
		{
			directory = Environment.getDataDirectory();
		}
		return directory.getAbsolutePath();
	}

	public static Bitmap zoomBitmap(Bitmap bitmap, int newWidth, int newHeight)
	{
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);

		return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
	}

	public static Size getMostSuitableSize(List<Size> sizeList, int screenWidth)
	{

		if (sizeList != null && sizeList.size() > 0)
		{
			int temp = 0;
			int[] arry = new int[sizeList.size()];
			for (Size size : sizeList)
			{
				arry[temp++] = Math.abs(size.width - screenWidth);
			}

			temp = 0;
			int index = 0;
			for (int i = 0; i < arry.length; i++)
			{
				if (i == 0)
				{
					temp = arry[i];
					index = 0;
				}
				else
				{
					if (arry[i] < temp)
					{
						index = i;
						temp = arry[i];
					}
				}
			}

			return sizeList.get(index);
		}

		return null;
	}

	public static Bitmap createFullScreenBitmap(Activity activity)
	{

		View view = activity.getWindow().getDecorView();
		view.buildDrawingCache();

		Rect rect = new Rect();
		view.getWindowVisibleDisplayFrame(rect);
		int statusBarHeights = rect.top;

		Display display = activity.getWindowManager().getDefaultDisplay();
		int widths = display.getWidth();
		int heights = display.getHeight();

		view.setDrawingCacheEnabled(true);

		Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache(), 0, statusBarHeights, widths, heights
				- statusBarHeights);

		view.destroyDrawingCache();

		return bitmap;
	}

	public static Bitmap createVerticalHalfScreenBitmap(Activity activity)
	{

		View view = activity.getWindow().getDecorView();
		view.buildDrawingCache();

		Rect rect = new Rect();
		view.getWindowVisibleDisplayFrame(rect);
		int statusBarHeights = rect.top;

		Display display = activity.getWindowManager().getDefaultDisplay();
		int widths = display.getWidth();
		int heights = display.getHeight();

		view.setDrawingCacheEnabled(true);

		Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache(), 0, statusBarHeights, widths,
				(heights - statusBarHeights) / 2);

		view.destroyDrawingCache();

		return bitmap;
	}

	public static Bitmap createHorizontalHalfScreenBitmap(Activity activity)
	{
		View view = activity.getWindow().getDecorView().getRootView();
		view.destroyDrawingCache();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();

		Display display = activity.getWindowManager().getDefaultDisplay();
		int widths = view.getWidth();
		int heights = view.getHeight();

		Rect rect = new Rect();
		view.getWindowVisibleDisplayFrame(rect);
		int statusBarHeights = rect.top;

		Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache(), 0, statusBarHeights, widths / 2,
				(heights - statusBarHeights));

		view.destroyDrawingCache();

		return bitmap;
	}

	public static boolean savePicture(Bitmap bitmap, File file)
	{
		FileOutputStream stream = null;
		try
		{
			boolean existed = false;
			if (!file.exists())
			{
				existed = file.createNewFile();
			}
			else
			{
				existed = true;
			}

			if (existed)
			{
				stream = new FileOutputStream(file);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

				stream.flush();
				stream.close();

				return true;
			}
		}
		catch (Exception e)
		{
		}

		return false;
	}

	public static Bitmap rotate(Bitmap bitmap, int degree)
	{
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		Matrix mtx = new Matrix();
		mtx.postRotate(degree);

		return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
	}

	public static Bitmap addLR2Bitmap(Bitmap first, Bitmap second)
	{
		return addLR2Bitmap(first, second, true);
	}

	public static Bitmap addLR2Bitmap(Bitmap first, Bitmap second, boolean needBorder)
	{
		int width = first.getWidth() + second.getWidth();
		int height = Math.max(first.getHeight(), second.getHeight());

		Bitmap result = Bitmap.createBitmap(width, height, Config.ARGB_8888);

		Canvas canvas = new Canvas(result);
		canvas.drawBitmap(first, 0, 0, null);
		canvas.drawBitmap(second, first.getWidth(), 0, null);

		if (needBorder)
		{
			setBitmapBorder(canvas, width, height);
		}

		first.recycle();
		second.recycle();
		
		return result;
	}

	private static void setBitmapBorder(Canvas canvas, int width, int height)
	{

		Rect rect = canvas.getClipBounds();
		Paint paint = new Paint();

		paint.setColor(Color.rgb(136, 136, 136));
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setStrokeWidth(18);

		// 画外围边框
		// 上
		canvas.drawLine(rect.left, rect.top, rect.left + width, rect.top, paint);
		// 左
		canvas.drawLine(rect.left, rect.top, rect.left, rect.top + height, paint);
		// 右
		canvas.drawLine(rect.left + width, rect.top, rect.right, rect.bottom, paint);
		// 下
		canvas.drawLine(rect.left, rect.top + height, rect.right, rect.bottom, paint);

		paint.setStrokeWidth(9);

		// 画中间水平向右二分之一的直线
		canvas.drawLine(rect.left, rect.top + (height / 2), rect.left + (width / 3), rect.top + (height / 2), paint);
		// 画中间垂直向下的直线
		canvas.drawLine(rect.left + (width / 3), rect.top, rect.left + (width / 3), rect.top + height, paint);
	}

	public static Bitmap addTB2Bitmap(Bitmap first, Bitmap second)
	{
		int width = Math.max(first.getWidth(), second.getWidth());
		int height = first.getHeight() + second.getHeight();

		Bitmap result = Bitmap.createBitmap(width, height, Config.ARGB_8888);

		Canvas canvas = new Canvas(result);
		canvas.drawBitmap(first, 0, 0, null);
		canvas.drawBitmap(second, 0, first.getHeight(), null);

		return result;
	}

	public static MyLocationStyle createMyLocationStyle()
	{
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		myLocationStyle.interval(2000);
		myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);
		myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
		myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));

		return myLocationStyle;
	}

	public static AMapLocationClientOption createAMapLocationClientOption()
	{
		AMapLocationClientOption mLocationOption = new AMapLocationClientOption();

		mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
		mLocationOption.setLocationMode(AMapLocationMode.Battery_Saving);
		mLocationOption.setInterval(1000);
		mLocationOption.setNeedAddress(true);
		mLocationOption.setGpsFirst(false);
		mLocationOption.setHttpTimeOut(20000);
		mLocationOption.setLocationCacheEnable(false);

		return mLocationOption;
	}

}