
- [x] Multiagent spport
    - [x] Create agent class
    - [x] Create instance class
    - [x] Modify input file (2 agents)
    - [x] Test
    - [x] Agent spowner

- [x] MGM Algorithm:
    - [x] Copy first cycle instructions.
    - [x] Test algorithm with only first cycle
    - [x] Create data structure to store neighboring states (retrieve energy consumption, retrieve gain)
    - [x] Create functions to check messages received

- [x] Create statistics data structure
    - [x] Check messages and runtime of my framework
    - [x] Extend messages, runtime, solverTime, etc. per cycle (in AgentStatistics.java directly)

- [x] Integrate Parameters from input to solver (timeout, weights)

- [x] Build smart homes topology generator
    - [x] Specialize this generator for clusters and non-clusters
    
- [x] Build simple rule generator 
    - [x] Test

- [x] Generate Random 100 1-agent instance problems and solve then 
   - modify parameters of generator untill all problems are solved  

- [x] Check statistics object (include gains and energy / current)
- [x] Check new algorithm

- [x] Debug - it seems there is something wrong in the number of messages exchanged 
- [x] Flash if scheduler is infeasible

- [ ] Create Instances : 3 cities | number of devices | change weights
    - [ ] Compute weights normalizing constant (for each of the 3 problems)
 
"We sample neighborhoods in three cities in the United States (Des Moines, IA;
Boston, MA; and San Francisco, CA) and estimate the density of houses in each city. 
The average density (in houses per square kilometers) is 718 in Des Moines, 
1357 in Boston, and 3766 in San Francisco. 
For each city, we created a 200m 200m grid, where the distance between intersections is 20m, 
and randomly placed houses in this grid until the density is the same as the sampled density. 
Finally, we greedily placed aggregators, with a communication radius of 100m, 
in this grid until all houses are within the radius of at least one aggregator. 
Aggregators can then communicate with all homes and aggregators within its communication radius."

- [x] Statistics: Add stats for all agents

- [ ] Install on NMSU servers




