package com.example.eman.gittask;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.eman.gittask.Fonts.TypefaceUtil;
import com.example.eman.gittask.RecyclerView.Adapter;
import com.example.eman.gittask.RecyclerView.Model;
import com.example.eman.gittask.RecyclerView.RecyclerViewPositionHelper;
import com.example.eman.gittask.RetrofitService.InterfaceService;
import com.example.eman.gittask.RetrofitService.ResultModle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.eman.gittask.RetrofitService.caching.REWRITE_CACHE_CONTROL_INTERCEPTOR;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.recycler)
    RecyclerView recyclerView;
    @Bind(R.id.activity_main)
    SwipeRefreshLayout activity_main;
    int width;
    public static boolean work;
    public static String PagesNum;
    ProgressDialog progressDialog;
    private List<Model> disList = new ArrayList<>();
    private Adapter mAdapter;
int DataLength=0;
    int num = 2;

    private int findLastVisibleItemPosition ;
    int firstVisibleItem, visibleItemCount, totalItemCount;
    Boolean flag=true;

    RecyclerViewPositionHelper mRecyclerViewHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        TypefaceUtil.overrideFonts(this, activity_main);
        work = isNetworkConnected();
        ////////////////////////////////////////////////////////////////////////////////////////////
        final Display display = ((WindowManager) getSystemService(getApplication().WINDOW_SERVICE)).getDefaultDisplay();
        width = display.getWidth();

        ////////////////////////////////////////////////////////////////////////////////////////////
        mAdapter = new Adapter(disList);
        final RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplication());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        PagesNum = "1";
        LoadData(1);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mRecyclerViewHelper = RecyclerViewPositionHelper.createHelper(recyclerView);
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = mRecyclerViewHelper.getItemCount();
                firstVisibleItem = mRecyclerViewHelper.findFirstVisibleItemPosition();
                findLastVisibleItemPosition=mRecyclerViewHelper.findLastCompletelyVisibleItemPosition();
                //System.out.println(findLastVisibleItemPosition+"jhjgjhg");
                if (findLastVisibleItemPosition
                        == DataLength-1&&flag) {
                    // End has been reached
                    // Do something
                    LoadData(2);
                    PagesNum = num + "";
                    num ++;


                }



            }


        });

//////////////////////////////////////////////////////////////////////////////////////////////////


            activity_main.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (isNetworkConnected()) {
                        disList.clear(); //clear list
                        mAdapter.notifyDataSetChanged();
                        num=2;
                        PagesNum = "1";
                        DataLength=0;
                        LoadData(2);

                        Toast.makeText(MainActivity.this, "Refresh", Toast.LENGTH_SHORT).show();
                    } else {
                        activity_main.setRefreshing(false);
                        Toast.makeText(MainActivity.this, "No Network Connected", Toast.LENGTH_SHORT).show();
                    }
                }
            });



    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public void LoadData(final int request) {
        //caching
        File httpCacheDirectory = new File(this.getCacheDir(), "responses");
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(httpCacheDirectory, cacheSize);

        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
                .cache(cache).build();
        ////////////////////////////////////////////////////////////////

        String URL = "https://api.github.com/users/square/";
        Retrofit retrofit = new Retrofit.Builder().baseUrl(URL).client(client).
                addConverterFactory(GsonConverterFactory.create()).build();
        InterfaceService AllData = retrofit.create(InterfaceService.class);//conected api
        System.out.println(PagesNum+"jhjgjhg");
        Call<List<ResultModle>> connection = AllData.getData(("repos?page=" + PagesNum + "&&per_page=10&?access_token=563052695d89c1208f90ff817e2896b723f0e79c"));
        if (request == 1) {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Please Waite...");
            progressDialog.show();
        }

        connection.enqueue(new Callback<List<ResultModle>>() {
            @Override
            public void onResponse(Call<List<ResultModle>> call, Response<List<ResultModle>> response) {
                if (request == 1) {
                    progressDialog.dismiss();
                } else {
                    activity_main.setRefreshing(false);
                }

                if (response != null) {
                    DataLength+=response.body().size();
                    try {
                        for (int i = 0; i < response.body().size(); i++) {

                            Model disUserControl = new Model(response.body().get(i).getName(), response.body().get(i).getDescription()
                                    , response.body().get(i).getOwner().getLogin(), response.body().get(i).getFork()
                                    , response.body().get(i).getHtml_url(), response.body().get(i).getOwner().getHtml_url(), width);
                            disList.add(disUserControl);
                            mAdapter.notifyDataSetChanged();
                        }
                    } catch (Exception ex) {
                        Toast.makeText(MainActivity.this, "API rate limit exceeded", Toast.LENGTH_LONG).show();

                    }
                }else {
                    flag=false;
                }
            }

            @Override
            public void onFailure(Call<List<ResultModle>> call, Throwable t) {


            }
        });


    }

}
