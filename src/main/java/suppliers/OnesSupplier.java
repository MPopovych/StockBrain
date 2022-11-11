package suppliers;

public class OnesSupplier extends ConstSupplier {

	public static final OnesSupplier INSTANCE = new OnesSupplier();

	public OnesSupplier() {
		super(0);
	}

	@Override
	public float supply(int x, int y) {
		return 1f;
	}
}
