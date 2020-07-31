package com.tingsic.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
//import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.GsonBuilder;
import com.stfalcon.smsverifycatcher.OnSmsCatchListener;
import com.stfalcon.smsverifycatcher.SmsVerifyCatcher;
import com.tingsic.API.Api_Interface;
import com.tingsic.API.MessageResponse;
import com.tingsic.NotificationService;
import com.tingsic.POJO.SignUp.Request.SignUpRequest;
import com.tingsic.POJO.SignUp.Response.SignUpResponse;
import com.tingsic.R;
import com.tingsic.Utils.PrefManager;
import com.tingsic.activity.HomeActivity;
import com.tingsic.activity.LogInActivity;
import com.tingsic.activity.LoginWithFacebook;
import com.tingsic.activity.MainActivity;
import com.tingsic.activity.PrivacyPolicy;
import com.tingsic.activity.SignUpActivity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.gson.Gson;
import com.tingsic.API.ApiClient;
import com.tingsic.API.ApiInterface;
import com.tingsic.POJO.LogIn.Request.Data;
import com.tingsic.POJO.LogIn.Request.LogInRequest;
import com.tingsic.POJO.LogIn.Request.Request;
import com.tingsic.POJO.LogIn.Response.LogIn;

import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.app.Activity.RESULT_OK;

public class LogInBSFragment extends BottomSheetDialogFragment {

    private static final String TAG = LogInBSFragment.class.getSimpleName();
    private boolean isProfileActivity;

    private static final int LOG_IN = 819;
    FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 234;
    GoogleSignInClient mGoogleSignInClient;
    private CallbackManager callbackManager;
    private AccessToken mAccessToken;

    private ProgressDialog dialog;
    private static final int GOOGLE_SIGN_IN = 80;
    JSONObject json;
    CustomDialogVerifyClass customDialogVerifyClass;
    CustomDialogClass customDialogClass;
    static Retrofit retrofit = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bs_login, container, false);

        if (getArguments() != null) {
            isProfileActivity = getArguments().getBoolean("isProfileActivity", false);
        }

        FacebookSdk.sdkInitialize(getContext());
        LoginManager.getInstance().logOut();

        dialog = new ProgressDialog(getContext());
        dialog.setMessage("Loading...");
        dialog.setCancelable(false);

        callbackManager = CallbackManager.Factory.create();
        //first we intialized the FirebaseAuth object
        mAuth = FirebaseAuth.getInstance();

        //Then we need a GoogleSignInOptions object
        //And we need to build it as below
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //Then we will get the GoogleSignInClient object from GoogleSignIn class
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        mGoogleSignInClient.signOut();
//                .addOnCompleteListener((Executor) this, new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        // ...
//                        Log.i(TAG, "onComplete: signout");
//                    }
//                });

        initView(view);

        return view;
    }



    private void initView(View view) {
        printKeyHash();
        //Terms and Conditions;
        TextView tvTerms = view.findViewById(R.id.tv_login_terms);

        SpannableString ss = new SpannableString(getResources().getString(R.string.terms_and_conditions));
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        ss.setSpan(boldSpan, 48, 66, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ClickableSpan span = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(getContext(), PrivacyPolicy.class));
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(span, 48, 66, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvTerms.setText(ss);
        tvTerms.setMovementMethod(LinkMovementMethod.getInstance());
        tvTerms.setHighlightColor(Color.TRANSPARENT);
        //--------------****************------------------//

        TextView tvNeedAC = view.findViewById(R.id.tv_need_account);

        String text = "<font color=#000000>You need a </font><font color=#FF1744>Tingsic</font><font color=#000000> account to continue</font>";
        tvNeedAC.setText(Html.fromHtml(text));

        Button btnSignUp = view.findViewById(R.id.btn_bs_signup);
        Button btnSignUpMobile = view.findViewById(R.id.btn_signup_mobile);
        ImageView facebook_btn = view.findViewById(R.id.facebook_btn);
        ImageView google_btn = view.findViewById(R.id.google_btn);
        //LoginButton login_button = view.findViewById(R.id.login_button);
        Button btnAlredyAc = view.findViewById(R.id.btn_already_account);

        btnSignUpMobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialogClass=new CustomDialogClass(getActivity());
                customDialogClass.show();
            }
        });

        facebook_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();
                //Loginwith_FB();
                if (!isProfileActivity) {
                    dismiss();
                }
                if (getActivity() != null) {
                    Loginwith_FB();
//                    Intent intent = new Intent(getActivity(), LoginWithFacebook.class);
//                    intent.putExtra("isfb",true);
//                    getActivity().startActivityForResult(intent, LOG_IN);
                }
                //startActivity(new Intent(getContext(), LoginWithFacebook.class));
            }
        });

        google_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isProfileActivity) {
                    dismiss();
                }
                if (getActivity() != null) {
                    Loginwith_Google();

//                    Intent intent = new Intent(getActivity(), LoginWithFacebook.class);
//                    intent.putExtra("isfb",false);
//                    getActivity().startActivityForResult(intent, LOG_IN);
                }
            }
        });

        text = "<font color=#FFFFFF>Already have account?   </font><font color=#FFD600>LogIn</font>";
        btnAlredyAc.setText(Html.fromHtml(text));

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isProfileActivity) {
                    dismiss();
                }
                if (getActivity() != null) {
                    getActivity().startActivityForResult(new Intent(getActivity(), SignUpActivity.class), LOG_IN);
                }
            }
        });
        btnAlredyAc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isProfileActivity) {
                    dismiss();
                }
                if (getActivity() != null) {
                    getActivity().startActivityForResult(new Intent(getActivity(), LogInActivity.class), LOG_IN);
                }
            }
        });

    }

    public void show(FragmentManager fragmentManager, String tag) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
