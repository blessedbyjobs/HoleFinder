package android.blessed.com.holefinder.network;

import android.blessed.com.holefinder.models.Brand;
import android.blessed.com.holefinder.models.RoadsResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface VistarServerAPI {
    @GET("brands")
    Call<List<Brand>> getBrands();
}
