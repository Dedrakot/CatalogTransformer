package org.dedrakot.remover;

import com.google.gson.Gson;
import org.dedrakot.nn.NeuralNetwork;
import org.dedrakot.nn.Trainer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

public class NeuralNetworkTest {
    private static final Path positiveTrainPath = Paths.get("tmp/images/square");
    private static final Path negativeTrainPath = Paths.get("tmp/images/square2");
    private static final Path positivePath = Paths.get("tmp/images/flat");
    private static final Path negativePath = Paths.get("tmp/images/flat2");
    private static final double[] positiveResult = new double[]{1.0, 0.0};
    private static final double[] negativeResult = new double[]{0.0, 1.0};

    @Disabled
    @Test
    void trainAndSave() throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        NeuralNetwork nn = NeuralNetwork.image64();
        trainAndSave(nn);
    }

    @Disabled
    @Test
    void restoreAndTrain() throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        NeuralNetwork nn = Trainer.load("tmp/trained");
        NeuralNetwork.initImage64(nn);
        trainAndSave(nn);
    }

    @Test
    void match() throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        NeuralNetwork nn = Trainer.load("tmp/trained");
        NeuralNetwork.initImage64(nn);

        ImageMatcher matcher = new NNMatcher(nn);
        match(matcher, negativePath, false);
        match(matcher, positivePath, true);
    }

    private void match(ImageMatcher matcher, Path path, boolean expected) throws IOException {
        try (var ds = Files.newDirectoryStream(path)) {
            ds.forEach(f -> {
                Mat mat = Imgcodecs.imread(f.toString());
                boolean result = matcher.match(mat);
                mat.release();
                assertEquals(expected, result, f.toString());
            });
        }
    }

    @Test
    void retrainFile() throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        NeuralNetwork nn = Trainer.load("tmp/trained");
        NeuralNetwork.initImage64(nn);
        double[] correctResult = positiveResult;
        byte[] buffer = getData("tmp/images/square/22_bced5b53f7077d839c4afce0b8809a77ee7e4cde.png");
        Trainer trainer = new Trainer(nn, 0.001);
        NNMatcher matcher = new NNMatcher(nn);
        boolean correctFl = correctResult == positiveResult;
        int i = 0;
        while (matcher.match(trainer.getNp(), buffer) != correctFl) {
            train(trainer, buffer, correctResult, correctFl ? 0 : 1);
            printTrainerStat(trainer, i++);
            trainer.cleanCounters();
        }
    }

    private void trainAndSave(NeuralNetwork nn) throws IOException {
        trainAndSave(nn, loadDirectory(positiveTrainPath), loadDirectory(negativeTrainPath));
    }

    private void trainAndSave(NeuralNetwork nn, final List<byte[]> positive, final List<byte[]> negative) throws IOException {
        byte[] buffer;
        Trainer trainer = new Trainer(nn, 0.001);

        int batchSize = 50;
        int epochSize = 100;
        double negativeThreshold = 0.7;

        for (int i = 0; i < epochSize; i++) {
            for (int j = 0; j < batchSize; j++) {
                if (Math.random() < negativeThreshold) {
                    buffer = negative.get((int) (negative.size() * Math.random()));
                    train(trainer, buffer, negativeResult, 1);
                } else {
                    buffer = positive.get((int) (positive.size() * Math.random()));
                    train(trainer, buffer, positiveResult, 0);
                }
            }
            printTrainerStat(trainer, i);
            trainer.cleanCounters();
        }
        trainer.save("tmp/trained");
    }

    static List<byte[]> loadDirectory(Path path) throws IOException {
        List<byte[]> result = new ArrayList<>();
        try (var ds = Files.newDirectoryStream(path)) {
            ds.forEach(f -> result.add(getData(f.toString())));
        }
        return result;
    }

    private static byte[] getData(String fileName) {
        byte[] buffer = new byte[ImageUtils.BGR_DATA_SIZE64];
        ImageUtils.bgrData(Imgcodecs.imread(fileName), buffer);
        return buffer;
    }

    private static void train(Trainer trainer, byte[] buffer, double[] result, int max) {
        trainer.setInput(buffer);
        trainer.train(result, max);
    }

    private static void printTrainerStat(Trainer trainer, int epoch) {
        System.out.println("epoch: " + epoch + ". correct: " + trainer.right + ". from: " + trainer.counter + " error: " + trainer.errorSum);
    }

    @Disabled
    @Test
    void jsonSerialization() {
        DoubleUnaryOperator sigmoid = x -> 1 / (1 + Math.exp(-x));
        DoubleUnaryOperator dsigmoid = y -> y * (1 - y);
        NeuralNetwork nn = new NeuralNetwork(sigmoid, dsigmoid, 784, 512, 128, 32, 10);
        Gson gson = new Gson();

        String json = gson.toJson(nn);
        NeuralNetwork result = gson.fromJson(json, NeuralNetwork.class);

        assertNull(result.getActivation());
    }
}
