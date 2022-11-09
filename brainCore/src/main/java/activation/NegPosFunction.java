package activation;

public class NegPosFunction implements ActivationFunction {
    @Override
    public float apply(float value) {
        return value > 0 ? 1 : -1;
    }
}
