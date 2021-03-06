package org.techtown.recipe.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.recipe.MyApplication;
import org.techtown.recipe.R;
import org.techtown.recipe.grade.GradeActivity;
import org.techtown.recipe.grade.GradeAdapter;
import org.techtown.recipe.grade.GradeItem;
import org.techtown.recipe.grade.GradeModifyActivity;
import org.techtown.recipe.grade.OnGradeItemClickListener;
import org.techtown.recipe.login.AutoLoginActivity;
import org.techtown.recipe.login.LoginActivity;
import org.techtown.recipe.login.RegisterActivity;
import org.techtown.recipe.main.MainActivity;
import org.techtown.recipe.main.MainAdapter;
import org.techtown.recipe.main.MainItem;
import org.techtown.recipe.mypage.MyPageActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecipeActivity extends AppCompatActivity {
    private SharedPreferences preferences;

    private RecyclerView orderRecyclerView;
    private RecyclerView gradeRecyclerView;

    private OrderAdapter orderAdapter;
    private GradeAdapter gradeAdapter;

    private ImageView RecipeImageView;
    private TextView recipeTitleTextView;
    private TextView recipePeopleTextView;
    private TextView recipeMinuteTextView;
    private TextView recipeDifficultyTextView;
    private TextView recipeSourceTextView;

    private RatingBar ratingBar;

    private ImageButton exit_button;
    private ImageButton favorite_button;

    private Button recipe_button;

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
        setContentView(R.layout.activity_recipe);

        RecipeImageView = findViewById(R.id.RecipeImageView);
        recipeTitleTextView = findViewById(R.id.recipeTitleTextView);
        recipePeopleTextView = findViewById(R.id.recipePeopleTextView);
        recipeMinuteTextView = findViewById(R.id.recipeMinuteTextView);
        recipeDifficultyTextView = findViewById(R.id.recipeDifficultyTextView);
        recipeSourceTextView = findViewById(R.id.recipeSourceTextView);

        ratingBar = findViewById(R.id.middleRatingBar);

        exit_button = findViewById(R.id.exit_button);
        favorite_button = findViewById(R.id.favorite_button);
        recipe_button=findViewById(R.id.recipe_button);

        //Recycler view ??????
        orderRecyclerView = findViewById(R.id.orderRecyclerView);
        LinearLayoutManager orderLayoutManager = new LinearLayoutManager(getApplicationContext());
        orderRecyclerView.setLayoutManager(orderLayoutManager);
        orderAdapter = new OrderAdapter();

        gradeRecyclerView = findViewById(R.id.gradeRecyclerView);
        LinearLayoutManager gradeLayoutManager = new LinearLayoutManager(getApplicationContext());
        gradeRecyclerView.setLayoutManager(gradeLayoutManager);
        gradeAdapter = new GradeAdapter();

        Intent intent = getIntent();
        String modify_RId = intent.getStringExtra("modify_RId");
        String recipe_url=intent.getStringExtra("recipe_url");

        //url ????????????
        MyApplication myApp = (MyApplication) getApplication();
        String url=myApp.getGlobalString();
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

                                headers.put("accessToken",newAccessToken);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            NetworkResponse networkResponse=error.networkResponse;
                            if (networkResponse.statusCode == 411||networkResponse.statusCode == 412) {
                                //1. refresh token, access token??? ???????????? ???????????? ?????? ?????? 411
                                //2. refresh token??? ????????? ?????? ????????? ?????? ?????? 412
                                Log.d("statuscode2", "" + networkResponse.statusCode);
                                Toast.makeText( getApplicationContext(), "????????? ??????????????? ????????? ?????????????????????. ?????? ????????? ????????????.", Toast.LENGTH_SHORT ).show();

                            }
                            else{
                                //?????? ??????????????? ???
                                Log.d("statuscode2", "" + networkResponse.statusCode);
                                Toast.makeText( getApplicationContext(), "????????? ????????? ?????????????????????.", Toast.LENGTH_SHORT ).show();
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
                    RequestQueue requestQueue = Volley.newRequestQueue(RecipeActivity.this);
                    requestQueue.add(TokenReissueRequest);
                }
                else if (networkResponse.statusCode == 401||networkResponse.statusCode == 402) {
                    //2. access token??? ???????????? ???????????? ?????? ?????? 401
                    //3. ???????????? ?????? ?????? ????????? ?????? 402
                    Log.d("statuscode1", "" + networkResponse.statusCode);
                    Toast.makeText( getApplicationContext(), "????????? ??????????????? ????????? ?????????????????????. ?????? ????????? ????????????.", Toast.LENGTH_SHORT ).show();

                    Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent2);
                    finish();
                }
                else{
                    //?????? ??????????????? ???
                    Log.d("statuscode1", "" + networkResponse.statusCode);
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
        RequestQueue requestQueue = Volley.newRequestQueue(RecipeActivity.this);
        requestQueue.add(TokenValidateRequest);

        //recipe ????????????
        //url ????????????
        String RecipeShowurl = myApp.getGlobalString();
        RecipeShowurl += "/recipe/?rId=" + modify_RId;

        StringRequest RecipeShowRequest = new StringRequest(Request.Method.GET, RecipeShowurl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray recipesArray = jsonObject.optJSONArray("recipes");
                    JSONObject element;

                    for (int i = 0; i < recipesArray.length(); i++) {
                        element = (JSONObject) recipesArray.opt(i);

                        recipeTitleTextView.setText(element.optString("recipe_title"));
                        recipePeopleTextView.setText(element.optString("serving"));
                        recipeMinuteTextView.setText(element.optString("cookingTime"));
                        recipeDifficultyTextView.setText(element.optString("difficult"));
                        recipeSourceTextView.setText(element.optString("recipe_source"));
                        String imgPath = element.optString("menu_img");

                        if (imgPath != null && !"".equals(imgPath)) {
                            Glide.with(RecipeImageView).load(imgPath).into(RecipeImageView);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                //?????? ??????????????? ???
                Log.d("statuscode", "" + networkResponse.statusCode);
                Toast.makeText(getApplicationContext(), "????????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };
        RequestQueue requestQueue1 = Volley.newRequestQueue(RecipeActivity.this);
        requestQueue1.add(RecipeShowRequest);

        //????????? ?????? ????????????
        //url ????????????
        String RecipeOrderUrl = myApp.getGlobalString();
        RecipeOrderUrl += "/recipe/order/?rId=" + modify_RId;

        StringRequest RecipeOrderRequest = new StringRequest(Request.Method.GET, RecipeOrderUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray orderArray = jsonObject.optJSONArray("r_order");
                    JSONObject element;

                    ArrayList<OrderItem> items = new ArrayList<OrderItem>();

                    for (int i = 0; i < orderArray.length(); i++) {
                        element = (JSONObject) orderArray.opt(i);
                        items.add(new OrderItem(element.optString("rId")
                                , element.optString("recipe_order")
                                , element.optString("description")));
                    }
                    orderAdapter.setItems(items);
                    orderAdapter.notifyDataSetChanged();

                } catch (JSONException e) {

                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                //?????? ??????????????? ???
                if (networkResponse.statusCode == 421) {
                    Log.d("statuscode", "" + networkResponse.statusCode);
                    Toast.makeText(getApplicationContext(), "???????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("statuscode", "" + networkResponse.statusCode);
                    Toast.makeText(getApplicationContext(), "????????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();

                }
            }
        });
        RequestQueue requestQueue2 = Volley.newRequestQueue(RecipeActivity.this);
        requestQueue2.add(RecipeOrderRequest);

        orderRecyclerView.setAdapter(orderAdapter);

        //????????? ?????? ????????????
        //url ????????????
        String GradeListUrl = myApp.getGlobalString();
        GradeListUrl += "/recipe/usergrade/?rId=" + modify_RId;

        StringRequest GradeListRequest = new StringRequest(Request.Method.GET, GradeListUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray gradeArray = jsonObject.optJSONArray("grade_list");
                    JSONObject element;

                    ArrayList<GradeItem> items = new ArrayList<GradeItem>();

                    for (int i = 0; i < gradeArray.length(); i++) {
                        element = (JSONObject) gradeArray.opt(i);
                        if(element.optString("comment").isEmpty()){
                            items.add(new GradeItem(element.optString("id")
                                    , element.optString("userId")
                                    , element.optString("rId")
                                    , element.optString("grade")
                                    , ""
                                    ,0));
                            Log.d("grade", element.optString("userId"));
                        }else{
                            items.add(new GradeItem(element.optString("id")
                                    , element.optString("userId")
                                    , element.optString("rId")
                                    , element.optString("grade")
                                    , element.optString("comment")
                                    ,0));
                            Log.d("grade", element.optString("userId"));
                        }
                    }
                    gradeAdapter.setItems(items);
                    gradeAdapter.notifyDataSetChanged();

                } catch (JSONException e) {

                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                //?????? ??????????????? ???
                if (networkResponse.statusCode == 421) {
                    Log.d("statuscode", "" + networkResponse.statusCode);
                } else {
                    Log.d("statuscode", "" + networkResponse.statusCode);
                    Toast.makeText(getApplicationContext(), "????????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();

                }
            }
        });
        RequestQueue requestQueue3 = Volley.newRequestQueue(RecipeActivity.this);
        requestQueue3.add(GradeListRequest);

        gradeRecyclerView.setAdapter(gradeAdapter);

        //?????? ??????
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

                Intent intent = new Intent( RecipeActivity.this, GradeActivity.class );

                float rating_number=ratingBar.getRating();
                intent.putExtra("modify_RId",modify_RId);
                intent.putExtra("rating_number",rating_number);
                Log.d("rating",""+rating_number);

                startActivity( intent );

                finish();
            }
        });

        //?????? ???????????? ??? ???????????? ??????
        gradeAdapter.setOnItemClickListener(new OnGradeItemClickListener() {
            @Override
            public void onItemClick(GradeAdapter.ViewHolder holder, View view, int position) {
                GradeItem item=gradeAdapter.getItem(position);

                String modify_Id=item.getId();
                String modify_Comment=item.getComment();
                String rating=item.getGrade();
                float rating_number=Float.parseFloat(rating);

                //url ????????????
                String IdUrl = myApp.getGlobalString();
                IdUrl += "/users/access/id";

                StringRequest IdRequest = new StringRequest(Request.Method.GET, IdUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String UserId = jsonObject.optString("userId");

                            if(UserId.equals(item.getUserId())){
                                Log.d("same","same");
                                Intent intent = new Intent( RecipeActivity.this, GradeModifyActivity.class );

                                intent.putExtra("modify_Id",modify_Id);
                                intent.putExtra("modify_Comment",modify_Comment);
                                intent.putExtra("rating_number",rating_number);

                                startActivity( intent );
                                finish();
                            }
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
                            Intent intent = new Intent( RecipeActivity.this, LoginActivity.class );
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
                RequestQueue requestQueue2 = Volley.newRequestQueue(RecipeActivity.this);
                requestQueue2.add(IdRequest);

            }
        });
        //?????? ??????
        exit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        recipe_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( RecipeActivity.this, RecipeWebViewActivity.class );

                intent.putExtra("recipe_url",recipe_url);

                startActivity( intent );
            }
        });


        //?????????
        favorite_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //url ????????????
                MyApplication myApp = (MyApplication) getApplication();
                String url = myApp.getGlobalString();
                url += "/users/access";

                StringRequest TokenValidateRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //url ????????????
                        String WishListUrl = myApp.getGlobalString();
                        WishListUrl += "/recipe/wishlist/?rId=" + modify_RId;

                        StringRequest RecipeOrderRequest = new StringRequest(Request.Method.POST, WishListUrl, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(getApplicationContext(), "??? ????????? ???????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                NetworkResponse networkResponse = error.networkResponse;
                                //?????? ??????????????? ???
                                if (networkResponse.statusCode == 411) {
                                    Log.d("statuscode", "" + networkResponse.statusCode);
                                    Toast.makeText(getApplicationContext(), "???????????? ????????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                                } else if (networkResponse.statusCode == 401) {
                                    Log.d("statuscode", "" + networkResponse.statusCode);
                                    Toast.makeText(getApplicationContext(), "?????? ??? ???????????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show();
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
                        RequestQueue requestQueue2 = Volley.newRequestQueue(RecipeActivity.this);
                        requestQueue2.add(RecipeOrderRequest);
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

                                        //url ????????????
                                        String WishListUrl = myApp.getGlobalString();
                                        WishListUrl += "/recipe/wishlist/?rId=" + modify_RId;

                                        StringRequest RecipeOrderRequest = new StringRequest(Request.Method.POST, WishListUrl, new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                Toast.makeText(getApplicationContext(), "??? ????????? ???????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                NetworkResponse networkResponse = error.networkResponse;
                                                //?????? ??????????????? ???
                                                if (networkResponse.statusCode == 411) {
                                                    Log.d("statuscode", "" + networkResponse.statusCode);
                                                    Toast.makeText(getApplicationContext(), "???????????? ????????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                                                } else if (networkResponse.statusCode == 401) {
                                                    Log.d("statuscode", "" + networkResponse.statusCode);
                                                    Toast.makeText(getApplicationContext(), "?????? ??? ???????????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show();
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
                                        RequestQueue requestQueue2 = Volley.newRequestQueue(RecipeActivity.this);
                                        requestQueue2.add(RecipeOrderRequest);

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
                            RequestQueue requestQueue = Volley.newRequestQueue(RecipeActivity.this);
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
                RequestQueue requestQueue = Volley.newRequestQueue(RecipeActivity.this);
                requestQueue.add(TokenValidateRequest);

            }
        });
    }
}