package cmsc420.structure;

import java.awt.geom.Point2D;

/**
 * City class is an analogue to a real-world city in 2D space. Each city
 * contains a location ((x,y) coordinates), name, radius, and color.
 * <p>
 * Useful <code>java.awt.geom.Point2D</code> methods (such as distance()) can
 * be utilized by calling toPoint2D(), which creates a Point2D copy of this
 * city's location.
 * 
 * @author Ben Zoller
 * @editor Ruofei Du
 * @version 1.0, 19 Feb 2007
 * @revise 1.1, 11 Jun 2014
 */
public class Metropole {
	
	
	/** 2D coordinates of this city */
	public Point2D.Float pt;

	/**
	 * Constructs a city.
	 * 
	 * @param name
	 *            name of the city
	 * @param x
	 *            X coordinate of the city
	 * @param y
	 *            Y coordinate of the city
	 * @param radius
	 *            radius of the city
	 * @param color
	 *            color of the city
	 */
	public Metropole(int remoteX, final int remoteY) {
		pt = new Point2D.Float(remoteX, remoteY);
	}

	

	
	public int getRemoteX() {
		return (int) pt.x;
	}

	
	public int getRemoteY() {
		return (int) pt.y;
	}

	

	/**
	 * Determines if this city is equal to another object. The result is true if
	 * and only if the object is not null and a City object that contains the
	 * same name, X and Y coordinates, radius, and color.
	 * 
	 * @param obj
	 *            the object to compare this city against
	 * @return <code>true</code> if cities are equal, <code>false</code>
	 *         otherwise
	 */
	public boolean equals(final Object obj) {
		if (obj == this)
			return true;
		if (obj != null && (obj.getClass().equals(this.getClass()))) {
			Metropole c = (Metropole) obj;
			return (pt.equals(c.pt) );
		}
		return false;
	}

	/**
	 * Returns a hash code for this city.
	 * 
	 * @return hash code for this city
	 */
	public int hashCode() {
		int hash = 12;
		hash = 37 * hash + pt.hashCode();
		return hash;
	}

	/**
	 * Returns an (x,y) representation of the city. Important: casts the x and y
	 * coordinates to integers.
	 * 
	 * @return string representing the location of the city
	 */
	public String getLocationString() {
		final StringBuilder location = new StringBuilder();
		location.append("(");
		location.append(getRemoteX());
		location.append(",");
		location.append(getRemoteY());
		location.append(")");
		return location.toString();
	}

	/**
	 * Returns a Point2D instance representing the City's location.
	 * 
	 * @return location of this city
	 */
	public Point2D remotetoPoint2D() {
		return new Point2D.Float(pt.x, pt.y);
	}

	
}