package com.example.hacks_000.giffcreator_02.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.hacks_000.giffcreator_02.R;
import com.example.hacks_000.giffcreator_02.data.model.Constant;
import com.example.hacks_000.giffcreator_02.ui.adapter.ImageFacebookAdapter;
import com.example.hacks_000.giffcreator_02.ui.mylistener.MyOnClickListener;
import com.example.hacks_000.giffcreator_02.util.InternetUtil;

import java.io.IOException;
import java.util.List;

public class FacebookImageActivity extends Activity implements MyOnClickListener {
    private RecyclerView mRecyclerView;
    private ImageFacebookAdapter mAdapter;
    private List mListImageLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_image);
        findView();
    }

    private void findView() {
        mListImageLink = getIntent().getStringArrayListExtra(Constant.INTENT_DATA);
        mRecyclerView = (RecyclerView) findViewById(R.id.grid_images);
        mAdapter = new ImageFacebookAdapter(getApplicationContext(), mListImageLink);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(FacebookImageActivity.this, 4);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(FacebookImageActivity.this, EditImageActivity.class);
        intent.putExtra(Constant.INTENT_DATA, (String)mListImageLink.get(position));
        intent.putExtra(Constant.INTENT_TYPE_DATA, HomeActivity.TYPE_FACEBOOK);
        if (getIntent().getBooleanExtra(Constant.INTENT_TYPE_START, false)) {
            setResult(GifPreviewActivity.ADD_IMAGE_REQUEST_CODE, intent);
        } else {
            startActivity(intent);
        }
    }
}
