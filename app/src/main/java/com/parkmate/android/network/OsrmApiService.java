package com.parkmate.android.network;

import com.parkmate.android.model.RouteResponse;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * API Service cho OSRM (Open Source Routing Machine)
 * Free routing service cho OpenStreetMap
 */
public interface OsrmApiService {

    /**
     * Lấy route từ điểm A đến điểm B
     * @param coordinates format: "lng1,lat1;lng2,lat2"
     * @param overview "full" để lấy toàn bộ geometry
     * @param geometries "geojson" hoặc "polyline"
     * @return RouteResponse
     */
    @GET("route/v1/driving/{coordinates}")
    Single<RouteResponse> getRoute(
        @Path("coordinates") String coordinates,
        @Query("overview") String overview,
        @Query("geometries") String geometries,
        @Query("steps") boolean steps
    );
}

