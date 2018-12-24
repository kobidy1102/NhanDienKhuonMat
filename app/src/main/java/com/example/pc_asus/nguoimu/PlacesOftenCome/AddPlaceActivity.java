package com.example.pc_asus.nguoimu.PlacesOftenCome;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.pc_asus.nguoimu.Model.PlaceOC;
import com.example.pc_asus.nguoimu.R;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddPlaceActivity extends AppCompatActivity {
    public static GoogleMap mMap;
    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;
    String uid;
    ImageView imgPick;
    int PLACE_PICKER_REQUEST = 1;
    Button btnCancel, btnAdd;
    EditText edt_namePlace;
    String address;
    PlaceAutocompleteFragment autocompleteFragment1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        uid= mCurrentUser.getUid();
        mDatabase= FirebaseDatabase.getInstance().getReference();

        autocompleteFragment1= (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment1);

        autocompleteFragment1.getView().setBackgroundColor(Color.WHITE);

        autocompleteFragment1.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                address= place.getAddress().toString();

            }

            @Override
            public void onError(Status status) {
                Log.e("abc", "An error occurred: " + status);
            }


        });




        imgPick= findViewById(R.id.img_pickPlace);
        imgPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(AddPlaceActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });


        btnCancel= findViewById(R.id.btn_addPlace_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        edt_namePlace= findViewById(R.id.edt_namePlace);

        btnAdd= findViewById(R.id.btn_addPlace_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edt_namePlace.getText().toString().isEmpty() || address.isEmpty()){
                    Toast.makeText(AddPlaceActivity.this, "Vui lòng nhập tên và địa chỉ", Toast.LENGTH_SHORT).show();
                    return;
                }
                PlaceOC p= new PlaceOC(edt_namePlace.getText().toString(),address);
                mDatabase.child("NguoiMu").child("PlacesOftenCome").child(uid).push().setValue(p);
                edt_namePlace.setText("");
                address="";
                autocompleteFragment1.setText("");
                Toast.makeText(AddPlaceActivity.this, "Đã lưu", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                address=place.getAddress().toString();
                Toast.makeText(this, place.getAddress(), Toast.LENGTH_SHORT).show();
            }
        }
    }


}
