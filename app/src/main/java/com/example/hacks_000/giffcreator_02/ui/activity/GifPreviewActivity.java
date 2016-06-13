package com.example.hacks_000.giffcreator_02.ui.activity;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.hacks_000.giffcreator_02.R;
import com.example.hacks_000.giffcreator_02.data.model.Constant;
import com.example.hacks_000.giffcreator_02.ui.adapter.ListImageAdapter;
import com.example.hacks_000.giffcreator_02.ui.mylistener.MyOnClickListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GifPreviewActivity extends AppCompatActivity implements MyOnClickListener {
    private RecyclerView mRecyclerview;
    private ListImageAdapter mListImageAdapter;
    private List mListImages;
    private ImageView mImagePreviewGif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif_preview);
        init();
        findView();
    }

    private void init() {
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
        Picasso.with(this).load(new File((String)mListImages.get(0)))
                .into(mImagePreviewGif);
    }

    @Override
    public void onItemClick(View view, int position) {

    }
}
