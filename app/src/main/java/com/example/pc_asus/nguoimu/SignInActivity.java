package com.example.pc_asus.nguoimu;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignInActivity extends AppCompatActivity {

    EditText edt_email,edt_password;

    TextView tv_forgotPW;
    Button btn_signIn, btn_signUp;
    private FirebaseAuth mAuth;
    Boolean isBlind = false;
    static String android_id ;

// ...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
       // Intent intent= new Intent(MainActivity.this,VideoChatViewActivity.class);
       // startActivity(intent);
        edt_email = findViewById(R.id.edt_email);
        edt_password = findViewById(R.id.edt_password);
        btn_signIn = findViewById(R.id.btn_dangNhap);
        btn_signUp = findViewById(R.id.btn_dangKi);
        tv_forgotPW = findViewById(R.id.tv_forgotPw);
        mAuth = FirebaseAuth.getInstance();



        Bundle bundle = getIntent().getBundleExtra("Bundle");
        if(bundle != null){
            edt_email.setText(bundle.getString("Email"));
            edt_password.setText(bundle.getString("Password"));
            Log.e("bundle thanh cong","Gia tri");
        }
        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        btn_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dangNhap();
            }
        });

        tv_forgotPW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forgotPW();
            }
        });


    }
    private void forgotPW() {
        final String userEmail = edt_email.getText().toString().trim();

        if (userEmail.equals(""))
        {
            Toast.makeText(SignInActivity.this,"Vui lòng nhập địa chỉ Email",Toast.LENGTH_SHORT).show();

        }else {
            mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(SignInActivity.this,"Email đặt lại mật khẩu đã được gửi", Toast.LENGTH_LONG).show();
                        edt_email.setText("");
                        edt_email.setHint(userEmail);
                    }
                    else Toast.makeText(SignInActivity.this,"Yêu cầu đặt lại mật khẩu không thành công",Toast.LENGTH_LONG).show();
                }
            });
        }
    }



    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.e("abc","trang thai dang nhap "+currentUser);
        if(currentUser!=null) {
              Intent intent= new Intent(SignInActivity.this,MainActivity.class);
              startActivity(intent);
              finish();
        }
    }

    private void  dangNhap() {
        final ProgressDialog dialog;
        dialog = new ProgressDialog(this);
        dialog.setMessage("loading...");

        final String email = edt_email.getText().toString().trim();
        final String password = edt_password.getText().toString().trim();

        Log.e("abc",email);
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(SignInActivity.this, "Chưa nhập email hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
        } else {
            dialog.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                ///// đăng nhập kiểm tra có đúng tài khoản nguoi mu hay không, nếu đúng thì cho phép đăng nhập
                                final DatabaseReference mDatabase;
                                FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
                                String uid= mCurrentUser.getUid();
                                mDatabase = FirebaseDatabase.getInstance().getReference().child("NguoiMu").child("Users").child(uid);
                                Log.e("abcde", String.valueOf(mDatabase));
                                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        try {
                                            String data = dataSnapshot.getValue().toString();
                                            if (!data.equals(null)) {
                                                dialog.dismiss();
                                                isBlind = true;
                                                Log.e("abcde", "Dang nhap");
                                                mDatabase.child("idDevice").setValue(android_id);
                                                checkEmailValification();
                                            } else {
                                                dialog.dismiss();
                                                //Toast.makeText(SignInActivity.this,"Vui lòng đăng nhập đúng tài khoản", Toast.LENGTH_SHORT).show();
                                                Log.e("abcde", "Lỗi");
                                                //FirebaseAuth.getInstance().signOut();
                                            }
                                        }
                                        catch (Exception e){
                                            Log.e("abcde", "Khong dang nhap duoc");
                                            isBlind = false;
                                            dialog.dismiss();
                                            Toast.makeText(SignInActivity.this,"Tài khoản hoặc mật khẩu không chính xác!",Toast.LENGTH_SHORT).show();
                                            FirebaseAuth.getInstance().signOut();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                /////


                            } else {
                                dialog.dismiss();
                                Toast.makeText(SignInActivity.this, "Tài khoản hoặc mật khẩu không chính xác!", Toast.LENGTH_SHORT).show();

                            }

                            // ...
                        }
                    });

        }
    }


    private void checkEmailValification(){
        FirebaseUser firebaseUser = mAuth.getInstance().getCurrentUser();
        Boolean emailFlag = firebaseUser.isEmailVerified();
        if(emailFlag){
           // Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
            finish();
        }else{
            Toast.makeText(this,"Xác nhận email của bạn",Toast.LENGTH_SHORT).show();
            mAuth.signOut();
        }
    }
}
