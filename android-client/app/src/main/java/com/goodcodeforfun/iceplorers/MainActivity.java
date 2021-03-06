package com.goodcodeforfun.iceplorers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.commonsware.cwac.cam2.CameraActivity;
import com.commonsware.cwac.cam2.Facing;
import com.commonsware.cwac.cam2.FlashMode;
import com.commonsware.cwac.cam2.ZoomStyle;
import com.commonsware.cwac.security.RuntimePermissionUtils;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.seismic.ShakeDetector;
import com.yayandroid.locationmanager.base.LocationBaseActivity;
import com.yayandroid.locationmanager.configuration.Configurations;
import com.yayandroid.locationmanager.configuration.LocationConfiguration;
import com.yayandroid.locationmanager.constants.FailType;
import com.yayandroid.locationmanager.constants.ProcessType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends LocationBaseActivity implements OnMapReadyCallback, SensorEventListener, ShakeDetector.Listener, GoogleMap.OnMarkerClickListener {
    private static final String[] PERMS_ALL = {
            CAMERA,
            RECORD_AUDIO,
            WRITE_EXTERNAL_STORAGE
    };
    private static final int REQUEST_LANDSCAPE = 1337;
    private static final int RESULT_PERMS_ALL = 1338;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final FlashMode[] FLASH_MODES = {
            FlashMode.OFF,
            FlashMode.AUTO
    };
    private OkHttpClient client = new OkHttpClient.Builder().addNetworkInterceptor(new StethoInterceptor()).build();
    private SensorManager mSensorManager;
    private Sensor mCompass;
    private ProgressDialog progressDialog;
    private File testRoot;
    private GoogleMap mMap;
    private MapView mMapView;
    private RuntimePermissionUtils utils;
    private float azimuth;
    private File photoResult;
    private boolean isCapturingPhoto = false;

    public static long getFolderSize(File f) {
        long size = 0;
        if (f.isDirectory()) {
            for (File file : f.listFiles()) {
                size += getFolderSize(file);
            }
        } else {
            size = f.length();
        }
        return size;
    }

    public void hearShake() {
        Toast.makeText(this, "Don't shake me, bro!", Toast.LENGTH_SHORT).show();
        if (!isCapturingPhoto) {
            capturePhoto();
        }
    }

    // The following method is required by the SensorEventListener interface;
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        azimuth = Math.round(event.values[0]);
    }

    @Override
    public LocationConfiguration getLocationConfiguration() {
        return Configurations.defaultConfiguration("Gimme the permission!", "Would you mind to turn GPS on?");
    }

    @Override
    public void onLocationChanged(Location location) {
        dismissProgress();
        dataCollectionCompleted(location);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        ShakeDetector sd = new ShakeDetector(this);
        sd.setSensitivity(ShakeDetector.SENSITIVITY_HARD);
        sd.start(sensorManager);


        String filename = "cam2_" + Build.MANUFACTURER + "_" + Build.PRODUCT
                + "_" + new SimpleDateFormat("yyyyMMdd'-'HHmmss", Locale.getDefault()).format(new Date());

        filename = filename.replaceAll(" ", "_");

        testRoot = new File(getExternalFilesDir(null), filename);

        utils = new RuntimePermissionUtils(this);
        if (!haveNecessaryPermissions() && utils.useRuntimePermissions()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(PERMS_ALL, RESULT_PERMS_ALL);
            }
        } else {
            handlePage();
        }
    }

    private void handlePage() {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        FloatingActionButton takePictureButton = (FloatingActionButton) findViewById(R.id.take_picture_button);

        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capturePhoto();
            }
        });
    }

    private void capturePhoto() {
        isCapturingPhoto = true;
        Intent intent;
        String filename = "rear_" + new SimpleDateFormat("yyyyMMdd'-'HHmmss", Locale.getDefault()).format(new Date()) + ".jpg";

        filename = filename.replaceAll(" ", "_");

        photoResult = new File(testRoot, filename);
        intent = new CameraActivity.IntentBuilder(this)
                .skipConfirm()
                .facing(Facing.BACK)
                .facingExactMatch()
                .to(photoResult)
                .updateMediaStore()
                .flashModes(FLASH_MODES)
                .zoomStyle(ZoomStyle.SEEKBAR)
                .timer(5)
                .debugSavePreviewFrame()
                .debug()
                .build();

        startActivityForResult(intent, REQUEST_LANDSCAPE);
    }

    @Override
    protected void onActivityResult(final int requestCode,
                                    final int resultCode,
                                    final Intent data) {
        switch (requestCode) {

            case REQUEST_LANDSCAPE:
                isCapturingPhoto = false;
                handlePage();
                startDataCollection();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onProcessTypeChanged(@ProcessType int processType) {
        switch (processType) {
            case ProcessType.GETTING_LOCATION_FROM_GOOGLE_PLAY_SERVICES: {
                updateProgress("Getting Location from Google Play Services...");
                break;
            }
            case ProcessType.GETTING_LOCATION_FROM_GPS_PROVIDER: {
                updateProgress("Getting Location from GPS...");
                break;
            }
            case ProcessType.GETTING_LOCATION_FROM_NETWORK_PROVIDER: {
                updateProgress("Getting Location from Network...");
                break;
            }
            case ProcessType.ASKING_PERMISSIONS:
            case ProcessType.GETTING_LOCATION_FROM_CUSTOM_PROVIDER:
                // Ignored
                break;
        }
    }

    private void startDataCollection() {
        getLocation();
        //if (getLocationManager().isWaitingForLocation()
        //        && !getLocationManager().isAnyDialogShowing()) {
        displayProgress();
        //}
    }

    private void dataCollectionCompleted(final Location location) {
        final float localAzimuth = azimuth;
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.result_dialog_title)
                .customView(R.layout.result_dialog_custom_view, true)// true for wrap in scrollview
                .positiveText(R.string.positive)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Log.e("TODO", "send it out!");
                        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://arcticgame.cloudapp.net/api/photos/upload").newBuilder();
                        urlBuilder.addQueryParameter("username", "oman");
                        urlBuilder.addQueryParameter("comment", "coolStoryBro");
                        urlBuilder.addQueryParameter("lon", String.valueOf(location.getLongitude()));
                        urlBuilder.addQueryParameter("lat", String.valueOf(location.getLatitude()));
                        urlBuilder.addQueryParameter("az", String.valueOf(localAzimuth));
                        String url = urlBuilder.build().toString();

                        Request request = null;
                        RequestBody requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("", "", RequestBody.create(MediaType.parse("image/jpeg"), photoResult))
                                .build();

                        request = new Request.Builder()
                                .url(url)
                                .addHeader("UniqueId", "ccp")
                                //.method("POST", RequestBody.create(MediaType.parse("image/jpeg"), photoResult))
                                .addHeader("Content-Type", "multipart/form-data;")
                                .post(requestBody)
                                .build();

                        Log.e("man", request.method());
                        Log.e("man", String.valueOf(request.headers()));
                        Log.e("man", String.valueOf(request.toString()));

                        String value = null;
                        long Filesize = getFolderSize(photoResult) / 1024;//call function and convert bytes into Kb
                        if (Filesize >= 1024)
                            value = Filesize / 1024 + " Mb";
                        else
                            value = Filesize + " Kb";


                        Log.e("man ", value);

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, final Response response) throws IOException {
                                if (!response.isSuccessful()) {
                                    throw new IOException("Unexpected code " + response);
                                } else {
                                    //TODO: handle responce
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            requestData();
                                            Toast.makeText(MainActivity.this, "Cool! You added a photo!", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }
                        });
                    }
                }).show();

        View rootView = dialog.getCustomView();
        if (rootView != null) {
            ((TextView) rootView.findViewById(R.id.locationText)).setText("Lat: " + location.getLatitude() + "\nLon: " + location.getLongitude());
            ((TextView) rootView.findViewById(R.id.azimuthText)).setText("Azimuth: " + localAzimuth + "\n");
            final ImageView photoView = (ImageView) rootView.findViewById(R.id.photo);
            Glide.with(MainActivity.this)
                    .load(photoResult)
                    .into(photoView);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        mSensorManager.registerListener(this, mCompass, SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }

    private void displayProgress() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.getWindow().addFlags(Window.FEATURE_NO_TITLE);
            progressDialog.setMessage("Getting location...");
        }

        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    public void updateProgress(String text) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.setMessage(text);
        }
    }

    public void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }


    @Override
    protected void onPause() {
        mMapView.onPause();
        dismissProgress();
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        // Add a marker in Sydney and move the camera
        requestData();
    }

    private void requestData() {
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://arcticgame.cloudapp.net/api/photos").newBuilder();
        //urlBuilder.addQueryParameter("v", "1.0");
        //urlBuilder.addQueryParameter("user", "vogella");
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    try {
                        JSONArray photos = new JSONArray(response.body().string());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mMap.clear();
                            }
                        });
                        for (int i = 0; i < photos.length(); i++) {
                            JSONObject row = photos.getJSONObject(i);
                            final int key = row.getInt("Key");
                            final String comment = row.getString("Comment");
                            JSONObject location = row.getJSONObject("Location");
                            double lon = location.getDouble("Longtitude");
                            double lat = location.getDouble("Latitude");
                            double az = location.getDouble("Azimuth");
                            Log.e("yo", "key " + key);
                            final LatLng markerLocation = new LatLng(lat, lon);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mMap.addMarker(new MarkerOptions().position(markerLocation).flat(true).snippet(String.valueOf(key)).title(comment));
                                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLocation, 20f));
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (haveNecessaryPermissions()) {
            handlePage();
        } else {
            Toast.makeText(this, R.string.msg_perms_missing,
                    Toast.LENGTH_LONG).show();
            finish();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean haveNecessaryPermissions() {
        return (utils.hasPermission(CAMERA) &&
                utils.hasPermission(RECORD_AUDIO) &&
                utils.hasPermission(WRITE_EXTERNAL_STORAGE));
    }

    public void onLocationFailed(int failType) {
        dismissProgress();

        switch (failType) {
            case FailType.TIMEOUT: {
                Toast.makeText(this, "Couldn't get location, and timeout!", Toast.LENGTH_LONG).show();
                break;
            }
            case FailType.PERMISSION_DENIED: {
                Toast.makeText(this, "Couldn't get location, because user didn't give permission!", Toast.LENGTH_LONG).show();
                break;
            }
            case FailType.NETWORK_NOT_AVAILABLE: {
                Toast.makeText(this, "Couldn't get location, because network is not accessible!", Toast.LENGTH_LONG).show();
                break;
            }
            case FailType.GOOGLE_PLAY_SERVICES_NOT_AVAILABLE: {
                Toast.makeText(this, "Couldn't get location, because Google Play Services not available!", Toast.LENGTH_LONG).show();
                break;
            }
            case FailType.GOOGLE_PLAY_SERVICES_CONNECTION_FAIL: {
                Toast.makeText(this, "Couldn't get location, because Google Play Services connection failed!", Toast.LENGTH_LONG).show();
                break;
            }
            case FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DIALOG: {
                Toast.makeText(this, "Couldn't display settingsApi dialog!", Toast.LENGTH_LONG).show();
                break;
            }
            case FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DENIED: {
                Toast.makeText(this, "Couldn't get location, because user didn't activate providers via settingsApi!", Toast.LENGTH_LONG).show();
                break;
            }
            case FailType.VIEW_DETACHED: {
                Toast.makeText(this, "Couldn't get location, because in the process view was detached!", Toast.LENGTH_LONG).show();
                break;
            }
            case FailType.VIEW_NOT_REQUIRED_TYPE: {
                Toast.makeText(this, "Couldn't get location, "
                        + "because view wasn't sufficient enough to fulfill given configuration!", Toast.LENGTH_LONG).show();
                break;
            }
            case FailType.UNKNOWN: {
                Toast.makeText(this, "Ops! Something went wrong!", Toast.LENGTH_LONG).show();
                break;
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(marker.getTitle())
                .customView(R.layout.marker_dialog_custom_view, true)// true for wrap in scrollview
                .positiveText(R.string.close)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).show();

        View rootView = dialog.getCustomView();
        if (rootView != null) {
            final ImageView photoView = (ImageView) rootView.findViewById(R.id.photo);
            Glide.with(MainActivity.this)
                    .load("http://arcticgame.cloudapp.net/api/photos/" + marker.getSnippet())
                    .into(photoView);
        }
        return true;
    }
}
