
import java.io.*;
import java.util.*;
import directedgraph.*;
import graph.*;

/**
 * @author Sayf Elhawary
 */

public class SkiO {

	private static AdjacencyListDirectedGraph skioGraph =
	    new AdjacencyListDirectedGraph();
	private static String startPoint; // starting point name on the track
	private static String endPoint; // ending point name on the track
	private static AdjacencyListDirectedVertex startNode;// start vertex
	private static AdjacencyListDirectedVertex endNode;// end vertex

	// contains the name of control points and a unique index starting from 0
	private static HashMap<String,Integer> controlPointsLocation =
	    new HashMap<String,Integer>();
	// contains the name of control points and their vertices
	private static HashMap<String,AdjacencyListDirectedVertex> controlPoints =
	    new HashMap<String,AdjacencyListDirectedVertex>();
	// contains all of the vertices in the graph
	private static HashMap<Integer,WeightedNode> nodes =
	    new HashMap<Integer,WeightedNode>();

	public static void main ( String[] args ) throws IOException {
		BufferedReader readFile =
		    new BufferedReader(new FileReader(args[1]));

		startPoint = readFile.readLine().split(" +")[1];
		endPoint = readFile.readLine().split(" +")[1];
		String courseControls[] = readFile.readLine().split(" +");

		// load the control points into the hashmap
		for ( int i = 2 ; i < courseControls.length ; i++ ) {
			controlPointsLocation.put(courseControls[i],null);
		}

		readFile.close();

		// create the graph using data from the provided file
		generateGraph(args[0]);
		int totalDistance = 0;// total distance traveled

		// distance from the start vertex to the first control point
		totalDistance += djikstra(startNode,controlPoints.get(courseControls[2]));
		// iterate through all of the control points until the last control point
		for ( int i = 3 ; i < courseControls.length ; i++ ) {
			totalDistance += djikstra(controlPoints.get(courseControls[i - 1]),
			                          controlPoints.get(courseControls[i]));
		}
		// distance from the last control point to the end vertex
		totalDistance +=
		    djikstra(controlPoints.get(courseControls[courseControls.length - 1]),
		             endNode);
		System.out.println("Total Distance: " + totalDistance);
	}

	/**
	 * Finds the shortest distance between the given start and end vertices
	 * 
	 * @param start
	 *          vertex
	 * @param end
	 *          vertex
	 * @return the shortest distance between the start and end vertices
	 */
	public static int djikstra ( Vertex start, Vertex end ) {
		// contains the distance to each vertex
		int[] dist = new int[skioGraph.numVertices()];
		// contains the origin of a vertex
		int[] prev = new int[skioGraph.numVertices()];
		// for each vertex in the graph
		for ( Vertex vertex : skioGraph.vertices() ) {
			int value = Integer.MAX_VALUE;
			// set it's distance to the maximum possible value
			dist[getIndex(vertex)] = value;
			// set it's previous to be -1
			prev[getIndex(vertex)] = -1;
			// create the weighted vertices for each vertex
			nodes.put(getIndex(vertex),new WeightedNode(vertex,value));
		}

		// sets the start node's to the distance 0
		dist[getIndex(start)] = 0;

		// create the priority queue using the weighted vertices
		PriorityQueue<WeightedNode> queue =
		    new PriorityQueue<WeightedNode>(new WeightedNodeComparator());
		queue.add(nodes.get(getIndex(start)));// add the first vertex
		// set the previous of the first vertex to be -1
		prev[getIndex(start)] = -1;

		while ( !queue.isEmpty() ) {// while the priority queue is not empty
			// remove the smallest weighted (distance) vertex
			WeightedNode curVertex = queue.poll();
			// if the end vertex is reached then exit the loop
			if ( curVertex.getNode() == end ) {
				break;
			}
			// for each incident vertex for the current vertex
			for ( Edge incidentEdge : skioGraph
			    .inIncidentEdges(curVertex.getNode()) ) {
				Vertex adjacentVertex =
				    skioGraph.opposite(curVertex.getNode(),incidentEdge);
				// if we have found a shorter path to the vertex
				if ( dist[getIndex(adjacentVertex)] > dist[getIndex(curVertex
				    .getNode())] + (int) incidentEdge.getObject() ) {
					// set the new distance to the vertex
					dist[getIndex(adjacentVertex)] = dist[getIndex(curVertex.getNode())]
					    + (int) incidentEdge.getObject();
					// set the new previous to the vertex
					prev[getIndex(adjacentVertex)] = getIndex(curVertex.getNode());
					// remove the weighted node from the priority queue and re-add it
					// using a new smaller weight
					// (the two operations are equivalent to decrease key)
					queue.remove(nodes.get(getIndex(adjacentVertex)));
					queue.add(new WeightedNode(adjacentVertex,
					                           dist[getIndex(curVertex.getNode())]
					                               + (int) incidentEdge.getObject()));
				}
			}
		}

		System.out.println("From " + start.getObject() + " to " + end.getObject());
		System.out.println("Distance: " + dist[getIndex(end)]);// the distance from
		// start to the end
		// vertex
		Stack<Object> path = new Stack<Object>();
		// access the origin vertex of each vertex starting from the end vertex
		for ( int i = getIndex(end) ; prev[i] != -1 ; i = prev[i] ) {
			path.push(nodes.get(i).getNode().getObject());
		}

		// print out the path in reverse order
		System.out.println("    " + start.getObject());
		for ( ; !path.isEmpty() ; ) {
			System.out.println("    " + path.pop());
		}
		System.out.println();

		return dist[getIndex(end)];
	}

