package com.memes.khom.mnews;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


public class PictureActivity extends AppCompatActivity {

    public static final String PHOTO_URL = "photo_url";
    private PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_edit);

        try {
            Bundle extras = getIntent().getExtras();
            final ImageView mImageView = findViewById(R.id.imagePhotoView);
            TextView pict_tag =  findViewById(R.id.pict_tag);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            this.setTitle("Picture");
            Uri uri = null;
            if (extras != null) {
                uri = extras.getParcelable(PHOTO_URL);
            }
            if (mImageView != null && uri != null) {

                Callback imageLoadedCallback = new Callback() {

                    @Override
                    public void onSuccess() {
                        if (mAttacher != null) {
                            mAttacher.update();
                        } else {
                            mAttacher = new PhotoViewAttacher(mImageView);
                        }
                    }

                    @Override
                    public void onError() {
                        // TODO Auto-generated method stub
                    }
                };

                Picasso.with(this).load(uri).fit().into(mImageView, imageLoadedCallback);
              //  pict_tag.setText(uri.getEncodedPath());
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

