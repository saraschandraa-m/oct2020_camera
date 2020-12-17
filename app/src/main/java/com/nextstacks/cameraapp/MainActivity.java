package com.nextstacks.cameraapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FrameLayout mCameraFrame;
    private ImageView mIvCaptureImg;
    private ImageView mIvFlipCamera;
    private ImageView mIvPreviewImg;

    private int cameraID;
    private Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraFrame = findViewById(R.id.camera_holder);
        mIvCaptureImg = findViewById(R.id.iv_capture);
        mIvFlipCamera = findViewById(R.id.iv_switch_camera);
        mIvPreviewImg = findViewById(R.id.iv_preview);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startCameraPreview(true);
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }


        mIvFlipCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isBackCamera = cameraID == Camera.CameraInfo.CAMERA_FACING_BACK ? false : true;
                camera.stopPreview();
                startCameraPreview(isBackCamera);
            }
        });

        mIvCaptureImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        Bitmap capturedImage = BitmapFactory.decodeByteArray(data, 0, data.length);
                        saveImageToDevice(capturedImage);
                    }
                });
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED
            && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                startCameraPreview(true);
            }
        }
    }

    private void startCameraPreview(boolean isBackCamera){
//        if(isBackCamera){
//            cameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
//        }else{
//            cameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
//        }

        cameraID = isBackCamera ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
        camera = Camera.open(cameraID);
        CameraSurfaceView cameraView = new CameraSurfaceView(MainActivity.this, camera);
        mCameraFrame.addView(cameraView);
    }

    private void saveImageToDevice(Bitmap capturedImage){
        File myDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "MyCameraApp");

        if(!myDirectory.exists()){
            myDirectory.mkdir();
        }

        File imageName = new File(myDirectory, "IMG_" + System.currentTimeMillis() +".png");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageName);
            capturedImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        }catch (Exception e){
            e.printStackTrace();
        }

        mIvPreviewImg.setImageBitmap(capturedImage);
        camera.startPreview();
    }

    private void readImagesFromDevice(){
        Uri imageURI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] proj = new String[]{MediaStore.Images.Media.DATA};

        ArrayList<String> imageList = new ArrayList<>();

        Cursor cursor = getApplicationContext().getContentResolver().query(imageURI, proj, null, null, null);

        if(cursor != null){
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
                String image = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                imageList.add(image);
            }
        }
    }
}