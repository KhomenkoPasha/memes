package com.memes.khom.mnews.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

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
import com.memes.khom.memsnews.R;
import com.memes.khom.mnews.models.Category;
import com.memes.khom.mnews.models.GlideApp;
import com.memes.khom.mnews.models.Post;
import com.memes.khom.mnews.models.User;
import com.memes.khom.mnews.utils.ImageUtils;
import com.mrgames13.jimdo.drawingactivity.DrawingActivity;
import com.mrgames13.jimdo.drawingactivity.DrawingActivityBuilder;
import com.rilixtech.materialfancybutton.MaterialFancyButton;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DrawActivity extends BaseActivity implements View.OnClickListener {

    //Constants
    private final int REQ_DRAWING = 10001;
    private ImageView mIVpicture;
    private String mImageUriPath;
    private DatabaseReference mDatabase;
    private DatabaseReference categRef;
    private StorageReference mStorageRef;
    private EditText mTitleField;
    private EditText mBodyField;
    private SearchableSpinner catSpinner;
    private FloatingActionButton mSubmitButton;
    private String mReference = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        mIVpicture = findViewById(R.id.iv_piture);

        DrawingActivityBuilder.getInstance(this)
                .draw(REQ_DRAWING);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        try {
            mStorageRef = FirebaseStorage.getInstance().getReference();
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mIVpicture = findViewById(R.id.iv_piture);
            MaterialFancyButton mBTNadaPicture = findViewById(R.id.btn_add_picture);

            mBTNadaPicture.setOnClickListener(this);

            mTitleField = findViewById(R.id.field_title);
            mTitleField.setText(R.string.tag_symbol);
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

            mBodyField = findViewById(R.id.field_body);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(DrawActivity.this);
                    builder.setTitle(R.string.add_cat);
                    // I'm using fragment here so I'm using getView() to provide ViewGroup
                    // but you can provide here any other instance of ViewGroup from your Fragment / Activity
                    View viewInflated = LayoutInflater.from(DrawActivity.this)
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
                                        Toast.makeText(DrawActivity.this, R.string.error_add_cat, Toast.LENGTH_SHORT).show();
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

    private void writeNewPost(String userId, String username, String title, String body, int type) {

        String key = mDatabase.child("posts").push().getKey();
        String cat = "";
        if (catSpinner.getSelectedItem() != null) cat = catSpinner.getSelectedItem().toString();
        if (title.length() < 2)
            title = "";
        if (mImageUriPath != null) {
            Post post = new Post(userId, username, title, body, cat, type, "");
            Map<String, Object> postValues = post.toMap();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/posts/" + key, postValues);
            childUpdates.put("/user-posts/" + userId + "/" + key, postValues);
            mDatabase.updateChildren(childUpdates);
            mReference = key;
            uploadFileInFireBaseStorage(mImageUriPath);
        }

    }


    public void uploadFileInFireBaseStorage(String uri) {


        UploadTask uploadTask = mStorageRef.child("images/" + mReference).putFile(Uri.fromFile(new File(mImageUriPath)));

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred());
                Log.i("Load", "Upload is " + progress + "% done");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(DrawActivity.this, R.string.loaded, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DrawActivity.this, StartActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                android.os.Process.killProcess(android.os.Process.myPid());
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

        if (mIVpicture == null) {
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
                            Toast.makeText(DrawActivity.this,
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
                        setEditingEnabled(true);
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_DRAWING && resultCode == RESULT_OK && data != null) {
            // Toast.makeText(this, drawing_path, Toast.LENGTH_LONG).show();
            mImageUriPath = data.getStringExtra(DrawingActivity.DRAWING_PATH);
            ImageUtils.compressAndRotatePhotoTemp(mImageUriPath, mImageUriPath);

            GlideApp.with(getBaseContext())
                    .load(mImageUriPath)
                    .skipMemoryCache(true)
                    .into(mIVpicture);

        }
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
        ArrayAdapter arrayAdapter = new ArrayAdapter<>(DrawActivity.this, R.layout.item_sp, cats);
        catSpinner.setAdapter(arrayAdapter);

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

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_add_picture) {
            DrawingActivityBuilder.getInstance(this)
                    .draw(REQ_DRAWING);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
