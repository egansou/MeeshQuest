package cmsc420.meeshquest.part1;


import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cmsc420.drawing.CanvasPlus;
import cmsc420.xml.XmlUtility;

class CityLocationComparator implements Comparator<City>{
	 
	@Override
	public int compare(City o1, City o2) {
		if (o1.getY() > o2.getY()){
			return 1;
		}
		else if (o1.getY() < o2.getY()){
			return -1;
		}
		else {
			if (o1.getX() > o2.getX()){
				return 1;
			}
			if (o1.getX() < o2.getX()){
				return -1;
			}
			else return 0;
		}
	}
}
class CityNameComparator implements Comparator<String>{
	 
	@Override
	public int compare(String o1, String o2) {
		if (o1.compareTo(o2) > 0){
			return -1;
		}
		else if (o1.compareTo(o2) < 0){
			return 1;
		}
		
		else return 0;
	}
}


public class MeeshQuest {

    public static void main(String[] args) {
    	
    	TreeMap<String, City> treeNameToCity = new TreeMap<String, City>(new CityNameComparator());
		TreeMap<City, String> treeCityToName = new TreeMap<City, String>(new CityLocationComparator());
    	
		PRQuadTree prQuadTree = null;
		CanvasPlus canvas = new CanvasPlus("MeeshQuest");;

		
    	Document results = null;
    	
        try {
        	Document doc = XmlUtility.validateNoNamespace(System.in);
        	//Document doc = XmlUtility.validateNoNamespace(new File("./src/part1.createCity1.input.xml"));
        	//Document doc = XmlUtility.validateNoNamespace(new File("./src/part1.primary.input.xml"));
        	results = XmlUtility.getDocumentBuilder().newDocument();
        	results.setXmlStandalone(true);
        	
        	// root element
            Element rootElement = results.createElement("results");
            results.appendChild(rootElement);
            
            Element commandNode = doc.getDocumentElement();
        	
            int spatialWidth = Integer.parseInt(commandNode.getAttribute("spatialWidth"));
        	int spatialHeight = Integer.parseInt(commandNode.getAttribute("spatialHeight"));;
        	
        	
        	
    		prQuadTree = new PRQuadTree(0, spatialWidth, 0, spatialHeight, canvas);
        	prQuadTree.setCanvas();
        	
        	final NodeList nl = commandNode.getChildNodes();
        	for (int i = 0; i < nl.getLength(); i++) {
        		if (nl.item(i).getNodeType() == Document.ELEMENT_NODE) {
        			
        			commandNode = (Element) nl.item(i);
        			
        			if (commandNode.getTagName().equals("createCity")){
        				
        				Element success = null; 
        				
        				String val_name = commandNode.getAttribute("name");
        				String val_x = commandNode.getAttribute("x");
        				String val_y = commandNode.getAttribute("y");
        				String val_radius = commandNode.getAttribute("radius");
        				String val_color = commandNode.getAttribute("color");
        				
        				boolean dup = false;
        				
        				for(Map.Entry<City, String> entry : treeCityToName.entrySet()) {
      					  City key = entry.getKey();
      					  if (key.getX() == Integer.parseInt(val_x) && key.getY() == Integer.parseInt(val_y)){
      						  dup = true;
      						  break;
      					  }
        				}
        				 
        				if(dup){
        					success = results.createElement("error");
        					success.setAttribute("type", "duplicateCityCoordinates");
            				rootElement.appendChild(success);
        				}
        				else if(treeNameToCity.containsKey(val_name)){
        					success = results.createElement("error");
        					success.setAttribute("type", "duplicateCityName");
            				rootElement.appendChild(success);
        				}
        				else {
        					success = results.createElement("success");
            				rootElement.appendChild(success);
            				City city = new City(val_name, Integer.parseInt(val_x),
            						Integer.parseInt(val_y), Integer.parseInt(val_radius), val_color);
        					treeNameToCity.put(val_name, city);
        					treeCityToName.put(city, val_name);
        				}
        				
        				Element command = results.createElement("command");
        				command.setAttribute("name", "createCity");
        				success.appendChild(command);
        				
        				Element parameters = results.createElement("parameters");
        				success.appendChild(parameters);
        				
        				Element name = results.createElement("name");
        				name.setAttribute("value", val_name);
        				parameters.appendChild(name);
        				
        				Element x = results.createElement("x");
        				x.setAttribute("value", val_x);
        				parameters.appendChild(x);
        				
        				Element y = results.createElement("y");
        				y.setAttribute("value", val_y);
        				parameters.appendChild(y);
        				
        				Element radius = results.createElement("radius");
        				radius.setAttribute("value", val_radius);
        				parameters.appendChild(radius);
        				
        				Element color = results.createElement("color");
        				color.setAttribute("value", val_color);
        				parameters.appendChild(color);
        				
        				if(success.getTagName().equals("success")){
        					Element output = results.createElement("output");
        					success.appendChild(output);
        				}
        			}
        
        			else if (commandNode.getTagName().equals("deleteCity")){
        				
        				String val_name = commandNode.getAttribute("name");
        				String val_x = null;
        				String val_y = null;
        				String val_color = null;
        				String val_radius = null;
        				Element success = null;
        				boolean checked = false;
        				
        				if(!treeNameToCity.containsKey(val_name)){
        					success = results.createElement("error");
        					success.setAttribute("type", "cityDoesNotExist");
        					rootElement.appendChild(success);
        				}
        				
        				else {
        					if(prQuadTree.contains(val_name, treeNameToCity)){
        						prQuadTree.delete(val_name, treeNameToCity);
        						checked = true;
        					}
        					success = results.createElement("success");
            				rootElement.appendChild(success);
            				
            				City city = treeNameToCity.get(val_name);
            				val_x = String.valueOf(city.getX());
            				val_y = String.valueOf(city.getY());
            				val_color = String.valueOf(city.getColor());
            				val_radius = String.valueOf(city.getRadius());
            				treeNameToCity.remove(val_name);
            				treeCityToName.remove(city);
        				}
        				
        				Element command = results.createElement("command");
        				command.setAttribute("name", "deleteCity");
        				success.appendChild(command);
        				
        				Element parameters = results.createElement("parameters");
        				success.appendChild(parameters);
        				
        				Element name = results.createElement("name");
        				name.setAttribute("value", val_name);
        				parameters.appendChild(name);
        				
        				if(success.getTagName().equals("success")){
        					Element output = results.createElement("output");
        					if(checked == true){
        						Element unmap = results.createElement("cityUnmapped");
        						unmap.setAttribute("name", val_name);
        						unmap.setAttribute("x", val_x);
        						unmap.setAttribute("y", val_y);
        						unmap.setAttribute("color", val_color);
        						unmap.setAttribute("radius", val_radius);
        						output.appendChild(unmap);
        					}
        					success.appendChild(output);
        				}
        			}
           			
        			else if (commandNode.getTagName().equals("listCities")){
        				
        				Element success = null;
        				
        				if(treeNameToCity.isEmpty() && treeCityToName.isEmpty()){
        					success = results.createElement("error");
        					success.setAttribute("type", "noCitiesToList");
        					rootElement.appendChild(success);
        				}
        				else{
        					success = results.createElement("success");
        					rootElement.appendChild(success);
        				}
        			     			
        				Element command = results.createElement("command");
        				command.setAttribute("name", "listCities");
        				success.appendChild(command);
        				
        				if(commandNode.getAttribute("sortBy").equals("name")){
        					Element parameters = results.createElement("parameters");
            				success.appendChild(parameters);
            				
            				Element sort = results.createElement("sortBy");
            				String val_sort = commandNode.getAttribute("sortBy");
            				sort.setAttribute("value", val_sort);
            				parameters.appendChild(sort);
            				
            				if(success.getTagName().equals("success")){
            					Element output = results.createElement("output");
            					success.appendChild(output);
            				
            					Element cityList = results.createElement("cityList");
            					output.appendChild(cityList);
            					for(Map.Entry<String, City> entry : treeNameToCity.entrySet()) {
            						String key = entry.getKey();
            						City value = entry.getValue();
            					 
            						Element city = results.createElement("city");
            						city.setAttribute("name", key);
            						city.setAttribute("y", String.valueOf(value.getY()));
            						city.setAttribute("x", String.valueOf(value.getX()));
            						city.setAttribute("radius", String.valueOf(value.getRadius()));
            						city.setAttribute("color", value.getColor());
            					  
            						cityList.appendChild(city);
            					}
            				}	
        				}
        				
        				if(commandNode.getAttribute("sortBy").equals("coordinate")){
        					Element parameters = results.createElement("parameters");
            				success.appendChild(parameters);
            				
            				Element sort = results.createElement("sortBy");
            				String val_sort = commandNode.getAttribute("sortBy");
            				sort.setAttribute("value", val_sort);
            				parameters.appendChild(sort);
            				if(success.getTagName().equals("success")){
            					Element output = results.createElement("output");
            					success.appendChild(output);
            				
            					Element cityList = results.createElement("cityList");
            					output.appendChild(cityList);
            					for(Map.Entry<City, String> entry : treeCityToName.entrySet()) {
            						City key = entry.getKey();
            						String value = entry.getValue();
            					 
            						Element city = results.createElement("city");
            						city.setAttribute("name", value);
            						city.setAttribute("y", String.valueOf(key.getY()));
            						city.setAttribute("x", String.valueOf(key.getX()));
            						city.setAttribute("radius", String.valueOf(key.getRadius()));
            						city.setAttribute("color", key.getColor());
            					  
            						cityList.appendChild(city);
            					}
            				}
            				
        				}
        			}
      
        			else if (commandNode.getTagName().equals("clearAll")){
        				prQuadTree.clear();
        				treeNameToCity.clear();
        				treeCityToName.clear();
        				Element success = results.createElement("success");
        				rootElement.appendChild(success);
        				
        				Element command = results.createElement("command");
        				command.setAttribute("name", "clearAll");
        				success.appendChild(command);
        				
        				Element parameters = results.createElement("parameters");
        				success.appendChild(parameters);
        				
        				Element output = results.createElement("output");
        				success.appendChild(output);
        			}
        			
        			else if (commandNode.getTagName().equals("mapCity")){
        				
        				String val_name = commandNode.getAttribute("name");
        				City city = treeNameToCity.get(val_name);
        				
        				Element success = null;
        				
        				if(!treeNameToCity.containsKey(val_name)){
        					success = results.createElement("error");
        					success.setAttribute("type", "nameNotInDictionary");
        					rootElement.appendChild(success);
        				}
        				
        				else if(prQuadTree.contains(val_name, treeNameToCity)){
        					success = results.createElement("error");
        					success.setAttribute("type", "cityAlreadyMapped");
        					rootElement.appendChild(success);
        				}
        				
        				else if(city.getX() > spatialWidth || city.getX() < 0 || city.getY() > spatialHeight || city.getY() < 0){
        					success = results.createElement("error");
        					success.setAttribute("type", "cityOutOfBounds");
        					rootElement.appendChild(success);
        				}
        				else {
            				prQuadTree.insert(city);
        					success = results.createElement("success");
            				rootElement.appendChild(success);	
        				}
        				
        				Element command = results.createElement("command");
        				command.setAttribute("name", "mapCity");
        				success.appendChild(command);
        				
        				Element parameters = results.createElement("parameters");
        				success.appendChild(parameters);
        				
        				Element name = results.createElement("name");
        				name.setAttribute("value", val_name);
        				parameters.appendChild(name);
        				
        				if(success.getTagName().equals("success")){
        					Element output = results.createElement("output");
        					success.appendChild(output);
        				}
        			}
        			
        			else if (commandNode.getTagName().equals("unmapCity")){
        				
        				String val_name = commandNode.getAttribute("name");
        				City city = treeNameToCity.get(val_name);
        				
        				Element success = null;
        				
        				if(!treeNameToCity.containsKey(val_name)){
        					success = results.createElement("error");
        					success.setAttribute("type", "nameNotInDictionary");
        					rootElement.appendChild(success);
        				}
        				else if(!prQuadTree.contains(val_name, treeNameToCity)){
        					success = results.createElement("error");
        					success.setAttribute("type", "cityNotMapped");
        					rootElement.appendChild(success);
        				}
        				
        				else {
            				prQuadTree.delete(city.name, treeNameToCity);
        					success = results.createElement("success");
            				rootElement.appendChild(success);	
        				}
        				
        				Element command = results.createElement("command");
        				command.setAttribute("name", "unmapCity");
        				success.appendChild(command);
        				
        				Element parameters = results.createElement("parameters");
        				success.appendChild(parameters);
        				
        				Element name = results.createElement("name");
        				name.setAttribute("value", val_name);
        				parameters.appendChild(name);
        				
        				if(success.getTagName().equals("success")){
        					Element output = results.createElement("output");
        					success.appendChild(output);
        				}
        			}
        			
        			else if (commandNode.getTagName().equals("saveMap")){
        				String val_name = commandNode.getAttribute("name");
        				
        				Element success = results.createElement("success");
        				rootElement.appendChild(success);
        				
        				Element command = results.createElement("command");
        				command.setAttribute("name", "saveMap");
        				success.appendChild(command);
        				
        				Element parameters = results.createElement("parameters");
        				success.appendChild(parameters);
        				
        				Element name = results.createElement("name");
        				name.setAttribute("value", val_name);
        				parameters.appendChild(name);
        				
        				/* Saving a map to an image file */
        				canvas.save(val_name);
        				canvas.dispose();
        				
        				Element output = results.createElement("output");
        				success.appendChild(output);
        			}
        			
        			else if (commandNode.getTagName().equals("nearestCity")){
        				Element success = null;
        				String val_x = commandNode.getAttribute("x");
        				String val_y = commandNode.getAttribute("y");
        				City val_city = null;
        				
        				if(prQuadTree.isEmpty()){
        					success = results.createElement("error");
        					success.setAttribute("type", "mapIsEmpty");
        					rootElement.appendChild(success);
        				}
        				
        				else {
        					success = results.createElement("success");
        					val_city = prQuadTree.nearestCity(Double.parseDouble(val_x),Double.parseDouble(val_y));
        					rootElement.appendChild(success);
        				}
        				
        				Element command = results.createElement("command");
        				command.setAttribute("name", "nearestCity");
        				success.appendChild(command);
        				
        				Element parameters = results.createElement("parameters");
        				success.appendChild(parameters);
        				
        				Element x = results.createElement("x");
        				x.setAttribute("value", val_x);
        				parameters.appendChild(x);
        				
        				Element y = results.createElement("y");
        				y.setAttribute("value", val_y);
        				parameters.appendChild(y);
        				
        				if(success.getTagName().equals("success")){
        					Element output = results.createElement("output");
        					
        					Element city = results.createElement("city");
        					city.setAttribute("name", val_city.getName());
        					city.setAttribute("y", String.valueOf(val_city.getY()));
        					city.setAttribute("x", String.valueOf(val_city.getX()));
        					city.setAttribute("radius", String.valueOf(val_city.getRadius()));
        					city.setAttribute("color", val_city.getColor());
        					
        					output.appendChild(city);
        					success.appendChild(output);
        				}
        			}
        			
        			else if (commandNode.getTagName().equals("rangeCities")){
        				Element success = null;
        				String val_x = commandNode.getAttribute("x");
        				String val_y = commandNode.getAttribute("y");
        				String val_radius = commandNode.getAttribute("radius");
        				String val_saveMap = commandNode.getAttribute("saveMap");
        				
        				TreeMap <String, City> cities = prQuadTree.rangeCities(Double.parseDouble(val_x), Double.parseDouble(val_y), 
        						Double.parseDouble(val_radius));
        				
        				if(cities.isEmpty()){
        					success = results.createElement("error");
        					success.setAttribute("type", "noCitiesExistInRange");
        					rootElement.appendChild(success);
        				}
        				
        				else{
        					success = results.createElement("success");
        					rootElement.appendChild(success);
        				}
        				
        				Element command = results.createElement("command");
        				command.setAttribute("name", "rangeCities");
        				success.appendChild(command);
        				
        				Element parameters = results.createElement("parameters");
        				success.appendChild(parameters);
        				
        				Element x = results.createElement("x");
        				x.setAttribute("value", val_x);
        				parameters.appendChild(x);
        				
        				Element y = results.createElement("y");
        				y.setAttribute("value", val_y);
        				parameters.appendChild(y);
        				
        				Element radius = results.createElement("radius");
        				radius.setAttribute("value", val_radius);
        				parameters.appendChild(radius);
        				
        				if (!val_saveMap.equals("")){
        					Element saveMap = results.createElement("saveMap");
        					saveMap.setAttribute("value", val_saveMap);
        					parameters.appendChild(saveMap);
        					canvas.save(val_saveMap);
        					canvas.dispose();
        				}
        				
        				if(success.getTagName().equals("success")){
        					Element output = results.createElement("output");
        					Element cityList = results.createElement("cityList");
        					output.appendChild(cityList);
        					for(Map.Entry<String, City> entry : cities.entrySet()) {
        						String key = entry.getKey();
        						City value = entry.getValue();
        					 
        						Element city = results.createElement("city");
        						city.setAttribute("name", key);
        						city.setAttribute("y", String.valueOf(value.getY()));
        						city.setAttribute("x", String.valueOf(value.getX()));
        						city.setAttribute("radius", String.valueOf(value.getRadius()));
        						city.setAttribute("color", value.getColor());
        
        						cityList.appendChild(city);
        					}	
        					success.appendChild(output);
        				}
        			}
        			
        			
        			else if (commandNode.getTagName().equals("printPRQuadtree")){
        				Element success = null;
        				
        				if(prQuadTree.isEmpty()){
        					success = results.createElement("error");
        					success.setAttribute("type", "mapIsEmpty");
        					rootElement.appendChild(success);
        				}
        				else {
        					success = results.createElement("success");
        					rootElement.appendChild(success);
        				}
        				
        				Element command = results.createElement("command");
        				command.setAttribute("name", "printPRQuadtree");
        				success.appendChild(command);
        				
        				Element parameters = results.createElement("parameters");
        				success.appendChild(parameters);
        				
        				if(success.getTagName().equals("success")){
        					Element output = results.createElement("output");
        					Element quadtree = results.createElement("quadtree");
        					output.appendChild(prQuadTree.printTree(results, quadtree));
        					success.appendChild(output);
        				}
        			}
        			
        			
        		}
        		
        	}
        	
        	canvas.draw();
        } catch (SAXException | IOException | ParserConfigurationException e) {
        	e.printStackTrace();
        	try {
    			results = XmlUtility.getDocumentBuilder().newDocument();
    			final Element fatalError = results.createElement("fatalError");
    			results.appendChild(fatalError);
    		} catch (ParserConfigurationException exception) {
    			System.exit(-1);
    		}
        	
		} finally {
			
            try {
				XmlUtility.print(results);
				
				//System.out.println(prQuadTree.show());
			} catch (TransformerException e) {
				e.printStackTrace();
			}
        }
    }
}
