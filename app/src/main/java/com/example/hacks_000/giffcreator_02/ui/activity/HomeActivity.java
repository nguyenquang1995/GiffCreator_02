package com.example.hacks_000.giffcreator_02.ui.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.hacks_000.giffcreator_02.R;
import com.example.hacks_000.giffcreator_02.data.model.Constant;
import com.example.hacks_000.giffcreator_02.data.model.FacebookConstant;
import com.example.hacks_000.giffcreator_02.ui.adapter.ListGifAdapter;
import com.example.hacks_000.giffcreator_02.ui.mylistener.MyOnClickListener;
import com.example.hacks_000.giffcreator_02.util.ImageUtil;
import com.example.hacks_000.giffcreator_02.util.InternetUtil;
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
import com.joanfuentes.hintcase.HintCase;
import com.joanfuentes.hintcaseassets.hintcontentholders.SimpleHintContentHolder;
import com.joanfuentes.hintcaseassets.shapeanimators.RevealCircleShapeAnimator;
import com.joanfuentes.hintcaseassets.shapeanimators.UnrevealCircleShapeAnimator;
import com.joanfuentes.hintcaseassets.shapes.CircularShape;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements MyOnClickListener, FacebookConstant {
    public static final int TYPE_LIBRARY = 0;
    public static final int TYPE_FACEBOOK = 1;
    public static final int TYPE_CAMERA = 2;
    private static final int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 1;
    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 2;
    private static final int MY_PERMISSIONS_USE_CAMERA = 3;
    private static final int PICK_IMAGE_REQUEST_CODE = 1;
    private static final int CAPTURE_IMAGE_REQUEST_CODE = 2;
    private static final String PICK_IMAGE_TYPE = "image/*";
    private static final String PICK_IMAGE_TITLE = "Select Picture";
    private Toolbar mToolbar;
    private FloatingActionButton mFab;
    private RecyclerView mRecyclerView;
    private ListGifAdapter mGifAdapter;
    private Uri mCaptureImageUri;
    private CallbackManager mCallbackManager;
    private AccessToken mToken;
    private List mImageLink = new ArrayList();
    private int mAlbumGot;
    private List mListGifs = new ArrayList();
    private boolean mIsCreated;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        checkPermissionAndPickImage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsCreated) {
            mListGifs = ImageUtil.getFilePaths(HomeActivity.this);
            mGifAdapter.mListGifs = mListGifs;
            mGifAdapter.notifyDataSetChanged();
        }
    }

    private void findView() {
        mIsCreated = true;
        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        mToolbar.setTitle(R.string.home_title);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list_gif);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mListGifs = ImageUtil.getFilePaths(HomeActivity.this);
        mGifAdapter = new ListGifAdapter(getApplicationContext(), mListGifs);
        mGifAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mGifAdapter);
        mProgressDialog = new ProgressDialog(HomeActivity.this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setTitle(R.string.string_collecting_image);
        setUptoUseFacebook();
        launchAutomaticHint();
    }

    private void launchAutomaticHint() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        if(!sharedPref.getBoolean(getString(R.string.is_new_install_home), false)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mFab != null) {
                        SimpleHintContentHolder blockInfo =
                            new SimpleHintContentHolder.Builder(mFab.getContext())
                                .setContentText(R.string.text_hint_add_create_gif)
                                .setContentStyle(R.style.content_light)
                                .setMarginByResourcesId(R.dimen.activity_vertical_margin,
                                    R.dimen.activity_horizontal_margin,
                                    R.dimen.activity_vertical_margin,
                                    R.dimen.activity_horizontal_margin)
                                .build();
                        new HintCase(mFab.getRootView())
                            .setTarget(mFab, new CircularShape())
                            .setShapeAnimators(new RevealCircleShapeAnimator(),
                                new UnrevealCircleShapeAnimator())
                            .setHintBlock(blockInfo)
                            .show();
                    }
                }
            }, Constant.TIME_DELAY_SHOW_HINT);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getString(R.string.is_new_install_home), true);
            editor.commit();
        }
    }

    private void setUptoUseFacebook() {
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        AppEventsLogger.activateApp(this.getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance()
            .registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    mProgressDialog.show();
                    mToken = loginResult.getAccessToken();
                    GraphRequest.Callback getAlbumIdCallback = new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse graphResponse) {
                            JSONObject jsonObject = graphResponse.getJSONObject();
                            try {
                                JSONArray array = jsonObject.getJSONArray(FACEBOOK_DATA);
                                int lengthAlbum = array.length();
                                for (int i = 0; i < lengthAlbum; i++) {
                                    getAllImageLink(array.getJSONObject(i).getString(FACEBOOK_ID),
                                        lengthAlbum);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    GraphRequest req = GraphRequest
                        .newGraphPathRequest(mToken, FACEBOOK_ALBUM, getAlbumIdCallback);
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
                    if (mAlbumGot >= lengthAlbum) {
                        mProgressDialog.dismiss();
                        startFacebookImageActivity();
                    }
                } catch (JSONException e) {
                    mProgressDialog.dismiss();
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

    private void startFacebookImageActivity() {
        Intent intent = new Intent(HomeActivity.this, FacebookImageActivity.class);
        intent.putStringArrayListExtra(Constant.INTENT_DATA, (ArrayList) mImageLink);
        startActivity(intent);
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setItems(R.array.item_choose_images, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case TYPE_LIBRARY:
                        handlePickPhotoClick();
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
            findView();
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
            findView();
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
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE) {
            Intent intent = new Intent(HomeActivity.this, EditImageActivity.class);
            String imagePath = ImageUtil.getPath(HomeActivity.this, data);
            intent.putExtra(Constant.INTENT_TYPE_DATA, TYPE_LIBRARY);
            intent.putExtra(Constant.INTENT_DATA, imagePath);
            startActivity(intent);
        }
    }

    private void checkWriteExternalPermissionAndGetTakenPhoto() throws IOException {
        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission
            .WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(HomeActivity.this, EditImageActivity.class);
            intent.putExtra(Constant.INTENT_TYPE_DATA, TYPE_CAMERA);
            intent.putExtra(Constant.INTENT_DATA, mCaptureImageUri.toString());
            startActivity(intent);
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
        if (InternetUtil.isNetworkConnected(getApplicationContext())) {
            LoginManager.getInstance()
                .logInWithReadPermissions(this, Arrays.asList(FACEBOOK_PHOTO_PERMISSION));
        } else {
            Toast.makeText(getApplicationContext(), R.string.no_internet, Toast.LENGTH_SHORT)
                .show();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        int length = mListGifs.size();
        for (int i = 0; i < length; i++) {
            ListGifAdapter.GifViewHolder viewHolder =
                (ListGifAdapter.GifViewHolder) mRecyclerView.findViewHolderForAdapterPosition(i);
            if(viewHolder != null) {
                if (i == position) {
                    Glide.with(getApplicationContext())
                        .load(mListGifs.get(i))
                        .asGif()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(viewHolder.mGifView);
                    viewHolder.mButtonPlay.setVisibility(View.INVISIBLE);
                } else {
                    Glide.with(getApplicationContext())
                        .load(mListGifs.get(i))
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(viewHolder.mGifView);
                    viewHolder.mButtonPlay.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
