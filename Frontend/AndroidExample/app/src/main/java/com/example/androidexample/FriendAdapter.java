package com.example.androidexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.VH> {

    public enum Mode { FRIENDS, INCOMING, SUGGESTIONS, SEARCH }

    private final Context ctx;
    private final List<Friend> items;
    private String currentUsername;
    private Mode mode = Mode.FRIENDS;

    private static final String BASE = "http://coms-3090-026.class.las.iastate.edu:8080/api/v8";

    // constructor for adapter
    public FriendAdapter(Context ctx, List<Friend> items, String currentUsername) {
        this.ctx = ctx;
        this.items = items;
        this.currentUsername = currentUsername;
    }

    // set current user name
    public void setCurrentUsername(String me) { this.currentUsername = me; }

    // set adapter mode (friends, incoming, etc.)
    public void setMode(Mode m) { this.mode = m; notifyDataSetChanged(); }

    // create a new view holder for recycler view
    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_friend, parent, false);
        return new VH(v);
    }

    // bind data to each row in the list
    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Friend f = items.get(pos);
        h.tvName.setText(f.getDisplayName());
        h.tvUsername.setText("@" + f.getUsername());

        h.btnUnfriend.setVisibility(View.GONE);
        h.btnAccept.setVisibility(View.GONE);
        h.btnReject.setVisibility(View.GONE);

        switch (mode) {
            case FRIENDS:
                h.btnUnfriend.setVisibility(View.VISIBLE);
                h.btnUnfriend.setOnClickListener(v -> unfriend(f.getUsername()));
                break;
            case INCOMING:
                h.btnAccept.setVisibility(View.VISIBLE);
                h.btnReject.setVisibility(View.VISIBLE);
                h.btnAccept.setOnClickListener(v -> accept(f.getUsername()));
                h.btnReject.setOnClickListener(v -> reject(f.getUsername()));
                break;
            case SUGGESTIONS:
            case SEARCH:
                h.btnAccept.setVisibility(View.VISIBLE);
                String label = f.getMutualCount() > 0
                        ? "Add (" + f.getMutualCount() + ")"
                        : "Add Friend";
                h.btnAccept.setText(label);
                h.btnAccept.setOnClickListener(v -> sendRequest(f.getUsername()));
                break;
        }
    }

    // return total number of items
    @Override
    public int getItemCount() { return items.size(); }

    // view holder for list item
    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvUsername;
        Button btnUnfriend, btnAccept, btnReject;

        // initialize ui components
        VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvFriendName);
            tvUsername = v.findViewById(R.id.tvFriendUsername);
            btnUnfriend = v.findViewById(R.id.btnUnfriend);
            btnAccept = v.findViewById(R.id.btnAccept);
            btnReject = v.findViewById(R.id.btnReject);
        }
    }

    // send friend request
    private void sendRequest(String targetUsername) {
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(ctx, "Enter your username first", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = BASE + "/users/" + currentUsername + "/friends/requests/" + targetUsername;
        JsonObjectRequest r = new JsonObjectRequest(Request.Method.POST, url, null,
                res -> Toast.makeText(ctx, "Request sent to @" + targetUsername, Toast.LENGTH_SHORT).show(),
                err -> Toast.makeText(ctx, "Send failed: " + err.getMessage(), Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(ctx).add(r);
    }

    // unfriend a user
    private void unfriend(String other) {
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(ctx, "Enter your username first", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = BASE + "/users/" + currentUsername + "/friends/" + other;
        JsonObjectRequest r = new JsonObjectRequest(Request.Method.DELETE, url, null,
                res -> Toast.makeText(ctx, "Unfriended @" + other, Toast.LENGTH_SHORT).show(),
                err -> Toast.makeText(ctx, "Unfriend failed: " + err.getMessage(), Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(ctx).add(r);
    }

    // accept friend request
    private void accept(String requester) {
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(ctx, "Enter your username first", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = BASE + "/users/" + currentUsername + "/friends/requests/" + requester + "/accept";
        JsonObjectRequest r = new JsonObjectRequest(Request.Method.POST, url, null,
                res -> Toast.makeText(ctx, "Accepted @" + requester, Toast.LENGTH_SHORT).show(),
                err -> Toast.makeText(ctx, "Accept failed: " + err.getMessage(), Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(ctx).add(r);
    }

    // reject friend request
    private void reject(String requester) {
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(ctx, "Enter your username first", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = BASE + "/users/" + currentUsername + "/friends/requests/" + requester + "/reject";
        JsonObjectRequest r = new JsonObjectRequest(Request.Method.POST, url, null,
                res -> Toast.makeText(ctx, "Rejected @" + requester, Toast.LENGTH_SHORT).show(),
                err -> Toast.makeText(ctx, "Reject failed: " + err.getMessage(), Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(ctx).add(r);
    }
}
