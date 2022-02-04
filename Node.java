import java.io.*;
import java.util.*;

/**
 * Class representing a node.
 */
public class Node {

  private Coord state;      // coord of node
  private Node parentNode;  // parent node of node
  private double pathCost;  // path cost to get to node
  private int depth;        // depth of node
  private double priority;  // priority

  /**
   * Constructor for DFS and BFS, who dont need the priority variable.
   * @param state       coord
   * @param parentNode  parent
   */
  public Node(Coord state, Node parentNode) {
    this.state = state;
    this.parentNode = parentNode;

    // If parent is null this node is the starting node, so set cost and depth
    // to 0.
    if (parentNode == null) {
      pathCost = 0;
      depth = 0;
    } else {
      pathCost = parentNode.getPathCost() + 1;
      depth = parentNode.getDepth() + 1;
    }
  }

  /**
   * Constructor for BestF and A*, who both need the priority variable.
   * @param state       coord
   * @param parentNode  parent
   * @param priority    priority/cost of node
   */
  public Node(Coord state, Node parentNode, double priority) {
    this.state = state;
    this.parentNode = parentNode;
    this.priority = priority;

    // If parent is null this node is the starting node, so set cost and depth
    // to 0.
    if (parentNode == null) {
      pathCost = 0;
      depth = 0;
    } else {
      pathCost = parentNode.getPathCost() + 1;
      depth = parentNode.getDepth() + 1;
    }
  }

  public Coord getState() {
		return state;
	}
	public Node getParentNode() {
		return parentNode;
	}
  public double getPathCost() {
		return pathCost;
	}
	public int getDepth() {
		return depth;
	}
  public double getPriority() {
		return priority;
	}

  public void printNode() {
    System.out.println("Node: " + state.toString());
    System.out.println("\tParent: " + parentNode.getState().toString());
    System.out.println("\tCost:" + pathCost);
    System.out.println("\tDepth:" + depth);

  }
}
