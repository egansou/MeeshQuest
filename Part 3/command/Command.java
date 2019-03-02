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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import cmsc420.exception.CityOutOfBoundsException;
import cmsc420.exception.MetropoleOutOfBoundsException;
import cmsc420.exception.RoadAlreadyMappedException;
import cmsc420.exception.RoadIntersectsAnotherRoadException;
import cmsc420.exception.RoadOutOfBoundsException;
import cmsc420.exception.ViolatesPMRulesException;
import cmsc420.geom.Circle2D;
import cmsc420.geom.Shape2DDistanceCalculator;
import cmsc420.sortedmap.Treap;
import cmsc420.structure.Airport;
import cmsc420.structure.City;
import cmsc420.structure.CityLocationComparator;
import cmsc420.structure.CityNameComparator;
import cmsc420.structure.Metropole;
import cmsc420.structure.Terminal;
//import cmsc420.structure.prquadtree.InternalNode;
//import cmsc420.structure.prquadtree.LeafNode;
//import cmsc420.structure.prquadtree.Node;
//import cmsc420.structure.prquadtree.PRQuadtree;
import cmsc420.structure.pmquadtree.InternalNode;
import cmsc420.structure.pmquadtree.LeafNode;
import cmsc420.structure.pmquadtree.Node;
import cmsc420.structure.pmquadtree.PMQuadtree;
import cmsc420.structure.pmquadtree.StartEndComparator;
import cmsc420.structure.prquadtree.PRQuadtree;
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
	
	protected final TreeMap<City, Integer> allMappedCitiesByName = new TreeMap<City, Integer>(new Comparator<City>() {
		@Override
		public int compare(City o1, City o2) {
				return o2.getName().compareTo(o1.getName());
			}
	});
	
	protected final TreeSet<City> airportsByLocation = new TreeSet<City>(
			new CityLocationComparator());
	
	protected final TreeMap<String, City> airportsByName = new TreeMap<String, City>(new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			return o2.compareTo(o1);
		}

	});
	
	protected final TreeSet<City> terminalsByLocation = new TreeSet<City>(
			new CityLocationComparator());
	
	protected final TreeMap<String, City> terminalsByName = new TreeMap<String, City>(new Comparator<String>() {
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
	
	protected final HashSet<String> roadEndpoints = new HashSet<String>();
	protected final Treap<String, City> citiesByNameTreap = new Treap<String, City>(new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			return o2.compareTo(o1);
		}
	});
	protected final TreeMap<String,TreeMap<String,Double>> graph = 
			new TreeMap<String,TreeMap<String,Double>>();
	
	protected final HashMap<Metropole, PMQuadtree> rep = 
			new HashMap<Metropole, PMQuadtree>();
	
	protected final PRQuadtree prQuadtree = new PRQuadtree();

	/**order, spatial width and height of the PM Quadtree */
	protected int localSpatialHeight, localSpatialWidth,
	remoteSpatialHeight, remoteSpatialWidth, order;

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
		remoteSpatialWidth = Integer.parseInt(node.getAttribute("remoteSpatialWidth"));
		remoteSpatialHeight = Integer.parseInt(node.getAttribute("remoteSpatialHeight"));
		localSpatialHeight = Integer.parseInt(node.getAttribute("localSpatialHeight"));
		localSpatialWidth = Integer.parseInt(node.getAttribute("localSpatialWidth"));
		
		order = Integer.parseInt(node.getAttribute("pmOrder"));

		/* initialize canvas */
		Canvas.instance.setFrameSize(remoteSpatialWidth, remoteSpatialHeight);
		/* add a rectangle to show where the bounds of the map are located */
		Canvas.instance.addRectangle(0, 0, (remoteSpatialWidth > remoteSpatialHeight) ? 
				remoteSpatialWidth : remoteSpatialHeight, 
				(remoteSpatialWidth > remoteSpatialHeight) ? remoteSpatialWidth :
					remoteSpatialHeight, Color.WHITE, true);
		Canvas.instance.addRectangle(0, 0, remoteSpatialWidth, remoteSpatialHeight, Color.BLACK,
				false);

		/* set PR Quadtree range */
		prQuadtree.setRange(remoteSpatialWidth, remoteSpatialHeight);
		
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
		final int localX = processIntegerAttribute(node, "localX", parametersNode);
		final int localY = processIntegerAttribute(node, "localY", parametersNode);
		final int remoteX = processIntegerAttribute(node, "remoteX", parametersNode);
		final int remoteY = processIntegerAttribute(node, "remoteY", parametersNode);

		final int radius = processIntegerAttribute(node, "radius",
				parametersNode);
		final String color = processStringAttribute(node, "color",
				parametersNode);

		/* create the city */
		final City city = new City(name, remoteX, remoteY, localX, 
				localY, radius, color);

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
	public void processDeleteCity(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final String name = processStringAttribute(node, "name", parametersNode);

		if (!citiesByName.containsKey(name)) {
			/* city with name does not exist */
			addErrorNode("cityDoesNotExist", commandNode, parametersNode);
		} else {
			/* delete city */
			final Element outputNode = results.createElement("output");
			final City deletedCity = citiesByName.get(name);

			Metropole m = new Metropole(deletedCity.getRemoteX(), deletedCity.getRemoteY());
			PMQuadtree pm = rep.get(m);
			TreeSet <ArrayList<City>> unmappedRoads = 
					new TreeSet <ArrayList<City>>(new StartEndComparator());
			if(pm != null){ 
				for( ArrayList<City> road : pm.getRoads()){
					if (road.get(0).equals(deletedCity)){
						unmappedRoads.add(road);
					}
					else if (road.get(1).equals(deletedCity)){
						unmappedRoads.add(road);
					}

				}

				Element roadUnmapped = null;
				for (ArrayList<City> road : unmappedRoads) {
					if (road.get(1).equals(deletedCity)){
						pm.removeRoad(road.get(0), road.get(1));
						
//						roadUnmapped = results.createElement("roadUnmapped");
//						roadUnmapped.setAttribute("start", road.get(0).getName());
//						roadUnmapped.setAttribute("end", road.get(1).getName());
//						outputNode.appendChild(roadUnmapped);

						boolean isolated = true;
						for(ArrayList<City> r : pm.getRoads()){
							if (r.get(0).equals(road.get(0)) || r.get(1).equals(road.get(0))){
								isolated = false;
								break;
							}
						}
						if(isolated == true) {
							pm.removeCity(road.get(0));
							allMappedCitiesByName.remove(road.get(0));
						}
					}
					else if (road.get(0).equals(deletedCity)){
						pm.removeRoad(road.get(0), road.get(1));
//						if(pm.removeCity(deletedCity)) {
//							allMappedCitiesByName.remove(deletedCity);
//							addCityNode(outputNode, "cityUnmapped", deletedCity);
//						}
//						roadUnmapped = results.createElement("roadUnmapped");
//						roadUnmapped.setAttribute("start", road.get(0).getName());
//						roadUnmapped.setAttribute("end", road.get(1).getName());
//						outputNode.appendChild(roadUnmapped);

						boolean isolated = true;
						for(ArrayList<City> r : pm.getRoads()){
							if (r.get(0).equals(road.get(1)) || r.get(1).equals(road.get(1))){
								isolated = false;
								break;
							}
						}
						if(isolated == true) {
							pm.removeCity(road.get(1));
							allMappedCitiesByName.remove(road.get(1));
						}
					}

				}
				
				if(pm.removeCity(deletedCity)) {
					addCityNode(outputNode, "cityUnmapped", deletedCity);
					allMappedCitiesByName.remove(deletedCity);
				}
				for (ArrayList<City> road : unmappedRoads) {
					roadUnmapped = results.createElement("roadUnmapped");
					roadUnmapped.setAttribute("start", road.get(0).getName());
					roadUnmapped.setAttribute("end", road.get(1).getName());
					outputNode.appendChild(roadUnmapped);
					
				}
			}
			citiesByName.remove(name);
			citiesByLocation.remove(deletedCity);
			citiesByNameTreap.remove(name);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

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
		terminalsByLocation.clear();
		terminalsByName.clear();
		airportsByLocation.clear();
		airportsByName.clear();
		citiesByNameTreap.clear();
		prQuadtree.clear();
		rep.clear();
		allMappedCitiesByName.clear();
		roadEndpoints.clear();
		graph.clear();

		/* clear canvas */
		Canvas.instance.clear();
		/* add a rectangle to show where the bounds of the map are located */
		Canvas.instance.addRectangle(0, 0, remoteSpatialWidth, remoteSpatialHeight, Color.BLACK,
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
		cityNode.setAttribute("localX", Integer.toString((int) city.getLocalX()));
		cityNode.setAttribute("localY", Integer.toString((int) city.getLocalY()));
		cityNode.setAttribute("remoteX", Integer.toString((int) city.getRemoteX()));
		cityNode.setAttribute("remoteY", Integer.toString((int) city.getRemoteY()));
		cityNode.setAttribute("radius", Integer.toString((int) city.getRadius()));
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
	
	private void addAirportNode(final Element node, final String cityNodeName,
			final Airport airport) {
		final Element cityNode = results.createElement(cityNodeName);
		cityNode.setAttribute("name", airport.getName());
		cityNode.setAttribute("localX", Integer.toString((int) airport.getLocalX()));
		cityNode.setAttribute("localY", Integer.toString((int) airport.getLocalY()));
		cityNode.setAttribute("remoteX", Integer.toString((int) airport.getRemoteX()));
		cityNode.setAttribute("remoteY", Integer.toString((int) airport.getRemoteY()));
		node.appendChild(cityNode);
	}
	
	private void addAirportNode(final Element node, final Airport airport) {
		addAirportNode(node, "airport", airport);
	}

	private void addTerminalNode(final Element node, final String cityNodeName,
			final Terminal terminal) {
		final Element cityNode = results.createElement(cityNodeName);
		cityNode.setAttribute("name", terminal.getName());
		cityNode.setAttribute("airportName", terminal.getAirport().getName());
		cityNode.setAttribute("cityName", terminal.getCity().getName());
		cityNode.setAttribute("localX", Integer.toString((int) terminal.getLocalX()));
		cityNode.setAttribute("localY", Integer.toString((int) terminal.getLocalY()));
		cityNode.setAttribute("remoteX", Integer.toString((int) terminal.getRemoteX()));
		cityNode.setAttribute("remoteY", Integer.toString((int) terminal.getRemoteY()));
		node.appendChild(cityNode);
	}
	
	private void addTerminalNode(final Element node, final Terminal terminal) {
		addTerminalNode(node, "terminal", terminal);
	}
	
	

	/**
	 * Maps an airport to the spatial map.
	 * 
	 * @param node
	 *            mapCity command node to be processed
	 */
	public void processMapAirport(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final String name = processStringAttribute(node, "name", parametersNode);
		final int localX = processIntegerAttribute(node, "localX", parametersNode);
		final int localY = processIntegerAttribute(node, "localY", parametersNode);
		final int remoteX = processIntegerAttribute(node, "remoteX", parametersNode);
		final int remoteY = processIntegerAttribute(node, "remoteY", parametersNode);
		final String terminalName = processStringAttribute(node, "terminalName", parametersNode);
		final int terminalX = processIntegerAttribute(node, "terminalX", parametersNode);
		final int terminalY = processIntegerAttribute(node, "terminalY", parametersNode);
		final String terminalCity = processStringAttribute(node, "terminalCity", parametersNode);
		
		City airport = new Airport(name, remoteX, remoteY, localX, localY, 0, null);
		City terminal = new Terminal(terminalName, remoteX, remoteY, terminalX, terminalY, 0, null);
		
		Metropole m = new Metropole(remoteX, remoteY);
		PMQuadtree pmQuadtree = rep.get(m);
		
		
		if (airportsByName.containsKey(name) || terminalsByName.containsKey(name) 
				|| citiesByName.containsKey(name)) {
			addErrorNode("duplicateAirportName", commandNode, parametersNode);
		} else if (airportsByLocation.contains(airport) ||
				citiesByLocation.contains(airport) ||
				terminalsByLocation.contains(airport)) {
			addErrorNode("duplicateAirportCoordinates", commandNode, parametersNode);
		} else if (airport.getLocalX() < 0 || airport.getLocalX() > localSpatialWidth 
				|| airport.getLocalY() < 0 || airport.getLocalY() > localSpatialHeight
				|| airport.getRemoteX() < 0 || airport.getRemoteX() >= remoteSpatialWidth 
				|| airport.getRemoteY() < 0 || airport.getRemoteY() >= remoteSpatialHeight) {
			addErrorNode("airportOutOfBounds", commandNode, parametersNode);
		} else if (airportsByName.containsKey(terminalName) || terminalsByName.containsKey(terminalName)
				|| citiesByName.containsKey(terminalName)) {
			addErrorNode("duplicateTerminalName", commandNode, parametersNode);
		} else if (airportsByLocation.contains(terminal) || citiesByLocation.contains(terminal) 
				|| terminalsByLocation.contains(terminal)) {
			addErrorNode("duplicateTerminalCoordinates", commandNode, parametersNode);
		} else if (terminal.getLocalX() < 0 || terminal.getLocalX() > localSpatialWidth 
				|| terminal.getLocalY() < 0 || terminal.getLocalY() > localSpatialHeight
				|| terminal.getRemoteX() < 0 || terminal.getRemoteX() >= remoteSpatialWidth 
				|| terminal.getRemoteY() < 0 || terminal.getRemoteY() >= remoteSpatialHeight) {
			addErrorNode("terminalOutOfBounds", commandNode, parametersNode);
		} else if (!citiesByName.containsKey(terminalCity)) {
			addErrorNode("connectingCityDoesNotExist", commandNode, parametersNode);
		} else if (pmQuadtree == null ||( pmQuadtree != null &&  
				allMappedCitiesByName.containsKey(citiesByName.get(terminalCity)) &&
				!pmQuadtree.containsCity(citiesByName.get(terminalCity)))) {
			addErrorNode("connectingCityNotInSameMetropole", commandNode, parametersNode);
		} else {
			
				try {
					
					City city = citiesByName.get(terminalCity);
					pmQuadtree.addAirport(airport);
					
					if(!allMappedCitiesByName.containsKey(city)){
						pmQuadtree.removeAirport(airport);
						addErrorNode("connectingCityNotMapped", commandNode, parametersNode);
						return;
					}
					
					try {
						
						pmQuadtree.addTerminal(airport, terminal, city);
						
						rep.put(m, pmQuadtree);
//						if(!graph.containsKey(name)){
//							TreeMap<String, Double> startInfo = new TreeMap<String, Double>();
//							startInfo.put(name, 0.0);
//							graph.put(name, startInfo);
//							}
//						else{
//							graph.get(name).put(name, 0.0);
//						
//						}
//						roadEndpoints.add(name);

						/* add city to canvas */
						HashSet<Terminal> terminals = ((Airport) airport).getTerminals();
						terminals.add((Terminal) terminal);
						((Airport) airport).setTerminals(terminals);
						((Terminal)terminal).setAirport(airport);
						((Terminal)terminal).setCity(city);
						Canvas.instance.addPoint(airport.getName(), airport.getLocalX(), 
							airport.getLocalY(), Color.RED);
						
						Canvas.instance.addPoint(terminal.getName(), terminal.getLocalX(), 
								terminal.getLocalY(), Color.darkGray);
						/* add road to canvas */
						Canvas.instance.addLine(terminal.getLocalX(), terminal.getLocalY(),
								city.getLocalX(), city.getLocalY(), Color.BLUE);
						
						terminalsByLocation.add(terminal);
						terminalsByName.put(terminalName, terminal);
						
						airportsByLocation.add(airport);
						airportsByName.put(name, airport);

						/* add success node to results */
						addSuccessNode(commandNode, parametersNode, outputNode);
					}
					catch (ViolatesPMRulesException e) {
						pmQuadtree.removeRoad(city, terminal);
						pmQuadtree.removeTerminal(terminal);
						pmQuadtree.removeAirport(airport);
						addErrorNode("terminalViolatesPMRules", commandNode, parametersNode);
					} catch (RoadIntersectsAnotherRoadException e) {
						pmQuadtree.removeRoad(city, terminal);
						pmQuadtree.removeTerminal(terminal);
						pmQuadtree.removeAirport(airport);
						addErrorNode("roadIntersectsAnotherRoad", commandNode, parametersNode);
					}
				} catch (ViolatesPMRulesException e) {
					addErrorNode("airportViolatesPMRules", commandNode, parametersNode);
				} 
			 
		}
	}

	/**
	 * Maps a road to the spatial map.
	 * 
	 * @param node
	 *            mapRoad command node to be processed
	 * @throws ViolatesPMRulesException 
	 * @throws MetropoleOutOfBoundsException 
	 */
	public void processMapRoad(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String start = processStringAttribute(node, "start", parametersNode);
		final String end = processStringAttribute(node, "end", parametersNode);
		
		City startCity = citiesByName.get(start);
		City endCity = citiesByName.get(end);
		PMQuadtree pmQuadtree = null;
		Metropole m = null;
		boolean check_start = false;
		boolean check_end = false;
		
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
		else if (startCity.getRemoteX() != endCity.getRemoteX() ||
				startCity.getRemoteY() != endCity.getRemoteY()){
				addErrorNode("roadNotInOneMetropole", commandNode, parametersNode);
		}else if (startCity.getRemoteX() < 0 || startCity.getRemoteX() >= remoteSpatialWidth 
				|| startCity.getRemoteY() < 0 || startCity.getRemoteY() >= remoteSpatialHeight) {
			/* city out of bounds */
			addErrorNode("roadOutOfBounds", commandNode, parametersNode);
		} else {
			try {
				
				m = new Metropole(startCity.getRemoteX(), startCity.getRemoteY());
				/* insert road into PM Quadtree */
				if(!prQuadtree.contains(m)) {
					prQuadtree.add(m);
					pmQuadtree = new PMQuadtree();
					pmQuadtree.setRange(localSpatialWidth, localSpatialHeight);
					pmQuadtree.setOrder(order);
					rep.put(m, pmQuadtree);	
					
					pmQuadtree.addRoad(startCity, endCity);
				}
				else {
					pmQuadtree = rep.get(m);
					
					check_start = pmQuadtree.containsCity(startCity);
					check_end = pmQuadtree.containsCity(endCity);
					
					pmQuadtree.addRoad(startCity, endCity);
				}
				
				double roadlength = Math.sqrt(Math.pow(startCity.getLocalX()-endCity.getLocalX(),2) + 
						Math.pow(startCity.getLocalY()-endCity.getLocalY(),2));

				if (startCity.getLocalX() >= 0 && startCity.getLocalX() <= localSpatialWidth 
						&& startCity.getLocalY() >= 0 && startCity.getLocalY() <= localSpatialHeight &&
						endCity.getLocalX() >= 0 && endCity.getLocalX() <= localSpatialWidth 
						&& endCity.getLocalY() >= 0 && endCity.getLocalY() <= localSpatialHeight ) {
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
				
				if (pmQuadtree.containsCity(startCity)) {
					 allMappedCitiesByName.put(startCity, startCity.getRadius());
					Canvas.instance.addPoint(startCity.getName(), startCity.getLocalX(), 
							startCity.getLocalY(), Color.BLACK);
				}
				if (pmQuadtree.containsCity(endCity)) {
					 allMappedCitiesByName.put(endCity, endCity.getRadius());
					Canvas.instance.addPoint(endCity.getName(), endCity.getLocalX(), 
							endCity.getLocalY(), Color.BLACK);
				}

				/* add road to canvas */
				Canvas.instance.addLine(startCity.getLocalX(), startCity.getLocalY(),
						endCity.getLocalX(), endCity.getLocalY(), Color.BLACK);

				/* add success node to results */
				addSuccessNode(commandNode, parametersNode, outputNode);
			}  catch (RoadOutOfBoundsException e) {
				addErrorNode("roadOutOfBounds", commandNode, parametersNode);
			} catch (RoadAlreadyMappedException e) {
				addErrorNode("roadAlreadyMapped", commandNode, parametersNode);
			} catch (RoadIntersectsAnotherRoadException e) {
				addErrorNode("roadIntersectsAnotherRoad", commandNode, parametersNode);
			} catch (ViolatesPMRulesException e) {
				pmQuadtree.removeRoad(startCity, endCity);
				if (!check_end) pmQuadtree.removeCity(endCity);
				if (!check_start) pmQuadtree.removeCity(startCity);
				addErrorNode("roadViolatesPMRules", commandNode, parametersNode);
			}	
		}
	}
	/**
	 * Maps a road to the spatial map.
	 * 
	 * @param node
	 *            mapRoad command node to be processed
	 * @throws ViolatesPMRulesException 
	 * @throws MetropoleOutOfBoundsException 
	 */
	public void processUnmapRoad(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");
		final String start = processStringAttribute(node, "start", parametersNode);
		final String end = processStringAttribute(node, "end", parametersNode);
		
		City startCity = citiesByName.get(start);
		City endCity = citiesByName.get(end);
		
		

		if (!citiesByName.containsKey(start)) {
			addErrorNode("startPointDoesNotExist", commandNode, parametersNode);
		}
		else if (!citiesByName.containsKey(end)) {
			addErrorNode("endPointDoesNotExist", commandNode, parametersNode);
		}
		else if (start.equals(end)) {
			addErrorNode("startEqualsEnd", commandNode, parametersNode);
		} else {
			
			Metropole m = new Metropole(startCity.getRemoteX(), startCity.getRemoteY());
			PMQuadtree pm = rep.get(m);
			
			ArrayList<City> road = new ArrayList<City> ();
			int cmp = start.compareTo(end);
			if(cmp < 0) {
				road.add(startCity);
				road.add(endCity);
			}
			else {
				road.add(endCity);
				road.add(startCity);
			}
					
			if (pm != null && pm.getRoads().contains(road)){
				pm.removeRoad(startCity, endCity);
				boolean isolatedStart = true;
				boolean isolatedEnd = true;
				for(ArrayList<City> r : pm.getRoads()){
					if (r.get(0).equals(startCity) || r.get(1).equals(startCity)){
						isolatedStart = false;
						break;
					}
				}
				
				for(ArrayList<City> r : pm.getRoads()){
					if (r.get(0).equals(endCity) || r.get(1).equals(endCity)){
						isolatedEnd = false;
						break;
					}
				}
				if(isolatedStart) {
					pm.removeCity(startCity);
					allMappedCitiesByName.remove(startCity);
				}
				if(isolatedEnd) {
					pm.removeCity(endCity);
					allMappedCitiesByName.remove(endCity);
				}
		
				Element roadDeleted = results.createElement("roadDeleted");
				roadDeleted.setAttribute("start", startCity.getName());
				roadDeleted.setAttribute("end", endCity.getName());
				outputNode.appendChild(roadDeleted);
				addSuccessNode(commandNode, parametersNode, outputNode);
			}
			else {
				addErrorNode("roadNotMapped", commandNode, parametersNode);
			}
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
		final int remoteX = processIntegerAttribute(node, "remoteX", parametersNode);
		final int remoteY = processIntegerAttribute(node, "remoteY", parametersNode);
		
		Metropole m = new Metropole(remoteX, remoteY);
		PMQuadtree pmQuadtree = rep.get(m);
		

		if (pmQuadtree == null || pmQuadtree.isEmpty()) {
			/* empty PM Quadtree */
			addErrorNode("metropoleIsEmpty", commandNode, parametersNode);
		} else if (remoteX < 0 || remoteX >= remoteSpatialWidth ||
				remoteY < 0 || remoteY >= remoteSpatialHeight) {
				/* city out of bounds */
			addErrorNode("metropoleOutOfBounds", commandNode, parametersNode);
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
	private void printPMQuadtreeHelper(final Node currentNode, final Element xmlNode) {
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
					if(city.getId() == 3) addTerminalNode(black, (Terminal)currentLeaf.getCity());
					else if(city.getId() == 2) addAirportNode(black, (Airport)currentLeaf.getCity());
					else addCityNode(black, currentLeaf.getCity());
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
		final Element outputNode = results.createElement("output");

		final int remoteX = processIntegerAttribute(node, "remoteX", parametersNode);
		final int remoteY = processIntegerAttribute(node, "remoteY", parametersNode);
		final String name = processStringAttribute(node, "name", parametersNode);
		
		Metropole m = new Metropole(remoteX, remoteY);
		
		PMQuadtree pmQuadtree = rep.get(m);
		
		if(pmQuadtree != null && pmQuadtree.isEmpty()){
			addErrorNode("metropoleIsEmpty", commandNode, parametersNode);
		} else if (remoteX < 0 || remoteX >= remoteSpatialWidth ||
				remoteY < 0 || remoteY >= remoteSpatialHeight) {
				/* city out of bounds */
			addErrorNode("metropoleOutOfBounds", commandNode, parametersNode);
		} else {
		
			/* save canvas to '<name>.png' */
			Canvas.instance.save(name);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	
	/**
	 * Prints out the structure of the Treap in a human-readable format.
	 * 
	 * @param node
	 *            Treap command to be processed
	 */
	public void processPrintTreap(Element node) {
        final Element commandNode = getCommandNode(node);
        final Element parametersNode = results.createElement("parameters");
        final Element outputNode = results.createElement("output");

        if (citiesByNameTreap.isEmpty()) {
            addErrorNode("emptyTree", commandNode, parametersNode);
        } else {
			citiesByNameTreap.createXml(outputNode);
            addSuccessNode(commandNode, parametersNode, outputNode);
        }
    }
	
	
	/**
	 * Determine the shortest path from a starting to an ending vertex.
	 * 
	 * @param node
	 *            shortestPath command node to be processed
	 */
//	public void processShortestPath(final Element node) throws IOException {
//		final Element commandNode = getCommandNode(node);
//		final Element parametersNode = results.createElement("parameters");
//
//		final String start = processStringAttribute(node, "start", parametersNode);
//		final String end = processStringAttribute(node, "end", parametersNode);
//		CanvasPlus canvas = null;
//		
//		String pathFile = "";
//		
//		if (node.getAttribute("saveMap").compareTo("") != 0) {
//			pathFile = processStringAttribute(node, "saveMap", parametersNode);
//			canvas = new CanvasPlus();
//			canvas.setFrameSize(spatialWidth, spatialHeight);
//			/* add a rectangle to show where the bounds of the map are located */
//			canvas.addRectangle(0, 0, (spatialWidth > spatialHeight) ? spatialWidth : spatialHeight, 
//					(spatialWidth > spatialHeight) ? spatialWidth : spatialHeight, Color.WHITE, true);
//			canvas.addRectangle(0, 0, spatialWidth, spatialHeight, Color.BLACK,
//					false);
//		}
//		
//		String html = "";
//		if (node.getAttribute("saveHTML").compareTo("") != 0) {
//			html = processStringAttribute(node, "saveHTML", parametersNode);
//		}
//		
//		final Element outputNode = results.createElement("output");
//
//		if (!pmQuadtree.contains(start)) {
//			addErrorNode("nonExistentStart", commandNode, parametersNode);
//		} else if (!pmQuadtree.contains(end)) {
//			addErrorNode("nonExistentEnd", commandNode, parametersNode);
//		} else if (!roadEndpoints.contains(start) || !roadEndpoints.contains(end)) {
//			addErrorNode("noPathExists", commandNode, parametersNode);
//		} else {
//			TreeMap<String, CloseCity> sp = shortestPath(graph, start, end);
//			
//			final Element pathNode = results.createElement("path");
//			
//			
//			CloseCity obj = sp.get(end); 
//			
//			if(obj.getDistance() == Double.POSITIVE_INFINITY){
//				addErrorNode("noPathExists", commandNode, parametersNode);
//				return;
//			}
//			
//			String e = end;
//			 
//			LinkedList<String> linkedlist1 = new LinkedList<String>();
//			LinkedList<CloseCity> linkedlist2 = new LinkedList<CloseCity>();
//			while(!e.equals(obj.getComeFrom())) {
//				linkedlist1.addFirst(e);
//				linkedlist2.addFirst(obj);
//				e = obj.getComeFrom();
//				obj = sp.get(e); 	
//			}
//			
//			int size = linkedlist1.size();
//			Arc2D.Double arc = new Arc2D.Double();
//			
//			pathNode.setAttribute("hops", String.valueOf(size));
//			if(size > 0){
//				 int yourScale = 3;
//				pathNode.setAttribute("length", String.valueOf(
//						BigDecimal.valueOf(linkedlist2.get(size-1).getDistance()).
//				setScale(yourScale, BigDecimal.ROUND_HALF_UP)));
//			}
//			else{
//				pathNode.setAttribute("length", "0.000");
////				City startingCity = citiesByName.get(start);
////				if (startingCity != null )canvas.addPoint(startingCity.getName(), 
////						startingCity.getX(), startingCity.getY(), Color.GREEN);
//			}
//			
//			for (int i = 0; i < size; i++){
//				int j = i + 1;
//				String endingName = linkedlist1.get(i);
//				String startingName = linkedlist2.get(i).getComeFrom();
//				City startingCity = citiesByName.get(startingName);
//				City endingCity = citiesByName.get(endingName); 
//				if (pathFile.compareTo("") != 0) {
//					if(i == 0) canvas.addPoint(startingCity.getName(), 
//							startingCity.getX(), startingCity.getY(), Color.GREEN);
//					canvas.addPoint(endingCity.getName(), 
//								endingCity.getX(), endingCity.getY(), Color.BLUE);
//					canvas.addLine(startingCity.getX(), startingCity.getY()
//							, endingCity.getX(), endingCity.getY(), Color.BLUE);
//						
//						if(i == size-1) canvas.addPoint(endingCity.getName(), 
//								endingCity.getX(), endingCity.getY(), Color.RED);
//				}
//				final Element road = results.createElement("road");
//				road.setAttribute("start", startingName);
//				road.setAttribute("end", endingName);
//				Element direction = null;
//				if(j < size){
//					String nextName = linkedlist1.get(j);
//					Point2D.Float startingPoint = startingCity.pt; 
//					Point2D.Float endingPoint = endingCity.pt; 
//					Point2D.Float nextPoint = citiesByName.get(nextName).pt; 
//					arc.setArcByTangent(startingPoint, endingPoint, nextPoint, 1);
//					Double angle;
//					//System.out.println(arc.getAngleExtent());
//					if (arc.getAngleExtent() == -180) angle = 180.0;
//					else angle = arc.getAngleExtent();
//					if(arc.getAngleExtent() < -45.0 && arc.getAngleExtent() > -180.0){
//						direction  = results.createElement("left");
//					}
//					else if(angle >= 45.0 && angle <= 180 ){
//						direction  = results.createElement("right");
//					}
//					else direction  = results.createElement("straight");
//				}
//				pathNode.appendChild(road);
//				if(direction != null) pathNode.appendChild(direction);
//			}
//			outputNode.appendChild(pathNode);
//			
//			/* add success node to results */
//			Element successNode = addSuccessNode(commandNode, parametersNode, outputNode);
//			
//			if (pathFile.compareTo("") != 0) {
//				/* save canvas to file with range circle */
//				canvas.save(pathFile);
//				//canvas.dispose();
//			}
//			if (html.compareTo("") != 0) {
//				org.w3c.dom.Document shortestPathDoc;
//				try {
//					shortestPathDoc = XmlUtility.getDocumentBuilder().newDocument();
//					org.w3c.dom.Node spNode = shortestPathDoc.importNode(successNode, true);
//					XmlUtility.transform(shortestPathDoc, new File("shortestPath.xsl"), new File(html + ".html"));
//					shortestPathDoc.appendChild(spNode);
//				} catch (ParserConfigurationException e1) {
//					e1.printStackTrace();
//				} catch (TransformerException e1) {
//					e1.printStackTrace();
//				}
//							
//			}
//			
//		}
//	}
	
	
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
	 * Finds the mapped cities within the range of a given point.
	 * 
	 * @param node
	 *            rangeCities command to be processed
	 * @throws IOException
	 */
	public void processGlobalRangeCities(final Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final TreeSet<City> citiesInRange = new TreeSet<City>(
				new CityNameComparator());

		/* extract values from command */
		final int remoteX = processIntegerAttribute(node, "remoteX", parametersNode);
		final int remoteY = processIntegerAttribute(node, "remoteY", parametersNode);
		final int radius = processIntegerAttribute(node, "radius",
				parametersNode);

		String pathFile = "";
		if (node.getAttribute("saveMap").compareTo("") != 0) {
			pathFile = processStringAttribute(node, "saveMap", parametersNode);
		}
		/* get cities within range */
		final Point2D.Double point = new Point2D.Double(remoteX, remoteY);
		
		for (City city : allMappedCitiesByName.keySet()){
			final double distance = point.distance(city.remotetoPoint2D());
			if (distance <= radius) {
				citiesInRange.add(city);
			}
		}
		


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
					Canvas.instance.addCircle(remoteX, remoteY, radius, Color.BLUE, false);
				}
				Canvas.instance.save(pathFile);
				if(radius != 0) {
					Canvas.instance.removeCircle(remoteX, remoteY, radius, Color.BLUE, false);
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
//	private void rangeCitiesHelper(final Point2D.Double point,
//			final int radius, final Node node, final TreeSet<City> citiesInRange) {
//		if (node.getType() == Node.LEAF && node.getCity() != null) {
//			final LeafNode leaf = (LeafNode) node;
//			final double distance = point.distance(leaf.getCity().toPoint2D());
//			if (distance <= radius) {
//				/* city is in range */
//				final City city = leaf.getCity();
//				citiesInRange.add(city);
//			}
//		} else if (node.getType() == Node.INTERNAL) {
//			/* check each quadrant of internal node */
//			final InternalNode internal = (InternalNode) node;
//
//			final Circle2D.Double circle = new Circle2D.Double(point, radius);
//			for (int i = 0; i < 4; i++) {
//				if (pmQuadtree.intersects(circle, internal.getChildRegion(i))) {
//					rangeCitiesHelper(point, radius, internal.getChild(i),
//							citiesInRange);
//				}
//			}
//		}
//	}
	
	/**
	 * Finds the mapped roads within the range of a given point.
	 * 
	 * @param node
	 *            rangeRoads command to be processed
	 * @throws IOException
	 */
//	public void processRangeRoads(final Element node) throws IOException {
//		final Element commandNode = getCommandNode(node);
//		final Element parametersNode = results.createElement("parameters");
//		final Element outputNode = results.createElement("output");
//
//		final TreeSet<ArrayList<City>> roadsInRange = new TreeSet<ArrayList<City>>(
//				new StartEndComparator());
//
//		/* extract values from command */
//		final int x = processIntegerAttribute(node, "x", parametersNode);
//		final int y = processIntegerAttribute(node, "y", parametersNode);
//		final int radius = processIntegerAttribute(node, "radius",
//				parametersNode);
//
//		String pathFile = "";
//		if (node.getAttribute("saveMap").compareTo("") != 0) {
//			pathFile = processStringAttribute(node, "saveMap", parametersNode);
//		}
//		/* get cities within range */
//		final Point2D.Double point = new Point2D.Double(x, y);
//		rangeRoadsHelper(point, radius, pmQuadtree.getRoot(), roadsInRange);
//
//		/* print out cities within range */
//		if (roadsInRange.isEmpty()) {
//			addErrorNode("noRoadsExistInRange", commandNode, parametersNode);
//		} else {
//			/* get road list */
//			final Element roadListNode = results.createElement("roadList");
//			for (ArrayList<City> r : roadsInRange) {
//				final Element road = results.createElement("road");
//				road.setAttribute("start", r.get(0).getName());
//				road.setAttribute("end", r.get(1).getName());
//				roadListNode.appendChild(road);
//			}
//			outputNode.appendChild(roadListNode);
//
//			/* add success node to results */
//			addSuccessNode(commandNode, parametersNode, outputNode);
//
//			if (pathFile.compareTo("") != 0) {
//				/* save canvas to file with range circle */
//				if(radius != 0) {
//					Canvas.instance.addCircle(x, y, radius, Color.BLUE, false);
//				}
//				Canvas.instance.save(pathFile);
//				if(radius != 0) {
//					Canvas.instance.removeCircle(x, y, radius, Color.BLUE, false);
//				}
//			}
//		}
//	}

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
//	private void rangeRoadsHelper(final Point2D.Double point,
//			final int radius, final Node node, final TreeSet<ArrayList<City>> roadsInRange) {
//		if (node.getType() == Node.LEAF && !node.getRoads().isEmpty()) {
//			for (ArrayList<City> road : node.getRoads()){
//				if(!roadsInRange.contains(road)){
//					Line2D.Float r = new Line2D.Float(road.get(0).getX(),road.get(0).getY(),
//							road.get(1).getX(),road.get(1).getY());
//					final double distance = r.ptSegDist(point);
//					if (distance <= radius) {
//						/* road is in range */
//						roadsInRange.add(road);
//					}
//				}
//			}
//		} else if (node.getType() == Node.INTERNAL) {
//			/* check each quadrant of internal node */
//			final InternalNode internal = (InternalNode) node;
//
//			final Circle2D.Double circle = new Circle2D.Double(point, radius);
//			for (int i = 0; i < 4; i++) {
//				if (pmQuadtree.intersects(circle, internal.getChildRegion(i))) {
//					rangeRoadsHelper(point, radius, internal.getChild(i),
//							roadsInRange);
//				}
//			}
//		}
//	}
	
	

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
		final int localX = processIntegerAttribute(node, "localX", parametersNode);
		final int localY = processIntegerAttribute(node, "localY", parametersNode);
		final int remoteX = processIntegerAttribute(node, "remoteX", parametersNode);
		final int remoteY = processIntegerAttribute(node, "remoteY", parametersNode);
		
		Metropole m = new Metropole(remoteX, remoteY);
		PMQuadtree pm = rep.get(m);

		final Point2D.Float point = new Point2D.Float(localX, localY);

		if (citiesByName.size() <= 0) {
			addErrorNode("cityNotFound", commandNode, parametersNode);
			return;
		}

		if (pm == null || pm.isEmpty()) {
			addErrorNode("cityNotFound", commandNode, parametersNode);
		} else if (pm.getRoot().getType() == Node.LEAF && pm.getRoot().getCity() == null ) {
			addErrorNode("cityNotFound", commandNode, parametersNode);
		} else if (pm.getRoot().getType() == Node.LEAF && pm.getRoot().getCity() != null &&
				!pm.containsCity(pm.getRoot().getCity())) {
			addErrorNode("cityNotFound", commandNode, parametersNode);}
		else {
			City n = nearestCityHelper(pm.getRoot(), point, pm);
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
	private City nearestCityHelper(Node root, Point2D.Float point, PMQuadtree pm) {
		PriorityQueue<QuadrantDistance> q = new PriorityQueue<QuadrantDistance>();
		Node currNode = root;
		while (currNode.getType() != Node.LEAF ) {
			InternalNode g = (InternalNode) currNode;
			for (int i = 0; i < 4; i++) {
				Node kid = g.children[i];
				if (kid.getType() == Node.INTERNAL || 
					   (kid.getType() == Node.LEAF && kid.getCity() != null 
					   && pm.containsCity(kid.getCity()))) {
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
				distance = pt.distance(leaf.getCity().localtoPoint2D());
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
	
	
//	class QuadrantDistanceRoad implements Comparable<QuadrantDistanceRoad> {
//		public Node quadtreeNode;
//		private double distance = Integer.MAX_VALUE;
//		public ArrayList<City> minRoad = null;
//		public HashSet<ArrayList<City>> computedRoads = new HashSet<ArrayList<City>>();
//
//		public QuadrantDistanceRoad(Node node, Point2D.Float pt) {
//			quadtreeNode = node;
//			if (node.getType() == Node.INTERNAL) {
//				InternalNode gray = (InternalNode) node;
//				distance = Shape2DDistanceCalculator.distance(pt, 
//						new Rectangle2D.Float(gray.origin.x, gray.origin.y, gray.width, gray.height));
//			} else if (node.getType() == Node.LEAF) {
//				for (ArrayList<City> r : node.getRoads()){
//					if( !computedRoads.contains(r)){
//						Line2D.Float road = new Line2D.Float(r.get(0).getX(), r.get(0).getY(),
//							r.get(1).getX(), r.get(1).getY());
//						Double d = road.ptSegDist(pt);
//						if(d < distance){
//							distance = d; 
//							minRoad = r;
//						}
//						
//						computedRoads.add(r);
//					}
//				}
//			} else {
//				throw new IllegalArgumentException("Only leaf or internal node can be passed in");
//			}
//		}
//		
//		public QuadrantDistanceRoad(Node node, Point2D.Float start, Point2D.Float end) {
//			quadtreeNode = node;
//			Line2D.Float road = new Line2D.Float(start, end);
//			if (node.getType() == Node.INTERNAL) {
//				InternalNode gray = (InternalNode) node;
//				distance = Shape2DDistanceCalculator.distance(road, 
//						new Rectangle2D.Float(gray.origin.x, gray.origin.y, gray.width, gray.height));
//			} else if (node.getType() == Node.LEAF) {
//				LeafNode leaf = (LeafNode) node;
//				distance = road.ptSegDist(leaf.getCity().pt);
//			} else {
//				throw new IllegalArgumentException("Only leaf or internal node can be passed in");
//			}
//		}
//
//		public int compareTo(QuadrantDistanceRoad qd) {
//			if (distance < qd.distance) {
//				return -1;
//			} else if (distance > qd.distance) {
//				return 1;
//			} else {
//				if (quadtreeNode.getType() != qd.quadtreeNode.getType()) {
//					if (quadtreeNode.getType() == Node.INTERNAL) {
//						return -1;
//					} else {
//						return 1;
//					}
//				} else if (quadtreeNode.getType() == Node.LEAF) {
//					// both are leaves
//					if(minRoad == null){
//						return ((LeafNode) qd.quadtreeNode).getCity().getName().compareTo(
//								((LeafNode) quadtreeNode).getCity().getName());
//					}
//					else{
//						
//						int cmp = (qd.minRoad).get(0).getName().compareTo(
//								minRoad.get(0).getName());
//						if (cmp != 0) return cmp;
//						else return (qd.minRoad).get(1).getName().compareTo(
//								minRoad.get(1).getName());
//					}
//				} else {
//					// both are internals
//					return 0;
//				}
//			}
//		}
//	}

}