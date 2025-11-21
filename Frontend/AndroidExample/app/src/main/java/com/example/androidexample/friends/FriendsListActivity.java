package com.example.androidexample.friends;
import com.example.androidexample.R;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.androidexample.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidexample.HomeActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class FriendsListActivity extends AppCompatActivity {

    private static final String HOST = "http://coms-3090-026.class.las.iastate.edu:8080";
    private static final String BASE_V8 = HOST + "/api/v8";
    private static final String USER_BY_ID = HOST + "/api/v2/users/";

    private EditText etCurrentUsername;
    private RecyclerView recyclerFriends;
    private TextView tvNoFriends;
    private Button btnRequests, btnSuggestions, btnAddFriend, btnLoad;

    private final ArrayList<Friend> friendList = new ArrayList<>();
    private FriendAdapter adapter;
    private ProgressDialog progressDialog;
    private RequestQueue queue;

    private String currentUsername;
    private int currentUserId = -1;
    private FriendAdapter.Mode mode = FriendAdapter.Mode.FRIENDS;

    private static final String[] STARTER_USERNAMES =
            new String[]{"alice123", "ufclover1234", "testuser"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        queue = Volley.newRequestQueue(this); // create network queue

        // setup ui components
        etCurrentUsername = findViewById(R.id.etCurrentUsername);
        recyclerFriends = findViewById(R.id.recyclerFriends);
        tvNoFriends = findViewById(R.id.tvNoFriends);
        btnRequests = findViewById(R.id.btnViewRequests);
        btnSuggestions = findViewById(R.id.btnViewSuggestions);
        btnAddFriend = findViewById(R.id.btnAddFriend);
        btnLoad = findViewById(R.id.btnLoadFriends);


        recyclerFriends.setLayoutManager(new LinearLayoutManager(this)); // vertical list
        adapter = new FriendAdapter(this, friendList, null, -1L);
        adapter.setMode(FriendAdapter.Mode.FRIENDS);
        recyclerFriends.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // hide username input if already logged in
        if (etCurrentUsername != null) etCurrentUsername.setVisibility(View.GONE);

        // try to get username and userId from intent or saved session
        currentUsername = getIntent().getStringExtra("username");
        currentUserId = getIntent().getIntExtra("userId", -1);

        if (TextUtils.isEmpty(currentUsername)) {
            currentUsername = getSharedPreferences("session", MODE_PRIVATE)
                    .getString("username", null);
        }
        if (currentUserId <= 0) {
            currentUserId = getSharedPreferences("session", MODE_PRIVATE)
                    .getInt("userId", -1);
        }

        // Set adapter identity immediately so it's never stale
        adapter.setCurrentUsername(currentUsername);
        adapter.setCurrentUserId(currentUserId);

        // load friends automatically if user known
        if (!TextUtils.isEmpty(currentUsername)) {
            loadFriends(currentUsername, currentUserId);
        } else if (currentUserId > 0) {
            // try to resolve username from id
            progressDialog.setMessage("Loading your profile…");
            progressDialog.show();
            String url = USER_BY_ID + currentUserId;
            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.GET, url, null,
                    resp -> {
                        progressDialog.dismiss();
                        currentUsername = resp.optString("username", null);
                        if (!TextUtils.isEmpty(currentUsername)) {
                            getSharedPreferences("session", MODE_PRIVATE)
                                    .edit()
                                    .putString("username", currentUsername)
                                    .putLong("userId", currentUserId)
                                    .apply();
                            // keep adapter in sync
                            adapter.setCurrentUsername(currentUsername);
                            adapter.setCurrentUserId(currentUserId);
                            loadFriends(currentUsername, currentUserId);
                        } else {
                            tvNoFriends.setVisibility(View.VISIBLE);
                            tvNoFriends.setText("Couldn’t resolve username. Please log in again.");
                        }
                    },
                    err -> {
                        progressDialog.dismiss();
                        tvNoFriends.setVisibility(View.VISIBLE);
                        tvNoFriends.setText("Couldn’t resolve username. Please log in again.");
                    });
            queue.add(req);
        } else {
            tvNoFriends.setVisibility(View.VISIBLE);
            tvNoFriends.setText("Please log in to load your friends.");
        }

        // button click actions
        if (btnLoad != null) {
            btnLoad.setOnClickListener(v -> {
                mode = FriendAdapter.Mode.FRIENDS;
                adapter.setMode(FriendAdapter.Mode.FRIENDS);
                if (ensureMe()) loadFriends(currentUsername, currentUserId);
            });
        }

        btnRequests.setOnClickListener(v -> {
            mode = FriendAdapter.Mode.INCOMING;
            adapter.setMode(FriendAdapter.Mode.INCOMING);
            if (ensureMe()) loadIncoming(currentUsername, currentUserId);
        });

        btnSuggestions.setOnClickListener(v -> {
            mode = FriendAdapter.Mode.SUGGESTIONS;
            adapter.setMode(FriendAdapter.Mode.SUGGESTIONS);
            if (ensureMe()) loadSuggestions(currentUsername, 50, currentUserId);
        });

        btnAddFriend.setOnClickListener(v -> showSearchDialog());

        //intent to return home
        Button btnReturnHome = findViewById(R.id.btnReturnHome);
        btnReturnHome.setOnClickListener(v -> {
            Intent intent = new Intent(FriendsListActivity.this, HomeActivity.class);
            intent.putExtra("userId", currentUserId);
            intent.putExtra("username", currentUsername);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // refresh list when coming back
        if (!TextUtils.isEmpty(currentUsername) && mode == FriendAdapter.Mode.FRIENDS) {
            loadFriends(currentUsername, currentUserId);
        }
    }

    // make sure username is set
    private boolean ensureMe() {
        if (!TextUtils.isEmpty(currentUsername)) return true;
        Toast.makeText(this, "Resolving your profile…", Toast.LENGTH_SHORT).show();
        return false;
    }

    // load current friends
    private void loadFriends(String me, long id) {
        progressDialog.setMessage("Loading friends…");
        progressDialog.show();
        String url = BASE_V8 + "/users/" + me + "/friends";
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                arr -> {
                    progressDialog.dismiss();
                    friendList.clear();
                    parseFriendDTOList(arr, false);
                    adapter.setCurrentUsername(me);
                    adapter.setCurrentUserId(id); // keep adapter in sync in FRIENDS mode
                    refreshEmptyState();
                },
                err -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to load friends: " + err, Toast.LENGTH_SHORT).show();
                    refreshEmptyState();
                });
        queue.add(req);
    }

    // load incoming friend requests
    private void loadIncoming(String me, long id) {
        progressDialog.setMessage("Loading incoming requests…");
        progressDialog.show();
        String url = BASE_V8 + "/users/" + me + "/friends/requests/incoming";
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                arr -> {
                    progressDialog.dismiss();
                    friendList.clear();
                    parseFriendDTOList(arr, true);
                    adapter.setCurrentUsername(me);
                    adapter.setCurrentUserId(id); // already present previously
                    refreshEmptyState();
                },
                err -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to load requests: " + err, Toast.LENGTH_SHORT).show();
                    refreshEmptyState();
                });
        queue.add(req);
    }

    // load suggested friends
    private void loadSuggestions(String me, int limit, long id) {
        progressDialog.setMessage("Loading suggestions…");
        progressDialog.show();
        String url = BASE_V8 + "/users/" + me + "/friends/suggestions?limit=" + limit;
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                arr -> {
                    progressDialog.dismiss();
                    friendList.clear();
                    if (arr != null && arr.length() > 0) {
                        parseSuggestionDTOList(arr);
                    } else {
                        populateDemoSuggestions();
                        Toast.makeText(this, "Showing starter users (no mutuals yet).", Toast.LENGTH_SHORT).show();
                    }
                    adapter.setCurrentUsername(me);
                    adapter.setCurrentUserId(id); // keep adapter in sync in SUGGESTIONS mode
                    refreshEmptyState();
                },
                err -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to load suggestions: " + err, Toast.LENGTH_SHORT).show();
                    refreshEmptyState();
                });
        queue.add(req);
    }

    // parse json friend data
    private void parseFriendDTOList(JSONArray arr, boolean markIncoming) {
        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject o = arr.getJSONObject(i);
                Friend f = new Friend();
                f.setId(o.getLong("id"));
                f.setDisplayName(o.optString("displayName", o.optString("username", "")));
                f.setUsername(o.optString("username", ""));
                f.setStatus(o.optString("status", "UNKNOWN"));
                f.setIncoming(markIncoming);
                friendList.add(f);
            } catch (JSONException ignored) {}
        }
        adapter.notifyDataSetChanged();
    }

    // parse json suggestion data
    private void parseSuggestionDTOList(JSONArray arr) {
        Set<String> seen = new HashSet<>();
        if (!TextUtils.isEmpty(currentUsername)) {
            seen.add(currentUsername.toLowerCase(Locale.US));
        }
        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject o = arr.getJSONObject(i);
                long id = o.optLong("id", -1);
                String uname = o.optString("username", "");
                String display = o.optString("displayName", uname);
                int mutuals = o.optInt("mutualCount", 0);
                if (isSelf(id, uname)) continue;
                String key = uname.toLowerCase(Locale.US);
                if (seen.contains(key)) continue;
                seen.add(key);
                Friend f = new Friend();
                f.setId(id);
                f.setDisplayName(display);
                f.setUsername(uname);
                f.setStatus("SUGGESTED");
                f.setMutualCount(mutuals);
                friendList.add(f);
            } catch (JSONException ignored) {}
        }
        adapter.notifyDataSetChanged();
    }

    // check if friend object refers to current user
    private boolean isSelf(long id, String username) {
        if (currentUserId > 0 && id == currentUserId) return true;
        return !TextUtils.isEmpty(currentUsername)
                && !TextUtils.isEmpty(username)
                && username.equalsIgnoreCase(currentUsername);
    }

    // add default fake suggestions
    private void populateDemoSuggestions() {
        Set<String> seen = new HashSet<>();
        for (Friend f : friendList) seen.add(f.getUsername().toLowerCase(Locale.US));
        if (!TextUtils.isEmpty(currentUsername)) seen.add(currentUsername.toLowerCase(Locale.US));
        for (String u : STARTER_USERNAMES) {
            if (TextUtils.isEmpty(u)) continue;
            String low = u.toLowerCase(Locale.US);
            if (seen.contains(low)) continue;
            if (!TextUtils.isEmpty(currentUsername) && low.equals(currentUsername.toLowerCase(Locale.US)))
                continue;
            Friend f = new Friend();
            f.setId(-1);
            f.setDisplayName(u);
            f.setUsername(u);
            f.setStatus("SUGGESTED");
            f.setMutualCount(0);
            friendList.add(f);
        }
        adapter.notifyDataSetChanged();
    }

    // update empty state message
    private void refreshEmptyState() {
        if (friendList.isEmpty()) {
            tvNoFriends.setVisibility(View.VISIBLE);
            if (mode == FriendAdapter.Mode.FRIENDS) {
                tvNoFriends.setText("No friends yet — add someone!");
            } else if (mode == FriendAdapter.Mode.INCOMING) {
                tvNoFriends.setText("No incoming requests.");
            } else {
                tvNoFriends.setText("No suggestions right now.");
            }
        } else {
            tvNoFriends.setVisibility(View.GONE);
        }
    }

    // popup dialog for searching and adding users
    private void showSearchDialog() {
        View dialog = LayoutInflater.from(this).inflate(R.layout.dialog_search_user, null);
        EditText etSearch = dialog.findViewById(R.id.etSearchUsernameAuto);
        RecyclerView rv = dialog.findViewById(R.id.rvSearchResults);
        TextView tvEmpty = dialog.findViewById(R.id.tvEmptySearch);
        ProgressBar pb = dialog.findViewById(R.id.pbSearch);

        rv.setLayoutManager(new LinearLayoutManager(this));
        SearchAdapter sAdapter = new SearchAdapter(new ArrayList<>());
        rv.setAdapter(sAdapter);

        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle("Add / Search User")
                .setView(dialog)
                .setNegativeButton("CANCEL", (x, y) -> {})
                .create();
        d.show();

        Handler handler = new Handler(Looper.getMainLooper());
        final int[] lastQueryId = {0};

        // handle text input for search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                String q = s.toString().trim();
                tvEmpty.setText(q.isEmpty() ? "Start typing to search…" : "Searching…");
                sAdapter.clear();
                pb.setVisibility(q.isEmpty() ? View.GONE : View.VISIBLE);

                int myQueryId = ++lastQueryId[0];
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> {
                    if (myQueryId != lastQueryId[0]) return;
                    if (q.isEmpty()) { pb.setVisibility(View.GONE); return; }

                    String url = HOST + "/api/v2/users/by-username/" + Uri.encode(q);
                    JsonObjectRequest req = new JsonObjectRequest(
                            Request.Method.GET, url, null,
                            resp -> {
                                pb.setVisibility(View.GONE);
                                sAdapter.clear();
                                if (resp != null && resp.has("username")) {
                                    String uname = resp.optString("username", "");
                                    long uid = resp.optLong("id", -1L);
                                    if (isSelf(uid, uname)) {
                                        tvEmpty.setText("That’s you.");
                                        return;
                                    }
                                    UserRow u = new UserRow(uid, uname, resp.optString("email", ""));
                                    sAdapter.replaceWithSingle(u);
                                    tvEmpty.setText("");
                                } else {
                                    tvEmpty.setText("No users found.");
                                }
                            },
                            err -> {
                                pb.setVisibility(View.GONE);
                                sAdapter.clear();
                                tvEmpty.setText("No users found.");
                            }
                    );
                    queue.add(req);
                }, 350);
            }
        });

        // send friend request when user clicked
        sAdapter.setOnClickUser(u -> {
            if (!ensureMe()) return;
            sendFriendRequest(currentUsername, u.username);
            if (mode == FriendAdapter.Mode.SUGGESTIONS) loadSuggestions(currentUsername, 50, currentUserId);
            d.dismiss();
        });
    }

    // send friend request to another user
    private void sendFriendRequest(String me, String target) {
        String url = BASE_V8 + "/users/" + me + "/friends/requests/" + target;
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST, url, null,
                res -> Toast.makeText(this, "Request sent to @" + target, Toast.LENGTH_SHORT).show(),
                err -> Toast.makeText(this, "Send failed: " + err, Toast.LENGTH_SHORT).show()
        );
        queue.add(req);
    }

    // simple user holder for search results
    private static class UserRow {
        long id; String username; String email;
        UserRow(long id, String username, String email) {
            this.id = id; this.username = username; this.email = email;
        }
    }

    // view holder for search list
    private static class SearchVH extends RecyclerView.ViewHolder {
        TextView tvUser, tvEmail; Button btnAdd;
        public SearchVH(@NonNull View v) {
            super(v);
            tvUser = v.findViewById(R.id.tvUserUsername);
            tvEmail = v.findViewById(R.id.tvUserEmail);
            btnAdd = v.findViewById(R.id.btnAdd);
        }
    }

    // adapter for search results
    private static class SearchAdapter extends RecyclerView.Adapter<SearchVH> {
        interface OnClickUser { void onClick(UserRow u); }
        private final ArrayList<UserRow> data;
        private OnClickUser onClickUser;

        SearchAdapter(ArrayList<UserRow> d) { this.data = d; } // constructor
        void setOnClickUser(OnClickUser l) { this.onClickUser = l; } // set click listener
        void clear() { data.clear(); notifyDataSetChanged(); } // clear list
        void replaceWithSingle(UserRow u) { data.clear(); if (u != null) data.add(u); notifyDataSetChanged(); } // show one user

        @NonNull @Override
        public SearchVH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            View v = LayoutInflater.from(p.getContext()).inflate(R.layout.row_search_user, p, false);
            return new SearchVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchVH h, int i) {
            UserRow u = data.get(i);
            h.tvUser.setText("@" + u.username);
            h.tvEmail.setText(u.email == null ? "" : u.email);
            View.OnClickListener send = vv -> { if (onClickUser != null) onClickUser.onClick(u); };
            h.itemView.setOnClickListener(send);
            h.btnAdd.setOnClickListener(send);
        }

        @Override public int getItemCount() { return data.size(); } // number of items
    }
}