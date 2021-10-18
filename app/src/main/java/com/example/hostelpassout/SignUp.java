package com.example.hostelpassout;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.parceler.Parcel;
import org.parceler.Parcels;

import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUp extends AppCompatActivity {

    private static final String TAG = "mytag";
    private CircleImageView imageView;
    private AutoCompleteTextView course;
    private EditText edtEmail, edtPassword, edtName, edtPhone;
    TextInputLayout courseList;
    RadioGroup radioGroupHostel;
    RadioButton radioButtonHostel;
    private Button signUpBtn;
    private Uri profilePic;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_layout);

        pd = new ProgressDialog(this);
        pd.setTitle("Loading!");
        pd.setMessage("Data Adding to DB. Please wait!");

        imageView = findViewById(R.id.profile_image);

        init();

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.show();
                pd.setCanceledOnTouchOutside(false);
                FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();


                StorageReference storageReference = firebaseStorage.getReference("image" + new Date().toString());
                storageReference.putFile(profilePic)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        createUser(uri);
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                    }
                });


                int radioButtonId = radioGroupHostel.getCheckedRadioButtonId();
                radioButtonHostel = findViewById(radioButtonId);
                Log.d(TAG, "onClick hostel: " + radioButtonHostel.getText());

            }
        });
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        course = findViewById(R.id.autoCompleteTextViewCourse);


        String[] courseList = getResources().getStringArray(R.array.course);
        ArrayAdapter<String> cadp = new ArrayAdapter<>(getApplicationContext(), R.layout.dropdown_item, courseList);
        course.setAdapter(cadp);

        // pick image from gallery
        ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if (result != null) {
                    imageView.setImageURI(result);
                    profilePic = result;
                    Log.d(TAG, "onActivityResult: " + result.toString());
                }


            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetContent.launch("image/*");
            }
        });

    }


    void init() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        signUpBtn = findViewById(R.id.btnSignUp);
        courseList = findViewById(R.id.textInputLayoutCourse);
        radioGroupHostel = findViewById(R.id.radioGroupHostel);
    }

    void createUser(Uri uri) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(edtEmail.getText().toString(),
                edtPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                addDataIntoFireStore(authResult, uri);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                pd.setMessage(e.getMessage());
                Log.d(TAG, "onFailure: " + "falied" + e.getMessage());
            }
        });


    }

    void addDataIntoFireStore(AuthResult authResult, Uri uri) {

        String userid = authResult.getUser().getUid();

        Student student = new Student(edtName.getText().toString(),
                edtEmail.getText().toString(),
                edtPhone.getText().toString(),
                edtPassword.getText().toString(),
                "notAllow",
                uri.toString(), courseList.getEditText().getText().toString(),
                radioButtonHostel.getText().toString(), userid);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("students").document(userid).set(student).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                pd.dismiss();
                Intent intent = new Intent(SignUp.this,StudentActivity.class);
                startActivity(intent);
                finish();

        }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
            }
        });
        Log.d(TAG, "onSuccess: " + "Created");

    }
}