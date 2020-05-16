package org.dedrakot.remover;

import org.dedrakot.nn.NetworkPayload;
import org.dedrakot.nn.NeuralNetwork;
import org.opencv.core.Mat;

public class NNMatcher implements ImageMatcher {
    private final NeuralNetwork nn;
    private final ThreadLocal<MatcherState> np = new ThreadLocal<>();


    public NNMatcher(NeuralNetwork nn) {
        this.nn = nn;
    }

    @Override
    public boolean match(Mat other) {
        MatcherState state = getState();
        fillState(state, other);
        state.np.setInput(state.buffer);
        return match(state.np);
    }

    private boolean match(NetworkPayload np) {
        nn.feedForward(np);
        double[] result = np.result();
        return result[0] > result[1];
    }

    public boolean match(NetworkPayload np, byte[] buffer) {
        np.setInput(buffer);
        return match(np);
    }

    private void fillState(MatcherState state, Mat other) {
        Mat center = ImageUtils.center(other);
        Mat sq = ImageUtils.sqare64(center);
        center.release();
        ImageUtils.bgrData(sq, state.buffer);
    }

    private MatcherState getState() {
        MatcherState state = np.get();
        if (state == null) {
            state = new MatcherState(nn.createPayload());
            np.set(state);
        }
        return state;
    }

    private static class MatcherState {
        final byte[] buffer = new byte[ImageUtils.BGR_DATA_SIZE64];
        final NetworkPayload np;

        private MatcherState(NetworkPayload np) {
            this.np = np;
        }
    }
}
