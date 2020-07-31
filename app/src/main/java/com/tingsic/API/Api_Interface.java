package com.tingsic.API;

import com.tingsic.POJO.Follow.DoFollowRequest;
import com.tingsic.POJO.Follow.DoFollowResponse;
import com.tingsic.POJO.LikenShare.LikenShareResponse;
import com.tingsic.POJO.LikenShare.Requests.AddLikeRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface Api_Interface {

    //    @GET("{api_key}/SMS/+91{users_phone_no}/AUTOGEN")
//    @Headers({
//            "Accept: application/json",
//            "Content-Type: application/json"
//    })
    @FormUrlEncoded
    @POST("send_otp")
    Call<MessageResponse> send_otp(@Field("mobile_no") String phone_no);


    @POST("Userfollow")
    Call<DoFollowResponse> doFollow(@Body DoFollowRequest request);


    @POST("UserUnfollow")
    Call<DoFollowResponse> doUnFollow(@Body DoFollowRequest request);

    @FormUrlEncoded
    @POST("SaveLikes")
    Call<LikenShareResponse> addLike(@Body AddLikeRequest request);


//    @GET("{api_key}/SMS/VERIFY/{session_id}/{otp_entered_by_user}")
//    Call<MessageResponse> verifyOTP(@Path("api_key")String apiKey, @Path("session_id")String session_id,@Path("otp_entered_by_user")String otp_entered_by_user);
}