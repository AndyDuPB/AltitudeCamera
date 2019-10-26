package com.andy.altitudecamera.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.andy.altitudecamera.BuildConfig;
import com.andy.altitudecamera.R;
import com.andy.altitudecamera.constant.CameraConstant;

import java.io.File;

public class ImagePreviewActivity extends Activity implements View.OnClickListener
{

    private ImageView view;
    private String imagePath;
    private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_image_preview);

        findViewById(R.id.btnBack).setOnClickListener(this);
        findViewById(R.id.btnDelete).setOnClickListener(this);
        findViewById(R.id.btnShare).setOnClickListener(this);

        view = (ImageView) findViewById(R.id.previewView);
        this.imagePath = getIntent().getStringExtra(CameraConstant.IMAGE_PATH);
        if (this.imagePath != null)
        {
            view.setImageURI(Uri.fromFile(new File(this.imagePath)));
        }
    }

    @Override
    public void onClick(View v)
    {

        switch (v.getId())
        {
            case R.id.btnBack:

                ImagePreviewActivity.this.finish();
                break;

            case R.id.btnDelete:

                if (this.imagePath != null)
                {
                    File file = new File(this.imagePath);
                    if (file.exists())
                    {
                        if (file.delete())
                        {
                            Log.d(TAG, "Delete the picture file, result : true");
                            Toast.makeText(this, this.getString(R.string.file_delete_success), Toast.LENGTH_SHORT).show();
                            this.finish();
                        }
                        else
                        {
                            Log.d(TAG, "Delete the picture file, result : false");
                        }
                    }
                    else
                    {
                        Log.d(TAG, "Delete the picture file failed, the file : " + file.getAbsolutePath() + " not exists.");
                    }
                }
                break;
            case R.id.btnShare:

                if (this.imagePath != null)
                {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);

                    Uri uri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    {
                        uri = FileProvider.getUriForFile(ImagePreviewActivity.this, BuildConfig.APPLICATION_ID + ".FileProvider", new File(imagePath));
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        shareIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }
                    else {
                        uri = Uri.fromFile(new File(imagePath));
                    }

                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent.setType("image/*");
                    startActivity(Intent
                            .createChooser(shareIntent, ImagePreviewActivity.this.getString(R.string.btn_share)));
                }
                break;
        }
    }
}
