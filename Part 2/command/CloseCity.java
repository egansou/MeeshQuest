/**
 * The close city class stores information about the nearest city to a main city. 
 * The shortest distance to get to that city and the city we came from
 * @author Enock Gansou
 */
package cmsc420.command;

public class CloseCity {
	
	/*
	 * the city we came from 
	 */
	private String comeFrom;
	/*
	 * the distance from the main city
	 */
	private Double distance;
	
	
	public CloseCity(String comeFrom, Double distance){
		this.comeFrom = comeFrom;
		this.distance = distance;
	}


	public String getComeFrom() {
		return comeFrom;
	}


	public void setComeFrom(String comeFrom) {
		this.comeFrom = comeFrom;
	}


	public Double getDistance() {
		return distance;
	}


	public void setDistance(Double distance) {
		this.distance = distance;
	}
	
	public String toString(){
		return comeFrom+", "+distance;
	}

}
