package com.memes.khom.mnews;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;


public class UserProfile extends AppCompatActivity {

    private ShareDialog shareDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        shareDialog = new ShareDialog(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        FloatingActionButton fab = findViewById(R.id.fab);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
               // ShareLinkContent content = new ShareLinkContent.Builder().build();
                //shareDialog.show(content);
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        TextView nameView = findViewById(R.id.nameAndSurname);
        TextView email = findViewById(R.id.email);
        TextView number = findViewById(R.id.number);

        this.setTitle(user.getDisplayName());
        nameView.setText(user.getDisplayName());
        email.setText(user.getEmail());
        number.setText(user.getPhoneNumber());
       // Button logout = findViewById(R.id.logout);
        /*
        logout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                LoginManager.getInstance().logOut();
                Intent login = new Intent(UserProfile.this, FaceebookRegAct.class);
                startActivity(login);
                finish();
            }
        });
*/
        Picasso.with(this).load(user.getPhotoUrl()).into((ImageView)findViewById(R.id.profileImage));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                this.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}