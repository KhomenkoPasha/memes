package com.memes.khom.mnews.activities;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.memes.khom.mnews.R;
import com.memes.khom.mnews.models.Category;
import com.memes.khom.mnews.models.GlideApp;
import com.memes.khom.mnews.models.Post;
import com.memes.khom.mnews.models.User;
import com.memes.khom.mnews.utils.ImageUtils;
import com.rilixtech.materialfancybutton.MaterialFancyButton;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.memes.khom.mnews.utils.ImageUtils.getRealPathFromURI;

public class NewPostActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "NewPostActivity";
    private static final int REQUEST_CODE_PERMISSION_RECEIVE_CAMERA = 102;
    private static final int REQUEST_CODE_TAKE_PHOTO = 103;

    private ImageView mIVpicture;
    private File mTempPhoto;
    private String mImageUriPath = "";
    private Uri imageUriToUpload;
    private String mReference = "";

    private DatabaseReference mDatabase;
    private DatabaseReference categRef;

    private StorageReference mStorageRef;
    private EditText mTitleField;
    private EditText mBodyField;
    private SearchableSpinner catSpinner;
    private FloatingActionButton mSubmitButton;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_new_post);

            adView = findViewById(R.id.ad_view);

            AdRequest ar = new AdRequest.Builder().addTestDevice("6EC851088930A5060EB4B112825D3241").build();
            adView.loadAd(ar);

            mStorageRef = FirebaseStorage.getInstance().getReference();
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mIVpicture = findViewById(R.id.iv_piture);
            MaterialFancyButton mBTNadaPicture = findViewById(R.id.btn_add_picture);

            mBTNadaPicture.setOnClickListener(this);
            mTitleField = findViewById(R.id.field_title);
            mTitleField.setText(R.string.tag_symbol);
            mTitleField.setImeOptions(EditorInfo.IME_ACTION_DONE);

            mTitleField.setSelection(mTitleField.getText().length());
            mTitleField.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                }

                @Override
                public void afterTextChanged(Editable text) {
                    if (text.length() == 0)
                        text.append(getResources().getString(R.string.tag_symbol));
                    if (text.length() > 0 && text.toString().contains(" ")) {
                        final String newText = text.toString().replace(" ", "");
                        mTitleField.setText(newText);
                        mTitleField.setSelection(newText.length());
                    }
                }
            });

            catSpinner = findViewById(R.id.searchableSpinnerCat);

            MaterialFancyButton add_cat_button = findViewById(R.id.add_cat_button);

            catSpinner.setTitle(getString(R.string.select_cat));
            catSpinner.setPositiveButton(getString(R.string.chose));
            // catSpinner.setBackgroundColor(getColor(R.color.white));

            mBodyField = findViewById(R.id.field_body);
            mBodyField.setImeOptions(EditorInfo.IME_ACTION_DONE);
            mSubmitButton = findViewById(R.id.fab_submit_post);
            mStorageRef = FirebaseStorage.getInstance().getReference();
            categRef = FirebaseDatabase.getInstance().getReference().child("categ");
            fillSpinnerCat();

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            mSubmitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    submitPost();
                }
            });

            this.setTitle(getString(R.string.create_post));

            add_cat_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(NewPostActivity.this);
                    builder.setTitle(R.string.add_cat);
                    // I'm using fragment here so I'm using getView() to provide ViewGroup
                    // but you can provide here any other instance of ViewGroup from your Fragment / Activity
                    View viewInflated = LayoutInflater.from(NewPostActivity.this)
                            .inflate(R.layout.text_input_string,
                                    (ViewGroup) findViewById(android.R.id.content), false);
                    // Set up the input
                    final EditText input = viewInflated.findViewById(R.id.input);
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    builder.setView(viewInflated);

                    // Set up the buttons
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String key = categRef.push().getKey();
                            Category categ = new Category();
                            categ.name = input.getText().toString();
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put(key, categ);

                            categRef.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                    if (databaseError != null) {
                                        Toast.makeText(NewPostActivity.this, R.string.error_add_cat, Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                }
            });


            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }


    @Override
    public void onStart() {
        super.onStart();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }


    private void fillSpinnerCat() {

        final List<String> cats = new ArrayList<>();
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Category ct = postSnapshot.getValue(Category.class);
                    if (ct != null)
                        if (!cats.contains(ct.name)) cats.add(ct.name);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        categRef.addValueEventListener(valueEventListener);
        ArrayAdapter arrayAdapter = new ArrayAdapter<>(NewPostActivity.this, R.layout.item_sp, cats);
        catSpinner.setAdapter(arrayAdapter);


        catSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                View view = NewPostActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

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

    public void removeSearchableDialog() {
        Fragment searchableSpinnerDialog = getFragmentManager().findFragmentByTag("TAG");
        if (searchableSpinnerDialog != null && searchableSpinnerDialog.isAdded()) {
            getFragmentManager().beginTransaction().remove(searchableSpinnerDialog).commit();
        }
    }


    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
        removeSearchableDialog();
    }

    private void submitPost() {
        final String title = mTitleField.getText().toString();
        final String body = mBodyField.getText().toString();

        // Title is required
        // if (title.length() < 2) {
        //     mTitleField.setError(REQUIRED);
        //     return;
        //  }

        // Cat is required
        //if (catSpinner.getSelectedItem() == null) {
        //   //  Toast.makeText(this, R.string.need_add_cat, Toast.LENGTH_SHORT).show();
        //      return;
        //   }

        if (imageUriToUpload == null) {
            Toast.makeText(this, R.string.add_image, Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);


        Toast.makeText(this, R.string.posting, Toast.LENGTH_SHORT).show();
        final String userId = getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(NewPostActivity.this,
                                    R.string.cannot_fetch,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new post
                            writeNewPost(userId, user.username, title, body, 0);
                        }
                        // Finish this Activity, back to the stream
                        setEditingEnabled(true);
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        setEditingEnabled(true);
                    }
                });
    }

    private void setEditingEnabled(boolean enabled) {
        mTitleField.setEnabled(enabled);
        mBodyField.setEnabled(enabled);
        if (enabled) {
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }
    }

    private void writeNewPost(String userId, String username, String title, String body, int type) {

        String key = mDatabase.child("posts").push().getKey();
        String cat = "";
        if (catSpinner.getSelectedItem() != null) cat = catSpinner.getSelectedItem().toString();
        if (title.length() < 2)
            title = "";
        Post post = new Post(userId, username, title, body, cat, type, "");
        Map<String, Object> postValues = post.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/" + key, postValues);
        mDatabase.updateChildren(childUpdates);
        mReference = key;
        uploadFileInFireBaseStorage(imageUriToUpload);

    }


    //Метод для добавления фото
    private void addPhoto() {
        try {
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
                    permissions = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
                } else if (!isCameraPermissionGranted) {
                    permissions = new String[]{android.Manifest.permission.CAMERA};
                } else {
                    permissions = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
                }
                //Запрашиваем разрешения у пользователя
                ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSION_RECEIVE_CAMERA);
            } else {
                //Если все разрешения получены
                //StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                //  StrictMode.setVmPolicy(builder.build());
                try {
                    List<Intent> intentList = new ArrayList<>();
                    mTempPhoto = createTempImageFile(getExternalCacheDir());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        try {
                            Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                            m.invoke(null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mImageUriPath = (FileProvider.getUriForFile(NewPostActivity.this,
                                NewPostActivity.this.getPackageName() + ".fileProv", mTempPhoto)).getPath();

                    } else {
                        mImageUriPath = mTempPhoto.getAbsolutePath();
                    }
                    //Создаём лист с интентами для работы с изображениями

                    Intent chooserIntent = null;
                    Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    takePhotoIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTempPhoto));
                    intentList = addIntentsToList(this, intentList, pickIntent);
                    intentList = addIntentsToList(this, intentList, takePhotoIntent);

                    if (!intentList.isEmpty()) {
                        chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1), getString(R.string.chose_img));
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
                    }
                /*После того как пользователь закончит работу с приложеним(которое работает с изображениями)
                 будет вызван метод onActivityResult
                */
                    startActivityForResult(chooserIntent, REQUEST_CODE_TAKE_PHOTO);
                } catch (IOException e) {
                    Log.e("ERROR", e.getMessage(), e);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
      File storageDir -  абсолютный путь к каталогу конкретного приложения на
      основном общем /внешнем устройстве хранения, где приложение может размещать
      файлы кеша, которыми он владеет.
     */
    public static File createTempImageFile(File storageDir) throws IOException {

        // Генерируем имя файла
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());//получаем время
        String imageFileName = "photo_" + timeStamp;//состовляем имя файла

        //Создаём файл
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    /*
    Метод для добавления интента в лист интентов
    */
    public static List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
        }
        return list;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (data != null && data.getData() != null) {

                        mImageUriPath = getRealPathFromURI(data.getData(), NewPostActivity.this);

                        if (!mImageUriPath.contains("gif")) {
                            ImageUtils.compressAndRotatePhotoTemp(mImageUriPath, mTempPhoto.getAbsolutePath());
                            GlideApp.with(getBaseContext())
                                    .load(mTempPhoto)
                                    .into(mIVpicture);

                            imageUriToUpload = Uri.fromFile(mTempPhoto);
                        } else {
                            GlideApp.with(getBaseContext())
                                    .asGif()
                                    .centerCrop()
                                    .load(mImageUriPath)
                                    .into(mIVpicture);

                            imageUriToUpload = Uri.fromFile(new File(mImageUriPath));
                        }

                    } else if (mImageUriPath != null) {

                        mImageUriPath = Uri.fromFile(mTempPhoto).toString();
                        ImageUtils.compressAndRotatePhoto(mTempPhoto.getAbsolutePath());

                        GlideApp.with(getBaseContext())
                                .load(mImageUriPath)
                                .into(mIVpicture);

                        imageUriToUpload = Uri.fromFile((mTempPhoto));


                    }
                }
                break;
        }
    }

    public void uploadFileInFireBaseStorage(Uri uri) {
        UploadTask uploadTask = mStorageRef.child("images/" + mReference).putFile(uri);
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred());
                Log.i("Load", "Upload is " + progress + "% done");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(NewPostActivity.this, R.string.loaded, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(NewPostActivity.this, StartActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_add_picture) {
            addPhoto();
        }
    }

}
