package com.example.sanketiclipi;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.InvalidMarkException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.media.MediaRecorder.VideoSource.CAMERA;

public class ImageRecognitionActivity extends AppCompatActivity {

    private static final String MODEL_PATH = "model10.tflite";
    private static final boolean QUANT = false;
    private static final String LABEL_PATH = "label.txt";
    private static final int INPUT_SIZE = 224;

    private com.example.sanketiclipi.Classifier classifier;

    private Executor executor = Executors.newSingleThreadExecutor();

    ImageView loadImageView, navigateBack;
    TextToSpeech textToSpeech;
    private static int GALLERY=1;
    private RequestManager requestManager;
    TextView textRecognized;
    String Result;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_recognition);

        loadImageView = (ImageView) findViewById(R.id.iv);
        navigateBack = (ImageView) findViewById(R.id.navigateBack);
        textRecognized = (TextView) findViewById(R.id.textRecognized);

        //to load image into imageView
        requestManager = Glide.with(ImageRecognitionActivity.this);

//        Intent intent = getIntent();
//        Result = intent.getStringExtra("result");
//        textRecognized.setText(Result);

        Uri selectedImgUri = getIntent().getData();
//        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImgUri);

        final List<Classifier.Recognition> results;

            results = classifier.recognizeImage(getApplicationContext(),selectedImgUri);
            textRecognized.setText(results.toString());



//        Log.d("valueofbitmap", String.valueOf(bitmap));




//
//        if (selectedImgUri != null) {
//            Log.d("GalleryImageURI", "" + selectedImgUri);
//
//            requestManager
//                    .load(selectedImgUri)
//                    .into(loadImageView);
//        }

        navigateBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ImageRecognitionActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int ttsLang = textToSpeech.setLanguage(Locale.forLanguageTag("hin"));

                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                            || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "The Language is not supported!");
                    } else {
                        Log.i("TTS", "Language Supported.");
                    }
                    Log.i("TTS", "Initialization success.");
                } else {
                    Toast.makeText(getApplicationContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

//        textRecognized.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                textToSpeech.speak(textRecognized.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
//            }
//        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                textToSpeech.speak(textRecognized.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
            }
        }, 800);

        initTensorFlowAndLoadModel();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_PATH,
                            LABEL_PATH,
                            INPUT_SIZE,
                            QUANT);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }
}



