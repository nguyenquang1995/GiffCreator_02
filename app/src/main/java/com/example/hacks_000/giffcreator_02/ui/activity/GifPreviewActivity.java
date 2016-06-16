package com.example.hacks_000.giffcreator_02.ui.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.view.View;
import android.widget.ImageView;

import com.example.hacks_000.giffcreator_02.R;
import com.example.hacks_000.giffcreator_02.data.model.Constant;
import com.example.hacks_000.giffcreator_02.ui.adapter.ListImageAdapter;
import com.example.hacks_000.giffcreator_02.ui.mylistener.MyOnClickListener;
import com.example.hacks_000.giffcreator_02.util.ImageUtil;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GifPreviewActivity extends AppCompatActivity implements MyOnClickListener {
    private static final int TIME_DELAY = 1000;
    private static final int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 1;
    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 2;
    private static final int MY_PERMISSIONS_USE_CAMERA = 3;
    private static final int PICK_IMAGE_REQUEST_CODE = 1;
    private static final int CAPTURE_IMAGE_REQUEST_CODE = 2;
    public static final int ADD_IMAGE_REQUEST_CODE = 3;
    private static final String PICK_IMAGE_TYPE = "image/*";
    private static final String PICK_IMAGE_TITLE = "Select Picture";
    public static final int TYPE_LIBRARY = 0;
    public static final int TYPE_FACEBOOK = 1;
    public static final int TYPE_CAMERA = 2;
    private RecyclerView mRecyclerview;
    private ListImageAdapter mListImageAdapter;
    private List mListImages;
    private ImageView mImagePreviewGif;
    private int mIndex;
    private Uri mCaptureImageUri;
    private int mPositionInsert;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif_preview);
        init();
        findView();
    }

    private void init() {
        mIndex = 0;
        mListImages = new ArrayList();
        mListImages.add(getIntent().getStringExtra(Constant.INTENT_DATA));
    }

    private void findView() {
        getWindow().setBackgroundDrawable(null);
        mRecyclerview = (RecyclerView) findViewById(R.id.rv_list_image);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerview.setLayoutManager(linearLayoutManager);
        mListImageAdapter = new ListImageAdapter(getApplicationContext(), mListImages);
        mRecyclerview.setAdapter(mListImageAdapter);
        mListImageAdapter.setOnItemClickListener(this);
        mImagePreviewGif = (ImageView) findViewById(R.id.gif_preview);
        Picasso.with(this).load(new File((String) mListImages.get(mIndex)))
                .into(mImagePreviewGif);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                new PlayGifAsynTask().execute();
            }
        }, TIME_DELAY);
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GifPreviewActivity.this);
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
        if (ContextCompat.checkSelfPermission(GifPreviewActivity.this, Manifest.permission
                .READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            handlePickPhotoClick();
            return;
        }
        if (ContextCompat.checkSelfPermission(GifPreviewActivity.this, Manifest.permission
                .READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GifPreviewActivity.this, new String[]
                            {Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_READ_EXTERNAL_STORAGE);
        }
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
        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE) {
            Intent intent = new Intent(GifPreviewActivity.this, EditImageActivity.class);
            String imagePath = ImageUtil.getPath(GifPreviewActivity.this, data);
            intent.putExtra(Constant.INTENT_TYPE_DATA, TYPE_LIBRARY);
            intent.putExtra(Constant.INTENT_DATA, imagePath);
            intent.putExtra(Constant.INTENT_TYPE_START, true);
            startActivityForResult(intent, ADD_IMAGE_REQUEST_CODE);
            return;
        }
        if(requestCode == ADD_IMAGE_REQUEST_CODE) {
            mListImages.add(data.getStringExtra(Constant.INTENT_DATA));
            mListImageAdapter.notifyItemInserted(mPositionInsert);
        }
    }

    private void checkWriteExternalPermissionAndGetTakenPhoto() throws IOException {
        if (ContextCompat.checkSelfPermission(GifPreviewActivity.this, Manifest.permission
                .WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(GifPreviewActivity.this, EditImageActivity.class);
            intent.putExtra(Constant.INTENT_TYPE_DATA, TYPE_CAMERA);
            intent.putExtra(Constant.INTENT_DATA, mCaptureImageUri.toString());
            intent.putExtra(Constant.INTENT_TYPE_START, true);
            startActivityForResult(intent, ADD_IMAGE_REQUEST_CODE);
        }
        if (ContextCompat
                .checkSelfPermission(GifPreviewActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GifPreviewActivity.this, new String[]
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

    @Override
    public void onItemClick(View view, int position) {
        mPositionInsert = position + 1;
        showDialog();
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
            Picasso.with(GifPreviewActivity.this)
                    .load(new File((String) mListImages.get(mIndex)))
                    .into(mImagePreviewGif);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    new PlayGifAsynTask().execute();
                }
            }, TIME_DELAY);
        }
    }
}
