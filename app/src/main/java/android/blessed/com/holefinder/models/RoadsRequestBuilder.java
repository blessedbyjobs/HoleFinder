package android.blessed.com.holefinder.models;

import org.osmdroid.util.BoundingBox;

public class RoadsRequestBuilder {
    private int timeout;
    private BoundingBox bbox;

    private static final String urlBeggining = "interpreter?data=";
    private static final String roadTypes = "\"highway\"~\"primary|tertiary|residential|service\"";

    public RoadsRequestBuilder(int timeout, BoundingBox bbox) {
        this.timeout = timeout;
        this.bbox = bbox;
    }

    @Override
    public String toString() {
        return urlBeggining + "[out:json][timeout:"
                + timeout + "];way[" + roadTypes + "](" + bboxToString() + ");out body geom;";
    }

    private String bboxToString() {
        return bbox.getLatNorth() + "," + bbox.getLonEast() + "," + bbox.getLatSouth() + "," + bbox.getLonWest();
    }
}
