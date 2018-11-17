package car.repair.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.login.LoginManager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Scanner;

import car.repair.R;
import http.connection.HttpConnection;

public class Categories extends AppCompatActivity {

    private String category;
    private static final String HTTP = "http://";
    private static final String CATEGORIES_ENDPOINT = "/car-repair-server-1.0-SNAPSHOT/rest/categoryId?category=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        ImageViewOnClickListener imageViewOnClickListener = new ImageViewOnClickListener();

        ImageView engine = findViewById(R.id.Engine);
        engine.setOnClickListener(imageViewOnClickListener);

        ImageView suspension = findViewById(R.id.Suspension);
        suspension.setOnClickListener(imageViewOnClickListener);

        ImageView brakes = findViewById(R.id.Brakes);
        brakes.setOnClickListener(imageViewOnClickListener);

        ImageView electricity = findViewById(R.id.Electricity);
        electricity.setOnClickListener(imageViewOnClickListener);

        ImageView interior = findViewById(R.id.Interior);
        interior.setOnClickListener(imageViewOnClickListener);

        ImageView exterior = findViewById(R.id.Exterior);
        exterior.setOnClickListener(imageViewOnClickListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.new_choice) {
            Intent begin = new Intent(Categories.this, BrandModel.class);
            startActivity(begin);
        } else if (item.getItemId() == R.id.logout) {
            LoginManager.getInstance().logOut();
            Intent login = new Intent(Categories.this, Login.class);
            startActivity(login);
            finish();
        }

        return true;
    }

    private class ImageViewOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            category = getResources().getResourceEntryName(view.getId());
            new CategoryIdRequestor().execute(HTTP + BrandModel.IP + CATEGORIES_ENDPOINT + category);
        }
    }

    private class CategoryIdRequestor extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... urls) {

            int id = -1;
            try {
                HttpURLConnection connection = HttpConnection.getHttpURLConnection(urls[0], "GET", false, 5000, 5000);
                Scanner responseReader = new Scanner(new InputStreamReader(connection.getInputStream()));

                while (responseReader.hasNextLine()) {
                    id = Integer.parseInt(responseReader.nextLine());
                }
            }  catch (IOException e) {
                Toast.makeText(Categories.this, "Сървъра не успя да извлече исканата от Вас категория. Опитайте по-късно.", Toast.LENGTH_LONG).show();
            }

            return  id;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result > 0) {
                nextActivity(result);
            } else {
                Toast.makeText(Categories.this, "Грешка при комуникацията със сървъра!", Toast.LENGTH_LONG).show();
            }
        }

        private void nextActivity(int categoryId) {

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                Intent nextActivityIntent = new Intent(Categories.this, Articles.class);
                nextActivityIntent.putExtra("brand", bundle.getString("brand"));
                nextActivityIntent.putExtra("brandId", bundle.getInt("brandId"));
                nextActivityIntent.putExtra("model", bundle.getString("model"));
                nextActivityIntent.putExtra("modelId", bundle.getInt("modelId"));
                nextActivityIntent.putExtra("category", category);
                nextActivityIntent.putExtra("categoryId", categoryId);

                Bundle dataFromLastActivity = getIntent().getExtras();
                if (dataFromLastActivity != null) {
                    nextActivityIntent.putExtra("names", dataFromLastActivity.getString("names"));
                }
                startActivity(nextActivityIntent);
            } else {
                Toast.makeText(Categories.this, "Грешка при комуникацията със сървъра!", Toast.LENGTH_LONG).show();
            }

        }
    }
}
