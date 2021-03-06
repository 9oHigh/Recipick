package org.techtown.recipe.mypage;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.recipe.MyApplication;
import org.techtown.recipe.R;
import org.techtown.recipe.grade.GradeActivity;
import org.techtown.recipe.login.LoginActivity;
import org.techtown.recipe.main.MainActivity;
import org.techtown.recipe.main.OrderItem;
import org.techtown.recipe.main.RecipeActivity;
import org.techtown.recipe.ranking.RankingActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyPageActivity extends AppCompatActivity {

    private SharedPreferences preferences;

    Button withdraw_button;
    Button favorite_button;
    TextView idTextView;

    private BottomNavigationView navigation;

    static RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //????????? ????????? token ????????????
        preferences = getSharedPreferences("UserToken", MODE_PRIVATE);
        String accessToken = preferences.getString("accessToken", "");
        String refreshToken = preferences.getString("refreshToken", "");

        //????????? ?????? ??????
        HashMap<String, String> headers = new HashMap<>();
        headers.put("accessToken", accessToken);
        headers.put("refreshToken", refreshToken);

        //?????? ??????
        setContentView(R.layout.activity_mypage);
        withdraw_button=findViewById(R.id.withdraw_button);
        favorite_button=findViewById(R.id.favorite_button);
        idTextView=findViewById(R.id.idTextView);

        navigation = findViewById( R.id.navigation );

        navigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        Intent intent1 = new Intent( MyPageActivity.this, MainActivity.class );
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity( intent1 );
                        overridePendingTransition(0,0);
                        finish();
                        break;
                    case R.id.ranking:
                        Intent intent2 = new Intent( MyPageActivity.this, RankingActivity.class );
                        intent2.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity( intent2 );
                        overridePendingTransition(0,0);
                        finish();
                        break;
                }
                return true;
            }
        });
        navigation.setSelectedItemId(R.id.mypage);

        //accessToken ?????? ??????
        //url ????????????
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
                    //1. access token??? ???????????? ???????????? ????????? ??????????????? ?????? ??????
                    Log.d("statuscode1", "" + networkResponse.statusCode);

                    //url ????????????
                    MyApplication myApp = (MyApplication) getApplication();
                    String url = myApp.getGlobalString();
                    url += "/users/reissuance";

                    StringRequest TokenReissueRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                //access token ????????? ??????
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
                                //1. refresh token, access token??? ???????????? ???????????? ?????? ?????? 411
                                //2. refresh token??? ????????? ?????? ????????? ?????? ?????? 412
                                Log.d("statuscode2", "" + networkResponse.statusCode);
                                Toast.makeText(getApplicationContext(), "????????? ??????????????? ????????? ?????????????????????. ?????? ????????? ????????????.", Toast.LENGTH_SHORT).show();

                            } else {
                                //?????? ??????????????? ???
                                Log.d("statuscode2", "" + networkResponse.statusCode);
                                Toast.makeText(getApplicationContext(), "????????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
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
                    RequestQueue requestQueue = Volley.newRequestQueue(MyPageActivity.this);
                    requestQueue.add(TokenReissueRequest);
                } else if (networkResponse.statusCode == 401 || networkResponse.statusCode == 402) {
                    //2. access token??? ???????????? ???????????? ?????? ?????? 401
                    //3. ???????????? ?????? ?????? ????????? ?????? 402
                    Log.d("statuscode1", "" + networkResponse.statusCode);
                    Toast.makeText(getApplicationContext(), "????????? ??????????????? ????????? ?????????????????????. ?????? ????????? ????????????.", Toast.LENGTH_SHORT).show();

                    Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent2);
                    finish();
                } else {
                    //?????? ??????????????? ???
                    Log.d("statuscode1", "" + networkResponse.statusCode);
                    Toast.makeText(getApplicationContext(), "????????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();

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
        RequestQueue requestQueue = Volley.newRequestQueue(MyPageActivity.this);
        requestQueue.add(TokenValidateRequest);

        //????????? ?????? ????????????
        //url ????????????
        String IdUrl = myApp.getGlobalString();
        IdUrl += "/users/access/id";

        StringRequest IdRequest = new StringRequest(Request.Method.GET, IdUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String UserId = jsonObject.optString("userId")+"??? ???????????????.";
                    idTextView.setText(UserId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                //???????????? ?????? accessToken??? ?????? ???(??? ?????? ????????????????????? ?????????????????? ?????? ???????????? ???????????? ????????? ???)
                if (networkResponse.statusCode == 411) {
                    Log.d("statuscode", "" + networkResponse.statusCode);
                    Toast.makeText(getApplicationContext(), "????????? ????????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                    //login ???????????? ????????????
                    Intent intent = new Intent( MyPageActivity.this, LoginActivity.class );
                    startActivity( intent );
                    finish();
                } else {
                    Log.d("statuscode", "" + networkResponse.statusCode);
                    Toast.makeText(getApplicationContext(), "????????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };
        RequestQueue requestQueue2 = Volley.newRequestQueue(MyPageActivity.this);
        requestQueue2.add(IdRequest);

        favorite_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //?????????
                Intent intent = new Intent( MyPageActivity.this, WishListActivity.class );
                startActivity( intent );
            }
        });

        //???????????? ??????
        withdraw_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MyPageActivity.this);

                builder.setTitle("????????????").setMessage("?????? ????????? ???????????? ????????? ???????????????.");

                builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //url ????????????
                        MyApplication myApp = (MyApplication) getApplication();
                        String url=myApp.getGlobalString();
                        url += "/users/secession";

                        StringRequest SecessionRequest = new StringRequest(Request.Method.DELETE, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                //?????? ?????? ??????
                                preferences = getSharedPreferences("UserToken", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.remove("accessToken").commit();
                                editor.remove("refreshToken").commit();

                                Toast.makeText(getApplicationContext(), "?????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();

                                //login ???????????? ????????????
                                Intent intent = new Intent( MyPageActivity.this, LoginActivity.class );
                                startActivity( intent );
                                finish();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                NetworkResponse networkResponse=error.networkResponse;
                                if (networkResponse.statusCode == 401) {
                                    //1. access token ???????????? ????????? ??? reissueance
                                    Log.d("statuscode", "" + networkResponse.statusCode);

                                    //url ????????????
                                    MyApplication myApp = (MyApplication) getApplication();
                                    String url=myApp.getGlobalString();
                                    url += "/users/reissuance";

                                    StringRequest TokenReissueRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                JSONObject jsonObject = new JSONObject(response);

                                                //access token ????????? ??????
                                                String newAccessToken = jsonObject.getString("newAccessToken");
                                                preferences = getSharedPreferences("UserToken", MODE_PRIVATE);
                                                SharedPreferences.Editor editor = preferences.edit();
                                                editor.putString("accessToken", newAccessToken);
                                                editor.commit();

                                                headers.put("accessToken", newAccessToken);

                                                //reissueance ???????????? ???

                                                //url ????????????
                                                MyApplication myApp = (MyApplication) getApplication();
                                                String url=myApp.getGlobalString();
                                                url += "/users/secession";

                                                StringRequest SecessionRequest = new StringRequest(Request.Method.DELETE, url, new Response.Listener<String>() {
                                                    @Override
                                                    public void onResponse(String response) {
                                                        preferences = getSharedPreferences("UserToken", MODE_PRIVATE);
                                                        SharedPreferences.Editor editor = preferences.edit();
                                                        editor.remove("accessToken").commit();
                                                        editor.remove("refreshToken").commit();
                                                        editor.commit();

                                                        Toast.makeText(getApplicationContext(), "?????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();

                                                        //login ???????????? ????????????
                                                        Intent intent = new Intent( MyPageActivity.this, LoginActivity.class );
                                                        startActivity( intent );
                                                        finish();
                                                    }
                                                }, new Response.ErrorListener() {
                                                    @Override
                                                    public void onErrorResponse(VolleyError error) {
                                                        NetworkResponse networkResponse=error.networkResponse;
                                                        if (networkResponse.statusCode == 401) {
                                                            //????????? access token ?????? ?????????
                                                            Toast.makeText( getApplicationContext(), "????????? ??????????????? ????????? ?????????????????????. ?????? ????????? ????????????.", Toast.LENGTH_SHORT ).show();

                                                            //login ???????????? ????????????
                                                            Intent intent = new Intent( MyPageActivity.this, LoginActivity.class );
                                                            startActivity( intent );
                                                            finish();
                                                        }
                                                        else{
                                                            //?????? ??????????????? ???
                                                            Log.d("statuscode", "" + networkResponse.statusCode);
                                                            Toast.makeText( getApplicationContext(), "????????? ????????? ?????????????????????.", Toast.LENGTH_SHORT ).show();
                                                        }
                                                    }
                                                }) {
                                                    @Override
                                                    public Map<String, String> getHeaders() throws AuthFailureError {
                                                        return headers;
                                                    }
                                                };
                                                RequestQueue requestQueue = Volley.newRequestQueue(MyPageActivity.this);
                                                requestQueue.add(SecessionRequest);

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            NetworkResponse networkResponse=error.networkResponse;
                                            //access token ????????? ???????????? ??????????????? ?????????
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
                                    RequestQueue requestQueue = Volley.newRequestQueue(MyPageActivity.this);
                                    requestQueue.add(TokenReissueRequest);

                                }
                                else{
                                    //?????? ??????????????? ???
                                    Log.d("statuscode", "" + networkResponse.statusCode);
                                    Toast.makeText( getApplicationContext(), "????????? ????????? ?????????????????????.", Toast.LENGTH_SHORT ).show();

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
                        RequestQueue requestQueue = Volley.newRequestQueue(MyPageActivity.this);
                        requestQueue.add(SecessionRequest);
                    }
                });

                builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
        });

    }
}