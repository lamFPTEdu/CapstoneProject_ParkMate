package com.parkmate.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parkmate.android.R;
import com.parkmate.android.model.response.ParkingLotDetailResponse;

import java.util.ArrayList;
import java.util.List;

public class ParkingImageAdapter extends RecyclerView.Adapter<ParkingImageAdapter.ViewHolder> {

    private final List<ParkingLotDetailResponse.ImageData> images = new ArrayList<>();

    public void submitList(List<ParkingLotDetailResponse.ImageData> newImages) {
        images.clear();
        if (newImages != null) {
            // Chỉ thêm ảnh active
            for (ParkingLotDetailResponse.ImageData img : newImages) {
                if (img.getIsActive() != null && img.getIsActive()) {
                    images.add(img);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parking_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (images.isEmpty()) {
            // Show placeholder when no images
            holder.bind(null);
        } else {
            holder.bind(images.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return images.isEmpty() ? 1 : images.size(); // At least 1 for placeholder
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivParkingImage);
        }

        public void bind(ParkingLotDetailResponse.ImageData image) {
            if (image != null && image.getPath() != null && !image.getPath().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(image.getPath())
                        .centerCrop()
                        .placeholder(R.drawable.ic_parking_placeholder)
                        .error(R.drawable.ic_parking_placeholder)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.ic_parking_placeholder);
            }
        }
    }
}

