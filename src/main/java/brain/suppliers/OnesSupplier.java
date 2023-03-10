package brain.suppliers;

public class OnesSupplier extends ConstSupplier {

	public static final OnesSupplier INSTANCE = new OnesSupplier();

	public OnesSupplier() {
		super(1f);
	}

}
