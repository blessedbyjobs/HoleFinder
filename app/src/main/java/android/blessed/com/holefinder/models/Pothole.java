package android.blessed.com.holefinder.models;

public class Pothole {
    private String longitude;
    private String latitude;
    private float factor;
    private int id;

    public Pothole(String longitude, String latitude, float factor, int id) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.factor = factor;
        this.id = id;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public float getFactor() {
        return factor;
    }

    public void setFactor(float factor) {
        this.factor = factor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
