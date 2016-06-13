package com.example.hacks_000.giffcreator_02.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.example.hacks_000.giffcreator_02.R;
import com.example.hacks_000.giffcreator_02.data.model.Constant;
import com.example.hacks_000.giffcreator_02.util.ImageUtil;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.io.File;
import java.io.IOException;

public class EditImageActivity extends AppCompatActivity {
    private Bitmap mBitmapSource;
    private int mTypeIntent;
    private ImageView mImagePrevivew;
    private Toolbar mToolbar;
    private MyClickListener mMyClickListener;
    private ImageButton mButtonCrop;
    private Uri mImageUri;

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
        mMyClickListener =  new MyClickListener();
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
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch(id) {
                case R.id.button_crop:
                    startCrop();
                    break;
            }
        }
    }
}
