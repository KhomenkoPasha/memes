package com.memes.khom.mnews.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.memes.khom.mnews.R;
import com.memes.khom.mnews.utils.ImageUtils;
import com.rilixtech.materialfancybutton.MaterialFancyButton;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.memes.khom.mnews.utils.ImageUtils.getRealPathFromURI;


public class UserProfile extends AppCompatActivity {

    private MaterialFancyButton btn_edit_save;
    private static final int REQUEST_CODE_PERMISSION_RECEIVE_CAMERA = 102;
    private static final int REQUEST_CODE_TAKE_PHOTO = 103;
    private File mTempPhoto;
    private ImageView imgPhoto;
    private TextView nameView;
    private Uri mageUri;
    private String mImageUri = "";
    private FirebaseUser user;
    private String oldUserName = "";
    private boolean setNewNameEqualsOld;
    private SpinKitView loadIndic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        user = FirebaseAuth.getInstance().getCurrentUser();
        nameView = findViewById(R.id.nameAndSurname);
        loadIndic = findViewById(R.id.spin_kit);
        TextView email = findViewById(R.id.email);
        TextView number = findViewById(R.id.number);
        MaterialFancyButton btn_edit_picture = findViewById(R.id.btn_edit_picture);
        imgPhoto = findViewById(R.id.profileImage);
        btn_edit_save = findViewById(R.id.btn_edit_save);

        btn_edit_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPhoto();
            }
        });
        btn_edit_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadIndic.setVisibility(View.VISIBLE);
                btn_edit_save.setVisibility(View.GONE);
                if (mageUri != null)
                    uploadFileInFireBaseStorage(mageUri);
                else if (!setNewNameEqualsOld) updateUserName();
            }
        });


        if (user != null) {
            this.setTitle(user.getDisplayName());
            nameView.setText(user.getDisplayName());
            oldUserName = user.getDisplayName();
            email.setText(user.getEmail());
            number.setText(user.getPhoneNumber());
            if (user.getPhotoUrl() != null)
                Picasso.with(this).load(user.getPhotoUrl()).into(imgPhoto);
            nameView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    setNewNameEqualsOld = oldUserName.equals(charSequence);
                    if (!setNewNameEqualsOld) btn_edit_save.setVisibility(View.VISIBLE);
                    else
                        btn_edit_save.setVisibility(View.GONE);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }
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

    //Метод для добавления фото
    private void addPhoto() {

        //Проверяем разрешение на работу с камерой
        boolean isCameraPermissionGranted = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        //Проверяем разрешение на работу с внешнем хранилещем телефона
        boolean isWritePermissionGranted = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        //Если разрешения != true
        if (!isCameraPermissionGranted || !isWritePermissionGranted) {
            String[] permissions;//Разрешения которые хотим запросить у пользователя
            if (!isCameraPermissionGranted && !isWritePermissionGranted) {
                permissions = new String[]{android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            } else if (!isCameraPermissionGranted) {
                permissions = new String[]{android.Manifest.permission.CAMERA};
            } else {
                permissions = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            }
            //Запрашиваем разрешения у пользователя
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSION_RECEIVE_CAMERA);
        } else {
            //Если все разрешения получены
            try {

                List<Intent> intentList = new ArrayList<>();
                mTempPhoto = NewPostActivity.createTempImageFile(getExternalCacheDir());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    try{
                        Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                        m.invoke(null);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    mImageUri = (FileProvider.getUriForFile(UserProfile.this,
                            UserProfile.this.getPackageName() + ".fileProv", mTempPhoto)).getPath();

                } else {
                    mImageUri = mTempPhoto.getAbsolutePath();
                }

                Intent chooserIntent = null;
                Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePhotoIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTempPhoto));
                intentList = NewPostActivity.addIntentsToList(this, intentList, pickIntent);
                intentList = NewPostActivity.addIntentsToList(this, intentList, takePhotoIntent);

                if (!intentList.isEmpty()) {
                    chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),getString(R.string.chose_img));
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
                }
                startActivityForResult(chooserIntent, REQUEST_CODE_TAKE_PHOTO);
            } catch (IOException e) {
                Log.e("ERROR", e.getMessage(), e);
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (data != null && data.getData() != null) {

                        mImageUri = getRealPathFromURI(data.getData(), UserProfile.this);
                        ImageUtils.compressAndRotatePhotoTemp(mImageUri, mTempPhoto.getAbsolutePath());

                        Picasso.with(getBaseContext())
                                .load(mTempPhoto)
                                .into(imgPhoto);

                        mageUri = Uri.fromFile(mTempPhoto);
                        btn_edit_save.setVisibility(View.VISIBLE);

                    } else if (mImageUri != null) {
                        mImageUri = Uri.fromFile(mTempPhoto).toString();
                        ImageUtils.compressAndRotatePhoto(mTempPhoto.getAbsolutePath());
                        Picasso.with(this)
                                .load(mImageUri)
                                .into(imgPhoto);

                        mageUri = Uri.fromFile((mTempPhoto));
                        btn_edit_save.setVisibility(View.VISIBLE);
                    }
                }
                break;
        }
    }


    private void updateUserName() {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(nameView.getText().toString())
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            btn_edit_save.setVisibility(View.GONE);
                            nameView.setEnabled(false);
                            loadIndic.setVisibility(View.GONE);
                            Toast.makeText(UserProfile.this, R.string.userNameUpd, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    public void uploadFileInFireBaseStorage(Uri uri) {
        if (uri != null) {
            UploadTask uploadTask = FirebaseStorage.getInstance().getReference().child("usersPhoto/" + user.getUid()).putFile(uri);
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred());
                    Log.i("Load", "Upload is " + progress + "% done");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri donwoldUri = taskSnapshot.getMetadata().getDownloadUrl();
                    if (donwoldUri != null) {
                        FirebaseDatabase.getInstance().getReference().child("users")
                                .child(user.getUid()).child("uriPhoto").setValue(donwoldUri.toString());

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(donwoldUri)
                                .setDisplayName(nameView.getText().toString())
                                .build();

                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(UserProfile.this, R.string.update_us_name, Toast.LENGTH_SHORT).show();
                                            btn_edit_save.setVisibility(View.GONE);
                                            loadIndic.setVisibility(View.GONE);
                                            nameView.setEnabled(false);
                                        }
                                    }
                                });
                    }
                }
            });
        }
    }

}