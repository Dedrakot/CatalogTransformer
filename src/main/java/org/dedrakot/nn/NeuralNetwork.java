package org.dedrakot.nn;

import java.util.function.DoubleUnaryOperator;

public class NeuralNetwork {
    private Layer[] layers;
    private transient DoubleUnaryOperator activation;
    private transient DoubleUnaryOperator derivative;


    public static NeuralNetwork image64() {
        DoubleUnaryOperator sigmoid = x -> 1 / (1 + Math.exp(-x));
        DoubleUnaryOperator dsigmoid = y -> y * (1 - y);
        return new NeuralNetwork(sigmoid, dsigmoid, 4096, 1024, 256, 64, 16, 4, 2);
    }

    public static void initImage64(NeuralNetwork nn) {
        DoubleUnaryOperator sigmoid = x -> 1 / (1 + Math.exp(-x));
        DoubleUnaryOperator dsigmoid = y -> y * (1 - y);
        nn.setActivation(sigmoid);
        nn.setDerivative(dsigmoid);
    }

    public NeuralNetwork() {

    }

    public NeuralNetwork(DoubleUnaryOperator activation, DoubleUnaryOperator derivative, int... sizes) {
        this.activation = activation;
        this.derivative = derivative;
        layers = new Layer[sizes.length];
        for (int i = 0; i < sizes.length; i++) {
            int nextSize = 0;
            if (i < sizes.length - 1) nextSize = sizes[i + 1];
            layers[i] = new Layer(sizes[i], nextSize);
            for (int j = 0; j < sizes[i]; j++) {
                layers[i].biases[j] = Math.random() * 2.0 - 1.0;
                for (int k = 0; k < nextSize; k++) {
                    layers[i].weights[j][k] = Math.random() * 2.0 - 1.0;
                }
            }
        }
    }

    public NetworkPayload createPayload() {
        double[][] neurons = new double[layers.length][];
        for (int i = 0; i < layers.length; i++) {
            neurons[i] = new double[layers[i].size()];
        }
        return new NetworkPayload(neurons);
    }

    public void setActivation(DoubleUnaryOperator activation) {
        this.activation = activation;
    }

    public void setDerivative(DoubleUnaryOperator derivative) {
        this.derivative = derivative;
    }

    public DoubleUnaryOperator getActivation() {
        return activation;
    }

    public DoubleUnaryOperator getDerivative() {
        return derivative;
    }

    public void feedForward(NetworkPayload networkPayload) {
        for (int i = 1; i < layers.length; i++) {
            Layer l = layers[i - 1];
            Layer l1 = layers[i];
            double[] neurons = networkPayload.neurons[i-1];
            double[] neurons1 = networkPayload.neurons[i];
            for (int j = 0; j < l1.size(); j++) {
                neurons1[j] = 0;
                for (int k = 0; k < l.size(); k++) {
                    neurons1[j] += neurons[k] * l.weights[k][j];
                }
                neurons1[j] += l1.biases[j];
                neurons1[j] = activation.applyAsDouble(neurons1[j]);
            }
        }
    }

    public void backpropagation(NetworkPayload networkPayload, double[] targets, double learningRate) {
        double[] result = networkPayload.result();
        double[] errors = new double[result.length];
        for (int i = 0; i < layers[layers.length - 1].size(); i++) {
            errors[i] = targets[i] - result[i];
        }
        for (int k = layers.length - 2; k >= 0; k--) {
            Layer l = layers[k];
            Layer l1 = layers[k + 1];
            double[] errorsNext = new double[l.size()];
            double[] gradients = new double[l1.size()];
            double[] neurons1 = networkPayload.neurons[k + 1];
            for (int i = 0; i < l1.size(); i++) {
                gradients[i] = errors[i] * derivative.applyAsDouble(neurons1[i]);
                gradients[i] *= learningRate;
            }
            double[][] deltas = new double[l1.size()][l.size()];
            double[] neurons = networkPayload.neurons[k];
            for (int i = 0; i < l1.size(); i++) {
                for (int j = 0; j < l.size(); j++) {
                    deltas[i][j] = gradients[i] * neurons[j];
                }
            }
            for (int i = 0; i < l.size(); i++) {
                errorsNext[i] = 0;
                for (int j = 0; j < l1.size(); j++) {
                    errorsNext[i] += l.weights[i][j] * errors[j];
                }
            }
            errors = new double[l.size()];
            System.arraycopy(errorsNext, 0, errors, 0, l.size());
            double[][] weightsNew = new double[l.weights.length][l.weights[0].length];
            for (int i = 0; i < l1.size(); i++) {
                for (int j = 0; j < l.size(); j++) {
                    weightsNew[j][i] = l.weights[j][i] + deltas[i][j];
                }
            }
            l.weights = weightsNew;
            for (int i = 0; i < l1.size(); i++) {
                l1.biases[i] += gradients[i];
            }
        }
    }
}
