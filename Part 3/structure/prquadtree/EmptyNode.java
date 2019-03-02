package cmsc420.structure.prquadtree;

import java.awt.geom.Point2D;

import cmsc420.structure.Metropole;

/**
 * Represents an empty leaf node of a PR Quadtree.
 */
public class EmptyNode extends Node {
	
	/** empty PR Quadtree node */
	public static EmptyNode instance = new EmptyNode();
	
	/**
	 * Constructs and initializes an empty node.
	 */
	public EmptyNode() {
		super(Node.EMPTY);
	}

	public Node add(Metropole m, Point2D.Float origin, int width, int height) {
		Node leafNode = new LeafNode();
		return leafNode.add(m, origin, width, height);
	}

	public Node remove(Metropole m, Point2D.Float origin, int width,
			int height) {
		/* should never get here, nothing to remove */
		throw new IllegalArgumentException();
	}
}
