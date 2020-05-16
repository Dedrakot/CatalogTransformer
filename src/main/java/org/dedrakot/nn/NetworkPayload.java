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
        for (int i = 0; i < buffer.length; i++) {
            input()[i] =  buffer[i] & 0xff;
        }
    }
}
