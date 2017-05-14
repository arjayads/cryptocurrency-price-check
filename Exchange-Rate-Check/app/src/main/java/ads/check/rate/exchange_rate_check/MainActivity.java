package ads.check.rate.exchange_rate_check;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ads.check.rate.exchange_rate_check.api.CryptonatorApi;
import ads.check.rate.exchange_rate_check.api.CryptonatorResponse;
import ads.check.rate.exchange_rate_check.api.Ticker;
import ads.check.rate.exchange_rate_check.sqlite.ConversionResultContract;
import ads.check.rate.exchange_rate_check.sqlite.CxrDbHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    CryptonatorResponse cryptonatorResponse;

    ProgressBar progressBar;
    TableRow volumeRow;
    TableRow volumeValueRow;
    ScrollView mainScrollView;
    Button reverseButton;

    AutoCompleteTextView baseAutoCompleteTV;
    AutoCompleteTextView targetAutoCompleteTV;
    private static Snackbar snackbar;

    ArrayList<Currency> currencyList = new ArrayList<>();

    Currency baseCurrency;
    Currency targetCurrency;

    CxrDbHelper cxrDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        getSupportActionBar().setTitle(R.string.toolbar_title);

        mainScrollView = (ScrollView) findViewById(R.id.main_ScrollView);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        Button goButton = (Button) findViewById(R.id.go_button);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findRates();
            }
        });

        reverseButton = (Button) findViewById(R.id.reverse_button);
        reverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!hasEnteredCurrency()) {
                    showSnackbar("Select base and target currency", findViewById(R.id.toolbar), Snackbar.LENGTH_INDEFINITE);
                    return;
                }

                Currency temp = baseCurrency;
                baseCurrency = targetCurrency;
                targetCurrency = temp;

                targetAutoCompleteTV.setText(targetCurrency.toString());
                baseAutoCompleteTV.setText(baseCurrency.toString());

                baseAutoCompleteTV.dismissDropDown();
                targetAutoCompleteTV.dismissDropDown();

                baseAutoCompleteTV.requestFocus();

                mainScrollView.smoothScrollTo(0,0);

                findRates();
            }
        });

        readCurrencies();
        setBaseAutocompleteData();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (currencyList.isEmpty()) {
            readCurrencies();
            setBaseAutocompleteData();
            toogleResultViews(View.INVISIBLE);
        }
    }

    private void findRates() {

        cryptonatorResponse = null; // reset result

        hideKeyBoard();
        toogleResultViews(View.INVISIBLE);

        if (snackbar != null) {
            snackbar.dismiss();
        }

        if (!hasEnteredCurrency()) {
            showSnackbar("Select base and target currency", findViewById(R.id.toolbar), Snackbar.LENGTH_INDEFINITE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CryptonatorApi.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        CryptonatorApi cryptonatorApi = retrofit.create(CryptonatorApi.class);

        String param = baseCurrency.getCode() + "-" + targetCurrency.getCode();
        Call<CryptonatorResponse> call = cryptonatorApi.tick(param.toLowerCase());

        call.enqueue(new Callback<CryptonatorResponse>() {

            @Override
            public void onResponse(Call<CryptonatorResponse> call, Response<CryptonatorResponse> response) {
                cryptonatorResponse = response.body();

                progressBar.setVisibility(View.GONE);
                updateResultsView();
            }

            @Override
            public void onFailure(Call<CryptonatorResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showSnackbar("Consider checking your internet connection", findViewById(R.id.toolbar), Snackbar.LENGTH_INDEFINITE);
            }
        });

    }

    private boolean hasEnteredCurrency() {


        String baseStr = baseAutoCompleteTV.getText().toString();
        String targetStr = targetAutoCompleteTV.getText().toString();

        if (targetCurrency == null && baseCurrency == null) {

            if (baseStr != null && !baseStr.equals("")) {
                baseCurrency = new Currency();
                baseCurrency.setCode(baseStr);
                baseCurrency.setName(baseStr);
            }

            if (targetStr != null && !targetStr.equals("")) {
                targetCurrency = new Currency();
                targetCurrency.setCode(targetStr);
                targetCurrency.setName(targetStr);
            }

            return (targetCurrency != null && baseCurrency != null) ;

        } else {

            if(baseStr.equals("")) return false;
            if(targetStr.equals("")) return false;

            return true;
        }

    }

    private void hideKeyBoard() {

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void updateResultsView() {

        TextView baseLabel = (TextView) findViewById(R.id.base_textView);
        TextView targetLabel = (TextView) findViewById(R.id.target_textView);

        TextView priceTextView = (TextView) findViewById(R.id.price_value_textView);
        TextView changeTextView = (TextView) findViewById(R.id.change_value_textView);
        TextView volumeTextView = (TextView) findViewById(R.id.volume_value_textView);
        TextView timeTextView = (TextView) findViewById(R.id.time_value_textView);

        if (cryptonatorResponse != null && cryptonatorResponse.isSuccess()) {

            Ticker ticker = cryptonatorResponse.getTicker();

            baseLabel.setText("Base: " + ticker.getBase());
            targetLabel.setText("Target: " + ticker.getTarget());

            priceTextView.setText(ticker.getPrice());
            changeTextView.setText(ticker.getChange());

            volumeRow = (TableRow) findViewById(R.id.volume_Row);
            volumeValueRow = (TableRow) findViewById(R.id.volume_value_Row);

            if (ticker.getVolume().equals("")) {

                volumeRow.setVisibility(View.GONE);
                volumeValueRow.setVisibility(View.GONE);

            } else {

                volumeRow.setVisibility(View.VISIBLE);
                volumeValueRow.setVisibility(View.VISIBLE);

                volumeTextView.setText(ticker.getVolume());
            }


            Date date = new Date(Long.parseLong(cryptonatorResponse.getTimestamp()) * 1000);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd yyyy hh:mm:ss");

            timeTextView.setText(simpleDateFormat.format(date));

            toogleResultViews(View.VISIBLE);

        } else {
            showSnackbar(cryptonatorResponse.getError(), findViewById(R.id.toolbar), Snackbar.LENGTH_INDEFINITE);
        }
    }

    private void toogleResultViews(int visibility) {

        TableLayout result = (TableLayout)findViewById(R.id.result_tableLayout);
        result.setVisibility(visibility);

        LinearLayout header = (LinearLayout)findViewById(R.id.base_target_linearLayout);
        header.setVisibility(visibility);

        reverseButton.setVisibility(visibility);

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
        baseAutoCompleteTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                baseCurrency = null;

                Object item = parent.getItemAtPosition(position);
                if (item instanceof Currency){

                    baseCurrency =  (Currency) item;
                }

            }
        });


        targetAutoCompleteTV = (AutoCompleteTextView)
                findViewById(R.id.target_autoCompleteTextView);

        ArrayAdapter<Currency> tadapter = new ArrayAdapter<> (this,android.R.layout.select_dialog_item, currencyList);

        targetAutoCompleteTV.setAdapter(tadapter);
        targetAutoCompleteTV.setThreshold(1);
        targetAutoCompleteTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                targetCurrency = null;

                Object item = parent.getItemAtPosition(position);
                if (item instanceof Currency){
                    targetCurrency = (Currency) item;
                }
            }
        });

        targetAutoCompleteTV.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_GO) {
                    findRates();
                }
                return false;
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        int orientation = newConfig.orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)  {

            baseAutoCompleteTV.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            targetAutoCompleteTV.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        } else if (orientation == Configuration.ORIENTATION_PORTRAIT)  {

            baseAutoCompleteTV.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            targetAutoCompleteTV.setImeOptions(EditorInfo.IME_ACTION_GO);
        }
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

        cxrDbHelper = new CxrDbHelper(getBaseContext());

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_view) {

            SQLiteDatabase db = cxrDbHelper.getReadableDatabase();

            String [] columns = {
                    ConversionResultContract.ConversionEntry._ID,
                    ConversionResultContract.ConversionEntry.COLUMN_NAME_BASE,
                    ConversionResultContract.ConversionEntry.COLUMN_NAME_TARGET,
                    ConversionResultContract.ConversionEntry.COLUMN_NAME_PRICE,
                    ConversionResultContract.ConversionEntry.COLUMN_NAME_BASE_DESC,
                    ConversionResultContract.ConversionEntry.COLUMN_NAME_TARGET_DESC,
                    ConversionResultContract.ConversionEntry.COLUMN_NAME_VOLUME,
                    ConversionResultContract.ConversionEntry.COLUMN_NAME_CHANGE,
                    ConversionResultContract.ConversionEntry.COLUMN_NAME_DATETIME,
            };


            Cursor cursor = db.query(
                    ConversionResultContract.ConversionEntry.TABLE_NAME,
                    columns,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            while (cursor.moveToNext()) {
                String val = cursor.getString(cursor.getColumnIndex(ConversionResultContract.ConversionEntry.COLUMN_NAME_BASE));
                val = cursor.getString(cursor.getColumnIndex(ConversionResultContract.ConversionEntry.COLUMN_NAME_BASE_DESC));
                val = cursor.getString(cursor.getColumnIndex(ConversionResultContract.ConversionEntry.COLUMN_NAME_TARGET));
                val = cursor.getString(cursor.getColumnIndex(ConversionResultContract.ConversionEntry.COLUMN_NAME_TARGET_DESC));
                val = cursor.getString(cursor.getColumnIndex(ConversionResultContract.ConversionEntry.COLUMN_NAME_PRICE));
                val = cursor.getString(cursor.getColumnIndex(ConversionResultContract.ConversionEntry.COLUMN_NAME_VOLUME));
                val = cursor.getString(cursor.getColumnIndex(ConversionResultContract.ConversionEntry.COLUMN_NAME_CHANGE));
                val = cursor.getString(cursor.getColumnIndex(ConversionResultContract.ConversionEntry.COLUMN_NAME_DATETIME));
            }

            cursor.close();

        } else if (id == R.id.action_save) {

            try {

                // check if has result
                if (cryptonatorResponse != null && cryptonatorResponse.getTicker() != null) {

                    // Gets the data repository in write mode
                    SQLiteDatabase db = cxrDbHelper.getWritableDatabase();

                    // Create a new map of values, where column names are the keys
                    ContentValues values = new ContentValues();
                    values.put(ConversionResultContract.ConversionEntry.COLUMN_NAME_BASE, baseCurrency.getCode());
                    values.put(ConversionResultContract.ConversionEntry.COLUMN_NAME_BASE_DESC, baseCurrency.getName());

                    values.put(ConversionResultContract.ConversionEntry.COLUMN_NAME_TARGET, targetCurrency.getCode());
                    values.put(ConversionResultContract.ConversionEntry.COLUMN_NAME_TARGET_DESC, targetCurrency.getName());

                    Ticker ticker = cryptonatorResponse.getTicker();

                    values.put(ConversionResultContract.ConversionEntry.COLUMN_NAME_PRICE, ticker.getPrice());
                    values.put(ConversionResultContract.ConversionEntry.COLUMN_NAME_VOLUME, ticker.getVolume());
                    values.put(ConversionResultContract.ConversionEntry.COLUMN_NAME_CHANGE, ticker.getChange());
                    values.put(ConversionResultContract.ConversionEntry.COLUMN_NAME_DATETIME, cryptonatorResponse.getTimestamp());

                    // Insert the new row, returning the primary key value of the new row
                    long newRowId = db.insert(ConversionResultContract.ConversionEntry.TABLE_NAME, null, values);

                    showSnackbar("Result saved", findViewById(R.id.toolbar), Snackbar.LENGTH_SHORT);

                } else {
                    showSnackbar("No conversion results", findViewById(R.id.toolbar), Snackbar.LENGTH_INDEFINITE);
                }

            }catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
