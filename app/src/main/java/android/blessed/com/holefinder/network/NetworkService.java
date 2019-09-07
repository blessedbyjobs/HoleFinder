package android.blessed.com.holefinder.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkService {
    private static NetworkService mInstance;
    private static final String BASE_URL = "http://192.168.0.40:8080/";
    private static final String OSM_URL = "http://overpass-api.de/api/";

    private Retrofit mRetrofitVistar;
    private Retrofit mRetrofitOSM;

    private NetworkService() {
        mRetrofitVistar = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mRetrofitOSM = new Retrofit.Builder()
                .baseUrl(OSM_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static NetworkService getInstance() {
        if (mInstance == null) {
            mInstance = new NetworkService();
        }
        return mInstance;
    }

    public VistarServerAPI getServerApi() {
        return mRetrofitVistar.create(VistarServerAPI.class);
    }
    public OSMServerAPI getOSMApi() {
        return mRetrofitOSM.create(OSMServerAPI.class);
    }
}
