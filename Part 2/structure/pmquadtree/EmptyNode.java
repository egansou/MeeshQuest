package cmsc420.structure.pmquadtree;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.TreeSet;

import cmsc420.structure.City;

public class EmptyNode extends Node {
	
	/** empty PM Quadtree node */
	public static EmptyNode instance = new EmptyNode();
	
	/**
	 * Constructs and initializes an empty node.
	 */
	public EmptyNode() {
		super(Node.EMPTY);
	}

	public Node addCity(City city, Point2D.Float origin, int width, int height) {
		Node leafNode = new LeafNode();
		return leafNode.addCity(city, origin, width, height);
	}

	public Node removeCity(City city, Point2D.Float origin, int width,
			int height) {
		/* should never get here, nothing to remove */
		throw new IllegalArgumentException();
	}

	public Node addRoad(City start, City end, Point2D.Float origin, int width, int height) {
		Node leafNode = new LeafNode();
		return leafNode.addRoad(start, end, origin, width, height);
	}

	public Node removeRoad(City start, City end, Point2D.Float origin, int width, int height) {
		/* should never get here, nothing to remove */
		throw new IllegalArgumentException();
	}

	@Override
	public City getCity() {
		/* should never get here, nothing to remove */
		throw new IllegalArgumentException();
	}

	@Override
	public TreeSet<ArrayList<City>> getRoads() {
		/* should never get here, nothing to remove */
		throw new IllegalArgumentException();
	}
}
