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
 * Step 1: Thông tin cơ bản
 * - Số CCCD/CMND
 * - Họ và tên
 * - Giới tính
 * - Ngày sinh
 */
public class CccdStep1Fragment extends Fragment {

    private CccdViewModel viewModel;
    private TextInputLayout tilCccdNumber, tilFullName, tilGender, tilDateOfBirth;
    private TextInputEditText etCccdNumber, etFullName, etDateOfBirth;
    private AutoCompleteTextView actvGender;

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
        return inflater.inflate(R.layout.fragment_cccd_step1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupDropdowns();
        setupDatePicker();
        setupTextWatchers();
        restoreData();
        observeData(); // Observe LiveData for async updates
    }

    /**
     * Observe LiveData to update UI when data is loaded from API
     */
    private void observeData() {
        viewModel.getCccdNumber().observe(getViewLifecycleOwner(), value -> {
            if (value != null && !value.isEmpty() && !value.equals(getText(etCccdNumber))) {
                etCccdNumber.setText(value);
            }
        });

        viewModel.getFullName().observe(getViewLifecycleOwner(), value -> {
            if (value != null && !value.isEmpty() && !value.equals(getText(etFullName))) {
                etFullName.setText(value);
            }
        });

        viewModel.getGender().observe(getViewLifecycleOwner(), value -> {
            if (value != null && !value.isEmpty() && !value.equals(actvGender.getText().toString())) {
                actvGender.setText(value, false);
            }
        });

        viewModel.getDateOfBirth().observe(getViewLifecycleOwner(), value -> {
            if (value != null && !value.isEmpty() && !value.equals(getText(etDateOfBirth))) {
                etDateOfBirth.setText(value);
            }
        });
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString() : "";
    }

    private void initViews(View view) {
        tilCccdNumber = view.findViewById(R.id.tilCccdNumber);
        tilFullName = view.findViewById(R.id.tilFullName);
        tilGender = view.findViewById(R.id.tilGender);
        tilDateOfBirth = view.findViewById(R.id.tilDateOfBirth);

        etCccdNumber = view.findViewById(R.id.etCccdNumber);
        etFullName = view.findViewById(R.id.etFullName);
        actvGender = view.findViewById(R.id.actvGender);
        etDateOfBirth = view.findViewById(R.id.etDateOfBirth);
    }

    private void setupDropdowns() {
        String[] genderOptions = getResources().getStringArray(R.array.gender_options);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                genderOptions);
        actvGender.setAdapter(adapter);
        actvGender.setOnItemClickListener((parent, view, position, id) -> {
            viewModel.setGender(genderOptions[position]);
            tilGender.setError(null);
        });
    }

    private void setupDatePicker() {
        etDateOfBirth.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String date = dateFormatter.format(calendar.getTime());
                        etDateOfBirth.setText(date);
                        viewModel.setDateOfBirth(date);
                        tilDateOfBirth.setError(null);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            // Set max date to today minus 14 years
            Calendar maxDate = Calendar.getInstance();
            maxDate.add(Calendar.YEAR, -14);
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
            datePickerDialog.show();
        });

        // Also handle click on the layout's end icon
        if (tilDateOfBirth != null) {
            tilDateOfBirth.setEndIconOnClickListener(v -> etDateOfBirth.performClick());
        }
    }

    private void setupTextWatchers() {
        etCccdNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setCccdNumber(s.toString());
                tilCccdNumber.setError(null);
            }
        });

        etFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setFullName(s.toString());
                tilFullName.setError(null);
            }
        });
    }

    private void restoreData() {
        // Restore data from ViewModel when returning to this fragment
        String cccd = viewModel.getCccdNumberValue();
        if (!cccd.isEmpty())
            etCccdNumber.setText(cccd);

        String name = viewModel.getFullNameValue();
        if (!name.isEmpty())
            etFullName.setText(name);

        String gender = viewModel.getGenderValue();
        if (!gender.isEmpty())
            actvGender.setText(gender, false);

        String dob = viewModel.getDateOfBirthValue();
        if (!dob.isEmpty())
            etDateOfBirth.setText(dob);
    }

    /**
     * Validate step 1 data
     * 
     * @return true if valid
     */
    public boolean validate() {
        boolean isValid = true;

        CccdValidator.ValidationResult cccdResult = CccdValidator.validateCccdNumber(
                etCccdNumber.getText().toString().trim());
        if (!cccdResult.isValid()) {
            tilCccdNumber.setError(cccdResult.getErrorMessage());
            if (isValid)
                etCccdNumber.requestFocus();
            isValid = false;
        }

        CccdValidator.ValidationResult nameResult = CccdValidator.validateFullName(
                etFullName.getText().toString().trim());
        if (!nameResult.isValid()) {
            tilFullName.setError(nameResult.getErrorMessage());
            if (isValid)
                etFullName.requestFocus();
            isValid = false;
        }

        CccdValidator.ValidationResult genderResult = CccdValidator.validateGender(
                actvGender.getText().toString().trim());
        if (!genderResult.isValid()) {
            tilGender.setError(genderResult.getErrorMessage());
            if (isValid)
                actvGender.requestFocus();
            isValid = false;
        }

        CccdValidator.ValidationResult dobResult = CccdValidator.validateDateOfBirth(
                etDateOfBirth.getText().toString().trim());
        if (!dobResult.isValid()) {
            tilDateOfBirth.setError(dobResult.getErrorMessage());
            if (isValid)
                etDateOfBirth.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    /**
     * Save current data to ViewModel
     */
    public void saveData() {
        viewModel.setCccdNumber(etCccdNumber.getText().toString().trim());
        viewModel.setFullName(etFullName.getText().toString().trim());
        viewModel.setGender(actvGender.getText().toString().trim());
        viewModel.setDateOfBirth(etDateOfBirth.getText().toString().trim());
    }
}
