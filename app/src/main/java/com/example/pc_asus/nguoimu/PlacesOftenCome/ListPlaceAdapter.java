package com.example.pc_asus.nguoimu.PlacesOftenCome;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.pc_asus.nguoimu.Model.PlaceOC;
import com.example.pc_asus.nguoimu.R;

import java.util.List;


public class ListPlaceAdapter extends ArrayAdapter<PlaceOC>{


    private List<PlaceOC> items;
    private  Context context;

    public ListPlaceAdapter(Context context, int resourse, List<PlaceOC> items) {
        super(context, resourse, items);
        this.context=context;
        this.items = items;
    }
    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi;
            vi=LayoutInflater.from(getContext());
            v= vi.inflate(R.layout.custom_list_place,null);
        }

        // Get item
        PlaceOC place = getItem(position);
        if (place!=null){
            TextView tvName = (TextView) v.findViewById(R.id.tv_listPlace_name);
            TextView tvAddress = (TextView) v.findViewById(R.id.tv_listPlace_address);

            tvName.setText(place.namePlace);
            tvAddress.setText(place.address);


        }



        return v;
    }

    public List<PlaceOC> getItems() {
        return items;
    }
}
