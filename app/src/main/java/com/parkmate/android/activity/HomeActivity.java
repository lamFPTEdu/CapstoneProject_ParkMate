package com.parkmate.android.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.messaging.FirebaseMessaging;
import com.parkmate.android.R;
import com.parkmate.android.model.request.CreateMobileDeviceRequest;
import com.parkmate.android.network.ApiClient;
import com.parkmate.android.network.ApiService;
import com.parkmate.android.network.OsrmApiClient;
import com.parkmate.android.network.OsrmApiService;
import com.parkmate.android.model.response.ParkingLotResponse;
import com.parkmate.android.model.RouteResponse;
import com.parkmate.android.util.PolylineUtils;

import org.maplibre.android.MapLibre;
import org.maplibre.android.annotations.Icon;
import org.maplibre.android.annotations.IconFactory;
import org.maplibre.android.annotations.Marker;
import org.maplibre.android.annotations.MarkerOptions;
import org.maplibre.android.annotations.Polyline;
import org.maplibre.android.annotations.PolylineOptions;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.geometry.LatLngBounds;
import org.maplibre.android.location.LocationComponent;
import org.maplibre.android.location.LocationComponentActivationOptions;
import org.maplibre.android.location.engine.LocationEngine;
import org.maplibre.android.location.engine.LocationEngineCallback;
import org.maplibre.android.location.engine.LocationEngineRequest;
import org.maplibre.android.location.engine.LocationEngineResult;
import org.maplibre.android.location.modes.CameraMode;
import org.maplibre.android.location.modes.RenderMode;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.Style;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomeActivity extends BaseActivity {

    private static final String TAG = "HomeActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final long LOCATION_UPDATE_INTERVAL = 1000L;
    private static final long LOCATION_UPDATE_FASTEST_INTERVAL = 500L;

    private MapView mapView;
    private MapLibreMap mapLibreMap;
    private LocationComponent locationComponent;
    private FloatingActionButton fabMyLocation;
    private boolean isFirstLocationUpdate = true;
    private LocationEngineCallback<LocationEngineResult> locationCallback;
    private ApiService apiService;
    private OsrmApiService osrmApiService;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final java.util.HashMap<Long, Long> markerToParkingLotMap = new java.util.HashMap<>();

    // Routing variables
    private Polyline currentRoutePolyline;
    private Marker destinationMarker;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // Khởi tạo MapLibre TRƯỚC KHI inflate layout
        if (!MapLibre.hasInstance()) {
            MapLibre.getInstance(base);
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_home_content;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "╔════════════════════════════════════════╗");
        Log.d(TAG, "║   HomeActivity onCreate() STARTED     ║");
        Log.d(TAG, "╚════════════════════════════════════════╝");

        // Khởi tạo API service
        apiService = ApiClient.getApiService();
        osrmApiService = OsrmApiClient.getApiService();
        Log.d(TAG, "✓ API service initialized");

        // Setup toolbar with search (no navigation icon for Home screen)
        setupToolbarWithSearch(
                true, // isMainScreen = true
                "Tìm kiếm bãi đỗ xe...",
                v -> onSearchBarClicked(),
                null, // No filter button - search handled in SearchParkingActivity
                false // Hide navigation icon
        );

        // Setup bottom navigation with Home selected
        setupBottomNavigation(true, R.id.nav_home);

        // Load notification badge count from API
        loadNotificationBadgeCount();

        // Khởi tạo MapView
        initMapView(savedInstanceState);

        // Tự động request location permission
        if (!checkLocationPermission()) {
            requestLocationPermission();
        }

        // Đăng ký FCM token
        registerFcmToken();

        // Xử lý intent để vẽ route nếu có
        handleRoutingIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent called");
        setIntent(intent); // Important: update the intent

        // Reset navbar highlight về Home khi routing intent
        if (intent.hasExtra("destination_lat")) {
            setupBottomNavigation(true, R.id.nav_home);
        }

        handleRoutingIntent();
    }

    /**
     * Xử lý intent khi được gọi từ ParkingLotDetailActivity để vẽ route
     */
    private void handleRoutingIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("destination_lat") && intent.hasExtra("destination_lng")) {
            double destLat = intent.getDoubleExtra("destination_lat", 0);
            double destLng = intent.getDoubleExtra("destination_lng", 0);
            String destName = intent.getStringExtra("destination_name");

            Log.d(TAG, "╔════════════════════════════════════════╗");
            Log.d(TAG, "║   ROUTING INTENT DETECTED             ║");
            Log.d(TAG, "║   Destination: " + destName);
            Log.d(TAG, "║   Location: [" + destLat + ", " + destLng + "]");
            Log.d(TAG, "╚════════════════════════════════════════╝");

            // Hiển thị toast để user biết đang xử lý
            Toast.makeText(this, "Đang tìm đường đến " + (destName != null ? destName : "bãi xe") + "...", Toast.LENGTH_SHORT).show();

            // Đợi map load xong rồi vẽ route
            if (mapLibreMap != null && mapLibreMap.getStyle() != null && locationComponent != null) {
                Log.d(TAG, "Map is ready, drawing route now");
                drawRouteToDestination(destLat, destLng, destName);
            } else {
                // Đợi map load
                Log.d(TAG, "Map not ready, waiting 3 seconds...");
                mapView.postDelayed(() -> {
                    if (mapLibreMap != null && mapLibreMap.getStyle() != null) {
                        Log.d(TAG, "Map ready after delay, drawing route");
                        drawRouteToDestination(destLat, destLng, destName);
                    } else {
                        Log.e(TAG, "Map still not ready after delay!");
                        Toast.makeText(this, "Bản đồ chưa sẵn sàng, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                    }
                }, 3000);
            }
        } else {
            Log.d(TAG, "No routing intent extras found");
        }
    }

    private void onSearchBarClicked() {
        Intent intent = new Intent(this, SearchParkingActivity.class);
        intent.putExtra(SearchParkingActivity.EXTRA_FROM_ACTIVITY, "HomeActivity");
        startActivity(intent);
    }

    private void initMapView(Bundle savedInstanceState) {
        mapView = findViewById(R.id.mapView);
        fabMyLocation = findViewById(R.id.fabMyLocation);

        if (mapView == null) return;

        // Khởi tạo MapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(map -> {
            mapLibreMap = map;

            // Load style từ OpenMap.vn
            String styleUrl = "https://maptiles.openmap.vn/styles/day-v1/style.json?apikey=" +
                    com.parkmate.android.BuildConfig.OPENMAP_API_KEY.replace("\"", "");

            map.setStyle(styleUrl, style -> onStyleLoaded(style));
        });

        // My location button
        if (fabMyLocation != null) {
            fabMyLocation.setOnClickListener(v -> {
                if (checkLocationPermission()) {
                    enableLocationComponent();
                    moveToCurrentLocation();
                } else {
                    requestLocationPermission();
                }
            });
        }
    }

    private void onStyleLoaded(@NonNull Style style) {
        Log.d(TAG, "╔════════════════════════════════════════╗");
        Log.d(TAG, "║   MAP STYLE LOADED SUCCESSFULLY       ║");
        Log.d(TAG, "╚════════════════════════════════════════╝");
        Toast.makeText(this, "Bản đồ đã tải xong", Toast.LENGTH_SHORT).show();

        // Set marker click listener
        if (mapLibreMap != null) {
            mapLibreMap.setOnMarkerClickListener(marker -> {
                onMarkerClick(marker);
                return true;
            });
            Log.d(TAG, "✓ Marker click listener set");
        }

        if (!isGPSEnabled()) {
            showGPSEnableDialog();
            return;
        }

        if (checkLocationPermission()) {
            enableLocationComponent();
            mapView.postDelayed(() -> {
                moveToCurrentLocation();
                // Load bãi xe sau khi có vị trí
                loadNearbyParkingLots();
            }, 1500);
        }
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null &&
               (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    private void showGPSEnableDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Bật GPS")
                .setMessage("Để xem vị trí hiện tại trên bản đồ, vui lòng bật GPS/Định vị.")
                .setPositiveButton("Cài đặt", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Hủy", (dialog, which) ->
                    Toast.makeText(this, "Không thể hiển thị vị trí hiện tại nếu chưa bật GPS", Toast.LENGTH_LONG).show())
                .setCancelable(false)
                .show();
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @SuppressWarnings("MissingPermission")
    private void enableLocationComponent() {
        if (mapLibreMap == null) return;

        Style style = mapLibreMap.getStyle();
        if (style == null) return;

        try {
            locationComponent = mapLibreMap.getLocationComponent();

            if (!locationComponent.isLocationComponentActivated()) {
                LocationComponentActivationOptions options =
                        LocationComponentActivationOptions.builder(this, style)
                                .useDefaultLocationEngine(true)
                                .build();
                locationComponent.activateLocationComponent(options);
            }

            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.NONE);
            locationComponent.setRenderMode(RenderMode.COMPASS);

            setupLocationCallback();
        } catch (Exception e) {
            Log.e(TAG, "Error enabling location component: " + e.getMessage());
        }
    }

    @SuppressWarnings("MissingPermission")
    private void setupLocationCallback() {
        if (locationComponent == null) return;

        LocationEngine locationEngine = locationComponent.getLocationEngine();
        if (locationEngine == null) return;

        LocationEngineRequest request = new LocationEngineRequest.Builder(LOCATION_UPDATE_INTERVAL)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(LOCATION_UPDATE_FASTEST_INTERVAL)
                .build();

        locationCallback = new LocationEngineCallback<LocationEngineResult>() {
            @Override
            public void onSuccess(LocationEngineResult result) {
                Location location = result.getLastLocation();
                if (location != null && isFirstLocationUpdate) {
                    isFirstLocationUpdate = false;
                    animateCameraToLocation(location, 16);
                }
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG, "Location callback failed: " + exception.getMessage());
            }
        };

        locationEngine.requestLocationUpdates(request, locationCallback, getMainLooper());
    }

    @SuppressWarnings("MissingPermission")
    private void moveToCurrentLocation() {
        if (mapLibreMap == null || locationComponent == null) return;

        if (!locationComponent.isLocationComponentActivated()) {
            enableLocationComponent();
            mapView.postDelayed(this::moveToCurrentLocation, 2000);
            return;
        }

        LocationEngine locationEngine = locationComponent.getLocationEngine();
        if (locationEngine != null) {
            locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
                @Override
                public void onSuccess(LocationEngineResult result) {
                    Location location = result.getLastLocation();
                    if (location != null) {
                        animateCameraToLocation(location, 17);
                        Toast.makeText(HomeActivity.this, "Đã tìm thấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(HomeActivity.this, "Không thể lấy vị trí", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void animateCameraToLocation(Location location, double zoom) {
        if (mapLibreMap == null || location == null) return;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition position = new CameraPosition.Builder()
                .target(latLng)
                .zoom(zoom)
                .build();

        mapLibreMap.animateCamera(
                org.maplibre.android.camera.CameraUpdateFactory.newCameraPosition(position),
                1500
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!isGPSEnabled()) {
                    showGPSEnableDialog();
                } else {
                    enableLocationComponent();
                    mapView.postDelayed(this::moveToCurrentLocation, 1000);
                }
            } else {
                Toast.makeText(this, "Cần quyền truy cập vị trí để sử dụng tính năng này", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();

        // Refresh notification badge khi user quay lại màn hình
        loadNotificationBadgeCount();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mapView != null) mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }

    // ============= ROUTING METHODS =============

    /**
     * Vẽ route từ vị trí hiện tại đến đích
     */
    @SuppressWarnings("MissingPermission")
    private void drawRouteToDestination(double destLat, double destLng, String destName) {
        Log.d(TAG, "╔════════════════════════════════════════╗");
        Log.d(TAG, "║   DRAW ROUTE TO DESTINATION           ║");
        Log.d(TAG, "║   Name: " + destName);
        Log.d(TAG, "║   Dest: [" + destLat + ", " + destLng + "]");
        Log.d(TAG, "╚════════════════════════════════════════╝");

        if (mapLibreMap == null) {
            Log.e(TAG, "ERROR: mapLibreMap is null");
            Toast.makeText(this, "Bản đồ chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (locationComponent == null) {
            Log.e(TAG, "ERROR: locationComponent is null");
            Toast.makeText(this, "Location component chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Map and location component OK");

        // Lấy vị trí hiện tại
        Location currentLocation = locationComponent.getLastKnownLocation();
        if (currentLocation == null) {
            Log.e(TAG, "ERROR: Current location is null");
            Toast.makeText(this, "Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
            // Chỉ zoom đến đích thôi
            animateCameraToLocation(destLat, destLng, 16);
            return;
        }

        double originLat = currentLocation.getLatitude();
        double originLng = currentLocation.getLongitude();

        Log.d(TAG, "Current location: [" + originLat + ", " + originLng + "]");
        Log.d(TAG, "Drawing route from current location to destination");

        // Xóa route cũ nếu có
        clearRoute();

        // Gọi OSRM API để lấy route
        String coordinates = originLng + "," + originLat + ";" + destLng + "," + destLat;

        compositeDisposable.add(
            osrmApiService.getRoute(coordinates, "full", "polyline", true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> handleRouteResponse(response, destLat, destLng, destName),
                    error -> handleRouteError(error, destLat, destLng)
                )
        );
    }

    /**
     * Xử lý response từ OSRM API
     */
    private void handleRouteResponse(RouteResponse response, double destLat, double destLng, String destName) {
        if (!"Ok".equals(response.getCode()) || response.getRoutes() == null || response.getRoutes().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy đường đi", Toast.LENGTH_SHORT).show();
            animateCameraToLocation(destLat, destLng, 16);
            return;
        }

        RouteResponse.Route route = response.getRoutes().get(0);
        String polylineEncoded = route.getGeometry();

        if (polylineEncoded == null || polylineEncoded.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu đường đi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Decode polyline
        List<LatLng> routePoints = PolylineUtils.decode(polylineEncoded);

        Log.d(TAG, "Route decoded: " + routePoints.size() + " points");

        // Vẽ route lên map
        drawRouteOnMap(routePoints);

        // Thêm marker đích
        addDestinationMarker(destLat, destLng, destName);

        // Zoom camera để thấy toàn bộ route
        fitCameraToRoute(routePoints);
    }

    /**
     * Xử lý lỗi khi call OSRM API
     */
    private void handleRouteError(Throwable error, double destLat, double destLng) {
        Log.e(TAG, "Error getting route", error);
        Toast.makeText(this, "Lỗi khi tìm đường: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        // Vẫn zoom đến đích
        animateCameraToLocation(destLat, destLng, 16);
    }

    /**
     * Vẽ polyline route lên map
     */
    private void drawRouteOnMap(List<LatLng> points) {
        if (mapLibreMap == null || points == null || points.isEmpty()) {
            return;
        }

        // Tạo polyline options với màu primary
        int routeColor = ContextCompat.getColor(this, R.color.primary);

        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(points)
                .color(routeColor)
                .width(8f);

        // Thêm polyline vào map
        currentRoutePolyline = mapLibreMap.addPolyline(polylineOptions);

        Log.d(TAG, "Route polyline added to map");
    }

    /**
     * Thêm marker đích
     */
    private void addDestinationMarker(double lat, double lng, String name) {
        if (mapLibreMap == null) return;

        // Tạo icon màu đỏ cho đích đến
        Icon destinationIcon = createDestinationMarkerIcon();

        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(lat, lng))
                .title(name != null ? name : "Điểm đến")
                .snippet("Bãi xe đỗ")
                .icon(destinationIcon);

        destinationMarker = mapLibreMap.addMarker(markerOptions);

        Log.d(TAG, "Destination marker added");
    }

    /**
     * Tạo icon chữ P màu đỏ cho marker đích đến
     */
    private Icon createDestinationMarkerIcon() {
        int redColor = ContextCompat.getColor(this, android.R.color.holo_red_dark);
        int whiteColor = ContextCompat.getColor(this, R.color.white);

        // Tạo bitmap với kích thước tương tự marker thường
        int width = 120;
        int height = 120; // Vuông để chứa chữ P
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        float centerX = width / 2f;
        float centerY = height / 2f;
        float circleRadius = 50f; // Bán kính hình tròn

        // Paint cho hình tròn chính
        Paint circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(redColor);
        circlePaint.setStyle(Paint.Style.FILL);

        // Paint cho viền trắng
        Paint strokePaint = new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setColor(whiteColor);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(8);

        // Vẽ hình tròn với viền trắng
        canvas.drawCircle(centerX, centerY, circleRadius + 4, strokePaint);
        canvas.drawCircle(centerX, centerY, circleRadius, circlePaint);

        // Paint cho chữ P
        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(whiteColor);
        textPaint.setTextSize(70f); // Kích thước chữ lớn
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));

        // Vẽ chữ P ở giữa
        float textY = centerY - ((textPaint.descent() + textPaint.ascent()) / 2);
        canvas.drawText("P", centerX, textY, textPaint);

        IconFactory iconFactory = IconFactory.getInstance(this);
        return iconFactory.fromBitmap(bitmap);
    }

    /**
     * Zoom camera để thấy toàn bộ route
     */
    private void fitCameraToRoute(List<LatLng> points) {
        if (mapLibreMap == null || points == null || points.isEmpty()) {
            return;
        }

        // Tạo LatLngBounds từ các điểm
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng point : points) {
            boundsBuilder.include(point);
        }
        LatLngBounds bounds = boundsBuilder.build();

        // Animate camera với padding
        int padding = 100; // pixels
        mapLibreMap.animateCamera(
            org.maplibre.android.camera.CameraUpdateFactory.newLatLngBounds(bounds, padding),
            2000
        );

        Log.d(TAG, "Camera fitted to route bounds");
    }

    /**
     * Xóa route hiện tại
     */
    private void clearRoute() {
        if (currentRoutePolyline != null && mapLibreMap != null) {
            mapLibreMap.removePolyline(currentRoutePolyline);
            currentRoutePolyline = null;
            Log.d(TAG, "Route cleared");
        }

        if (destinationMarker != null && mapLibreMap != null) {
            mapLibreMap.removeMarker(destinationMarker);
            destinationMarker = null;
            Log.d(TAG, "Destination marker cleared");
        }
    }

    /**
     * Animate camera đến location cụ thể
     */
    private void animateCameraToLocation(double lat, double lng, double zoom) {
        if (mapLibreMap == null) return;

        LatLng latLng = new LatLng(lat, lng);
        CameraPosition position = new CameraPosition.Builder()
                .target(latLng)
                .zoom(zoom)
                .build();

        mapLibreMap.animateCamera(
                org.maplibre.android.camera.CameraUpdateFactory.newCameraPosition(position),
                1500
        );
    }

    // ============= END ROUTING METHODS =============

    @Override
    protected void onDestroy() {
        super.onDestroy();

        compositeDisposable.dispose();

        if (locationComponent != null && locationCallback != null) {
            LocationEngine locationEngine = locationComponent.getLocationEngine();
            if (locationEngine != null) {
                locationEngine.removeLocationUpdates(locationCallback);
            }
        }

        if (mapView != null) mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    // ============= PARKING LOTS METHODS =============

    // Load bãi xe xung quanh vị trí hiện tại
    @SuppressWarnings("MissingPermission")
    private void loadNearbyParkingLots() {
        Log.d(TAG, "=== loadNearbyParkingLots called ===");

        if (locationComponent == null || !locationComponent.isLocationComponentActivated()) {
            Log.w(TAG, "Location component not ready, cannot load nearby parking lots");
            return;
        }

        Location currentLocation = locationComponent.getLastKnownLocation();
        if (currentLocation == null) {
            Log.w(TAG, "Current location is null, waiting...");
            mapView.postDelayed(this::loadNearbyParkingLots, 2000);
            return;
        }

        double userLat = currentLocation.getLatitude();
        double userLng = currentLocation.getLongitude();
        Log.d(TAG, "Loading parking lots near: [" + userLat + ", " + userLng + "]");

        compositeDisposable.add(
            apiService.getParkingLots(
                null,  // ownedByMe
                null,  // name
                null,  // city
                null,  // is24Hour
                "ACTIVE",  // status
                0,     // page
                100,   // size
                "name", // sortBy
                "ASC"   // sortOrder
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> handleParkingLotsSuccess(response, userLat, userLng),
                this::handleParkingLotsError
            )
        );
    }

    private void handleParkingLotsSuccess(ParkingLotResponse response, double userLat, double userLng) {
        Log.d(TAG, "API Response - Success: " + response.isSuccess());
        Log.d(TAG, "API Response - Message: " + response.getMessage());

        if (response.isSuccess() && response.getData() != null) {
            List<ParkingLotResponse.ParkingLot> allParkingLots = response.getData().getContent();
            Log.d(TAG, "Received " + allParkingLots.size() + " parking lots from API");

            // Lọc chỉ lấy bãi xe trong bán kính 50km
            double radiusKm = 50.0;
            List<ParkingLotResponse.ParkingLot> nearbyLots = new java.util.ArrayList<>();

            for (ParkingLotResponse.ParkingLot lot : allParkingLots) {
                if (lot.getLatitude() != null && lot.getLongitude() != null) {
                    double distance = calculateDistance(
                        userLat, userLng,
                        lot.getLatitude(), lot.getLongitude()
                    );

                    Log.d(TAG, lot.getName() + " - Distance: " + String.format("%.2f", distance) + " km");

                    if (distance <= radiusKm) {
                        nearbyLots.add(lot);
                    }
                }
            }

            Log.d(TAG, "Found " + nearbyLots.size() + " parking lots within " + radiusKm + " km");

            if (nearbyLots.isEmpty()) {
                Toast.makeText(this, "Không có bãi xe nào gần bạn (trong bán kính " + radiusKm + "km)", Toast.LENGTH_LONG).show();
                // Vẫn hiển thị tất cả để user thấy
                displayParkingLotsOnMap(allParkingLots);
            } else {
                displayParkingLotsOnMap(nearbyLots);
                Toast.makeText(this, "Tìm thấy " + nearbyLots.size() + " bãi xe gần bạn", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.w(TAG, "No parking lots data in response");
            Toast.makeText(this, "Không có bãi xe nào", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleParkingLotsError(Throwable throwable) {
        Log.e(TAG, "Error loading parking lots", throwable);
        Toast.makeText(this, "Lỗi tải bãi xe: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
    }

    // Tính khoảng cách giữa 2 điểm (Haversine formula)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // Bán kính trái đất (km)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    // Hiển thị các bãi xe lên map
    private void displayParkingLotsOnMap(List<ParkingLotResponse.ParkingLot> parkingLots) {
        if (mapLibreMap == null) {
            Log.e(TAG, "mapLibreMap is null, cannot display markers");
            return;
        }

        if (parkingLots == null || parkingLots.isEmpty()) {
            Log.w(TAG, "No parking lots to display");
            return;
        }

        Log.d(TAG, "Displaying " + parkingLots.size() + " parking lots on map");

        // Tạo icon màu xanh primary cho marker
        Icon customIcon = createPrimaryColorMarkerIcon();

        int markersAdded = 0;

        for (ParkingLotResponse.ParkingLot lot : parkingLots) {
            // Kiểm tra có tọa độ không
            if (lot.getLatitude() == null || lot.getLongitude() == null) {
                Log.w(TAG, "Parking lot " + lot.getName() + " has no coordinates - SKIPPING");
                continue;
            }

            double lat = lot.getLatitude();
            double lng = lot.getLongitude();

            Log.d(TAG, "Adding marker: " + lot.getName() + " at [" + lat + ", " + lng + "]");

            // Tạo snippet với thông tin cơ bản
            String snippet = lot.getFullAddress() + "\n" + lot.getOperatingHours();

            // Tạo marker
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(lat, lng))
                    .title(lot.getName())
                    .snippet(snippet)
                    .icon(customIcon);

            Marker marker = mapLibreMap.addMarker(markerOptions);

            // Lưu mapping giữa marker ID và parking lot ID
            if (lot.getId() != null && marker != null) {
                markerToParkingLotMap.put(marker.getId(), lot.getId());
                Log.d(TAG, "Saved mapping: Marker ID " + marker.getId() + " -> Parking Lot ID " + lot.getId());
            }

            markersAdded++;
            Log.d(TAG, "✓ Marker added successfully for: " + lot.getName());
        }

        Log.d(TAG, "Total markers added: " + markersAdded);

        if (markersAdded == 0) {
            Log.w(TAG, "No markers were added to the map");
        }
    }

    // Tạo icon marker chữ P màu xanh primary
    private Icon createPrimaryColorMarkerIcon() {
        // Lấy màu primary từ resources
        int primaryColor = ContextCompat.getColor(this, R.color.primary);
        int whiteColor = ContextCompat.getColor(this, R.color.white);

        // Tạo bitmap với kích thước tương tự pin cũ
        int width = 120;
        int height = 120; // Vuông để chứa chữ P
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        float centerX = width / 2f;
        float centerY = height / 2f;
        float circleRadius = 50f; // Bán kính hình tròn

        // Paint cho hình tròn chính
        Paint circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(primaryColor);
        circlePaint.setStyle(Paint.Style.FILL);

        // Paint cho viền trắng
        Paint strokePaint = new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setColor(whiteColor);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(8);

        // Vẽ hình tròn với viền trắng
        canvas.drawCircle(centerX, centerY, circleRadius + 4, strokePaint);
        canvas.drawCircle(centerX, centerY, circleRadius, circlePaint);

        // Paint cho chữ P
        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(whiteColor);
        textPaint.setTextSize(70f); // Kích thước chữ lớn
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));

        // Vẽ chữ P ở giữa
        float textY = centerY - ((textPaint.descent() + textPaint.ascent()) / 2);
        canvas.drawText("P", centerX, textY, textPaint);

        // Tạo icon từ bitmap
        IconFactory iconFactory = IconFactory.getInstance(this);
        return iconFactory.fromBitmap(bitmap);
    }

    // Xử lý khi click vào marker
    private void onMarkerClick(Marker marker) {
        String title = marker.getTitle();
        long markerId = marker.getId();

        Log.d(TAG, "Clicked marker: " + title + " (Marker ID: " + markerId + ")");

        // Lấy parking lot ID từ HashMap
        Long parkingLotId = markerToParkingLotMap.get(markerId);

        if (parkingLotId != null) {
            Log.d(TAG, "Found Parking Lot ID: " + parkingLotId);
            // Mở ParkingLotDetailActivity
            openParkingLotDetail(parkingLotId, title);
        } else {
            Log.e(TAG, "Cannot find parking lot ID for marker ID: " + markerId);
            Toast.makeText(this, "Không thể mở chi tiết bãi xe", Toast.LENGTH_SHORT).show();
        }
    }

    // Mở trang chi tiết bãi xe
    private void openParkingLotDetail(long parkingLotId, String parkingLotName) {
        Intent intent = new Intent(this, ParkingLotDetailActivity.class);
        intent.putExtra("parking_lot_id", parkingLotId);
        intent.putExtra("parking_lot_name", parkingLotName);
        startActivity(intent);
    }

    // ============= FIREBASE NOTIFICATION METHODS =============

    /**
     * Đăng ký FCM token với server
     */
    private void registerFcmToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();
                    Log.d(TAG, "FCM Token: " + token);

                    // Send token to server
                    sendTokenToServer(token);
                });
    }

    /**
     * Gửi FCM token lên server để đăng ký device
     * Backend sẽ check ownedByMe=true và lấy userId từ JWT token
     */
    private void sendTokenToServer(String token) {
        try {
            String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            String deviceName = Build.MANUFACTURER + " " + Build.MODEL;
            String deviceOs = "ANDROID";

            Log.d(TAG, "Registering device - DeviceId: " + deviceId + ", DeviceName: " + deviceName + ", Token: " + token.substring(0, 20) + "...");

            // Backend sẽ tự lấy userId từ JWT token khi check ownedByMe=true
            CreateMobileDeviceRequest request = new CreateMobileDeviceRequest(
                    deviceId,
                    deviceName,
                    deviceOs,
                    token
            );

            compositeDisposable.add(
                    apiService.registerMobileDevice(request)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    response -> {
                                        if (response.isSuccess()) {
                                            Log.d(TAG, "✓ Device registered successfully with server");
                                            Log.d(TAG, "✓ Response: " + response.getData());
                                        } else {
                                            Log.e(TAG, "Failed to register device: " + response.getMessage());
                                        }
                                    },
                                    error -> {
                                        Log.e(TAG, "Error registering device: " + error.getMessage(), error);
                                    }
                            )
            );

        } catch (Exception e) {
            Log.e(TAG, "Error sending token to server: " + e.getMessage(), e);
        }
    }

    // ============= NOTIFICATION BADGE METHODS =============

    /**
     * Load số lượng notifications chưa đọc từ SharedPreferences
     */
    private void loadNotificationBadgeCount() {
        try {
            SharedPreferences prefs = getSharedPreferences("parkmate_notifications", Context.MODE_PRIVATE);
            int unreadCount = prefs.getInt("unread_count", 0);
            setupNotificationBadge(unreadCount);
            Log.d(TAG, "✓ Unread notifications from local storage: " + unreadCount);
        } catch (Exception e) {
            Log.e(TAG, "Error getting unread count from SharedPreferences: " + e.getMessage());
            setupNotificationBadge(0);
        }
    }
}
