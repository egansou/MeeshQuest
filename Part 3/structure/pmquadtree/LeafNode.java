package cmsc420.structure.pmquadtree;

import java.awt.geom.Point2D; 

import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeSet;
import java.awt.geom.Line2D;

import cmsc420.exception.ViolatesPMRulesException;
import cmsc420.structure.City;


/**
 * Represents a leaf node of a PM Quadtree.
 */
@SuppressWarnings("serial")
public class LeafNode extends Node implements Serializable {
	
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
			int height, int order) throws ViolatesPMRulesException {
		
		if(order == 3){
			if (city == null ) {
				/* node is empty, add city */
				city = newCity;
				return this;
			} else {
				if(city == newCity){
					return this;
				}
				else {
					InternalNode internalNode = new InternalNode(origin, width,
							height, order);
					internalNode.addCity(city, origin, width, height, order);
					internalNode.addCity(newCity, origin, width, height, order);
			
					for(ArrayList<City> road : roads){
						internalNode.addRoad(road.get(0), road.get(1), origin, width, height, order);
					}
			
					return internalNode;
				}
			}
		}
		else {
			if (city == null ) {
				/* node is empty, add city */
				if(roads.isEmpty()) {
					city = newCity;
					return this;
				}
				else {
					boolean check = false;
					for(ArrayList<City> road : roads){
						if(road.get(0) == newCity || road.get(1) == newCity){
							check = true;
						}
					}
					if(check) {
						city = newCity;
						return this;
					}
					else{
						InternalNode internalNode = new InternalNode(origin, width,
								height, order);
						internalNode.addCity(newCity, origin, width, height, order);
						for(ArrayList<City> road : roads){
							internalNode.addRoad(road.get(0), road.get(1), origin, width, height, order);
						}
			
						return internalNode;
					}
					
				}
			}
			else {
				if(city == newCity){
					return this;
				}
				else {
					/* node is full, partition node and then add city */
					InternalNode internalNode = new InternalNode(origin, width,
						height, order);
					internalNode.addCity(city, origin, width, height, order);
					internalNode.addCity(newCity, origin, width, height, order);
			
					for(ArrayList<City> road : roads){
						internalNode.addRoad(road.get(0), road.get(1), origin, width, height, order);
				}
			
				return internalNode;
				}
			}
			
		}
		
	}
	
	@Override
	public Node addRoad(City start, City end, Point2D.Float origin, int width, int height, int order) 
			throws ViolatesPMRulesException {
		Line2D.Float road = new Line2D.Float(start.getLocalX(), start.getLocalY(), 
				end.getLocalX(), end.getLocalY());
		Rectangle2D.Float rec = new Rectangle2D.Float(origin.x, origin.y, width, height);
		ArrayList<City> cities1 =  new ArrayList<City>();
		cities1.add(start);
		cities1.add(end);
		ArrayList<City> cities2 =  new ArrayList<City>();
		cities2.add(end);
		cities2.add(start);
		
		if(roads.contains(cities1) || roads.contains(cities2)){
			return this;
		}
		
		if(order == 3){
			if (road.intersects(rec)) {
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
		else {
			if (road.intersects(rec)) {
				if(city == null){
					if(roads.isEmpty()){
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
						return this;
					}
					else{
						InternalNode internalNode = new InternalNode(origin, width,
								height, order);
		
						for(ArrayList<City> r : roads){
							internalNode.addRoad(r.get(0), r.get(1), origin, width, height, order);
						}
						
						internalNode.addRoad(start, end, origin, width, height, order);
						return internalNode;
					}
				}
				else if (city != null && (city == start || city == end) ){
				
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
					return this;
				}
				else {
					InternalNode internalNode = new InternalNode(origin, width,
							height, order);
					internalNode.addCity(city, origin, width, height, order);
					internalNode.addRoad(start, end, origin, width, height, order);
				
					for(ArrayList<City> r : roads){
						internalNode.addRoad(r.get(0), r.get(1), origin, width, height, order);
					}
				
					return internalNode;
					
				}
			}
			else return this;
		}
	}
	

	public Node removeCity(City city, Point2D.Float origin, int width,
			int height, int order) {
		if (this.city != null && this.city == city) {
			this.city = null;
			if (this.city == null && roads.isEmpty()) return EmptyNode.instance;
			else return this;
		}
		return this;
	}
	
	
	
	@Override
	public Node removeRoad(City start, City end, Point2D.Float origin, int width, int height, int order) {
		ArrayList<City> cities =  new ArrayList<City>();
		cities.add(start);
		cities.add(end);
		roads.remove(cities);
		cities =  new ArrayList<City>();
		cities.add(end);
		cities.add(start);
		roads.remove(cities);
		if (this.city == null && roads.isEmpty()) return EmptyNode.instance;
		else return this;
	}
}