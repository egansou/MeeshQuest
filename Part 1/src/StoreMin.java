package cmsc420.meeshquest.part1;

public class StoreMin{
	
	private City city;
	private double min;
	
	public StoreMin(City c, double m){
		city = c;
		min = m;
	}
	
	public City getCity(){
		return city;
	}
	public double getMin(){
		return min;
	}
	public void setMin(double m){
		min = m;
	}
	
	public void setCity(City c){
		city = c;
	}
	
}