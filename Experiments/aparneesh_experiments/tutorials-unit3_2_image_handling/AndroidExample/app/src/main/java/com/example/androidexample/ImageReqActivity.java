package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageReqActivity extends AppCompatActivity {
    private Button btnImageReq;
    private Button btnCancel;
    private Button btnSave;
    private ImageView imageView;
    private ProgressBar progressBar; // loading bar

    public static final String URL_IMAGE = "http://sharding.org/outgoing/temp/testimg3.jpg";

    private static final String REQ_TAG = "image_request";
    private Bitmap lastBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_req);

        btnImageReq = findViewById(R.id.btnImageReq);
        btnCancel = findViewById(R.id.btnCancel); // cancel image request
        btnSave = findViewById(R.id.btnSave);     // save image to device
        imageView = findViewById(R.id.imgView);
        progressBar = findViewById(R.id.progressBar); // loading bar

        btnImageReq.setOnClickListener(v -> makeImageRequest());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> cancelImageRequest());
        if (btnSave != null) btnSave.setOnClickListener(v -> saveBitmapToDownloads());
    }

    private void showLoading(boolean loading) {
        if (progressBar != null) progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (imageView != null) imageView.setForeground(loading ? new ColorDrawable(0x33FFFFFF) : null);
        if (btnImageReq != null) btnImageReq.setEnabled(!loading);
        if (btnCancel != null) btnCancel.setEnabled(loading);
    }

    /**
     * Making image request with explicit RetryPolicy and cancellation tag.
     */
    private void makeImageRequest() {
        showLoading(true);

        ImageRequest imageRequest = new ImageRequest(
                URL_IMAGE,
                response -> {
                    showLoading(false);
                    lastBitmap = response;
                    imageView.setImageBitmap(response);
                    Toast.makeText(getApplicationContext(), "Image loaded", Toast.LENGTH_SHORT).show();
                },
                0,
                0,
                ImageView.ScaleType.CENTER_CROP,
                Bitmap.Config.ARGB_8888,
                error -> {
                    showLoading(false);
                    Log.e("Volley Error", String.valueOf(error));
                }
        );

        // Stronger retry policy
        imageRequest.setRetryPolicy(new DefaultRetryPolicy(
                6000,
                2,
                1.5f
        ));

        imageRequest.setTag(REQ_TAG);
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(imageRequest);
    }

    private void cancelImageRequest() {
        VolleySingleton.getInstance(getApplicationContext()).getRequestQueue().cancelAll(REQ_TAG);
        showLoading(false);
        Toast.makeText(this, "Request cancelled", Toast.LENGTH_SHORT).show();
    }

    private void saveBitmapToDownloads() {
        if (lastBitmap == null) {
            Toast.makeText(this, "No image to save yet", Toast.LENGTH_SHORT).show();
            return;
        }
        File outDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!outDir.exists() && !outDir.mkdirs()) {
            Toast.makeText(this, "Unable to access Downloads", Toast.LENGTH_LONG).show();
            return;
        }
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + "_volley.jpg";
        File outFile = new File(outDir, name);
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            lastBitmap.compress(Bitmap.CompressFormat.JPEG, 92, fos);
            Toast.makeText(this, "Saved to: " + outFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("SaveBitmap", "Error saving bitmap", e);
            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Prevent leaks
        VolleySingleton.getInstance(getApplicationContext()).getRequestQueue().cancelAll(REQ_TAG);
    }
}