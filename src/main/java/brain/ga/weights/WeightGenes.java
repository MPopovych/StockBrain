package brain.ga.weights;

public class WeightGenes {
	public String weightName;
	public float[] genes;
	public int width;
	public int height;
	public int callOrder;

	public WeightGenes(String weightName, float[] genes, int width, int height, int callOrder) {
		this.weightName = weightName;
		this.genes = new float[genes.length];
		this.width = width;
		this.height = height;
		this.callOrder = callOrder;
		System.arraycopy(genes, 0, this.genes, 0, genes.length);
	}

	public WeightGenes(String weightName, int width, int height, int callOrder) {
		this.weightName = weightName;
		this.genes = new float[width * height];
		this.width = width;
		this.height = height;
		this.callOrder = callOrder;
	}

	public int getSize() {
		return genes.length;
	}

	public WeightGenes emptyCopy() {
		return new WeightGenes(weightName, width, height, callOrder);
	}

	public WeightGenes copy() {
		return new WeightGenes(weightName, genes, width, height, callOrder);
	}

	public void copyTo(WeightGenes destination) {
		System.arraycopy(genes, 0, destination.genes, 0, genes.length);
	}

}
