package com.parkmate.android.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parkmate.android.R;

import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.location.LocationComponent;
import org.maplibre.android.location.LocationComponentActivationOptions;
import org.maplibre.android.location.modes.CameraMode;
import org.maplibre.android.location.modes.RenderMode;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.Style;

public class MapActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private MapView mapView;
    private MapLibreMap mapLibreMap;
    private LocationComponent locationComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo MapLibre
        MapLibre.getInstance(this);

        setContentView(R.layout.activity_map);

        // Setup edge-to-edge display (không apply padding cho map)
        com.parkmate.android.utils.EdgeToEdgeHelper.setupEdgeToEdge(this, false, false);

        // Khởi tạo views
        mapView = findViewById(R.id.mapView);
        FloatingActionButton fabBack = findViewById(R.id.fabBack);
        FloatingActionButton fabMyLocation = findViewById(R.id.fabMyLocation);

        // Khởi tạo MapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(map -> {
            mapLibreMap = map;

            // Load style từ OpenMap.vn với API key
            String styleUrl = "https://maptiles.openmap.vn/styles/day-v1/style.json?apikey=" +
                    com.parkmate.android.BuildConfig.OPENMAP_API_KEY.replace("\"", "");

            map.setStyle(styleUrl, this::onStyleLoaded);

            // Set camera position mặc định (Hà Nội)
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(21.0285, 105.8542))
                    .zoom(13)
                    .build();
            map.setCameraPosition(position);
        });

        // Back button
        fabBack.setOnClickListener(v -> finish());

        // My location button
        fabMyLocation.setOnClickListener(v -> {
            if (checkLocationPermission()) {
                enableLocationComponent();
                moveToCurrentLocation();
            } else {
                requestLocationPermission();
            }
        });

        // Tự động request location permission khi mở map
        if (!checkLocationPermission()) {
            requestLocationPermission();
        }
    }

    private void onStyleLoaded(@NonNull Style style) {
        Toast.makeText(this, "Bản đồ đã tải xong", Toast.LENGTH_SHORT).show();

        // Enable location component nếu có permission
        if (checkLocationPermission()) {
            enableLocationComponent();
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @SuppressWarnings("MissingPermission")
    private void enableLocationComponent() {
        if (mapLibreMap == null) {
            Toast.makeText(this, "Bản đồ chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            return;
        }

        Style style = mapLibreMap.getStyle();
        if (style == null) {
            Toast.makeText(this, "Style chưa được tải", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Activate location component
            locationComponent = mapLibreMap.getLocationComponent();

            // Kiểm tra nếu đã được activate rồi thì không activate lại
            if (!locationComponent.isLocationComponentActivated()) {
                LocationComponentActivationOptions locationComponentActivationOptions =
                        LocationComponentActivationOptions.builder(this, style)
                                .useDefaultLocationEngine(true)
                                .build();

                locationComponent.activateLocationComponent(locationComponentActivationOptions);
            }

            locationComponent.setLocationComponentEnabled(true);
            // Không dùng TRACKING mode vì sẽ conflict với animate camera thủ công
            locationComponent.setCameraMode(CameraMode.NONE);
            locationComponent.setRenderMode(RenderMode.COMPASS);

        } catch (Exception e) {
            Toast.makeText(this, "Không thể bật vị trí: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("MissingPermission")
    private void moveToCurrentLocation() {
        if (mapLibreMap == null) {
            Toast.makeText(this, "Bản đồ chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (locationComponent == null || !locationComponent.isLocationComponentActivated()) {
            enableLocationComponent();

            // Đợi location component được activate xong rồi mới di chuyển
            mapView.postDelayed(this::moveToCurrentLocation, 1000);
            return;
        }

        Location lastLocation = locationComponent.getLastKnownLocation();

        if (lastLocation != null) {
            LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

            CameraPosition position = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(16)
                    .build();

            mapLibreMap.animateCamera(
                    org.maplibre.android.camera.CameraUpdateFactory.newCameraPosition(position),
                    1500
            );

            Toast.makeText(this,
                    "Vị trí: " + lastLocation.getLatitude() + ", " + lastLocation.getLongitude(),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Đợi 2 giây để lấy vị trí
            Toast.makeText(this, "Đang lấy vị trí hiện tại...", Toast.LENGTH_SHORT).show();

            mapView.postDelayed(() -> {
                Location location = locationComponent.getLastKnownLocation();
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraPosition position = new CameraPosition.Builder()
                            .target(latLng)
                            .zoom(16)
                            .build();
                    mapLibreMap.animateCamera(
                            org.maplibre.android.camera.CameraUpdateFactory.newCameraPosition(position),
                            1500
                    );
                    Toast.makeText(this, "Đã tìm thấy vị trí của bạn!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Không thể lấy vị trí. Hãy bật GPS và thử lại.",
                            Toast.LENGTH_LONG).show();
                }
            }, 2000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocationComponent();
                Toast.makeText(this, "Đã cấp quyền truy cập vị trí", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Cần quyền truy cập vị trí để sử dụng tính năng này",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // MapView lifecycle methods
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}

