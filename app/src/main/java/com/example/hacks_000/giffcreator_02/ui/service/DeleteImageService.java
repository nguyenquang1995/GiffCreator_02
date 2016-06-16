package com.example.hacks_000.giffcreator_02.ui.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.hacks_000.giffcreator_02.data.model.Constant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by framgia on 21/06/2016.
 */
public class DeleteImageService extends Service {
    List mListImages = new ArrayList();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mListImages = intent.getStringArrayListExtra(Constant.INTENT_DATA);
        int length = mListImages.size();
        for (int i = 0; i < length; i++) {
            File file = new File((String) mListImages.get(i));
            if (file.exists()) {
                getApplicationContext().deleteFile(file.getName());
            }
        }
        return START_STICKY;
    }
}
