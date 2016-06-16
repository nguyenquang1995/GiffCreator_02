package com.example.hacks_000.giffcreator_02.ui.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hacks_000.giffcreator_02.R;
import com.example.hacks_000.giffcreator_02.data.model.Constant;
import com.example.hacks_000.giffcreator_02.ui.adapter.ListImageAdapter;
import com.example.hacks_000.giffcreator_02.ui.mylistener.MyOnClickListener;
import com.example.hacks_000.giffcreator_02.ui.service.DeleteImageService;
import com.example.hacks_000.giffcreator_02.util.GifEncoder;
import com.example.hacks_000.giffcreator_02.util.ImageUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GifPreviewActivity extends AppCompatActivity implements MyOnClickListener {
    public static final int ADD_IMAGE_REQUEST_CODE = 3;
    public static final int MAX_FRAME = 10;
    public static final int TYPE_LIBRARY = 0;
    public static final int TYPE_FACEBOOK = 1;
    public static final int TYPE_CAMERA = 2;
    private static final int TIME_DELAY = 500;
    private static final int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 1;
    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 2;
    private static final int MY_PERMISSIONS_USE_CAMERA = 3;
    private static final int PICK_IMAGE_REQUEST_CODE = 1;
    private static final int CAPTURE_IMAGE_REQUEST_CODE = 2;
    private static final String PICK_IMAGE_TYPE = "image/*";
    private static final String PICK_IMAGE_TITLE = "Select Picture";
    private RecyclerView mRecyclerview;
    private ListImageAdapter mListImageAdapter;
    private List mListImages;
    private ImageView mImagePreviewGif;
    private int mIndex;
    private Uri mCaptureImageUri;
    private int mPositionInsert;
    private Toolbar mToolbar;
    private ProgressDialog mProgressDialog;
    private boolean mIsTimerCreated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif_preview);
        init();
        findView();
    }

    private void init() {
        mIndex = 0;
        mListImages = new ArrayList<String>();
        mListImages.add(getIntent().getStringExtra(Constant.INTENT_DATA));
        mProgressDialog = new ProgressDialog(GifPreviewActivity.this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setTitle(R.string.string_exporting);
    }

    private void findView() {
        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("");
        mRecyclerview = (RecyclerView) findViewById(R.id.rv_list_image);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerview.setLayoutManager(linearLayoutManager);
        mListImageAdapter = new ListImageAdapter(getApplicationContext(), mListImages);
        mRecyclerview.setAdapter(mListImageAdapter);
        mListImageAdapter.setOnItemClickListener(this);
        mImagePreviewGif = (ImageView) findViewById(R.id.gif_preview);
        Glide.with(getApplicationContext()).load(new File((String) mListImages.get(mIndex)))
            .into(mImagePreviewGif);
        playGifPreview();
    }

    private void playGifPreview() {
        if (mListImages.size() > 1 && !mIsTimerCreated) {
            mIsTimerCreated = true;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    new PlayGifAsynTask().execute();
                }
            }, TIME_DELAY);
        }
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GifPreviewActivity.this);
        builder.setItems(R.array.item_choose_images, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case TYPE_LIBRARY:
                        handlePickPhotoClick();
                        break;
                    case TYPE_FACEBOOK:
                        break;
                    case TYPE_CAMERA:
                        checkPermissionAndTakePhoto();
                        break;
                }
            }
        });
        builder.setCancelable(true);
        builder.show();
    }

    private void checkPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(GifPreviewActivity.this, Manifest.permission
            .CAMERA) == PackageManager.PERMISSION_GRANTED) {
            captureAndGetFullSizeImage();
            return;
        }
        if (ContextCompat.checkSelfPermission(GifPreviewActivity.this, Manifest.permission
            .CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GifPreviewActivity.this, new String[]
                    {Manifest.permission.CAMERA},
                MY_PERMISSIONS_USE_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_USE_CAMERA && grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            captureAndGetFullSizeImage();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
            try {
                checkWriteExternalPermissionAndGetTakenPhoto();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE) {
            Intent intent = new Intent(GifPreviewActivity.this, EditImageActivity.class);
            String imagePath = ImageUtil.getPath(GifPreviewActivity.this, data);
            intent.putExtra(Constant.INTENT_TYPE_DATA, TYPE_LIBRARY);
            intent.putExtra(Constant.INTENT_DATA, imagePath);
            intent.putExtra(Constant.INTENT_TYPE_START, true);
            startActivityForResult(intent, ADD_IMAGE_REQUEST_CODE);
            return;
        }
        if (requestCode == ADD_IMAGE_REQUEST_CODE && data != null) {
            mListImages.add(data.getStringExtra(Constant.INTENT_DATA));
            mListImageAdapter.notifyItemInserted(mPositionInsert);
            playGifPreview();
        }
    }

    private void checkWriteExternalPermissionAndGetTakenPhoto() throws IOException {
        if (ContextCompat.checkSelfPermission(GifPreviewActivity.this, Manifest.permission
            .WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            getMoreImage();
        }
        if (ContextCompat
            .checkSelfPermission(GifPreviewActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GifPreviewActivity.this, new String[]
                    {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
        }
    }

    private void getMoreImage() {
        Intent intent = new Intent(GifPreviewActivity.this, EditImageActivity.class);
        intent.putExtra(Constant.INTENT_TYPE_DATA, TYPE_CAMERA);
        intent.putExtra(Constant.INTENT_DATA, mCaptureImageUri.toString());
        intent.putExtra(Constant.INTENT_TYPE_START, true);
        startActivityForResult(intent, ADD_IMAGE_REQUEST_CODE);
    }

    private void handlePickPhotoClick() {
        Intent intent = new Intent();
        intent.setType(PICK_IMAGE_TYPE);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, PICK_IMAGE_TITLE),
            PICK_IMAGE_REQUEST_CODE);
    }

    private void captureAndGetFullSizeImage() {
        Calendar cal = Calendar.getInstance();
        File file =
            new File(Environment.getExternalStorageDirectory(), (cal.getTimeInMillis() + ".jpg"));
        mCaptureImageUri = Uri.fromFile(file);
        Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, mCaptureImageUri);
        startActivityForResult(i, CAPTURE_IMAGE_REQUEST_CODE);
    }

    @Override
    public void onItemClick(View view, int position) {
        if (mListImages.size() > MAX_FRAME) {
            Toast.makeText(this, R.string.string_need_more, Toast.LENGTH_SHORT).show();
        } else {
            mPositionInsert = position + 1;
            showDialog();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gif_preview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.export:
                if (mListImages.size() > 1) {
                    ExportGifAsynTask exportGifAsynTask = new ExportGifAsynTask();
                    exportGifAsynTask.execute();
                } else {
                    Toast.makeText(this, R.string.string_need_more, Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public byte[] generateGIF() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GifEncoder encoder = new GifEncoder();
        encoder.start(bos);
        for (int i = 0; i < mListImages.size(); i++) {
            Bitmap bitmap =
                ImageUtil.decodeBitmapFromPath((String) mListImages.get(i), ImageUtil.LOW_IMAGE,
                    ImageUtil.LOW_IMAGE);
            encoder.addFrame(bitmap);
            bitmap.recycle();
        }
        encoder.finish();
        return bos.toByteArray();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(GifPreviewActivity.this, DeleteImageService.class);
        intent.putStringArrayListExtra(Constant.INTENT_DATA, (ArrayList) mListImages);
        startService(intent);
    }

    private class PlayGifAsynTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mIndex++;
            if (mIndex == mListImages.size()) {
                mIndex = 0;
            }
            Glide.with(getApplicationContext())
                .load(new File((String) mListImages.get(mIndex)))
                .into(mImagePreviewGif);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    new PlayGifAsynTask().execute();
                }
            }, TIME_DELAY);
        }
    }

    private class ExportGifAsynTask extends AsyncTask<Void, Boolean, Boolean> {
        @Override
        protected void onPreExecute() {
            mProgressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean isSuccess = true;
            try {
                String gifImageName = java.text.DateFormat.getDateTimeInstance().format(Calendar
                    .getInstance().getTime());
                FileOutputStream outStream = new FileOutputStream(
                    Environment.getExternalStorageDirectory() + "/" + gifImageName + ".gif");
                outStream.write(generateGIF());
                outStream.close();
            } catch (Exception e) {
                isSuccess = false;
            } catch (OutOfMemoryError e) {
                isSuccess = false;
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), isSuccess ? R.string.string_export_sucess : R
                    .string.string_export_fail,
                Toast.LENGTH_SHORT).show();
        }
    }
}
