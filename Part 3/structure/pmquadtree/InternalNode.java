package cmsc420.structure.pmquadtree;

 
import java.awt.Color;  
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

import cmsc420.exception.ViolatesPMRulesException;
import cmsc420.structure.City;
import cmsc420.utils.*;


/**
 * Represents an internal node of a PR Quadtree.
 */
@SuppressWarnings("serial")
public class InternalNode extends Node {
	

	/** children nodes of this node */
	public Node[] children;

	/** rectangular quadrants of the children nodes */
	protected Rectangle2D.Float[] regions;

	/** origin of the rectangular bounds of this node */
	public Point2D.Float origin;

	/** origins of the rectangular bounds of each child node */
	protected Point2D.Float[] origins;

	/** width of the rectangular bounds of this node */
	public int width;

	/** height of the rectangular bounds of this node */
	public int height;

	/** half of the width of the rectangular bounds of this node */
	protected int halfWidth;

	/** half of the height of the rectangular bounds of this node */
	protected int halfHeight;
	
	/** order */
	protected int order;

	/**
	 * Constructs and initializes this internal PR Quadtree node.
	 * 
	 * @param origin
	 *            origin of the rectangular bounds of this node
	 * @param width
	 *            width of the rectangular bounds of this node
	 * @param height
	 *            height of the rectangular bounds of this node
	 * @throws RoadViolatesPMRulesException 
	 */
	public InternalNode(Point2D.Float origin, int width, int height, int order) 
			throws ViolatesPMRulesException {
		super(Node.INTERNAL);

		this.origin = origin;

		children = new Node[4];
		for (int i = 0; i < 4; i++) {
			children[i] = EmptyNode.instance;
		}
		
		this.width = width;
		this.height = height;
		this.order = order;

		halfWidth = 	
		halfHeight = height / 2;
		
		if (halfWidth < 1) throw new ViolatesPMRulesException();
		
		origins = new Point2D.Float[4];
		origins[0] = new Point2D.Float(origin.x, origin.y + halfHeight);
		origins[1] = new Point2D.Float(origin.x + halfWidth, origin.y
				+ halfHeight);
		origins[2] = new Point2D.Float(origin.x, origin.y);
		origins[3] = new Point2D.Float(origin.x + halfWidth, origin.y);

		regions = new Rectangle2D.Float[4];
		int i = 0;
		while (i < 4) {
			regions[i] = new Rectangle2D.Float(origins[i].x, origins[i].y,
					halfWidth, halfHeight);
			i++;
		}

		/* add a cross to the drawing panel */
		if (Canvas.instance != null) {
            Canvas.instance.addCross(getCenterX(), getCenterY(), halfWidth, Color.GRAY);
		}
	}

	public Node addCity(City city, Point2D.Float origin, int width, int height, int order) 
			throws ViolatesPMRulesException  {
		final Point2D cityLocation = city.localtoPoint2D();
		for (int i = 0; i < 4; i++) {
			if (intersects(cityLocation, regions[i])) {
				children[i] = children[i].addCity(city, origins[i], halfWidth,
						halfHeight, order);
			}
		}
		return this;
	}
	
	@Override
	public Node addRoad(City start, City end, Point2D.Float origin, int width, int height, int order) 
			throws ViolatesPMRulesException {
		Line2D.Float road = new Line2D.Float(start.getLocalX(), start.getLocalY(), 
				end.getLocalX(),end.getLocalY());
		for (int i = 0; i < 4; i++) {
			if (road.intersects(regions[i])) {
				children[i] = children[i].addRoad(start, end, origins[i], halfWidth,
						halfHeight, order);
			}
		}
		return this;
	}
	
