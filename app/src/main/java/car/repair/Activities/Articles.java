package car.repair.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import Utils.CSVparser;
import beans.Article;
import car.repair.R;
import http.connection.HttpConnection;

public class Articles extends AppCompatActivity {

    private int brandId = -1;
    private int modelId = -1;
    private int categoryId = 1;
    private String brand;
    private String model;
    private String category;
    private String user;

    private static final String HTTP = "http://";
    private static final String ARTICLES_TITLE_ENDPOINT = "/car-repair-server-1.0-SNAPSHOT/rest/articles?brandId=";
    private static final String ARTICLE_BY_ID_ENDPOINT = "/car-repair-server-1.0-SNAPSHOT/rest/articleById?id=";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articles);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            brand = bundle.getString("brand");
            brandId = bundle.getInt("brandId");
            model = bundle.getString("model");
            modelId = bundle.getInt("modelId");
            category = bundle.getString("category");
            categoryId = bundle.getInt("categoryId");
            user = bundle.getString("names");
            new ArticleTitleRequestor().execute(HTTP + BrandModel.IP + ARTICLES_TITLE_ENDPOINT + brandId + "&modelId=" +modelId + "&categoryId=" +categoryId);

            if (brand != null) {
                setImageFromFileName(brand);
                TextView articlesLabel = findViewById(R.id.articlesLabel);
                articlesLabel.setText("Всички статии за\n" + brand + " " +  model);
                articlesLabel.setGravity(Gravity.CENTER);
            }
        } else {
            Toast.makeText(Articles.this, "Грешка при стартирането на активитито. Опитайте отново!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent articlesActivity = new Intent(Articles.this, Categories.class);
        articlesActivity.putExtra("brand", brand);
        articlesActivity.putExtra("brandId", brandId);
        articlesActivity.putExtra("model", model);
        articlesActivity.putExtra("modelId", modelId);
        articlesActivity.putExtra("category", category);
        articlesActivity.putExtra("categoryId", categoryId);
        articlesActivity.putExtra("names", user);
        startActivity(articlesActivity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_articles_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.new_choice) {
            Intent begin = new Intent(Articles.this, BrandModel.class);
            startActivity(begin);
        } else if (item.getItemId() == R.id.logout) {
            LoginManager.getInstance().logOut();
            Intent login = new Intent(Articles.this, Login.class);
            startActivity(login);
            finish();
        } else if (item.getItemId() == R.id.create_article) {
            if (brandId != -1 && modelId != -1 && categoryId != -1) {
                Intent createArticle = new Intent(Articles.this, ArticleCreator.class);
                createArticle.putExtra("brand", brand);
                createArticle.putExtra("brandId", brandId);
                createArticle.putExtra("modelId", modelId);
                createArticle.putExtra("model", model);
                createArticle.putExtra("category", category);
                createArticle.putExtra("categoryId", categoryId);
                createArticle.putExtra("names", user);

                startActivity(createArticle);
            } else {
                Toast.makeText(Articles.this, "Грешка при обработване на данните. Не може да се създаде статия!", Toast.LENGTH_LONG).show();
            }
        }

        return true;
    }

    private void setImageFromFileName(String fileName) {
        Resources resources = this.getResources();
        if (fileName.contains(" ")) {
            fileName = fileName.replaceAll(" ", "");
        }
        final int resourceId = resources.getIdentifier(fileName.toLowerCase(), "drawable",
                this.getPackageName());
        ImageView imageView = findViewById(R.id.brand_imageView);
        imageView.setImageResource(resourceId);
    }


    private class ArticleTitleRequestor extends AsyncTask<String, Void, List<Article>> {

        @Override
        protected List<Article> doInBackground(String... urls) {

            List<Article> articles = new ArrayList<>();
            StringBuilder response = new StringBuilder();
            try {
                HttpURLConnection connection = HttpConnection.getHttpURLConnection(urls[0], "GET", false, 5000, 5000);
                Scanner responseReader = new Scanner(new InputStreamReader(connection.getInputStream()));

                while (responseReader.hasNextLine()) {
                    response.append(responseReader.nextLine());
                }

                parseJSONWithAllTitles(articles, response);

            } catch (ProtocolException | JSONException e) {
                Toast.makeText(Articles.this, "Грешка при свързването със сървъра. Не могат да бъдат заредени статиите.", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return articles;
        }

        private void parseJSONWithAllTitles(List<Article> articles, StringBuilder response) throws JSONException {
            JSONArray arrayOfBrands = new JSONArray(response.toString());
            for (int i = 0; i < arrayOfBrands.length(); i++) {
                JSONObject jsonObject = arrayOfBrands.getJSONObject(i);
                try {
                    Date date = parseDate(jsonObject.getString("date"));
                    articles.add(new Article(jsonObject.getInt("id"), jsonObject.getString("title"), date));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        private Date parseDate(String dateString) throws ParseException {
            dateString = dateString.substring(0 , dateString.indexOf('+'));

            DateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date date = parser.parse(dateString);

            return date;
        }


        @Override
        protected void onProgressUpdate(Void... progress) {
        }

        protected void onPostExecute(List<Article> articles) {
            GridView gridView = findViewById(R.id.articlesGridView);
            ArticlesAdapter articlesAdapter = new ArticlesAdapter(Articles.this, articles);
            gridView.setAdapter(articlesAdapter);
//            Toast.makeText(BrandModel.this, result, Toast.LENGTH_LONG).show();
        }
    }

    private final class ArticlesAdapter extends BaseAdapter {

        private final Context context;
        private final List<Article> articles;

        public ArticlesAdapter(Context context, List<Article> articles) {
            this.context = context;
            this.articles = articles;
        }

        public int getCount() {
            return articles.size();
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final Article article = articles.get(position);

            if (convertView == null) {
                final LayoutInflater layoutInflater = LayoutInflater.from(context);
                convertView = layoutInflater.inflate(R.layout.gridlayout_articles, null);
            }

            ImageView currentCategory = convertView.findViewById(R.id.current_category);
            TextView articleTitle = convertView.findViewById(R.id.articleTitle);
            TextView articleDate = convertView.findViewById(R.id.article_date);

            if ("Engine".equals(category)) {
                currentCategory.setImageResource(R.drawable.engine);
            } else if ("Suspension".equals(category)) {
                currentCategory.setImageResource(R.drawable.suspension);
            } else if ("Brakes".equals(category)) {
                currentCategory.setImageResource(R.drawable.brakes);
            } else if ("Brakes".equals(category)) {
                currentCategory.setImageResource(R.drawable.brakes);
            } else if ("Electricity".equals(category)) {
                currentCategory.setImageResource(R.drawable.battery);
            } else if ("Interior".equals(category)) {
                currentCategory.setImageResource(R.drawable.carinterior);
            } else if ("Exterior".equals(category)) {
                currentCategory.setImageResource(R.drawable.carexterior);
            }
            articleTitle.setText(article.getTitle());
            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            String formattedDate = formatter.format(article.getDate());
            articleDate.setText("Дата: " + formattedDate);
            articleTitle.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
//                    Toast.makeText(Articles.this, "Вие избрахте статия с id=" + article.getId(), Toast.LENGTH_LONG).show();
                new ArticleRequestor().execute(HTTP + BrandModel.IP + ARTICLE_BY_ID_ENDPOINT + article.getId());
                }
            });
//            articleTitle.setBackgroundColor(Color.WHITE);

            return convertView;
        }
    }


    private class ArticleRequestor extends AsyncTask<String, Void, Article> {

        @Override
        protected Article doInBackground(String... urls) {

            Article article = null;
            List<Article> articles = new ArrayList<>();
            StringBuilder response = new StringBuilder();
            try {
                HttpURLConnection connection = HttpConnection.getHttpURLConnection(urls[0], "GET", false, 50000, 50000);
                Scanner responseReader = new Scanner(new InputStreamReader(connection.getInputStream()));

                while (responseReader.hasNextLine()) {
                    response.append(responseReader.nextLine());
                }

                article = new Gson().fromJson(response.toString(), Article.class);

            } catch (ProtocolException e) {
                Toast.makeText(Articles.this, "Грешка при свързването със сървъра. Не могат да бъдат заредени статиите.", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return article;
        }


        protected void onPostExecute(Article article) {
            if (article != null) {
                Toast.makeText(Articles.this, "Статията се зарежда!", Toast.LENGTH_LONG).show();
                Intent loadArticleIntent = new Intent(Articles.this, LoadedArticle.class);
                loadArticleIntent.putExtra("article", article);
                startActivity(loadArticleIntent);
            } else {
                Toast.makeText(Articles.this, "Проблем със зареждането на статията. Опитайте отново!", Toast.LENGTH_LONG).show();
            }
//            Toast.makeText(BrandModel.this, result, Toast.LENGTH_LONG).show();
        }
    }
}
