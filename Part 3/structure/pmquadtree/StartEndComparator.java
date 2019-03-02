package cmsc420.structure.pmquadtree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import cmsc420.structure.City;

@SuppressWarnings("serial")
public class StartEndComparator implements Comparator<ArrayList<City>>, Serializable {


	public int compare(final ArrayList<City> one, final ArrayList<City> two) {
		int cmp = two.get(0).getName().compareTo(one.get(0).getName());
		if(cmp == 0){
			cmp = two.get(1).getName().compareTo(one.get(1).getName());
		}
		return cmp;
	}
}