package com.example.pc_asus.nguoimu;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pc_asus.nguoimu.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;

public class AccountSettingsActivity extends AppCompatActivity {

    EditText edt_name,edt_email, edt_phoneNumber;
    Button btn_choose, btn_camera,btn_save, btn_cancel;
    TextView tv_resetPassword;
    ImageView img_avatar;
    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    final StorageReference storageRef = storage.getReference();
    String nameImg;
    final String[] photoURL = new String[1];
    boolean saved;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        edt_name=findViewById(R.id.edt_as_name);
        edt_email= findViewById(R.id.edt_as_email);
        edt_phoneNumber= findViewById(R.id.edt_as_phoneNumber);
        btn_choose=findViewById(R.id.btn_as_choose);
        btn_camera= findViewById(R.id.btn_as_camera);
        btn_save= findViewById(R.id.btn_as_save);
        btn_cancel= findViewById(R.id.btn_as_cancel);
        tv_resetPassword= findViewById(R.id.tv_resetPassword);
        img_avatar= findViewById(R.id.img_avatar);

        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        String uid= mCurrentUser.getUid();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("NguoiMu").child("Users").child(uid);


        final ProgressDialog dialog;
        dialog = new ProgressDialog(this);
        dialog.setMessage("loading...");
        dialog.show();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                edt_email.setText(dataSnapshot.child("email").getValue().toString());
                edt_phoneNumber.setText(dataSnapshot.child("phoneNumber").getValue().toString());
                edt_name.setText(dataSnapshot.child("name").getValue().toString());
                photoURL[0] =dataSnapshot.child("photoURL").getValue().toString();

                RequestOptions requestOptions = new RequestOptions();
                requestOptions.fitCenter();
                requestOptions.placeholder(R.mipmap.user);
                Glide.with(getApplicationContext())
                        .load(photoURL[0])
                        .apply(requestOptions)
                        //   .override(200,150)
                        .into(img_avatar);

                dialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {




                final ProgressDialog dialog;
                dialog = new ProgressDialog(AccountSettingsActivity.this);
                dialog.setMessage("      Đang lưu...");
                dialog.setCancelable(false);
                dialog.show();
                Calendar calendar = Calendar.getInstance();
                nameImg="img"+calendar.getTimeInMillis()+".png";

                StorageReference mountainsRef = storageRef.child(nameImg);
                img_avatar.setDrawingCacheEnabled(true);
                img_avatar.buildDrawingCache();
                Bitmap bitmap = ((BitmapDrawable) img_avatar.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = mountainsRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(AccountSettingsActivity.this, "Lỗi!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(AccountSettingsActivity.this, "Đã lưu!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        Log.e("abc","upload xong");
                        getLinkAvatar();

                    }

                });


            }
        });


        btn_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });


        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,2);
            }
        });


        tv_resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(AccountSettingsActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_change_password);
                final EditText edt_oldPass= dialog.findViewById(R.id.edt_oldPass);
                final EditText edt_newPass= dialog.findViewById(R.id.edt_newPass);
                final EditText edt_confirmNewPass= dialog.findViewById(R.id.edt_confirm_newPass);
                Button btn_confirm= dialog.findViewById(R.id.btn_confirm);
                Button btn_cancel = dialog.findViewById(R.id.btn_changePass_cancel);
                btn_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                btn_confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final ProgressDialog progressDialogdialog;
                        progressDialogdialog = new ProgressDialog(AccountSettingsActivity.this);
                        progressDialogdialog.setMessage("      Loading...");
                        progressDialogdialog.setCancelable(false);
                        progressDialogdialog.show();
                        AuthCredential credential = EmailAuthProvider.getCredential(mCurrentUser.getEmail(), edt_oldPass.getText().toString().trim());

                        mCurrentUser.reauthenticate(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(edt_newPass.getText().toString().trim().equals(edt_confirmNewPass.getText().toString().trim())==true) {
                                            if (task.isSuccessful()) {
                                                mCurrentUser.updatePassword(edt_newPass.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(AccountSettingsActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                                                            progressDialogdialog.dismiss();
                                                            dialog.dismiss();
                                                        } else {
                                                            Toast.makeText(AccountSettingsActivity.this, "    Đổi mật khẩu thất bại\nMật khẩu phải từ 6 ký tự trở lên", Toast.LENGTH_SHORT).show();
                                                            progressDialogdialog.dismiss();
                                                        }
                                                    }
                                                });
                                            } else {
                                                Toast.makeText(AccountSettingsActivity.this, "Mật khẩu cũ không chính xác", Toast.LENGTH_SHORT).show();
                                                progressDialogdialog.dismiss();

                                            }
                                        }else{
                                            Toast.makeText(AccountSettingsActivity.this, "Mật khẩu mới và mật khẩu xác nhận phải trùng nhau", Toast.LENGTH_SHORT).show();
                                            progressDialogdialog.dismiss();

                                        }
                                    }
                                });
                    }
                });
                dialog.show();



            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {              //chọn ảnh
            if (data == null) {
                return;
            }
            try {
                InputStream inputStream = AccountSettingsActivity.this.getContentResolver().openInputStream(data.getData());
                Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                img_avatar.setImageBitmap(bitmap);



            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }else if (requestCode == 2 && resultCode == Activity.RESULT_OK && data!=null) {       //camera
                Bitmap bitmap= (Bitmap) data.getExtras().get("data");
                img_avatar.setImageBitmap(bitmap);
        }

    }

    private void getLinkAvatar(){
        Log.e("abc","getlink");
        storageRef.child(nameImg).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                photoURL[0]=uri.toString();
                User user = new User(edt_name.getText().toString(),edt_email.getText().toString(),edt_phoneNumber.getText().toString(),photoURL[0],SignInActivity.android_id);
                mDatabase.setValue(user);
                Log.e("abc","get link xong");

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });

    }
}
