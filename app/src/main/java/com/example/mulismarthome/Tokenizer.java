package com.example.mulismarthome;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Tokenizer {
    private Map<String, Integer> wordIndex;
    private static final int OOV_TOKEN = 1;
    private int numClasses;

    public Tokenizer(Context context, String configFileName) throws IOException {
        wordIndex = new HashMap<>();
        loadTokenizerConfig(context, configFileName);
    }

    private void loadTokenizerConfig(Context context, String configFileName) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(configFileName)));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        reader.close();
        try {
            JSONObject jsonObject = new JSONObject(jsonBuilder.toString());
            JSONObject wordIndexJson = jsonObject.getJSONObject("word_index");
            for (Iterator<String> it = wordIndexJson.keys(); it.hasNext(); ) {
                String key = it.next();
                int value = wordIndexJson.getInt(key);
                wordIndex.put(key, value);
            }
            numClasses = jsonObject.getInt("num_classes");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int[] textToSequence(String text, int maxLength) {
        String[] words = text.split(" ");
        int[] sequence = new int[maxLength];
        Arrays.fill(sequence, 0);  // Pad with zeros
        for (int i = 0; i < Math.min(words.length, maxLength); i++) {
            sequence[i] = wordIndex.getOrDefault(words[i], OOV_TOKEN);
        }
        return sequence;
    }

    public int getNumClasses() {
        return numClasses;
    }
}
