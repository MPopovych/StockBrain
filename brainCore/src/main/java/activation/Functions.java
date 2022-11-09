package activation;

@Deprecated
public class Functions {

	public static final ReLuFunction ReLu = new ReLuFunction();
	public static final LeakyReLuFunction LeReLu = new LeakyReLuFunction();
	public static final NegPosFunction NegPos = new NegPosFunction();
	public static final NegZeroPosFunction NegZeroPos = new NegZeroPosFunction();
	public static final BinaryStepFunction ZeroOne = new BinaryStepFunction();

}
