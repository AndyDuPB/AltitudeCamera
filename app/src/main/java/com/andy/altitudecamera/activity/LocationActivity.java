package com.andy.altitudecamera.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.andy.altitudecamera.LocationViewModel;
import com.andy.altitudecamera.R;

public class LocationActivity extends Activity implements View.OnClickListener
{

    private LocationViewModel locationViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_location);

        locationViewModel = new LocationViewModel(LocationActivity.this);
        locationViewModel.initialize(savedInstanceState);

        findViewById(R.id.btnBack).setOnClickListener(this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (locationViewModel != null)
        {
            locationViewModel.onDestroy();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (locationViewModel != null)
        {
            locationViewModel.onResume();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
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

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btnBack:

                LocationActivity.this.finish();
                break;
        }
    }

}
