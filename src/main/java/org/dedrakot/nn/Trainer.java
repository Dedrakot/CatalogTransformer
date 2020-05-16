package org.dedrakot.nn;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Trainer {
    private final NeuralNetwork nn;
    private final NetworkPayload np;
    private final double learningRate;

    public int right = 0;
    public double errorSum = 0;
    public int counter = 0;

    public Trainer(NeuralNetwork nn, double learningRate) {
        this.learningRate = learningRate;
        this.nn = nn;
        this.np = nn.createPayload();
    }

    public double[] input() {
        return np.input();
    }

    public void train(double[] targets, int max) {
        nn.feedForward(np);
        double[] outputs = np.result();
        int resultMax = np.resultMax();
        if (resultMax == max) right++;
        for (int k = 0; k < outputs.length; k++) {
            errorSum += (targets[k] - outputs[k]) * (targets[k] - outputs[k]);
        }
        nn.backpropagation(np, targets, learningRate);
        counter++;
    }

    public void save(String fileName) throws IOException {
        Gson gson = new Gson();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, StandardCharsets.UTF_8))) {
            gson.toJson(nn, writer);
        }
    }

    public static NeuralNetwork load(String fileName) throws IOException {
        Gson gson = new Gson();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName, StandardCharsets.UTF_8))) {
            return gson.fromJson(reader, NeuralNetwork.class);
        }
    }

    public void cleanCounters() {
        right = 0;
        counter = 0;
        errorSum = 0;
    }

    public void setInput(byte[] buffer) {
        np.setInput(buffer);
    }

    public NetworkPayload getNp() {
        return np;
    }
}
