package com.example.pc_asus.nguoimu;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pc_asus.nguoimu.FaceRecognition.API;
import com.example.pc_asus.nguoimu.FaceRecognition.FaceRecognitionActivity;
import com.example.pc_asus.nguoimu.FaceRecognition.TrainingActivity;
import com.example.pc_asus.nguoimu.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SignUpActivity extends AppCompatActivity {
    EditText edt_email,edt_password, edt_name, edt_phoneNumber;
    Button btn_signUp;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edt_email = findViewById(R.id.edt_email2);
        edt_password = findViewById(R.id.edt_pass2);
        edt_name = findViewById(R.id.edt_hoTen);
        edt_phoneNumber = findViewById(R.id.edt_sdt);

        btn_signUp = findViewById(R.id.btn_dangKy2);
        mAuth = FirebaseAuth.getInstance();
        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dangKy();
            }
        });
    }

    private void dangKy() {
        final ProgressDialog dialog;
        dialog = new ProgressDialog(this);
        dialog.setMessage("loading...");

        String email = edt_email.getText().toString().trim();
        final String password = edt_password.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Đăng Ký Thất Bại", Toast.LENGTH_SHORT).show();
        } else {
            dialog.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                dialog.dismiss();
                                User user= new User(edt_name.getText().toString(),edt_email.getText().toString(), edt_phoneNumber.getText().toString(),"https://firebasestorage.googleapis.com/v0/b/map-82eb0.appspot.com/o/generic-user-purple.png?alt=media&token=21815e8a-2bcd-477a-bf37-f6b382f0c409","");
                                FirebaseUser currentUser= FirebaseAuth.getInstance().getCurrentUser();
                                String uid=currentUser.getUid();
                                DatabaseReference mDatabase= FirebaseDatabase.getInstance().getReference().child("NguoiMu").child("Users").child(uid);
                                mDatabase.setValue(user);
                                sendEmailVerification();
                                createPersonGroup();

                            } else {
                                dialog.dismiss();
                                if (password.length() < 6) {
                                    Toast.makeText(SignUpActivity.this, "    Đăng Ký Thất Bại\nMật Khẩu Không Được Dưới 6 Ký Tự", Toast.LENGTH_SHORT).show();
                                } else
                                    Toast.makeText(SignUpActivity.this, "Đăng Ký Thất Bại", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        }
    }
    private void sendEmailVerification(){
        final FirebaseUser firebaseUser = mAuth.getInstance().getCurrentUser();
        if(firebaseUser != null){
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(SignUpActivity.this,"Đăng ký thành công, vui lòng xác nhận email !",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignUpActivity.this,SignInActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("Email",edt_email.getText().toString());
                        bundle.putString("Password",edt_password.getText().toString());
                        intent.putExtra("Bundle",bundle);
                        startActivity(intent);
                        finish();
                        mAuth.signOut();
                    }else{
                        Toast.makeText(SignUpActivity.this, "Thư xác nhận email chưa được gửi",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    private  void createPersonGroup(){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API.Base_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final API api = retrofit.create(API.class);

        Call<String> call = api.createPersonGroup(AppUtil.getUidLowerCase(),AppUtil.getUidLowerCase());

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                Log.e("abc", "result=" + response.body());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("abc", "lỗi cteate personGroup");
            }
        });
    }

}
