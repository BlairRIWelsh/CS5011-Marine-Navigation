/********************Starter Code
 *
 * This class contains some examples on how to handle the required inputs and outputs
 * and other debugging options
 *
 * @author at258
 *
 * run with java A1main <Algo> <ConfID>
 *
 */

import java.io.*;
import java.util.*;
import java.lang.Math;

public class A1main {

  static final boolean DEBUG = false; // Change to true to read debug information

  // set manhattan distance to work on a triangle grid insted of a square
  static final boolean MAN_DIST_TRI = false;

  // number of visited nodes for bidirectional search
  private static int biVistedNodes = 0;

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

    // Run algorithm. All use the same general 'search' function apart from
    // bi-directional search which needs its own special function.
    switch (args[0]) {
      case "BFS":
			case "DFS":
      case "BestF":
      case "AStar":
        runSearch(args[0],conf.getMap(),conf.getS(),conf.getG());
        break;
      case "BiS":
        biDirectionalSearch(conf.getMap(),conf.getS(),conf.getG());
        break;
      default:
        throw new java.lang.UnsupportedOperationException("Algorithim '" + args[0] + "' not supported.");
    }

	}

  /** REGULAR SEARCH FUNCTIONS **/

  /**
   * Main search tree algorithim. Changes for different algorthims happen during
   * smaller functions.
   * @param algo   algorithm selected as a string
   * @param map    map to navigate
   * @param start  start coords
   * @param goal   end coords
   */
	private static void runSearch(String algo, Map map, Coord start, Coord goal) {

    int visitedNodes = 0; // Number of visited nodes during the search

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

			if (goalTest(tempNode, goal)) { // Check if tempNode is the goal coords

        finished(tempNode, visitedNodes);

			} else {

 				// Loop through available neighbour coords
				for (Coord coord : successorFunction(map, tempNode.getState())) {

          // if A* it doesnt matter if the coords are already in frontier
          if (algo == "AStar") {
            if (!explored.contains(coord.toString())) {       // If the coord is not explored
              insert(coord, tempNode, algo, frontier, goal);  // Insert new node in frontier
            }

          // Else if coord not already explored and not in frontier...
          } else if (!explored.contains(coord.toString()) && !inFrontier(coord, frontier)) {
						insert(coord, tempNode, algo, frontier, goal); // Insert new node in frontier
					}
        }
			}
		}

		// Frontier is empty at this stage so return failure
		failure(visitedNodes);

	}

	/**
	 * Returns the first node from the frontier.
	 * BFS - returns the first node in frontier (Queue)
	 * DFS - returns the last node in frontier (Stack)
	 * BestF & A* - returns the lowest cost node (Priority queue)
	 * @param algo     algorithm to use
	 * @param frontier  frontier of nodes
	 */
	private static Node remove(String algo, LinkedList<Node> frontier) {

		Node temp = null;

		switch(algo) {

      // Frontier is treated as a queue - remove and return first element
			case "BFS":
				temp = frontier.get(0);
				frontier.remove(0);
        break;

      // Frontier is treated as a stack - remove and return last element
			case "DFS": //run DFS
				temp = frontier.get(frontier.size()-1);
				frontier.remove(frontier.size()-1);
				break;

      // Frontier is treated as a priority queue - find node with the smallest
      // cost and remove and return it.
      case "BestF":
      case "AStar":

        int indexOfSmallestPath = 0; // Index of the node with smallest cost

        // Loop through frontier, if we find a node with smaller cost than
        // frontier(indexOfSmallestPath), set indexOfSmallestPath to that node
        for (Node node : frontier) {
          if (frontier.get(indexOfSmallestPath).getPriority() >= node.getPriority()) {
            indexOfSmallestPath = frontier.indexOf(node);
          }
        }

        // Remove node from frontier and return it.
        temp = frontier.get(indexOfSmallestPath);
        frontier.remove(indexOfSmallestPath);

        break;
		}
		return temp;

	}

  /**
   * Inserts a new node into the frontier.
   * @param coord       current coord
   * @param parentNode  parent node of new node
   * @param algo        algorithim in use
   * @param frontier    frontier of nodes
   * @param goal        goal coord
   */
	private static void insert(Coord coord, Node parentNode, String algo, LinkedList<Node> frontier, Coord goal) {

    switch(algo) {

      // Create new node with no priority and add to frontier
			case "BFS":
			case "DFS":
				frontier.add(new Node(coord, parentNode));
				break;

      // Create new node with priority equal to Manhattan distance and add to frontier
      case "BestF":
        frontier.add(new Node(coord, parentNode, calculateManhattanDist(coord, goal)));
        break;

      // Create new node with priority equal to Manhattan distance + path cost of
      // new node and add to frontier
      case "AStar":

        // A* is different as we have not done a check in the search to see if
        // the coord is already in the frontier. This is because if it does exist
        // we compare the two path costs and only keep the node with the smallest
        // path cost in the frontier.

        // Calculate the path cost of the new node. If this node is the starting
        // node, parentNode will be null so we set path cost to 0.
        double pathCostOfNewNode;
        if (parentNode != null) {
          pathCostOfNewNode = parentNode.getPathCost() + 1;
        } else {
          pathCostOfNewNode = 0;
        }

        // Create new node
        Node temp = new Node(coord, parentNode, calculateManhattanDist(coord, goal) + pathCostOfNewNode);

        if (inFrontier(coord, frontier)) { // If the coord is already in the frontier

          // Find the node with the same coord
          for (Node node : frontier) {
            if (node.getState().equals(temp.getState())) {

              // If the new node has a smaller path cost replace old node in the
              // frontier.
              if (temp.getPathCost() < node.getPathCost()) {
                frontier.remove(node);
                frontier.add(temp);
              }
            }
          }

        // If the coord is not in the frontier just add it normally
        } else {
          frontier.add(temp);
        }

        break;

		}
	}

	/**
	 * Return a list of all possible coordinates available to move to from given
	 * coord. Coords outside the boundry or value = 1 (island) are not returned.
	 * @param map      map to navigate
	 * @param start    starting coord
	 */
	private static LinkedList<Coord> successorFunction(Map map, Coord start) {

		LinkedList<Coord> newCoords = new LinkedList<Coord>();

    // System.out.println("Searching coord: " + start);

    // Find the direction of the current trianlge
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

  /**
   * Is the given coord in the frontier.
   * @param  coord                  given coord
   * @param  frontier               frontier
   * @return          [description]
   */
  private static boolean inFrontier(Coord coord, LinkedList<Node> frontier) {
    for (Node node : frontier) {
      if (node.getState().equals(coord)) {
        return true;
      }
    }
    return false;
  }

	/**
	 * Returns true if state is a goal state
	 * @param node  node currently being tested
	 * @param goal   goal coords
	 */
	private static boolean goalTest(Node node, Coord goal) {
		if (node.getState().equals(goal)) {
			return true;
		} else {
			return false;
		}
	}

  /**
   * Called when a solution has been found.
   * @param tempNode      final node
   * @param visitedNodes  number of visited nodes during search
   */
  private static void finished(Node tempNode, int visitedNodes) {

    double cost = tempNode.getPathCost(); // Final cost of route

    StringBuilder str = new StringBuilder();

    // Find the taken route by iterating through the parent of tempNode and its
    // parents until we get to the starting node (parentNode == null).
    while (tempNode.getParentNode() != null) {
      str.insert(0, tempNode.getState().toString());
      Node temp = tempNode.getParentNode();
      tempNode = temp;
    }
    str.insert(0, tempNode.getState().toString());

    // Print route, cost and number of visited nodes
    System.out.println(str);
    System.out.println(cost);
    System.out.println(visitedNodes);

    // Exit
    System.exit(0);

  }

  /**
   * Called when no solution has been found.
   * @param visitedNodes  Number of visited nodes
   */
  private static void failure(int visitedNodes) {
    System.out.println("fail");
    System.out.println(visitedNodes);
  }

  /**
   * Calculate the manhattan distance betweek two points.
   * @param  start               starting coords
   * @param  goal                finishing coords
   * @return       manhattan distance
   */
  private static int calculateManhattanDist(Coord start, Coord goal) {

    // Calculate manDistance as a grid of triangles
    if (MAN_DIST_TRI == true) {

      // Get 3 coord representation of both coords
      int startCoords[] = squareToTriangleCoords(start);
      int goalCoords[] = squareToTriangleCoords(goal);

      int changeInA = Math.abs(startCoords[0] + goalCoords[0]);
      int changeInB = Math.abs(startCoords[1] + goalCoords[1]);
      int changeInC = Math.abs(startCoords[2] + goalCoords[2]);

      return changeInA + changeInB + changeInC;

    // Calculate manDistance as a grid of squares
    } else {

      return Math.abs(start.getR() - goal.getR()) + Math.abs(start.getC() - goal.getC());

    }

  }

  /**
   * Convert a x,y coord representation to a,b,c
   * @param  start               coord to convert
   * @return       a 3-size array of coords a,b and c
   */
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


  /** BIDIRECTIONAL SEARCH FUNCTIONS **/

  /**
   * Main search method for bi-directional search. Instead of using one frontier
   * and explored set for the start coord we have one each for the starting
   * coord and the ending coord. Once we find where they meet we can finish.
   * @param map    map to navigate
   * @param start  starting coordinate
   * @param goal   goal coordinate
   */
  public static void biDirectionalSearch(Map map, Coord start, Coord goal) {

		// Create frontiers
		LinkedList<Node> frontierA = new LinkedList<Node>();
    LinkedList<Node> frontierB = new LinkedList<Node>();

    // Add starting nodes to frontiers
    insert(start, null, "BFS", frontierA, goal);
    insert(goal, null, "BFS", frontierB, start);

    // Create explored sets for visited coords
		Set<Node> exploredA = new HashSet<Node> ();
		Set<Node> exploredB = new HashSet<Node> ();

    // While both frontiers are empty
    while (!frontierA.isEmpty() || !frontierB.isEmpty()) {

        // Print frontiers
        printFrontiers(frontierA, frontierB);
        // printExploreds(exploredA, exploredB);

        removeNodeAndCheckFinish(map, frontierA, exploredA, exploredB, start);
        removeNodeAndCheckFinish(map, frontierB, exploredB, exploredA, start);

    }

    // If we reach here there is no route
    failure(biVistedNodes);

  }

  /**
   * Used in biDirectionalSearch. For the search from one direction, removes a
   * node from the given frontier, adds it to its explored list and finds its neigbour
   * coordinates. It then checks to see if any of those coords are in the other
   * search direction's explored nodes. If it is, we have coonected the two
   * directions and can finish. Else, as long as those coords are not already
   * explored by this direction or are not in its frontier already, we add them
   * to its frontier.
   * @param map                  map to explore
   * @param frontier             frontier to remove node from
   * @param visitedFromThisSide  list of explored nodes from this search side
   * @param visitedFromThatSide  list of explored nodes from the other search side
   * @param start                starting coordinates
   */
  private static void removeNodeAndCheckFinish(Map map, LinkedList<Node> frontier, Set<Node> visitedFromThisSide, Set<Node> visitedFromThatSide, Coord start) {

        if (!frontier.isEmpty()) {

          // Remove node from frontier, add it to explored and increment nodes visited.
          Node tempNode = remove("BFS", frontier);
          visitedFromThisSide.add(tempNode);
          biVistedNodes++;

          // System.out.println("Now on coord: " + tempNode.getState().toString());

          // Loop through available neighbour coords
          for (Coord coord : successorFunction(map, tempNode.getState())) {

              // If the neighbour coordinate is in the other search directions
              // explored list, we have connected the two directions and have
              // found a route, so can finish.
              if (coordInExplored(coord, visitedFromThatSide)) {
                  finishedBi(new Node(coord, tempNode), visitedFromThatSide, start);

              // Else, as long as the coord is not already explored or not already
              // in the frontier add it to the frontier.
              } else if (!coordInExplored(coord, visitedFromThisSide) && !inFrontier(coord, frontier)) {
                  insert(coord, tempNode, "BFS", frontier, null);
              }
          }
        }
    }

  /**
   * Function to check a coordinate is not in an explored set.
   * @param  coord                  coordinate
   * @param  explored               explored set
   * @return          true or false if the coordinate is found.
   */
  private static boolean coordInExplored(Coord coord, Set<Node> explored) {
    for (Node node : explored) {
      if (coord.toString().equals(node.getState().toString())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Finishing function for bi-directional search.
   * Given the middle node, iterates through its parents from each side to
   * get the given route and output it to the user.
   * @param middleNode     [description]
   * @param otherExplored  [description]
   * @param start          [description]
   */
  private static void finishedBi(Node middleNode, Set<Node> otherExplored, Coord start) {

    // Coordinates of the middle (connecting) node
    String middleNodeCoords = middleNode.getState().toString();

    // The path costs of each half of the route
    double firstHalfCost = middleNode.getPathCost();
    double secondHalfCost = 0;

    // List containg the route coordinates
    LinkedList<String> route = new LinkedList<String>();

    // Iterate through all the parent nodes, adding their coords to the route list
    while (middleNode.getParentNode() != null) {
      route.addFirst(middleNode.getState().toString());
      Node temp = middleNode.getParentNode();
      middleNode = temp;
    }
    route.addFirst(middleNode.getState().toString());

    // Used in case route is less than 2 cost long, as then the second half of
    // the search will be empty and cause null pointer exceptions.
    if (firstHalfCost > 2) {

      // Find the middle node in the other directions explored list
      Node nodeFromOtherSide = null;
      for (Node node : otherExplored) {
        if (node.getState().toString().equals(middleNodeCoords)){
          nodeFromOtherSide = node.getParentNode();
          break;
        }
      }

      // Find the other half of the cost of the route
      secondHalfCost = nodeFromOtherSide.getPathCost();

      // Iterate through all the parent nodes, adding their coords to the route list
      while (nodeFromOtherSide.getParentNode() != null) {
        route.addLast(nodeFromOtherSide.getState().toString());
        Node temp = nodeFromOtherSide.getParentNode();
        nodeFromOtherSide = temp;
      }
      route.addLast(nodeFromOtherSide.getState().toString());
    }

    // Test to print the route in the correct order. If the starting coordinate
    // of route is not the starting coord given, print the LinkedList backwards,
    // else print it forwards.
    if (route.get(0).equals(start.toString())) {

      // print route in forward order
      for(int i = 0; i <= route.size()-1; i++)
            System.out.print( route.get(i) );

    } else {

      // Print route in backwards order
      for(int i = route.size()-1; i >= 0 ; i--)
            System.out.print( route.get(i) );
    }

    // Print output information (route, cost, visited nodes) to the user
    System.out.println();
    System.out.println(firstHalfCost + secondHalfCost + 1); // + 1 to make up for middle node
    System.out.println(biVistedNodes-1); // - 1 to make up for middle node

    System.exit(0);
  }


  /** PRINT FUNCTIONS **/

  /**
   * Print the frontier.
   * @param frontier  Frontier
   * @param algo      Algorithm in use
   */
	private static void printFrontier(LinkedList<Node> frontier, String algo) {

		System.out.print("[");
		for (int i = 0; i < frontier.size() - 1; i++) {

      switch(algo) {

        // For BFS & DFS, no need to print costs/priorities with coords
  			case "BFS":
  			case "DFS":
          System.out.print(frontier.get(i).getState().toString() + ",");
  				break;

        // For BestF & A* print costs/priorities alongside coords
  			case "BestF":
        case "AStar":
  				System.out.print(frontier.get(i).getState().toString() + ":" + frontier.get(i).getPriority() + ",");
				break;
  		}

		}

    // Previous loop only printed up to the second last element, so repeat but this
    // time dont print a comma and instead close the square brackets.
    switch(algo) {
      case "BFS":
      case "DFS":
        System.out.print(frontier.get(frontier.size() - 1).getState().toString() + "]\n");
        break;
    case "BestF":
    case "AStar":
        System.out.print(frontier.get(frontier.size() - 1).getState().toString() + ":" + frontier.get(frontier.size() - 1).getPriority() + "]\n");
      break;
    }

	}

  /**
   * Print the frontier.
   * @param frontier  Frontier
   * @param algo      Algorithm in use
   */
	private static void printFrontiers(LinkedList<Node> frontierA, LinkedList<Node> frontierB) {

		System.out.print("A: [");

    for (int i = 0; i < frontierA.size() - 1; i++) {
      System.out.print(frontierA.get(i).getState().toString() + ",");
		}
    if (frontierA.size() != 0) {
      System.out.print(frontierA.get(frontierA.size() - 1).getState().toString() + "]\n");

    } else {
      System.out.print("]\n");
    }

    System.out.print("B: [");

    for (int i = 0; i < frontierB.size() - 1; i++) {
      System.out.print(frontierB.get(i).getState().toString() + ",");
		}
    if (frontierB.size() != 0) {
      System.out.print(frontierB.get(frontierB.size() - 1).getState().toString() + "]\n");
    } else {
      System.out.print("]\n");
    }

	}

  /**
   * Prints the two frontiers used in bidirectional search.
   * @param frontierA    First frontier
   * @param frontierB    Second frontier
   */
  private static void printExploreds(Set<Node>frontierA, Set<Node> frontierB) {

		System.out.print("Ae: [");
    Iterator itr = frontierA.iterator();
    while (itr.hasNext()) {
      Node temp = (Node) itr.next();
      System.out.print(temp.getState().toString() + ", ");
    }
    System.out.print("]\n");

    System.out.print("Be: [");
    itr = frontierB.iterator();
    while (itr.hasNext()) {
          Node temp = (Node) itr.next();
          System.out.print(temp.getState().toString() + ", ");
    }
    System.out.print("]\n\n");

	}


  /** GIVEN FUNCTIONS **/

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
