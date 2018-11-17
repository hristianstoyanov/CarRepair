package beans;

public class Image {

    private String name;
    private String encodedData;

    public Image(String name, String encodedData) {
        this.name = name;
        this.encodedData = encodedData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEncodedData() {
        return encodedData;
    }

    public void setEncodedData(String encodedData) {
        this.encodedData = encodedData;
    }
}
