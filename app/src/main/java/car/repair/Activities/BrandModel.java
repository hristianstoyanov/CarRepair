package car.repair.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.view.Menu;

import com.facebook.login.LoginManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import car.repair.R;
import http.connection.HttpConnection;

public class BrandModel extends AppCompatActivity {

    private HashMap<String, Integer> brandsMap = new HashMap<>();
    private HashMap<String, Integer> modelsMap = new HashMap<>();
    private Spinner brandSpinner;
    private Spinner modelSpinner;
    private int selectedBrandId;
    private int selectedModelId;
    private String selectedBrand;
    private String selectedModel;

    private static final String HTTP = "http://";
    private static final String BRANDS_ENDPOINT = "/car-repair-server-1.0-SNAPSHOT/rest/brands";
    private static final String MODELS_ENDPOINT = "/car-repair-server-1.0-SNAPSHOT/rest/models?brandId=";
    private static final String DEFAULT_BRAND_MODEL = "<Всички>";

    public static String IP;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand_model);
        String connectedDeviceIP = getServerIP();
        if (connectedDeviceIP != null) {
            IP = connectedDeviceIP + ":8080";
            Toast.makeText(this, "IP: " + IP, Toast.LENGTH_LONG);
            new BrandsRequestor().execute(HTTP + IP + BRANDS_ENDPOINT);
        }

       System.out.println("IP ------------------> " + IP);
        Button logout = findViewById(R.id.logout);
        brandSpinner = findViewById(R.id.brandSpinner);
        modelSpinner = findViewById(R.id.modelSpinner);

        brandSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedBrandId = 0;
                selectedBrand = brandSpinner.getSelectedItem().toString();
                ArrayAdapter<String> default_spinner = new ArrayAdapter<>(BrandModel.this, android.R.layout.simple_spinner_item, new String[]{DEFAULT_BRAND_MODEL});
                modelSpinner.setAdapter(default_spinner);
                if (!DEFAULT_BRAND_MODEL.equals(selectedBrand)) {
                    selectedBrandId = brandsMap.get(selectedBrand);
                    new ModelsRequestor().execute(HTTP + IP + MODELS_ENDPOINT + selectedBrandId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedModelId = 0;
                selectedModel = modelSpinner.getSelectedItem().toString();
                if (!DEFAULT_BRAND_MODEL.equals(selectedModel)) {
                    selectedModelId = modelsMap.get(selectedModel);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ImageView nextPageButton = findViewById(R.id.nextPageButton);
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextActivity();
            }
        });
    }

    public String getServerIP() {
        String ip = null;
        try {
            Scanner fileReader = new Scanner(new File("/proc/net/arp"));

            while (fileReader.hasNextLine()) {
                String line = fileReader.nextLine();
                ip = line.split("[ ]+")[0];
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ip;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout_button) {
            LoginManager.getInstance().logOut();
            Intent login = new Intent(BrandModel.this, Login.class);
            startActivity(login);
            finish();
        }

        return true;
    }

    private void nextActivity() {

        if (selectedBrandId > 0 && selectedModelId > 0) {
            Intent intent = new Intent(BrandModel.this, Categories.class);
            Bundle dataFromLastActivity = getIntent().getExtras();
            intent.putExtra("brand", selectedBrand);
            intent.putExtra("brandId", selectedBrandId);
            intent.putExtra("model", selectedModel);
            intent.putExtra("modelId", selectedModelId);
            if (dataFromLastActivity != null) {
                intent.putExtra("names", dataFromLastActivity.getString("names"));
            }
            startActivity(intent);
        } else {
            Toast.makeText(BrandModel.this, "Трябва да изберете марка и модел", Toast.LENGTH_LONG).show();
        }
    }

    private class BrandsRequestor extends AsyncTask<String, Void, List<String>> {

        @Override
        protected List<String> doInBackground(String... urls) {

            List<String> brands = new ArrayList<>();
            brands.add(DEFAULT_BRAND_MODEL);
            StringBuilder response = new StringBuilder();
            try {
                HttpURLConnection connection = HttpConnection.getHttpURLConnection(urls[0], "GET", false, 50000, 50000);
                Scanner responseReader = new Scanner(new InputStreamReader(connection.getInputStream()));

                while (responseReader.hasNextLine()) {
                    response.append(responseReader.nextLine());
                }

                parseJSONWithAllBrands(brands, response);

            } catch (ProtocolException | JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return brands;
        }

        private void parseJSONWithAllBrands(List<String> brands, StringBuilder response) throws JSONException {
            JSONArray arrayOfBrands = new JSONArray(response.toString());
            for (int i = 0; i < arrayOfBrands.length(); i++) {
                JSONObject jsonObject = arrayOfBrands.getJSONObject(i);
                brandsMap.put(jsonObject.getString("brandName"), jsonObject.getInt("id"));
                brands.add(jsonObject.getString("brandName"));
            }
        }


        @Override
        protected void onProgressUpdate(Void... progress) {
        }

        protected void onPostExecute(List<String> result) {
            brandSpinner = findViewById(R.id.brandSpinner);
            ArrayAdapter<String> brands = new ArrayAdapter<>(BrandModel.this, android.R.layout.simple_spinner_item, result);
            brandSpinner.setAdapter(brands);
        }
    }


    private class ModelsRequestor extends AsyncTask<String, Void, List<String>> {

        @Override
        protected List<String> doInBackground(String... urls) {

            List<String> models = new ArrayList<>();
            models.add(DEFAULT_BRAND_MODEL);
            StringBuilder response = new StringBuilder();
            try {
                HttpURLConnection connection = HttpConnection.getHttpURLConnection(urls[0], "GET", false, 5000, 5000);
                Scanner responseReader = new Scanner(new InputStreamReader(connection.getInputStream()));

                while (responseReader.hasNextLine()) {
                    response.append(responseReader.nextLine());
                }

                parseJSONWithAllModels(models, response);

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return  models;
        }

        @Override
        protected void onPostExecute(List<String> result) {

            modelSpinner = findViewById(R.id.modelSpinner);
            ArrayAdapter<String> models = new ArrayAdapter<>(BrandModel.this, android.R.layout.simple_spinner_item, result);
            modelSpinner.setAdapter(models);
        }

        private void parseJSONWithAllModels(List<String> models, StringBuilder response) throws JSONException {
            JSONArray jsonArray = new JSONArray(response.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                modelsMap.put(jsonObject.getString("modelName"), jsonObject.getInt("id"));
                models.add(jsonObject.getString("modelName"));
            }
        }

    }
}



