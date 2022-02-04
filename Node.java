import java.io.*;
import java.util.*;

public class Node {
  private Coord state;
  private Node parentNode;
  //private String action;
  private double pathCost;
  private int depth;
  private double priority;

  public Node(Coord state, Node parentNode) {
    this.state = state;
    this.parentNode = parentNode;

    if (parentNode == null) {
      pathCost = 0;
      depth = 0;
    } else {
      pathCost = parentNode.getPathCost() + 1;
      depth = parentNode.getDepth() + 1;
    }
  }

  public Node(Coord state, Node parentNode, double priority) {
    this.state = state;
    this.parentNode = parentNode;
    this.priority = priority;

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
