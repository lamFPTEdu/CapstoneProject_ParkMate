package com.parkmate.android.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.parkmate.android.R;
import com.parkmate.android.utils.validation.CccdValidator;
import com.parkmate.android.viewmodel.CccdViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Step 2: Chi tiết CCCD
 * - Quốc tịch
 * - Địa chỉ thường trú
 * - Ngày cấp
 * - Nơi cấp
 * - Ngày hết hạn
 */
public class CccdStep2Fragment extends Fragment {

    private CccdViewModel viewModel;
    private TextInputLayout tilNationality, tilPermanentAddress, tilIssueDate, tilIssuePlace, tilExpiryDate;
    private AutoCompleteTextView actvNationality;
    private TextInputEditText etPermanentAddress, etIssueDate, etIssuePlace, etExpiryDate;

    private Calendar calendar;
    private SimpleDateFormat dateFormatter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(CccdViewModel.class);
        calendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cccd_step2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupDropdowns();
        setupDatePickers();
        setupTextWatchers();
        restoreData();
        observeData(); // Observe LiveData for async updates
    }

    /**
     * Observe LiveData to update UI when data is loaded from API
     */
    private void observeData() {
        viewModel.getNationality().observe(getViewLifecycleOwner(), value -> {
            if (value != null && !value.isEmpty() && !value.equals(actvNationality.getText().toString())) {
                actvNationality.setText(value, false);
            }
        });

        viewModel.getPermanentAddress().observe(getViewLifecycleOwner(), value -> {
            if (value != null && !value.isEmpty() && !value.equals(getText(etPermanentAddress))) {
                etPermanentAddress.setText(value);
            }
        });

        viewModel.getIssueDate().observe(getViewLifecycleOwner(), value -> {
            if (value != null && !value.isEmpty() && !value.equals(getText(etIssueDate))) {
                etIssueDate.setText(value);
            }
        });

        viewModel.getIssuePlace().observe(getViewLifecycleOwner(), value -> {
            if (value != null && !value.isEmpty() && !value.equals(getText(etIssuePlace))) {
                etIssuePlace.setText(value);
            }
        });

        viewModel.getExpiryDate().observe(getViewLifecycleOwner(), value -> {
            if (value != null && !value.isEmpty() && !value.equals(getText(etExpiryDate))) {
                etExpiryDate.setText(value);
            }
        });
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString() : "";
    }

    private void initViews(View view) {
        tilNationality = view.findViewById(R.id.tilNationality);
        tilPermanentAddress = view.findViewById(R.id.tilPermanentAddress);
        tilIssueDate = view.findViewById(R.id.tilIssueDate);
        tilIssuePlace = view.findViewById(R.id.tilIssuePlace);
        tilExpiryDate = view.findViewById(R.id.tilExpiryDate);

        actvNationality = view.findViewById(R.id.actvNationality);
        etPermanentAddress = view.findViewById(R.id.etPermanentAddress);
        etIssueDate = view.findViewById(R.id.etIssueDate);
        etIssuePlace = view.findViewById(R.id.etIssuePlace);
        etExpiryDate = view.findViewById(R.id.etExpiryDate);
    }

    private void setupDropdowns() {
        String[] nationalityOptions = getResources().getStringArray(R.array.nationality_options);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                nationalityOptions);
        actvNationality.setAdapter(adapter);
        actvNationality.setOnItemClickListener((parent, view, position, id) -> {
            viewModel.setNationality(nationalityOptions[position]);
            tilNationality.setError(null);
        });
    }

    private void setupDatePickers() {
        // Issue Date picker
        etIssueDate.setOnClickListener(v -> showDatePicker(etIssueDate, tilIssueDate, false));
        if (tilIssueDate != null) {
            tilIssueDate.setEndIconOnClickListener(v -> etIssueDate.performClick());
        }

        // Expiry Date picker
        etExpiryDate.setOnClickListener(v -> showDatePicker(etExpiryDate, tilExpiryDate, true));
        if (tilExpiryDate != null) {
            tilExpiryDate.setEndIconOnClickListener(v -> etExpiryDate.performClick());
        }
    }

    private void showDatePicker(TextInputEditText editText, TextInputLayout layout, boolean isExpiry) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    String date = dateFormatter.format(calendar.getTime());
                    editText.setText(date);
                    if (isExpiry) {
                        viewModel.setExpiryDate(date);
                    } else {
                        viewModel.setIssueDate(date);
                    }
                    if (layout != null)
                        layout.setError(null);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        if (!isExpiry) {
            // Issue date: max is today
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        } else {
            // Expiry date: min is today
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        }

        datePickerDialog.show();
    }

    private void setupTextWatchers() {
        etPermanentAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setPermanentAddress(s.toString());
                tilPermanentAddress.setError(null);
            }
        });

        etIssuePlace.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setIssuePlace(s.toString());
                tilIssuePlace.setError(null);
            }
        });
    }

    private void restoreData() {
        String nationality = viewModel.getNationalityValue();
        if (!nationality.isEmpty())
            actvNationality.setText(nationality, false);

        String address = viewModel.getPermanentAddressValue();
        if (!address.isEmpty())
            etPermanentAddress.setText(address);

        String issueDate = viewModel.getIssueDateValue();
        if (!issueDate.isEmpty())
            etIssueDate.setText(issueDate);

        String issuePlace = viewModel.getIssuePlaceValue();
        if (!issuePlace.isEmpty())
            etIssuePlace.setText(issuePlace);

        String expiryDate = viewModel.getExpiryDateValue();
        if (!expiryDate.isEmpty())
            etExpiryDate.setText(expiryDate);
    }

    /**
     * Validate step 2 data
     * 
     * @return true if valid
     */
    public boolean validate() {
        boolean isValid = true;
        String dateOfBirth = viewModel.getDateOfBirthValue();

        CccdValidator.ValidationResult nationalityResult = CccdValidator.validateNationality(
                actvNationality.getText().toString().trim());
        if (!nationalityResult.isValid()) {
            tilNationality.setError(nationalityResult.getErrorMessage());
            if (isValid)
                actvNationality.requestFocus();
            isValid = false;
        }

        CccdValidator.ValidationResult addressResult = CccdValidator.validateAddress(
                etPermanentAddress.getText().toString().trim());
        if (!addressResult.isValid()) {
            tilPermanentAddress.setError(addressResult.getErrorMessage());
            if (isValid)
                etPermanentAddress.requestFocus();
            isValid = false;
        }

        CccdValidator.ValidationResult issueDateResult = CccdValidator.validateIssueDate(
                etIssueDate.getText().toString().trim(), dateOfBirth);
        if (!issueDateResult.isValid()) {
            tilIssueDate.setError(issueDateResult.getErrorMessage());
            if (isValid)
                etIssueDate.requestFocus();
            isValid = false;
        }

        CccdValidator.ValidationResult issuePlaceResult = CccdValidator.validateIssuePlace(
                etIssuePlace.getText().toString().trim());
        if (!issuePlaceResult.isValid()) {
            tilIssuePlace.setError(issuePlaceResult.getErrorMessage());
            if (isValid)
                etIssuePlace.requestFocus();
            isValid = false;
        }

        CccdValidator.ValidationResult expiryResult = CccdValidator.validateExpiryDate(
                etExpiryDate.getText().toString().trim(), etIssueDate.getText().toString().trim());
        if (!expiryResult.isValid()) {
            tilExpiryDate.setError(expiryResult.getErrorMessage());
            if (isValid)
                etExpiryDate.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    /**
     * Save current data to ViewModel
     */
    public void saveData() {
        viewModel.setNationality(actvNationality.getText().toString().trim());
        viewModel.setPermanentAddress(etPermanentAddress.getText().toString().trim());
        viewModel.setIssueDate(etIssueDate.getText().toString().trim());
        viewModel.setIssuePlace(etIssuePlace.getText().toString().trim());
        viewModel.setExpiryDate(etExpiryDate.getText().toString().trim());
    }
}
