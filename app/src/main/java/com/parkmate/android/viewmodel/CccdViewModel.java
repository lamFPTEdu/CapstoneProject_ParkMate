package com.parkmate.android.viewmodel;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Shared ViewModel để lưu trữ và chia sẻ dữ liệu CCCD giữa các fragments
 */
public class CccdViewModel extends ViewModel {

    // Step 1: Thông tin cơ bản
    private final MutableLiveData<String> cccdNumber = new MutableLiveData<>("");
    private final MutableLiveData<String> fullName = new MutableLiveData<>("");
    private final MutableLiveData<String> gender = new MutableLiveData<>("");
    private final MutableLiveData<String> dateOfBirth = new MutableLiveData<>("");

    // Step 2: Chi tiết
    private final MutableLiveData<String> nationality = new MutableLiveData<>("Việt Nam");
    private final MutableLiveData<String> permanentAddress = new MutableLiveData<>("");
    private final MutableLiveData<String> issueDate = new MutableLiveData<>("");
    private final MutableLiveData<String> issuePlace = new MutableLiveData<>("");
    private final MutableLiveData<String> expiryDate = new MutableLiveData<>("");

    // Step 3: Ảnh
    private final MutableLiveData<Uri> frontImageUri = new MutableLiveData<>();
    private final MutableLiveData<Uri> backImageUri = new MutableLiveData<>();
    private final MutableLiveData<String> frontImagePath = new MutableLiveData<>("");
    private final MutableLiveData<String> backImagePath = new MutableLiveData<>("");
    private final MutableLiveData<String> frontPhotoPresignedUrl = new MutableLiveData<>("");
    private final MutableLiveData<String> backPhotoPresignedUrl = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> frontImageUploaded = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> backImageUploaded = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> commitmentChecked = new MutableLiveData<>(false);

    // Current step
    private final MutableLiveData<Integer> currentStep = new MutableLiveData<>(0);

    // === Step 1 Getters/Setters ===
    public LiveData<String> getCccdNumber() {
        return cccdNumber;
    }

    public void setCccdNumber(String value) {
        cccdNumber.setValue(value);
    }

    public String getCccdNumberValue() {
        return cccdNumber.getValue() != null ? cccdNumber.getValue() : "";
    }

    public LiveData<String> getFullName() {
        return fullName;
    }

    public void setFullName(String value) {
        fullName.setValue(value);
    }

    public String getFullNameValue() {
        return fullName.getValue() != null ? fullName.getValue() : "";
    }

    public LiveData<String> getGender() {
        return gender;
    }

    public void setGender(String value) {
        gender.setValue(value);
    }

    public String getGenderValue() {
        return gender.getValue() != null ? gender.getValue() : "";
    }

