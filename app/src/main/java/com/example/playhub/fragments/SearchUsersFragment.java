package com.example.playhub.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.playhub.models.FollowRequest;
import com.example.playhub.api.PlayHubApiService;
import com.example.playhub.R;
import com.example.playhub.models.ResponseBody;
import com.example.playhub.adapters.UsersAdapter;
import com.example.playhub.models.User;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchUsersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchUsersFragment extends Fragment {

    private EditText etSearch;
    private ImageButton btnSearch, btnBack;
    private RecyclerView rvResults;
    private UsersAdapter adapter;

    private PlayHubApiService apiService;
    private String currentUid;

    // List of users that the current user is following
    private List<String> myFollowingList = new ArrayList<>();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SearchUsersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchUsersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchUsersFragment newInstance(String param1, String param2) {
        SearchUsersFragment fragment = new SearchUsersFragment();
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
        return inflater.inflate(R.layout.fragment_search_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etSearch = view.findViewById(R.id.etSearchUser);
        btnSearch = view.findViewById(R.id.btnSearchAction);
        rvResults = view.findViewById(R.id.rvUserResults);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.0.13:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(PlayHubApiService.class);

        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setupRecyclerView();

        // Fetch my profile to know who I follow
        fetchMyProfile();

        // Set Search Listener
        btnSearch.setOnClickListener(v -> performSearch());

        btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });
    }

    private void setupRecyclerView() {
        adapter = new UsersAdapter();
        rvResults.setLayoutManager(new LinearLayoutManager(getContext()));
        rvResults.setAdapter(adapter);

        adapter.setListener(new UsersAdapter.OnUserActionListener() {
            @Override
            public void onFollowClick(User user) {
                toggleFollow(user, true);
            }

            @Override
            public void onUnfollowClick(User user) {
                toggleFollow(user, false);
            }
        });
    }

    private void fetchMyProfile() {
        apiService.getUser(currentUid).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Update local list
                    if (response.body().getFollowing() != null) {
                        myFollowingList = response.body().getFollowing();
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(getContext(), "Error loading profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSearch() {
        String query = etSearch.getText().toString().trim();
        if (query.isEmpty()) return;

        apiService.searchUsers(query, currentUid).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    // Update adapter with results AND my following list
                    adapter.updateData(response.body(), myFollowingList);

                    if (response.body().isEmpty()) {
                        Toast.makeText(getContext(), "No users found", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(getContext(), "Search failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Toggle follow/unfollow for a user
    private void toggleFollow(User targetUser, boolean shouldFollow) {
        FollowRequest request = new FollowRequest(currentUid, targetUser.getUid());
        Call<ResponseBody> call;

        if (shouldFollow) {
            call = apiService.followUser(request);
        } else {
            call = apiService.unfollowUser(request);
        }

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!isAdded()) return;

                if (response.isSuccessful()) {
                    // Update local list of following users
                    if (shouldFollow) {
                        myFollowingList.add(targetUser.getUid());
                        Toast.makeText(getContext(), "Followed " + targetUser.getNickname(), Toast.LENGTH_SHORT).show();
                    } else {
                        myFollowingList.remove(targetUser.getUid());
                        Toast.makeText(getContext(), "Unfollowed " + targetUser.getNickname(), Toast.LENGTH_SHORT).show();
                    }

                    // Refresh adapter to update button color
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Action failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
