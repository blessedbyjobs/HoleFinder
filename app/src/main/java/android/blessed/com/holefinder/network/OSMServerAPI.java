package android.blessed.com.holefinder.network;

import android.blessed.com.holefinder.models.RoadsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface OSMServerAPI {
    @GET
    Call<RoadsResponse> getRoads(@Url String url);
}
