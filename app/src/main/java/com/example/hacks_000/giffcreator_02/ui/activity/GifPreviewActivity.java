package com.example.hacks_000.giffcreator_02.ui.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.hacks_000.giffcreator_02.R;
import com.example.hacks_000.giffcreator_02.data.model.Constant;
import com.example.hacks_000.giffcreator_02.data.model.FacebookConstant;
import com.example.hacks_000.giffcreator_02.ui.adapter.ListImageAdapter;
import com.example.hacks_000.giffcreator_02.ui.mylistener.MyOnClickListener;
import com.example.hacks_000.giffcreator_02.ui.service.DeleteImageService;
import com.example.hacks_000.giffcreator_02.util.GifEncoder;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GifPreviewActivity extends AppCompatActivity implements MyOnClickListener,
    FacebookConstant {
    public static final int ADD_IMAGE_REQUEST_CODE = 3;
    public static final int MAX_FRAME = 10;
    public static final int TYPE_LIBRARY = 0;
    public static final int TYPE_FACEBOOK = 1;
    public static final int TYPE_CAMERA = 2;
    private static final int TIME_DELAY = 500;
    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 2;
    private static final int MY_PERMISSIONS_USE_CAMERA = 3;
    private static final int PICK_IMAGE_REQUEST_CODE = 1;
    private static final int CAPTURE_IMAGE_REQUEST_CODE = 2;
    private static final String PICK_IMAGE_TYPE = "image/*";
    private static final String PICK_IMAGE_TITLE = "Select Picture";
    private RecyclerView mRecyclerview;
    private ListImageAdapter mListImageAdapter;
    private List mListImages;
    private ImageView mImagePreviewGif;
    private int mIndex;
    private Uri mCaptureImageUri;
    private int mPositionInsert;
    private Toolbar mToolbar;
    private ProgressDialog mProgressDialog;
    private boolean mIsTimerCreated;
    private CallbackManager mCallbackManager;
    private AccessToken mToken;
    private List mImageLink = new ArrayList();
    private int mAlbumGot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif_preview);
        init();
        findView();
        setUptoUseFacebook();
    }

    private void init() {
        mIndex = 0;
        mListImages = new ArrayList<String>();
        mListImages.add(getIntent().getStringExtra(Constant.INTENT_DATA));
        mProgressDialog = new ProgressDialog(GifPreviewActivity.this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setTitle(R.string.string_exporting);
    }

    private void findView() {
        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("");
        mRecyclerview = (RecyclerView) findViewById(R.id.rv_list_image);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerview.setLayoutManager(linearLayoutManager);
        mListImageAdapter = new ListImageAdapter(getApplicationContext(), mListImages);
        mRecyclerview.setAdapter(mListImageAdapter);
        mListImageAdapter.setOnItemClickListener(this);
        mImagePreviewGif = (ImageView) findViewById(R.id.gif_preview);
        Glide.with(getApplicationContext())
            .load(new File((String) mListImages.get(mIndex)))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(mImagePreviewGif);
        playGif();
    }

    private void setUptoUseFacebook() {
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        AppEventsLogger.activateApp(this.getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager
            .getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
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
                GraphRequest req =
                    GraphRequest.newGraphPathRequest(mToken, FACEBOOK_ALBUM, getAlbumIdCallback);
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

    private void getImageFromFacebook() {
        if (InternetUtil.isNetworkConnected(getApplicationContext())) {
            LoginManager.getInstance()
                .logInWithReadPermissions(this, Arrays.asList(FACEBOOK_PHOTO_PERMISSION));
        } else {
            Toast.makeText(getApplicationContext(), R.string.no_internet, Toast.LENGTH_SHORT)
                .show();
        }
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
                        startFacebookImageActivity();
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

    private void startFacebookImageActivity() {
        Intent intent = new Intent(GifPreviewActivity.this, FacebookImageActivity.class);
        intent.putStringArrayListExtra(Constant.INTENT_DATA, (ArrayList) mImageLink);
        intent.putExtra(Constant.INTENT_TYPE_START, true);
        startActivity(intent);
    }

    private void playGif() {
        if (mListImages.size() > 1 && !mIsTimerCreated) {
            mIsTimerCreated = true;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    new PlayGifAsynTask().execute();
                }
            }, TIME_DELAY);
        }
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GifPreviewActivity.this);
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

    private void checkPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(GifPreviewActivity.this, Manifest.permission
            .CAMERA) == PackageManager.PERMISSION_GRANTED) {
            captureAndGetFullSizeImage();
            return;
        }
        if (ContextCompat.checkSelfPermission(GifPreviewActivity.this, Manifest.permission
            .CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GifPreviewActivity.this, new String[]
                    {Manifest.permission.CAMERA},
                MY_PERMISSIONS_USE_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE) {
            Intent intent = new Intent(GifPreviewActivity.this, EditImageActivity.class);
            String imagePath = ImageUtil.getPath(GifPreviewActivity.this, data);
            intent.putExtra(Constant.INTENT_TYPE_DATA, TYPE_LIBRARY);
            intent.putExtra(Constant.INTENT_DATA, imagePath);
            intent.putExtra(Constant.INTENT_TYPE_START, true);
            startActivityForResult(intent, ADD_IMAGE_REQUEST_CODE);
            return;
        }
        if (requestCode == ADD_IMAGE_REQUEST_CODE && data != null) {
            mListImages.add(mPositionInsert, data.getStringExtra(Constant.INTENT_DATA));
            mListImageAdapter.notifyItemInserted(mPositionInsert);
            playGif();
        }
    }

    private void checkWriteExternalPermissionAndGetTakenPhoto() throws IOException {
        if (ContextCompat.checkSelfPermission(GifPreviewActivity.this, Manifest.permission
            .WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            getMoreImage();
        }
        if (ContextCompat
            .checkSelfPermission(GifPreviewActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GifPreviewActivity.this, new String[]
                    {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
        }
    }

    private void getMoreImage() {
        Intent intent = new Intent(GifPreviewActivity.this, EditImageActivity.class);
        intent.putExtra(Constant.INTENT_TYPE_DATA, TYPE_CAMERA);
        intent.putExtra(Constant.INTENT_DATA, mCaptureImageUri.toString());
        intent.putExtra(Constant.INTENT_TYPE_START, true);
        startActivityForResult(intent, ADD_IMAGE_REQUEST_CODE);
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

    @Override
    public void onItemClick(View view, int position) {
        if (mListImages.size() > MAX_FRAME) {
            Toast.makeText(this, R.string.string_need_more, Toast.LENGTH_SHORT).show();
        } else {
            mPositionInsert = position + 1;
            showDialog();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gif_preview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.export:
                if (mListImages.size() > 1) {
                    ExportGifAsynTask exportGifAsynTask = new ExportGifAsynTask();
                    exportGifAsynTask.execute();
                } else {
                    Toast.makeText(this, R.string.string_need_more, Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public byte[] generateGIF() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GifEncoder encoder = new GifEncoder();
        encoder.start(bos);
        for (int i = 0; i < mListImages.size(); i++) {
            Bitmap bitmap =
                ImageUtil.decodeBitmapFromPath((String) mListImages.get(i), ImageUtil.LOW_IMAGE,
                    ImageUtil.LOW_IMAGE);
            encoder.addFrame(bitmap);
            bitmap.recycle();
        }
        encoder.finish();
        return bos.toByteArray();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(GifPreviewActivity.this, DeleteImageService.class);
        intent.putStringArrayListExtra(Constant.INTENT_DATA, (ArrayList) mListImages);
        startService(intent);
    }

    private class PlayGifAsynTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mIndex++;
            if (mIndex == mListImages.size()) {
                mIndex = 0;
            }
            Glide.with(getApplicationContext())
                .load(new File((String) mListImages.get(mIndex)))
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(mImagePreviewGif);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    new PlayGifAsynTask().execute();
                }
            }, TIME_DELAY);
        }
    }

    private class ExportGifAsynTask extends AsyncTask<Void, Boolean, Boolean> {
        @Override
        protected void onPreExecute() {
            mProgressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean isSuccess = true;
            try {
                String gifImageName = java.text.DateFormat.getDateTimeInstance().format(Calendar
                    .getInstance().getTime());
                FileOutputStream outStream = new FileOutputStream(
                    Environment.getExternalStorageDirectory() + "/" + gifImageName + ".gif");
                outStream.write(generateGIF());
                outStream.close();
            } catch (Exception e) {
                isSuccess = false;
            } catch (OutOfMemoryError e) {
                isSuccess = false;
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), isSuccess ? R.string.string_export_sucess : R
                    .string.string_export_fail,
                Toast.LENGTH_SHORT).show();
        }
    }
}
