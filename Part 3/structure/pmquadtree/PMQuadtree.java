package cmsc420.structure.pmquadtree ;

import java.awt.geom.*; 
import java.io.Serializable;
import java.util.*;

import cmsc420.exception.CityOutOfBoundsException;
import cmsc420.exception.RoadAlreadyMappedException;
import cmsc420.exception.RoadIntersectsAnotherRoadException;
import cmsc420.exception.RoadOutOfBoundsException;
import cmsc420.exception.ViolatesPMRulesException;
import cmsc420.geom.*;
import cmsc420.structure.*;


/**
 * PM Quadtree is a region quadtree capable of storing points and roads.
 * 
 * @author Ruofei Du, Ben Zoller
 * @version 2.1, 09/09/2014
 */
@SuppressWarnings("serial")
public class PMQuadtree implements Cloneable, Serializable {
	

	/** root of the PM Quadtree */
	private Node root;

	/** bounds of the spatial map */
	protected Point2D.Float spatialOrigin;

	/** width of the spatial map */
	protected int spatialWidth;

	/** height of the spatial map */
	protected int spatialHeight;
	
	/** pm order */
	protected int order;

	/** used to keep track of cities within the spatial map */
	protected HashSet<City> cities;
	
	/** used to keep track of airports within the spatial map */
	protected HashSet<City> airports;
	
	/** used to keep track of airports within the spatial map */
	protected HashSet<City> terminals;
	
	protected HashSet<ArrayList<City>> roads;


	/**
	 * Constructs an empty PM Quadtree.
	 */
	public PMQuadtree() {
		root = EmptyNode.instance;
		cities = new HashSet<City>();
		airports = new HashSet<City>();
		terminals = new HashSet<City>();
		roads = new HashSet<ArrayList<City>>();
		spatialOrigin = new Point2D.Float(0, 0);
	}
	
	 
	/**
	 * Sets the width and height of the spatial map.
	 * 
	 * @param spatialWidth
	 *            width of the spatial map
	 * @param spatialHeight
	 *            height of the spatial map
	 */
	public void setRange(int spatialWidth, int spatialHeight) {
		this.spatialWidth = spatialWidth;
		this.spatialHeight = spatialHeight;
	}
	
	
	/**
	 * Sets the pm order.
	 *
	 * @param order the order of the pm quadtree.
	 */
	public void setOrder(int order) {
		this.order = order;
	}


	/**
	 * Gets the height of the spatial map
	 * 
	 * @return height of the spatial map
	 */
	public float getSpatialHeight() {
		return spatialHeight;
	}

	/**
	 * Gets the width of the spatial map
	 * 
	 * @return width of the spatial map
	 */
	public float getSpatialWidth() {
		return spatialWidth;
	}

	/**
	 * Gets the root node of the PM Quadtree.
	 * 
	 * @return root node of the PM Quadtree
	 */
	public Node getRoot() {
		return root;
	}
	
	
	public HashSet<ArrayList<City>> getRoads() {
		return roads;
	}
	
	
	/**
	 * Whether the PM Quadtree has zero or more elements.
	 * 
	 * @return <code>true</code> if the PM Quadtree has no non-empty nodes.
	 *         Otherwise returns <code>false</code>
	 */
	public boolean isEmpty() {
		return (root == EmptyNode.instance);
	}

	/**
	 * Inserts a city into the spatial map.
	 * 
	 * @param city
	 *            city to be added
	 * @throws CityAlreadyMappedException
	 *             city is already in the spatial map
	 * @throws CityOutOfBoundsException
	 *             city's location is outside the bounds of the spatial map
	 * @throws RoadViolatesPMRulesException 
	 */
	public void addAirport(City airport) throws ViolatesPMRulesException {
		for (ArrayList<City> r : roads) {
			Line2D.Float line  = new Line2D.Float(r.get(0).getLocalX(), r.get(0).getLocalY(),
					r.get(1).getLocalX(), r.get(1).getLocalY());
			if(line.ptSegDist(((Airport)airport).getLocalPT()) == 0) {
					throw new ViolatesPMRulesException();
			}
		}
		
		airports.add(airport);
		root = root.addCity(airport, spatialOrigin, spatialWidth, spatialHeight, order);
		
	}
	
