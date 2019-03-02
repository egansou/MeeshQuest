package cmsc420.structure.pmquadtree;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeSet;

import cmsc420.exception.ViolatesPMRulesException;
import cmsc420.structure.City;

@SuppressWarnings("serial")
public class EmptyNode extends Node implements Serializable{
	
	
	/** empty PM Quadtree node */
	public static EmptyNode instance = new EmptyNode();
	
	/**
	 * Constructs and initializes an empty node.
	 */
	public EmptyNode() {
		super(Node.EMPTY);
	}

	public Node addCity(City city, Point2D.Float origin, int width, int height, int order) 
			throws ViolatesPMRulesException {
		Node leafNode = new LeafNode();
		return leafNode.addCity(city, origin, width, height, order);
	}

	public Node removeCity(City city, Point2D.Float origin, int width,
			int height, int order) {
		/* should never get here, nothing to remove */
		throw new IllegalArgumentException();
	}

	public Node addRoad(City start, City end, Point2D.Float origin, int width, int height, int order)
			throws ViolatesPMRulesException {
		Node leafNode = new LeafNode();
		return leafNode.addRoad(start, end, origin, width, height, order);
	}

	public Node removeRoad(City start, City end, Point2D.Float origin, int width, int height, int order) {
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
