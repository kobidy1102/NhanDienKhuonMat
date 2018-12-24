package com.example.pc_asus.nguoimu.FaceRecognition;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pc_asus.nguoimu.AppUtil;
import com.example.pc_asus.nguoimu.Model.PersonTraining;
import com.example.pc_asus.nguoimu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ListPersonTrainedAdapter extends RecyclerView.Adapter<ListPersonTrainedAdapter.ViewHolder> {

    List<PersonTraining> arr;
    Context context;
   // private final View.OnClickListener mOnClickListener = new MyOnClickListener();
    public ListPersonTrainedAdapter(List<PersonTraining> arr, Context context) {
        this.arr = arr;
        this.context = context;
    }



    @NonNull
    @Override
    public ListPersonTrainedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        //TODO: Refactor speed 1
        View itemView = layoutInflater.inflate(R.layout.custom_list_trained, parent, false);
        return new ListPersonTrainedAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ListPersonTrainedAdapter.ViewHolder holder, final int position) {
      //  holder.img_avatar.setImageBitmap(arr.get(position));
        holder.tv_name.setText(arr.get(position).name);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.fitCenter();
        requestOptions.placeholder(R.mipmap.user);
        Glide.with(context)
                .load(arr.get(position).photoURL)
                .apply(requestOptions)
                //   .override(200,150)
                .into(holder.img_avatar);

        holder.llItemPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, ""+arr.get(position).name, Toast.LENGTH_SHORT).show();
            }
        });

        holder.imgDeletePerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deletePerson(position);

            }
        });


        holder.imgAddFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(context,AddFaceToPersonActivity.class);
                intent.putExtra("personId", arr.get(position).id);
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return arr.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView img_avatar,imgAddFace, imgDeletePerson;
        private TextView  tv_name;
        private LinearLayout llItemPerson;

        public ViewHolder(View itemView) {
            super(itemView);
            img_avatar = (ImageView) itemView.findViewById(R.id.custom_list_trained_img_avatar);
            tv_name= itemView.findViewById(R.id.custom_list_trained_tv_name);
            llItemPerson= itemView.findViewById(R.id.ll_item_person);
            imgAddFace = (ImageView) itemView.findViewById(R.id.img_add_face);
            imgDeletePerson = (ImageView) itemView.findViewById(R.id.img_delete_person);



        }
    }



    private  void deletePerson( final int position){

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Vui lòng chờ...");
        progressDialog.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API.Base_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final API api = retrofit.create(API.class);

        Call<String> call = api.deletePerson(AppUtil.getUidLowerCase(),arr.get(position).id);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                progressDialog.dismiss();
                Toast.makeText(context, ""+response.body(), Toast.LENGTH_SHORT).show();
                if (response.body().contains("Error")){
                    Log.e("abc", "result=" + response.body());

                }else{

                    AppUtil.getmDatabase().child("NguoiMu").child("Training").child(AppUtil.getUid()).child(arr.get(position).key).removeValue();

                    arr.remove(position);
                    notifyDataSetChanged();
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(context, "Lỗi! "+t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("abc", "lỗi delete person");
            }
        });
    }
}