//        ft.add(LogInBSFragment.this, tag);
        ft.commit();
        Log.e(TAG, "show: ");
    }

    public class CustomDialogClass extends Dialog {

        public Activity c;
        public Dialog d;
        public TextView verify_otp,dialogTitle;
        EditText otp;


        public CustomDialogClass(Activity a) {
            super(a);
            // TODO Auto-generated constructor stub
            this.c = a;
        }

        @SuppressLint("WrongViewCast")
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.activity_otp_login);
            verify_otp = findViewById(R.id.verify_otp);
            dialogTitle = findViewById(R.id.dialogTitle);
            otp = findViewById(R.id.otp);
            verify_otp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.e(TAG, "onClick: "+otp.getText().toString());
                    sendotp(otp.getText().toString());



                }
            });

        }

        private void requestFocus(View view) {
            if (view.requestFocus()) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        }

        private boolean validateMobileNumber() {
            String phone = otp.getText().toString();
            if (phone.trim().isEmpty() || phone.length() != 10){
                otp.setError(getString(R.string.err_mobile_no));
                requestFocus(otp);
                return false;
            }
            return true;
        }


        public void sendotp(String s) {
            dialog.show();
            Log.e(TAG, "sendotpmessage: "+s);

            try {
                Api_Interface apiService =
                        Api_Client.getClient().create(Api_Interface.class);

                Call<MessageResponse> call = apiService.send_otp(s);
                call.enqueue(new Callback<MessageResponse>() {
                    @Override
                    public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
//                 sessionId = response.body().getDetails();

                        dialog.hide();
                        if (response.isSuccessful()) {
                            Log.e(TAG, "onResponse: ");
                            Log.e("SenderIDsucc", response.body().getData());
                            Log.e("SenderIDmess", response.body().getMessage());
//                        Log.e("SenderID", response.body().getSuccess());
                            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString("otp",response.body().getData()).apply();
//                            customDialogClass=new CustomDialogClass(getActivity());
                            customDialogClass.dismiss();
                            customDialogVerifyClass = new CustomDialogVerifyClass(getActivity(),s);
                            customDialogVerifyClass.show();
                        }
                        else {
                            Log.e(TAG, "onResponse: ");
                        }

                        //you may add code to automatically fetch OTP from messages.
                    }

                    @Override
                    public void onFailure(Call<MessageResponse> call, Throwable t) {
                        Log.e("ERROR", t.toString());
                    }

                });
            }
            catch (Exception e){
                Log.e(TAG, "sendotpexx: "+e.getMessage());
            }

        }



    }



    public class CustomDialogVerifyClass extends Dialog implements
            android.view.View.OnClickListener {
        public Activity c;
        public Dialog d;
        public TextView verify_otp;
        SmsVerifyCatcher smsVerifyCatcher;
        EditText otp;
        String mobile_no;

        public CustomDialogVerifyClass(Activity a, String mobile_no) {
            super(a);
            // TODO Auto-generated constructor stub
            this.c = a;
            this.mobile_no = mobile_no;
            Log.e(TAG, "CustomDialogVerifyClass: ");
        }


        @SuppressLint("WrongViewCast")
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.activity_otp_verify);
            verify_otp = findViewById(R.id.verify_otp);
            otp = findViewById(R.id.otp);
            verify_otp.setOnClickListener(this);


        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.verify_otp:

                    if (otp.getText().toString().isEmpty()) {
                        Toast.makeText(c, "Please Enter OTP", Toast.LENGTH_SHORT).show();
                    } else if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("otp", "").equalsIgnoreCase(otp.getText().toString())) {
                        Toast.makeText(c, "OTP Verified", Toast.LENGTH_SHORT).show();
                        getSignUpAPI(mobile_no);
                    } else if (!PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("otp", "").equalsIgnoreCase(otp.getText().toString())) {
                        Toast.makeText(c, "OTP is incorrect", Toast.LENGTH_SHORT).show();
                    }

                    break;
            }
        }


