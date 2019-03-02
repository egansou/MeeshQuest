package cmsc420.structure.pmquadtree;


import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.TreeSet;

import cmsc420.structure.City;
import cmsc420.utils.*;


/**
 * Represents an internal node of a PR Quadtree.
 */
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

	/**
	 * Constructs and initializes this internal PR Quadtree node.
	 * 
	 * @param origin
	 *            origin of the rectangular bounds of this node
	 * @param width
	 *            width of the rectangular bounds of this node
	 * @param height
	 *            height of the rectangular bounds of this node
	 */
	public InternalNode(Point2D.Float origin, int width, int height) {
		super(Node.INTERNAL);

		this.origin = origin;

		children = new Node[4];
		for (int i = 0; i < 4; i++) {
			children[i] = EmptyNode.instance;
		}
		
		this.width = width;
		this.height = height;

		halfWidth = 	
		halfHeight = height >> 1;

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
            //canvas.addCross(getCenterX(), getCenterY(), halfWidth, Color.d);
			int cx = getCenterX();
			int cy = getCenterY();
            Canvas.instance.addLine(cx - halfWidth, cy, cx + halfWidth, cy, Color.GRAY);
            Canvas.instance.addLine(cx, cy - halfHeight, cx, cy + halfHeight, Color.GRAY);
		}
	}

	public Node addCity(City city, Point2D.Float origin, int width, int height) {
		final Point2D cityLocation = city.toPoint2D();
		for (int i = 0; i < 4; i++) {
			if (intersects(cityLocation, regions[i])) {
				children[i] = children[i].addCity(city, origins[i], halfWidth,
						halfHeight);
			}
		}
		return this;
	}
	
	

	public Node removeCity(City city, Point2D.Float origin, int width,
			int height) {
		final Point2D cityLocation = city.toPoint2D();
		for (int i = 0; i < 4; i++) {
			if (intersects(cityLocation, regions[i])) {
				children[i] = children[i].removeCity(city, origins[i],
						halfWidth, halfHeight);
			}
		}

		if (getNumEmptyNodes() == 4) {
			/* remove cross from the drawing panel */
			if (Canvas.isEnabled()) {
                Canvas.instance.removeCross(getCenterX(), getCenterY(), halfWidth, Color.BLACK);
			}
			return EmptyNode.instance;

		} else if (getNumEmptyNodes() == 3 && getNumLeafNodes() == 1) {
			/* remove cross from the drawing panel */
            if (Canvas.isEnabled()) {
                Canvas.instance.removeCross(getCenterX(), getCenterY(), halfWidth, Color.BLACK);
            }
            
			for (Node node : children) {
				if (node.getType() == Node.LEAF) {
					return node;
				}
			}
			/* should never get here */
			return null;

		} else {
			return this;
		}

	}
	
	@Override
	public Node addRoad(City start, City end, Point2D.Float origin, int width, int height) {
		Line2D.Float road = new Line2D.Float(start.getX(), start.getY(), end.getX(),end.getY());
		for (int i = 0; i < 4; i++) {
			if (road.intersects(regions[i])) {
				children[i] = children[i].addRoad(start, end, origins[i], halfWidth,
						halfHeight);
			}
		}
		return this;
	}

	@Override
	public Node removeRoad(City start, City end, Point2D.Float origin, int width, int height) {
		
		for (int i = 0; i < 4; i++) {
			
			Line2D.Float road = new Line2D.Float(start.getX(), start.getY(), end.getX(),end.getY());
			if (road.intersects(regions[i])) {	
				children[i] = children[i].removeRoad(start, end, origins[i],
						halfWidth, halfHeight);
			}
		}

		if (getNumEmptyNodes() == 4) {
			/* remove cross from the drawing panel */
			if (Canvas.isEnabled()) {
                Canvas.instance.removeCross(getCenterX(), getCenterY(), halfWidth, Color.BLACK);
			}
			return EmptyNode.instance;

		} else if (getNumEmptyNodes() == 3 && getNumLeafNodes() == 1) {
			/* remove cross from the drawing panel */
            if (Canvas.isEnabled()) {
                Canvas.instance.removeCross(getCenterX(), getCenterY(), halfWidth, Color.BLACK);
            }
            
			for (Node node : children) {
				if (node.getType() == Node.LEAF) {
					return node;
				}
			}
			/* should never get here */
			return null;

		} else {
			return this;
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
