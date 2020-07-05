package com.tingsic.API;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tingsic.POJO.Banner.Request.BannerRequest;
import com.tingsic.POJO.Banner.Response.BannerResponse;
import com.tingsic.POJO.ChangePassword.ChangePasswordRequest;
import com.tingsic.POJO.ChangePassword.ChangePasswordResponse;
import com.tingsic.POJO.Comment.Delete.Request.DeleteCommentRequest;
import com.tingsic.POJO.Comment.Delete.Response.DeleteCommentResponse;
import com.tingsic.POJO.Comment.Recieve.Request.CommentRequest;
import com.tingsic.POJO.Comment.Recieve.Response.CommentResponse;
import com.tingsic.POJO.Comment.Send.Request.AddCommentRequest;
import com.tingsic.POJO.Comment.Send.Response.AddCommentResponse;
import com.tingsic.POJO.Contest.Request.ContestRequest;
import com.tingsic.POJO.Contest.Response.ContestResponse;
import com.tingsic.POJO.ContestVideo.ContestVideoResponse;
import com.tingsic.POJO.ContestVideo.Request.ContestVideoRequest;
import com.tingsic.POJO.Follow.DoFollowRequest;
import com.tingsic.POJO.Follow.DoFollowResponse;
import com.tingsic.POJO.Following.FollowingResponse;
import com.tingsic.POJO.Following.FollowingRequest;
import com.tingsic.POJO.ForgetPassword.Request.ForgetPasswordRequest;
import com.tingsic.POJO.ForgetPassword.Response.ForgetPasswordResponse;
import com.tingsic.POJO.LikenShare.Requests.AddLikeRequest;
import com.tingsic.POJO.LikenShare.Requests.ShareRequest;
import com.tingsic.POJO.LikenShare.LikenShareResponse;
import com.tingsic.POJO.LogIn.Response.LogIn;
import com.tingsic.POJO.LogIn.Request.LogInRequest;
import com.tingsic.POJO.Search.Request.SearchRequest;
import com.tingsic.POJO.Search.Response.SearchResponse;
import com.tingsic.POJO.SignUp.Request.SignUpRequest;
import com.tingsic.POJO.SignUp.Response.SignUpResponse;
import com.tingsic.POJO.Upload.Multipart.UploadImageResponse;
import com.tingsic.POJO.Upload.Multipart.UploadVideoResponse;
import com.tingsic.POJO.Upload.Video.AddVideoRequest;
import com.tingsic.POJO.Upload.Video.AddVideoResponse;
import com.tingsic.POJO.User.Request.UserRequest;
import com.tingsic.POJO.User.UpdateProfile.Request.UpdateProfileRequest;
import com.tingsic.POJO.User.UpdateProfile.UpdateProfileResponse;
import com.tingsic.POJO.User.UserResponse;
import com.tingsic.POJO.Username.Request.UsernameRequest;
import com.tingsic.POJO.Username.UsernameResponse;
import com.tingsic.POJO.Video.DeleteVideo.DeleteVideoRequest;
import com.tingsic.POJO.Video.DeleteVideo.DeleteVideoResponse;
import com.tingsic.POJO.Video.Request.Popular.PopularVideoRequest;
import com.tingsic.POJO.Video.Request.Live.LiveVideoRequest;
import com.tingsic.POJO.Video.Request.Following.FollowingVideoRequest;
import com.tingsic.POJO.Video.VideoResponse;
import com.tingsic.POJO.hastag.HashTagVideoResponse;
import com.tingsic.POJO.payment.PaymentResponse;
import com.tingsic.POJO.paymentdetail.PaymentDetailResponse;
import com.tingsic.POJO.service.Service;
import com.tingsic.POJO.service.ServiceResponse;
import com.tingsic.POJO.userpoints.UserPointsResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiInterface {

    /*
     * Get Log In API
     * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<LogIn> getLogIn(@Body LogInRequest request);

    /*
     * Get SignUP API
     * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<SignUpResponse> getSignUp(@Body SignUpRequest request);

    /*
    * Get Forget Password API
    * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<ForgetPasswordResponse> getForgetPassword(@Body ForgetPasswordRequest request);

    /*
     * Get Change Password API
     * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<ChangePasswordResponse> getChangePassword(@Body ChangePasswordRequest request);

    /*
     * Get Existing Username API
     * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<UsernameResponse> getUsername(@Body UsernameRequest request);

    /*
     * Get Banner API
     * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<BannerResponse> getBanner(@Body BannerRequest request);

    /*
     * Get Popular Videos API
     * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<VideoResponse> getPopularVideos(@Body PopularVideoRequest request);

    /*
     * Get Live Videos API
     * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<VideoResponse> getLiveVideos(@Body LiveVideoRequest request);

    /*
     * Get Live Videos API
     * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<VideoResponse> getFollowingVideos(@Body FollowingVideoRequest request);

    /*
     * Get Own User Profile API
     * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<UserResponse> getUserProfile(@Body UserRequest request);

    /*
     * Get Search People API
     * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<SearchResponse> getSearchPeople(@Body SearchRequest request);

    /*
     * Get Contest List API
     * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<ContestResponse> getContestList(@Body ContestRequest request);

    /*
     * Get Contest Videos API
     * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<ContestVideoResponse> getContestVideos(@Body ContestVideoRequest request);

    /*
     * Get Video Comments API
     * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<CommentResponse> getVideoComment(@Body CommentRequest request);

    /*
     * Set Video Comments API
     * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<AddCommentResponse> addVideoComment(@Body AddCommentRequest request);

    /*
     * Delete Video Comments API
     * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<DeleteCommentResponse> removeVideoComment(@Body DeleteCommentRequest request);

    /*
     * Add Video after upload API
     * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<AddVideoResponse> addVideo(@Body AddVideoRequest request);

    /*
     * Add Video after upload API
     * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<DeleteVideoResponse> deleteVideo(@Body DeleteVideoRequest request);

    /*
     * Add Like Video API
     * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<LikenShareResponse> addLike(@Body AddLikeRequest request);

    /*
     * Add Share Video API
     * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<LikenShareResponse> saveShare(@Body ShareRequest request);

    /*
     * Get Following People API
     * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<FollowingResponse> getFollowingPeople(@Body FollowingRequest request);

    /*
     * Update User Profile API
     * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<UpdateProfileResponse> updateProfile(@Body UpdateProfileRequest request);

    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<ServiceResponse> countView(@Body Service service);

    /*
     * Start Following/Unfollowing the person API
     * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<DoFollowResponse> doFollow(@Body DoFollowRequest request);

    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<PaymentResponse> getPaymentService(@Body Service service);

    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<UserPointsResponse> getUserPoints(@Body Service service);

    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<PaymentDetailResponse> addPaymentDetail(@Body Service service);

    /**
     * get audio list for mixing with video
     * @param service
     * @return
     */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<JsonElement> audioService(@Body Service service);

    /*
    *
    * Multipart for posting Videos and pictures
    * */


    /*
    *For Posting Videos
    * */
    @Multipart
    @POST("upload/uploadvideo")
    Call<UploadVideoResponse> uploadVideo(@Part MultipartBody.Part video, @Part MultipartBody.Part image);

    /*
     *Update Profile Picture
     * */
    @Multipart
    @POST("upload/uploadprofilepic")
    Call<UploadImageResponse> uploadProfilePhoto(@Part MultipartBody.Part image);

    /*
     * Get HashTag Videos API
     * */
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("WebService/service")
    Call<HashTagVideoResponse> getHashTagVideo(@Body JsonObject request);

}
