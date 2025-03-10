package com.example.depth_android;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("predict")
    Call<ResponseBody> uploadImage(@Part MultipartBody.Part file);

    @GET("/")
    Call<ResponseBody> hello();

}
