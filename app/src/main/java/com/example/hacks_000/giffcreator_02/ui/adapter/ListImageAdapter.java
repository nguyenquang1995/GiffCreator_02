package com.example.hacks_000.giffcreator_02.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.hacks_000.giffcreator_02.R;
import com.example.hacks_000.giffcreator_02.ui.mylistener.MyOnClickListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * Created by framgia on 16/06/2016.
 */
public class ListImageAdapter extends RecyclerView.Adapter<ListImageAdapter.ImageViewHolder> {
    private List<String> mListImages;
    private Context mContext;
    private MyOnClickListener mMyOnClickListener;

    public ListImageAdapter(Context context, List listImages) {
        mContext = context;
        mListImages = listImages;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view, mMyOnClickListener);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        holder.mPosition = position;
        Picasso.with(mContext).load(new File(mListImages.get(position))).into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mListImages.size();
    }

    public void setOnItemClickListener(MyOnClickListener listener) {
        this.mMyOnClickListener = listener;
    }

    class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private int mPosition;
        private ImageView mImageView;
        private MyOnClickListener mMyOnClickListener;

        public ImageViewHolder(View itemView, MyOnClickListener listener) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.image_in_list);
            itemView.setOnClickListener(this);
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
