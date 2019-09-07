package android.blessed.com.holefinder.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RoadsResponse {

    @SerializedName("version")
    @Expose
    private Double version;
    @SerializedName("generator")
    @Expose
    private String generator;
    @SerializedName("osm3s")
    @Expose
    private Osm3s osm3s;
    @SerializedName("elements")
    @Expose
    private List<Element> elements = null;

    public Double getVersion() {
        return version;
    }

    public void setVersion(Double version) {
        this.version = version;
    }

    public String getGenerator() {
        return generator;
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    public Osm3s getOsm3s() {
        return osm3s;
    }

    public void setOsm3s(Osm3s osm3s) {
        this.osm3s = osm3s;
    }

    public List<Element> getElements() {
        return elements;
    }

    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

    public class Bounds {

        @SerializedName("minlat")
        @Expose
        private Double minlat;
        @SerializedName("minlon")
        @Expose
        private Double minlon;
        @SerializedName("maxlat")
        @Expose
        private Double maxlat;
        @SerializedName("maxlon")
        @Expose
        private Double maxlon;

        public Double getMinlat() {
            return minlat;
        }

        public void setMinlat(Double minlat) {
            this.minlat = minlat;
        }

        public Double getMinlon() {
            return minlon;
        }

        public void setMinlon(Double minlon) {
            this.minlon = minlon;
        }

        public Double getMaxlat() {
            return maxlat;
        }

        public void setMaxlat(Double maxlat) {
            this.maxlat = maxlat;
        }

        public Double getMaxlon() {
            return maxlon;
        }

        public void setMaxlon(Double maxlon) {
            this.maxlon = maxlon;
        }

    }

    public class Element {

        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("id")
        @Expose
        private Long id;
        @SerializedName("bounds")
        @Expose
        private Bounds bounds;
        @SerializedName("nodes")
        @Expose
        private List<Long> nodes = null;
        @SerializedName("geometry")
        @Expose
        private List<Geometry> geometry = null;
        @SerializedName("tags")
        @Expose
        private Tags tags;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public Bounds getBounds() {
            return bounds;
        }

        public void setBounds(Bounds bounds) {
            this.bounds = bounds;
        }

        public List<Long> getNodes() {
            return nodes;
        }

        public void setNodes(List<Long> nodes) {
            this.nodes = nodes;
        }

        public List<Geometry> getGeometry() {
            return geometry;
        }

        public void setGeometry(List<Geometry> geometry) {
            this.geometry = geometry;
        }

        public Tags getTags() {
            return tags;
        }

        public void setTags(Tags tags) {
            this.tags = tags;
        }

    }

    public class Geometry {

        @SerializedName("lat")
        @Expose
        private Double lat;
        @SerializedName("lon")
        @Expose
        private Double lon;

        public Double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public Double getLon() {
            return lon;
        }

        public void setLon(Double lon) {
            this.lon = lon;
        }

    }

    public class Osm3s {

        @SerializedName("timestamp_osm_base")
        @Expose
        private String timestampOsmBase;
        @SerializedName("copyright")
        @Expose
        private String copyright;

        public String getTimestampOsmBase() {
            return timestampOsmBase;
        }

        public void setTimestampOsmBase(String timestampOsmBase) {
            this.timestampOsmBase = timestampOsmBase;
        }

        public String getCopyright() {
            return copyright;
        }

        public void setCopyright(String copyright) {
            this.copyright = copyright;
        }

    }

    public class Tags {

        @SerializedName("highway")
        @Expose
        private String highway;
        @SerializedName("lanes")
        @Expose
        private String lanes;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("name:en")
        @Expose
        private String nameEn;
        @SerializedName("name:ru")
        @Expose
        private String nameRu;
        @SerializedName("oneway")
        @Expose
        private String oneway;
        @SerializedName("surface")
        @Expose
        private String surface;

        public String getHighway() {
            return highway;
        }

        public void setHighway(String highway) {
            this.highway = highway;
        }

        public String getLanes() {
            return lanes;
        }

        public void setLanes(String lanes) {
            this.lanes = lanes;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNameEn() {
            return nameEn;
        }

        public void setNameEn(String nameEn) {
            this.nameEn = nameEn;
        }

        public String getNameRu() {
            return nameRu;
        }

        public void setNameRu(String nameRu) {
            this.nameRu = nameRu;
        }

        public String getOneway() {
            return oneway;
        }

        public void setOneway(String oneway) {
            this.oneway = oneway;
        }

        public String getSurface() {
            return surface;
        }

        public void setSurface(String surface) {
            this.surface = surface;
        }

    }

}