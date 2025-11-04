package com.example.androidexample;

import android.content.Context;
import android.content.Intent;
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

    public FriendAdapter(Context ctx, List<Friend> items, String currentUsername) {
        this.ctx = ctx;
        this.items = items;
        this.currentUsername = currentUsername;
    }

    // sets the current user's username
    public void setCurrentUsername(String me) { this.currentUsername = me; }
    // changes the adapter mode and refreshes the view
    public void setMode(Mode m) { this.mode = m; notifyDataSetChanged(); }

    // inflates each friend list item layout
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_friend, parent, false);
        return new VH(v);
    }

    // binds friend data to each view depending on mode
    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Friend f = items.get(pos);
        h.tvName.setText(f.getDisplayName());
        h.tvUsername.setText("@" + f.getUsername());

        h.btnProfile.setVisibility(View.GONE);
        h.btnAddFriend.setVisibility(View.GONE);
        h.llRequestActions.setVisibility(View.GONE);

        switch (mode) {
            // show profile button for friends
            case FRIENDS:
                h.btnProfile.setVisibility(View.VISIBLE);
                h.btnProfile.setOnClickListener(v -> {
                    Intent i = new Intent(ctx, FriendProfileActivity.class);
                    i.putExtra("username", f.getUsername());
                    i.putExtra("displayName", f.getDisplayName());
                    i.putExtra("currentUser", currentUsername);
                    ctx.startActivity(i);
                });
                break;

            case INCOMING:
                // show accept, reject, and block buttons for incoming requests
                h.llRequestActions.setVisibility(View.VISIBLE);
                h.btnAccept.setOnClickListener(v -> accept(f.getUsername()));
                h.btnReject.setOnClickListener(v -> reject(f.getUsername()));
                h.btnBlock.setOnClickListener(v -> block(f.getUsername()));
                break;

            case SUGGESTIONS:
                // show add friend button for suggestions
                h.btnAddFriend.setVisibility(View.VISIBLE);
                String label = f.getMutualCount() > 0
                        ? "Add (" + f.getMutualCount() + ")"
                        : "Add Friend";
                h.btnAddFriend.setText(label);
                h.btnAddFriend.setOnClickListener(v -> sendRequest(f.getUsername()));
                break;
        }
    }

    // returns total number of items in the list
    @Override
    public int getItemCount() { return items.size(); }

    // sends api request to accept a friend request
    private void accept(String username) {
        String url = BASE + "/users/" + currentUsername + "/friends/requests/" + username + "/accept";
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, null,
                r -> Toast.makeText(ctx, "Accepted " + username, Toast.LENGTH_SHORT).show(),
                e -> Toast.makeText(ctx, "Accept failed", Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(ctx).add(req);
    }

    // sends api request to reject a friend request
    private void reject(String username) {
        String url = BASE + "/users/" + currentUsername + "/friends/requests/" + username + "/reject";
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, null,
                r -> Toast.makeText(ctx, "Rejected " + username, Toast.LENGTH_SHORT).show(),
                e -> Toast.makeText(ctx, "Reject failed", Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(ctx).add(req);
    }

    // sends api request to block a user
    private void block(String username) {
        String url = BASE + "/users/" + currentUsername + "/friends/" + username + "/block";
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, null,
                r -> Toast.makeText(ctx, "Blocked " + username, Toast.LENGTH_SHORT).show(),
                e -> Toast.makeText(ctx, "Block failed", Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(ctx).add(req);
    }

    // sends api request to add a new friend
    private void sendRequest(String username) {
        String url = BASE + "/users/" + currentUsername + "/friends/requests/" + username;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, null,
                r -> Toast.makeText(ctx, "Request sent to " + username, Toast.LENGTH_SHORT).show(),
                e -> Toast.makeText(ctx, "Request failed", Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(ctx).add(req);
    }

    // viewholder class that holds references to all ui elements in an item
    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvUsername;
        Button btnAddFriend, btnAccept, btnReject, btnBlock, btnProfile;
        LinearLayout llRequestActions;

        // initializes all view references
        VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvFriendName);
            tvUsername = v.findViewById(R.id.tvFriendUsername);
            btnProfile = v.findViewById(R.id.btnProfile);
            btnAddFriend = v.findViewById(R.id.btnAddFriend);
            btnAccept = v.findViewById(R.id.btnAccept);
            btnReject = v.findViewById(R.id.btnReject);
            btnBlock = v.findViewById(R.id.btnBlock);
            llRequestActions = v.findViewById(R.id.llRequestActions);
        }
    }
}