	public void addTerminal(City airport, City terminal, City city) throws ViolatesPMRulesException, 
		RoadIntersectsAnotherRoadException {
		
		Line2D.Float road = new Line2D.Float(city.getLocalX(), city.getLocalY(),
				terminal.getLocalX(), terminal.getLocalY());
		
		for (ArrayList<City> r : roads) {
				Line2D.Float line  = new Line2D.Float(r.get(0).getLocalX(), r.get(0).getLocalY(),
						r.get(1).getLocalX(), r.get(1).getLocalY());
				
				if(line.intersectsLine(road)) {	
					if (((line.getP1().equals(road.getP1())) ||
							(line.getP1().equals(road.getP2())) ||
									(line.getP2().equals(road.getP1())) ||
											(line.getP2().equals(road.getP2()))) &&
							(line.ptSegDist(city.getLocalPT()) != 0.0 
							|| line.ptSegDist(terminal.getLocalPT()) != 0.0) &&
							(road.ptSegDist(line.getP1()) != 0.0 
							|| road.ptSegDist(line.getP2()) != 0.0)) {
						//Do nothing
					}
					else throw new RoadIntersectsAnotherRoadException();	
				}
		}
		
		terminals.add(terminal);
		root = root.addCity(terminal, spatialOrigin, spatialWidth, spatialHeight, order);
		
		ArrayList<City> citiesToAdd = new ArrayList<City>();
		int cmp = city.getName().compareTo(terminal.getName());
		if(cmp < 0) {
			citiesToAdd.add(city);
			citiesToAdd.add(terminal);
		}
		else {
			citiesToAdd.add(terminal);
			citiesToAdd.add(city);
		}
		roads.add(citiesToAdd);
		root = root.addRoad(city, terminal, spatialOrigin, spatialWidth, spatialHeight, order);	
	}
	
	/**
	 * Removes a given a wrongly mapped data from the spatial map.
	 * 
	 * @param city
	 *            city to be removed
	 * @throws CityNotMappedException
	 *             city is not in the spatial map
	 */
	public boolean removeAirport(City city) {
		final boolean success = airports.contains(city);
		if (success) {
			airports.remove(city);
			root = root.removeCity(city, spatialOrigin, spatialWidth, spatialHeight, order);
		}
		return success;
	}
	
	public boolean removeCity(City city) {
		final boolean success = cities.contains(city);
		if (success) {
			cities.remove(city);
			root = root.removeCity(city, spatialOrigin, spatialWidth, spatialHeight, order);
		}
		return success;
	}
	
	public boolean removeTerminal(City city) {
		final boolean success = terminals.contains(city);
		if (success) {
			terminals.remove(city);
			root = root.removeCity(city, spatialOrigin, spatialWidth, spatialHeight, order);
		}
		return success;
	}
	
	public void addCity(City city) throws ViolatesPMRulesException {	
		/* check bounds */
		int x = (int) city.getLocalX();
		int y = (int) city.getLocalY();
		if (x < spatialOrigin.x || x > spatialWidth || y < spatialOrigin.y
				|| y > spatialHeight) {
			//Do nothing
		}
		else {
		/* insert city into PMQuadTree */
			if (!cities.contains(city)) {
				cities.add(city);
				root = root.addCity(city, spatialOrigin, spatialWidth, spatialHeight, order);
				
			}
		}
		
	}
	
	public void addRoad(City start, City end) throws RoadIntersectsAnotherRoadException, 
		RoadAlreadyMappedException, RoadOutOfBoundsException, ViolatesPMRulesException{
		Line2D.Float road = new Line2D.Float(start.getLocalX(),start.getLocalY(),
				end.getLocalX(), end.getLocalY());
		Rectangle2D.Float spatialMap = new Rectangle2D.Float(0, 0,
				this.spatialWidth, this.spatialHeight);
		
		ArrayList<City> citiesToAdd = new ArrayList<City>();
		int cmp = start.getName().compareTo(end.getName());
		if(cmp < 0) {
			citiesToAdd.add(start);
			citiesToAdd.add(end);
		}
		else {
			citiesToAdd.add(end);
			citiesToAdd.add(start);
		}
		
		if(!road.intersects(spatialMap)) throw new RoadOutOfBoundsException();
		
		if(roads.contains(citiesToAdd)){
			throw new RoadAlreadyMappedException();
		}

		for (ArrayList<City> r : roads) {
			Line2D.Float line  = new Line2D.Float(r.get(0).getLocalX(), r.get(0).getLocalY(),
					r.get(1).getLocalX(), r.get(1).getLocalY());
			if(line.intersectsLine(road)) {	
					if (((line.getP1().equals(road.getP1())) ||
							(line.getP1().equals(road.getP2())) ||
									(line.getP2().equals(road.getP1())) ||
											(line.getP2().equals(road.getP2()))) &&
							(line.ptSegDist(start.getLocalPT()) != 0.0 
							|| line.ptSegDist(end.getLocalPT()) != 0.0) &&
							(road.ptSegDist(line.getP1()) != 0.0 
							|| road.ptSegDist(line.getP2()) != 0.0) ) {
						//Do nothing
					}
					else throw new RoadIntersectsAnotherRoadException();	
			}
		}
		
		for (City airport : airports) {
			if( road.ptSegDist(((Airport)airport).getLocalPT()) == 0.0) {
					throw new ViolatesPMRulesException();
			}
		}
		
		addCity(start);
		addCity(end); 
		roads.add(citiesToAdd);
		root = root.addRoad(start, end, spatialOrigin, spatialWidth, spatialHeight, order);
		
	}

