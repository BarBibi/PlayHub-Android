package com.example.playhub.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.playhub.R;
import com.example.playhub.models.Comment;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private List<Comment> comments;

    public CommentsAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    public void setComments(List<Comment> newComments) {
        this.comments = newComments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.tvUser.setText(comment.getNickname());
        holder.tvEmail.setText(comment.getEmail());
        holder.tvContent.setText(comment.getContent());
        holder.tvDate.setText(comment.getTimestamp());
    }

    @Override
    public int getItemCount() { return comments.size(); }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvEmail, tvContent, tvDate;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tvCommentUser);
            tvEmail = itemView.findViewById(R.id.tvCommentEmail);
            tvContent = itemView.findViewById(R.id.tvCommentContent);
            tvDate = itemView.findViewById(R.id.tvCommentDate);
        }
    }
}