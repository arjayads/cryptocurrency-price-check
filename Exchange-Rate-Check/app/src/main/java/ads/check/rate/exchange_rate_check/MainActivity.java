package ads.check.rate.exchange_rate_check;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    AutoCompleteTextView baseAutoCompleteTV;
    AutoCompleteTextView targetAutoCompleteTV;
 
    ArrayList<Currency> currencyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        readCurrencies();
        setBaseAutocompleteData();
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
