package com.example.pc_asus.nguoimu.FaceRecognition;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.pc_asus.nguoimu.AppUtil;
import com.example.pc_asus.nguoimu.FaceRecognition.liveVideo.FaceTrackerActivity;
import com.example.pc_asus.nguoimu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FaceRecognitionActivity extends AppCompatActivity {

    private static final String URL="";
    private Retrofit retrofit;
    Call<String> call;
    ImageView img;
    File f;


    int PICK_IMAGE_MULTIPLE = 1;
    String imageEncoded;
    List<String> imagesEncodedList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognition);

        Button btn_album = findViewById(R.id.btn_album);
        Button btn_camera= findViewById(R.id.btn_camera);
        Button btn_send= findViewById(R.id.btn_send);
        img= findViewById(R.id.imageView);


        btn_album.setOnClickListener(new View.OnClickListener() {
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






        String android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        Toast.makeText(this, ""+android_id, Toast.LENGTH_SHORT).show();

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog dialog= new ProgressDialog(FaceRecognitionActivity.this);
                dialog.setMessage("         please wait...");
                dialog.show();

                Log.e("abc",f.getAbsolutePath()+" "+f.length());

                RequestBody requestBody= RequestBody.create(MediaType.parse("multipart/form-data"),f);
                MultipartBody.Part body= MultipartBody.Part.createFormData("upload_image","/data/test.jpg",requestBody);

                retrofit = new Retrofit.Builder()
                        .baseUrl(API.Base_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                API api= retrofit.create(API.class);

                call= api.recognitionFace(AppUtil.getUidLowerCase(),body);

                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        dialog.dismiss();
                        Toast.makeText(FaceRecognitionActivity.this, response.body(), Toast.LENGTH_SHORT).show();
                        Log.e("abc","result="+response.body());
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.e("abc","lỗi "+" "+t.getMessage());
                        Toast.makeText(FaceRecognitionActivity.this, "Lỗi", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });






        Button btnTrain= findViewById(R.id.btn_training);
        btnTrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FaceRecognitionActivity.this, TrainingActivity.class));
            }
        });



        Button btnVideo= findViewById(R.id.btn_video);
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FaceRecognitionActivity.this, FaceTrackerActivity.class));
            }
        });



        Button btnCreatePersonGroup= findViewById(R.id.btn_createPersonGroup);
        btnCreatePersonGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPersonGroup();
            }
        });



    }


    private  class ReadData extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String result = call.execute().body();
                Log.e("abcp","a:"+result);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {              //chọn ảnh
            if (data == null) {
                return;
            }
            try {
                //  Uri uri= data.getData();
                InputStream inputStream = FaceRecognitionActivity.this.getContentResolver().openInputStream(data.getData());
                Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                bitmap = AppUtil.getResizedBitmap(bitmap, 350, 350);

                img.setImageBitmap(bitmap);

                f=convertBitmapToFile(bitmap);
                Log.e("abc","path "+f.getAbsolutePath());



            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
        else if (requestCode == 2 && resultCode == Activity.RESULT_OK && data!=null) {       //camera
            Bitmap bitmap= (Bitmap) data.getExtras().get("data");
            img.setImageBitmap(bitmap);

            f=convertBitmapToFile(bitmap);
            Log.e("abc","path "+f.getAbsolutePath());
        }


    }

    private File convertBitmapToFile(Bitmap bitmap) {
        File imageFile = new File(getCacheDir(), "test.jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e("abc", "Error writing bitmap", e);
        }


        return imageFile;
    }




    private  void deletePerson(){

        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid= mCurrentUser.getUid();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API.Base_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final API api = retrofit.create(API.class);

        Call<String> call = api.deletePerson(uid,"personid");

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

                Toast.makeText(FaceRecognitionActivity.this, ""+response.body(), Toast.LENGTH_SHORT).show();
                Log.e("abc", "result=" + response.body());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(FaceRecognitionActivity.this, "Lỗi "+t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("abc", "lỗi cteate personGroup");
            }
        });
    }
}

