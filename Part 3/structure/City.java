package cmsc420.structure;


import java.awt.geom.Point2D;
import java.io.Serializable;

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
@SuppressWarnings("serial")
public class City implements Serializable {
	

	/** name of this city */
	protected String name;

	/** 2D coordinates of this city */
	protected Point2D.Float remotePT;
	
	/** 2D coordinates of this city */
	protected Point2D.Float localPT;

	/** radius of this city */
	protected int radius;

	/** color of this city */
	protected String color;
	
	private static final int id = 1;

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
	public City(final String name, final int remoteX, final int remoteY, 
			final int localX, final int localY, final int radius, final String color) {
		this.name = name;
		this.remotePT = new Point2D.Float(remoteX, remoteY);
		this.localPT = new Point2D.Float(localX, localY);
		this.radius = radius;
		this.color = color;
	}

	public Point2D.Float getRemotePT() {
		return remotePT;
	}

	public Point2D.Float getLocalPT() {
		return localPT;
	}

	/**
	 * Gets the name of this city.
	 * 
	 * @return name of this city
	 */
	public String getName() {
		return name;
	}
	
	public int getId() {
		return id;
	}

	/**
	 * Gets the X coordinate of this city.
	 * 
	 * @return X coordinate of this city
	 */
	public int getLocalX() {
		return (int) localPT.x;
	}

	/**
	 * Gets the Y coordinate of this city.
	 * 
	 * @return Y coordinate of this city
	 */
	public int getLocalY() {
		return (int) localPT.y;
	}
	
	public int getRemoteX() {
		return (int) remotePT.x;
	}

	
	public int getRemoteY() {
		return (int) remotePT.y;
	}

	/**
	 * Gets the color of this city.
	 * 
	 * @return color of this city
	 */
	public String getColor() {
		return color;
	}

	/**
	 * Gets the radius of this city.
	 * 
	 * @return radius of this city.
	 */
	public int getRadius() {
		return radius;
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
			City c = (City) obj;
			return (localPT.equals(c.localPT) && remotePT.equals(c.remotePT) &&
					(radius == c.radius) && color
					.equals(c.color) && name.equals(c.name));
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
		hash = 37 * hash + name.hashCode();
		hash = 37 * hash + remotePT.hashCode();
		hash = 37 * hash + localPT.hashCode();
		hash = 37 * hash + radius;
		if (color != null) hash = 37 * hash + color.hashCode();
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
		location.append("(Local: ");
		location.append(getLocalX());
		location.append(",");
		location.append(getLocalY());
		location.append("; Remote: ");
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
		return new Point2D.Float(remotePT.x, remotePT.y);
	}
	
	public Point2D localtoPoint2D() {
		return new Point2D.Float(localPT.x, localPT.y);
	}
	
	public String toString() {
		return name;
	}
}