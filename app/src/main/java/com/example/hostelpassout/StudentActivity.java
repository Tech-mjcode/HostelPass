package com.example.hostelpassout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.zxing.WriterException;


import org.parceler.Parcels;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import de.hdodenhof.circleimageview.CircleImageView;

public class StudentActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {
    private static final String TAG = "mytag";
    FirebaseAuth firebaseAuth;
    Dialog dialog;
    FirebaseFirestore db;
    private String userId;
    CircleImageView imageView;
    TextView name, roomNo, branch, email, phoneNo;
    private Button txtLog, btnRequest, btnCheckStatus;
    String status;
    CardView cardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_layout);
        initdata();

        String jsonData = "{\"date\":\"17/10/2021\",\"studentId\":\"12345\",\"documentId\":\"00000\"}";

        //Final Data
        Gson gson = new Gson();
        QrData qrData = gson.fromJson(jsonData, QrData.class);
        Log.d(TAG, "onCreate: jsondat"+qrData.getDate());
        Log.d(TAG, "onCreate: jsondat"+qrData.getStudentId());
        Log.d(TAG, "onCreate: jsondat"+qrData.getStudentId());


        dialog = new Dialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = firebaseAuth.getUid();
        getStudentData();
        checkDateDb();
        btnCheckStatus.setOnClickListener(this::showStatus);

        txtLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                Intent intent = new Intent(StudentActivity.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void initdata() {
        txtLog = findViewById(R.id.Logout);
        name = findViewById(R.id.txtName);
        branch = findViewById(R.id.txtBranch);
        phoneNo = findViewById(R.id.txtPhone);
        email = findViewById(R.id.txtEmail);
        roomNo = findViewById(R.id.txtRoomNo);
        imageView = findViewById(R.id.circleImageViewProfile);
        btnRequest = findViewById(R.id.btnRequest);
        btnCheckStatus = findViewById(R.id.btnCheck);
        cardView = findViewById(R.id.cardShow);
        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRequestDialog();
            }
        });
    }

    private void getStudentData() {
        db.collection("students").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                status = (String) documentSnapshot.get("roomNo");
                Glide.with(getApplicationContext()).load(Uri.parse(documentSnapshot.getString("profilePic"))).into(imageView);
                name.setText("Name: " + (String) documentSnapshot.get("name"));
                branch.setText("Branch: " + (String) documentSnapshot.get("course"));
                phoneNo.setText("Phone No : " + (String) documentSnapshot.get("phoneNumber"));
                email.setText("Name: " + (String) documentSnapshot.get("email"));

                if (status.equals("notAllow")) {
                    //showPopup();
                    roomNo.setText("Room : Not allocated");
                } else {
                    cardView.setVisibility(View.VISIBLE);
                    roomNo.setText("Room No : " + (String) documentSnapshot.get("roomNo"));
                }
                // Log.d(TAG, "onSuccess: "+documentSnapshot.get("roomNo"));
                Log.d(TAG, "onSuccess: " + status);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(this);
    }

    private void startLoginActivity() {
        startActivity(new Intent(StudentActivity.this, Login.class));
        this.finish();
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getCurrentUser() == null) {
            startLoginActivity();
            return;
        }

        firebaseAuth.getCurrentUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
            @Override
            public void onSuccess(GetTokenResult getTokenResult) {
                //Log.d(TAG,getTokenResult.getToken());
            }
        });
    }

