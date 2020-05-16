package org.dedrakot.nn;

public class NetworkPayload {
    double[][] neurons;

    NetworkPayload(double[][] neurons) {
        this.neurons = neurons;
    }

    public double[] input() {
        return neurons[0];
    }

    public double[] result() {
        return neurons[neurons.length - 1];
    }

    public int resultMax() {
        double[] result = result();
        double maxDigitWeight = Double.MIN_VALUE;
        int ret = 0;
        for (int k = 0; k < result.length; k++) {
            if (result[k] > maxDigitWeight) {
                maxDigitWeight = result[k];
                ret = k;
            }
        }
        return ret;
    }

    public void setInput(byte[] buffer) {
        for (int i = 0, j = 0; i < buffer.length; i += 3, j++) {
            input()[j] = (buffer[i] & 0xff) | ((buffer[i + 1] & 0xff) << 8) | ((buffer[i + 2] & 0xff) << 16);
        }
    }
}
