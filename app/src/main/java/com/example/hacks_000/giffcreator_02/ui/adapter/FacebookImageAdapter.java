package com.example.hacks_000.giffcreator_02.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.hacks_000.giffcreator_02.R;
import com.example.hacks_000.giffcreator_02.data.model.Constant;
import com.example.hacks_000.giffcreator_02.ui.activity.EditImageActivity;
import com.example.hacks_000.giffcreator_02.ui.activity.HomeActivity;
import com.example.hacks_000.giffcreator_02.util.InternetUtil;

import java.util.List;

/**
 * Created by framgia on 22/06/2016.
 */
public class FacebookImageAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private Context mContext;
    public List mListImages;

    public FacebookImageAdapter(Context context, List listImages) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        mListImages = listImages;
    }

    @Override
    public int getCount() {
        return mListImages.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final MyViewHolder mMyViewHolder;
        mMyViewHolder = new MyViewHolder();
        convertView = mInflater.inflate(R.layout.facebook_image_item, null, false);
        mMyViewHolder.mImgThumb = (ImageView) convertView.findViewById(R.id.facebook_image);
        mMyViewHolder.mImgThumb.setId(position);
        mMyViewHolder.mImgThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(mContext, EditImageActivity.class);
                mMyViewHolder.mImgThumb.setDrawingCacheEnabled(true);
                mMyViewHolder.mImgThumb.buildDrawingCache();
                intent.putExtra(Constant.INTENT_DATA, InternetUtil.convertBitmapTobyteArray
                    (mMyViewHolder.mImgThumb.getDrawingCache().copy(Bitmap.Config.ARGB_8888,
                        true)));
                mMyViewHolder.mImgThumb.setDrawingCacheEnabled(false);
                intent.putExtra(Constant.INTENT_TYPE_DATA, HomeActivity.TYPE_FACEBOOK);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
        Glide.with(mContext)
            .load(mListImages.get(position))
            .into(mMyViewHolder.mImgThumb);
        return convertView;
    }

    private class MyViewHolder {
        private ImageView mImgThumb;
    }
}
