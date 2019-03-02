package cmsc420.structure.pmquadtree ;

import java.awt.geom.*;
import java.util.*;

import cmsc420.exception.CityAlreadyMappedException;
import cmsc420.exception.CityOutOfBoundsException;
import cmsc420.exception.RoadAlreadyMappedException;
import cmsc420.exception.RoadOutOfBoundsException;
import cmsc420.exception.StartOrEndIsIsolatedExeption;
import cmsc420.geom.*;
import cmsc420.structure.*;


/**
 * PM Quadtree is a region quadtree capable of storing points and roads.
 * 
 * @author Ruofei Du, Ben Zoller
 * @version 2.1, 09/09/2014
 */
public class PMQuadtree {
	/** root of the PM Quadtree */
	protected Node root;

	/** bounds of the spatial map */
	protected Point2D.Float spatialOrigin;

	/** width of the spatial map */
	protected int spatialWidth;

	/** height of the spatial map */
	protected int spatialHeight;

	/** used to keep track of cities within the spatial map */
	protected HashSet<String> cityNames;
	
	/** used to keep track of isolated cities within the spatial map */
	protected HashSet<String> isolatedCityNames;
	
	protected HashSet<ArrayList<String>> roads;


	/**
	 * Constructs an empty PM Quadtree.
	 */
	public PMQuadtree() {
		root = EmptyNode.instance;
		cityNames = new HashSet<String>();
		isolatedCityNames = new HashSet<String>();
		roads = new HashSet<ArrayList<String>>();
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
	
	public City getCity() {
		return root.getCity();
	}
	
	
	public TreeSet<ArrayList<City>> getRoads() {
		return root.getRoads();
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
	 */
	public void addIsolatedCity(City city) throws CityAlreadyMappedException,
			CityOutOfBoundsException {
		if (cityNames.contains(city.getName())) {
			/* city already mapped */
			throw new CityAlreadyMappedException();
		}

		/* check bounds */
		int x = (int) city.getX();
		int y = (int) city.getY();
		if (x < spatialOrigin.x || x > spatialWidth || y < spatialOrigin.y
				|| y > spatialHeight) {
			/* city out of bounds */
			throw new CityOutOfBoundsException();
		}

		/* insert city into PMQuadTree */
		cityNames.add(city.getName());
		isolatedCityNames.add(city.getName());
		root = root.addCity(city, spatialOrigin, spatialWidth, spatialHeight);
	}
	
	public void addCity(City city) {	
		/* check bounds */
		int x = (int) city.getX();
		int y = (int) city.getY();
		if (x < spatialOrigin.x || x > spatialWidth || y < spatialOrigin.y
				|| y > spatialHeight) {
			//Do nothing
		}
		else {
		/* insert city into PMQuadTree */
			if (!cityNames.contains(city.getName())) {
				cityNames.add(city.getName());
				root = root.addCity(city, spatialOrigin, spatialWidth, spatialHeight);
			}
		}
		
	}
	
	public void addRoad(City start, City end) throws StartOrEndIsIsolatedExeption, 
		RoadAlreadyMappedException, RoadOutOfBoundsException{
		Line2D.Float road = new Line2D.Float(start.getX(),start.getY(), end.getX(), end.getY());
		Rectangle2D.Float spatialMap = new Rectangle2D.Float(0, 0,
				this.spatialWidth, this.spatialHeight);
		ArrayList<String> citiesToAdd = new ArrayList<String>();
		citiesToAdd.add(start.getName());
		citiesToAdd.add(end.getName());
		
		ArrayList<String> citiesToCheck = new ArrayList<String>();
		citiesToCheck.add(end.getName());
		citiesToCheck.add(start.getName());
		
		
		if (isolatedCityNames.contains(start.getName()) || 
			isolatedCityNames.contains(end.getName())) {
			/* city is isolated */
			throw new StartOrEndIsIsolatedExeption();
		}
		
		if(containsRoad(citiesToAdd) || containsRoad(citiesToCheck)){
			
			throw new RoadAlreadyMappedException();
		}
		
		if(!road.intersects(spatialMap)) throw new RoadOutOfBoundsException();
		
		addCity(start);
		addCity(end); 
		roads.add(citiesToAdd);
		root = root.addRoad(start, end, spatialOrigin, spatialWidth, spatialHeight);
	}

	public void removeRoad(City start, City end){
		throw new UnsupportedOperationException();
	}
	
	public boolean containsRoad(ArrayList<String> cities){
		
		return roads.contains(cities);
	}

	/**
	 * Removes a given city from the spatial map.
	 * 
	 * @param city
	 *            city to be removed
	 * @throws CityNotMappedException
	 *             city is not in the spatial map
	 */
	public boolean removeIsolatedCity(City city) {
		final boolean success = cityNames.contains(city.getName());
		if (success) {
			isolatedCityNames.remove(city.getName());
			cityNames.remove(city.getName());
			root = root
					.removeCity(city, spatialOrigin, spatialWidth, spatialHeight);
		}
		return success;
	}

	/**
	 * Clears the PM Quadtree so it contains no non-empty nodes.
	 */
	public void clear() {
		root = EmptyNode.instance;
		isolatedCityNames.clear();
		roads.clear();
		cityNames.clear();
	}

	/**
	 * Returns if the PM Quadtree contains a city with the given name.
	 * 
	 * @return true if the city is in the spatial map. false otherwise.
	 */
	public boolean contains(String name) {
		return cityNames.contains(name);
	}
	
	public boolean isolatedContains(String name) {
		return isolatedCityNames.contains(name);
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
