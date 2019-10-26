package com.andy.altitudecamera.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.andy.altitudecamera.LocationViewModel;
import com.andy.altitudecamera.R;
import com.andy.altitudecamera.constant.CameraConstant;
import com.andy.altitudecamera.helper.CameraHelper;
import com.andy.altitudecamera.helper.PermissionHelper;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener, Camera.PictureCallback, Camera.AutoFocusCallback
{

    private int cameraPosition;

    private String flashMode = Camera.Parameters.FLASH_MODE_AUTO;

    private int flashModeType = CameraConstant.FLASH_MODE_AUTO;

    private Camera camera = null;

    private SurfaceView surfaceView;

    private View operationView;

    private View locationFullView;

    private LocationViewModel locationViewModel;

    private PermissionHelper permissionHelper;

    private Camera.Size suitableSizeBack;

    private Camera.Size suitableSizeFront;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);

        final Bundle instanceState = savedInstanceState;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 拍照过程屏幕一直处于高亮

        setContentView(R.layout.activity_camera);

        // 当系统为6.0以上时，需要申请权限
        permissionHelper = new PermissionHelper(this);
        permissionHelper.setOnApplyPermissionListener(new PermissionHelper.OnApplyPermissionListener()
        {
            @Override
            public void onAfterApplyAllPermission()
            {
                Log.i(CameraConstant.TAG, "All of requested permissions has been granted, so run app logic.");
                runApp(instanceState);
            }
        });
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            // 如果系统版本低于23，直接跑应用的逻辑
            Log.d(CameraConstant.TAG, "The api level of system is lower than 23, so run app logic directly.");
            runApp(instanceState);
        }
        else
        {
            // 如果权限全部申请了，那就直接跑应用逻辑
            if (permissionHelper.isAllRequestedPermissionGranted())
            {
                Log.d(CameraConstant.TAG, "All of requested permissions has been granted, so run app logic directly.");
                runApp(instanceState);
            }
            else
            {
                // 如果还有权限为申请，而且系统版本大于23，执行申请权限逻辑
                Log.i(CameraConstant.TAG,
                        "Some of requested permissions hasn't been granted, so apply permissions first.");
                permissionHelper.applyPermissions();
            }
        }
    }

    public void runApp(Bundle savedInstanceState)
    {
        operationView = findViewById(R.id.operationView);
        surfaceView = (SurfaceView) findViewById(R.id.cameraSurfaceView);
        surfaceView.getHolder().addCallback(this);
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        findViewById(R.id.btnTakePhoto).setOnClickListener(this);
        findViewById(R.id.btnSwitchFlash).setOnClickListener(this);
        findViewById(R.id.btnSwitchCamera).setOnClickListener(this);
        findViewById(R.id.btnSwitchLocation).setOnClickListener(this);
        findViewById(R.id.btnPreview).setOnClickListener(this);

        locationFullView = findViewById(R.id.locationFullView);

        locationViewModel = new LocationViewModel(CameraActivity.this);
        locationViewModel.initialize(savedInstanceState);
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        permissionHelper.onActivityResult(requestCode, resultCode, data);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void initializeCamera()
    {

        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
        {
            if (camera == null)
            {
                cameraPosition = Camera.CameraInfo.CAMERA_FACING_BACK;
                camera = Camera.open(cameraPosition);
                try
                {
                    setCameraParameters(camera);
                    camera.setDisplayOrientation(90);
                    camera.setPreviewDisplay(surfaceView.getHolder());
                    camera.startPreview();
                }
                catch (IOException e)
                {
                    destoryCurrentCamera();
                    Log.e(CameraConstant.TAG, "initializeCamera() Error.", e);
                }
            }
        }
        else
        {
            Toast.makeText(this, CameraActivity.this.getString(R.string.message_not_found_camera_device),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        initializeCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        destoryCurrentCamera();
    }

    @Override
    public void onClick(View v)
    {

        switch (v.getId())
        {
            case R.id.btnTakePhoto:
                // 快门
                camera.autoFocus(this);// 自动对焦
                break;
            case R.id.btnSwitchFlash:
                switchFlashMode();
                break;
            case R.id.btnSwitchCamera:
                // 切换前后摄像头
                switctCamera();
                break;
            case R.id.btnSwitchLocation:
                startActivity(new Intent(CameraActivity.this, LocationActivity.class));
                break;
            case R.id.btnPreview:
                File[] files = getAltitudeCameraPictures();
                if (files.length > 0)
                {
                    Intent imagePreviewIntent = new Intent(CameraActivity.this, ImagePreviewActivity.class);
                    imagePreviewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    imagePreviewIntent.putExtra(CameraConstant.IMAGE_PATH, files[0].getAbsolutePath());

                    CameraActivity.this.startActivity(imagePreviewIntent);
                }
                else
                {
                    Toast.makeText(CameraActivity.this, CameraActivity.this.getString(R.string.no_picture_to_review),
                            Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    private File[] getAltitudeCameraPictures()
    {

        String baseDir = CameraHelper.getAltitudeCameraBaseDir();
        File directy = new File(baseDir);
        File[] files = directy.listFiles(new FileFilter()
        {

            @Override
            public boolean accept(File f)
            {
                return f.isFile() && f.getName().toUpperCase(Locale.CHINA).endsWith(CameraConstant.JPG);
            }
        });

        Arrays.sort(files, new Comparator<File>()
        {

            @Override
            public int compare(File lhs, File rhs)
            {

                if (lhs.lastModified() == rhs.lastModified())
                {
                    return 0;
                }

                if (lhs.lastModified() > rhs.lastModified())
                {
                    return -1;
                }

                return 1;
            }
        });

        return files;
    }

    private void switchFlashMode()
    {

        if (flashModeType == CameraConstant.FLASH_MODE_AUTO)
        {
            flashMode = Camera.Parameters.FLASH_MODE_ON;
            flashModeType = CameraConstant.FLASH_MODE_ON;
            ((ImageButton) findViewById(R.id.btnSwitchFlash)).setImageResource(R.mipmap.ic_flash_selected);
        }
        else if (flashModeType == CameraConstant.FLASH_MODE_ON)
        {
            flashMode = Camera.Parameters.FLASH_MODE_OFF;
            flashModeType = CameraConstant.FLASH_MODE_OFF;
            ((ImageButton) findViewById(R.id.btnSwitchFlash)).setImageResource(R.mipmap.ic_flash_off_selected);
        }
        else if (flashModeType == CameraConstant.FLASH_MODE_OFF)
        {
            flashMode = Camera.Parameters.FLASH_MODE_AUTO;
            flashModeType = CameraConstant.FLASH_MODE_AUTO;
            ((ImageButton) findViewById(R.id.btnSwitchFlash)).setImageResource(R.mipmap.ic_flash_auto_selected);
        }

        if (cameraPosition == Camera.CameraInfo.CAMERA_FACING_BACK)
        {
            Camera.Parameters params = camera.getParameters();
            params.setFlashMode(flashMode);
            camera.setParameters(params);
        }
    }

    /**
     * 代表摄像头的方位，CAMERA_FACING_FRONT前置 CAMERA_FACING_BACK后置
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void switctCamera()
    {

        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int i = 0; i < cameraCount; i++)
        {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraPosition == Camera.CameraInfo.CAMERA_FACING_BACK)
            {
                // 现在是后置，变更为前置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
                {
                    destoryCurrentCamera();
                    camera = Camera.open(i);
                    try
                    {
                        cameraPosition = Camera.CameraInfo.CAMERA_FACING_FRONT;
                        ((ImageButton) findViewById(R.id.btnSwitchCamera)).setImageResource(R.mipmap.ic_switch_camera_front);
                        setCameraParameters(camera);
                        camera.setDisplayOrientation(90);
                        camera.setPreviewDisplay(surfaceView.getHolder());
                        camera.startPreview();
                    }
                    catch (IOException e)
                    {
                        destoryCurrentCamera();
                        Log.e(CameraConstant.TAG, "switctCamera() Error.", e);
                    }
                    break;
                }
            }
            else
            {
                // 现在是前置， 变更为后置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
                {
                    destoryCurrentCamera();
                    camera = Camera.open(i);
                    try
                    {
                        cameraPosition = Camera.CameraInfo.CAMERA_FACING_BACK;
                        ((ImageButton) findViewById(R.id.btnSwitchCamera)).setImageResource(R.mipmap.ic_switch_camera_back);
                        setCameraParameters(camera);
                        camera.setDisplayOrientation(90);
                        camera.setPreviewDisplay(surfaceView.getHolder());
                        camera.startPreview();
                    }
                    catch (IOException e)
                    {
                        destoryCurrentCamera();
                        Log.e(CameraConstant.TAG, "switctCamera() Error.", e);
                    }
                    break;
                }
            }
        }
    }

    private void destoryCurrentCamera()
    {

        if (camera != null)
        {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private String createFileName()
    {
        return MessageFormat.format(CameraConstant.FILE_NAME_FORMAT,
                CameraHelper.formatDate(new Date(), CameraConstant.SIMPLE_DATE_FORMAT02));
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera)
    {

        final boolean isFrontCamera = isFrontCamera();

        new AsyncTask<byte[], String, String>()
        {

            @Override
            protected String doInBackground(byte[]... params)
            {
                Bitmap locationScreenBitmap = CameraHelper.createHorizontalHalfScreenBitmap(CameraActivity.this);
                publishProgress(CameraConstant.VISIBLE);

                Bitmap cameraImageBitmap = BitmapFactory.decodeByteArray(params[0], 0, params[0].length);

                if (isFrontCamera)
                {
                    cameraImageBitmap = CameraHelper.rotate(cameraImageBitmap, 270);
                }
                else
                {
                    cameraImageBitmap = CameraHelper.rotate(cameraImageBitmap, 90);
                }

                cameraImageBitmap = CameraHelper.zoomBitmap(cameraImageBitmap, surfaceView.getWidth(),
                        surfaceView.getHeight());

                Bitmap resultBitmap = CameraHelper.addLR2Bitmap(locationScreenBitmap, cameraImageBitmap);
                File resultFile = new File(CameraHelper.getAltitudeCameraBaseDir(), createFileName());
                if (CameraHelper.savePicture(resultBitmap, resultFile))
                {
                    Intent imagePreviewIntent = new Intent(CameraActivity.this, ImagePreviewActivity.class);
                    imagePreviewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    imagePreviewIntent.putExtra(CameraConstant.IMAGE_PATH, resultFile.getAbsolutePath());

                    CameraActivity.this.startActivity(imagePreviewIntent);
                }
                else
                {
                    publishProgress(CameraConstant.SAVE_FAILED);
                }

                return null;
            }

            @Override
            protected void onPreExecute()
            {
                surfaceView.setVisibility(View.INVISIBLE);
                operationView.setVisibility(View.INVISIBLE);
                locationFullView.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onProgressUpdate(String... values)
            {
                if (values != null && CameraConstant.VISIBLE.equals(values[0]))
                {
                    surfaceView.setVisibility(View.VISIBLE);
                    operationView.setVisibility(View.VISIBLE);
                    locationFullView.setVisibility(View.INVISIBLE);
                }

                if (values != null && CameraConstant.SAVE_FAILED.equals(values[0]))
                {
                    Toast.makeText(CameraActivity.this,
                            CameraActivity.this.getString(R.string.message_failed_save_picture), Toast.LENGTH_SHORT)
                            .show();
                }
            }

        }.execute(data);

    }

    protected boolean isFrontCamera()
    {
        return CameraActivity.this.cameraPosition == Camera.CameraInfo.CAMERA_FACING_FRONT;
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera)
    {

        if (cameraPosition == Camera.CameraInfo.CAMERA_FACING_FRONT)
        {
            doTakePicture(camera);
        }
        else
        {
            if (success)
            {
                doTakePicture(camera);
            }
        }
    }

    private void doTakePicture(Camera camera)
    {
        Camera.Parameters params = camera.getParameters();
        params.setPictureFormat(ImageFormat.JPEG);
        Camera.Size suitableSize = getSuitableSize(params);

        params.setPictureSize(suitableSize.width, suitableSize.height);
        params.setJpegQuality(100);

        camera.setParameters(params);// 将参数设置到我的camera
        camera.takePicture(null, null, CameraActivity.this);// 将拍摄到的照片给自定义的对象
    }

    public Camera.Size getSuitableSize(Camera.Parameters params)
    {
        if (cameraPosition == Camera.CameraInfo.CAMERA_FACING_BACK)
        {
            if (suitableSizeBack == null)
            {
                suitableSizeBack = CameraHelper.getMostSuitableSize(params.getSupportedPictureSizes(),
                        surfaceView.getWidth());
            }

            return suitableSizeBack;
        }
        else
        {
            if (suitableSizeFront == null)
            {
                suitableSizeFront = CameraHelper.getMostSuitableSize(params.getSupportedPictureSizes(),
                        surfaceView.getWidth());
            }

            return suitableSizeFront;
        }
    }

    private void setCameraParameters(Camera camera)
    {

        Camera.Parameters params = camera.getParameters();
        Camera.Size suitableSize = getSuitableSize(params);

        params.setPreviewSize(suitableSize.width, suitableSize.height);

        if (cameraPosition == Camera.CameraInfo.CAMERA_FACING_BACK)
        {
            params.setFlashMode(flashMode);
        }

        params.set("orientation", "portrait");

        // 将参数设置到我的camera
        camera.setParameters(params);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        destoryCurrentCamera();
        if (locationViewModel != null)
        {
            locationViewModel.onDestroy();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
//		initializeCamera();
        if (locationViewModel != null)
        {
            locationViewModel.onResume();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        destoryCurrentCamera();
        if (locationViewModel != null)
        {
            locationViewModel.onPause();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (locationViewModel != null)
        {
            locationViewModel.onSaveInstanceState(outState);
        }
    }
}