//    void showPopup() {
//
//        dialog.setContentView(R.layout.wait_layout);
//
//        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        dialog.show();
//        // dialog.onBackPressed();
//        //finish();
//    }

    private void showRequestDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(StudentActivity.this);
        alertDialog.setTitle("Please Enter Details");
        View view = getLayoutInflater().inflate(R.layout.request_send_layout, null);

        TextInputLayout purpose = (TextInputLayout) view.findViewById(R.id.textInputPurpose);
        TextInputLayout dateInput = (TextInputLayout) view.findViewById(R.id.txtInputDate);

        EditText edtdate = (EditText) view.findViewById(R.id.edtDate);
        Button btnsend = view.findViewById(R.id.btnSend);


        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        edtdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(StudentActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                month = month + 1;
                                String date = day + "/" + month + "/" + year;
                                edtdate.setText(date);

                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        alertDialog.setView(view);
        AlertDialog alertDialog1 = alertDialog.create();
        alertDialog1.show();

        btnsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (purpose.getEditText().getText().length() < 1) {
                    purpose.setError("Please Enter");
                } else if (edtdate.getText().length() < 1) {
                    purpose.setError(null);
                    dateInput.setError("KLRj");
                } else {
                    Log.d(TAG, "onClick:" + "dataok");

                    Map<String, Object> dataForOut = new HashMap<>();
                    dataForOut.put("studentId", userId);
                    dataForOut.put("date", edtdate.getText().toString());
                    dataForOut.put("documentId", null);
                    dataForOut.put("purpose", purpose.getEditText().getText().toString());
                    dataForOut.put("outTime", null);
                    dataForOut.put("inTime", null);
                    dataForOut.put("roomNo", roomNo.getText().toString().substring(10));
                    dataForOut.put("tokenId", null);
                    dataForOut.put("branch",branch.getText().toString().substring(8));
                    dataForOut.put("name",name.getText().toString().substring(6));
                    dataForOut.put("phoneNo",phoneNo.getText().toString().substring(11));
                    dataForOut.put("status", "notApproved");

                    db.collection("details").add(dataForOut).addOnSuccessListener(
                            new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    documentReference.update("documentId",documentReference.getId());
                                    Log.d(TAG, "onSuccess: "+"Data add to details");
                                    alertDialog1.dismiss();
                                    btnRequest.setVisibility(View.INVISIBLE);
                                    btnCheckStatus.setVisibility(View.VISIBLE);
                                }
                            }
                    );

                    dateInput.setError(null);
                }


            }
        });
    }

    public void showStatus(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        View checkView = getLayoutInflater().inflate(R.layout.check_status_layout, null);

        TextView date = checkView.findViewById(R.id.txtDateStatus);
        TextView purpose = checkView.findViewById(R.id.txtPurposeCheck);
        TextView status = checkView.findViewById(R.id.txtStatusCheck);
        Button btnQr = checkView.findViewById(R.id.btnGetQr);
        btnQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(StudentActivity.this);
                View view = getLayoutInflater().inflate(R.layout.qr_layout, null);
                ImageView qr = view.findViewById(R.id.qr);
                alertDialog.setView(view);
                AlertDialog alertDialog1 = alertDialog.create();
                alertDialog1.show();
                generateQr(qr);
            }
        });
        getTicketData(date,purpose,status,btnQr);
        alert.setView(checkView);
        AlertDialog alertDialog1 = alert.create();
        alertDialog1.show();

    }

    private void generateQr(ImageView qr) {

//        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
//        BitMatrix bitMatrix = multiFormatWriter.encode();
        getQrData(qr);


    }

    private void getQrData(ImageView qr) {
        db.collection("details").whereEqualTo("studentId",userId)
                .whereEqualTo("date",currentDate()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();

                for (DocumentSnapshot data : documents){
                 String qrdata = (String) data.get("tokenId");
                    QRGEncoder qrgEncoder = new QRGEncoder(qrdata,null, QRGContents.Type.TEXT,500);
                    try {
                        // Getting QR-Code as Bitmap
                        Bitmap bitmap = qrgEncoder.getBitmap();
                        // Setting Bitmap to ImageView
                        qr.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                    }
                }

            }
        });

    }

    public String currentDate(){

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String date = dateFormat.format(calendar.getTime());
        return date;
    }

    public void checkDateDb(){
        db.collection("details").whereEqualTo("studentId",userId)
                .whereEqualTo("date",currentDate()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                if (documents.size() == 0){
                    btnRequest.setVisibility(View.VISIBLE);
                }else {
                    btnCheckStatus.setVisibility(View.VISIBLE);
                    btnRequest.setVisibility(View.INVISIBLE);
                }

            }
        });
    }

    public void getTicketData(TextView d, TextView p, TextView s, Button getQr){

        db.collection("details").whereEqualTo("studentId",userId)
                .whereEqualTo("date",currentDate()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();

                for (DocumentSnapshot data : documents){
                    p.setText("Purpose : "+(String)data.get("purpose"));
                    d.setText("Date : "+(String)data.get("date"));
                    String status = (String) data.get("status");
                    if (status.equals("notApproved")){
                        getQr.setVisibility(View.INVISIBLE);
                        s.setText("Status : Not Approved");
                    }else {
                        getQr.setVisibility(View.VISIBLE);
                        s.setText("Status : Approved");
                    }
                }

            }
        });

    }

}

