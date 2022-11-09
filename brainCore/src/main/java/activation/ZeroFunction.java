package activation;
// for tests
public class ZeroFunction implements ActivationFunction {
    @Override
    public float apply(float value) {
        return 0f;
    }
}
