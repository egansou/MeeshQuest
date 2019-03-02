package cmsc420.structure.pmquadtree;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.TreeSet;
import java.awt.geom.Line2D;

import cmsc420.structure.City;


/**
 * Represents a leaf node of a PM Quadtree.
 */
public class LeafNode extends Node {
	/** city contained within this leaf node */
	protected City city;
	/** roads contained within this leaf node */
	protected TreeSet<ArrayList<City>> roads;

	/**
	 * Constructs and initializes a leaf node.
	 */
	public LeafNode() {
		super(Node.LEAF);
		this.city = null;
		this.roads = new TreeSet<ArrayList<City>>(new StartEndComparator());
	}

	/**
	 * Gets the city contained by this node.
	 * 
	 * @return city contained by this node
	 */
	public City getCity() {
		return city;
	}
	
	/**
	 * Gets the city contained by this node.
	 * 
	 * @return city contained by this node
	 */
	public TreeSet<ArrayList<City>> getRoads() {
		return roads;
	}

	public Node addCity(City newCity, Point2D.Float origin, int width,
			int height) {
		if (city == null) {
			/* node is empty, add city */
			city = newCity;
			return this;
		} else {
			/* node is full, partition node and then add city */
			InternalNode internalNode = new InternalNode(origin, width,
					height);
			internalNode.addCity(city, origin, width, height);
			internalNode.addCity(newCity, origin, width, height);
			
			for(ArrayList<City> road : roads){
				internalNode.addRoad(road.get(0), road.get(1), origin, width, height);
			}
			
			return internalNode;
		}
	}

	public Node removeCity(City city, Point2D.Float origin, int width,
			int height) {
		if (this.city != city) {
			/* city not here */
			throw new IllegalArgumentException();
		} else {
			/* remove city, node becomes empty */
			this.city = null;
			if (this.city == null && roads.isEmpty()) return EmptyNode.instance;
			else return this;
		}
	}
	
	
	@Override
	public Node addRoad(City start, City end, Point2D.Float origin, int width, int height) {
		Line2D.Float road = new Line2D.Float(start.getX(), start.getY(), end.getX(), end.getY());
		Rectangle2D.Float rec = new Rectangle2D.Float(origin.x, origin.y, width, height);
		if (road.intersects(rec)){
			ArrayList<City> cities =  new ArrayList<City>();
			if(start.getName().compareTo(end.getName()) < 0){
				cities.add(start);
				cities.add(end);
			}
			else {
				cities.add(end);
				cities.add(start);
			}
			
			roads.add(cities);	
		}
		return this;
	}

	@Override
	public Node removeRoad(City start, City end, Point2D.Float origin, int width, int height) {
		ArrayList<City> cities =  new ArrayList<City>();
		cities.add(start);
		cities.add(end);
		roads.remove(cities);
		if (this.city == null && roads.isEmpty()) return EmptyNode.instance;
		else return this;
	}
}