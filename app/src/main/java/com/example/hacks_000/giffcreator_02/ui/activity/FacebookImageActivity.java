package com.example.hacks_000.giffcreator_02.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.example.hacks_000.giffcreator_02.R;
import com.example.hacks_000.giffcreator_02.data.model.Constant;
import com.example.hacks_000.giffcreator_02.ui.adapter.ImageFacebookAdapter;
import com.example.hacks_000.giffcreator_02.ui.mylistener.MyOnClickListener;
import java.util.List;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_facebook_image)
public class FacebookImageActivity extends Activity implements MyOnClickListener {
    @ViewById(R.id.grid_images)
    protected RecyclerView mRecyclerView;
    private ImageFacebookAdapter mAdapter;
    private List mListImageLink;

    @AfterViews
    protected void findView() {
        mListImageLink = getIntent().getStringArrayListExtra(Constant.INTENT_DATA);
        mAdapter = new ImageFacebookAdapter(getApplicationContext(), mListImageLink);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(FacebookImageActivity.this, 4);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(FacebookImageActivity.this, EditImageActivity.class);
        intent.putExtra(Constant.INTENT_DATA, (String) mListImageLink.get(position));
        intent.putExtra(Constant.INTENT_TYPE_DATA, HomeActivity.TYPE_FACEBOOK);
        if (getIntent().getBooleanExtra(Constant.INTENT_TYPE_START, false)) {
            setResult(GifPreviewActivity.ADD_IMAGE_REQUEST_CODE, intent);
        } else {
            startActivity(intent);
        }
    }
}
