package com.example.hacks_000.giffcreator_02.ui.widget;

import android.content.Context;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.WebView;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by hacks_000 on 6/2/2016.
 */
public class GifView extends WebView {
    private Context mContext;
    private String mPath;

    public GifView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public void setGifImage(String path) {
        mPath = path;
        loadUrl(path);
    }

    public void scaleWebView() throws IOException {
        FileInputStream gifInputStream = new FileInputStream(mPath);
        Movie gifMovie = Movie.decodeStream(gifInputStream);
        Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        int width = display.getWidth();
        Double a = new Double(gifMovie.width());
        Double val = new Double(width) / a;
        val = val * 100d;
        setInitialScale(val.intValue());
        gifInputStream.close();
    }
}
