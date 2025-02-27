package src.pas.stealth.agents;


// SYSTEM IMPORTS
import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.UnitTemplate;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


// JAVA PROJECT IMPORTS
import edu.bu.pas.stealth.agents.AStarAgent;                // the base class of your class
import edu.bu.pas.stealth.agents.AStarAgent.AgentPhase;     // INFILTRATE/EXFILTRATE enums for your state machine
import edu.bu.pas.stealth.agents.AStarAgent.ExtraParams;    // base class for creating your own params objects
import edu.bu.pas.stealth.graph.Vertex;                     // Vertex = coordinate
import edu.bu.pas.stealth.graph.Path;                       // see the documentation...a Path is a linked list



public class StealthAgent
    extends AStarAgent
{
    public class VertexExtraParams extends AStarAgent.ExtraParams {
        private Vertex vertex;
    
        public VertexExtraParams(Vertex vertex) {
            this.vertex = vertex;
        }
    
        public Vertex getVertex() {
            return vertex;
        }
    
        public void setVertex(Vertex vertex) {
            this.vertex = vertex;
        }
    }

    // Fields of this class
    // TODO: add your fields here! For instance, it might be a good idea to
    // know when you've killed the enemy townhall so you know when to escape!
    // TODO: implement the state machine for following a path once we calculate it
    //       this will for sure adding your own fields.
    private int enemyChebyshevSightLimit;
    private int start_x;
    private int start_y;
    private boolean townhall = false;
    private boolean gold = false;
    private ArrayList<UnitView> archers = new ArrayList<>();
    private Vertex townhall_coords = new Vertex(0,0);
    private Vertex goalVertex = new Vertex(0, 0);
    private List<Vertex> instructions = new ArrayList<>();
    private Map<Integer, Vertex> previousEnemyPositions = new HashMap<>();

    public boolean isValid(Vertex vert, StateView state, Vertex goal) {
        int x = vert.getXCoordinate();
        int y = vert.getYCoordinate();
        return (x >= 0 && y >= 0 && x < state.getXExtent() && y < state.getYExtent() && (!state.isResourceAt(x, y))) || vert == goal;
    }

    public Collection<Vertex> getValidNeighbors(Vertex vert, StateView state) {
        Vertex goal = this.getTownhallCoords();
        Collection<Vertex> verts = new ArrayList<>();
        int[] rows = {-1, -1, 1, 1, -1, 1, 0, 0};
        int[] cols = {-1, 1, -1, 1, 0, 0, -1, 1};
        for (int i = 0; i < rows.length; i++) {
            int x = vert.getXCoordinate() + rows[i];
            int y = vert.getYCoordinate() + cols[i];
            Vertex newVert = new Vertex(x, y);
            if (isValid(newVert, state, goal)) {
                verts.add(newVert);
            }
        }
        return verts;
    }
    

    public StealthAgent(int playerNum)
    {
        super(playerNum);

        this.enemyChebyshevSightLimit = -1; // invalid value....we won't know this until initialStep()

    }

    // TODO: add some getter methods for your fields! Thats the java way to do things!
    public final int getEnemyChebyshevSightLimit() { return this.enemyChebyshevSightLimit; }

    public final int getStart_X() { return this.start_x; }

    public final int getStart_Y() {return this.start_y; }

    public final boolean getGold() { return this.gold; }

    public void setEnemyChebyshevSightLimit(int i) { this.enemyChebyshevSightLimit = i; }

    public void setStart_X(int i) { this.start_x = i; }

    public void setStart_Y(int i) { this.start_y = i; }

    public void gotGold() { this.gold = true; }

    public void setArchers(StateView state) {
        // Used to keep track of enemy positions on map
        ArrayList<UnitView> temp = new ArrayList<>();
        List<UnitView> enemyUnits = state.getAllUnits();
        Map<Integer, Vertex> newEnemyPositions = new HashMap<>();

        for (int i = 0; i < enemyUnits.size(); i++) {
            UnitView enemy = enemyUnits.get(i);
            String unitTypeName = enemy.getTemplateView().getName();
            if(unitTypeName.equals("Archer")) {
                temp.add(enemy);
                Vertex currentPos = new Vertex(enemy.getXPosition(), enemy.getYPosition());
                newEnemyPositions.put(enemy.getID(), currentPos);
            }
        }
        this.archers = temp;
        this.previousEnemyPositions = newEnemyPositions; 
    }
    public final Map<Integer, Vertex> getPreviousEnemyPositions() {
        return this.previousEnemyPositions;
    }

    public final ArrayList<UnitView> getArchers() {
        return this.archers;
    }

    public void setTownhallCoords(StateView state) {
        // Used for path finding to townhall
        UnitView townHall = state.getUnit(getEnemyBaseUnitID());
        int x = townHall.getXPosition();
        int y = townHall.getYPosition();
        Vertex goal = new Vertex(x, y);
        this.townhall_coords = goal;
    }
    public final Vertex getTownhallCoords() {
        return this.townhall_coords;
    }

    public void setInstructions(Path path) {
        // Converts AStar path returned into list of vertecies to move to
        List<Vertex> instructions = new ArrayList<>();
        while (path != null) {
            instructions.add(path.getDestination());  // Add each vertex to the instructions
            path = path.getParentPath();  // Move to the parent path
        }
        Collections.reverse(instructions);  // Reverse the list to start from the source
        this.instructions = instructions;
    }
    public final List<Vertex> getInstructions() {
        return this.instructions;
    }

    ///////////////////////////////////////// Sepia methods to override ///////////////////////////////////

    /**
        TODO: if you add any fields to this class it might be a good idea to initialize them here
              if they need sepia information!
     */

    
    @Override
    public Map<Integer, Action> initialStep(StateView state,
                                            HistoryView history)
    {
        super.initialStep(state, history); // call AStarAgent's initialStep() to set helpful fields and stuff

        // now some fields are set for us b/c we called AStarAgent's initialStep()
        // let's calculate how far away enemy units can see us...this will be the same for all units (except the base)
        // which doesn't have a sight limit (nor does it care about seeing you)
        // iterate over the "other" (i.e. not the base) enemy units until we get a UnitView that is not null
        UnitView otherEnemyUnitView = null;
        Iterator<Integer> otherEnemyUnitIDsIt = this.getOtherEnemyUnitIDs().iterator();
        while(otherEnemyUnitIDsIt.hasNext() && otherEnemyUnitView == null)
        {
            otherEnemyUnitView = state.getUnit(otherEnemyUnitIDsIt.next());
        }

        if(otherEnemyUnitView == null)
        {
            System.err.println("[ERROR] StealthAgent.initialStep: could not find a non-null 'other' enemy UnitView??");
            System.exit(-1);
        }

        this.setTownhallCoords(state);
        this.goalVertex = this.getTownhallCoords();
        this.setArchers(state);

        // lookup an attribute from the unit's "template" (which you can find in the map .xml files)
        // When I specify the unit's (i.e. "footman"'s) xml template, I will use the "range" attribute
        // as the enemy sight limit
        this.setEnemyChebyshevSightLimit(otherEnemyUnitView.getTemplateView().getRange());

        // Set starting coords so we know where to return to
        UnitView unit = state.getUnit(getMyUnitID());
        this.start_x = unit.getXPosition();
        this.start_y = unit.getYPosition();
        Vertex current = new Vertex(unit.getXPosition(), unit.getYPosition());
        
        this.setInstructions((aStarSearch(current, townhall_coords, state, null)));

        return null;
    }
    

    /**
        TODO: implement me! This is the method that will be called every turn of the game.
              This method is responsible for assigning actions to all units that you control
              (which should only be a single footman in this game)
     */
    @Override
    public Map<Integer, Action> middleStep(StateView state, HistoryView history) {
        Map<Integer, Action> actions = new HashMap<Integer, Action>();
        UnitView unit = state.getUnit(getMyUnitID()); 
        Vertex current = new Vertex(unit.getXPosition(), unit.getYPosition());

        // Are we near the townhall? Attack!
        if (chebyshevDistance(current, this.getTownhallCoords()) == 1 && this.getAgentPhase() == AStarAgent.AgentPhase.INFILTRATE) {
            if (!townHallDead(state)) {
                actions.put(getMyUnitID(), Action.createPrimitiveAttack(getMyUnitID(), getEnemyBaseUnitID()));
            } else {
                this.setAgentPhase(AStarAgent.AgentPhase.EXFILTRATE);
            }
        // Else --> shouldReplacePlan & Astarsearch path finding
        } else {
            if (!this.getInstructions().isEmpty()) {
                if (!this.shouldReplacePlan(state, null)) {
                    this.getInstructions().remove(0);
                    Vertex nextMove = this.getInstructions().get(0);
                    actions.put(getMyUnitID(), Action.createPrimitiveMove(getMyUnitID(), getDirectionToMoveTo(current, nextMove)));
                } else {
                    // Reset instructions and move to new lowest risk tile
                    if (this.getAgentPhase() == AStarAgent.AgentPhase.EXFILTRATE) {
                        this.goalVertex = new Vertex(this.start_x, this.start_y);
                        System.out.println(this.start_x);
                        System.out.println(this.start_y);
                    }
                    this.setInstructions(aStarSearch(current, this.goalVertex, state, null));
                    this.getInstructions().remove(0);
                    Vertex nextMove = this.getInstructions().get(0);
                    actions.put(getMyUnitID(), Action.createPrimitiveMove(getMyUnitID(), getDirectionToMoveTo(current, nextMove)));
                }
            }    
        }
       
        
        return actions;
        /**
            I would suggest implementing a state machine here to calculate a path when neccessary.
            For instance beginning with something like:

            if(this.shouldReplacePlan(state))
            {
                // recalculate the plan
            }

            then after this, worry about how you will follow this path by submitting sepia actions
            the trouble is that we don't want to move on from a point on the path until we reach it
            so be sure to take that into account in your design

            once you have this working I would worry about trying to detect when you kill the townhall
            so that you implement escaping
         */
    }

    /*
     * if (unit.getID() == getEnemyBaseUnitID()) {
     */

    ////////////////////////////////// End of Sepia methods to override //////////////////////////////////

    /////////////////////////////////// AStarAgent methods to override ///////////////////////////////////

    public Collection<Vertex> getNeighbors(Vertex v, StateView state, ExtraParams extraParams) {
        Collection<Vertex> neighbors = getValidNeighbors(v, state);
        return neighbors;
    }

    public Path aStarSearch(Vertex src, Vertex dst, StateView state, ExtraParams extraParams) {
        PriorityQueue<Path> openSet = new PriorityQueue<>(Comparator.comparingDouble(path -> path.getTrueCost() + path.getEstimatedPathCostToGoal()));
        HashMap<Vertex, Float> totalCost = new HashMap<>();
        HashMap<Vertex, Vertex> parents = new HashMap<>();

        openSet.add(new Path(src)); // Start from the source
        totalCost.put(src, 0f);
        while (!openSet.isEmpty()) {
            Path currentPath = openSet.poll();
            Vertex currentTile = currentPath.getDestination();

            // If we are 1 tile adjacent to townhall
            if (chebyshevDistance(currentTile, dst) == 1) {
                return currentPath;
            }

            // Expand neighbors
            for (Vertex neighbor : getNeighbors(currentTile, state, extraParams)) {
                float edgeCost = getEdgeWeight(currentTile, neighbor, state, extraParams);
                float tenantiveCost = currentPath.getTrueCost() + edgeCost;
                float heuristicCost = euclidian_distance(neighbor, dst);

                if (!totalCost.containsKey(neighbor) || tenantiveCost < totalCost.get(neighbor)) {
                    totalCost.put(neighbor, tenantiveCost);
                    parents.put(neighbor, currentTile);

                    //New Path: destination vertex, true edge cost, estimate edge cost, parent path
                    openSet.add(new Path(neighbor, edgeCost, heuristicCost, currentPath));
                }
            }

        }

        return null;
    }

    public float euclidian_distance(Vertex d1, Vertex d2) {
        int d1_x = d1.getXCoordinate();
        int d1_y = d1.getYCoordinate();
        int d2_x = d2.getXCoordinate();
        int d2_y = d2.getYCoordinate();

        return (float) Math.sqrt(Math.pow((d2_x - d1_x),2) + Math.pow((d2_y - d1_y),2));
    }


    public boolean towardGoal(Vertex current, Vertex dest, StateView state) {
        // If the neighbor tile we are evaluating is closer to townhall
        Vertex goal = this.goalVertex;

        if (euclidian_distance(current, goal) > euclidian_distance(dest, goal)) {
            return true;
        } else {
            return false;
        }
    }


    public float getEdgeWeight(Vertex src, Vertex dst, StateView state, ExtraParams extraParams) {
        // src = current node, dst = neighbor node we are evaluating
        float cost = 1f;

        // If the neighbor we are looking at it closer to the townhall make it cost half as much & make sure its still > 1
        if (towardGoal(src, dst, state)) {
            cost = (float) 0.5 * euclidian_distance(src, dst);
            if (cost < 1) {
                cost = 1;
            }
        } else {
            cost = euclidian_distance(src, dst);
        }
    
        // Check the distance to all archers and increase the cost if we're near an archer
        for (UnitView archer : this.getArchers()) {
            Vertex archerPos = new Vertex(archer.getXPosition(), archer.getYPosition());
            float distance = chebyshevDistance(dst, archerPos);
            
            // Additional cost is inverse the distance to enemy by map size
            cost += (state.getYExtent()/distance);
            if (distance <= 2) {
                cost += (state.getYExtent()/distance);
            }
        }
        return cost;
    }

    public float chebyshevDistance(Vertex v1, Vertex v2) {
        return Math.max(Math.abs(v1.getXCoordinate() - v2.getXCoordinate()), Math.abs(v1.getYCoordinate() - v2.getYCoordinate()));
    }

    public boolean shouldReplacePlan(StateView state, ExtraParams extraParams) {
        // Enemy moves. Replace plan and reset enemyPosition variable
        Map<Integer, Vertex> currentEnemyPositions = getPreviousEnemyPositions();
        List<UnitView> enemyUnits = state.getAllUnits();

        for (int i = 0; i < enemyUnits.size(); i++) {
            UnitView enemy = enemyUnits.get(i);
            Vertex lastKnownPosition = currentEnemyPositions.get(enemy.getID());
            String unitTypeName = enemy.getTemplateView().getName();
            if(unitTypeName.equals("Archer")) {
                Vertex currentPos = new Vertex(enemy.getXPosition(), enemy.getYPosition());
                if (!currentPos.equals(lastKnownPosition)) {
                    this.setArchers(state);
                    return true;
                }
            }
        }
        return false;
    }
    public boolean townHallDead(StateView state) {
        // Used to switch to exfiltrate mode
        List<UnitView> enemyUnits = state.getAllUnits();
        for (UnitView enemy: enemyUnits) {
           if (enemy.getID() == getEnemyBaseUnitID()) {
            return false;
           }
        }
        System.out.println("CHANGE TO EXFILTRATE");
        return true;
    }

    //////////////////////////////// End of AStarAgent methods to override ///////////////////////////////

}

