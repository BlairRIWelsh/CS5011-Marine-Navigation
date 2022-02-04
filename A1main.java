/********************Starter Code
 *
 * This class contains some examples on how to handle the required inputs and outputs
 * and other debugging options
 *
 * @author at258
 *
 * run with
 * java A1main <Algo> <ConfID>
 *
 */

import java.io.*;
import java.util.*;
import java.lang.Math;

public class A1main {

  static final boolean DEBUG = true; // Change to true to read debug information

  // manhattan distance works on a triangle grid insted of a square
  static final boolean MAN_DIST_TRI = false;

	/**
	 * Main method for A1Main class.
	 * Loads config and runs algorithim based on given arguments.
	 * Usage: java A1main <DFS|BFS|AStar|BestF|...> <ConfID> [<any other param>]
	 * @param args  - command line arguments
	 */
	public static void main(String[] args) {

		// Retrieve configuration
		Conf conf = Conf.valueOf(args[1]);

		// Debug
		if (DEBUG == true) {
			System.out.println("Configuration:"+args[1]);
			System.out.println("Map:");
			printMap(conf.getMap(), conf.getS(), conf.getG());
			System.out.println("Departure port: Start (r_s,c_s): "+conf.getS());
			System.out.println("Destination port: Goal (r_g,c_g): "+conf.getG());
			System.out.println("Search algorithm: "+args[0]);
			System.out.println();
		}

		// Run algorithim
		runSearch(args[0],conf.getMap(),conf.getS(),conf.getG());

	}

	private static void runSearch(String algo, Map map, Coord start, Coord goal) {

		switch(algo) {
			case "BFS": //run BFS
				runBasic(algo, map, start, goal);
				break;
			case "DFS": //run DFS
        runBasic(algo, map, start, goal);
				break;
			case "BestF": //run BestF
				runBasic(algo, map, start, goal);
        break;
			// case "AStar": //run AStar
			// 	throw new java.lang.UnsupportedOperationException("Not supported yet.");
			// 	break;
		}

	}

	private static void runBasic(String algo, Map map, Coord start, Coord goal) {

    int visitedNodes = 0;

		// Create frontier and add starting node
		LinkedList<Node> frontier = new LinkedList<Node>();
    insert(start, null, algo, frontier, goal);

		// Create explored set for visited coords
		Set<String> explored = new HashSet<String> ();

		while (frontier.size() != 0) { // While frontier not empty

			printFrontier(frontier, algo); // Print frontier

			Node tempNode = remove(algo, frontier); // Remove next node from frontier
      visitedNodes++;

      // System.out.println("Tempnode: " + tempNode.getState().toString());

			explored.add(tempNode.getState().toString()); // Add coord to explored
      // System.out.println("explored = " + explored);

			if (goalTest(tempNode, goal)) { // Check tempNode is goal

        finished(tempNode, visitedNodes);

			} else {

 				// Loop through available neighbour coords
				for (Coord coord : successorFunction(map, tempNode.getState())) {

          // If not already explored and not in frontier...
					if (!explored.contains(coord.toString()) && !inFrontier(coord, frontier)) {
						insert(coord, tempNode, algo, frontier, goal);
					}
        }
			}
		}

		// frontier empty - return failure
		failure(visitedNodes);

	}

	/**
	 * Returns the first node from the frontier.
	 * @param index     [description]
	 * @param frontier  [description]
	 */
	private static Node remove(String algo, LinkedList<Node> frontier) {

		Node temp = null;

    // System.out.println(frontier.size());

		switch(algo) {
			case "BFS": //run BFS
				temp = frontier.get(0);
        // System.out.println(frontier);
        // System.out.println(frontier.size());
				frontier.remove(0);
        break;
			case "DFS": //run DFS
				temp = frontier.get(frontier.size()-1);
				frontier.remove(frontier.size()-1);
				break;
      case "BestF":

        int indexOfSmallestPath = 0;
        for (Node node : frontier) {

          // System.out.println("\tis " + frontier.get(indexOfSmallestPath).getPriority() + "<" + node.getPriority());
          if (frontier.get(indexOfSmallestPath).getPriority() >= node.getPriority()) {
            indexOfSmallestPath = frontier.indexOf(node);
          }
        }
        temp = frontier.get(indexOfSmallestPath);
        // System.out.println("chosen " + temp.getState().toString() + ":" + temp.getPriority());
				frontier.remove(indexOfSmallestPath);

        break;

      case "AStar":
        break;
		}
		return temp;

	}

