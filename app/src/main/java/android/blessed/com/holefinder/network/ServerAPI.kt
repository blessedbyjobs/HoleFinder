package android.blessed.com.holefinder.network

import android.blessed.com.holefinder.models.RoadsResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Url

interface ServerAPI {
    interface OSM {
        @GET
        fun getRoads(@Url url: String) : Observable<RoadsResponse>
    }
}