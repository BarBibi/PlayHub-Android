package com.example.playhub;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    private EditText etEmail, etNickname, etPassword, etPhone, etBirthDate;
    private RadioGroup rgGender;
    private Button btnSave, btnLogout;

    private FirebaseAuth mAuth;
    private PlayHubApiService apiService;
    private String currentUid;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            // If not logged in, navigate back to Login Screen
            Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_loginFragment);
            return;
        }

        currentUid = mAuth.getCurrentUser().getUid();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.0.11:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(PlayHubApiService.class);

        initViews(view);

        loadUserData();

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        btnSave.setOnClickListener(v -> saveChanges());
        btnLogout.setOnClickListener(v -> logoutUser(v));
    }

    private void initViews(View view) {
        etEmail = view.findViewById(R.id.etEmail);
        etNickname = view.findViewById(R.id.etNickname);
        etPassword = view.findViewById(R.id.etPassword);
        etPhone = view.findViewById(R.id.etPhone);
        etBirthDate = view.findViewById(R.id.etBirthDate);
        rgGender = view.findViewById(R.id.rgGender);
        btnSave = view.findViewById(R.id.btnSave);
        btnLogout = view.findViewById(R.id.btnLogout);
    }

    private void loadUserData() {
        apiService.getUser(currentUid).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();

                    // Populate fields
                    etEmail.setText(user.getEmail());
                    etNickname.setText(user.getNickname());
                    etPassword.setText(user.getPassword());
                    etPhone.setText(user.getPhone());
                    etBirthDate.setText(user.getBirthDate());

                    // Set gender radio button
                    String gender = user.getGender();
                    if (gender != null) {
                        if (gender.equalsIgnoreCase("Male")) rgGender.check(R.id.rbMale);
                        else if (gender.equalsIgnoreCase("Female")) rgGender.check(R.id.rbFemale);
                        else rgGender.check(R.id.rbOther);
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveChanges() {
        String newPassword = etPassword.getText().toString().trim();

        // Scenario A: User wants to change password
        if (!newPassword.isEmpty()) {
            FirebaseUser user = mAuth.getCurrentUser();

            if (user != null) {
                user.updatePassword(newPassword)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Password updated successfully -> Proceed to update MongoDB
                                updateMongoDB();
                                Toast.makeText(getContext(), "Password updated in Firebase", Toast.LENGTH_SHORT).show();
                            } else {
                                // Error handling
                                Exception e = task.getException();

                                if (e instanceof FirebaseAuthRecentLoginRequiredException) {
                                    Toast.makeText(getContext(), "Security Alert: Please Logout and Login again to change password.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getContext(), "Password Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
        // Scenario B: No password change
        else {
            updateMongoDB();
        }
    }

    private void updateMongoDB() {
        String nickname = etNickname.getText().toString();
        String phone = etPhone.getText().toString();
        String birthDate = etBirthDate.getText().toString();
        String password = etPassword.getText().toString();

        String gender = null;
        int selectedId = rgGender.getCheckedRadioButtonId();
        if (selectedId == R.id.rbMale) gender = "Male";
        else if (selectedId == R.id.rbFemale) gender = "Female";
        else if (selectedId == R.id.rbOther) gender = "Other";

        // Note: We send existing email and uid, but Python will ignore email updates.
        User updatedUser = new User(currentUid, etEmail.getText().toString(), password, birthDate, nickname, phone, gender);

        apiService.updateUser(currentUid, updatedUser).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!isAdded()) return;

                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(getView()).navigateUp();
                } else {
                    Toast.makeText(getContext(), "Failed to save profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void logoutUser(View view) {
        // Sign out from Firebase
        mAuth.signOut();

        // Navigate back to Login Screen (and clear history so back button won't work)
        Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_loginFragment);
    }
}
