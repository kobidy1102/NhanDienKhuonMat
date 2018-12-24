package com.example.pc_asus.nguoimu.PlacesOftenCome;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.pc_asus.nguoimu.Model.PlaceOC;
import com.example.pc_asus.nguoimu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class PlacesOftenComeActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;
    String uid;
    ListView lvPlaces;
    ArrayList<PlaceOC> arrPlace=new ArrayList<PlaceOC>();
    ArrayList<String> arrKey= new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_often_come);

        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        uid= mCurrentUser.getUid();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("NguoiMu").child("PlacesOftenCome").child(uid);

        lvPlaces= findViewById(R.id.lv_places);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                startActivity(new Intent(PlacesOftenComeActivity.this, AddPlaceActivity.class));
            }
        });


         final ListPlaceAdapter adapter= new ListPlaceAdapter(PlacesOftenComeActivity.this,R.layout.custom_list_place,arrPlace);
        lvPlaces.setAdapter(adapter);
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                PlaceOC p= dataSnapshot.getValue(PlaceOC.class);
                arrPlace.add(new PlaceOC(dataSnapshot.child("namePlace").getValue().toString(),dataSnapshot.child("address").getValue().toString()));
                arrKey.add(dataSnapshot.getKey());

                Log.e("abc","name:"+p.namePlace);
                adapter.notifyDataSetChanged();

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




        lvPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(PlacesOftenComeActivity.this);
                alertDialog.setTitle("          Xác Nhận...");
                alertDialog.setMessage("Xóa địa điểm này?");
                alertDialog.setIcon(R.mipmap.war);

                alertDialog.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        mDatabase.child(arrKey.get(position)).removeValue();
                        arrPlace.remove(position);
                        adapter.notifyDataSetChanged();

                        Toast.makeText(PlacesOftenComeActivity.this, "Đã xóa", Toast.LENGTH_SHORT).show();

                    }
                });
                alertDialog.setNeutralButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                alertDialog.show();

            }
        });

    }
}
