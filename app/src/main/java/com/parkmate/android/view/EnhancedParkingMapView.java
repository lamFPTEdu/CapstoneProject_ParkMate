package com.parkmate.android.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.parkmate.android.R;
import com.parkmate.android.model.ParkingArea;
import com.parkmate.android.model.ParkingFloor;
import com.parkmate.android.model.ParkingSpot;

import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced Parking Map View với Material Design 3
 * Vẽ 3 layers: Floor → Areas → Spots với shadows, gradients, animations
 */
public class EnhancedParkingMapView extends View {

    private ParkingFloor floor;
    private List<ParkingArea> areas = new ArrayList<>();
    private List<ParkingSpot> spots = new ArrayList<>();
    private ParkingSpot selectedSpot = null;

    // Paints
    private Paint floorPaint, floorBorderPaint, floorShadowPaint;
    private Paint areaPaint, areaBorderPaint, areaShadowPaint;
    private Paint areaTextPaint, areaSubtextPaint;
    private Paint spotPaint, spotTextPaint, spotBorderPaint;
    private Paint selectedBorderPaint, gridPaint;

    private float scaleFactor = 1.0f;
    private float offsetX = 0f, offsetY = 0f;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;

    private ValueAnimator pulseAnimator;
    private float pulseScale = 1f;

    private OnSpotClickListener spotClickListener;
    private static final float METER_TO_PIXEL = 40f;
    private static final float MIN_SCALE = 0.3f;
    private static final float MAX_SCALE = 4f;

    public interface OnSpotClickListener {
        void onSpotClick(ParkingSpot spot);
    }

    public EnhancedParkingMapView(Context context) {
        super(context);
        init(context);
    }

    public EnhancedParkingMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setLayerType(LAYER_TYPE_SOFTWARE, null); // Enable shadow rendering

        // Floor paints - Grey theme (basic hơn tím)
        floorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        floorPaint.setColor(Color.parseColor("#FAFAFA")); // Light grey
        floorPaint.setStyle(Paint.Style.FILL);

        floorBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        floorBorderPaint.setColor(Color.parseColor("#9E9E9E")); // Grey
        floorBorderPaint.setStyle(Paint.Style.STROKE);
        floorBorderPaint.setStrokeWidth(6);

        floorShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        floorShadowPaint.setColor(Color.parseColor("#40000000"));
        floorShadowPaint.setShadowLayer(20, 0, 10, Color.parseColor("#40000000"));

        // Area paints - Blue theme
        areaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        areaPaint.setColor(Color.parseColor("#E3F2FD"));
        areaPaint.setStyle(Paint.Style.FILL);

        areaBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        areaBorderPaint.setColor(Color.parseColor("#2196F3"));
        areaBorderPaint.setStyle(Paint.Style.STROKE);
        areaBorderPaint.setStrokeWidth(4);
        areaBorderPaint.setPathEffect(new DashPathEffect(new float[]{15, 10}, 0));

        areaShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        areaShadowPaint.setColor(Color.parseColor("#30000000"));
        areaShadowPaint.setShadowLayer(10, 0, 5, Color.parseColor("#30000000"));

