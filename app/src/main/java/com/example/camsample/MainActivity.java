package com.example.camsample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.util.Collection;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {
    private TextureView mTextureView = null;
    private CameraManager mCameraManager = null;
    private CameraDevice mCameraDevice = null;
    private String mBackCameraId = "0";
    private CameraCharacteristics mCameraCharacteristics = null;
    private Size mPreviewSize = null;
    private CaptureRequest.Builder mPreviewRequestBuilder = null;
    private CameraCaptureSession mSession = null;
    private static final int PERMISSION_ALL = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("KEG", "onCreate()");

        checkPermission();


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("KEG", "onStart()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("KEG", "onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("KEG", "onDestory()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("KEG", "onPause()");
        closeCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("KEG", "onResume()");

        init();
        setPreviewSize();

        if (mTextureView.isAvailable()) {
            openCamera();
        }else {
            mTextureView.setSurfaceTextureListener(this);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e("KEG", "onSurfaceTextureAvailable");
        openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.e("KEG", "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e("KEG", "onSurfaceTextureDestoryed");
        return false;

    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private CameraDevice.StateCallback mCameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.e("KEG", "onOpend");
            mCameraDevice = camera;
            showPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.e("KEG", "onDisconnected");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e("KEG", "onError");
        }
    };

    CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.e("KEG","CameraCaptureSession.StateCallback onConfigured()");
            mSession = session;
            updatePreview();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.e("KEG","CameraCaptureSession.StateCallback onConfigureFailed()");

        }
    };

    private void init() {
        mTextureView = findViewById(R.id.textureView);
        mCameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
    }

    private void openCamera() {
        if (mCameraManager == null) {
            Log.e("KEG", "CameraManager is null");
            return;
        }


        try {
            mCameraManager.openCamera(mBackCameraId, mCameraStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (SecurityException se) {
            se.printStackTrace();
            Log.e("KEG","Check Permission");
        }
    }

    private void setPreviewSize() {
        if(mTextureView!= null) {

        }
        try {
            mCameraCharacteristics = mCameraManager.getCameraCharacteristics(mBackCameraId);
            StreamConfigurationMap map = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] size = map.getOutputSizes(SurfaceTexture.class);
            mPreviewSize = size[0];

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



    private void showPreview() {
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(),mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface),mSessionStateCallback,null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }
    private void updatePreview() {
        Log.e("KEG","updatePreview()");
        if(mSession==null) {
            Log.e("KEG","CameraSession is null");
            return;
        }

        try {
            mSession.setRepeatingRequest(mPreviewRequestBuilder.build(),null,null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if(mCameraDevice!=null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        if(mSession!=null) {
            mSession.close();
            mSession = null;
        }
    }

    public void checkPermission(){
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)  {
            /*
                checkSelfPermission() 해당 권한을 가지고 있는지 체크
             */
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {

                // shouldShowRequestPermissionRationale() 해당 권한을 가져와야 하는 이유 설명
                Toast.makeText(this, "CANERA", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_ALL);
            //requestPermissions() 해당 권한을 요청
        } else {
            Log.e("KEG","Check Permission");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {

            case PERMISSION_ALL:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("KEG","onRequestPermission()");
                } else {
                    //퍼미션 거절

                }
                break;
        }
    }
}
