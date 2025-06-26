**Three Projects:**

**1. Maze Search Agent Astar** src < pas < stealth < agents < StealthAgent.java

**2. Stochastic Adversarial Search Pokemon** src < pas < pokemon < agents < Node.java & TreeTraversalAgent.java

**3. Reinforcement Learning Tetris** src < pas < tetris < agents < TetrisQAgent.java

----

**Global Search Agent: Astar Algorithm**

Objective: Destroy enemy townhall and return to starting position --> enemy units subsequently perish (from grief) and simulation is won
This AI agent uses an A* algorithm to find routes between desired coordinates on the map.
A* heuristic gets neighboring tiles and assigns costs based on percieved risk values. The closer the tile is to an enemy position, the more it costs. This allows our agent to move intelligently without having to predict enemy moves in advance.

  **Cost Modeling**
  
  **Things to Consider:** Movement toward objective & staying out of enemy unit range (Chebyshev distance of 2)
  
  Our cost function discounts neighbors closer to the current object. This creates the effect of almost "pulling" the agent toward the townhall, or toward the starting tile on returning.
  
 ```
   if (towardGoal(src, dst, state)) {
             cost = (float) 0.5 * euclidian_distance(src, dst);
             if (cost < 1) {
                 cost = 1;
             }
    } else {
            cost = euclidian_distance(src, dst);
    }
 ```
  
  After goal discounts, our cost function considers the distance for each enemy unit. Cost decreases exponentially as distance from an enemey increases.
  Additional costs are added to tiles that are within 3 or 2 units of an enemy. This creates an effect where our agent "steps-around" tiles which will prove a threat in the future.
  ```
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
  ```

  

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

----

**Stochastic Adversarial Search Pokemon**

Objective: Create a tree search algorithm and heuristic that can inform Pokémon trainer to make optimal battle decisions and defeat enemy trainers.

**Building the Tree:** 
Node objects were created of several types. Min nodes (representing our enemy's turn to minimize our utility), Max nodes (representing our turn to maximize our utility), and chance nodes (representing the stochastic outcome following either player's move). Min or Max nodes consider all legal moves available at the state of the turn. Since there are a combinatorial number of ways for a move to resolve, we intuitively created a function which selects the most probable outcome from all outcomes of a move. Thus, after each Min or Max node, there was only one chance node, representing the summary of outcomes. This reduced the size of our decision tree dramatically, and allowed for our agent to make predictions further in advance. For multi-hit moves, which have the potential to hit 2-5 times in a turn, and for each of those attack have a chance to critical hit or miss, the number of chance nodes generated could cause the game to crash due to heap-space usage. We decided to average out the number of times a multihit move attacks to just 3, and then calculate the damage applied from that move hitting three times as the singular chance node generated for that move. This also worked to our advantage since other students had to ignore predicting multi-hit possibilities in fear of crashing their agent. For the sake of this assignment, since our agent was given good pokemon, we assume we always start first. The tree follows a pattern branching out of: Max --> Chance... --> Min... --> Chance... --> Max..., branching out wider with each turn evaluated.
If the tree reaches a terminal state, meaning either the agent or its opponent are out of pokemon, node generation stops early.

**A-B Pruning:** Given our tree, which branches are impossible? Each Max/Min node is pruned if the expected outcome is already worse than a known alternative. Chance nodes are always fully evaluated but do not prune their children, consistent with standard expectimax. This saves time calculating utility for impossible branches and optimizes time during decision making.

**Hueristic:** 
Our heuristic is used to assign utility value to non-terminal nodes. Without getting too deep into Pokemon battle mechanics, our agent would evaluate our current moves.
-Do we have any 'super effective' moves on our current Pokemon to attack the enemy with?
-Does the enemy have any 'super effective' moves it can use on us?
-Do we get 'same type attack bonus' (STAB) when selecting a move?
-Do we have status moves that could dramatically reduce important stats for the enemy's Pokemon (such as accuracy; this is the most important stat in Generation 1 battle mechanics)
-Do we have any Pokemon that have a more optimal type match-up? (resisting attacks, and dealing super effective damage)

**Conclusions**
Our agent successfully, and swiftly defeated the easy, medium, and hard difficulty trainers, selecting super effective moves, and switching to use different Pokemon when vulnerable. Possible improvements include some kind of function calculating which player would go first before building the tree, but as stated earlier, the Pokemon our agent was given, as well as those of the opponent made it reasonable to assume our agent would always go first (we were given faster Pokemon).

----

**Tetris Reinforcement Learning**
Objective: Train an agent to play tetris and score an average of 20 points across 500 games.

**What features would make our agent perform better**
-Topography: Generally speaking, an incredibly bumpy tetris board is not optimal. Although human players with better planning skills might set the board this way on purpose.
-Height: The closer your blocks are to the top of the screen, the closer you are to losing. Ideally, we want our agent to avoid stacking for height as much as possible.
-Number of unreachable spaces: If blocks are stacked in a way such that empty bubble form between those blocks, we dramatically reduce our chances of clearing a row. Too many empty spaces nested between blocks adds up, and our agent is forced to stack upward since they cannot clear the row.
-Snuggness of pieces: Pieces should fit well together. Evaluating if each square making up a block (tetraimo) is touching existing blocks on the board is important. Snug fits lead to less empty spaces and a cohesive way to fill the board.

**Granularization & Exploration**
In order for our model to gain novel experiences, we created a large HashTable which stored a vector of our 4 features values at the state it was seen as the key, and an integer as the value denoting how many times that particular state was seen. This was preferable over storing the entire game matrix, since it saved us a lot on memory. When evaluating how to place a block, we examine possible states from the outcome of placing it. If we have seen a particular state less than 2 times we prioritize placing the block in this manner to gain a novel experience.

**The Nueral Network**
Input layer of the 4 featurs discussed above, with a single ReLu activation function; this was all that was needed for our model to succeed. A replay buffer of 1,000,000 was set so the model could store novel experiences. Trained for 6 hours in Boston University's SCC on a linux machine.

**Life is Pain: The utility function**
Our agent was always penalized for heavily for height, and empty spaces. It was only rewarded a little bit for snuggness (to encourage placing blocks snugly), and rewarded a lot for scoring points (but it never offset the negative utility). Scoring points was rare and this large reward resulted in prioritizing clearing a row if the agent could.

**Conclusions**
The model plays a very flat game of tetris. It places piece that fit-well together and avoids stacking high. Sometimes however, in its avoidance to stack high, it places pieces unoptimally, leaving tiny empty spaces in rows. This evantually leads to the model failing after a good number of turns. However, it scores an average of 25 points across 500 games. This recieves full credit and more. 
Better reward tuning to avoid blank spaces nested between blocks could possibly lessen this issue.
A more complex neural network also could have been used for more advanced learning given our limited feature set.
