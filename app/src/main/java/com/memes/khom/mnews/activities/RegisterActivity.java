package com.memes.khom.mnews.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.memes.khom.memsnews.R;
import com.memes.khom.mnews.models.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    private EditText fullName, emailId,
            password, confirmPassword;
    private TextView login;
    private LinearLayout lay_reg;
    private Button signUpButton;
    public static final String regEx = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}\\b";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initViews();
        setListeners();
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // Set Listeners
    private void setListeners() {
        signUpButton.setOnClickListener(this);
        login.setOnClickListener(this);
    }

    private void initViews() {
        fullName = findViewById(R.id.fullName);
        emailId = findViewById(R.id.userEmailId);
        password = findViewById(R.id.password);
        lay_reg = findViewById(R.id.lay_reg);
        confirmPassword = findViewById(R.id.confirmPassword);
        signUpButton = findViewById(R.id.signUpBtn);
        login = findViewById(R.id.already_user);
        //  terms_conditions = findViewById(R.id.terms_conditions);
        @SuppressLint("ResourceType")
        XmlResourceParser xrp = getResources().getXml(R.drawable.text_selector);
        try {
            ColorStateList csl = ColorStateList.createFromXml(getResources(), xrp);
            login.setTextColor(csl);
            //   terms_conditions.setTextColor(csl);
        } catch (Exception e) {
            e.printStackTrace();
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

    // Check Validation Method
    private void checkValidation() {

        // Get all edittext texts
        String getFullName = fullName.getText().toString();
        String getEmailId = emailId.getText().toString();
        String getPassword = password.getText().toString();
        String getConfirmPassword = confirmPassword.getText().toString();

        // Pattern match for email id
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(getEmailId);

        // Check if all strings are null or not
        if (getFullName.equals("") || getFullName.length() == 0
                || getEmailId.equals("") || getEmailId.length() == 0
                || getPassword.equals("") || getPassword.length() == 0
                || getConfirmPassword.equals("")
                || getConfirmPassword.length() == 0)

            new CustomToast().Show_Toast(this, lay_reg,
                    "All fields are required.");

            // Check if email id valid or not
        else if (!m.find())
            new CustomToast().Show_Toast(this, lay_reg,
                    "Your Email Id is Invalid.");

            // Check if both password should be equal
        else if (!getConfirmPassword.equals(getPassword))
            new CustomToast().Show_Toast(this, lay_reg,
                    "Both password doesn't match.");

            // Make sure user should check Terms and Conditions checkbox
            //   else if (!terms_conditions.isChecked())
            //       new CustomToast().Show_Toast(this, lay_reg,
            //              "Please select Terms and Conditions.");

            // Else do signup or do your stuff
        else {

            showProgressDialog();
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(getEmailId, getPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d("Create USER", "createUser:onComplete:" + task.isSuccessful());
                            hideProgressDialog();
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = task.getResult().getUser();
                                writeNewUser(firebaseUser.getUid(), fullName.getText().toString(),
                                        firebaseUser.getEmail(), firebaseUser.getPhotoUrl() != null ?
                                                firebaseUser.getPhotoUrl().toString() : "", firebaseUser);
                            } else {
                                Toast.makeText(RegisterActivity.this, "Sign Up Failed",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


    private void writeNewUser(String userId, String name, String email, String uriPhoto, FirebaseUser fuser) {
        try {
            User user = new User(name, email, uriPhoto);
            FirebaseDatabase.getInstance().getReference().child("users").child(userId).setValue(user);

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();

            fuser.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                startActivity(new Intent(RegisterActivity.this, StartActivity.class));
                                finish();
                            }
                        }
                    });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signUpBtn:
                checkValidation();
                break;

            case R.id.already_user:
                onBackPressed();
                break;
        }

    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public class CustomToast {

        // Custom Toast Method
        void Show_Toast(Context context, View view, String error) {

            // Layout Inflater for inflating custom view
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // inflate the layout over view
            View layout;
            if (inflater != null) {
                layout = inflater.inflate(R.layout.custom_toast,
                        (ViewGroup) view.findViewById(R.id.toast_root));


                // Get TextView id and set error
                TextView text = layout.findViewById(R.id.toast_error);
                text.setText(error);
                Toast toast = new Toast(context);// Get Toast Context
                toast.setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 0);// Set
                toast.setDuration(Toast.LENGTH_SHORT);// Set Duration
                toast.setView(layout); // Set Custom View over toast

                toast.show();// Finally show toast
            }
        }

    }


}
