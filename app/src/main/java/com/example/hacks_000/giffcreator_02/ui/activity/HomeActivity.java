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
import com.example.hacks_000.giffcreator_02.data.model.FacebookConstant;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements FacebookConstant {
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
    private CallbackManager mCallbackManager;
    private AccessToken mToken;
    private List mImageLink = new ArrayList();
    private int mAlbumGot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        findView();
        setUptoUseFacebook();
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

    private void setUptoUseFacebook() {
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        AppEventsLogger.activateApp(this.getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mToken = loginResult.getAccessToken();
                String userId = mToken.getUserId();
                GraphRequest.Callback getAlbumIdCallback = new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse graphResponse) {
                        JSONObject jsonObject = graphResponse.getJSONObject();
                        try {
                            JSONArray array = jsonObject.getJSONArray(FACEBOOK_DATA);
                            int lengthAlbum = array.length();
                            for (int i = 0; i < lengthAlbum; i++) {
                                getAllImageLink(array.getJSONObject(i).getString(FACEBOOK_ID), lengthAlbum);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                GraphRequest req = GraphRequest.newGraphPathRequest(mToken, FACEBOOK_ALBUM, getAlbumIdCallback);
                Bundle parameters = new Bundle();
                parameters.putString(FACEBOOK_FIELD, FACEBOOK_ID);
                req.setParameters(parameters);
                req.executeAsync();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
            }
        });
    }

    private void getAllImageLink(String albumId, final int lengthAlbum) {
        GraphRequest.Callback getImageCallback = new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                JSONObject jsonObject = response.getJSONObject();
                try {
                    JSONArray array = jsonObject.getJSONArray(FACEBOOK_DATA);
                    int lengthImage = array.length();
                    for (int i = 0; i < lengthImage; i++) {
                        mImageLink.add(array.getJSONObject(i).getString(FACEBOOK_SOURCE));
                    }
                    mAlbumGot++;
                    if (mAlbumGot == lengthAlbum) {
                        startActivity(new Intent(HomeActivity.this, HomeActivity.class));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        String api = "/" + albumId + FACEBOOK_PHOTO_API;
        GraphRequest req = GraphRequest.newGraphPathRequest(mToken, api, getImageCallback);
        Bundle parameters = new Bundle();
        parameters.putString(FACEBOOK_FIELD, FACEBOOK_SOURCE);
        req.setParameters(parameters);
        req.executeAsync();

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
                        mImageLink.clear();
                        getImageFromFacebook();
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
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
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

    private void getImageFromFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList(FACEBOOK_PHOTO_PERMISSION));
    }
}
