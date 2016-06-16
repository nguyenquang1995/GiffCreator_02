package com.example.hacks_000.giffcreator_02.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;
import com.example.hacks_000.giffcreator_02.R;
import com.example.hacks_000.giffcreator_02.data.model.Constant;
import com.example.hacks_000.giffcreator_02.ui.adapter.FacebookImageAdapter;

import java.util.List;

public class FacebookImageActivity extends Activity {
    private GridView mGridViewPhoto;
    private List mListImageLink;
    private FacebookImageAdapter mFacebookImageAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_image);
        findView();
    }
    private void findView() {
        mListImageLink = getIntent().getStringArrayListExtra(Constant.INTENT_DATA);
        mGridViewPhoto = (GridView) findViewById(R.id.grid_images);
        mFacebookImageAdapter = new FacebookImageAdapter(getApplicationContext(), mListImageLink);
        mGridViewPhoto.setAdapter(mFacebookImageAdapter);
    }
}