//     private void sendotp() {
//
//         queue = Volley.newRequestQueue(getContext());
//         StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://tingsic.com/WebService/send_otp",
//                 new Response.Listener<String>() {
//                     @Override
//                     public void onResponse(String response) {
//                         Log.e(TAG, "responsetostring: " + response.toString());
//                         try{
//                             json = new JSONObject(response);
//                             Log.e(TAG, "onResponse: "+json);
//                             if (json.getString("status").equalsIgnoreCase("success"))
//                             {
//                                 try {
//                                     Toast.makeText(getContext(), "OTP Sent Successfully", Toast.LENGTH_SHORT).show();
//                                 } catch (Exception e)
//                                 {
//                                     Log.e(TAG, "onResponse: "+e.getMessage());
//                                 }
//
//                             } else {
//                                 Toast.makeText(getContext(), json.getString("message"), Toast.LENGTH_SHORT).show();
//                             }
//                         }
//                         catch (Exception e)
//                         {
//
//                             e.printStackTrace();
//                         }
//                     }
//                 },
//                 new Response.ErrorListener() {
//                     @Override
//                     public void onErrorResponse(VolleyError error) {
//                         Log.e(TAG, "onErrorResponse: " + error.getMessage());
//                     }
//                 })
//         {
//             @Override
//             protected Map<String, String> getParams() {
//                 Map<String, String> params = new HashMap<>();
//                 params.put("mobile_no", mobile_no);
//                 return params;
//             }
//         };
//         queue.add(stringRequest);
//     }


