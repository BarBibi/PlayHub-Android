package com.example.playhub;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.text.Html;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GameDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GameDetailsFragment extends Fragment {

    private ImageView ivBanner;
    private TextView tvTitle, tvDesc, tvGenre, tvPlatform, tvPublisher, tvReleaseDate;
    private RecyclerView rvComments;
    private EditText etCommentInput;
    private ImageButton btnSend;

    private Game game;
    private CommentsAdapter adapter;
    private PlayHubApiService apiService;
    private FirebaseAuth mAuth;
    private String currentNickname = "Guest"; // Default nickname until user loaded
    private String currentEmail = "";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public GameDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GameDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GameDetailsFragment newInstance(String param1, String param2) {
        GameDetailsFragment fragment = new GameDetailsFragment();
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
        return inflater.inflate(R.layout.fragment_game_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Receive game object from arguments
        if (getArguments() != null) {
            game = (Game) getArguments().getSerializable("gameData");
        }

        initViews(view);
        initRetrofit();
        mAuth = FirebaseAuth.getInstance();

        // View game details
        if (game != null) {
            populateGameDetails();
            loadComments();
        }

        // Load current user's nickname and email
        loadCurrentUserDetails();

        btnSend.setOnClickListener(v -> postComment());

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });
    }

    private void initViews(View view) {
        ivBanner = view.findViewById(R.id.ivGameBanner);
        tvTitle = view.findViewById(R.id.tvDetailTitle);
        tvDesc = view.findViewById(R.id.tvDetailDesc);
        tvGenre = view.findViewById(R.id.tvDetailGenre);
        tvPlatform = view.findViewById(R.id.tvDetailPlatform);
        tvPublisher = view.findViewById(R.id.tvDetailPublisher);
        tvReleaseDate = view.findViewById(R.id.tvDetailDate);

        rvComments = view.findViewById(R.id.rvComments);
        etCommentInput = view.findViewById(R.id.etCommentInput);
        btnSend = view.findViewById(R.id.btnSendComment);

        rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CommentsAdapter(new ArrayList<>());
        rvComments.setAdapter(adapter);
    }

    private void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.0.8:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(PlayHubApiService.class);
    }

    private void populateGameDetails() {
        tvTitle.setText(game.getTitle());
        tvDesc.setText(game.getShortDescription());
        tvGenre.setText(Html.fromHtml("<b>Genre:</b> " + game.getGenre()));
        tvPlatform.setText(Html.fromHtml("<b>Platform:</b> " + game.getPlatform()));
        tvPublisher.setText(Html.fromHtml("<b>Publisher:</b> " + game.getPublisher()));
        tvReleaseDate.setText(Html.fromHtml("<b>Release Date:</b> " + game.getReleaseDate()));
        Glide.with(this).load(game.getThumbnail()).into(ivBanner);
    }

    private void loadCurrentUserDetails() {
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();
        currentEmail = mAuth.getCurrentUser().getEmail();

        apiService.getUser(uid).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentNickname = response.body().getNickname();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {}
        });
    }

    private void loadComments() {
        apiService.getComments(game.getId()).enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<Comment> comments = response.body();

                    // Update recycler view with new comments
                    adapter.setComments(comments);

                    if (comments.isEmpty()) {
                        Toast.makeText(getContext(), "No comments yet...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                if (isAdded()) Toast.makeText(getContext(), "Error loading comments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postComment() {
        String content = etCommentInput.getText().toString().trim();
        if (TextUtils.isEmpty(content)) return;

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please login to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create new comment object
        Comment newComment = new Comment(
                game.getId(),
                mAuth.getCurrentUser().getUid(),
                currentNickname,
                currentEmail,
                content
        );

        // Send comment to server
        apiService.addComment(newComment).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!isAdded()) return;

                if (response.isSuccessful()) {
                    etCommentInput.setText(""); // Clear input field
                    Toast.makeText(getContext(), "Comment posted!", Toast.LENGTH_SHORT).show();
                    loadComments(); // Refresh comments
                } else {
                    Toast.makeText(getContext(), "Failed to post", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (isAdded()) Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}