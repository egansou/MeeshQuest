/**
 * @(#)Command.java        1.1 
 * 
 * 2014/09/09
 *
 * @author Ruofei Du, Ben Zoller (University of Maryland, College Park), 2014
 * 
 * All rights reserved. Permission is granted for use and modification in CMSC420 
 * at the University of Maryland.
 */
package cmsc420.command;

import java.awt.Color;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.drawing.CanvasPlus;
import cmsc420.exception.CityAlreadyMappedException;
import cmsc420.exception.CityOutOfBoundsException;
import cmsc420.exception.RoadAlreadyMappedException;
import cmsc420.exception.RoadOutOfBoundsException;
import cmsc420.exception.StartOrEndIsIsolatedExeption;
import cmsc420.geom.Circle2D;
import cmsc420.geom.Shape2DDistanceCalculator;
import cmsc420.sortedmap.Treap;
import cmsc420.structure.City;
import cmsc420.structure.CityLocationComparator;
import cmsc420.structure.CityNameComparator;
//import cmsc420.structure.prquadtree.InternalNode;
//import cmsc420.structure.prquadtree.LeafNode;
//import cmsc420.structure.prquadtree.Node;
//import cmsc420.structure.prquadtree.PRQuadtree;
import cmsc420.structure.pmquadtree.InternalNode;
import cmsc420.structure.pmquadtree.LeafNode;
import cmsc420.structure.pmquadtree.Node;
import cmsc420.structure.pmquadtree.PMQuadtree;
import cmsc420.structure.pmquadtree.StartEndComparator;
import cmsc420.utils.Canvas;
import cmsc420.xml.XmlUtility;


/**
 * Processes each command in the MeeshQuest program. Takes in an XML command
 * node, processes the node, and outputs the results.
 * 
 * @author Ben Zoller
 * Modified by Enock Gansou
 * @version 2.0, 23 Jan 2007
 */
public class Command {
	/** output DOM Document tree */
	protected Document results;

	/** root node of results document */
	protected Element resultsNode;
	

	/**
	 * stores created cities sorted by their names (used with listCities command)
	 */
	protected final TreeMap<String, City> citiesByName = new TreeMap<String, City>(new Comparator<String>() {

		@Override
		public int compare(String o1, String o2) {
			return o2.compareTo(o1);
		}

	});

	/**
	 * stores created cities sorted by their locations (used with listCities command)
	 */
	protected final TreeSet<City> citiesByLocation = new TreeSet<City>(
			new CityLocationComparator());

	protected final TreeMap<City, Integer> allMappedCitiesByName = new TreeMap<City, Integer>(new Comparator<City>() {
		

		@Override
		public int compare(City o1, City o2) {
				return o2.getName().compareTo(o1.getName());
			}
	});
	
	protected final HashSet<String> roadEndpoints = new HashSet<String>();
	protected final Treap<String, City> citiesByNameTreap = new Treap<String, City>();
	protected final TreeMap<String,TreeMap<String,Double>> graph = 
			new TreeMap<String,TreeMap<String,Double>>();
	
	protected final PMQuadtree pmQuadtree = new PMQuadtree();

	/**order, spatial width and height of the PM Quadtree */
	protected int spatialWidth, spatialHeight, order;

	/**
	 * Set the DOM Document tree to send the of processed commands to.
	 * 
	 * Creates the root results node.
	 * 
	 * @param results
	 *            DOM Document tree
	 */
	public void setResults(Document results) {
		this.results = results;
		resultsNode = results.createElement("results");
		results.appendChild(resultsNode);
	}

	/**
	 * Creates a command result element. Initializes the command name.
	 * 
	 * @param node
	 *            the command node to be processed
	 * @return the results node for the command
	 */
	private Element getCommandNode(final Element node) {
		final Element commandNode = results.createElement("command");
		if (!node.getAttribute("id").equals("")) commandNode.setAttribute("id", node.getAttribute("id"));
		commandNode.setAttribute("name", node.getNodeName());
		return commandNode;
	}

	/**
	 * Processes an integer attribute for a command. Appends the parameter to
	 * the parameters node of the results. Should not throw a number format
	 * exception if the attribute has been defined to be an integer in the
	 * schema and the XML has been validated beforehand.
	 * 
	 * @param commandNode
	 *            node containing information about the command
	 * @param attributeName
	 *            integer attribute to be processed
	 * @param parametersNode
	 *            node to append parameter information to
	 * @return integer attribute value
	 */
	private int processIntegerAttribute(final Element commandNode,
			final String attributeName, final Element parametersNode) {
		final String value = commandNode.getAttribute(attributeName);

		if (parametersNode != null) {
			/* add the parameters to results */
			final Element attributeNode = results.createElement(attributeName);
			attributeNode.setAttribute("value", value);
			parametersNode.appendChild(attributeNode);
		}

		/* return the integer value */
		return Integer.parseInt(value);
	}

	/**
	 * Processes a string attribute for a command. Appends the parameter to the
	 * parameters node of the results.
	 * 
	 * @param commandNode
	 *            node containing information about the command
	 * @param attributeName
	 *            string attribute to be processed
	 * @param parametersNode
	 *            node to append parameter information to
	 * @return string attribute value
	 */
	private String processStringAttribute(final Element commandNode,
			final String attributeName, final Element parametersNode) {
		final String value = commandNode.getAttribute(attributeName);

		if (parametersNode != null) {
			/* add parameters to results */
			final Element attributeNode = results.createElement(attributeName);
			attributeNode.setAttribute("value", value);
			parametersNode.appendChild(attributeNode);
		}

		/* return the string value */
		return value;
	}

	/**
	 * Reports that the requested command could not be performed because of an
	 * error. Appends information about the error to the results.
	 * 
	 * @param type
	 *            type of error that occurred
	 * @param command
	 *            command node being processed
	 * @param parameters
	 *            parameters of command
	 */
	private void addErrorNode(final String type, final Element command,
			final Element parameters) {
		final Element error = results.createElement("error");
		error.setAttribute("type", type);
		error.appendChild(command);
		error.appendChild(parameters);
		resultsNode.appendChild(error);
	}

	/**
	 * Reports that a command was successfully performed. Appends the report to
	 * the results.
	 * 
	 * @param command
	 *            command not being processed
	 * @param parameters
	 *            parameters used by the command
	 * @param output
	 *            any details to be reported about the command processed
	 * @return 
	 */
	private Element addSuccessNode(final Element command,
			final Element parameters, final Element output) {
		final Element success = results.createElement("success");
		success.appendChild(command);
		success.appendChild(parameters);
		success.appendChild(output);
		resultsNode.appendChild(success);
		return success;
	}

