**Global Search Agent: Astar Algorithm**

Objective: Destroy enemy townhall and return to starting position --> enemy units subsequently perish (from grief) and simulation is won
This AI agent uses an A* algorithm to find routes between desired coordinates on the map.
A* heuristic gets neighboring tiles and assigns costs based on percieved risk values. The closer the tile is to an enemy position, the more it costs. This allows our agent to move intelligently without having to predict enemy moves in advance.
  **Cost Modeling**
  
  **Things to Consider:** Movement toward objective & staying out of enemy unit range (Chebyshev distance of 2)
  
  Our cost function discounts neighbors closer to the current object. This creates the effect of almost "pulling" the agent toward the townhall, or toward the starting tile on returning.
  
  '''
   if (towardGoal(src, dst, state)) {
             cost = (float) 0.5 * euclidian_distance(src, dst);
             if (cost < 1) {
                 cost = 1;
             }
    } else {
            cost = euclidian_distance(src, dst);
    }
   '''
  
  After goal discounts, our cost function considers the distance for each enemy unit. Cost decreases exponentially as distance from an enemey increases.
  Additional costs are added to tiles that are within 3 or 2 units of an enemy. This creates an effect where our agent "steps-around" tiles which will prove a threat in the future.
  '''
   for (UnitView archer : this.getArchers()) {
            Vertex archerPos = new Vertex(archer.getXPosition(), archer.getYPosition());
            float distance = chebyshevDistance(dst, archerPos);
            
            // Additional cost is inverse the distance to enemy by map size
            cost += (state.getYExtent()/distance);
            if (distance <= 2) {
                cost += ((state.getYExtent()/distance) * 2);
            } else if (distance <=3 && distance > 2) {
                cost += ((state.getYExtent()/distance) * 2);
            }
        }
        return cost;
    }
  '''

  

**Run Maze Simulation**

  **# Mac, Linux. Run from the cs440 directory.**
  javac -cp "./lib/*:." @pas-stealth.srcs
  java -cp "./lib/*:." edu.cwru.sepia.Main2 data/pas/stealth/[MazeName].xml
  
  **# Windows. Run from the cs440 directory.**
  javac -cp ./lib/*;. @pas-stealth.srcs
  java -cp ./lib/*;. edu.cwru.sepia.Main2 data/pas/stealth/[MazeName].xml
  

**Conclusions**

Accuracy quotas were used for each map based on difficulty for the A* agent. After running 300 simulations for each maze, our accuracy was as follows.:
- OneUnit Small Maze 95% games won (95%/100% quota met)
- TwoUnit Small Maze 82% games won (82%/95% quota met)
- FourUnit Big Maze 20% games won (20%/20% quota met)
- Overall score: 94%
  **Factors Which Influenced Accuracy**
-   Random enemy movement can lead to pinsir movements, sometimes leading to losing terminal states no matter which moves our A* agent makes
-   Our unit is "pulled" toward the goal because of cost discounts. This means while they might side-step around enemies, they will not retreat, leading to states where our agent throws themselves into danger in favor of approaching the goal. While this made our agent far simpler and less operationally intensive, it definitely affected its accuracy.
