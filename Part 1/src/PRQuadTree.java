package cmsc420.meeshquest.part1;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.drawing.CanvasPlus;



public class PRQuadTree   {
	
	
	public class prQuadNode {
		 double x, y;              // x- and y- coordinates
		 prQuadNode NW, NE, SE, SW;   // four subtrees
	     City city;           // associated data    
	}
	public class prQuadLeaf extends prQuadNode {
		public prQuadLeaf (City city, double x , double y){
			super.city = city;
			super.x = x;
			super.y = y;
		}
	}
	public class prQuadInternal extends prQuadNode {
		public prQuadInternal (prQuadNode NW, prQuadNode NE, prQuadNode SE, prQuadNode SW){
			super.NW = NW;
			super.NE = NE;
			super.SE = SE;
			super.SW = SW;
		}
	}
	
	private prQuadNode root;
	
	private double xMin, xMax, yMin, yMax;

	private CanvasPlus canvas;
	
	public PRQuadTree(double xMin, double xMax, double yMin, double yMax, CanvasPlus canvas) {
		
		
    	this.canvas = canvas;
		this.root = null;
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMax = yMax;
		this.yMin = yMin;
	}
	
	public void setCanvas(){
		this.canvas.setFrameSize((int)xMax, (int)yMax);
    	this.canvas.addRectangle(0, 0, xMax, yMax, Color.WHITE, true);
    	this.canvas.addRectangle(0, 0, xMax, yMax, Color.BLACK, false);
	}
	
	
	public void insert(City city) {
		root = insertHelper(this.root, city, xMin, xMax, yMin, yMax);	
	}
	
	private prQuadNode insertHelper(prQuadNode root,City city, double xLo, double xHi, double yLo, double yHi) {
		
		double x = city.getX();
		double y = city.getY();
		
		if (root == null){
			prQuadNode newNode = new prQuadLeaf(city, x, y);
			canvas.addPoint(city.getName(), city.getX() , city.getY(), Color.BLACK);
			root = newNode;
		}
		
		else if (root.getClass().getName().equals("cmsc420.meeshquest.part1.PRQuadTree$prQuadLeaf") ) {
			prQuadNode newInternal = new prQuadInternal(null, null, null, null);
			
			City curr_city = root.city;
			double curr_x = root.x;
			double curr_y = root.y;
	
			root = newInternal;
			
			double xMid = (xLo + xHi)/2;
			double yMid = (yLo + yHi)/2;
			 canvas.addCross(xMid, yMid,xMid - xLo,Color.BLACK);
			
			if (curr_x < xMid && curr_y < yMid){
				root.SW = insertHelper(root.SW, curr_city, xLo, xMid, yLo, yMid);
			}	
			if (curr_x < xMid && curr_y >= yMid){
				root.NW = insertHelper(root.NW, curr_city, xLo, xMid, yMid, yHi);
			}	
			if (curr_x >= xMid && curr_y >= yMid){
				root.NE = insertHelper(root.NE, curr_city, xMid, xHi, yMid, yHi);
			}	
			if (curr_x >= xMid && curr_y < yMid){
				root.SE = insertHelper(root.SE, curr_city, xMid, xHi, yLo, yMid);
			}	
			
			if (x < xMid && y < yMid){
				root.SW = insertHelper(root.SW, city, xLo, xMid, yLo, yMid);
			}	
			if (x < xMid && y >= yMid){
				root.NW = insertHelper(root.NW, city, xLo, xMid, yMid, yHi);
			}	
			if (x >= xMid && y >= yMid){
				root.NE = insertHelper(root.NE, city, xMid, xHi, yMid, yHi);
			}	
			if (x >= xMid && y < yMid){
				root.SE = insertHelper(root.SE, city, xMid, xHi, yLo, yMid);
			}	
		}
		else {
			double xMid = (xLo + xHi)/2;
			double yMid = (yLo + yHi)/2;
			
			if (x < xMid && y < yMid){
				root.SW = insertHelper(root.SW, city, xLo, xMid, yLo, yMid);
			}	
			if (x < xMid && y >= yMid){
				root.NW = insertHelper(root.NW, city, xLo, xMid, yMid, yHi);
			}	
			if (x >= xMid && y >= yMid){
				root.NE = insertHelper(root.NE, city, xMid, xHi, yMid, yHi);
			}	
			if (x >= xMid && y < yMid){
				root.SE = insertHelper(root.SE, city, xMid, xHi, yLo, yMid);
			}	
		}	
		return root;
	}
	
