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
import com.example.hacks_000.giffcreator_02.util.InternetUtil;
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
    private MyClickListener mButtonEffectClickListener;
    private ImageButton mButtonCrop, mButtonInvert, mButtonHighlight;
    private Uri mImageUri;
    private ProgressDialog mProgressDialog;
    private File mImagePath;
    private boolean mIsStartForResult;
    private int mEffectType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);
        init();
        findView();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBitmapSource.recycle();
    }

    private void init() {
        Intent intent = getIntent();
        mIsStartForResult = intent.getBooleanExtra(Constant.INTENT_TYPE_START, false);
        mTypeIntent = intent.getIntExtra(Constant.INTENT_TYPE_DATA, -1);
        mProgressDialog = new ProgressDialog(EditImageActivity.this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setTitle(R.string.string_invert);
        mImagePrevivew = (ImageView) findViewById(R.id.image_effect_pr);
        DecodeBitmap decodeBitmap = new DecodeBitmap();
        decodeBitmap.execute(getIntent().getIntExtra(Constant.INTENT_TYPE_DATA, -1));
        mButtonEffectClickListener = new MyClickListener();
    }

    /*private void init() throws IOException {
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
                break;
            case HomeActivity.TYPE_FACEBOOK:
                byte[] byteArray = intent.getByteArrayExtra(Constant.INTENT_DATA);
                mBitmapSource = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                mImageUri = ImageUtil.getImageUri(getApplicationContext(), mBitmapSource);
                break;
        }
        mButtonEffectClickListener = new MyClickListener();
        mProgressDialog = new ProgressDialog(EditImageActivity.this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setTitle(R.string.string_invert);
    }*/

    private void findView() {
        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        mToolbar.setTitle(R.string.edit_image_title);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mImagePrevivew = (ImageView) findViewById(R.id.image_effect_pr);
        mImagePrevivew.setImageBitmap(mBitmapSource);
        mButtonCrop = (ImageButton) findViewById(R.id.button_crop);
        mButtonCrop.setOnClickListener(mButtonEffectClickListener);
        mButtonHighlight = (ImageButton) findViewById(R.id.button_highlight);
        mButtonHighlight.setOnClickListener(mButtonEffectClickListener);
        mButtonInvert = (ImageButton) findViewById(R.id.button_invert);
        mButtonInvert.setOnClickListener(mButtonEffectClickListener);
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
            Intent intent = new Intent(EditImageActivity.this, GifPreviewActivity.class);
            intent.putExtra(Constant.INTENT_DATA, mImagePath.getAbsolutePath());
            if (mIsStartForResult) {
                setResult(GifPreviewActivity.ADD_IMAGE_REQUEST_CODE, intent);
            } else {
                startActivity(intent);
            }
            finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkWriteExternalPermissionAndGetTakenPhoto() throws IOException {
        if (ContextCompat.checkSelfPermission(EditImageActivity.this, Manifest.permission
            .WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            saveImage();
        }
        if (ContextCompat
            .checkSelfPermission(EditImageActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
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
            int typeEffect = v.getId();
            if (typeEffect == R.id.button_crop) {
                startCrop();
            } else {
                ImageEffectAsyncTask invertImageAsyncTask = new ImageEffectAsyncTask(typeEffect);
                invertImageAsyncTask.execute();
            }
        }
    }

    private class ImageEffectAsyncTask extends AsyncTask<Void, Void, Void> {
        private int mEffectType;

        public ImageEffectAsyncTask(int effectType) {
            mEffectType = effectType;
        }

        @Override
        protected void onPreExecute() {
            switch (mEffectType) {
                case R.id.button_crop:
                    break;
                case R.id.button_highlight:
                    mProgressDialog.setTitle(R.string.string_highlight);
                    break;
                case R.id.button_invert:
                    mProgressDialog.setTitle(R.string.string_invert);
                    break;
            }
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            switch (mEffectType) {
                case R.id.button_crop:
                    break;
                case R.id.button_highlight:
                    mBitmapSource = EffectUtil.doHighlightImage(mBitmapSource);
                    break;
                case R.id.button_invert:
                    mBitmapSource = EffectUtil.doInvert(mBitmapSource);
                    break;
            }
            mImageUri = ImageUtil.getImageUri(getApplicationContext(), mBitmapSource);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressDialog.dismiss();
            mImagePrevivew.setImageBitmap(mBitmapSource);
        }
    }

    private class DecodeBitmap extends AsyncTask<Integer, Void, Void> {
        @Override
        protected void onPreExecute() {
            mProgressDialog.setTitle(R.string.string_getting_image);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Integer... params) {
            switch (params[0]) {
                case HomeActivity.TYPE_CAMERA:
                    mImageUri = Uri.parse(getIntent().getStringExtra(Constant.INTENT_DATA));
                    try {
                        mBitmapSource = MediaStore.Images.Media
                            .getBitmap(getApplicationContext().getContentResolver(), mImageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case HomeActivity.TYPE_LIBRARY:
                    String imagePath = getIntent().getStringExtra(Constant.INTENT_DATA);
                    File file = new File(imagePath);
                    mImageUri = Uri.fromFile(file);
                    mBitmapSource =
                        ImageUtil
                            .decodeBitmapFromPathToFitScreen(getApplicationContext(), imagePath);
                    break;
                case HomeActivity.TYPE_FACEBOOK:
                    try {
                        mBitmapSource = InternetUtil.downloadImage(getIntent().getStringExtra
                            (Constant.INTENT_DATA));
                        mImageUri = ImageUtil.getImageUri(getApplicationContext(), mBitmapSource);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressDialog.dismiss();
            mImagePrevivew.setImageBitmap(mBitmapSource);
        }
    }
}
