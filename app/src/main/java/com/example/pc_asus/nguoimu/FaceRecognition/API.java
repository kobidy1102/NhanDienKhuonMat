package com.example.pc_asus.nguoimu.FaceRecognition;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface API {
    String Base_URL="http://khoaluantotnghiep.somee.com/";
    String groupID="";
    String personName="";
    @GET("api/values")
        Call<String> getResult();

    @Multipart
    @POST("api/recognition/{personGroup}")
    Call<String> recognitionFace(@Path("personGroup") String personGroup, @Part MultipartBody.Part photo);

    @POST("api/addPersonToGroup/{personGroup}/{personName}")
    Call<String> addPersontoGroup(@Path("personGroup") String personGroup, @Path("personName") String personName);

    @Multipart
    @POST("api/addFaceToPerson/{personGroup}/{personId}")
    Call<String> addFaceToPerson(@Path("personGroup") String personGroup, @Path("personId") String personID, @Part MultipartBody.Part photo);

    @POST("api/trainingAI/{personGroup}")
    Call<String> trainingPerson(@Path("personGroup") String personGroup);

    @POST("api/deletePerson/{personGroup}/{personId}")
    Call<String> deletePerson(@Path("personGroup") String personGroup, @Path("personId") String personId);

    @POST("api/createPersonGroup/{personGroup}/{personGroupName}")
    Call<String> createPersonGroup(@Path("personGroup") String personGroup, @Path("personGroupName") String personGroupName);

}
