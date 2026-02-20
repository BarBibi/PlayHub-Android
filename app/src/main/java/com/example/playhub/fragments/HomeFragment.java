package com.example.playhub.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import androidx.appcompat.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.playhub.models.FavoriteRequest;
import com.example.playhub.adapters.GameAdapter;
import com.example.playhub.api.GameApiService;
import com.example.playhub.api.PlayHubApiService;
import com.example.playhub.R;
import com.example.playhub.models.ResponseBody;
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
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private RecyclerView rvGames;
    private ProgressBar progressBar;
    private GameAdapter adapter;
    private SearchView searchView;
    private Spinner spinnerGenre, spinnerPlatform;
    private ImageButton btnSettings, btnFavorites;

    private String currentQuery = "";
    private String currentGenre = "All Genres";
    private String currentPlatform = "All Platforms";

    private PlayHubApiService myApiService;
    private FirebaseAuth mAuth;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvGames = view.findViewById(R.id.rvGames);
        searchView = view.findViewById(R.id.searchView);
        spinnerGenre = view.findViewById(R.id.spinnerGenre);
        spinnerPlatform = view.findViewById(R.id.spinnerPlatform);
        progressBar = view.findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        Retrofit myRetrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.0.13:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        myApiService = myRetrofit.create(PlayHubApiService.class);

        setupRecyclerView();

        setupSpinners();

        setupFilters();

        fetchGames();

        syncFavorites();

        btnSettings = view.findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_settingsFragment);
        });

        btnFavorites = view.findViewById(R.id.btnFavorites);
        btnFavorites.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_favoritesFragment);
        });

        ImageButton btnAddFriend = view.findViewById(R.id.btnAddFriend);
        btnAddFriend.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_searchUsersFragment);
        });
    }

    private void setupRecyclerView() {
        rvGames.setLayoutManager(new LinearLayoutManager(getContext()));

        // Create adapter with empty list for favorites
        adapter = new GameAdapter(getContext(), new ArrayList<>(), new GameAdapter.OnFavoriteClickListener() {
            @Override
            public void onFavoriteClick(int gameId, boolean isFavorite) {
                handleFavoriteAction(gameId, isFavorite);
            }
        });

        // Set listener for game item click
        adapter.setOnItemClickListener(new GameAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Game game) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("gameData", game);
                Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_gameDetailsFragment, bundle);
            }
        });

        rvGames.setAdapter(adapter);
    }

    private void setupSpinners() {
        // Genre Spinner
        ArrayAdapter<CharSequence> genreAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.game_genres, android.R.layout.simple_spinner_item);
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenre.setAdapter(genreAdapter);

        // Platform Spinner
        ArrayAdapter<CharSequence> platformAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.game_platforms, android.R.layout.simple_spinner_item);
        platformAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlatform.setAdapter(platformAdapter);
    }

    private void setupFilters() {
        // Listening for a change in search text
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                adapter.filter(currentQuery, currentGenre, currentPlatform);
                return true;
            }
        });

        // Listening to genre selection
        spinnerGenre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentGenre = parent.getItemAtPosition(position).toString();
                adapter.filter(currentQuery, currentGenre, currentPlatform);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Listening to platform selection
        spinnerPlatform.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentPlatform = parent.getItemAtPosition(position).toString();
                adapter.filter(currentQuery, currentGenre, currentPlatform);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void fetchGames() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.freetogame.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GameApiService apiService = retrofit.create(GameApiService.class);

        Call<List<Game>> call = apiService.getGames();
        call.enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                // If fragment is not attached, stop here to avoid crash
                if (!isAdded()) return;

                // Hide loading indicator
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                if (response.isSuccessful() && response.body() != null) {
                    // Use setGames() to update both full and displayed lists
                    adapter.setGames(response.body());
                } else {
                    Toast.makeText(getContext(), "Failed to load games", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                if (!isAdded()) return;

                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFavoriteAction(int gameId, boolean isFavorite) {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        FavoriteRequest request = new FavoriteRequest(gameId);

        if (isFavorite) {
            // User checked the box -> Add to favorites
            myApiService.addFavorite(uid, request).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(getContext(), "Failed to add favorite", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // User unchecked the box -> Remove from favorites
            myApiService.removeFavorite(uid, request).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(getContext(), "Failed to remove favorite", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void syncFavorites() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        // Fetch user data from MongoDB
        myApiService.getUser(uid).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // Check if fragment is still alive to avoid crashes
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();

                    // Helper list to store valid Game IDs
                    List<Integer> favoriteIds = new ArrayList<>();

                    // Get the raw list from the user object
                    List<Object> rawFavorites = user.getFavorites();

                    if (rawFavorites != null) {
                        for (Object item : rawFavorites) {
                            // Gson might treat numbers as Double (e.g., 540.0)
                            if (item instanceof Double) {
                                favoriteIds.add(((Double) item).intValue());
                            } else if (item instanceof Integer) {
                                favoriteIds.add((Integer) item);
                            }
                        }
                    }

                    // Update the adapter with the clean list of IDs
                    if (adapter != null) {
                        adapter.setFavorites(favoriteIds);
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // Silent fail - we don't want to annoy user if favorites don't load instantly
            }
        });
    }
}
