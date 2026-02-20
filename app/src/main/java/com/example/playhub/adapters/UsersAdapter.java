package com.example.playhub.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.playhub.R;
import com.example.playhub.models.User;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {
    private List<User> usersList = new ArrayList<>();
    private List<String> followingList = new ArrayList<>();
    private OnUserActionListener listener;

    // Interface for click events
    public interface OnUserActionListener {
        void onFollowClick(User user);
        void onUnfollowClick(User user);
    }

    public void setListener(OnUserActionListener listener) {
        this.listener = listener;
    }

    public void updateData(List<User> users, List<String> following) {
        this.usersList = users;
        this.followingList = following;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = usersList.get(position);

        holder.tvName.setText(user.getNickname());

        // Decode Base64 Image
        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(user.getProfileImage(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.ivProfile.setImageBitmap(decodedByte);
            } catch (Exception e) {
                holder.ivProfile.setImageResource(android.R.drawable.sym_def_app_icon);
            }
        } else {
            holder.ivProfile.setImageResource(android.R.drawable.sym_def_app_icon);
        }

        // Check if I am already following this user
        if (followingList.contains(user.getUid())) {
            // Already following -> Show "Unfollow" state
            holder.btnFollow.setText("Unfollow");
            holder.btnFollow.setBackgroundColor(0xFF757575); // Gray

            holder.btnFollow.setOnClickListener(v -> {
                if (listener != null) listener.onUnfollowClick(user);
            });
        } else {
            // Not following -> Show "Follow" state
            holder.btnFollow.setText("Follow");
            holder.btnFollow.setBackgroundColor(0xFF2196F3); // Blue

            holder.btnFollow.setOnClickListener(v -> {
                if (listener != null) listener.onFollowClick(user);
            });
        }
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvName;
        Button btnFollow;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivUserProfile);
            tvName = itemView.findViewById(R.id.tvUserNickname);
            btnFollow = itemView.findViewById(R.id.btnFollow);
        }
    }
}