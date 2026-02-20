package com.example.playhub.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.playhub.adapters.GameAdapter;
import com.example.playhub.api.GameApiService;
import com.example.playhub.api.PlayHubApiService;
import com.example.playhub.R;
import com.example.playhub.models.Game;
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
 * Use the {@link FavoritesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private GameAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private ImageButton btnBack;

    private FirebaseAuth mAuth;
    private PlayHubApiService apiService;
    private GameApiService gameApiService;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FavoritesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FavoritesFragment newInstance(String param1, String param2) {
        FavoritesFragment fragment = new FavoritesFragment();
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
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFavorites = view.findViewById(R.id.rvFavorites);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        Retrofit myRetrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.0.13:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = myRetrofit.create(PlayHubApiService.class);

        Retrofit gameRetrofit = new Retrofit.Builder()
                .baseUrl("https://www.freetogame.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        gameApiService = gameRetrofit.create(GameApiService.class);

        setupRecyclerView();

        loadFavorites(uid);
    }

    private void setupRecyclerView() {
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));

        // Listener is null because we don't handle un-liking directly from here yet
        adapter = new GameAdapter(getContext(), new ArrayList<>(), null);

        rvFavorites.setAdapter(adapter);
    }

    private void loadFavorites(String uid) {
        apiService.getUser(uid).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    List<Object> rawFavorites = user.getFavorites();
                    List<Integer> favIds = new ArrayList<>();

                    if (rawFavorites != null) {
                        for (Object id : rawFavorites) {
                            if (id instanceof Double) favIds.add(((Double) id).intValue());
                            else if (id instanceof Integer) favIds.add((Integer) id);
                        }
                    }

                    if (favIds.isEmpty()) {
                        // Show "No Favorites" message
                        progressBar.setVisibility(View.GONE);
                        tvEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        // Fetch the actual game data
                        fetchAndFilterGames(favIds);
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                if (isAdded()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error loading profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchAndFilterGames(List<Integer> favIds) {
        gameApiService.getGames().enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Game> allGames = response.body();
                    List<Game> favoriteGames = new ArrayList<>();

                    for (Game game : allGames) {
                        if (favIds.contains(game.getId())) {
                            favoriteGames.add(game);
                        }
                    }

                    // Update UI
                    adapter.setGames(favoriteGames);
                    // Mark as favorites
                    adapter.setFavorites(favIds);
                }
            }

            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                if (isAdded()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error loading games", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
