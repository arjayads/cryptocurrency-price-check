package ads.check.rate.exchange_rate_check;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class MainActivity extends AppCompatActivity {

    AutoCompleteTextView baseAutoCompleteTV;
    AutoCompleteTextView targetAutoCompleteTV;

    String[] arr = { "Paries,France", "PA,United States","Parana,Brazil",
            "Padua,Italy", "Pasadena,CA,United States"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setBaseAutocompleteData();
    }

    private void setBaseAutocompleteData() {
        baseAutoCompleteTV = (AutoCompleteTextView)
                findViewById(R.id.base_autoCompleteTextView);

        ArrayAdapter<String> badapter = new ArrayAdapter<> (this,android.R.layout.select_dialog_item, arr);

        baseAutoCompleteTV.setThreshold(1);
        baseAutoCompleteTV.setAdapter(badapter);


        targetAutoCompleteTV = (AutoCompleteTextView)
                findViewById(R.id.target_autoCompleteTextView);

        ArrayAdapter<String> tadapter = new ArrayAdapter<> (this,android.R.layout.select_dialog_item, arr);

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
