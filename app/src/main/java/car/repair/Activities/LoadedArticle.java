package car.repair.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import beans.Article;
import car.repair.R;
import http.connection.HttpConnection;

public class LoadedArticle extends AppCompatActivity {

    HashMap<Integer, Integer> imagesIds = new HashMap<>();

    private static final String DOWNLOAD_IMAGE_ENDPOINT = "/car-repair-server-1.0-SNAPSHOT/rest/downloadImage?id=";
    private static final String HTTP = "http://";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loaded_article);

        Article article = null;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            article = (Article) bundle.getSerializable("article");
            ConstraintLayout layout = findViewById(R.id.loaded_article_layout_id);
            ConstraintSet set = new ConstraintSet();

            List<View> allViews = new ArrayList<>();
            for (String paragraph : article.getContent()) {
                if (paragraph.startsWith("<img>")) {
                    addImageFromArticleContent(layout, allViews, paragraph);
                } else {
                    addTextFromArticleContent(layout, allViews, paragraph, Gravity.LEFT);
                }
            }

            addTextFromArticleContent(layout, allViews, "Автор: " + article.getAuthor(), Gravity.RIGHT);

            set.clone(layout);
            View firstView = allViews.get(0);
            set.connect(firstView.getId(), ConstraintSet.TOP, findViewById(R.id.article_divider).getId(), ConstraintSet.BOTTOM);
            set.connect(firstView.getId(), ConstraintSet.LEFT, findViewById(R.id.loaded_article_layout_id).getId(), ConstraintSet.RIGHT);
            set.connect(firstView.getId(), ConstraintSet.RIGHT, findViewById(R.id.loaded_article_layout_id).getId(), ConstraintSet.LEFT);

            for (int i = 1; i < allViews.size(); i++) {
                View view = allViews.get(i);
                set.connect(view.getId(), ConstraintSet.TOP, allViews.get(i-1).getId(), ConstraintSet.BOTTOM);
                set.connect(view.getId(), ConstraintSet.LEFT, findViewById(R.id.loaded_article_layout_id).getId(), ConstraintSet.RIGHT);
                set.connect(view.getId(), ConstraintSet.RIGHT, findViewById(R.id.loaded_article_layout_id).getId(), ConstraintSet.LEFT);
            }

            set.applyTo(layout);
        }

        if (article != null) {
            TextView loadedArticleTitle = findViewById(R.id.loaded_article_title);
            loadedArticleTitle.setText(article.getTitle());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.new_choice) {
            Intent begin = new Intent(LoadedArticle.this, BrandModel.class);
            startActivity(begin);
        } else if (item.getItemId() == R.id.logout) {
            LoginManager.getInstance().logOut();
            Intent login = new Intent(LoadedArticle.this, Login.class);
            startActivity(login);
            finish();
        }

        return true;
    }

    private void addTextFromArticleContent(ConstraintLayout layout, List<View> allViews, String paragraph, int left) {
        TextView textView = new TextView(LoadedArticle.this);
        int id = View.generateViewId();
        textView.setId(id);
        textView.setText(paragraph);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setGravity(left);
        textView.setLayoutParams(layoutParams);
        layout.addView(textView);
        allViews.add(textView);
    }

    private void addImageFromArticleContent(ConstraintLayout layout, List<View> allViews, String paragraph) {
        final ImageView imageView = new ImageView(LoadedArticle.this);
        int id = View.generateViewId();
        imageView.setId(id);
        imageView.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));
//        imageView.setLayoutParams(layoutParams);
        imageView.setAdjustViewBounds(true);
        imagesIds.put(id, Integer.valueOf(paragraph.substring(5, paragraph.indexOf("</"))));  //Put unique generatedId and id of the picture in DB.
        imageView.setTag("not set");
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("not set".equals(view.getTag())) {
                    int imageId = imagesIds.get(view.getId());
                    new ImageDownloader(imageView).execute(HTTP + BrandModel.IP + DOWNLOAD_IMAGE_ENDPOINT + imageId);
                    view.setTag("setted");
                } else {
                    //TODO code here to show image on full screen
//                    imageView.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));
//                    imageView.setAdjustViewBounds(true);
//                    imageView.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT));
//                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }
        });
        imageView.setImageResource(R.drawable.defaultphoto);
        layout.addView(imageView);
        allViews.add(imageView);
    }


    private class ImageDownloader extends AsyncTask<String, Void, String> {

        private Bitmap imageBitmap;
        private ImageView imageView;

        public ImageDownloader(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected String doInBackground(String... urls) {
            String result = null;

            imageBitmap = getDrawableFromStream(urls[0]);
            if (imageBitmap != null) {
                 result = "Изображението се зарежда!";
            } else {
                result = "Проблем със зареждането на изображението";
            }

            return result;
        }

        private Bitmap getDrawableFromStream(String url) {
            Bitmap bitmap = null;
            try {
                HttpURLConnection connection = HttpConnection.getHttpURLConnection(url, "GET", false, 10000, 10000);
                InputStream inputStream = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);

//                drawable = Drawable.createFromStream(inputStream, "fiat");
                return bitmap;
            } catch (IOException e) {
                Toast.makeText(LoadedArticle.this, "Възникна проблем при зареждането на изображението!", Toast.LENGTH_LONG).show();
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(String result) {
            if ("Изображението се зарежда!".equals(result)) {
                Toast.makeText(LoadedArticle.this, result, Toast.LENGTH_LONG).show();
                imageView.setImageBitmap(imageBitmap);
            } else {
                Toast.makeText(LoadedArticle.this, result, Toast.LENGTH_LONG).show();
            }
        }
    }
}
