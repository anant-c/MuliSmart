package com.example.mulismarthome;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LiteModel {
    private Interpreter interpreter;
    private Tokenizer tokenizer;
    private static final int MAX_LENGTH = 100;

    public LiteModel(Context context) throws IOException {
        interpreter = new Interpreter(loadModelFile(context));
        System.out.println("here2");
        tokenizer = new Tokenizer(context, "tokenizer_config.json");
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("model_new.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public int predict(String text) {
        // Tokenize and pad the input text
        int[] input = tokenizer.textToSequence(text, MAX_LENGTH);
        System.out.println("here1");
        // Prepare input tensor
        float[][] inputTensor = new float[1][input.length];
        for (int i = 0; i < input.length; i++) {
            inputTensor[0][i] = input[i];
        }

        // Prepare output tensor
        float[][] outputTensor = new float[1][tokenizer.getNumClasses()];

        // Run inference
        interpreter.run(inputTensor, outputTensor);

        // Get the predicted class
        int predictedClass = argmax(outputTensor[0]);
        return predictedClass;
    }

    private int argmax(float[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}

