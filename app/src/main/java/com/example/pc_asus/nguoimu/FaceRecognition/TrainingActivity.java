package com.example.pc_asus.nguoimu.FaceRecognition;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.pc_asus.nguoimu.AppUtil;
import com.example.pc_asus.nguoimu.MainActivity;
import com.example.pc_asus.nguoimu.Model.PersonTraining;
import com.example.pc_asus.nguoimu.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TrainingActivity extends AppCompatActivity {
    private ListImagePickAdapter adapter;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    List<Bitmap> arrBitMapImage;
    RecyclerView recyclerView;
    private Retrofit retrofit;
    Call<String> call;
    File mfile;
     EditText edt_name;
     int i=0;
    private ProgressDialog dialog;
     API api;

    FirebaseStorage storage = FirebaseStorage.getInstance();

    final StorageReference storageRef = storage.getReference();
   // private DatabaseReference mDatabase;
  //  private FirebaseUser mCurrentUser;
  //  String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        ImageView btn_addImage= findViewById(R.id.img_train_addImage);
        edt_name= findViewById(R.id.edt_train_name);

     //   mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
    //   uid = mCurrentUser.getUid();
     //   mDatabase= FirebaseDatabase.getInstance().getReference();

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btn_addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(TrainingActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(TrainingActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                } else {
                    Matisse.from(TrainingActivity.this)
                            .choose(MimeType.ofImage())
                            .countable(true)
                            .maxSelectable(10)
                            .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                            .thumbnailScale(0.85f)
                            .imageEngine(new PicassoEngine())
                            .forResult(1);
                }



            }
        });


        dialog= new ProgressDialog(this);
            dialog.setMessage("         please wait...");
        Button btnSave = findViewById(R.id.btn_train_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(edt_name.getText().toString().trim().isEmpty()){
                    Toast.makeText(TrainingActivity.this, "Bạn chưa nhập tên", Toast.LENGTH_SHORT).show();
                }else {
                    dialog.show();
                    addPersontoGroup();
                }

            }
        });










    }



    private  void addPersontoGroup(){


        retrofit = new Retrofit.Builder()
                .baseUrl(API.Base_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final API api = retrofit.create(API.class);

        call = api.addPersontoGroup(AppUtil.getUidLowerCase(),edt_name.getText().toString().trim());

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                Toast.makeText(TrainingActivity.this, ""+response.body(), Toast.LENGTH_SHORT).show();
                Log.e("abc", "result=" + response.body());

                    addFaceToPerson(response.body());                      ///////////////////
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("abc", "lỗi add person");
                Toast.makeText(TrainingActivity.this, "Lỗi add person", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private  void addFaceToPerson(final String personId){

        mfile = convertBitmapToFile(arrBitMapImage.get(i));
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), mfile);
        final MultipartBody.Part body = MultipartBody.Part.createFormData("upload_image", "/data/test.jpg", requestBody);

        retrofit = new Retrofit.Builder()
                .baseUrl(API.Base_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final API api = retrofit.create(API.class);

        call = api.addFaceToPerson(AppUtil.getUidLowerCase(),personId,body);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                Toast.makeText(TrainingActivity.this, response.body() + " "+(i+1), Toast.LENGTH_SHORT).show();
                Log.e("abc", "result=" + response.body());

                if(response.body().contains("Error")){
                    Toast.makeText(TrainingActivity.this, "Ảnh bị lỗi! Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }else {
                    i++;
                    if (i < arrBitMapImage.size()) {
                        addFaceToPerson(personId);                              ///////////////////////////
                    } else {
                        trainingPerson(personId);




                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("abc", "lỗi add face "+t.getMessage());
                Toast.makeText(TrainingActivity.this, "Lỗi add face", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void trainingPerson(final String personId){

        retrofit = new Retrofit.Builder()
                .baseUrl(API.Base_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api= retrofit.create(API.class);
        // api.personName=edt_name.getText().toString().trim();

        call= api.trainingPerson(AppUtil.getUidLowerCase());

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Toast.makeText(TrainingActivity.this, response.body(), Toast.LENGTH_SHORT).show();
                Log.e("abc","result="+response.body());

                upLoadimage(arrBitMapImage.get(0),personId);
              //  dialog.dismiss();


            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("abc","lỗi training ");
                Toast.makeText(TrainingActivity.this, "Lỗi training", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Uri> mSelected;
       // List<String> paths;
        arrBitMapImage= new ArrayList<Bitmap>();
        if (requestCode == 1 && resultCode == RESULT_OK) {
            mSelected = Matisse.obtainResult(data);


            //path of photo that taken by camera
            List<String> resultPaths;

            resultPaths = Matisse.obtainPathResult(data);
            if (resultPaths.size() > 0) {
             //   filesToUpload.clear();
             //   previewBitmaps.clear();

             //   if (resultPaths.size() < 4) {
            //        previewSize = previewSize * 2;
            //    }
                for (String path : resultPaths) {
                    Bitmap photoBitmap = AppUtil.rotateImage(path, 500, 500);
                    arrBitMapImage.add(photoBitmap);
                }
            }

            Log.e("abc","size="+arrBitMapImage.size());

            initRecycleView();

        }
    }



    private void initRecycleView(){
        recyclerView =findViewById(R.id.rv_train_image);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(layoutManager);
        adapter= new ListImagePickAdapter(arrBitMapImage,getApplicationContext());
        recyclerView.setAdapter(adapter);
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

        Log.e("abc","convert="+imageFile.length()+"- listFile="+imageFile.listFiles());

        return imageFile;
    }





    private void upLoadimage(Bitmap bitmap, final String personId){
//        final ProgressDialog dialog;
//        dialog = new ProgressDialog(TrainingActivity.this);
//        dialog.setMessage("      Đang lưu...");
//        dialog.setCancelable(false);
//        dialog.show();
        Calendar calendar = Calendar.getInstance();
       final String nameImg="img"+calendar.getTimeInMillis()+".png";

        StorageReference mountainsRef = storageRef.child(nameImg);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(TrainingActivity.this, "Lỗi!", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(TrainingActivity.this, "Đã lưu!", Toast.LENGTH_SHORT).show();
               // dialog.dismiss();
                Log.e("abc","upload xong");
                getLinkImage(nameImg,personId);

            }

        });
    }


    private void getLinkImage(String nameImg, final String personId){
        Log.e("abc","getlink");
        storageRef.child(nameImg).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                //add to firebase
                PersonTraining personTraining= new PersonTraining(edt_name.getText().toString(),personId,uri.toString());
                AppUtil.getmDatabase().child("NguoiMu").child("Training").child(AppUtil.getUid()).push().setValue(personTraining);
                Log.e("abc","get link xong");

                    arrBitMapImage.clear();
                    adapter.notifyDataSetChanged();
                    edt_name.setText("");

                    dialog.dismiss();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(android.R.id.home==item.getItemId()){

            Intent intent = new Intent(TrainingActivity.this, ListTrainedActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(TrainingActivity.this, ListTrainedActivity.class);
        startActivity(intent);
        finish();

    }
}
