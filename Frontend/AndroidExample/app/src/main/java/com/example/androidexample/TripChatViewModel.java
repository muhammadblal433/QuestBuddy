package com.example.androidexample;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.android.volley.RequestQueue;

import java.util.*;


public class TripChatViewModel extends ViewModel {
    private final TripChatApi api;
    private final long me;
    private final long tripId;

    private final MutableLiveData<List<TripMessageResponseDTO>> messages = new MutableLiveData<>(new ArrayList<>());
    private Long oldestIdLoaded = null;

    private final TripWebSocket wsOrNull;


    public TripChatViewModel(String baseUrl, String baseWsUrl, long me, long tripId, RequestQueue queue) {
        this.api = new TripChatApi(baseUrl, queue);
        this.me = me; this.tripId = tripId;

        // Only create/connect WS if we have a valid ws:// or wss:// URL
        if (baseWsUrl != null && (baseWsUrl.startsWith("ws://") || baseWsUrl.startsWith("wss://"))) {
            wsOrNull = new TripWebSocket(baseWsUrl, me, tripId, new TripWebSocket.WsListener() {
                @Override public void onOpen() {}
                @Override public void onMessage(String json) { reloadLatest(); }
                @Override public void onClosed(int code, String reason) {}
                @Override public void onFailure(Throwable t) { /* no crash; maybe log */ }
            });
        } else {
            wsOrNull = null;
        }

        start();
    }

    public LiveData<List<TripMessageResponseDTO>> getMessages() { return messages; }

    private void start() {
        reloadLatest();           // REST still works without WS
        try { if (wsOrNull != null) wsOrNull.connect(); } catch (Throwable ignored) {} }


    private void reloadLatest() {
        api.listMessages(me, tripId, null, 50, new TripChatApi.Callback<List<TripMessageResponseDTO>>() {
            @Override public void onSuccess(List<TripMessageResponseDTO> list) {
                list.sort((a,b) -> Long.compare(a.getId(), b.getId()));
                messages.postValue(list);
                if (!list.isEmpty()) {
                    long min = list.get(0).getId();
                    for (TripMessageResponseDTO m : list) if (m.getId() < min) min = m.getId();
                    oldestIdLoaded = min;
                }
            }
            @Override public void onError(Throwable t) { /* optional: expose error */ }
        });
    }

    public void loadMore() {
        if (oldestIdLoaded == null) return;
        api.listMessages(me, tripId, oldestIdLoaded, 50, new TripChatApi.Callback<List<TripMessageResponseDTO>>() {
            @Override public void onSuccess(List<TripMessageResponseDTO> page) {
                List<TripMessageResponseDTO> cur = new ArrayList<>(messages.getValue());
                cur.addAll(0, page);
                cur.sort((a,b) -> Long.compare(a.getId(), b.getId()));
                messages.postValue(cur);
                if (!page.isEmpty()) {
                    long min = page.get(0).getId();
                    for (TripMessageResponseDTO m : page) if (m.getId() < min) min = m.getId();
                    oldestIdLoaded = min;
                }
            }
            @Override public void onError(Throwable t) { }
        });
    }

    public void send(String content) {
        List<TripMessageResponseDTO> cur = new ArrayList<>(messages.getValue());
        long tempId = -System.currentTimeMillis();
        TripMessageResponseDTO optimistic = new TripMessageResponseDTO(
                tempId, tripId, me, content,
                null, null, isoNow(), null, false, null,
                null, null, null, false, null, null);
        cur.add(optimistic);
        messages.postValue(cur);


        TripMessageCreateDTO body = new TripMessageCreateDTO(content, null, null, UUID.randomUUID().toString(), isoNow());
        api.post(me, tripId, body, new TripChatApi.Callback<TripMessageResponseDTO>() {
            @Override public void onSuccess(TripMessageResponseDTO server) {
                List<TripMessageResponseDTO> list = new ArrayList<>(messages.getValue());
                for (int i = 0; i < list.size(); i++) if (list.get(i).getId() == tempId) { list.set(i, server); break; }
                messages.postValue(list);
            }
            @Override public void onError(Throwable t) {
                List<TripMessageResponseDTO> list = new ArrayList<>(messages.getValue());
                for (int i = 0; i < list.size(); i++) if (list.get(i).getId() == tempId) {
                    TripMessageResponseDTO fail = new TripMessageResponseDTO(
                            tempId, tripId, me, "Failed to send: " + content,
                            null, null, isoNow(), null, false, null,
                            null, null, null, false, null, null);
                    list.set(i, fail); break;
                }
                messages.postValue(list);
            }
        });
    }

    private static String isoNow() {
        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US);
        return f.format(new java.util.Date());
    }
}
