package ads.check.rate.exchange_rate_check;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

import ads.check.rate.exchange_rate_check.api.CryptonatorApi;
import ads.check.rate.exchange_rate_check.api.CryptonatorResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    CryptonatorResponse cryptonatorResponse;

    AutoCompleteTextView baseAutoCompleteTV;
    AutoCompleteTextView targetAutoCompleteTV;
    private static Snackbar snackbar;

    ArrayList<Currency> currencyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button goButton = (Button) findViewById(R.id.go_button);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findRates();
            }
        });

        readCurrencies();
        setBaseAutocompleteData();
    }

    private void findRates() {

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CryptonatorApi.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        CryptonatorApi cryptonatorApi = retrofit.create(CryptonatorApi.class);

        Call<CryptonatorResponse> call = cryptonatorApi.tick("btc-usd");
        call.enqueue(new Callback<CryptonatorResponse>() {

            @Override
            public void onResponse(Call<CryptonatorResponse> call, Response<CryptonatorResponse> response) {
                cryptonatorResponse = response.body();

                updateResultsView();
            }

            @Override
            public void onFailure(Call<CryptonatorResponse> call, Throwable t) {
                showSnackbar("Consider checking your internet connection", findViewById(R.id.toolbar), Snackbar.LENGTH_INDEFINITE);
            }
        });

    }

    private void updateResultsView() {

        if (cryptonatorResponse != null) {


        }
    }

    private void showSnackbar(String message, View view, int length) {

        snackbar = Snackbar.make(view, message, length);

        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });

        snackbar.show();
    }

    private void readCurrencies() {

        try {
            AssetManager assets = this.getAssets();
            InputStream inputStream = assets.open("currencies.json");

            int size = inputStream.available();

            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            String json = new String(buffer, "UTF-8");

            JSONObject jsonObject = new JSONObject(json);
            JSONArray currencies = jsonObject.getJSONArray("rows");

            for (int i = 0; i < currencies.length(); i++) {
                JSONObject object = currencies.getJSONObject(i);

                Currency currency = new Currency();
                currency.setCode(object.get("code").toString());
                currency.setName(object.get("name").toString());

                currencyList.add(currency);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setBaseAutocompleteData() {
        baseAutoCompleteTV = (AutoCompleteTextView)
                findViewById(R.id.base_autoCompleteTextView);

        ArrayAdapter<Currency> badapter = new ArrayAdapter<> (this,android.R.layout.select_dialog_item, currencyList);

        baseAutoCompleteTV.setThreshold(1);
        baseAutoCompleteTV.setAdapter(badapter);


        targetAutoCompleteTV = (AutoCompleteTextView)
                findViewById(R.id.target_autoCompleteTextView);

        ArrayAdapter<Currency> tadapter = new ArrayAdapter<> (this,android.R.layout.select_dialog_item, currencyList);

        targetAutoCompleteTV.setThreshold(1);
        targetAutoCompleteTV.setAdapter(tadapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