  /**
	 * Inserts a new node into the frontier.
	 * @param node      [description]
	 * @param frontier  [description]
	 */
	private static void insert(Coord coord, Node parentNode, String algo, LinkedList<Node> frontier, Coord goal) {

    switch(algo) {
			case "BFS":
				frontier.add(new Node(coord, parentNode)); // Add to frontier.
        break;
			case "DFS":
				frontier.add(new Node(coord, parentNode)); // Add to frontier.
				break;
      case "BestF":
        frontier.add(new Node(coord, parentNode, calculateManhattanDist(coord, goal)));
        break;
      case "AStar":
        break;
		}

	}

  private static int calculateManhattanDist(Coord start, Coord goal) {

    if (MAN_DIST_TRI == true) {
      int startCoords[] = squareToTriangleCoords(start);
      int goalCoords[] = squareToTriangleCoords(goal);

      int changeInA = Math.abs(startCoords[0] + goalCoords[0]);
      int changeInB = Math.abs(startCoords[1] + goalCoords[1]);
      int changeInC = Math.abs(startCoords[2] + goalCoords[2]);

      return changeInA + changeInB + changeInC;

    } else {

      return Math.abs(start.getR() - goal.getR()) + Math.abs(start.getC() - goal.getC());

    }

  }

  private static int[] squareToTriangleCoords(Coord start) {

    int dir;
		if((start.getR() % 2 == 0 && start.getC() % 2 == 0) || (start.getR() % 2 != 0 && start.getC() % 2 != 0)) {
			dir = 0;
		} else {
			dir = 1;
		}

    int coords[] = new int[3];
    coords[0] = -start.getR();
    coords[1] = (start.getR() + start.getC() - dir) / 2;
    coords[2] = (start.getR() + start.getC() - dir) / 2 - start.getR() + dir;

    return coords;

  }

	private static void printFrontier(LinkedList<Node> frontier, String algo) {

		System.out.print("[");
		for (int i = 0; i < frontier.size() - 1; i++) {

      switch(algo) {
  			case "BFS": //run BFS
  				System.out.print(frontier.get(i).getState().toString() + ",");
  				break;
  			case "DFS": //run DFS
          System.out.print(frontier.get(i).getState().toString() + ",");
  				break;
			case "BestF": //run BestF
  				System.out.print(frontier.get(i).getState().toString() + ":" + frontier.get(i).getPriority() + ",");
				break;
			// case "AStar": //run AStar
			// 	throw new java.lang.UnsupportedOperationException("Not done print yet.");
			// 	break;
  		}

		}

    switch(algo) {
      case "BFS": //run BFS
        System.out.print(frontier.get(frontier.size() - 1).getState().toString() + "]\n");
        break;
      case "DFS": //run DFS
        System.out.print(frontier.get(frontier.size() - 1).getState().toString() + "]\n");
        break;
    case "BestF": //run BestF
        System.out.print(frontier.get(frontier.size() - 1).getState().toString() + ":" + frontier.get(frontier.size() - 1).getPriority() + "]\n");
      break;
    // case "AStar": //run AStar
    // 	throw new java.lang.UnsupportedOperationException("Not done print yet.");
    // 	break;
    }

	}