        // Text paints - Area text mờ hơn
        areaTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        areaTextPaint.setColor(Color.parseColor("#1976D2"));
        areaTextPaint.setTextSize(24); // Giảm từ 36 xuống 24
        areaTextPaint.setAlpha(100); // 40% opacity - mờ hơn nhiều
        areaTextPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL)); // Bỏ bold
        areaTextPaint.setTextAlign(Paint.Align.CENTER);

        areaSubtextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        areaSubtextPaint.setColor(Color.parseColor("#757575"));
        areaSubtextPaint.setTextSize(20); // Giảm từ 28 xuống 20
        areaSubtextPaint.setAlpha(80); // 30% opacity - rất mờ
        areaSubtextPaint.setTextAlign(Paint.Align.CENTER);

        // Spot paints
        spotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        spotPaint.setStyle(Paint.Style.FILL);
        spotPaint.setShadowLayer(8, 0, 4, Color.parseColor("#40000000"));

        spotTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        spotTextPaint.setColor(Color.WHITE);
        spotTextPaint.setTextSize(20); // Giảm xuống 20 để vừa với spot
        spotTextPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        spotTextPaint.setTextAlign(Paint.Align.CENTER);
        spotTextPaint.setShadowLayer(4, 0, 2, Color.parseColor("#80000000")); // Thêm shadow cho chữ

        spotBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        spotBorderPaint.setStyle(Paint.Style.STROKE);
        spotBorderPaint.setStrokeWidth(2);
        spotBorderPaint.setColor(Color.parseColor("#40FFFFFF"));

        selectedBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedBorderPaint.setStyle(Paint.Style.STROKE);
        selectedBorderPaint.setStrokeWidth(6);
        selectedBorderPaint.setColor(ContextCompat.getColor(context, R.color.primary));
        selectedBorderPaint.setShadowLayer(15, 0, 0, ContextCompat.getColor(context, R.color.primary));

        // Grid paint
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#10000000"));
        gridPaint.setStrokeWidth(1);
        gridPaint.setStyle(Paint.Style.STROKE);

        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());

        setupPulseAnimation();
    }

    private void setupPulseAnimation() {
        pulseAnimator = ValueAnimator.ofFloat(1f, 1.15f);
        pulseAnimator.setDuration(800);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.addUpdateListener(animation -> {
            pulseScale = (float) animation.getAnimatedValue();
            invalidate();
        });
        pulseAnimator.start();
    }

    public void setMapData(ParkingFloor floor, List<ParkingArea> areas, List<ParkingSpot> spots) {
        this.floor = floor;
        this.areas = areas != null ? areas : new ArrayList<>();
        this.spots = spots != null ? spots : new ArrayList<>();
        this.selectedSpot = null;
        offsetX = 0;
        offsetY = 0;
        scaleFactor = 1.0f;
        post(this::autoCenter);
        invalidate();
    }

    private void autoCenter() {
        if (floor == null || getWidth() == 0 || getHeight() == 0) return;

        // Dùng kích thước THỰC TẾ từ API (đã là pixel)
        float mapWidth = (float) floor.getFloorWidth();
        float mapHeight = (float) floor.getFloorHeight();
        float viewWidth = getWidth();
        float viewHeight = getHeight();

        // Scale để fit vào màn hình với padding 15%
        scaleFactor = Math.min(viewWidth / mapWidth, viewHeight / mapHeight) * 0.85f;
        scaleFactor = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scaleFactor));

        // Center map trong view
        offsetX = (viewWidth - mapWidth * scaleFactor) / 2;
        offsetY = (viewHeight - mapHeight * scaleFactor) / 2;

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (floor == null) return;

        canvas.save();
        canvas.translate(offsetX, offsetY);
        canvas.scale(scaleFactor, scaleFactor);

        drawGrid(canvas);
        drawFloor(canvas);

        for (ParkingArea area : areas) {
            drawArea(canvas, area);
        }

        for (ParkingSpot spot : spots) {
            drawSpot(canvas, spot);
        }

        canvas.restore();
    }

    private void drawGrid(Canvas canvas) {
        if (floor == null) return;

        // Dùng kích thước THỰC TẾ từ API (đã là pixel)
        float width = (float) floor.getFloorWidth();
        float height = (float) floor.getFloorHeight();
        float gridSize = 100;

        for (float x = 0; x <= width; x += gridSize) {
            canvas.drawLine(x, 0, x, height, gridPaint);
        }

        for (float y = 0; y <= height; y += gridSize) {
            canvas.drawLine(0, y, width, y, gridPaint);
        }
    }

    private void drawFloor(Canvas canvas) {
        // Dùng tọa độ THỰC TẾ từ API (đã là pixel)
        float x = (float) floor.getFloorTopLeftX();
        float y = (float) floor.getFloorTopLeftY();
        float width = (float) floor.getFloorWidth();
        float height = (float) floor.getFloorHeight();

        RectF rect = new RectF(x, y, x + width, y + height);

        // Shadow
        RectF shadowRect = new RectF(rect);
        shadowRect.offset(5, 5);
        canvas.drawRoundRect(shadowRect, 20, 20, floorShadowPaint);

        // Floor
        canvas.drawRoundRect(rect, 20, 20, floorPaint);
        canvas.drawRoundRect(rect, 20, 20, floorBorderPaint);

        // Bỏ header bar - chỉ vẽ text mờ ở góc trên như watermark
        Paint floorTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        floorTextPaint.setColor(Color.parseColor("#757575"));
        floorTextPaint.setTextSize(20); // Nhỏ như area subtext
        floorTextPaint.setAlpha(80); // 30% opacity - rất mờ như area subtext
        floorTextPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        canvas.drawText(floor.getFloorName(), rect.left + 20, rect.top + 35, floorTextPaint);
    }

    private void drawArea(Canvas canvas, ParkingArea area) {
        // Area coordinates là ABSOLUTE (không phải relative to Floor!)
        float x = (float) area.getAreaTopLeftX();
        float y = (float) area.getAreaTopLeftY();
        float width = (float) area.getAreaWidth();
        float height = (float) area.getAreaHeight();

        RectF rect = new RectF(x, y, x + width, y + height);

        // Shadow
        RectF shadowRect = new RectF(rect);
        shadowRect.offset(4, 4);
        canvas.drawRoundRect(shadowRect, 15, 15, areaShadowPaint);

        // Area background
        canvas.drawRoundRect(rect, 15, 15, areaPaint);
        canvas.drawRoundRect(rect, 15, 15, areaBorderPaint);

        // Vẽ text mờ ở giữa area - không có card
        float centerX = rect.centerX();
        float centerY = rect.centerY();

        // Area name - mờ, nhỏ, ở giữa
        canvas.drawText(area.getName(), centerX, centerY - 15, areaTextPaint);

        // Đếm spots thuộc area này (dựa vào areaId)
        int totalSpots = 0;
        int availableSpots = 0;
        for (ParkingSpot s : spots) {
            if (s.getAreaId() != null && s.getAreaId() == area.getId()) {
                totalSpots++;
                if (s.isAvailableForSubscription()) {
                    availableSpots++;
                }
            }
        }

        // Available spots info - rất mờ, ở dưới tên
        String info = availableSpots + "/" + totalSpots + " chỗ";
        canvas.drawText(info, centerX, centerY + 15, areaSubtextPaint);
    }

    private void drawSpot(Canvas canvas, ParkingSpot spot) {
        // Spot coordinates là RELATIVE to Area
        // Area coordinates là ABSOLUTE
        // Nên: Spot absolute = Area absolute + Spot relative

        // Tìm area mà spot này thuộc về (dựa vào areaId)
        float areaOffsetX = 0;
        float areaOffsetY = 0;

        if (spot.getAreaId() != null) {
            for (ParkingArea area : areas) {
                if (area.getId() == spot.getAreaId()) {
                    areaOffsetX = (float) area.getAreaTopLeftX();  // Area ABSOLUTE position
                    areaOffsetY = (float) area.getAreaTopLeftY();
                    break;
                }
            }
        }

        float x = areaOffsetX + (float) spot.getSpotTopLeftX();
        float y = areaOffsetY + (float) spot.getSpotTopLeftY();
        float width = (float) spot.getSpotWidth();
        float height = (float) spot.getSpotHeight();

        RectF rect = new RectF(x, y, x + width, y + height);

        // Determine color với màu sắc đậm hơn
        if (spot.isAvailableForSubscription()) {
            spotPaint.setColor(Color.parseColor("#66BB6A")); // Green đậm hơn
        } else {
            String reason = spot.getSubscriptionUnavailabilityReason();
            if (reason != null) {
                switch (reason) {
                    case "ALREADY_ASSIGNED":
                        spotPaint.setColor(Color.parseColor("#EF5350")); // Red đậm hơn
                        break;
                    case "SPOT_HELD":
                        spotPaint.setColor(Color.parseColor("#FFA726")); // Orange đậm hơn
                        break;
                    default:
                        spotPaint.setColor(Color.parseColor("#BDBDBD")); // Grey đậm hơn
                }
            } else {
                spotPaint.setColor(Color.parseColor("#BDBDBD"));
            }
        }

        // Pulse animation for selected
        boolean isSelected = selectedSpot != null && selectedSpot.getId() == spot.getId();
        if (isSelected) {
            canvas.save();
            canvas.scale(pulseScale, pulseScale, rect.centerX(), rect.centerY());
        }

        // Draw spot với corner radius lớn hơn
        canvas.drawRoundRect(rect, 12, 12, spotPaint);
        canvas.drawRoundRect(rect, 12, 12, spotBorderPaint);

        // Vẽ tên ngắn của spot (chỉ phần sau dấu "-", ví dụ: "S2")
        String shortName = getShortSpotName(spot.getName());
        float textX = rect.centerX();
        float textY = rect.centerY() + (spotTextPaint.getTextSize() / 3); // Center vertically
        canvas.drawText(shortName, textX, textY, spotTextPaint);

        // Selection border với glow effect
        if (isSelected) {
            canvas.drawRoundRect(rect, 12, 12, selectedBorderPaint);
            canvas.restore();
        }
    }

    /**
     * Lấy tên ngắn của spot (phần sau dấu gạch ngang)
     * Ví dụ: "Area 1-S2" -> "S2"
     */
    private String getShortSpotName(String fullName) {
        if (fullName != null && fullName.contains("-")) {
            String[] parts = fullName.split("-");
            return parts[parts.length - 1]; // Lấy phần cuối cùng sau dấu "-"
        }
        return fullName; // Trả về tên đầy đủ nếu không có dấu "-"
    }

    private void drawCarIcon(Canvas canvas, RectF rect) {
        // Car icon với màu trắng rõ ràng hơn
        Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        iconPaint.setColor(Color.parseColor("#FFFFFF"));
        iconPaint.setAlpha(180); // 70% opacity cho dễ nhìn

        // Vẽ icon xe hơi lớn hơn và đẹp hơn
        float carWidth = rect.width() * 0.65f; // Tăng từ 0.5 lên 0.65
        float carHeight = rect.height() * 0.55f; // Tăng từ 0.4 lên 0.55
        float carLeft = rect.centerX() - carWidth / 2;
        float carTop = rect.top + 8;

        // Car body (thân xe)
        RectF carBody = new RectF(
            carLeft,
            carTop + carHeight * 0.35f,
            carLeft + carWidth,
            carTop + carHeight
        );
        canvas.drawRoundRect(carBody, 8, 8, iconPaint);

        // Car roof (mui xe)
        RectF carRoof = new RectF(
            carLeft + carWidth * 0.15f,
            carTop,
            carLeft + carWidth * 0.85f,
            carTop + carHeight * 0.5f
        );
        canvas.drawRoundRect(carRoof, 8, 8, iconPaint);

        // Wheels (bánh xe) - thêm chi tiết
        Paint wheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wheelPaint.setColor(Color.parseColor("#FFFFFF"));
        wheelPaint.setAlpha(200);

        float wheelRadius = carHeight * 0.12f;
        float wheelY = carBody.bottom - wheelRadius * 0.5f;

        // Left wheel
        canvas.drawCircle(carLeft + carWidth * 0.25f, wheelY, wheelRadius, wheelPaint);
        // Right wheel
        canvas.drawCircle(carLeft + carWidth * 0.75f, wheelY, wheelRadius, wheelPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_UP) {
            handleSpotClick(event.getX(), event.getY());
        }

        return true;
    }

    private void handleSpotClick(float touchX, float touchY) {
        float mapX = (touchX - offsetX) / scaleFactor;
        float mapY = (touchY - offsetY) / scaleFactor;

        // Area coordinates là ABSOLUTE (không phải relative to Floor!)
        // Spot coordinates là relative to Area
        // Nên: Spot absolute = Area absolute + Spot relative
        float areaOffsetX = 0;
        float areaOffsetY = 0;
        if (!areas.isEmpty()) {
            ParkingArea firstArea = areas.get(0);
            areaOffsetX = (float) firstArea.getAreaTopLeftX();  // Area ABSOLUTE position
            areaOffsetY = (float) firstArea.getAreaTopLeftY();
        }

        for (ParkingSpot spot : spots) {
            float x = areaOffsetX + (float) spot.getSpotTopLeftX();
            float y = areaOffsetY + (float) spot.getSpotTopLeftY();
            float width = (float) spot.getSpotWidth();
            float height = (float) spot.getSpotHeight();

            if (mapX >= x && mapX <= x + width && mapY >= y && mapY <= y + height) {
                if (spot.isAvailableForSubscription() && spotClickListener != null) {
                    selectedSpot = spot;
                    spotClickListener.onSpotClick(spot);
                    invalidate();
                }
                break;
            }
        }
    }

    public void setOnSpotClickListener(OnSpotClickListener listener) {
        this.spotClickListener = listener;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float oldScale = scaleFactor;
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(MIN_SCALE, Math.min(scaleFactor, MAX_SCALE));

            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();

            offsetX = focusX - (focusX - offsetX) * (scaleFactor / oldScale);
            offsetY = focusY - (focusY - offsetY) * (scaleFactor / oldScale);

            invalidate();
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            offsetX -= distanceX;
            offsetY -= distanceY;
            invalidate();
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
        }
    }
}