	public boolean removeRoad(City start, City end){
		ArrayList<City> road = new ArrayList<City>();
		int cmp = start.getName().compareTo(end.getName());
		if(cmp < 0) {
			road.add(start);
			road.add(end);
		}
		else {
			road.add(end);
			road.add(start);
		}
		if (roads.contains(road)) {
			roads.remove(road);
			root = root.removeRoad(start, end, spatialOrigin, spatialWidth, spatialHeight, order);
			return true;
		}
		
		return false;
	}
	

	/**
	 * Clears the PM Quadtree so it contains no non-empty nodes.
	 */
	public void clear() {
		root = EmptyNode.instance;
		roads.clear();
		cities.clear();
		airports.clear();
		terminals.clear();
	}

	/**
	 * Returns if the PM Quadtree contains a city with the given name.
	 * 
	 * @return true if the city is in the spatial map. false otherwise.
	 */
	public boolean containsCity(City city) {
		return cities.contains(city);
	}
	
	public boolean containsAirport(City airport) {
		return airports.contains(airport);
	}
	
	public boolean containsTerminal(City terminal) {
		return airports.contains(terminal);
	}


	/**
	 * Returns if any part of a circle lies within a given rectangular bounds
	 * according to the rules of the PM Quadtree.
	 * 
	 * @param circle
	 *            circular region to be checked
	 * @param rect
	 *            rectangular bounds the point is being checked against
	 * @return true if the point lies within the rectangular bounds, false
	 *         otherwise
	 */
	public boolean intersects(Circle2D circle, Rectangle2D rect) {
		final double radiusSquared = circle.getRadius() * circle.getRadius();

		/* translate coordinates, placing circle at origin */
		final Rectangle2D.Double r = new Rectangle2D.Double(rect.getX()
				- circle.getCenterX(), rect.getY() - circle.getCenterY(), rect
				.getWidth(), rect.getHeight());

		if (r.getMaxX() < 0) {
			/* rectangle to left of circle center */
			if (r.getMaxY() < 0) {
				/* rectangle in lower left corner */
				return ((r.getMaxX() * r.getMaxX() + r.getMaxY() * r.getMaxY()) < radiusSquared);
			} else if (r.getMinY() > 0) {
				/* rectangle in upper left corner */
				return ((r.getMaxX() * r.getMaxX() + r.getMinY() * r.getMinY()) < radiusSquared);
			} else {
				/* rectangle due west of circle */
				return (Math.abs(r.getMaxX()) < circle.getRadius());
			}
		} else if (r.getMinX() > 0) {
			/* rectangle to right of circle center */
			if (r.getMaxY() < 0) {
				/* rectangle in lower right corner */
				return ((r.getMinX() * r.getMinX() + r.getMaxY() * r.getMaxY()) < radiusSquared);
			} else if (r.getMinY() > 0) {
				/* rectangle in upper right corner */
				return ((r.getMinX() * r.getMinX() + r.getMinY() * r.getMinY()) <= radiusSquared);
			} else {
				/* rectangle due east of circle */
				return (r.getMinX() <= circle.getRadius());
			}
		} else {
			/* rectangle on circle vertical centerline */
			if (r.getMaxY() < 0) {
				/* rectangle due south of circle */
				return (Math.abs(r.getMaxY()) < circle.getRadius());
			} else if (r.getMinY() > 0) {
				/* rectangle due north of circle */
				return (r.getMinY() <= circle.getRadius());
			} else {
				/* rectangle contains circle center point */
				return true;
			}
		}
	}
	

	
}