	/**
	 * Returns true if state is a goal state
	 * @param state  [description]
	 * @param goal   [description]
	 */
	private static boolean goalTest(Node node, Coord goal) {
		if (node.getState().equals(goal)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Implements the successor function, i.e. expands a set of new nodes given
	 * all actions applicable in the state.
	 * @param node     [description]
	 * @param problem  [description]
	 */
	private static LinkedList<Coord> successorFunction(Map map, Coord start) {

		LinkedList<Coord> newCoords = new LinkedList<Coord>();

    // System.out.println("Searching coord: " + start);

		boolean triangleFacingUp;
		if((start.getR() % 2 == 0 && start.getC() % 2 == 0) || (start.getR() % 2 != 0 && start.getC() % 2 != 0)) {
			triangleFacingUp = true;
		} else {
			triangleFacingUp = false;
		}

    // System.out.println("\tUp: " + triangleFacingUp);

		// get right triangle
		if (start.getC() != map.getMap()[0].length-1) {
			if (map.getMap()[start.getR()][start.getC() + 1] != 1) {
				newCoords.add(new Coord(start.getR(), start.getC() + 1));
        // System.out.println("\tneighbour r : " + start.getR() + "," + (start.getC() + 1));
			}
		}

		// get down triangle
		if (triangleFacingUp && start.getR() != map.getMap().length-1) {
			if (map.getMap()[start.getR() + 1][start.getC()] != 1) {
				newCoords.add(new Coord(start.getR() + 1, start.getC()));
        // System.out.println("\tneighbour d : " + (start.getR() +1) + "," + start.getC());
			}
		}

		// get left triangle
		if (start.getC() != 0) {
			if (map.getMap()[start.getR()][start.getC() - 1] != 1) {
				newCoords.add(new Coord(start.getR(), start.getC() - 1));
        // System.out.println("\tneighbour l: " + start.getR() + "," + (start.getC() - 1));
			}
		}

		// get up triangle
		if (!triangleFacingUp && start.getR() != 0) {
			if (map.getMap()[start.getR() - 1][start.getC()] != 1) {
				newCoords.add(new Coord(start.getR() - 1, start.getC()));
        // System.out.println("\tneighbour u: " + (start.getR() -1) + "," + start.getC());
			}
		}

		return newCoords;

	}

  private static boolean inFrontier(Coord coord, LinkedList<Node> frontier) {
    for (Node node : frontier) {
      if (node.getState().equals(coord)) {
        return true;
      }
    }
    return false;
  }

  private static void finished(Node tempNode, int visitedNodes) {

    double cost = tempNode.getPathCost();

    StringBuilder str = new StringBuilder();

    while (tempNode.getParentNode() != null) {
      str.insert(0, tempNode.getState().toString());
      // tempNode.printNode();
      Node temp = tempNode.getParentNode();
      tempNode = temp;
    }

    str.insert(0, tempNode.getState().toString());

    System.out.println(str);
    System.out.println(cost);
    System.out.println(visitedNodes);

    System.exit(0);

  }

  private static void failure(int visitedNodes) {
    System.out.println("fail");
    System.out.println(visitedNodes);
  }


	// /**
	//  * Returns the cost for executing action in state.
	//  * @param state1  [description]
	//  * @param state2  [description]
	//  */
	// private static void cost(State state1,State state2) {
  //
	// }
  //



	private static void printMap(Map m, Coord init, Coord goal) {

		int[][] map=m.getMap();

		System.out.println();
		int rows=map.length;
		int columns=map[0].length;

		//top row
		System.out.print("  ");
		for(int c=0;c<columns;c++) {
			System.out.print(" "+c);
		}
		System.out.println();
		System.out.print("  ");
		for(int c=0;c<columns;c++) {
			System.out.print(" -");
		}
		System.out.println();

		//print rows
		for(int r=0;r<rows;r++) {
			boolean right;
			System.out.print(r+"|");
			if(r%2==0) { //even row, starts right [=starts left & flip right]
				right=false;
			}else { //odd row, starts left [=starts right & flip left]
				right=true;
			}
			for(int c=0;c<columns;c++) {
				System.out.print(flip(right));
				if(isCoord(init,r,c)) {
					System.out.print("S");
				}else {
					if(isCoord(goal,r,c)) {
						System.out.print("G");
					}else {
						if(map[r][c]==0){
							System.out.print(".");
						}else{
							System.out.print(map[r][c]);
						}
					}
				}
				right=!right;
			}
			System.out.println(flip(right));
		}
		System.out.println();


	}

	private static boolean isCoord(Coord coord, int r, int c) {
		//check if coordinates are the same as current (r,c)
		if(coord.getR()==r && coord.getC()==c) {
			return true;
		}
		return false;
	}

	public static String flip(boolean right) {
        //prints triangle edges
		if(right) {
			return "\\"; //right return left
		}else {
			return "/"; //left return right
		}

	}

}