	public boolean contains (String value, TreeMap<String, City> nameToCity) {
		double x = ((City)nameToCity.get(value)).getX(); 
		double y = ((City)nameToCity.get(value)).getY();
		return containsHelper(this.root, value, x, y, xMin, xMax, yMin, yMax);	
	}
	
	
	private boolean containsHelper(prQuadNode root,String value,double x, double y, double xLo, double xHi, double yLo, double yHi) {
		
		double xMid = (xLo + xHi)/2;
		double yMid = (yLo + yHi)/2;
		if (root == null){
			return false;
		}
		
		if(root.getClass().getName().equals("cmsc420.meeshquest.part1.PRQuadTree$prQuadLeaf") && root.city.getName().equals(value)){
			return true;
		}
		else if(root.getClass().getName().equals("cmsc420.meeshquest.part1.PRQuadTree$prQuadInternal")){
			if(x < xMid && y < yMid){
				return containsHelper(root.SW, value, x, y, xLo, xMid, yLo, yMid);
			}
			if(x < xMid && y >= yMid){
				return containsHelper(root.NW, value, x, y, xLo, xMid, yMid, yHi);
			}
			if(x >= xMid && y < yMid){
				return containsHelper(root.SE, value, x, y, xMid, xHi, yLo, yMid);
			}
			if(x >= xMid && y >= yMid){
				return containsHelper(root.NE, value, x, y, xMid, xHi, yMid, yHi);
			}
		}
		return false;
	}
	
	
	public void delete (String value, TreeMap<String, City> nameToCity) {
		double x = ((City)nameToCity.get(value)).getX(); 
		double y = ((City)nameToCity.get(value)).getY();
		root = deleteHelper(this.root, value, x, y, xMin, xMax, yMin, yMax);	
	}
	
	
	private prQuadNode deleteHelper(prQuadNode root,String value,double x, double y, double xLo, double xHi, double yLo, double yHi) {
		
		double xMid = (xLo + xHi)/2;
		double yMid = (yLo + yHi)/2;
		
		if (root == null){
			return null;
		}
		
		if(root.getClass().getName().equals("cmsc420.meeshquest.part1.PRQuadTree$prQuadLeaf") && root.city.getName().equals(value)){
			canvas.removePoint(value, x, y, Color.BLACK);
			root = null;
		}
		else if(root.getClass().getName().equals("cmsc420.meeshquest.part1.PRQuadTree$prQuadInternal")){
			if(x < xMid && y < yMid){
				root.SW = deleteHelper(root.SW, value, x, y, xLo, xMid, yLo, yMid);
			}
			if(x < xMid && y >= yMid){
				root.NW = deleteHelper(root.NW, value, x, y, xLo, xMid, yMid, yHi);
			}
			if(x >= xMid && y < yMid){
				root.SE = deleteHelper(root.SE, value, x, y, xMid, xHi, yLo, yMid);
			}
			if(x >= xMid && y >= yMid){
				root.NE = deleteHelper(root.NE, value, x, y, xMid, xHi, yMid, yHi);
			}
			
			if(root.NE == null && root.NW == null && root.SE == null && root.SW == null){
				canvas.removeCross(xMid, yMid, xMid - xLo, Color.BLACK);
				root = null;
			}
			
			else if(root.NE != null && root.NE.getClass().getName().equals("cmsc420.meeshquest.part1.PRQuadTree$prQuadLeaf") 
					&& root.NW == null && root.SE == null && root.SW == null){
				canvas.removeCross(xMid, yMid, xMid - xLo, Color.BLACK);
				root = root.NE;
			}
			else if(root.NW != null && root.NW.getClass().getName().equals("cmsc420.meeshquest.part1.PRQuadTree$prQuadLeaf") 
					&& root.NE == null && root.SE == null && root.SW == null){
				canvas.removeCross(xMid, yMid, xMid - xLo, Color.BLACK);
				root = root.NW;
			}
			else if(root.SE != null && root.SE.getClass().getName().equals("cmsc420.meeshquest.part1.PRQuadTree$prQuadLeaf")
					&& root.NW == null && root.NE == null && root.SW == null){
				canvas.removeCross(xMid, yMid, xMid - xLo, Color.BLACK);
				root = root.SE;
			}
			else if(root.SW != null && root.SW.getClass().getName().equals("cmsc420.meeshquest.part1.PRQuadTree$prQuadLeaf")
					&& root.NW == null && root.SE == null && root.NE == null){
				canvas.removeCross(xMid, yMid, xMid - xLo, Color.BLACK);
				root = root.SW;
			}
		
		}
		return root;
	}
	
