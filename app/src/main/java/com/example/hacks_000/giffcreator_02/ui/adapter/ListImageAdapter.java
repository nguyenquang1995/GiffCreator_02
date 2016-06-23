package com.example.hacks_000.giffcreator_02.ui.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.hacks_000.giffcreator_02.R;
import com.example.hacks_000.giffcreator_02.data.model.Constant;
import com.example.hacks_000.giffcreator_02.ui.mylistener.MyOnClickListener;
import com.joanfuentes.hintcase.HintCase;
import com.joanfuentes.hintcase.RectangularShape;
import com.joanfuentes.hintcaseassets.hintcontentholders.SimpleHintContentHolder;
import com.joanfuentes.hintcaseassets.shapeanimators.RevealRectangularShapeAnimator;
import com.joanfuentes.hintcaseassets.shapeanimators.UnrevealRectangularShapeAnimator;

import java.io.File;
import java.util.List;

/**
 * Created by framgia on 16/06/2016.
 */
public class ListImageAdapter extends RecyclerView.Adapter<ListImageAdapter.ImageViewHolder> {
    public List<String> mListImages;
    private Context mContext;
    private MyOnClickListener mMyOnClickListener;

    public ListImageAdapter(Context context, List listImages) {
        mContext = context;
        mListImages = listImages;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false),
            mMyOnClickListener);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        holder.mPosition = position;
        holder.launchAutomaticHint();
        Glide.with(mContext)
            .load(new File(mListImages.get(position)))
            .diskCacheStrategy(DiskCacheStrategy.RESULT)
            .into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mListImages.size();
    }

    public void setOnItemClickListener(MyOnClickListener listener) {
        this.mMyOnClickListener = listener;
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView mImageView;
        private int mPosition;
        private MyOnClickListener mMyOnClickListener;

        public ImageViewHolder(View itemView, MyOnClickListener listener) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.image_in_list);
            itemView.setOnClickListener(this);
            mMyOnClickListener = listener;
        }

        private void launchAutomaticHint() {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences("myPreference",
                Context.MODE_PRIVATE);
            sharedPreferences.getBoolean(mContext.getString(R.string.is_new_install_gif_preview),
                false);
            if (mPosition == 0 && !sharedPreferences.getBoolean(mContext.getString(R.string
                    .is_new_install_gif_preview),
                false)) {
                final View view = mImageView;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (view != null) {
                            SimpleHintContentHolder blockInfo =
                                new SimpleHintContentHolder.Builder(view.getContext())
                                    .setContentText(R.string.text_hint_add_more_image)
                                    .setContentStyle(R.style.content_light)
                                    .setMarginByResourcesId(R.dimen.activity_vertical_margin,
                                        R.dimen.activity_horizontal_margin,
                                        R.dimen.activity_vertical_margin,
                                        R.dimen.activity_horizontal_margin)
                                    .build();
                            new HintCase(view.getRootView())
                                .setTarget(view, new RectangularShape())
                                .setShapeAnimators(new RevealRectangularShapeAnimator(),
                                    new UnrevealRectangularShapeAnimator())
                                .setHintBlock(blockInfo)
                                .show();
                        }
                    }
                }, Constant.TIME_DELAY_SHOW_HINT);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(mContext.getString(R.string.is_new_install_gif_preview), true);
                editor.commit();
            }
        }

        @Override
        public void onClick(View v) {
            if (mMyOnClickListener != null) {
                mMyOnClickListener.onItemClick(v, mPosition);
            }
        }
    }
}
