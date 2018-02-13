package com.memes.khom.mnews.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;

import com.memes.khom.mnews.R;
import com.memes.khom.mnews.utils.ImageUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


public class PictureActivity extends AppCompatActivity {
    public static final String PHOTO_URL = "photo_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_edit);
        try {
            WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            if (wm != null) {
                wm.getDefaultDisplay().getMetrics(metrics);
            }
            this.getWindow().setLayout(metrics.widthPixels - 60, (int)(metrics.heightPixels / 1.5));
            Bundle extras = getIntent().getExtras();
            final ImageView mImageView = findViewById(R.id.imagePhotoView);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            this.setTitle("Picture");
            Uri uri = null;
            if (extras != null) {
                uri = extras.getParcelable(PHOTO_URL);
            }
            if (mImageView != null && uri != null) {

                Target target = new Target() {

                    @Override
                    public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                        Bitmap originalPhoto = ImageUtils.getResizeFile(bitmap, 800);
                        mImageView.setImageBitmap(originalPhoto);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                };
                Picasso.with(this)
                        .load(uri)
                        .into(target);
                mImageView.setTag(target);

                if (getSupportActionBar() != null)
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //  getMenuInflater().inflate(R.menu.menu_photo_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                PictureActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}

