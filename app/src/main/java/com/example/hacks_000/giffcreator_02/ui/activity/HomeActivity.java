package com.example.hacks_000.giffcreator_02.ui.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.hacks_000.giffcreator_02.R;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 1;
    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 2;
    private static final int MY_PERMISSIONS_USE_CAMERA = 3;
    private static final int PICK_IMAGE_REQUEST_CODE = 1;
    private static final int CAPTURE_IMAGE_REQUEST_CODE = 2;
    private static final String PICK_IMAGE_TYPE = "image/*";
    private static final String PICK_IMAGE_TITLE = "Select Picture";
    private static final int TYPE_LIBRARY = 0;
    private static final int TYPE_FACEBOOK = 1;
    private static final int TYPE_CAMERA = 2;
    private Toolbar mToolbar;
    private FloatingActionButton mFab;
    private Uri mCaptureImageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        findView();
    }

    private void findView() {
        getWindow().setBackgroundDrawable(null);
        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        mToolbar.setTitle(R.string.home_title);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setItems(R.array.item_choose_images, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case TYPE_LIBRARY:
                        checkPermissionAndPickImage();
                        break;
                    case TYPE_FACEBOOK:
                        break;
                    case TYPE_CAMERA:
                        checkPermissionAndTakePhoto();
                        break;
                }
            }
        });
        builder.setCancelable(true);
        builder.show();
    }
    private void checkPermissionAndPickImage() {
        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission
                .READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            handlePickPhotoClick();
            return;
        }
        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission
                .READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]
                            {Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_READ_EXTERNAL_STORAGE);
        }
    }

    private void checkPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission
                .CAMERA) == PackageManager.PERMISSION_GRANTED) {
            captureAndGetFullSizeImage();
            return;
        }
        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission
                .CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]
                            {Manifest.permission.CAMERA},
                    MY_PERMISSIONS_USE_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_READ_EXTERNAL_STORAGE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            handlePickPhotoClick();
            return;
        }
        if (requestCode == MY_PERMISSIONS_USE_CAMERA && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            captureAndGetFullSizeImage();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
            try {
                checkWriteExternalPermissionAndGetTakenPhoto();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
    }

    private void checkWriteExternalPermissionAndGetTakenPhoto() throws IOException {
        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission
                .WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Bitmap bitmap = MediaStore.Images.Media
                    .getBitmap(getApplicationContext().getContentResolver(), mCaptureImageUri);
        }
        if (ContextCompat
                .checkSelfPermission(HomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]
                            {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
        }
    }

    private void handlePickPhotoClick() {
        Intent intent = new Intent();
        intent.setType(PICK_IMAGE_TYPE);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, PICK_IMAGE_TITLE),
                PICK_IMAGE_REQUEST_CODE);
    }

    private void captureAndGetFullSizeImage() {
        Calendar cal = Calendar.getInstance();
        File file =
                new File(Environment.getExternalStorageDirectory(), (cal.getTimeInMillis() + ".jpg"));
        mCaptureImageUri = Uri.fromFile(file);
        Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, mCaptureImageUri);
        startActivityForResult(i, CAPTURE_IMAGE_REQUEST_CODE);
    }
}
