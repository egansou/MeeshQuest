package cmsc420.structure.pmquadtree;

import java.awt.geom.*;
import java.util.ArrayList;
import java.util.TreeSet;

import cmsc420.structure.City;

/**
 * Node abstract class for a PM Quadtree. A node can either be an empty
 * node, a leaf node, or an internal node.
 */
public abstract class Node {
	/** Type flag for an empty PM Quadtree node */
	public static final int EMPTY = 0;

	/** Type flag for a PM Quadtree leaf node */
	public static final int LEAF = 1;

	/** Type flag for a PM Quadtree internal node */
	public static final int INTERNAL = 2;

	/** type of PM Quadtree node (either empty, leaf, or internal) */
	protected final int type;

	/**
	 * Constructor for abstract Node class.
	 * 
	 * @param type
	 *            type of the node (either empty, leaf, or internal)
	 */
	protected Node(final int type) {
		this.type = type;
	}

	/**
	 * Adds a city to the node. If an empty node, the node becomes a leaf
	 * node. If a leaf node already, the leaf node becomes an internal node
	 * and both cities are added to it. If an internal node, the city is
	 * added to the child whose quadrant the city is located within.
	 * 
	 * @param city
	 *            city to be added to the PM Quadtree
	 * @param origin
	 *            origin of the rectangular bounds of this node
	 * @param width
	 *            width of the rectangular bounds of this node
	 * @param height
	 *            height of the rectangular bounds of this node
	 * @return this node after the city has been added
	 */
	public abstract Node addCity(City city, Point2D.Float origin, int width,
			int height);
	
	/**
	 * Gets the city contained by this node.
	 * 
	 * @return city contained by this node
	 */
	public abstract City getCity();
	
	/**
	 * Gets the city contained by this node.
	 * 
	 * @return city contained by this node
	 */
	public abstract TreeSet<ArrayList<City>> getRoads();
	
	/**
	 * Adds a road to the node. if the road intersects with any 
	 * side of the rectangle, then add road to the node
	 * 
	 * @param road
	 *            road to be added to the PM Quadtree
	 * @param origin
	 *            origin of the rectangular bounds of this node
	 * @param width
	 *            width of the rectangular bounds of this node
	 * @param height
	 *            height of the rectangular bounds of this node
	 * @return this node after the road has been added
	 */
	public abstract Node addRoad(City start, City end, Point2D.Float origin, int width,
			int height);

	/**
	 * Removes a city from the node. If this is a leaf node and the city is
	 * contained in it, the city is removed and the node becomes a leaf
	 * node. If this is an internal node, then the removal command is passed
	 * down to the child node whose quadrant the city falls in.
	 * 
	 * @param city
	 *            city to be removed
	 * @param origin
	 *            origin of the rectangular bounds of this node
	 * @param width
	 *            width of the rectangular bounds of this node
	 * @param height
	 *            height of the rectangular bounds of this node
	 * @return this node after the city has been removed
	 */
	public abstract Node removeCity(City city, Point2D.Float origin, int width,
			int height);
	
	/**
	 * Removes a road from the node. 
	 * 
	 * @param road
	 *            road to be removed
	 * @param origin
	 *            origin of the rectangular bounds of this node
	 * @param width
	 *            width of the rectangular bounds of this node
	 * @param height
	 *            height of the rectangular bounds of this node
	 * @return this node after the city has been removed
	 */
	public abstract Node removeRoad(City start, City end, Point2D.Float origin, int width,
			int height);

	/**
	 * Gets the type of the node (either empty, leaf, or internal).
	 * 
	 * @return type of the node
	 */
	public int getType() {
		return type;
	}
}