	/**
	 * Creates a directed, weighted graph
	 * 
	 * @param fileName
	 *          containing graph data
	 * @throws IOException
	 */
	public static void generateGraph ( String fileName ) throws IOException {
		// contains all points within the container
		HashMap<String,AdjacencyListDirectedVertex> container =
		    new HashMap<String,AdjacencyListDirectedVertex>();
		BufferedReader readFile = new BufferedReader(new FileReader(fileName));
		int controlPointIndex = 0;// control point unique index

		for ( String lineRead = readFile.readLine() ; lineRead != null ; lineRead =
		    readFile.readLine() ) {
			String lineComponents[] = lineRead.split(" ");

			AdjacencyListDirectedVertex fVertex = null;
			// if the vertex already exists within the graph
			if ( container.containsKey(lineComponents[1]) ) {
				fVertex = container.get(lineComponents[1]);
			} else {
				// add to graph
				fVertex = (AdjacencyListDirectedVertex) skioGraph
				    .insertVertex(lineComponents[1]);
				container.put(lineComponents[1],fVertex);// add to hashmap
				// if it equals the start point set the start vertex
				if ( lineComponents[1].equals(startPoint) ) {
					startNode = fVertex;
				} else if ( lineComponents[1].equals(endPoint) ) {// if it equals the
					// end point set the
					// end vertex
					endNode = fVertex;
				}
			}

			AdjacencyListDirectedVertex sVertex = null;
			// if the vertex already exists within the graph
			if ( container.containsKey(lineComponents[2]) ) {
				sVertex = container.get(lineComponents[2]);
			} else {
				// add to graph
				sVertex = (AdjacencyListDirectedVertex) skioGraph
				    .insertVertex(lineComponents[2]);
				container.put(lineComponents[2],sVertex);// add to hashmap
				// if it equals the start point set the start vertex
				if ( lineComponents[2].equals(startPoint) ) {
					startNode = sVertex;
				} // if it equals the end point set the end vertex
				else if ( lineComponents[2].equals(endPoint) ) {
					endNode = sVertex;
				}
			}

			// if is not a control point insert a directed edge from the first vertex
			// to the second vertex with the length and revlength data
			if ( !controlPointsLocation.containsKey(lineComponents[0]) ) {
				skioGraph
				    .insertDirectedEdge(fVertex,sVertex,
				                        (int) Double.parseDouble(lineComponents[4]));
				skioGraph
				    .insertDirectedEdge(sVertex,fVertex,
				                        (int) Double.parseDouble(lineComponents[5]));
			} else {
				// create a new vertex
				AdjacencyListDirectedVertex midVertex =
				    (AdjacencyListDirectedVertex) skioGraph
				        .insertVertex(lineComponents[0]);
				// create 4 directed edges between the control point vertex and the
				// first/second vertices. The weight for each edge is half it was,
				// rounded up (avoids 0 weight)
				skioGraph.insertDirectedEdge(fVertex,midVertex,(int) Math
				    .ceil(Double.parseDouble(lineComponents[4]) / 2));
				skioGraph.insertDirectedEdge(midVertex,fVertex,(int) Math
				    .ceil(Double.parseDouble(lineComponents[5]) / 2));
				skioGraph.insertDirectedEdge(midVertex,sVertex,(int) Math
				    .ceil(Double.parseDouble(lineComponents[4]) / 2));
				skioGraph.insertDirectedEdge(sVertex,midVertex,(int) Math
				    .ceil(Double.parseDouble(lineComponents[5]) / 2));
				// adds control point and unique index
				controlPointsLocation.put(lineComponents[0],controlPointIndex);
				// adds control point and corresponding vertex
				controlPoints.put(lineComponents[0],midVertex);
				controlPointIndex++;// increment unique control point index
			}
		}

		readFile.close();
	}

	/**
	 * Contains a vertex and the weight for the vertex (the length to get to the
	 * vertex from a starting vertex)
	 */
	public static class WeightedNode {
		private Vertex vertex_;
		private int weight_;

		public WeightedNode ( Vertex node, int weight ) {
			vertex_ = node;
			weight_ = weight;
		}

		public void setWeight ( int weight ) {
			weight_ = weight;
		}

		public int getWeight () {
			return weight_;
		}

		public Vertex getNode () {
			return vertex_;
		}
	}

	/**
	 * Comparator for the WeightedNode class allowing PriorityQueue to sort
	 * vertices by distance from a vertex
	 */
	public static class WeightedNodeComparator
	    implements Comparator<WeightedNode> {
		@Override
		public int compare ( WeightedNode fNode, WeightedNode sNode ) {
			// if node2 is shorter to get to then put it before node1
			if ( fNode.getWeight() > sNode.getWeight() ) {
				return 1;
			}
			// if node1 is shorter to get to then put it before node2
			else if ( fNode.getWeight() < sNode.getWeight() ) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	/**
	 * Calculates a unique index value for vertices
	 * 
	 * @param vertex
	 * @return the unique index value for a vertex, including control points
	 */
	private static int getIndex ( Vertex vertex ) {
		String obj = vertex.getObject().toString();
		// if the vertex is a control point the last x values are reserved for
		// control points
		if ( obj.substring(0,1).equals("T") ) {
			int midPointStart =
			    skioGraph.numVertices() - controlPointsLocation.size();
			// use the unique value for each control point to find a unique index
			return midPointStart + controlPointsLocation.get(vertex.getObject());
		}
		return Integer.parseInt(obj.substring(1));// return a unique index value
	}

}
