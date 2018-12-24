package com.example.pc_asus.nguoimu.FaceRecognition;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.pc_asus.nguoimu.AppUtil;
import com.example.pc_asus.nguoimu.FaceRecognition.liveVideo.FaceTrackerActivity;
import com.example.pc_asus.nguoimu.MainActivity;
import com.example.pc_asus.nguoimu.Model.PersonTraining;
import com.example.pc_asus.nguoimu.Model.TNV;
import com.example.pc_asus.nguoimu.Model.User;
import com.example.pc_asus.nguoimu.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;

public class ListTrainedActivity extends AppCompatActivity {
    private ListPersonTrainedAdapter adapter;
    RecyclerView rvListTrained;
    ArrayList<PersonTraining> arrPersonTraining= new ArrayList<PersonTraining>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_trained);

        FloatingActionButton floatingActionButton =findViewById(R.id.fbtn_add);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ListTrainedActivity.this, TrainingActivity.class));
                finish();
            }
        });


        initRecycleView();



        AppUtil.getmDatabase().child("NguoiMu").child("Training").child(AppUtil.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                PersonTraining personTraining= dataSnapshot.getValue(PersonTraining.class);

                PersonTraining personTrainingKey= new PersonTraining(personTraining.name,personTraining.id,personTraining.photoURL,dataSnapshot.getKey());
                arrPersonTraining.add(personTrainingKey);

                Log.e("abc",personTraining.name+" "+dataSnapshot.getKey());
                adapter.notifyDataSetChanged();
                //cho cai biến đếm

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        Button btnOpenActivity= findViewById(R.id.btn_open_activity);
        btnOpenActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // startActivity(new Intent(ListTrainedActivity.this,FaceRecognitionActivity.class));
                startActivity(new Intent(ListTrainedActivity.this, FaceTrackerActivity.class));
            }
        });




    }



    private void initRecycleView(){
         rvListTrained= findViewById(R.id.rv_list_trained);
   //     rvListTrained.setHasFixedSize(true);
       // LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);

        adapter= new ListPersonTrainedAdapter(arrPersonTraining,ListTrainedActivity.this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);
        //gridLayoutManager.
        rvListTrained.setLayoutManager(gridLayoutManager);

        rvListTrained.addItemDecoration(new SetCenterItemsInGridView(2, 50, false));
        rvListTrained.setAdapter(adapter);
    }
}
