package com.andy.altitudecamera.constant;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class CameraConstant
{
	public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
			Locale.CHINESE);

	public static final SimpleDateFormat SIMPLE_DATE_FORMAT02 = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINESE);

	public static final String FILE_NAME_FORMAT = "AltitudeCamera_{0}.JPG";
	
	public static String TAG = "AltitudeCamera";

	public static String BITMAP = "BITMAP";

	public static String IMAGE_PATH = "IMAGE_PATH";

	public static String LOCATION = "LOCATION";

	public static String VISIBLE = "VISIBLE";

	public static String JPG = ".JPG";

	public static int FLASH_MODE_AUTO = 0;

	public static int FLASH_MODE_OFF = -1;

	public static int FLASH_MODE_ON = 1;

	public static String GMT_TIME_ZONE = "GMT";

	public static String SAVE_FAILED = "SAVE_FAILED";

}
