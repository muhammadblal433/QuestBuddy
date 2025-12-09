package com.example.androidexample;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ApplicationProvider;

import com.example.androidexample.tripplanner.ApiClient;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ApiClientTest {

    @Test
    public void apiClient_singletonWorks() {
        ApiClient c1 = ApiClient.getInstance(ApplicationProvider.getApplicationContext());
        ApiClient c2 = ApiClient.getInstance(ApplicationProvider.getApplicationContext());

        assertSame(c1, c2);
        assertNotNull(c1.getRequestQueue());
    }
}