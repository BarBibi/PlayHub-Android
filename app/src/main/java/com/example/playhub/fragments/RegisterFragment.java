package com.example.playhub.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.playhub.api.PlayHubApiService;
import com.example.playhub.R;
import com.example.playhub.models.ResponseBody;
import com.example.playhub.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment {

    private FirebaseAuth mAuth;

    private EditText emailField, passwordField, nicknameField, birthDateField, phoneField;
    private RadioGroup genderGroup;
    private Button btnSignUp;
    private TextView tvBackToLogin;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegisterFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterFragment newInstance(String param1, String param2) {
        RegisterFragment fragment = new RegisterFragment();
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
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        mAuth = FirebaseAuth.getInstance();
        emailField = view.findViewById(R.id.emailField);
        passwordField = view.findViewById(R.id.passwordField);
        nicknameField = view.findViewById(R.id.nicknameField);
        birthDateField = view.findViewById(R.id.birthDateField);
        phoneField = view.findViewById(R.id.phoneField);
        genderGroup = view.findViewById(R.id.genderField);

        btnSignUp = view.findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(v -> registerUser(v));

        tvBackToLogin = view.findViewById(R.id.tvBackToLogin);
        tvBackToLogin.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_registerFragment_to_loginFragment));

        return  view;
    }

    // Register user in Firebase Auth
    private void registerUser(View view) {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        // Validating fields email & password are not empty (Required fields)
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in email & password fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Register user in Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show();

                        // Write user data to MongoDB
                        writeToDB();

                        // Navigate back to the login screen
                        Navigation.findNavController(view).navigate(R.id.action_registerFragment_to_loginFragment);
                    } else {
                        Exception exception = task.getException();

                        if (exception instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(requireContext(), "This email is already registered", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Registration failed. Please try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Write user data to MongoDB
    private void writeToDB() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        String birthDate = birthDateField.getText().toString();
        String nickname = nicknameField.getText().toString();
        String phone = phoneField.getText().toString();

        String gender = "Other";
        int selectedId = genderGroup.getCheckedRadioButtonId();
        if (selectedId != -1) {
            if (selectedId == R.id.rbMale) gender = "Male";
            else if (selectedId == R.id.rbFemale) gender = "Female";
            else if (selectedId == R.id.rbOther) gender = "Other";
        }

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();

            User user = new User(uid, email, password, birthDate, nickname, phone, gender);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://10.0.0.13:5000/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            PlayHubApiService apiService = retrofit.create(PlayHubApiService.class);

            apiService.createUser(user).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    // Check if fragment is still attached to an activity
                    if (!isAdded()) return;

                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(requireContext(), "Saved: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Connection Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
