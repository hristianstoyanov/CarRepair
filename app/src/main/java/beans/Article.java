package beans;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Article implements Serializable{

    private int id;
    private int brandId;
    private int modelId;
    private int categoryId;
    private String title;
    private String author;
    private List<String> content;
    Date date;

    public Article(int id, String title, Date date) {
        this.id = id;
        this.title = title;
        this.date = date;
    }

    public Article(int brandId, int modelId, int categoryId, String title, String author, List<String> content, Date date) {
        this.brandId = brandId;
        this.modelId = modelId;
        this.categoryId = categoryId;
        this.title = title;
        this.author = author;
        this.content = content;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBrandId() {
        return brandId;
    }

    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }

    public int getModelId() {
        return modelId;
    }

    public void setModelId(int modelId) {
        this.modelId = modelId;
    }

    public List<String> getContent() {
        return content;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
