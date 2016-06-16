package com.example.hacks_000.giffcreator_02.ui.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.example.hacks_000.giffcreator_02.R;
import com.example.hacks_000.giffcreator_02.data.model.Constant;
import com.example.hacks_000.giffcreator_02.util.EffectUtil;
import com.example.hacks_000.giffcreator_02.util.ImageUtil;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class EditImageActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 1;
    private Bitmap mBitmapSource;
    private int mTypeIntent;
    private ImageView mImagePrevivew;
    private Toolbar mToolbar;
    private MyClickListener mMyClickListener;
    private ImageButton mButtonCrop, mButtonInvert, mButtonHighlight;
    private Uri mImageUri;
    private ProgressDialog mProgressDialog;
    private File mImagePath;
    private boolean mIsStartForResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        findView();
    }

    private void init() throws IOException {
        Intent intent = getIntent();
        mIsStartForResult = intent.getBooleanExtra(Constant.INTENT_TYPE_START, false);
        mTypeIntent = intent.getIntExtra(Constant.INTENT_TYPE_DATA, -1);
        switch (mTypeIntent) {
            case HomeActivity.TYPE_CAMERA:
                mImageUri = Uri.parse(intent.getStringExtra(Constant.INTENT_DATA));
                mBitmapSource = MediaStore.Images.Media
                        .getBitmap(getApplicationContext().getContentResolver(), mImageUri);
                break;
            case HomeActivity.TYPE_LIBRARY:
                String imagePath = intent.getStringExtra(Constant.INTENT_DATA);
                File file = new File(imagePath);
                mImageUri = Uri.fromFile(file);
                mBitmapSource = ImageUtil.decodeBitmapFromPathToFitScreen(getApplicationContext(), imagePath);
        }
        mMyClickListener = new MyClickListener();
        mProgressDialog = new ProgressDialog(EditImageActivity.this);
    }

    private void findView() {
        getWindow().setBackgroundDrawable(null);
        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        mToolbar.setTitle(R.string.edit_image_title);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mImagePrevivew = (ImageView) findViewById(R.id.image_effect_pr);
        mImagePrevivew.setImageBitmap(mBitmapSource);
        mButtonCrop = (ImageButton) findViewById(R.id.button_crop);
        mButtonCrop.setOnClickListener(mMyClickListener);
        mButtonHighlight = (ImageButton) findViewById(R.id.button_highlight);
        mButtonHighlight.setOnClickListener(mMyClickListener);
        mButtonInvert = (ImageButton) findViewById(R.id.button_invert);
        mButtonInvert.setOnClickListener(mMyClickListener);
    }

    private void startCrop() {
        CropImage.activity(mImageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();
            try {
                mBitmapSource = ImageUtil.getImageFromUri(getApplicationContext(), mImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mImagePrevivew.setImageBitmap(mBitmapSource);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_image_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.done:
                startGifActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startGifActivity() {
        try {
            checkWriteExternalPermissionAndGetTakenPhoto();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(EditImageActivity.this, GifPreviewActivity.class);
        intent.putExtra(Constant.INTENT_DATA, mImagePath.getAbsolutePath());
        if(mIsStartForResult) {
            setResult(GifPreviewActivity.ADD_IMAGE_REQUEST_CODE, intent);
            finish();
        } else {
            startActivity(intent);
        }
    }

    private void checkWriteExternalPermissionAndGetTakenPhoto() throws IOException {
        if (ContextCompat.checkSelfPermission(EditImageActivity.this, Manifest.permission
                .WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            saveImage();
        }
        if (ContextCompat
                .checkSelfPermission(EditImageActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditImageActivity.this, new String[]
                            {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveImage();
            return;
        }
    }


    private void saveImage() {
        String imageName = java.text.DateFormat.getDateTimeInstance().format(Calendar
                .getInstance().getTime());
        mImagePath = ImageUtil.saveImage(EditImageActivity.this, mBitmapSource, imageName);
    }

    private class MyClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.button_crop:
                    startCrop();
                    break;
                case R.id.button_invert:
                    InvertImageAsyncTask invertImageAsyncTask = new InvertImageAsyncTask();
                    invertImageAsyncTask.execute();
                    break;
                case R.id.button_highlight:
                    mBitmapSource = EffectUtil.applyReflection(mBitmapSource);
                    mImagePrevivew.setImageBitmap(mBitmapSource);
                    break;
            }
        }

        private class InvertImageAsyncTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected void onPreExecute() {
                mProgressDialog.setCancelable(false);
                mProgressDialog.setTitle(R.string.string_invert);
                mProgressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                mBitmapSource = EffectUtil.bright(mBitmapSource);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mProgressDialog.dismiss();
                mImagePrevivew.setImageBitmap(mBitmapSource);
            }
        }
    }
}
