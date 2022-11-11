package brain.ga.weights;

public class WeightGenes {
	public String weightName;
	public float[] genes;

	public WeightGenes(String weightName, float[] genes) {
		this.weightName = weightName;
		this.genes = new float[genes.length];
		System.arraycopy(genes, 0, this.genes, 0, genes.length);
	}

	public WeightGenes(String weightName, int size) {
		this.weightName = weightName;
		this.genes = new float[size];
	}

	public int getSize() {
		return genes.length;
	}

	public WeightGenes emptyCopy() {
		return new WeightGenes(weightName, getSize());
	}

	public WeightGenes copy() {
		return new WeightGenes(weightName, genes);
	}

	public void copyTo(WeightGenes destination) {
		System.arraycopy(genes, 0, destination.genes, 0, genes.length);
	}

}
