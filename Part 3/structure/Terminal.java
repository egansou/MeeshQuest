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
public class Terminal extends City implements Serializable {

	protected City airport;
	
	protected City city;
	
	private static final int id = 3;

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
	public Terminal(final String name, final int remoteX, final int remoteY, 
			final int localX, final int localY, final int radius, final String color) {
		super(name, remoteX, remoteY, localX, localY, radius, color);
		this.airport = null;
		this.city = null;
	}

	public City getAirport() {
		return airport;
	}

	public void setAirport(City airport) {
		this.airport = airport;
	}

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

	public Point2D.Float getRemotePT() {
		return super.getRemotePT();
	}

	public Point2D.Float getLocalPT() {
		return super.getLocalPT();
	}

	
	/**
	 * Gets the name of this city.
	 * 
	 * @return name of this city
	 */
	public String getName() {
		return super.getName();
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
		return super.getLocalX();
	}

	/**
	 * Gets the Y coordinate of this city.
	 * 
	 * @return Y coordinate of this city
	 */
	public int getLocalY() {
		return super.getLocalY();
	}
	
	public int getRemoteX() {
		return super.getRemoteX();
	}

	
	public int getRemoteY() {
		return super.getRemoteY();
	}

	/**
	 * Gets the color of this city.
	 * 
	 * @return color of this city
	 */
	public String getColor() {
		return super.getColor();
	}

	/**
	 * Gets the radius of this city.
	 * 
	 * @return radius of this city.
	 */
	public int getRadius() {
		return super.getRadius();
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
		return super.equals(obj);
	}

	/**
	 * Returns a hash code for this city.
	 * 
	 * @return hash code for this city
	 */
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * Returns an (x,y) representation of the city. Important: casts the x and y
	 * coordinates to integers.
	 * 
	 * @return string representing the location of the city
	 */
	public String getLocationString() {
		return super.getLocationString();
	}

	/**
	 * Returns a Point2D instance representing the City's location.
	 * 
	 * @return location of this city
	 */
	public Point2D remotetoPoint2D() {
		return super.remotetoPoint2D();
	}
	
	public Point2D localtoPoint2D() {
		return super.localtoPoint2D();
	}
	
	public String toString() {
		return super.toString();
	}
}