package cmsc420.structure.prquadtree;

import java.awt.geom.Point2D;
import cmsc420.structure.Metropole;

/**
 * Represents a leaf node of a PR Quadtree.
 */
public class LeafNode extends Node {
	/** city contained within this leaf node */
	protected Metropole m = null;

	/**
	 * Constructs and initializes a leaf node.
	 */
	public LeafNode() {
		super(Node.LEAF);
	}

	/**
	 * Gets the city contained by this node.
	 * 
	 * @return city contained by this node
	 */
	public Metropole getMetropole() {
		return m;
	}

	public Node add(Metropole newM, Point2D.Float origin, int width,
			int height) {
		if (m == null) {
			/* node is empty, add city */
			m = newM;
			return this;
		} else {
			/* node is full, partition node and then add city */
			InternalNode internalNode = new InternalNode(origin, width,
					height);
			internalNode.add(m, origin, width, height);
			internalNode.add(newM, origin, width, height);
			return internalNode;
		}
	}

	public Node remove(Metropole m, Point2D.Float origin, int width,
			int height) {
		if (this.m != m) {
			/* metropole not here */
			throw new IllegalArgumentException();
		} else {
			/* remove city, node becomes empty */
			this.m = null;
			return EmptyNode.instance;
		}
	}
}