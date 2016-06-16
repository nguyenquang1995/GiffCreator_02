package com.example.hacks_000.giffcreator_02.util;

import android.os.AsyncTask;

import java.io.File;

/**
 * Created by framgia on 20/06/2016.
 */
public class DeleteFileAsynTask extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... params) {
        File file = new File(params[0]);
        if (file.exists()) {
            file.delete();
        }
        return null;
    }
}
