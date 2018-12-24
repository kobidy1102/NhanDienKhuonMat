package com.example.pc_asus.nguoimu.SearchTNV;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pc_asus.nguoimu.Model.TNV;
import com.example.pc_asus.nguoimu.R;

import java.util.List;


public class ListTnvAdapter extends ArrayAdapter<TNV>{


    private List<TNV> items;
    private  Context context;

    public ListTnvAdapter(Context context, int resourse, List<TNV> items) {
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
            v= vi.inflate(R.layout.custom_list_tnv,null);
        }

        // Get item
        TNV tnv = getItem(position);
        if (tnv!=null){
            TextView tvName = (TextView) v.findViewById(R.id.tv_list_name);
            TextView tvEmail = (TextView) v.findViewById(R.id.tv_list_email);
            ImageView imgAvatar = (ImageView) v.findViewById(R.id.img_list_avatar);

            tvName.setText(tnv.user.name);
            tvEmail.setText(tnv.user.email);
            RequestOptions  requestOptions = new RequestOptions();
            requestOptions.fitCenter();
            requestOptions.placeholder(R.mipmap.user);
            Glide.with(context)
                    .load(tnv.user.photoURL)
                    .apply(requestOptions)
                    .into(imgAvatar);

        }



        return v;
    }

    public List<TNV> getItems() {
        return items;
    }
}