	/**
	 * Processes the commands node (root of all commands). Gets the spatial
	 * width and height of the map and send the data to the appropriate data
	 * structures.
	 * 
	 * @param node
	 *            commands node to be processed
	 */
	public void processCommands(final Element node) {
		spatialWidth = Integer.parseInt(node.getAttribute("spatialWidth"));
		spatialHeight = Integer.parseInt(node.getAttribute("spatialHeight"));
		order = Integer.parseInt(node.getAttribute("pmOrder"));

		/* initialize canvas */
		Canvas.instance.setFrameSize(spatialWidth, spatialHeight);
		/* add a rectangle to show where the bounds of the map are located */
		Canvas.instance.addRectangle(0, 0, (spatialWidth > spatialHeight) ? spatialWidth : spatialHeight, 
				(spatialWidth > spatialHeight) ? spatialWidth : spatialHeight, Color.WHITE, true);
		Canvas.instance.addRectangle(0, 0, spatialWidth, spatialHeight, Color.BLACK,
				false);

		/* set PR Quadtree range */
		//prQuadtree.setRange(spatialWidth, spatialHeight);
		pmQuadtree.setRange(spatialWidth, spatialHeight);
	}

	/**
	 * Processes a createCity command. Creates a city in the dictionary (Note:
	 * does not map the city). An error occurs if a city with that name or
	 * location is already in the dictionary.
	 * 
	 * @param node
	 *            createCity node to be processed
	 */
	public void processCreateCity(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int radius = processIntegerAttribute(node, "radius",
				parametersNode);
		final String color = processStringAttribute(node, "color",
				parametersNode);

		/* create the city */
		final City city = new City(name, x, y, radius, color);

		if (citiesByLocation.contains(city)) {
			addErrorNode("duplicateCityCoordinates", commandNode,
					parametersNode);
		} else if (citiesByName.containsKey(name)) {
			addErrorNode("duplicateCityName", commandNode, parametersNode);
		} else {
			final Element outputNode = results.createElement("output");

			/* add city to dictionary */
			citiesByName.put(name, city);
			citiesByLocation.add(city);
			citiesByNameTreap.put(name, city);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Processes a deleteCity command. Deletes a city from the dictionary. An
	 * error occurs if the city does not exist or is currently mapped.
	 * 
	 * @param node
	 *            deleteCity node being processed
	 */
//	public void processDeleteCity(final Element node) {
//		final Element commandNode = getCommandNode(node);
//		final Element parametersNode = results.createElement("parameters");
//		final String name = processStringAttribute(node, "name", parametersNode);
//
//		if (!citiesByName.containsKey(name)) {
//			/* city with name does not exist */
//			addErrorNode("cityDoesNotExist", commandNode, parametersNode);
//		} else {
//			/* delete city */
//			final Element outputNode = results.createElement("output");
//			final City deletedCity = citiesByName.get(name);
//
//			if (prQuadtree.contains(name)) {
//				/* city is mapped */
//				prQuadtree.remove(deletedCity);
//				addCityNode(outputNode, "cityUnmapped", deletedCity);
//			}
//
//			citiesByName.remove(name);
//			citiesByLocation.remove(deletedCity);
//
//			/* add success node to results */
//			addSuccessNode(commandNode, parametersNode, outputNode);
//		}
//	}

	/**
	 * Clears all the data structures do there are not cities or roads in
	 * existence in the dictionary or on the map.
	 * 
	 * @param node
	 *            clearAll node to be processed
	 */
	public void processClearAll(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* clear data structures */
		citiesByName.clear();
		citiesByLocation.clear();
		allMappedCitiesByName.clear();
		citiesByNameTreap.clear();
		pmQuadtree.clear();

		/* clear canvas */
		Canvas.instance.clear();
		/* add a rectangle to show where the bounds of the map are located */
		Canvas.instance.addRectangle(0, 0, spatialWidth, spatialHeight, Color.BLACK,
				false);

		/* add success node to results */
		addSuccessNode(commandNode, parametersNode, outputNode);
	}

	/**
	 * Lists all the cities, either by name or by location.
	 * 
	 * @param node
	 *            listCities node to be processed
	 */
	public void processListCities(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final String sortBy = processStringAttribute(node, "sortBy",
				parametersNode);

		if (citiesByName.isEmpty()) {
			addErrorNode("noCitiesToList", commandNode, parametersNode);
		} else {
			final Element outputNode = results.createElement("output");
			final Element cityListNode = results.createElement("cityList");

			Collection<City> cityCollection = null;
			if (sortBy.equals("name")) {
				cityCollection = citiesByName.values();
			} else if (sortBy.equals("coordinate")) {
				cityCollection = citiesByLocation;
			} else {
				/* XML validator failed */
				System.exit(-1);
			}

			for (City c : cityCollection) {
				addCityNode(cityListNode, c);
			}
			outputNode.appendChild(cityListNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Creates a city node containing information about a city. Appends the city
	 * node to the passed in node.
	 * 
	 * @param node
	 *            node which the city node will be appended to
	 * @param cityNodeName
	 *            name of city node
	 * @param city
	 *            city which the city node will describe
	 */
	private void addCityNode(final Element node, final String cityNodeName,
			final City city) {
		final Element cityNode = results.createElement(cityNodeName);
		cityNode.setAttribute("name", city.getName());
		cityNode.setAttribute("x", Integer.toString((int) city.getX()));
		cityNode.setAttribute("y", Integer.toString((int) city.getY()));
		cityNode.setAttribute("radius", Integer
				.toString((int) city.getRadius()));
		cityNode.setAttribute("color", city.getColor());
		node.appendChild(cityNode);
	}

	/**
	 * Creates a city node containing information about a city. Appends the city
	 * node to the passed in node.
	 * 
	 * @param node
	 *            node which the city node will be appended to
	 * @param city
	 *            city which the city node will describe
	 */
	private void addCityNode(final Element node, final City city) {
		addCityNode(node, "city", city);
	}
	
	private void addIsolatedCityNode(final Element node, final City city) {
		addCityNode(node, "isolatedCity", city);
	}

	/**
	 * Maps a city to the spatial map.
	 * 
	 * @param node
	 *            mapCity command node to be processed
	 */
	public void processMapCity(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);

		final Element outputNode = results.createElement("output");

		if (!citiesByName.containsKey(name)) {
			addErrorNode("nameNotInDictionary", commandNode, parametersNode);
		} else if (pmQuadtree.contains(name)) {
			addErrorNode("cityAlreadyMapped", commandNode, parametersNode);
		} else {
			City city = citiesByName.get(name);
			try {
				/* insert city into PR Quadtree */
				//prQuadtree.add(city);
				pmQuadtree.addIsolatedCity(city);
				allMappedCitiesByName.put(city, city.getRadius());
				if(!graph.containsKey(name)){
					TreeMap<String, Double> startInfo = new TreeMap<String, Double>();
					startInfo.put(name, 0.0);
					graph.put(name, startInfo);
				}
				else{
					graph.get(name).put(name, 0.0);
					
				}
				roadEndpoints.add(name);

				/* add city to canvas */
				Canvas.instance.addPoint(city.getName(), city.getX(), city.getY(),
						Color.BLACK);

				/* add success node to results */
				addSuccessNode(commandNode, parametersNode, outputNode);
			} catch (CityAlreadyMappedException e) {
				addErrorNode("cityAlreadyMapped", commandNode, parametersNode);
			} catch (CityOutOfBoundsException e) {
				addErrorNode("cityOutOfBounds", commandNode, parametersNode);
			}
		}
	}

	/**
	 * Maps a road to the spatial map.
	 * 
	 * @param node
	 *            mapRoad command node to be processed
	 */
	public void processMapRoad(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String start = processStringAttribute(node, "start", parametersNode);
		final String end = processStringAttribute(node, "end", parametersNode);

		final Element outputNode = results.createElement("output");

		if (!citiesByName.containsKey(start)) {
			addErrorNode("startPointDoesNotExist", commandNode, parametersNode);
		}
		else if (!citiesByName.containsKey(end)) {
			addErrorNode("endPointDoesNotExist", commandNode, parametersNode);
		}
		else if (start.equals(end)) {
			addErrorNode("startEqualsEnd", commandNode, parametersNode);
		}
		else if (pmQuadtree.isolatedContains(start) || 
				pmQuadtree.isolatedContains(end)) {
			addErrorNode("startOrEndIsIsolated", commandNode, parametersNode);
		}
		else {
			City startCity = citiesByName.get(start);
			City endCity = citiesByName.get(end);
			try {
				/* insert road into PM Quadtree */
				pmQuadtree.addRoad(startCity, endCity);
				if(pmQuadtree.contains(start)) allMappedCitiesByName.put(startCity, startCity.getRadius());
				if(pmQuadtree.contains(end)) allMappedCitiesByName.put(endCity, endCity.getRadius());
				
				double roadlength = Math.sqrt(Math.pow(startCity.getX()-endCity.getX(),2) + 
						Math.pow(startCity.getY()-endCity.getY(),2));
				
				
				if (startCity.getX() >= 0 && startCity.getX() <= spatialWidth 
						&& startCity.getY() >= 0 && startCity.getY() <= spatialHeight &&
						endCity.getX() >= 0 && endCity.getX() <= spatialWidth 
						&& endCity.getY() >= 0 && endCity.getY() <= spatialHeight ) {
					if(!graph.containsKey(start)){
						TreeMap<String, Double> endInfo = new TreeMap<String, Double>();
						endInfo.put(end, roadlength);
						graph.put(start, endInfo);
						
						
					}
					else{
						graph.get(start).put(end, roadlength);
						
					}
					if(!graph.containsKey(end)){
						TreeMap<String, Double> startInfo = new TreeMap<String, Double>();
						startInfo.put(start, roadlength);
						graph.put(end, startInfo);
					}
					else{
						graph.get(end).put(start, roadlength);
						
					}
					roadEndpoints.add(start);
					roadEndpoints.add(end);
				}
				
				final Element roadCreated = results.createElement("roadCreated");
				roadCreated.setAttribute("start", start);
				roadCreated.setAttribute("end", end);
				outputNode.appendChild(roadCreated);
				
				if (pmQuadtree.contains(start)) {
					allMappedCitiesByName.put(startCity, startCity.getRadius());
					Canvas.instance.addPoint(startCity.getName(), startCity.getX(), 
							startCity.getY(), Color.BLACK);
				}
				if (pmQuadtree.contains(end)) {
					allMappedCitiesByName.put(endCity, endCity.getRadius());
					Canvas.instance.addPoint(endCity.getName(), endCity.getX(), 
							endCity.getY(), Color.BLACK);
				}

				/* add road to canvas */
				Canvas.instance.addLine(startCity.getX(), startCity.getY(),
						endCity.getX(), endCity.getY(), Color.BLACK);

				/* add success node to results */
				addSuccessNode(commandNode, parametersNode, outputNode);
			}  catch (RoadAlreadyMappedException e) {
				addErrorNode("roadAlreadyMapped", commandNode, parametersNode);
			} catch (RoadOutOfBoundsException e) {
				addErrorNode("roadOutOfBounds", commandNode, parametersNode);
			} catch (StartOrEndIsIsolatedExeption e) {
				addErrorNode("startOrEndIsIsolated", commandNode, parametersNode);
			}
			
			
		}
	}
	
	/**
	 * Determine the shortest path from a starting to an ending vertex.
	 * 
	 * @param node
	 *            shortestPath command node to be processed
	 */
	public void processShortestPath(final Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String start = processStringAttribute(node, "start", parametersNode);
		final String end = processStringAttribute(node, "end", parametersNode);
		CanvasPlus canvas = null;
		
		String pathFile = "";
		
		if (node.getAttribute("saveMap").compareTo("") != 0) {
			pathFile = processStringAttribute(node, "saveMap", parametersNode);
			canvas = new CanvasPlus();
			canvas.setFrameSize(spatialWidth, spatialHeight);
			/* add a rectangle to show where the bounds of the map are located */
			canvas.addRectangle(0, 0, (spatialWidth > spatialHeight) ? spatialWidth : spatialHeight, 
					(spatialWidth > spatialHeight) ? spatialWidth : spatialHeight, Color.WHITE, true);
			canvas.addRectangle(0, 0, spatialWidth, spatialHeight, Color.BLACK,
					false);
		}
		
		String html = "";
		if (node.getAttribute("saveHTML").compareTo("") != 0) {
			html = processStringAttribute(node, "saveHTML", parametersNode);
		}
		
		final Element outputNode = results.createElement("output");

		if (!pmQuadtree.contains(start)) {
			addErrorNode("nonExistentStart", commandNode, parametersNode);
		} else if (!pmQuadtree.contains(end)) {
			addErrorNode("nonExistentEnd", commandNode, parametersNode);
		} else if (!roadEndpoints.contains(start) || !roadEndpoints.contains(end)) {
			addErrorNode("noPathExists", commandNode, parametersNode);
		} else {
			TreeMap<String, CloseCity> sp = shortestPath(graph, start, end);
			
			final Element pathNode = results.createElement("path");
			
			
			CloseCity obj = sp.get(end); 
			
			if(obj.getDistance() == Double.POSITIVE_INFINITY){
				addErrorNode("noPathExists", commandNode, parametersNode);
				return;
			}
			
			String e = end;
			 
			LinkedList<String> linkedlist1 = new LinkedList<String>();
			LinkedList<CloseCity> linkedlist2 = new LinkedList<CloseCity>();
			while(!e.equals(obj.getComeFrom())) {
				linkedlist1.addFirst(e);
				linkedlist2.addFirst(obj);
				e = obj.getComeFrom();
				obj = sp.get(e); 	
			}
			
			int size = linkedlist1.size();
			Arc2D.Double arc = new Arc2D.Double();
			
			pathNode.setAttribute("hops", String.valueOf(size));
			if(size > 0){
				 int yourScale = 3;
				pathNode.setAttribute("length", String.valueOf(
						BigDecimal.valueOf(linkedlist2.get(size-1).getDistance()).
				setScale(yourScale, BigDecimal.ROUND_HALF_UP)));
			}
			else{
				pathNode.setAttribute("length", "0.000");
//				City startingCity = citiesByName.get(start);
//				if (startingCity != null )canvas.addPoint(startingCity.getName(), 
//						startingCity.getX(), startingCity.getY(), Color.GREEN);
			}
			
			for (int i = 0; i < size; i++){
				int j = i + 1;
				String endingName = linkedlist1.get(i);
				String startingName = linkedlist2.get(i).getComeFrom();
				City startingCity = citiesByName.get(startingName);
				City endingCity = citiesByName.get(endingName); 
				if (pathFile.compareTo("") != 0) {
					if(i == 0) canvas.addPoint(startingCity.getName(), 
							startingCity.getX(), startingCity.getY(), Color.GREEN);
					canvas.addPoint(endingCity.getName(), 
								endingCity.getX(), endingCity.getY(), Color.BLUE);
					canvas.addLine(startingCity.getX(), startingCity.getY()
							, endingCity.getX(), endingCity.getY(), Color.BLUE);
						
						if(i == size-1) canvas.addPoint(endingCity.getName(), 
								endingCity.getX(), endingCity.getY(), Color.RED);
				}
				final Element road = results.createElement("road");
				road.setAttribute("start", startingName);
				road.setAttribute("end", endingName);
				Element direction = null;
				if(j < size){
					String nextName = linkedlist1.get(j);
					Point2D.Float startingPoint = startingCity.pt; 
					Point2D.Float endingPoint = endingCity.pt; 
					Point2D.Float nextPoint = citiesByName.get(nextName).pt; 
					arc.setArcByTangent(startingPoint, endingPoint, nextPoint, 1);
					Double angle;
					//System.out.println(arc.getAngleExtent());
					if (arc.getAngleExtent() == -180) angle = 180.0;
					else angle = arc.getAngleExtent();
					if(arc.getAngleExtent() < -45.0 && arc.getAngleExtent() > -180.0){
						direction  = results.createElement("left");
					}
					else if(angle >= 45.0 && angle <= 180 ){
						direction  = results.createElement("right");
					}
					else direction  = results.createElement("straight");
				}
				pathNode.appendChild(road);
				if(direction != null) pathNode.appendChild(direction);
			}
			outputNode.appendChild(pathNode);
			
			/* add success node to results */
			Element successNode = addSuccessNode(commandNode, parametersNode, outputNode);
			
			if (pathFile.compareTo("") != 0) {
				/* save canvas to file with range circle */
				canvas.save(pathFile);
				//canvas.dispose();
			}
			if (html.compareTo("") != 0) {
				org.w3c.dom.Document shortestPathDoc;
				try {
					shortestPathDoc = XmlUtility.getDocumentBuilder().newDocument();
					org.w3c.dom.Node spNode = shortestPathDoc.importNode(successNode, true);
					XmlUtility.transform(shortestPathDoc, new File("shortestPath.xsl"), new File(html + ".html"));
					shortestPathDoc.appendChild(spNode);
				} catch (ParserConfigurationException e1) {
					e1.printStackTrace();
				} catch (TransformerException e1) {
					e1.printStackTrace();
				}
							
			}
			
		}
	}
	
	
	/**
	 * This method finds the shortest path from a starting to an ending vertex. 
	 * The model I used is from Ivan Palianytsia and the link to his code is
	 * https://github.com/palianytsia/algorithms/blob/master/src/main/java/
	 * edu/stanford/algo/greedy/Dijkstra.java
	 * @param graph the graph with all edges and distance values
	 * @param start the starting vertex
	 * @param end the ending vertex
	 * @return the shortest path 
	 */
	 private TreeMap<String, CloseCity> shortestPath(
			TreeMap<String,TreeMap<String,Double>> graph, String start, String end) {
			final  TreeMap<String, CloseCity> processed = new TreeMap<String, CloseCity>();
			final Map<String, CloseCity> greedyScores = new HashMap<String, CloseCity>();
			
			greedyScores.put(start, new CloseCity(start, 0d));
			for (String v : roadEndpoints) {
			    if (!v.equals(start)) {
				greedyScores.put(v, new CloseCity(null, Double.POSITIVE_INFINITY));
			    }
			}
			
			final PriorityQueue<String> remaining = new PriorityQueue<String>(
					roadEndpoints.size(), new Comparator<String>() {
			    @Override
			    public int compare(String v, String w) {
			    	int cmp = greedyScores.get(v).getDistance().compareTo(greedyScores.get(w).getDistance());
			    	if (cmp == 0) cmp = v.compareTo(w);
			    	return cmp;
			    }
			});
			remaining.addAll(roadEndpoints);
			while (remaining.size() > 0) {
			    String edgeStart = remaining.poll();
			    Double distance = greedyScores.get(edgeStart).getDistance();
			    CloseCity city = greedyScores.get(edgeStart);
			    processed.put(edgeStart, city);
			    
			    TreeMap<String, Double> edges = graph.get(edgeStart);
			    for (Map.Entry<String,Double> e : edges.entrySet()) {
			    	String edgeEnd = e.getKey();
			    	if (remaining.contains(edgeEnd)) {
			    		Double oldKey = greedyScores.get(edgeEnd).getDistance();
			    		if(oldKey > distance + e.getValue())
			    			greedyScores.put(edgeEnd, 
			    				new CloseCity(edgeStart, Math.min(oldKey, distance + e.getValue())));
			    		remaining.remove(edgeEnd);
			    		remaining.add(edgeEnd);
			    	}
			    }
			}
			return processed;
	    }

	/**
	 * Removes a city from the spatial map.
	 * 
	 * @param node
	 *            unmapCity command node to be processed
	 */
//	public void processUnmapCity(Element node) {
//		final Element commandNode = getCommandNode(node);
//		final Element parametersNode = results.createElement("parameters");
//
//		final String name = processStringAttribute(node, "name", parametersNode);
//
//		final Element outputNode = results.createElement("output");
//
//		if (!citiesByName.containsKey(name)) {
//			addErrorNode("nameNotInDictionary", commandNode, parametersNode);
//		} else if (!prQuadtree.contains(name)) {
//			addErrorNode("cityNotMapped", commandNode, parametersNode);
//		} else {
//			City city = citiesByName.get(name);
//
//			/* unmap the city in the PR Quadtree */
//			prQuadtree.remove(city);
//
//			/* remove city from canvas */
//			Canvas.instance.removePoint(city.getName(), city.getX(), city.getY(),
//					Color.BLACK);
//
//			/* add success node to results */
//			addSuccessNode(commandNode, parametersNode, outputNode);
//		}
//	}

	/**
	 * Processes a saveMap command. Saves the graphical map to a given file.
	 * 
	 * @param node
	 *            saveMap command to be processed
	 * @throws IOException
	 *             problem accessing the image file
	 */
	public void processSaveMap(final Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);

		final Element outputNode = results.createElement("output");
		
		/* save canvas to '<name>.png' */
		Canvas.instance.save(name);

		/* add success node to results */
		addSuccessNode(commandNode, parametersNode, outputNode);
	}

	/**
	 * Prints out the structure of the Treap in a human-readable format.
	 * 
	 * @param node
	 *            Treap command to be processed
	 */
	public void processPrintTreap(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		if (citiesByNameTreap.isEmpty()) {
			/* empty Treap */
			addErrorNode("emptyTree", commandNode, parametersNode);
		} else {
			/* print Treap */
			final Element treapNode = results.createElement("treap");
			treapNode.setAttribute("cardinality", String.valueOf(citiesByNameTreap.size()));
			printTreapHelper(citiesByNameTreap.getRoot(), treapNode);

			outputNode.appendChild(treapNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}
	
	/**
	 * Traverses each node of the Treap.
	 * 
	 * @param currentNode
	 *            Treap node being printed
	 * @param xmlNode
	 *            XML node representing the current treap node
	 */
	private void printTreapHelper(final Map.Entry<String, City> currentNode,
			final Element xmlNode) {
		if (currentNode == null) {
			Element emptyChild = results.createElement("emptyChild");
			xmlNode.appendChild(emptyChild);
			return;
		}
		else {
				final Element node = results.createElement("node");
				node.setAttribute("key", currentNode.getKey());
				node.setAttribute("priority", 
						Integer.toString((int) citiesByNameTreap.getPriority(currentNode)));
				node.setAttribute("value", "(" + Integer.toString(currentNode.getValue().getX())
				+ "," + Integer.toString(currentNode.getValue().getY())	+ ")");
				printTreapHelper(citiesByNameTreap.getRight(currentNode), node);
				printTreapHelper(citiesByNameTreap.getLeft(currentNode), node);
				xmlNode.appendChild(node);
		} 
	}	
	
	/**
	 * Prints out the structure of the PM Quadtree in a human-readable format.
	 * 
	 * @param node
	 *            printPMQuadtree command to be processed
	 */
	public void processPrintPMQuadtree(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		if (pmQuadtree.isEmpty()) {
			/* empty PM Quadtree */
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
		} else {
			/* print PM Quadtree */
			final Element quadtreeNode = results.createElement("quadtree");
			quadtreeNode.setAttribute("order", String.valueOf(order));
			printPMQuadtreeHelper(pmQuadtree.getRoot(), quadtreeNode);

			outputNode.appendChild(quadtreeNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}
	
	/**
	 * Traverses each node of the PM Quadtree.
	 * 
	 * @param currentNode
	 *            PM Quadtree node being printed
	 * @param xmlNode
	 *            XML node representing the current PM Quadtree node
	 */
	private void printPMQuadtreeHelper(final Node currentNode,
			final Element xmlNode) {
		if (currentNode.getType() == Node.EMPTY) {
			Element white = results.createElement("white");
			xmlNode.appendChild(white);
		} else {
			if (currentNode.getType() == Node.LEAF) {
				/* leaf node */
				int cardinality = 0;
				final LeafNode currentLeaf = (LeafNode) currentNode;
				final Element black = results.createElement("black");
				City city = currentLeaf.getCity();
				if (city != null){ 
					String name;
					if (pmQuadtree.isolatedContains(city.getName())){
						name = "isolatedCity";
					}
					else name = "city";
					final Element c = results.createElement(name);
					c.setAttribute("name", currentLeaf.getCity().getName());
					c.setAttribute("x", Integer.toString((int) currentLeaf
							.getCity().getX()));
					c.setAttribute("y", Integer.toString((int) currentLeaf
							.getCity().getY()));
					c.setAttribute("color", currentLeaf
							.getCity().getColor());
					c.setAttribute("radius", Integer.toString((int) currentLeaf
							.getCity().getRadius()));
					black.appendChild(c);
					cardinality++;
				}
				int size = currentLeaf.getRoads().size();
				for (ArrayList<City> r : currentLeaf.getRoads()){
					final Element road = results.createElement("road");
					road.setAttribute("start", r.get(0).getName());
					road.setAttribute("end", r.get(1).getName());
					black.appendChild(road);
				}
				cardinality += size;
				black.setAttribute("cardinality", String.valueOf(cardinality));
			
				xmlNode.appendChild(black);
			} else {
				/* internal node */
				final InternalNode currentInternal = (InternalNode) currentNode;
				final Element gray = results.createElement("gray");
				gray.setAttribute("x", Integer.toString((int) currentInternal
						.getCenterX()));
				gray.setAttribute("y", Integer.toString((int) currentInternal
						.getCenterY()));
				for (int i = 0; i < 4; i++) {
					printPMQuadtreeHelper(currentInternal.getChild(i), gray);
				}
				xmlNode.appendChild(gray);
			}
		}
	}

	

	/**
	 * Finds the mapped cities within the range of a given point.
	 * 
	 * @param node
	 *            rangeCities command to be processed
	 * @throws IOException
	 */
	public void processRangeCities(final Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final TreeSet<City> citiesInRange = new TreeSet<City>(
				new CityNameComparator());

		/* extract values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int radius = processIntegerAttribute(node, "radius",
				parametersNode);

		String pathFile = "";
		if (node.getAttribute("saveMap").compareTo("") != 0) {
			pathFile = processStringAttribute(node, "saveMap", parametersNode);
		}
		/* get cities within range */
		final Point2D.Double point = new Point2D.Double(x, y);
		rangeCitiesHelper(point, radius, pmQuadtree.getRoot(), citiesInRange);

		/* print out cities within range */
		if (citiesInRange.isEmpty()) {
			addErrorNode("noCitiesExistInRange", commandNode, parametersNode);
		} else {
			/* get city list */
			final Element cityListNode = results.createElement("cityList");
			for (City city : citiesInRange) {
				addCityNode(cityListNode, city);
			}
			outputNode.appendChild(cityListNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);

			if (pathFile.compareTo("") != 0) {
				/* save canvas to file with range circle */
				if(radius != 0) {
					Canvas.instance.addCircle(x, y, radius, Color.BLUE, false);
				}
				Canvas.instance.save(pathFile);
				if(radius != 0) {
					Canvas.instance.removeCircle(x, y, radius, Color.BLUE, false);
				}
			}
		}
	}

	/**
	 * Determines if any cities within the PM Quadtree not are within the radius
	 * of a given point.
	 * 
	 * @param point
	 *            point from which the cities are measured
	 * @param radius
	 *            radius from which the given points are measured
	 * @param node
	 *            PM Quadtree node being examined
	 * @param citiesInRange
	 *            a list of cities found to be in range
	 */
	private void rangeCitiesHelper(final Point2D.Double point,
			final int radius, final Node node, final TreeSet<City> citiesInRange) {
		if (node.getType() == Node.LEAF && node.getCity() != null) {
			final LeafNode leaf = (LeafNode) node;
			final double distance = point.distance(leaf.getCity().toPoint2D());
			if (distance <= radius) {
				/* city is in range */
				final City city = leaf.getCity();
				citiesInRange.add(city);
			}
		} else if (node.getType() == Node.INTERNAL) {
			/* check each quadrant of internal node */
			final InternalNode internal = (InternalNode) node;

			final Circle2D.Double circle = new Circle2D.Double(point, radius);
			for (int i = 0; i < 4; i++) {
				if (pmQuadtree.intersects(circle, internal.getChildRegion(i))) {
					rangeCitiesHelper(point, radius, internal.getChild(i),
							citiesInRange);
				}
			}
		}
	}
	
	/**
	 * Finds the mapped roads within the range of a given point.
	 * 
	 * @param node
	 *            rangeRoads command to be processed
	 * @throws IOException
	 */
	public void processRangeRoads(final Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final TreeSet<ArrayList<City>> roadsInRange = new TreeSet<ArrayList<City>>(
				new StartEndComparator());

		/* extract values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int radius = processIntegerAttribute(node, "radius",
				parametersNode);

		String pathFile = "";
		if (node.getAttribute("saveMap").compareTo("") != 0) {
			pathFile = processStringAttribute(node, "saveMap", parametersNode);
		}
		/* get cities within range */
		final Point2D.Double point = new Point2D.Double(x, y);
		rangeRoadsHelper(point, radius, pmQuadtree.getRoot(), roadsInRange);

		/* print out cities within range */
		if (roadsInRange.isEmpty()) {
			addErrorNode("noRoadsExistInRange", commandNode, parametersNode);
		} else {
			/* get road list */
			final Element roadListNode = results.createElement("roadList");
			for (ArrayList<City> r : roadsInRange) {
				final Element road = results.createElement("road");
				road.setAttribute("start", r.get(0).getName());
				road.setAttribute("end", r.get(1).getName());
				roadListNode.appendChild(road);
			}
			outputNode.appendChild(roadListNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);

			if (pathFile.compareTo("") != 0) {
				/* save canvas to file with range circle */
				if(radius != 0) {
					Canvas.instance.addCircle(x, y, radius, Color.BLUE, false);
				}
				Canvas.instance.save(pathFile);
				if(radius != 0) {
					Canvas.instance.removeCircle(x, y, radius, Color.BLUE, false);
				}
			}
		}
	}

	/**
	 * Determines if any roads within the PM Quadtree not are within the radius
	 * of a given point.
	 * 
	 * @param point
	 *            point from which the roads are measured
	 * @param radius
	 *            radius from which the given roads are measured
	 * @param node
	 *            PM Quadtree node being examined
	 * @param citiesInRange
	 *            a list of roads found to be in range
	 */
	private void rangeRoadsHelper(final Point2D.Double point,
			final int radius, final Node node, final TreeSet<ArrayList<City>> roadsInRange) {
		if (node.getType() == Node.LEAF && !node.getRoads().isEmpty()) {
			for (ArrayList<City> road : node.getRoads()){
				if(!roadsInRange.contains(road)){
					Line2D.Float r = new Line2D.Float(road.get(0).getX(),road.get(0).getY(),
							road.get(1).getX(),road.get(1).getY());
					final double distance = r.ptSegDist(point);
					if (distance <= radius) {
						/* road is in range */
						roadsInRange.add(road);
					}
				}
			}
		} else if (node.getType() == Node.INTERNAL) {
			/* check each quadrant of internal node */
			final InternalNode internal = (InternalNode) node;

			final Circle2D.Double circle = new Circle2D.Double(point, radius);
			for (int i = 0; i < 4; i++) {
				if (pmQuadtree.intersects(circle, internal.getChildRegion(i))) {
					rangeRoadsHelper(point, radius, internal.getChild(i),
							roadsInRange);
				}
			}
		}
	}
	
	

	/**
	 * Finds the nearest city, given this city is not an isolated city, to a given point.
	 * 
	 * @param node
	 *            nearestCity command being processed
	 */
	public void processNearestCity(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);

		final Point2D.Float point = new Point2D.Float(x, y);

		if (citiesByName.size() <= 0) {
			addErrorNode("cityNotFound", commandNode, parametersNode);
			return;
		}

		if (pmQuadtree.getRoot().getType() == Node.EMPTY) {
			addErrorNode("cityNotFound", commandNode, parametersNode);
		} else if (pmQuadtree.getRoot().getType() == Node.LEAF && pmQuadtree.getCity() == null ) {
			addErrorNode("cityNotFound", commandNode, parametersNode);
		} else if (pmQuadtree.getRoot().getType() == Node.LEAF && pmQuadtree.getCity() != null &&
				pmQuadtree.isolatedContains(pmQuadtree.getCity().getName())) {
			addErrorNode("cityNotFound", commandNode, parametersNode);}
		else {
			City n = nearestCityHelper2(pmQuadtree.getRoot(), point);
			if (n == null) addErrorNode("cityNotFound", commandNode, parametersNode);
			else {
				addCityNode(outputNode, n);
				/* add success node to results */
				addSuccessNode(commandNode, parametersNode, outputNode);
			}
		}
	}

	/**
	 * 2/25/2011
	 * @param root
	 * @param point
	 */
	private City nearestCityHelper2(Node root, Point2D.Float point) {
		PriorityQueue<QuadrantDistance> q = new PriorityQueue<QuadrantDistance>();
		Node currNode = root;
		while (currNode.getType() != Node.LEAF ) {
			InternalNode g = (InternalNode) currNode;
			for (int i = 0; i < 4; i++) {
				Node kid = g.children[i];
				if (kid.getType() == Node.INTERNAL || 
					   (kid.getType() == Node.LEAF && kid.getCity() != null 
					   && !pmQuadtree.isolatedContains(kid.getCity().getName()))) {
					q.add(new QuadrantDistance(kid, point));
				}
			}
			if(!q.isEmpty()) currNode = q.remove().quadtreeNode;
			else return null;
		}

		return ((LeafNode) currNode).getCity();
	}
	
	/**
	 * Finds the nearest isolated city to a given point.
	 * 
	 * @param node
	 *            nearestIsolatatedCity command being processed
	 */
	public void processNearestIsolatedCity(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);

		final Point2D.Float point = new Point2D.Float(x, y);

		if (citiesByName.size() <= 0) {
			addErrorNode("cityNotFound", commandNode, parametersNode);
			return;
		}
		
		if (pmQuadtree.getRoot().getType() == Node.EMPTY) {
			addErrorNode("cityNotFound", commandNode, parametersNode);
		} else if (pmQuadtree.getRoot().getType() == Node.LEAF && pmQuadtree.getCity() == null ) {
			addErrorNode("cityNotFound", commandNode, parametersNode);
		} else if (pmQuadtree.getRoot().getType() == Node.LEAF && pmQuadtree.getCity() != null &&
				!pmQuadtree.isolatedContains(pmQuadtree.getCity().getName())) {
			addErrorNode("cityNotFound", commandNode, parametersNode);}
		else {

			City n = nearestIsolatedCityHelper2(pmQuadtree.getRoot(), point);
			if (n == null) addErrorNode("cityNotFound", commandNode, parametersNode);
			else {
				addIsolatedCityNode(outputNode, n);
				/* add success node to results */
				addSuccessNode(commandNode, parametersNode, outputNode);
			}
		}
	}
	
	/**
	 * @param root
	 * @param point
	 */
	private City nearestIsolatedCityHelper2(Node root, Point2D.Float point) {
		PriorityQueue<QuadrantDistance> q = new PriorityQueue<QuadrantDistance>();
		Node currNode = root;
		while (currNode.getType() != Node.LEAF) {
			InternalNode g = (InternalNode) currNode;
			for (int i = 0; i < 4; i++) {
				Node kid = g.children[i];
				if (kid.getType() == Node.INTERNAL ||
					   (kid.getType() == Node.LEAF && kid.getCity() != null
					   && pmQuadtree.isolatedContains(kid.getCity().getName()))) {
					q.add(new QuadrantDistance(kid, point));
				}
			}
			if(!q.isEmpty()) currNode = q.remove().quadtreeNode;
			else return null;
		}

		return ((LeafNode) currNode).getCity();
	}

	
	class QuadrantDistance implements Comparable<QuadrantDistance> {
		public Node quadtreeNode;
		private double distance;

		public QuadrantDistance(Node node, Point2D.Float pt) {
			quadtreeNode = node;
			if (node.getType() == Node.INTERNAL) {
				InternalNode gray = (InternalNode) node;
				distance = Shape2DDistanceCalculator.distance(pt, 
						new Rectangle2D.Float(gray.origin.x, gray.origin.y, gray.width, gray.height));
			} else if (node.getType() == Node.LEAF) {
				LeafNode leaf = (LeafNode) node;
				distance = pt.distance(leaf.getCity().pt);
			} else {
				throw new IllegalArgumentException("Only leaf or internal node can be passed in");
			}
		}

		public int compareTo(QuadrantDistance qd) {
			if (distance < qd.distance) {
				return -1;
			} else if (distance > qd.distance) {
				return 1;
			} else {
				if (quadtreeNode.getType() != qd.quadtreeNode.getType()) {
					if (quadtreeNode.getType() == Node.INTERNAL) {
						return -1;
					} else {
						return 1;
					}
				} else if (quadtreeNode.getType() == Node.LEAF) {
					// both are leaves
					return ((LeafNode) qd.quadtreeNode).getCity().getName().compareTo(
							((LeafNode) quadtreeNode).getCity().getName());
				} else {
					// both are internals
					return 0;
				}
			}
		}
	}
	
	/**
	 * Finds the nearest road to a given point.
	 * 
	 * @param node
	 *            nearestRoad command being processed
	 */
	public void processNearestRoad(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
			final int x = processIntegerAttribute(node, "x", parametersNode);
			final int y = processIntegerAttribute(node, "y", parametersNode);

			final Point2D.Float point = new Point2D.Float(x, y);


		if (pmQuadtree.getRoot().getType() == Node.EMPTY) {
			addErrorNode("roadNotFound", commandNode, parametersNode);
		} else if (pmQuadtree.getRoot().getType() == Node.LEAF && 
				pmQuadtree.getRoads().isEmpty()) {
			addErrorNode("roadNotFound", commandNode, parametersNode);}
		else {
			final Element road = results.createElement("road");
			ArrayList<City> n = nearestRoadHelper(pmQuadtree.getRoot(), point);
			if (n == null) addErrorNode("roadNotFound", commandNode, parametersNode);
			else {
				road.setAttribute("start", n.get(0).getName());
				road.setAttribute("end", n.get(1).getName());
				outputNode.appendChild(road);
				/* add success node to results */
				addSuccessNode(commandNode, parametersNode, outputNode);
			}
			
		}
	}
	/**
	 * @param root
	 * @param point
	 */
	private ArrayList<City> nearestRoadHelper(Node root, Point2D.Float point) {
		PriorityQueue<QuadrantDistanceRoad> q = new PriorityQueue<QuadrantDistanceRoad>();
		Node currNode = root;
		ArrayList<City> currRoad = null;
		if(root.getType() == Node.LEAF){
			q.add(new QuadrantDistanceRoad(root, point));
			currRoad = q.peek().minRoad;
		}
		else{
			while (currNode.getType() != Node.LEAF ) {
				InternalNode g = (InternalNode) currNode;
				for (int i = 0; i < 4; i++) {
					Node kid = g.children[i];
					if (kid.getType() == Node.INTERNAL || 
							(kid.getType() == Node.LEAF && !kid.getRoads().isEmpty())) {
						q.add(new QuadrantDistanceRoad(kid, point));
					}
				}
				if (!q.isEmpty()) {
					currRoad = q.peek().minRoad;
					currNode = q.remove().quadtreeNode;
				}
				else return null;
			}
		}
		return currRoad;
	}
	
	/**
	 * Finds the nearest city to a given road.
	 * 
	 * @param node
	 *            nearestCityToRoad command being processed
	 */
	public void processNearestCityToRoad(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
		final String start = processStringAttribute(node, "start", parametersNode);
		final String end = processStringAttribute(node, "end", parametersNode);

		ArrayList<String> roadcheck1 = new ArrayList<String>();
		ArrayList<String> roadcheck2 = new ArrayList<String>();
		
		roadcheck1.add(start);
		roadcheck1.add(end);
		roadcheck2.add(end);
		roadcheck2.add(start);
		
		if(!pmQuadtree.containsRoad(roadcheck1) && !pmQuadtree.containsRoad(roadcheck2)){
			addErrorNode("roadIsNotMapped", commandNode, parametersNode);
			return;
		}
		
		if (pmQuadtree.getRoot().getType() == Node.EMPTY) {
			addErrorNode("noOtherCitiesMapped", commandNode, parametersNode);
		} else if (pmQuadtree.getRoot().getType() == Node.LEAF && pmQuadtree.getCity() == null ) {
			addErrorNode("noOtherCitiesMapped", commandNode, parametersNode);
		} else {
			City startCity = citiesByName.get(start);
			City endCity = citiesByName.get(end);
			
			final Point2D.Float s = new Point2D.Float(startCity.getX(), startCity.getY());
			final Point2D.Float e = new Point2D.Float(endCity.getX(), endCity.getY());
			City n = nearestCityToRoadHelper(pmQuadtree.getRoot(), s, e);
			if (n == null) addErrorNode("noOtherCitiesMapped", commandNode, parametersNode);
			else {
				addCityNode(outputNode, n);
				/* add success node to results */
				addSuccessNode(commandNode, parametersNode, outputNode);
			}
		}
	}
	
	private City nearestCityToRoadHelper(Node root, Point2D.Float start, Point2D.Float end) {
		PriorityQueue<QuadrantDistanceRoad> q = new PriorityQueue<QuadrantDistanceRoad>();
		Node currNode = root;
		if (currNode.getType() == Node.LEAF){
			final Point2D.Float city = new Point2D.Float(root.getCity().getX(), root.getCity().getY());
			if(city.equals(start) || city.equals(end)) return null;
		}
		
		while (currNode.getType() != Node.LEAF) {
			InternalNode g = (InternalNode) currNode;
			for (int i = 0; i < 4; i++) {
				Node kid = g.children[i];
				if (kid.getType() == Node.INTERNAL ||
					   (kid.getType() == Node.LEAF && kid.getCity() != null
					   && !start.equals(new Point2D.Float(kid.getCity().getX(), kid.getCity().getY()))
						&& !end.equals(new Point2D.Float(kid.getCity().getX(), kid.getCity().getY())))) {
					q.add(new QuadrantDistanceRoad(kid, start, end));
				}
			}
			if(!q.isEmpty()) currNode = q.remove().quadtreeNode;
			else return null;
		}
		return ((LeafNode) currNode).getCity();
	}

	
	
	class QuadrantDistanceRoad implements Comparable<QuadrantDistanceRoad> {
		public Node quadtreeNode;
		private double distance = Integer.MAX_VALUE;
		public ArrayList<City> minRoad = null;
		public HashSet<ArrayList<City>> computedRoads = new HashSet<ArrayList<City>>();

		public QuadrantDistanceRoad(Node node, Point2D.Float pt) {
			quadtreeNode = node;
			if (node.getType() == Node.INTERNAL) {
				InternalNode gray = (InternalNode) node;
				distance = Shape2DDistanceCalculator.distance(pt, 
						new Rectangle2D.Float(gray.origin.x, gray.origin.y, gray.width, gray.height));
			} else if (node.getType() == Node.LEAF) {
				for (ArrayList<City> r : node.getRoads()){
					if( !computedRoads.contains(r)){
						Line2D.Float road = new Line2D.Float(r.get(0).getX(), r.get(0).getY(),
							r.get(1).getX(), r.get(1).getY());
						Double d = road.ptSegDist(pt);
						if(d < distance){
							distance = d; 
							minRoad = r;
						}
						
						computedRoads.add(r);
					}
				}
			} else {
				throw new IllegalArgumentException("Only leaf or internal node can be passed in");
			}
		}
		
		public QuadrantDistanceRoad(Node node, Point2D.Float start, Point2D.Float end) {
			quadtreeNode = node;
			Line2D.Float road = new Line2D.Float(start, end);
			if (node.getType() == Node.INTERNAL) {
				InternalNode gray = (InternalNode) node;
				distance = Shape2DDistanceCalculator.distance(road, 
						new Rectangle2D.Float(gray.origin.x, gray.origin.y, gray.width, gray.height));
			} else if (node.getType() == Node.LEAF) {
				LeafNode leaf = (LeafNode) node;
				distance = road.ptSegDist(leaf.getCity().pt);
			} else {
				throw new IllegalArgumentException("Only leaf or internal node can be passed in");
			}
		}

		public int compareTo(QuadrantDistanceRoad qd) {
			if (distance < qd.distance) {
				return -1;
			} else if (distance > qd.distance) {
				return 1;
			} else {
				if (quadtreeNode.getType() != qd.quadtreeNode.getType()) {
					if (quadtreeNode.getType() == Node.INTERNAL) {
						return -1;
					} else {
						return 1;
					}
				} else if (quadtreeNode.getType() == Node.LEAF) {
					// both are leaves
					if(minRoad == null){
						return ((LeafNode) qd.quadtreeNode).getCity().getName().compareTo(
								((LeafNode) quadtreeNode).getCity().getName());
					}
					else{
						
						int cmp = (qd.minRoad).get(0).getName().compareTo(
								minRoad.get(0).getName());
						if (cmp != 0) return cmp;
						else return (qd.minRoad).get(1).getName().compareTo(
								minRoad.get(1).getName());
					}
				} else {
					// both are internals
					return 0;
				}
			}
		}
	}

}