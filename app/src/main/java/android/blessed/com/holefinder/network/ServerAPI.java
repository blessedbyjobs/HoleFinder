package android.blessed.com.holefinder.network;

import android.blessed.com.holefinder.models.Brand;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ServerAPI {
    @GET("brands")
    Call<List<Brand>> getBrands();
}
