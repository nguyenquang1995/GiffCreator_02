package com.example.hacks_000.giffcreator_02.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.hacks_000.giffcreator_02.R;
import com.example.hacks_000.giffcreator_02.ui.mylistener.MyOnClickListener;

import java.io.File;
import java.util.List;

/**
 * Created by framgia on 17/06/2016.
 */
public class ListGifAdapter extends RecyclerView.Adapter<ListGifAdapter.GifViewHolder> {
    public List mListGifs;
    private Context mContext;
    private MyOnClickListener mMyOnClickListener;

    public ListGifAdapter(Context context, List listGifs) {
        mContext = context;
        mListGifs = listGifs;
    }

    @Override
    public GifViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gif, parent, false);
        return new GifViewHolder(view, mMyOnClickListener);
    }

    @Override
    public void onBindViewHolder(GifViewHolder holder, int position) {
        holder.mPosition = position;
        File file = new File((String) mListGifs.get(position));
        Glide.with(mContext)
            .load(mListGifs.get(position))
            .asBitmap()
            .into(holder.mGifView);
    }

    @Override
    public int getItemCount() {
        return mListGifs.size();
    }

    public void setOnItemClickListener(MyOnClickListener listener) {
        this.mMyOnClickListener = listener;
    }

    public class GifViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView mGifView;
        private int mPosition;
        private MyOnClickListener mMyOnClickListener;

        public GifViewHolder(View itemView, MyOnClickListener listener) {
            super(itemView);
            mGifView = (ImageView) itemView.findViewById(R.id.gif_item);
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
