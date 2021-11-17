package org.techtown.recipe.grade;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.recipe.MyApplication;
import org.techtown.recipe.R;

import org.techtown.recipe.login.LoginActivity;
import org.techtown.recipe.login.RegisterActivity;
import org.techtown.recipe.main.MainActivity;
import org.techtown.recipe.main.MainAdapter;
import org.techtown.recipe.main.MainItem;
import org.techtown.recipe.main.OnMainItemClickListener;
import org.techtown.recipe.main.RecipeActivity;
import org.techtown.recipe.mypage.WishListActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GradeActivity extends AppCompatActivity {
    private SharedPreferences preferences;

    private RatingBar ratingBar;

    private EditText commentEditText;

    private ImageButton exit_button;
    private ImageButton save_button;

    static RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //단말에 저장된 token 받아오기
        preferences = getSharedPreferences("UserToken", MODE_PRIVATE);
        String accessToken = preferences.getString("accessToken", "");
        String refreshToken = preferences.getString("refreshToken", "");

        //헤더에 토큰 넣기
        HashMap<String, String> headers = new HashMap<>();
        headers.put("accessToken", accessToken);
        headers.put("refreshToken", refreshToken);

        //버튼 세팅
        setContentView(R.layout.activity_rating);

        commentEditText = findViewById(R.id.commentEditText);

        ratingBar = findViewById(R.id.bigRatingBar);

        exit_button = findViewById(R.id.exit_button);
        save_button = findViewById(R.id.save_button);

        Intent intent = getIntent();
        String modify_RId = intent.getStringExtra("modify_RId");
        float rating_number = intent.getFloatExtra("rating_number",0);
        ratingBar.setRating(rating_number);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

            }
        });

        //뒤로 가기
        exit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = commentEditText.getText().toString();
                float rating = ratingBar.getRating();
                String grade = Float.toString(rating);

                //accessToken 인증 받기
                //url 받아오기
                MyApplication myApp = (MyApplication) getApplication();
                String url = myApp.getGlobalString();
                url += "/users/access";


                StringRequest TokenValidateRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse.statusCode == 419) {
                            //1. access token과 일치하는 사용자가 있지만 만료기간이 지난 경우
                            Log.d("statuscode1", "" + networkResponse.statusCode);

                            //url 받아오기
                            MyApplication myApp = (MyApplication) getApplication();
                            String url = myApp.getGlobalString();
                            url += "/users/reissuance";

                            StringRequest TokenReissueRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);

                                        //access token 재발급 성공
                                        String newAccessToken = jsonObject.getString("newAccessToken");
                                        preferences = getSharedPreferences("UserToken", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.putString("accessToken", newAccessToken);
                                        editor.commit();

                                        headers.put("accessToken", newAccessToken);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    NetworkResponse networkResponse = error.networkResponse;
                                    if (networkResponse.statusCode == 411 || networkResponse.statusCode == 412) {
                                        //1. refresh token, access token과 일치하는 사용자가 없는 경우 411
                                        //2. refresh token이 올바른 토큰 형태가 아닌 경우 412
                                        Log.d("statuscode2", "" + networkResponse.statusCode);
                                        Toast.makeText(getApplicationContext(), "저장된 회원정보에 오류가 발생하였습니다. 다시 로그인 해주세요.", Toast.LENGTH_SHORT).show();

                                    } else {
                                        //서버 잘못되었을 때
                                        Log.d("statuscode2", "" + networkResponse.statusCode);
                                        Toast.makeText(getApplicationContext(), "서버에 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                    Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(intent2);
                                    finish();
                                }
                            }) {
                                @Override
                                public Map<String, String> getHeaders() throws AuthFailureError {
                                    return headers;
                                }
                            };
                            RequestQueue requestQueue = Volley.newRequestQueue(GradeActivity.this);
                            requestQueue.add(TokenReissueRequest);
                        } else if (networkResponse.statusCode == 401 || networkResponse.statusCode == 402) {
                            //2. access token과 일치하는 사용자가 없는 경우 401
                            //3. 유효하지 않은 토큰 형태인 경우 402
                            Log.d("statuscode1", "" + networkResponse.statusCode);
                            Toast.makeText(getApplicationContext(), "저장된 회원정보에 오류가 발생하였습니다. 다시 로그인 해주세요.", Toast.LENGTH_SHORT).show();

                            Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent2);
                            finish();
                        } else {
                            //서버 잘못되었을 때
                            Log.d("statuscode1", "" + networkResponse.statusCode);
                            Toast.makeText(getApplicationContext(), "서버에 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();

                            Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent2);
                            finish();
                        }
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        return headers;
                    }
                };
                RequestQueue requestQueue = Volley.newRequestQueue(GradeActivity.this);
                requestQueue.add(TokenValidateRequest);

                //POST 요청
                //url 받아오기
                String Gradeurl = myApp.getGlobalString();
                Gradeurl += "/recipe/usergrade/?rId=" + modify_RId;

                JSONObject postData = new JSONObject();
                try {
                    postData.put("grade", grade);
                    postData.put("comment", comment);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest GradeRequest = new JsonObjectRequest(Request.Method.POST, Gradeurl, postData, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getApplicationContext(), "평점이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse.statusCode == 404) {
                            //서버에러(serialize에러)
                            Log.d("statuscode1", "" + networkResponse.statusCode);
                            Toast.makeText(getApplicationContext(), "서버에 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                        if (networkResponse.statusCode == 401) {
                            //이 레시피에 이미 평점을 등록한 사용자라면 평점 등록 실패
                            Log.d("statuscode1", "" + networkResponse.statusCode);
                            Toast.makeText(getApplicationContext(), "이미 이 레시피에 평점을 등록하였습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        if (networkResponse.statusCode == 411) {
                            //사용자와 맞는 accessToken이 없을 때(이 전에 클라이언트에서 검사했는데도 없는 경우니까 로그아웃 시켜야 됨)
                            Log.d("statuscode1", "" + networkResponse.statusCode);
                            Toast.makeText(getApplicationContext(), "사용자 정보에 문제가 생겼습니다.", Toast.LENGTH_SHORT).show();
                            Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent2);
                            finish();
                        }
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        return headers;
                    }
                };
                RequestQueue requestQueue2 = Volley.newRequestQueue(GradeActivity.this);
                requestQueue2.add(GradeRequest);
            }
        });

    }
}