package com.pens.covidapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.pens.covidapp.BuildConfig;
import com.pens.covidapp.R;
import com.pens.covidapp.adapter.CountryAdapter;
import com.pens.covidapp.api.ApiClient;
import com.pens.covidapp.api.ApiService;
import com.pens.covidapp.model.indonesia.Indonesia;
import com.pens.covidapp.model.summary.Country;
import com.pens.covidapp.model.summary.Global;
import com.pens.covidapp.model.summary.Summary;
import com.pens.covidapp.utils.Constant;
import com.pens.covidapp.utils.Utils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    final String TAG = "MainActivity";

    RelativeLayout rlMain;
    SwipeRefreshLayout swipeLayout;

    TextView txtUpdate;

    //global
    TextView txtTotalKasus;
    TextView txtPositif, txtSembuh, txtMati;

    //country
    CountryAdapter countryAdapter;
    RecyclerView rvCountry;
    TextView tvTotalCountry;

    //indonesia
    TextView txtIdnKasus, txtIdnPositif, txtIdnSembuh, txtIdnMati, txtPersentaseMati, txtPersentaseSembuh, txtPersentaseKasus;

    //button
    TextView txtRiwayat;
    RelativeLayout rlProvinsi;

    //    global
    private String sPositif = "", sSembuh = "", sKasus = "", sMati = "", sUpdate = "",
            sNewPositif = "", sNewMati = "", sCountryTotal = "";
    private int gKasus = 0, gSembuh = 0, gMati = 0, gPositif = 0;
    //    indonesia
    private String sInaPositif = "", sInaSembuh = "", sInaKasus = "", sInaMati = "",
            sInaNewPositif = "", sInaNewMati = "", sInaPopulasi = "", sInaKritis = "",
            sInaPerMati = "", sInaPerKasus = "", sInaPerSembuh = "";
    private int inaPositif = 0, inaSembuh = 0, inaMati = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rlMain = findViewById(R.id.page_main);
        swipeLayout = findViewById(R.id.main_layout_reload);

        txtUpdate = findViewById(R.id.main_update);

        txtTotalKasus = findViewById(R.id.main_kasus);
        txtPositif = findViewById(R.id.main_positif);
        txtSembuh = findViewById(R.id.main_sembuh);
        txtMati = findViewById(R.id.main_mati);

        //indonesia
        txtIdnKasus = findViewById(R.id.main_ina_kasus);
        txtIdnPositif = findViewById(R.id.main_ina_positif);
        txtIdnSembuh = findViewById(R.id.main_ina_sembuh);
        txtIdnMati = findViewById(R.id.main_ina_mati);
        txtPersentaseKasus = findViewById(R.id.main_ina_persentase_kasus);
        txtPersentaseSembuh = findViewById(R.id.main_ina_persentase_sembuh);
        txtPersentaseMati = findViewById(R.id.main_ina_persentase_mati);

        //country
        rvCountry = findViewById(R.id.rv_country);
        tvTotalCountry = findViewById(R.id.tv_total_country);

        //button
        rlProvinsi = findViewById(R.id.main_ina_btn_provinsi);
        txtRiwayat = findViewById(R.id.main_ina_btn_detail);

        rlProvinsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToProvince();
            }
        });


        if (swipeLayout.isRefreshing()) swipeLayout.setRefreshing(false);
        swipeLayout.setOnRefreshListener(this::getData);
        swipeLayout.setColorScheme(R.color.colorPrimary);

        getData();
    }

    private void goToProvince() {

        Intent intent = new Intent(getApplicationContext(), ProvinceActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constant.send_kasus, sInaKasus);
        intent.putExtra(Constant.send_positif, sInaPositif);
        intent.putExtra(Constant.send_sembuh, sInaSembuh);
        intent.putExtra(Constant.send_mati, sInaMati);
        Log.d("ProvinceActivity", "inamati : " + sInaMati);
        startActivity(intent);
    }

    public void getData() {
        load();
        if (!Utils.internet(getApplicationContext())) {
            unload(getString(R.string.txt_koneksi_masalah));
            return;
        }
        getSummary();
    }

    private void getSummary() {
        if (!Utils.internet(getApplicationContext())) {
            unload(getString(R.string.txt_koneksi_masalah));
            return;
        }
        ApiService apiService = ApiClient.getClient(BuildConfig.BASE_URL_SUMMARY).create(ApiService.class);
        apiService.getSummary().enqueue(new Callback<Summary>() {
            @Override
            public void onResponse(Call<Summary> call, Response<Summary> response) {
                Log.d(TAG, "onResponse Summary " + response.toString());
                try {
                    if (response.isSuccessful()) {
//                        Log.d(TAG,"Summary "+response.body().toString());

                        Summary summary = response.body();
                        Global global = summary.getGlobal();

                        gKasus = global.getTotalConfirmed();
                        gSembuh = global.getTotalRecovered();
                        gMati = global.getTotalDeaths();

                        gPositif = gKasus - (gSembuh + gMati);

                        txtTotalKasus.setText(Utils.number("" + gKasus));
                        txtPositif.setText(Utils.number("" + gPositif));
                        txtMati.setText(Utils.number("" + gMati));
                        txtSembuh.setText(Utils.number("" + gSembuh));
                        String dateUpdate = summary.getDate();
                        Log.d(TAG, "date : " + dateUpdate);

                        txtUpdate.setText("Diperbarui pada " + Utils.getTimezone(dateUpdate));

                        updateCountry(summary.getCountries());
                        getIndonesia();

                    }
                } catch (Exception e) {
                    unload(getString(R.string.txt_koneksi_masalah));
                }
            }

            @Override
            public void onFailure(Call<Summary> call, Throwable t) {
                unload(getString(R.string.txt_koneksi_masalah));

            }
        });
    }

    private void updateCountry(List<Country> countries) {
        tvTotalCountry.setText("("+countries.size()+")");
        countryAdapter = new CountryAdapter(MainActivity.this, countries,
                gKasus);
        rvCountry.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
        rvCountry.setAdapter(countryAdapter);
        rvCountry.setItemAnimator(new DefaultItemAnimator());
        rvCountry.setHasFixedSize(true);
        countryAdapter.notifyDataSetChanged();
    }



    private void getIndonesia() {
        Log.d(TAG, "getIndonesia");

        if (!Utils.internet(getApplicationContext())) {
            unload(getString(R.string.txt_koneksi_masalah));
            return;
        }

        ApiService apiService = ApiClient.getClient(BuildConfig.BASE_URL_02).create(ApiService.class);

        apiService.getIndonesia().enqueue(new Callback<List<Indonesia>>() {

            @Override
            public void onResponse(Call<List<Indonesia>> call, Response<List<Indonesia>> response) {
                try {
                    if (response.isSuccessful()) {
                        Log.d(TAG, response.body().toString());

                        Indonesia idn = response.body().get(0);

                        sInaKasus = Utils.removeCharacter(idn.getPositif());
                        sInaPositif = Utils.removeCharacter(idn.getDirawat());
                        sInaSembuh = Utils.removeCharacter(idn.getSembuh());
                        sInaMati = Utils.removeCharacter(idn.getMeninggal());


                        txtIdnKasus.setText(Utils.number(sInaKasus));
                        txtIdnPositif.setText(Utils.number(sInaPositif));
                        txtIdnSembuh.setText(Utils.number(sInaSembuh));
                        txtIdnMati.setText(Utils.number(sInaMati));
                        Log.d(TAG, "kasus : " + gKasus + " sembuh : " + gSembuh + " gMati : " + gMati);


                        sInaPerKasus = "(" + Utils.persen(gKasus, Integer.parseInt(sInaKasus), "2") + "% dari Global)";
                        sInaPerMati = "(" + Utils.persen(gMati, Integer.parseInt(sInaMati), "2") + "%)";
                        sInaPerSembuh = "(" + Utils.persen(gSembuh, Integer.parseInt(sInaSembuh), "2") + "%)";
                        Log.d(TAG, "ina kasus : " + sInaPerKasus + " sembuh : " + sInaPerSembuh + " gMati : " + sInaPerMati);

                        txtPersentaseKasus.setText(sInaPerKasus);
                        txtPersentaseMati.setText(sInaPerMati);
                        txtPersentaseSembuh.setText(sInaPerSembuh);

                        unload("");

                    }
                } catch (Exception e) {
                    Log.d(TAG, " getIndonesia koneksi");
                    unload(getString(R.string.txt_koneksi_masalah));
                }

            }

            @Override
            public void onFailure(Call<List<Indonesia>> call, Throwable t) {
                Log.d(TAG, "error " + t.toString());
                unload(getString(R.string.txt_koneksi_masalah));

            }
        });
    }


    private void load() {
        swipeLayout.setRefreshing(true);
    }

    private void unload(String msg) {
        swipeLayout.setRefreshing(false);

        if (!msg.isEmpty()) snackbar(msg);
    }

    private void snackbar(String msg) {
        Utils.snackbar(findViewById(R.id.page_main), msg);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private boolean keluar = false;

    @Override
    public void onBackPressed() {
        if (keluar) {
            this.finishAffinity();
        }

        this.keluar = true;
        Toast.makeText(this, "Tekan lagi untuk menutup", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> keluar = false, 2000);
    }
}
