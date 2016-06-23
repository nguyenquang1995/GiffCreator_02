package com.example.hacks_000.giffcreator_02.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.hacks_000.giffcreator_02.R;
import com.example.hacks_000.giffcreator_02.ui.mylistener.MyOnClickListener;

import java.util.List;

/**
 * Created by framgia on 27/06/2016.
 */
public class ImageFacebookAdapter
    extends RecyclerView.Adapter<ImageFacebookAdapter.FacebookImageViewHolder> {
    public List<String> mListImages;
    private Context mContext;
    private MyOnClickListener mMyOnClickListener;

    public ImageFacebookAdapter(Context context, List listImages) {
        mContext = context;
        mListImages = listImages;
    }

    public void setOnItemClickListener(MyOnClickListener listener) {
        this.mMyOnClickListener = listener;
    }

    @Override
    public FacebookImageViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        return new FacebookImageViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_facebook_image, parent, false),
            mMyOnClickListener);
    }

    @Override
    public void onBindViewHolder(FacebookImageViewHolder holder,
                                 int position) {
        holder.mPosition = position;
        Glide.with(mContext)
            .load(mListImages.get(position))
            .diskCacheStrategy(DiskCacheStrategy.RESULT)
            .into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mListImages.size();
    }

    public class FacebookImageViewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener {
        public ImageView mImageView;
        private int mPosition;
        private MyOnClickListener mMyOnClickListener;

        public FacebookImageViewHolder(View itemView, MyOnClickListener listener) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.facebook_image);
            mImageView.setOnClickListener(this);
            mMyOnClickListener = listener;
        }

        @Override
        public void onClick(View v) {
            if (mMyOnClickListener != null) {
                mMyOnClickListener.onItemClick(v, mPosition);
            }
        }
    }
}
