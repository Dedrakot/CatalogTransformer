package org.dedrakot.nn;

class Layer {
    double[] biases;
    double[][] weights;

    public Layer(int size, int nextSize) {
        biases = new double[size];
        weights = new double[size][nextSize];
    }

    public int size() {
        return biases.length;
    }
}
