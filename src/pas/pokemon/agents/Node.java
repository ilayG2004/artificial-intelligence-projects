package src.pas.pokemon.agents;


// SYSTEM IMPORTS....feel free to add your own imports here! You may need/want to import more from the .jar!
import edu.bu.pas.pokemon.core.Agent;
import edu.bu.pas.pokemon.core.Battle;
import edu.bu.pas.pokemon.core.Battle.BattleView;
import edu.bu.pas.pokemon.core.Team;
import edu.bu.pas.pokemon.core.Team.TeamView;
import edu.bu.pas.pokemon.core.Move;
import edu.bu.pas.pokemon.core.Move.MoveView;
import edu.bu.pas.pokemon.utils.Pair;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Node {
  private Node parent;
  private List<Node> children;
  private int utilityValue;
  private boolean isChanceNode;
  private edu.bu.pas.pokemon.core.Move.MoveView move;

  /* CONSTRUCTORS */
  //DEFAULT ROOT OF TURN: Intializes a root node with no parent and no children. 
  public Node() {
    this.parent = null;
    this.utilityValue = -20;  
    this.children = null;
    this.isChanceNode = false;
    this.move = null;
  }
  //Initializes some node with an existing parent, a precalculated utility value, and whether or not it is a chance node
    // No children generated yet
  public Node(Node parent, int utilityValue, boolean chance, edu.bu.pas.pokemon.core.Move.MoveView move) {
    this.parent = parent;
    this.utilityValue = utilityValue;
    this.isChanceNode = chance;
    this.children = null;
    this.move = move;
  }

  /* GETTERS & SETTERS */
  //Add child nodes to current node
  public void addNode(Node child) {
    this.children.add(child);
  }
  //Set parent node of current node
  public void setParent(Node parent) {
    this.parent = parent;
  }

  //Return utility value of current node
  public int getUtil() {
    return this.utilityValue;
  }
  //Set utility value of current node
  public void setUtil(int util) {
    this.utilityValue = util;
  }

  //Return move
  public edu.bu.pas.pokemon.core.Move.MoveView getMove() {
    return this.move;
  }

  //FOR NICK SIMA TO IMPLEMENT
  public List<Node> generateChildren(Node root) {
    //During child generation please specify which nodes are chance nodes or not. This will help with our AB pruning later
    // Utility values generated in tree traversal. NO
    return null;
  }
}