package com.example.androidexample.trips;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.util.List;

public class TripInvitesActivity extends AppCompatActivity {

    private int userId;
    private TripInviteAdapter adapter;
    private View tvEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_invites);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Trip Invitations");
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        userId = getIntent().getIntExtra("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "Missing userId", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        RecyclerView recycler = findViewById(R.id.recyclerInvites);
        tvEmpty = findViewById(R.id.tvEmptyInvites);

        adapter = new TripInviteAdapter(new TripInviteAdapter.Listener() {
            @Override
            public void onAccept(TripInviteDTO invite) {
                TripMembershipAPI.approveInvite(
                        TripInvitesActivity.this,
                        userId,
                        invite.tripId,
                        new TripMembershipAPI.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(TripInvitesActivity.this,
                                        "Invite accepted", Toast.LENGTH_SHORT).show();
                                loadInvites();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(TripInvitesActivity.this,
                                        "Accept failed: " + message, Toast.LENGTH_LONG).show();
                            }
                        }
                );
            }

            @Override
            public void onDecline(TripInviteDTO invite) {
                TripMembershipAPI.declineInvite(
                        TripInvitesActivity.this,
                        userId,
                        invite.tripId,
                        new TripMembershipAPI.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(TripInvitesActivity.this,
                                        "Invite declined", Toast.LENGTH_SHORT).show();
                                loadInvites();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(TripInvitesActivity.this,
                                        "Decline failed: " + message, Toast.LENGTH_LONG).show();
                            }
                        }
                );
            }
        });

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        loadInvites();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void loadInvites() {
        TripMembershipAPI.listMyInvites(
                this,
                userId,
                new TripMembershipAPI.InvitesCallback() {
                    @Override
                    public void onSuccess(List<TripInviteDTO> invites) {
                        adapter.submit(invites);
                        tvEmpty.setVisibility(
                                (invites == null || invites.isEmpty()) ? View.VISIBLE : View.GONE
                        );
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(TripInvitesActivity.this,
                                "Failed to load invitations: " + message,
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
    }
}
