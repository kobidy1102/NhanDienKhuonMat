package com.example.pc_asus.nguoimu.FaceRecognition;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.pc_asus.nguoimu.R;

import java.util.List;

public class ListImagePickAdapter extends RecyclerView.Adapter<ListImagePickAdapter.ViewHolder> {

    List<Bitmap> arrImage;

    public ListImagePickAdapter(List<Bitmap> arrImage, Context context) {
        this.arrImage = arrImage;
        this.context = context;
    }

    Context context;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        //TODO: Refactor speed 1
        View itemView = layoutInflater.inflate(R.layout.custom_rv_image, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.img_avatar.setImageBitmap(arrImage.get(position));
    }

    @Override
    public int getItemCount() {
        return arrImage.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView img_avatar;

        public ViewHolder(View itemView) {
            super(itemView);
            img_avatar = (ImageView) itemView.findViewById(R.id.img_custom_rv_avatar);

        }
    }

}
