package com.example.hacks_000.giffcreator_02.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import com.example.hacks_000.giffcreator_02.R;
import com.example.hacks_000.giffcreator_02.data.model.Constant;
import com.example.hacks_000.giffcreator_02.util.ImageUtil;

import java.io.IOException;

public class EditImageActivity extends AppCompatActivity {
    private Bitmap mBitmapSource;
    private int mTypeIntent;
    private ImageView mImagePrevivew;
    private Toolbar mToolbar;

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
                Uri uri = Uri.parse(intent.getStringExtra(Constant.INTENT_DATA));
                mBitmapSource = MediaStore.Images.Media
                        .getBitmap(getApplicationContext().getContentResolver(), uri);
                break;
            case HomeActivity.TYPE_LIBRARY:
                String imagePath = intent.getStringExtra(Constant.INTENT_DATA);
                mBitmapSource = ImageUtil.decodeBitmapFromPathToFitScreen(getApplicationContext(), imagePath);
        }
    }

    private void findView() {
        getWindow().setBackgroundDrawable(null);
        mToolbar = (Toolbar)findViewById(R.id.tool_bar);
        mToolbar.setTitle(R.string.edit_image_title);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mImagePrevivew = (ImageView) findViewById(R.id.image_effect_pr);
        mImagePrevivew.setImageBitmap(mBitmapSource);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_image_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.done:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
