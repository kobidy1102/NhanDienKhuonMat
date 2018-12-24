package com.example.pc_asus.nguoimu.FaceRecognition;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.pc_asus.nguoimu.AppUtil;
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

public class AddFaceToPersonActivity extends AppCompatActivity {

    private ListImagePickAdapter adapter;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    List<Bitmap> arrBitMapImage;
    RecyclerView recyclerView;
    private Retrofit retrofit;
    Call<String> call;
    File mfile;
    int i=0;
    private ProgressDialog dialog;
    API api;

    FirebaseStorage storage = FirebaseStorage.getInstance();

    final StorageReference storageRef = storage.getReference();
    // private DatabaseReference mDatabase;
    //  private FirebaseUser mCurrentUser;
    //  String uid;
    String personId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_face_to_person);

        ImageView btn_addImage= findViewById(R.id.img_add_face_addImage);

        Intent intent = getIntent();
        personId = intent.getExtras().getString("personId");

        btn_addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(AddFaceToPersonActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(AddFaceToPersonActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                } else {
                    Matisse.from(AddFaceToPersonActivity.this)
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
        Button btnSave = findViewById(R.id.btn_add_face_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                    dialog.show();
                    addFaceToPerson(personId);
                //    addPersontoGroup();


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

                Toast.makeText(AddFaceToPersonActivity.this, response.body() + " "+(i+1), Toast.LENGTH_SHORT).show();
                Log.e("abc", "result=" + response.body());

                if(response.body().contains("Error")){
                    Toast.makeText(AddFaceToPersonActivity.this, "Ảnh bị lỗi! Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }else {
                    i++;
                    if (i < arrBitMapImage.size()) {
                        addFaceToPerson(personId);                              ///////////////////////////
                    } else {
                        trainingPerson();

                      //  upLoadimage(arrBitMapImage.get(0),personId);

                        //TODO lưu person lên firebase.
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("abc", "lỗi add face "+t.getMessage());
                Toast.makeText(AddFaceToPersonActivity.this, "Lỗi add face", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void trainingPerson(){

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
                Toast.makeText(AddFaceToPersonActivity.this, response.body(), Toast.LENGTH_SHORT).show();
                Log.e("abc","result="+response.body());

                if(response.body().contains("Complete")) {
                    arrBitMapImage.clear();
                    adapter.notifyDataSetChanged();
                }
                dialog.dismiss();


            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("abc","lỗi training ");
                Toast.makeText(AddFaceToPersonActivity.this, "Lỗi training", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        arrBitMapImage= new ArrayList<Bitmap>();
        if (requestCode == 1 && resultCode == RESULT_OK) {

            List<String> resultPaths;

            resultPaths = Matisse.obtainPathResult(data);
            if (resultPaths.size() > 0) {
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
        recyclerView =findViewById(R.id.rv_add_face_image);
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





//    private void upLoadimage(Bitmap bitmap, final String personId){
////        final ProgressDialog dialog;
////        dialog = new ProgressDialog(TrainingActivity.this);
////        dialog.setMessage("      Đang lưu...");
////        dialog.setCancelable(false);
////        dialog.show();
//        Calendar calendar = Calendar.getInstance();
//        final String nameImg="img"+calendar.getTimeInMillis()+".png";
//
//        StorageReference mountainsRef = storageRef.child(nameImg);
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        byte[] data = baos.toByteArray();
//
//        UploadTask uploadTask = mountainsRef.putBytes(data);
//        uploadTask.addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                Toast.makeText(AddFaceToPersonActivity.this, "Lỗi!", Toast.LENGTH_SHORT).show();
//            }
//        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                Toast.makeText(AddFaceToPersonActivity.this, "Đã lưu!", Toast.LENGTH_SHORT).show();
//                // dialog.dismiss();
//                Log.e("abc","upload xong");
//                getLinkImage(nameImg,personId);
//
//            }
//
//        });
//    }


//    private void getLinkImage(String nameImg, final String personId){
//        Log.e("abc","getlink");
//        storageRef.child(nameImg).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//
//                //add to firebase
//                PersonTraining personTraining= new PersonTraining(edt_name.getText().toString().trim(),personId,uri.toString());
//                AppUtil.getmDatabase().child("NguoiMu").child("Training").child(AppUtil.getUid()).push().setValue(personTraining);
//                Log.e("abc","get link xong");
//
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//            }
//        });

//    }


}
