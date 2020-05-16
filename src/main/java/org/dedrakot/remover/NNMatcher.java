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
        setInput(state, other);
        nn.feedForward(state.np);
        double[] result = state.np.result();
        return result[0] > result[1];
    }

    private void setInput(MatcherState state, Mat other) {
        Mat center = ImageUtils.center(other);
        Mat sq = ImageUtils.sqare64(center);
        center.release();
        ImageUtils.grayData(sq, state.buffer);
        state.np.setInput(state.buffer);
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
        final byte[] buffer = new byte[64 * 64];
        final NetworkPayload np;

        private MatcherState(NetworkPayload np) {
            this.np = np;
        }
    }
}
