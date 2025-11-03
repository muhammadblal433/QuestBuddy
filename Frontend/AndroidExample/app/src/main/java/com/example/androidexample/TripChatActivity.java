package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.androidexample.TripMessageResponseDTO;

import java.util.List;

public class TripChatActivity extends ComponentActivity {

    // --- Replace these with your real values or pass via Intent extras ---
    private final String baseUrl = "http://coms-3090-026.class.las.iastate.edu:8080"; // REST base (no trailing slash ok)
    private final String baseWsUrl = "";   // WS base
    private int me;                                        // current user id
    private final int tripId = 2;                                  // trip/conversation id

    private int userId;

    private TripChatViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_chat);

        //userId = getIntent().getIntExtra("userId", -1);

        /*
            if (userId == -1) {
                Toast.makeText(this, "Invalid user session", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }
         */


        me = 5;

        RecyclerView recycler = findViewById(R.id.recycler);
        EditText input = findViewById(R.id.input);
        View send = findViewById(R.id.send);

        // Volley queue (pass into ViewModel via factory)
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        // ViewModel with custom factory (provides URLs, ids, and Volley queue)
        vm = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull @Override
            @SuppressWarnings("unchecked")
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new TripChatViewModel(baseUrl, baseWsUrl, me, tripId, queue);
            }
        }).get(TripChatViewModel.class);

        // RecyclerView setup
        MessageAdapter adapter = new MessageAdapter(me);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);                // start list at the bottom like chat apps
        recycler.setLayoutManager(lm);
        recycler.setAdapter(adapter);

        // Observe messages and keep scrolled to the latest
        vm.getMessages().observe(this, (List<TripMessageResponseDTO> list) -> {
            adapter.submitList(list, () -> {
                if (list != null && !list.isEmpty()) {
                    recycler.scrollToPosition(Math.max(0, list.size() - 1));
                }
            });
        });

        // Send button
        send.setOnClickListener(v -> {
            String text = (input.getText() == null) ? "" : input.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                vm.send(text);
                input.setText("");
            }
        });

        // Infinite scroll: load more when near the top
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                LinearLayoutManager layout = (LinearLayoutManager) rv.getLayoutManager();
                if (layout != null && layout.findFirstVisibleItemPosition() <= 2) {
                    vm.loadMore();
                }
            }
        });
    }
}