//     @Override
//     protected void onStart() {
//         super.onStart();
//         smsVerifyCatcher.onStart();
//     }
//
//     @Override
//     protected void onStop() {
//         super.onStop();
//         smsVerifyCatcher.onStop();
//     }

        /**
         * need for Android 6 real time permissions
         */
    }

    public static class Api_Client {

        public static final String BASE_URL = "http://tingsic.com/WebService/";
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();


        public static Retrofit getClient() {
            if (retrofit==null) {
                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }

            return retrofit;

        }
    }

    private void getSignUpAPI(String mobile_no) {
        dialog.show();

        SignUpRequest service = new SignUpRequest();

        com.tingsic.POJO.SignUp.Request.Request request = new com.tingsic.POJO.SignUp.Request.Request();


        String deviceId = NotificationService.getToken(getContext());

        com.tingsic.POJO.SignUp.Request.Data data = new com.tingsic.POJO.SignUp.Request.Data();
        data.setType("customer");
        data.setFirstName("demo");
        data.setLastName("demo");
        data.setUsername("demo".concat(mobile_no));
        data.setEmail(mobile_no.concat("@gmail.com"));
        data.setPassword("");
        data.setPincode("");
        data.setMobile(mobile_no);
        data.setPlatform("android");
        data.setDeviceId(deviceId);
        data.setLat("2.17");
        data.setLng("3.501");


        data.setReferenceCode("");


        request.setData(data);


        service.setService("signup");
        service.setRequest(request);



        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<SignUpResponse> signUpCall = apiInterface.getSignUp(service);
        signUpCall.enqueue(new Callback<SignUpResponse>() {
            @Override
            public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {
                Log.e(TAG, "onResponse: "+response.message());
//                Log.e(TAG, "onResponse: "+response.body().getMessage());
                Log.e(TAG, "onResponse: "+response.errorBody());
                if (response.isSuccessful()){
                    Log.e(TAG, "onResponsesucc: "+response);
                    customDialogVerifyClass.dismiss();
                    if (response.body().getSuccess() == 1){
                        Log.e(TAG, "onResponse: "+response.raw().request().url());
                        Log.e(TAG, "onResponse: "+response.body());

                        int id = response.body().getUserId();
                        String token = response.body().getToken();

                        Toast.makeText(getContext(), "SignUp success.", Toast.LENGTH_SHORT).show();

                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isLoggedIn",true).apply();
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("id",id).apply();
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("token",token).apply();

                        String[] profilePic = response.body().getData().getProfilepic().split("/");
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("profilePic",profilePic[profilePic.length-1]).apply();

                        PrefManager.setFullName(getContext(),response.body().getData().getFirstName()+" "+response.body().getData().getLastName());

                        PrefManager.setReferralCode(getContext(),response.body().getData().getReferenceCode());

                        Intent intent = new Intent(getContext(), MainActivity.class);
                        getContext().startActivity(intent);
                        getActivity().finish();

                    }
                    else if (response.body().getSuccess() == 3) {
                        Log.e(TAG, "onResponse: Error success=3");
                        Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();

                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isLoggedIn",false).apply();
                    }
                    else if (response.body().getSuccess() == 2) {
                        Log.e(TAG, "onResponse: Error success=2");
                        Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isLoggedIn",false).apply();
                    }
                    else {
                        Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isLoggedIn",false).apply();
                        Log.e(TAG, "onResponse: Error success=0");
                    }
                    Log.e(TAG, "onResponse: Error success= "+response.body().getSuccess()+" "+response.body().getMessage());
                }
                else {
                    Log.e(TAG, "onResponseelse: "+response.message());
                }
                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<SignUpResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: "+call);
                Log.e(TAG, "onFailure: "+t.getMessage());
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isLoggedIn",false).apply();
                dialog.dismiss();
                Intent intent=new Intent(getContext(),LogInActivity.class);
                startActivity(intent);
            }
        });
    }


    private void getUserProfile(AccessToken currentAccessToken) {

        Log.i(TAG, "getUserProfile: "+currentAccessToken.getToken());

        GraphRequest request = GraphRequest.newMeRequest(
                currentAccessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        //You can fetch user info like thisâ€¦
                        //object.getJSONObject(â€œpictureâ€).
                        //getJSONObject(â€œdataâ€).getString(â€œurlâ€);
                        //object.getString(â€œnameâ€);
                        //object.getString(â€œemailâ€));
                        //object.getString(â€œidâ€));



                        if (object.has("email")) {

                            String id = object.optString("email");

                            Call_Api_For_Signup(""+id,""+object.optString("first_name")
                                    ,""+object.optString("last_name"));

                        } else {

                            Toast.makeText(getContext(), "Email not found", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(getContext(), SignUpActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                            startActivity(intent);
                            getActivity().finish();


                        }

                        Log.w(LoginWithFacebook.class.getSimpleName(), "onCompleted: "+object.toString());


                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,birthday,first_name,gender,last_name,email,picture.width(200)");
        request.setParameters(parameters);
        request.executeAsync();

    }


    // Bottom two function are related to Fb implimentation


    //facebook implimentation
    public void Loginwith_FB() {

        LoginManager.getInstance()
                .logInWithReadPermissions(getActivity(),
                        Arrays.asList("public_profile", "email"));

        // initialze the facebook sdk and request to facebook for login


        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mAccessToken = loginResult.getAccessToken();
                getUserProfile(mAccessToken);
            }

            @Override
            public void onCancel() {
                // App code
                Toast.makeText(getActivity(), "Login Cancel", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("resp", "" + error.toString());
                Toast.makeText(getActivity(), "Login Error" + error.toString(), Toast.LENGTH_SHORT).show();
            }

        });

    }

    public void Loginwith_Google() {

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    public void Call_Api_For_Signup(final String id,
                                    final String f_name,
                                    final String l_name) {

        dialog.show();

       /* Log.v("AAAAAAAAA", "id=> " + id);
        Log.v("AAAAAAAAA", "f_name=> " + f_name);
        Log.v("AAAAAAAAA", "l_name=> " + l_name);
        Log.v("AAAAAAAAA", "picture=> " + picture);
        Log.v("AAAAAAAAA", "singnup_type=> " + singnup_type);*/
        Log.v("AAAAAAAAA", "id=> " + id);
        LogInRequest service = new LogInRequest();
        Request request = new Request();
        Data data = new Data();
        data.setEmail(id);
        request.setData(data);
        service.setService("checkexistemail");
        service.setRequest(request);

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<LogIn> logInCall = apiInterface.getLogIn(service);
        Log.v("AAAAAAAAA","service=> "+new Gson().toJson(service));
        Log.v("AAAAAAAAA",""+logInCall.request());
        logInCall.enqueue(new Callback<LogIn>() {
            @Override
            public void onResponse(Call<LogIn> call, Response<LogIn> response) {
                Log.i("AAAAAA", "onResponse: "+response.code());
                if (response.isSuccessful()){
                    if (response.body().getSuccess() == 1)
                    {

                        int id = Integer.parseInt(response.body().getData().getUserId());
                        String token = response.body().getToken();

                        Toast toast = Toast.makeText(getContext(), "Log in Successful!", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();

                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isLoggedIn",true).apply();
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("id",id).apply();
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("token",token).apply();
                        String[] profilePic = response.body().getData().getProfilepic().split("/");
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("profilePic",profilePic[profilePic.length-1]).apply();

                        PrefManager.setFullName(getContext(),response.body().getData().getFirstName()+" "+response.body().getData().getLastName());

                        PrefManager.setReferralCode(getContext(),response.body().getData().getReferenceCode());

                        PrefManager.setUserNameEditable(getContext(),false);

                        Intent intent = new Intent(getContext(), MainActivity.class);
                        getContext().startActivity(intent);
                        getActivity().finish();

                    }
                    else {
                        getSignUpAPI(f_name, l_name, id, id, "", "");
                    }
                }
                else{
                    Toast.makeText(getContext(), "Server Error", Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();

            }

            @Override
            public void onFailure(Call<LogIn> call, Throwable t) {
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isLoggedIn",false).apply();
                dialog.dismiss();

            }
        });
    }

    private void getSignUpAPI(String firstName,
                              String lastName,
                              String email,
                              String username,
                              String password,
                              String mobileNumber) {
        Log.e(TAG, "getSignUpAPI: "+mobileNumber);

        SignUpRequest service = new SignUpRequest();

        com.tingsic.POJO.SignUp.Request.Request request = new com.tingsic.POJO.SignUp.Request.Request();

        String deviceId = NotificationService.getToken(getContext());

        com.tingsic.POJO.SignUp.Request.Data data = new com.tingsic.POJO.SignUp.Request.Data();
        data.setType("customer");
        data.setFirstName(firstName);
        data.setLastName(lastName);
        data.setUsername(username);
        data.setEmail(email);
        data.setPassword(password);
        data.setPincode("");
        data.setMobile(mobileNumber);
        data.setPlatform("android");
        data.setDeviceId(deviceId);
        data.setLat("2.17");
        data.setLng("3.501");

        request.setData(data);

        service.setService("signup");
        service.setRequest(request);


        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<SignUpResponse> signUpCall = apiInterface.getSignUp(service);
        signUpCall.enqueue(new Callback<SignUpResponse>() {
            @Override
            public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {
                Log.e(TAG, "onResponse: "+response.message());
                if (response.isSuccessful()){
                    if (response.body().getSuccess() == 1){

                        int id = response.body().getUserId();
                        String token = response.body().getToken();

                        Toast.makeText(getContext(), "SignUp success.", Toast.LENGTH_SHORT).show();

                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isLoggedIn",true).apply();
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("id",id).apply();
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("token",token).apply();

                        String[] profilePic = response.body().getData().getProfilepic().split("/");
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("profilePic",profilePic[profilePic.length-1]).apply();

                        PrefManager.setFullName(getContext(),response.body().getData().getFirstName()+" "+response.body().getData().getLastName());

                        PrefManager.setReferralCode(getContext(),response.body().getData().getReferenceCode());

                        PrefManager.setUserNameEditable(getContext(),false);

                        Intent intent = new Intent(getContext(), LogInBSFragment.class);
                        intent.putExtra("isLogInSuccessful",true);
                        getActivity().setResult(RESULT_OK,intent);
                        getActivity().finish();

                    }
                    else if (response.body().getSuccess() == 3) {
                        ///ilEmail.setError(getString(R.string.err_existing_email));
                        //requestFocus(etEmail);
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isLoggedIn",false).apply();
                    }
                    else if (response.body().getSuccess() == 2) {
                        //ilUserName.setErrorEnabled(true);
                        //ilUserName.setError(getString(R.string.existing_user));
                        //isUsername = false;
                        //requestFocus(etUserName);
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isLoggedIn",false).apply();
                    }
                    else {
                        Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isLoggedIn",false).apply();
                        Log.e(TAG, "onResponse: Error success=0");

                    }
                    Log.e(TAG, "onResponse: Error success= "+response.body().getSuccess()+" "+response.body().getMessage());
                }
                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<SignUpResponse> call, Throwable t) {
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isLoggedIn",false).apply();
                dialog.dismiss();
            }
        });
    }




    public void printKeyHash()  {
        try {
            PackageInfo info = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName() , PackageManager.GET_SIGNATURES);
            for(Signature signature:info.signatures)
            {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("keyhash" , Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void handleFacebookAccessToken(final AccessToken token) {
        // if user is login then this method will call and
        // facebook will return us a token which will user for get the info of user
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        Log.d("resp_token",token.getToken()+"");
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final String id = Profile.getCurrentProfile().getId();
                            GraphRequest request = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject user, GraphResponse graphResponse) {

                                    Log.d("resp",user.toString());
                                    //after get the info of user we will pass to function which will store the info in our server
                                    Call_Api_For_Signup(""+id,""+user.optString("first_name")
                                            ,""+user.optString("last_name"),
                                            "https://graph.facebook.com/"+id+"/picture?width=500&width=500",
                                            "facebook");

                                }
                            });

                            // here is the request to facebook sdk for which type of info we have required
                            Bundle parameters = new Bundle();
                            parameters.putString("fields", "last_name,first_name,email");
                            request.setParameters(parameters);
                            request.executeAsync();
                        } else {

                            Toast.makeText(getActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //if the requestCode is the Google Sign In code that we defined at starting
        if (requestCode == RC_SIGN_IN) {

            //Getting the GoogleSignIn Task
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                Log.v("Task",""+new Gson().toJson(task));
                //Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.v("Account",""+new Gson().toJson(account));

                //authenticating with firebase
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == GOOGLE_SIGN_IN) {

//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            handleSignInResult(task);
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                Log.e("Task",""+new Gson().toJson(task));
                //Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.e("Account",""+new Gson().toJson(account));

                //authenticating with firebase
                firebaseAuthWithGoogle(account);
                handleSignInResult(task);
            } catch (ApiException e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("Google ID", "firebaseAuthWithGoogle:" + acct.getId());
        Log.d("Google ID", "firebaseAuthWithGoogle:" + acct.getId());

        //getting the auth credential
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        //Now using firebase we are signing in the user here
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("Success", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("AAAAA", "signInWithCredential:failure", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }


    public void Call_Api_For_Signup(String id,
                                    String f_name,
                                    String l_name,
                                    String picture,
                                    String singnup_type) {

       /* Log.v("AAAAAAAAA", "id=> " + id);
        Log.v("AAAAAAAAA", "f_name=> " + f_name);
        Log.v("AAAAAAAAA", "l_name=> " + l_name);
        Log.v("AAAAAAAAA", "picture=> " + picture);
        Log.v("AAAAAAAAA", "singnup_type=> " + singnup_type);*/
        Log.v("AAAAAAAAA", "id=> " + id);
        LogInRequest service = new LogInRequest();
        Request request = new Request();
        Data data = new Data();
        data.setEmail(id);
        request.setData(data);
        service.setService("checkexistemail");
        service.setRequest(request);

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<LogIn> logInCall = apiInterface.getLogIn(service);
        Log.v("AAAAAAAAA","service=> "+new Gson().toJson(service));
        Log.v("AAAAAAAAA",""+logInCall.request());
        logInCall.enqueue(new Callback<LogIn>() {
            @Override
            public void onResponse(Call<LogIn> call, Response<LogIn> response) {
                Log.i("AAAAAA", "onResponse: "+response.code());
                if (response.isSuccessful()){
                    if (response.body().getSuccess() == 1)
                    {

                        int id = Integer.parseInt(response.body().getData().getUserId());
                        String token = response.body().getToken();

                        Toast toast = Toast.makeText(getActivity(), "Log in Successful!", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();

                        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("isLoggedIn",true).apply();
                        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt("id",id).apply();
                        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString("token",token).apply();
                        String[] profilePic = response.body().getData().getProfilepic().split("/");
                        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString("profilePic",profilePic[profilePic.length-1]).apply();

                        dismiss();
                    }
                    else {
                        Toast toast = Toast.makeText(getActivity(), response.body().getMessage(), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();
                    }
                }
                else{
                    PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("isLoggedIn",false).apply();
                    try {
                        Log.e("AAAAAA", "onResponse: error: "+response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<LogIn> call, Throwable t) {
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("isLoggedIn",false).apply();

            }
        });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            System.out.println(account);
            // Signed in successfully, show authenticated UI.
            onLoggedIn(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            e.printStackTrace();
            onLoggedIn(null);
        }
    }

    private void onLoggedIn(GoogleSignInAccount account) {

        if (account != null) {
            Log.d(TAG, "onLoggedIn() returned: " + account.getDisplayName() );
            Log.d(TAG, "onLoggedIn() returned: " + account.getEmail() );
            Log.d(TAG, "onLoggedIn() returned: " + account.getIdToken() );
            Log.d(TAG, "onLoggedIn() returned: " + account.getPhotoUrl() );
            Log.d(TAG, "onLoggedIn() returned: " + account.getId() );

            String fname,lname;
            try {
                String[] name = account.getDisplayName().split(" ");
                fname = name[0];
                lname = name[1];

            } catch (Exception e) {
                fname = account.getDisplayName();
                lname = "";
            }



            Call_Api_For_Signup(account.getEmail(),fname, lname);

        }

        //todo handle Missing Bday and email from fb and google


    }


    @Override
    public void onCancel(DialogInterface dialog) {
        if (isProfileActivity) {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        }
        super.onCancel(dialog);
    }

}
