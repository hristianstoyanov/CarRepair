package car.repair.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import beans.Article;
import car.repair.R;
import http.connection.HttpConnection;

public class ArticleCreator extends AppCompatActivity {

    private EditText title;
    private EditText articleContent;

    private String brand;
    private String model;
    private String category;
    private String autor = "unknown";
    private int brandId;
    private int modelId;
    private int categoryId;

    private static final int PICK_PHOTO = 1;
    private static final String HTTP = "http://";
    private static final String UPLOAD_ARTICLE_ENDPOINT = "/car-repair-server-1.0-SNAPSHOT/rest/article";
    private static final String UPLOAD_IMAGE_ENDPOINT = "/car-repair-server-1.0-SNAPSHOT/rest/upload";
    private static final String IMG_PATTERN = "(<img>[0-9]*?</img>)";
    private static int imagesCounter = 0;
    private static List<String> uploadedImages = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_creator);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            brand = bundle.getString("brand");
            brandId = bundle.getInt("brandId");
            model = bundle.getString("model");
            modelId = bundle.getInt("modelId");
            category = bundle.getString("category");
            categoryId = bundle.getInt("categoryId");
            if (bundle.getString("names") != null) {
                autor = bundle.getString("names");
            }
        } else {
            Toast.makeText(ArticleCreator.this, "Проблем при въртрешна обработка на данни!", Toast.LENGTH_LONG).show();
        }
        title = findViewById(R.id.article_title_edit_text);
        articleContent = findViewById(R.id.article_content_edit_text);


        Button sendArticleButton = findViewById(R.id.upload_article_button);
        sendArticleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendArticle();
            }
        });

        Button uploadImageButton = findViewById(R.id.upload_image_button);
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.new_choice) {
            Intent begin = new Intent(ArticleCreator.this, BrandModel.class);
            startActivity(begin);
        } else if (item.getItemId() == R.id.logout) {
            LoginManager.getInstance().logOut();
            Intent login = new Intent(ArticleCreator.this, Login.class);
            startActivity(login);
            finish();
        }

        return true;
    }

    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO);
    }

    private void uploadImageOnServer(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
        byte[] imageData = byteArrayOutputStream.toByteArray();

        String encodedString = Base64.encodeToString(imageData, Base64.NO_WRAP);
        new ImageSender(brand + "_" + model + "_" + category + new Random().nextInt(1000), encodedString).execute(HTTP + BrandModel.IP + UPLOAD_IMAGE_ENDPOINT);
        Toast.makeText(ArticleCreator.this, "Изображението се изпраща към сървъра.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(ArticleCreator.this, "Възникна проблем при избора на изображение.", Toast.LENGTH_SHORT).show();
                return;
            } else {
                try {
                    InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    uploadImageOnServer(bitmap);
                } catch (FileNotFoundException e) {
                    Toast.makeText(ArticleCreator.this, "Системата не може да намери указания файл.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void sendArticle() {
        if (title.getText() != null && title.getText().length() > 0 && articleContent.getText() != null && articleContent.getText().length() > 0) {
            if (isImgElementsCorrect(articleContent.getText().toString())) {

                Bundle bundle = getIntent().getExtras();
                String author = "unknown";
                if (bundle != null) {
                    int brandId = bundle.getInt("brandId");
                    int modelId = bundle.getInt("modelId");
                    int categoryId = bundle.getInt("categoryId");
                    if (bundle.getString("names") != null) {
                        author = bundle.getString("names");
                    }
                    List<String> articleContentList = new ArrayList<>();
                    articleContentList.add(articleContent.getText().toString());
                    Article article = new Article(brandId, modelId, categoryId, title.getText().toString(), author, articleContentList, null);
                    new ArticleSender(article).execute(HTTP + BrandModel.IP + UPLOAD_ARTICLE_ENDPOINT);
                    Toast.makeText(ArticleCreator.this, "Статията се изпрща.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ArticleCreator.this, "Възникна проблем при обработката на данните преди изпращане!", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(ArticleCreator.this, "Моля не редактирайте <img> елементите!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(ArticleCreator.this, "Въведете заглавие и съдържание на статията!", Toast.LENGTH_LONG).show();
        }
    }

    private static boolean isImgElementsCorrect(String articleContent) {
        Pattern pattern = Pattern.compile(IMG_PATTERN);
        Matcher matcher = pattern.matcher(articleContent);

        int numberOfImgElements = 0;
        while (matcher.find()) {
            if (!uploadedImages.contains(matcher.group(0))) {
                return false;
            }
        }

        return true;
    }


    private class ArticleSender extends AsyncTask<String, Void, String> {

        private Article article;

        public ArticleSender(Article article) {
            this.article = article;
        }

        @Override
        protected String doInBackground(String... urls) {

            String articleJson = new Gson().toJson(article);

            String responseStatus = "Error";
            PrintStream writer = null;
            try {
                HttpURLConnection connection = HttpConnection.getHttpURLConnection(urls[0], "POST", true, 20000, 20000);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                writer = new PrintStream(connection.getOutputStream());
                writer.print(articleJson);
                Scanner responseReader = new Scanner(new InputStreamReader(connection.getInputStream()));

                while (responseReader.hasNextLine()) {
                    responseStatus = responseReader.nextLine();
//                    responseStatus = Integer.parseInt(responseReader.nextLine());
                }
            } catch (IOException e) {
//                Toast.makeText(Categories.this, "Сървъра не успя да извлече исканата от Вас категория. Опитайте по-късно.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }

            return responseStatus;
        }

        @Override
        protected void onPostExecute(String result) {
            if ("Upload success!".equals(result)) {
                Toast.makeText(ArticleCreator.this, "Статията е записана успешно.", Toast.LENGTH_LONG).show();
                nextActivity();
            } else {
                Toast.makeText(ArticleCreator.this, "Възникна грешка. Вероятно съществува статия със същото заглавие.", Toast.LENGTH_LONG).show();
            }
        }

        public void nextActivity() {
            Intent articleActivity = new Intent(ArticleCreator.this, Articles.class);
            articleActivity.putExtra("brand", brand);
            articleActivity.putExtra("brandId", brandId);
            articleActivity.putExtra("model", model);
            articleActivity.putExtra("modelId", modelId);
            articleActivity.putExtra("category", category);
            articleActivity.putExtra("categoryId", categoryId);

            startActivity(articleActivity);
        }
    }

    private class ImageSender extends AsyncTask<String, Void, Integer> {

        private String fileName;
        private String encodedImage;

        public ImageSender(String fileName, String encodedImage) {
            this.fileName = fileName;
            this.encodedImage = encodedImage;
        }

        @Override
        public Integer doInBackground(String... urls) {
            int responseStatus = -1;
            PrintStream writer = null;

            try {
                HttpURLConnection connection = HttpConnection.getHttpURLConnection(urls[0], "POST", true, 1000000000, 1000000000);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

                writer = new PrintStream(connection.getOutputStream());
                String query = "encodedImage=" + encodedImage + "&fileName=" + fileName;
                writer.print(query);

                Scanner responseReader = new Scanner(new InputStreamReader(connection.getInputStream()));

                while (responseReader.hasNextLine()) {
                    responseStatus = Integer.parseInt(responseReader.nextLine());
//                    responseStatus = Integer.parseInt(responseReader.nextLine());
                }
            } catch (IOException e) {
                Toast.makeText(ArticleCreator.this, "Проблем при изпращането на снимката!", Toast.LENGTH_LONG).show();
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }

            return responseStatus;
        }

        @Override
        protected void onPostExecute(Integer result) {

            if (result > 0) {
                String imageId = "<img>" + result.toString() + "</img>";
                uploadedImages.add(imageId);
                articleContent.getText().append(imageId);
                imagesCounter++;
            } else {
                Toast.makeText(ArticleCreator.this, "Не успешно качване на снимка!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
