package com.example.depth_android;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.Locale;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private static final String TAG = "API_LOG";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable captureRunnable;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ToggleButton previewToggle;
    private boolean isTtsReady = false;
    private String lastSpokenLabel = null;
    private long lastSpokenTime = 0;
    private static final long SUPPRESSION_INTERVAL_MS = 15 * 1000;

    private TextToSpeech tts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        previewToggle = findViewById(R.id.previewToggle);

        tts = new TextToSpeech(this, this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            initializeCamera();
        }

        previewToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                previewView.setVisibility(View.VISIBLE);
                findViewById(R.id.blackScreenView).setVisibility(View.GONE);
                findViewById(R.id.centerImage).setVisibility(View.GONE);
            } else {
                previewView.setVisibility(View.GONE);
                findViewById(R.id.blackScreenView).setVisibility(View.VISIBLE);
                findViewById(R.id.centerImage).setVisibility(View.VISIBLE);
            }
        });
    }
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language not supported");
            } else {
                isTtsReady = true;
                Log.i("TTS", "TTS language set successfully");
            }
        } else {
            Log.e("TTS", "Initialization failed");
        }
    }

    private void speak(String text) {
        if (isTtsReady) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1");
            Log.d("TTS", "Speaking: " + text);
        } else {
            Log.w("TTS", "TTS not ready");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeCamera();
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera initialization failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview.Builder().build();

        if (previewView.getVisibility() == View.VISIBLE) {
            preview.setSurfaceProvider(previewView.getSurfaceProvider()); // Attach the preview to the surface if visible
        }

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();


        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);


        captureRunnable = new Runnable() {
            @Override
            public void run() {
                captureImage();
                handler.postDelayed(this, 2000); // Capture every 2 seconds to prevent overload
            }
        };
        handler.post(captureRunnable);
    }

    private void unbindCameraUseCases(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll(); // Unbind all use cases
    }

    private void captureImage() {
        if (imageCapture == null) return;

        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                Log.d(TAG, "Image captured successfully");
                Bitmap bitmap = imageToBitmap(image);

                if (bitmap != null) {
//                    File savedFile = saveImageToInternalStorage(bitmap);
//                    if (savedFile != null) {
//                        Log.d(TAG, "Image saved at: " + savedFile.getAbsolutePath());
//                    } else {
//                        Log.e(TAG, "Failed to save image");
//                    }
                    sendToApi(bitmap);
                } else {
                    Log.e(TAG, "Bitmap conversion failed");
                }
                image.close();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Image capture failed", exception);
            }
        });
    }

    private void sendToApi(Bitmap bitmap) {
        Log.d(TAG, "Sending image to API...");

        // Convert Bitmap to byte array
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        // Prepare Retrofit request
        RequestBody requestBody = RequestBody.create(byteArray, MediaType.parse("image/jpeg"));
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", "image.jpg", requestBody);

        // Initialize Retrofit service
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        Call<ResponseBody> call = apiService.uploadImage(filePart);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("API Response", "Received: " + responseBody);
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        float distance = (float) jsonResponse.getDouble("distance");
                         if (distance <= 500) {
                             triggerVibration();
                             String alertType = jsonResponse.getString("alert_type");
                             if ("object".equals(alertType)) {
                                 String label = jsonResponse.getString("label");
                                 if (label != null) {
                                     long currentTime = System.currentTimeMillis();
                                     boolean isNewLabel = !label.equals(lastSpokenLabel);
                                     boolean enoughTimePassed = (currentTime - lastSpokenTime) > SUPPRESSION_INTERVAL_MS;

                                     if (isNewLabel || enoughTimePassed) {
                                         String message = "Detected " + label;
                                         speak(message);
                                         lastSpokenLabel = label;
                                         lastSpokenTime = currentTime;
                                     } else {
                                         Log.d("TTS", "Suppressed repeat detection of: " + label);
                                     }
                                 }
                             }
                        } else{
                            Log.d("Not close", "No object detected");
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.e("API Error", "Request failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("API Error", "Request failed", t);
            }


        });
    }


    private Bitmap imageToBitmap(ImageProxy image) {
        int format = image.getFormat();
        Log.d(TAG, "Image format: " + format); // Log format for debugging

        if (format == ImageFormat.YUV_420_888) {
            return convertYUV420ToBitmap(image);
        } else if (format == ImageFormat.JPEG) {
            return convertJPEGToBitmap(image);
        } else {
            Log.e(TAG, "Unsupported image format: " + format);
            return null;
        }
    }

    // Convert JPEG ImageProxy to Bitmap
    private Bitmap convertJPEGToBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    // Convert YUV_420_888 ImageProxy to Bitmap
    private Bitmap convertYUV420ToBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();

        if (planes.length < 3) {
            Log.e(TAG, "Unexpected plane count: " + planes.length);
            return null;
        }

        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 100, out);
        byte[] imageBytes = out.toByteArray();

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }




    private File saveImageToInternalStorage(Bitmap bitmap) {
        File directory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyCapturedImages");
        if (!directory.exists() && !directory.mkdirs()) {
            Log.e(TAG, "Failed to create directory");
            return null;
        }

        File imageFile = new File(directory, "captured_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            return imageFile;
        } catch (IOException e) {
            Log.e(TAG, "Error saving image", e);
            return null;
        }
    }
    //vibrations function
    private void triggerVibration() {

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect effect = VibrationEffect.createWaveform(new long[]{0, 500, 1000}, -1);
                vibrator.vibrate(effect);
            } else {
                // Deprecated on newer versions
                vibrator.vibrate(new long[]{0, 500, 1000}, -1);
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(captureRunnable);
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}