    public LiveData<String> getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String value) {
        dateOfBirth.setValue(value);
    }

    public String getDateOfBirthValue() {
        return dateOfBirth.getValue() != null ? dateOfBirth.getValue() : "";
    }

    // === Step 2 Getters/Setters ===
    public LiveData<String> getNationality() {
        return nationality;
    }

    public void setNationality(String value) {
        nationality.setValue(value);
    }

    public String getNationalityValue() {
        return nationality.getValue() != null ? nationality.getValue() : "";
    }

    public LiveData<String> getPermanentAddress() {
        return permanentAddress;
    }

    public void setPermanentAddress(String value) {
        permanentAddress.setValue(value);
    }

    public String getPermanentAddressValue() {
        return permanentAddress.getValue() != null ? permanentAddress.getValue() : "";
    }

    public LiveData<String> getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String value) {
        issueDate.setValue(value);
    }

    public String getIssueDateValue() {
        return issueDate.getValue() != null ? issueDate.getValue() : "";
    }

    public LiveData<String> getIssuePlace() {
        return issuePlace;
    }

    public void setIssuePlace(String value) {
        issuePlace.setValue(value);
    }

    public String getIssuePlaceValue() {
        return issuePlace.getValue() != null ? issuePlace.getValue() : "";
    }

    public LiveData<String> getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String value) {
        expiryDate.setValue(value);
    }

    public String getExpiryDateValue() {
        return expiryDate.getValue() != null ? expiryDate.getValue() : "";
    }

    // === Step 3 Getters/Setters ===
    public LiveData<Uri> getFrontImageUri() {
        return frontImageUri;
    }

    public void setFrontImageUri(Uri value) {
        frontImageUri.setValue(value);
    }

    public Uri getFrontImageUriValue() {
        return frontImageUri.getValue();
    }

    public LiveData<Uri> getBackImageUri() {
        return backImageUri;
    }

    public void setBackImageUri(Uri value) {
        backImageUri.setValue(value);
    }

    public Uri getBackImageUriValue() {
        return backImageUri.getValue();
    }

    public LiveData<String> getFrontImagePath() {
        return frontImagePath;
    }

    public void setFrontImagePath(String value) {
        frontImagePath.setValue(value);
    }

    public String getFrontImagePathValue() {
        return frontImagePath.getValue() != null ? frontImagePath.getValue() : "";
    }

    public LiveData<String> getBackImagePath() {
        return backImagePath;
    }

    public void setBackImagePath(String value) {
        backImagePath.setValue(value);
    }

    public String getBackImagePathValue() {
        return backImagePath.getValue() != null ? backImagePath.getValue() : "";
    }

    // === Presigned URLs for displaying existing images ===
    public LiveData<String> getFrontPhotoPresignedUrl() {
        return frontPhotoPresignedUrl;
    }

    public void setFrontPhotoPresignedUrl(String value) {
        frontPhotoPresignedUrl.setValue(value);
    }

    public String getFrontPhotoPresignedUrlValue() {
        return frontPhotoPresignedUrl.getValue() != null ? frontPhotoPresignedUrl.getValue() : "";
    }

    public LiveData<String> getBackPhotoPresignedUrl() {
        return backPhotoPresignedUrl;
    }

    public void setBackPhotoPresignedUrl(String value) {
        backPhotoPresignedUrl.setValue(value);
    }

    public String getBackPhotoPresignedUrlValue() {
        return backPhotoPresignedUrl.getValue() != null ? backPhotoPresignedUrl.getValue() : "";
    }

    public LiveData<Boolean> getFrontImageUploaded() {
        return frontImageUploaded;
    }

    public void setFrontImageUploaded(Boolean value) {
        frontImageUploaded.setValue(value);
    }

    public Boolean isFrontImageUploaded() {
        return frontImageUploaded.getValue() != null && frontImageUploaded.getValue();
    }

    public LiveData<Boolean> getBackImageUploaded() {
        return backImageUploaded;
    }

    public void setBackImageUploaded(Boolean value) {
        backImageUploaded.setValue(value);
    }

    public Boolean isBackImageUploaded() {
        return backImageUploaded.getValue() != null && backImageUploaded.getValue();
    }

    public LiveData<Boolean> getCommitmentChecked() {
        return commitmentChecked;
    }

    public void setCommitmentChecked(Boolean value) {
        commitmentChecked.setValue(value);
    }

    public Boolean isCommitmentChecked() {
        return commitmentChecked.getValue() != null && commitmentChecked.getValue();
    }

    // === Current Step ===
    public LiveData<Integer> getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(Integer step) {
        currentStep.setValue(step);
    }

    public Integer getCurrentStepValue() {
        return currentStep.getValue() != null ? currentStep.getValue() : 0;
    }

    /**
     * Check if has front image (new URI, existing path, or presigned URL)
     */
    public boolean hasFrontImage() {
        return getFrontImageUriValue() != null ||
                (isFrontImageUploaded() && !getFrontImagePathValue().isEmpty()) ||
                !getFrontPhotoPresignedUrlValue().isEmpty();
    }

    /**
     * Check if has back image (new URI, existing path, or presigned URL)
     */
    public boolean hasBackImage() {
        return getBackImageUriValue() != null ||
                (isBackImageUploaded() && !getBackImagePathValue().isEmpty()) ||
                !getBackPhotoPresignedUrlValue().isEmpty();
    }
}