	public Node removeCity ( City city, Point2D.Float origin, int width,
			int height, int order) {
		HashSet<City> cities = new HashSet<City>();
		HashSet<ArrayList<City>> roads = new HashSet<ArrayList<City>>();
		
		/* initialize canvas */
		Canvas.instance.setFrameSize(width, height);
		/* add a rectangle to show where the bounds of the map are located */
		Canvas.instance.addRectangle(0, 0, (width > height) ? 
				width : height, 
				(width > height) ? width :
					height, Color.WHITE, true);
		Canvas.instance.addRectangle(0, 0, width, height, Color.BLACK,
				false);
		
		Node root = EmptyNode.instance;
	
		removeCityHelper(this, city, origin, width, height, order, cities, roads);
		
		try {
			for (City c : cities){
//				if(root.getType() == Node.EMPTY) System.out.println("it is empty");
//				System.out.println(c.getName());
				root = root.addCity(c, origin, width, height, order);
				
				Canvas.instance.addPoint(c.getName(), c.getLocalX(), c.getLocalY(), Color.GRAY);
			}
			for (ArrayList<City> r : roads){
//				if(root.getType() == Node.EMPTY) System.out.println("it is empty");
//				System.out.println(r.get(0).getName() + " - " + r.get(1).getName());
				root = root.addRoad(r.get(0), r.get(1),origin, this.width, this.height, order);
				Canvas.instance.addLine(r.get(0).getLocalX(), r.get(0).getLocalY(), r.get(1).getLocalX(),
						r.get(1).getLocalY(), Color.GRAY);
			}
			return root;
		} catch (ViolatesPMRulesException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		/* Should never get there */
		return null;
	}

	public void removeCityHelper(InternalNode curr, City city, Point2D.Float origin, int width,
			int height, int order, HashSet<City> cities, HashSet<ArrayList<City>> roads) {
		//final Point2D cityLocation = city.localtoPoint2D();
		 
		for (int i = 0; i < 4; i++) {
			if(curr.children[i].getType() == Node.LEAF) {
				curr.children[i] = curr.children[i].removeCity(city, curr.origins[i],
						width/2, height/2, order);
				if(curr.children[i].getType() == Node.LEAF){
					if(curr.children[i].getCity() != null) cities.add(curr.children[i].getCity());
					if(! curr.children[i].getRoads().isEmpty()){
						for(ArrayList<City> road : curr.children[i].getRoads() ){
							roads.add(road);
						}
					}
				}
			}
			if (curr.children[i].getType() == Node.INTERNAL) removeCityHelper((InternalNode)curr.children[i], 
					city, curr.origins[i], width/2, height/2, order,cities,roads);
		}
		
	}
	
	public Node removeRoad (City start, City end, Point2D.Float origin, int width,
			int height, int order) {
		HashSet<City> cities = new HashSet<City>();
		HashSet<ArrayList<City>> roads = new HashSet<ArrayList<City>>();
		/* initialize canvas */
		Canvas.instance.setFrameSize(width, height);
		/* add a rectangle to show where the bounds of the map are located */
		Canvas.instance.addRectangle(0, 0, (width > height) ? 
				width : height, 
				(width > height) ? width :
					height, Color.WHITE, true);
		Canvas.instance.addRectangle(0, 0, width, height, Color.BLACK,
				false);
		
		Node root = EmptyNode.instance;
	
		removeRoadHelper(this, start, end, origin, width, height, order, cities, roads);
		
		try {
			for (City c : cities){
//				if(root.getType() == Node.EMPTY) System.out.println("it is empty");
//				System.out.println(c.getName());
				root = root.addCity(c, origin, width, height, order);
				Canvas.instance.addPoint(c.getName(), c.getLocalX(), c.getLocalY(), Color.GRAY);
			}
			for (ArrayList<City> r : roads){
//				if(root.getType() == Node.EMPTY) System.out.println("it is empty");
//				System.out.println(r.get(0).getName() + " - " + r.get(1).getName());
				root = root.addRoad(r.get(0), r.get(1),origin, width, height, order);
				Canvas.instance.addLine(r.get(0).getLocalX(), r.get(0).getLocalY(), r.get(1).getLocalX(),
						r.get(1).getLocalY(), Color.GRAY);
			}
			return root;
		} catch (ViolatesPMRulesException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		/* Should never get there */
		return null;
		
	}

	public void removeRoadHelper(InternalNode curr, City start, City end, Point2D.Float origin, int width,
			int height, int order, HashSet<City> cities, HashSet<ArrayList<City>> roads) {
		//final Point2D cityLocation = city.localtoPoint2D();
		 
		for (int i = 0; i < 4; i++) {
			if(curr.children[i].getType() == Node.LEAF) {
				curr.children[i] = curr.children[i].removeRoad(start, end, curr.origins[i],
						width/2, height/2, order);
				if(curr.children[i].getType() == Node.LEAF){
					if(curr.children[i].getCity() != null) cities.add(curr.children[i].getCity());
					if(! curr.children[i].getRoads().isEmpty()){
						for(ArrayList<City> road : curr.children[i].getRoads() ){
							roads.add(road);
						}
					}
				}
			}
			if (curr.children[i].getType() == Node.INTERNAL) removeRoadHelper((InternalNode)curr.children[i], 
					start, end, curr.origins[i], width/2, height/2, order,cities,roads);
		}
		
	}
	
	
	/**
	 * Returns if a point lies within a given rectangular bounds according to
	 * the rules of the PR Quadtree.
	 * 
	 * @param point
	 *            point to be checked
	 * @param rect
	 *            rectangular bounds the point is being checked against
	 * @return true if the point lies within the rectangular bounds, false
	 *         otherwise
	 */	
	public static boolean intersects(Point2D point, Rectangle2D rect) {
		return (point.getX() >= rect.getMinX() && point.getX() <= rect.getMaxX()
				&& point.getY() >= rect.getMinY() && point.getY() <= rect
				.getMaxY());
	}

	/**
	 * Gets the number of empty child nodes contained by this internal node.
	 * 
	 * @return the number of empty child nodes
	 */
	protected int getNumEmptyNodes() {
		int numEmptyNodes = 0;
		for (Node node : children) {
			if (node == EmptyNode.instance) {
				numEmptyNodes++;
			}
		}
		return numEmptyNodes;
	}

	/**
	 * Gets the number of leaf child nodes contained by this internal node.
	 * 
	 * @return the number of leaf child nodes
	 */
	protected int getNumLeafNodes() {
		int numLeafNodes = 0;
		for (Node node : children) {
			if (node.getType() == Node.LEAF) {
				numLeafNodes++;
			}
		}
		return numLeafNodes;
	}

	/**
	 * Gets the child node of this node according to which quadrant it falls
	 * in
	 * 
	 * @param quadrant
	 *            quadrant number (top left is 0, top right is 1, bottom
	 *            left is 2, bottom right is 3)
	 * @return child node
	 */
	public Node getChild(int quadrant) {
		if (quadrant < 0 || quadrant > 3) {
			throw new IllegalArgumentException();
		} else {
			return children[quadrant];
		}
	}

	/**
	 * Gets the rectangular region for the specified child node of this
	 * internal node.
	 * 
	 * @param quadrant
	 *            quadrant that child lies within
	 * @return rectangular region for this child node
	 */
	public Rectangle2D.Float getChildRegion(int quadrant) {
		if (quadrant < 0 || quadrant > 3) {
			throw new IllegalArgumentException();
		} else {
			return regions[quadrant];
		}
	}

	/**
	 * Gets the rectangular region contained by this internal node.
	 * 
	 * @return rectangular region contained by this internal node
	 */
	public Rectangle2D.Float getRegion() {
		return new Rectangle2D.Float(origin.x, origin.y, width, height);
	}

	/**
	 * Gets the center X coordinate of this node's rectangular bounds.
	 * 
	 * @return center X coordinate of this node's rectangular bounds
	 */
	public int getCenterX() {
		return (int) origin.x + halfWidth;
	}

	/**
	 * Gets the center Y coordinate of this node's rectangular bounds.
	 * 
	 * @return center Y coordinate of this node's rectangular bounds
	 */
	public int getCenterY() {
		return (int) origin.y + halfHeight;
	}

	/**
	 * Gets half the width of this internal node.
	 * @return half the width of this internal node
	 */
	public int getHalfWidth() {
		return halfWidth;
	}

	/** 
	 * Gets half the height of this internal node.
	 * @return half the height of this internal node
	 */
	public int getHalfHeight() {
		return halfHeight;
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
