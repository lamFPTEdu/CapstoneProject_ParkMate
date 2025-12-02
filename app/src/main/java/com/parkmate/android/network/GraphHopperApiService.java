package com.parkmate.android.network;

import retrofit2.http.GET;
import retrofit2.http.Query;
import io.reactivex.rxjava3.core.Single;
import com.parkmate.android.model.GraphHopperResponse;

/**
 * GraphHopper Directions API
 * Free tier: 500 requests/day
 * Docs: https://docs.graphhopper.com/#tag/Routing-API
 */
public interface GraphHopperApiService {

    /**
     * Get route from origin to destination
     *
     * @param originPoint Origin point: "lat,lng"
     * @param destPoint Destination point: "lat,lng"
     * @param vehicle Vehicle type: car, bike, foot, etc.
     * @param locale Language for instructions
     * @param calcPoints Return route geometry (true/false)
     * @param pointsEncoded Use polyline encoding (true = more efficient)
     * @param apiKey GraphHopper API key
     * @return Single<GraphHopperResponse>
     */
    @GET("route")
    Single<GraphHopperResponse> getRoute(
        @Query("point") String originPoint,
        @Query("point") String destPoint,
        @Query("vehicle") String vehicle,
        @Query("locale") String locale,
        @Query("calc_points") boolean calcPoints,
        @Query("points_encoded") boolean pointsEncoded,
        @Query("key") String apiKey
    );
}

