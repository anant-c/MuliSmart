package com.example.mulismarthome;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;


import java.io.BufferedReader;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.net.URLEncoder;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_MICROPHONE = 1;
    private FloatingActionButton mic;

    private RecyclerView roomRecView;
    private RelativeLayout parent;
    private Spinner language;

    ArrayList<Room> roomList;

    RoomRecViewAdapter adapter;


    private String selectedLanguageCode = "en"; // Default language
    private static final String AZURE_TRANSLATION_API_KEY = "abe303d36a014c448b77164a979590f6";
    private static final String AZURE_TRANSLATION_ENDPOINT = "https://api.cognitive.microsofttranslator.com/";

    private LiteModel model;

    private TextToSpeech textToSpeech;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parent = findViewById(R.id.main);
        mic =findViewById(R.id.micBtn);
        language = findViewById(R.id.languagelist);

        roomRecView = findViewById(R.id.roomsRecView);

        // Check if the microphone permission is granted
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE);
        }

        mic.setOnClickListener(v -> startSpeechRecognition());

//        textToSpeech = new TextToSpeech(this, (TextToSpeech.OnInitListener) this);

        //language select logic
        language.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLanguage = language.getSelectedItem().toString();
                Toast.makeText(MainActivity.this,   language.getSelectedItem().toString()+ " Selected", Toast.LENGTH_SHORT).show();
                selectedLanguageCode = getLanguageCode(selectedLanguage);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //each room
        roomList = new ArrayList<>();

        roomList.add(new Room("Room 1","https://img.icons8.com/?size=100&id=67474&format=png&color=000000","https://img.icons8.com/?size=100&id=4OQ25MMXRSB0&format=png&color=000000"));
        roomList.add(new Room("Room 2","https://img.icons8.com/?size=100&id=67474&format=png&color=000000","https://img.icons8.com/?size=100&id=4OQ25MMXRSB0&format=png&color=000000"));
        roomList.add(new Room("Room 3","https://img.icons8.com/?size=100&id=67474&format=png&color=000000","https://img.icons8.com/?size=100&id=4OQ25MMXRSB0&format=png&color=000000"));
        roomList.add(new Room("Drawing Room","https://img.icons8.com/?size=100&id=tM2vH3oofz73&format=png&color=000000"));
        roomList.add(new Room("Master Control","https://img.icons8.com/?size=100&id=67370&format=png&color=000000","https://img.icons8.com/?size=100&id=12132&format=png&color=000000"));

        adapter = new RoomRecViewAdapter(this);

        adapter.setRooms(roomList);

        roomRecView.setAdapter(adapter);

        roomRecView.setLayoutManager(new LinearLayoutManager(this));


    }

    private void startSpeechRecognition() {
        Log.d(TAG, "Starting speech recognition");
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguageCode); // Set the selected language code
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something...");

        try {
            startActivityForResult(intent, REQUEST_MICROPHONE);
        } catch (Exception e) {
            Log.e(TAG, "Error starting speech recognition: " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_MICROPHONE) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null && !result.isEmpty()) {
                    String recognizedText = result.get(0);
                    Log.d(TAG, "Recognized text: " + recognizedText);
                    Toast.makeText(this, "You said: " + recognizedText, Toast.LENGTH_LONG).show();
                    translateAndProcessText(recognizedText);
                } else {
                    Log.d(TAG, "No recognized text found");
                    Toast.makeText(this, "No recognized text found", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.d(TAG, "Speech recognition canceled or failed");
                Toast.makeText(this, "Speech recognition canceled or failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void translateAndProcessText(String text) {
        new TranslateText().execute(text);
    }
    private class TranslateText extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            String textToTranslate = params[0];
            String translatedText = null;

            OkHttpClient client = new OkHttpClient();
            String url = null;
                url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=hi&tl=en&dt=t&q=" + textToTranslate;

            Log.d(TAG, "URL: " + url);
            Request request = new Request.Builder()
                .url(url)
                .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String URL_downloaded = response.request().url().toString();
            Log.d(TAG, "URL_downloaded: " + URL_downloaded);
            try {
                translatedText = fetchUrlContent(URL_downloaded);
                Log.d(TAG, " translated: " + translatedText); // Print JSON array with indentation
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }


//            translatedText = "hiiiiiiiiii";




            return translatedText;
        }
        protected String fetchUrlContent(String urlString) throws IOException, JSONException {
            // Create HttpClient
            URL url = new URL(urlString);

            // Create the request
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            //connection.setRequestMethod("GET");

            // Send the request and get the response
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            // Parse the response body
            org.json.JSONArray jsonArray = new JSONArray(response.toString());

            // Get the translated text
            String translatedText = jsonArray.getJSONArray(0).getJSONArray(0).getString(0);

            // Print the translated text
            System.out.println(translatedText);
            return translatedText;
        }
        @Override
        protected void onPostExecute(String translatedText) {
            super.onPostExecute(translatedText);
//            Log.d(TAG, "Translated text: " + translatedText);
            Toast.makeText(MainActivity.this, "Translated text: "+ translatedText, Toast.LENGTH_SHORT).show();
            procesTranslatedText(translatedText);
//            speak(translatedText);
        }
    }

    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Handle the error
                System.out.println("Language is not supported or data is missing");
            }
        } else {
            // Initialization failed
            System.out.println("Initialization failed");
        }
    }

    private void speak(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private void processTranslatedText(String text) {

        try {
            model = new LiteModel(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int predictedIntent = model.predict(text);

        Log.d(TAG, "Predicted Intent: " + predictedIntent);
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_MICROPHONE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Microphone permission granted");
        } else {
            Log.d(TAG, "Microphone permission denied");
            Toast.makeText(this, "Microphone permission is required for speech recognition", Toast.LENGTH_LONG).show();
        }
    }


    private String getLanguageCode(String language) {
        switch (language.toLowerCase()) {
            case "kannada":
                return "kn";
            case "gujarati":
                return "gu";
            case "marathi":
                return "mr";
            case "bengali":
                return "bn";
            case "hindi":
                return "hi";
            default:
                return "en";
        }
    }

    private void showSnackbar(){
        Snackbar.make(parent,"Say the command...",Snackbar.LENGTH_LONG).setTextColor(Color.YELLOW).show();
    }

    public void procesTranslatedText(String text){
        String lightOn = "https://img.icons8.com/?size=100&id=67370&format=png&color=000000";
        String lightOff = "https://img.icons8.com/?size=100&id=67474&format=png&color=000000";
        String fanOn = "https://img.icons8.com/?size=100&id=12132&format=png&color=000000";
        String fanOff = "https://img.icons8.com/?size=100&id=4OQ25MMXRSB0&format=png&color=000000";
        String tvOn = "https://img.icons8.com/?size=100&id=tM2vH3oofz73&format=png&color=000000";
        String tvOff = "https://img.icons8.com/?size=100&id=P9ynVIBEIW0z&format=png&color=000000";



        if ((text.contains("one") || text.contains("1") || text.contains("first") ) && (text.contains("on") || text.contains("start")) && (text.contains("light") || text.contains("lights"))) {
            // Change image for Room 1
            Room room1 = roomList.get(0);
            room1.setImageUrl(lightOn);
        } else if ((text.contains("one") || text.contains("1")|| text.contains("first")) && (text.contains("off") || text.contains("stop")) && (text.contains("light") || text.contains("lights"))){
            Room room1 = roomList.get(0);
            room1.setImageUrl(lightOff);
        }
        else if ((text.contains("one") || text.contains("1")|| text.contains("first")) && (text.contains("off") || text.contains("stop")) && (text.contains("fan") || text.contains("fans"))){
            Room room1 = roomList.get(0);
            room1.setImageUrl2(fanOff);
        }
        else if ((text.contains("one") || text.contains("1")|| text.contains("first")) && (text.contains("on") || text.contains("start")) && (text.contains("fan") || text.contains("fans"))){
            Room room1 = roomList.get(0);
            room1.setImageUrl2(fanOn);
        }
        else if ((text.contains("two") || text.contains("2")|| text.contains("second")) && (text.contains("on") || text.contains("start")) && (text.contains("light") || text.contains("lights"))) {
            // Change image for Room 1
            Room room2 = roomList.get(1);
            room2.setImageUrl(lightOn);
        } else if ((text.contains("two") || text.contains("2")|| text.contains("second")) && (text.contains("off") || text.contains("stop")) && (text.contains("light") || text.contains("lights"))){
            Room room2 = roomList.get(1);
            room2.setImageUrl(lightOff);
        }
        else if ((text.contains("two") || text.contains("2")||text.contains("second")) && (text.contains("off") || text.contains("stop")) && (text.contains("fan") || text.contains("fans"))){
            Room room2 = roomList.get(1);
            room2.setImageUrl2(fanOff);
        }
        else if ((text.contains("two") || text.contains("2")|| text.contains("second")) && (text.contains("on") || text.contains("start")) && (text.contains("fan") || text.contains("fans"))){
            Room room2 = roomList.get(1);
            room2.setImageUrl2(fanOn);
        }
        else if ((text.contains("three") || text.contains("3")|| text.contains("third")) && (text.contains("on") || text.contains("start")) && (text.contains("light") || text.contains("lights"))) {
            // Change image for Room 1
            Room room3 = roomList.get(2);
            room3.setImageUrl(lightOn);
        } else if ((text.contains("three") || text.contains("3")|| text.contains("third")) && (text.contains("off") || text.contains("stop")) && (text.contains("light") || text.contains("lights"))){
            Room room3 = roomList.get(2);
            room3.setImageUrl(lightOff);
        }
        else if ((text.contains("three") || text.contains("3")|| text.contains("third")) && (text.contains("off") || text.contains("stop")) && (text.contains("fan") || text.contains("fans"))){
            Room room3 = roomList.get(2);
            room3.setImageUrl2(fanOff);
        }
        else if ((text.contains("three") || text.contains("3")|| text.contains("third")) && (text.contains("on") || text.contains("start")) && (text.contains("fan") || text.contains("fans"))){
            Room room3 = roomList.get(2);
            room3.setImageUrl2(fanOn);
        }
        else if (text.contains("all") && (text.contains("on") || text.contains("start")) && (text.contains("light") || text.contains("lights"))) {
            // Change image for Room 1
            Room master = roomList.get(4);
            master.setImageUrl(lightOn);
        } else if (text.contains("all") && (text.contains("off") || text.contains("stop")) && (text.contains("light") || text.contains("lights"))){
            Room master = roomList.get(4);
            master.setImageUrl(lightOff);
        }
        else if (text.contains("all") && (text.contains("off") || text.contains("stop")) && (text.contains("fan") || text.contains("fans") || text.contains("wings"))){
            Room master = roomList.get(4);
            master.setImageUrl2(fanOff);
        }
        else if (text.contains("all") && (text.contains("on") || text.contains("start")) && (text.contains("fan") || text.contains("fans") || text.contains("wings"))){
            Room master = roomList.get(4);
            master.setImageUrl2(fanOn);
        }
        else if ((text.contains("on") || text.contains("start")) && text.contains("tv")) {
            // Change image for Room 1
            Room drawing = roomList.get(3);
            drawing.setImageUrl(tvOn);
        } else if ((text.contains("off") || text.contains("stop")) && text.contains("tv")){
            Room drawing = roomList.get(3);
            drawing.setImageUrl(tvOff);
        }
        else if ((text.contains("off") || text.contains("stop")) && text.contains("television")){
            Room drawing = roomList.get(3);
            drawing.setImageUrl(tvOff);
        }
        else if ((text.contains("on") || text.contains("start")) && text.contains("television")){
            Room drawing = roomList.get(3);
            drawing.setImageUrl(tvOn);
        }
        else{
            Toast.makeText(this, "Invalid command", Toast.LENGTH_SHORT).show();
        }
        adapter.notifyDataSetChanged();
    }


}

