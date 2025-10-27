package com.parkmate.android.model.response;

import com.google.gson.annotations.SerializedName;
import com.parkmate.android.model.Vehicle;

import java.util.List;

public class VehicleResponse {
    @SerializedName("content")
    private List<Vehicle> content;

    @SerializedName("totalElements")
    private int totalElements;

    @SerializedName("totalPages")
    private int totalPages;

    @SerializedName("size")
    private int size;

    @SerializedName("number")
    private int number;

    @SerializedName("first")
    private boolean first;

    @SerializedName("last")
    private boolean last;

    @SerializedName("empty")
    private boolean empty;

    public VehicleResponse() {
    }

    public List<Vehicle> getContent() {
        return content;
    }

    public void setContent(List<Vehicle> content) {
        this.content = content;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }
}

