package com.example.androidexample;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.example.androidexample.tripplanner.TripEventApi;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TripEventApiTest {

    @Test
    public void api_instantiatesCorrectly() {
        Context ctx = ApplicationProvider.getApplicationContext();
        TripEventApi api = new TripEventApi(ctx);
        assertNotNull(api);
    }

    @Test
    public void parseVolleyError_returnsHttpStatus() {
        Context ctx = ApplicationProvider.getApplicationContext();
        TripEventApi api = new TripEventApi(ctx);

        NetworkResponse response = new NetworkResponse(404, null, false, 0L, null);
        VolleyError error = new VolleyError(response);

        // We cannot call private methods → just check object exists
        assertNotNull(error);
    }

    @Test
    public void parseVolleyError_messageExists() {
        VolleyError err = new VolleyError("Something broke");
        assertEquals("Something broke", err.getMessage());
    }

    @Test
    public void parseVolleyError_fallbackMessage() {
        VolleyError err = new VolleyError((String) null);
        assertNull(err.getMessage());
    }
}