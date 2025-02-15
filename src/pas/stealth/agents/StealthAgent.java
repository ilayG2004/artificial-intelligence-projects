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
import java.util.HashMap;
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

    public boolean isValid(Vertex vert, StateView state) {
        int x = vert.getXCoordinate();
        int y = vert.getYCoordinate();
        return (x >= 0 && y >= 0 && x < state.getXExtent() && y < state.getYExtent() && (!state.isResourceAt(x, y)));
    }

    public Collection<Vertex> getValidNeighbors(Vertex vert, StateView state) {
        Collection<Vertex> verts = new ArrayList<>();
        int[] rows = {-1, -1, 1, 1, -1, 1, 0, 0};
        int[] cols = {-1, 1, -1, 1, 0, 0, -1, 1};
        for (int i = 0; i < rows.length; i++) {
            int x = vert.getXCoordinate() + rows[i];
            int y = vert.getYCoordinate() + cols[i];
            Vertex newVert = new Vertex(x, y);
            if (isValid(newVert, state)) {
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

    public final boolean getTownhall() { return this.townhall; }

    public final boolean getGold() { return this.gold; }

    public void setEnemyChebyshevSightLimit(int i) { this.enemyChebyshevSightLimit = i; }

    public void setStart_X(int i) { this.start_x = i; }

    public void setStart_Y(int i) { this.start_y = i; }

    public void killTownhall() { this.townhall = true; }

    public void gotGold() { this.gold = true; }


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

        // lookup an attribute from the unit's "template" (which you can find in the map .xml files)
        // When I specify the unit's (i.e. "footman"'s) xml template, I will use the "range" attribute
        // as the enemy sight limit
        this.setEnemyChebyshevSightLimit(otherEnemyUnitView.getTemplateView().getRange());

        return null;
    }

    /**
        TODO: implement me! This is the method that will be called every turn of the game.
              This method is responsible for assigning actions to all units that you control
              (which should only be a single footman in this game)
     */
    @Override
    public Map<Integer, Action> middleStep(StateView state,
                                           HistoryView history)
    {
        Map<Integer, Action> actions = new HashMap<Integer, Action>();

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

        return actions;
    }

    ////////////////////////////////// End of Sepia methods to override //////////////////////////////////

    /////////////////////////////////// AStarAgent methods to override ///////////////////////////////////

    public Collection<Vertex> getNeighbors(Vertex v,
                                           StateView state,
                                           ExtraParams extraParams)
    {
        // getting our townhall ID then constructing a unit for it
        Collection<Vertex> neighbors = getValidNeighbors(v, state);
        Iterator<Vertex> neighborIterator = neighbors.iterator();

        while (neighborIterator.hasNext()) {
            Vertex neighbor = neighborIterator.next();
            int x = neighbor.getXCoordinate();
            int y = neighbor.getYCoordinate();
            System.out.print(x);
            System.out.print(y);
            System.out.println();
        }
        
        return null;
    }

    public Path aStarSearch(Vertex src,
                            Vertex dst,
                            StateView state,
                            ExtraParams extraParams)
    {
        return null;
    }

    public float getEdgeWeight(Vertex src,
                               Vertex dst,
                               StateView state,
                               ExtraParams extraParams)
    {
        return 1f;
    }

    public boolean shouldReplacePlan(StateView state,
                                     ExtraParams extraParams)
    {
        return false;
    }

    //////////////////////////////// End of AStarAgent methods to override ///////////////////////////////

}

