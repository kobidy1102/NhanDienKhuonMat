package com.example.pc_asus.nguoimu.SearchTNV;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.pc_asus.nguoimu.Model.TNV;
import com.example.pc_asus.nguoimu.Model.User;
import com.example.pc_asus.nguoimu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SearchTnvActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;
    ArrayList<TNV> arrTNV= new ArrayList<TNV>();
    ArrayList<TNV> arrTNV2= new ArrayList<TNV>();

    ListView lv_listTnv;
    ListTnvAdapter adapter;
    SearchView searchView;
    String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_tnv);

        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        uid= mCurrentUser.getUid();
        mDatabase= FirebaseDatabase.getInstance().getReference();

        lv_listTnv= findViewById(R.id.lv_listTNV2);
        adapter= new ListTnvAdapter(getApplicationContext(),R.layout.custom_list_tnv,arrTNV2);
        lv_listTnv.setAdapter(adapter);

        searchView=(SearchView) findViewById(R.id.sv_search2);
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Nhập email");

        mDatabase.child("TinhNguyenVien").child("Users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                User user= dataSnapshot.getValue(User.class);
                arrTNV.add(new TNV(user,dataSnapshot.getKey()));
                arrTNV2.add(new TNV(user,dataSnapshot.getKey()));

                Log.e("abc",user.email+" "+dataSnapshot.getKey());
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



        lv_listTnv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(SearchTnvActivity.this);
                alertDialog.setTitle("          Xác Nhận...");
                alertDialog.setMessage("Thêm người này vào danh sách bạn bè?");
                alertDialog.setIcon(R.mipmap.war);

                alertDialog.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        mDatabase.child("NguoiMu").child("Friends").child(uid).child(arrTNV2.get(position).id).setValue(arrTNV2.get(position).user);
                        Toast.makeText(SearchTnvActivity.this, "Đã thêm", Toast.LENGTH_SHORT).show();

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

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        String mail;
        int s = newText.length();
        arrTNV2.clear();
        adapter.notifyDataSetChanged();

        for (int i = 0; i < arrTNV.size(); i++) {
                mail = arrTNV.get(i).user.email.substring(0, s);
                if (newText.equalsIgnoreCase(mail) == true) {
                    arrTNV2.add(arrTNV.get(i));
                    adapter.notifyDataSetChanged();

                }
            }

//         if(newText.isEmpty()){
//           arrTNV2.addAll(arrTNV);
//            adapter.notifyDataSetChanged();
//
//        }
        return false;
    }
}