	public Element printTree (Document results, Element output){
		return printTreeHelper (root, results, output, xMin, xMax, yMin, yMax);	
	}
	
	private Element printTreeHelper (prQuadNode root, Document results, Element output,
			double xLo, double xHi, double yLo, double yHi){
				double xMid = (xLo + xHi)/2;
				double yMid = (yLo + yHi)/2;
		if(root == null){
			Element white = results.createElement("white");
			output.appendChild(white);
			return output;
		}
		else if(root.getClass().getName().equals("cmsc420.meeshquest.part1.PRQuadTree$prQuadLeaf")){
			Element black = results.createElement("black");
			black.setAttribute("name", root.city.getName());
			black.setAttribute("x", String.valueOf(root.city.getX()));
			black.setAttribute("y", String.valueOf(root.city.getY()));
			output.appendChild(black);
			return output;
		}
		
		else if(root.getClass().getName().equals("cmsc420.meeshquest.part1.PRQuadTree$prQuadInternal")){
			Element gray = results.createElement("gray");
			gray.setAttribute("x", String.valueOf((int)xMid));
			gray.setAttribute("y", String.valueOf((int)yMid));
			output.appendChild(printTreeHelper(root.NW, results, gray, xLo, xMid, yMid, yHi));
			output.appendChild(printTreeHelper(root.NE, results, gray, xMid, xHi, yMid, yHi));
			output.appendChild(printTreeHelper(root.SW, results, gray, xLo, xMid, yLo, yMid));
			output.appendChild(printTreeHelper(root.SE, results, gray, xMid, xHi, yLo, yMid));
			return output;
		}
		
		return null;
	}
	
	public TreeMap<String, City> rangeCities(double x, double y, double radius){
		canvas.addPoint("origin", x, y, Color.BLUE);
		canvas.addCircle(x, y, radius, Color.BLUE, false);
		TreeMap<String, City> tree = new TreeMap<String, City>(new CityNameComparator());
		return rangeCitiesHelper(root, x, y, radius, tree);
		
	}
	private TreeMap<String, City> rangeCitiesHelper(prQuadNode root, double x, double y, 
			double radius, TreeMap<String, City> tree) {
		
		if(root == null){
			return tree;
		}
		else if(root.getClass().getName().equals("cmsc420.meeshquest.part1.PRQuadTree$prQuadLeaf")){
			double distance = Math.sqrt(Math.pow((x - root.x),2) + Math.pow((y - root.y),2));
			if( distance <= radius){
				tree.put(root.city.getName(), root.city);
			}		
		}
		else if(root.getClass().getName().equals("cmsc420.meeshquest.part1.PRQuadTree$prQuadInternal")){
			rangeCitiesHelper(root.NW, x, y, radius, tree);
			rangeCitiesHelper(root.NE, x, y, radius, tree);
			rangeCitiesHelper(root.SE, x, y, radius, tree);
			rangeCitiesHelper(root.SW, x, y, radius, tree);
		}
		return tree;
	}
	
	public City nearestCity (double x, double y){
		StoreMin storeMin = new StoreMin(null, Integer.MAX_VALUE);
		
		return nearestCityHelper(root, x, y, storeMin);
		
	}
	private City nearestCityHelper( prQuadNode root, double x, double y, StoreMin storeMin) {
		
		if(root == null){
			return null;
		}
		if(root.getClass().getName().equals("cmsc420.meeshquest.part1.PRQuadTree$prQuadLeaf")){
			double distance = Math.sqrt(Math.pow((x - root.x),2) + Math.pow((y - root.y),2));
			if(distance < storeMin.getMin()){
				storeMin.setMin(distance);
				storeMin.setCity(root.city);
			}	
			if(distance == storeMin.getMin()){
				if (storeMin.getCity().getName().compareTo(root.city.getName()) < 0){
					storeMin.setCity(root.city);
				}
			}
		}
		if(root.getClass().getName().equals("cmsc420.meeshquest.part1.PRQuadTree$prQuadInternal")){
			nearestCityHelper(root.NW, x, y, storeMin);
			nearestCityHelper(root.NE, x, y, storeMin);
			nearestCityHelper(root.SE, x, y, storeMin);
			nearestCityHelper(root.SW, x, y, storeMin);
		}
		return storeMin.getCity();
	}
	
	public boolean isEmpty(){
		if(root == null) return true;
		else return false;
	}
	
	
	public void clear(){
	    root = null;
	}
	
}


