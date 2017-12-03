# SHDS_dataset
A repository for the Smart Home Device Scheduling dataset generation


## Using the SHDS Dataset Generator

### Compiling

All code was written for Java version 8 so your version is up to date.
The only outside library that is used is org.json for JSON reading and writing.
To compile, simply type this into the console:
```
javac -cp json-20160810.jar;. *.java
```

### Running Modes
To run the code, simply type:
```
java -cp json-20160810.jar;. SHDS <mode>
```
where <mode> is one of the following:
- _datasets_	generates datasets with same settings used in paper
- _extras_	generates extra datasets mentioned in paper
- _generate_ <cityID> <gridLength> <clusterDiv> <numDevices> 
	generates a custom dataset with extra arguments as settings, where:
	  - _cityID_  is a city identifier:
		0 for Des Moines
		1 for Boston
		2 for San Francisco
	  - _clusters_  defines the number of clusters in the problem (each cluster is a fully connected graph of houses, a grouping). Each cluster is connected to one other cluster, connecting the whole graph together.
	  - _gridLength_ is the area of the city (in meters^2) 
- _regenerate_ <fileName> <OPTIONALLY:coalitions>
    regenerates a dataset from a CSV file (provided from generating a dataset, there are also some provided in the repository)
	the reason we created these CSV files (which are used to encapsulate all of the random variables) is to save space on the repo -- they are much shorter than JSONs
    Optionally, you can choose a number of coalitions(clusters) < |agents| of agents to divide the agents into.

###### Example 1:
```
java -cp json-20160810.jar;. SHDS  -generate 0 10 1 4
```
The above will generate a dataset file for cID 0 (which is Des Moines) with a gridLength of 10 and 1 cluster (meaning all agents are in the same group)

###### Example 2:
```
java -cp json-20160810.jar;. SHDS  -regenerate fileName
```
It regenerates a dataset from a CSV file (provided from generating a dataset, there are also some provided in the repository). 
The reason we created these CSV files (which are used to encapsulate all of the random variables) is to save space on the repository.


## Dataset Format: How To Read The Generated Datasets
#### 1 JSON File Format
There is an example dataset in this directory called example1.json
The file is a JSONObject, which includes four subcomponents: horizon, granularity, priceSchema, and agents.
```
JSONObject{
	"horizon": integer,
	"granularity": integer,
	"priceSchema": integer[horizon],
	"agents": JSONObject
}
```
_horizon_     - number of timesteps. Calculated by taking the timespan (in minutes) and dividing by the granularity (in minutes).
_granularity_ - the size of each timestep in minutes.
_priceSchema_ - the cost of kWh for each timestep. It is an integer array that's length is equivalent to horizon.

_agents_ is a JSONObject, which has a JSONObject for each agent.
```
"agents": {
	"h1": JSONObject,
	"h2": JSONObject,
	.
	.
	.
	"hX": JSONObject
}
```
Each agent in agents has four subcomponents: neighbors, backgroundLoad, houseType, and rules.
```
"h1": {
	"neighbors": String[num_neighbors],
	"backgroundLoad": double[horizon],
	"houseType": integer,
	"rules": String[num_rules]
}
```
_neighbors_      - An array of all agents connected to this agent.
_backgroundLoad_ - An array of additional random kWh amount to make the kWh usage more difficult to flatten.
_houseType_      - The type of house this agent represents (0 = small, 1 = medium, 2 = large). There are different values in the device dictionary for each size house.
_rules_          - The constraints applied to this agent, the problem that this agent is trying to solve. Each element is separated by a ' '.

The elements in a rule are active, location, property, relation, goal, prefix, time1, and time2
Example rules:
```
active location   property      relation goal prefix time1 time2
1      room       cleanliness   gt         78 before     9
0      room       cleanliness   geq         0
0      room       cleanliness   leq       100
0      Roomba_880 charge        geq         0
0      Roomba_880 charge        leq       100
1      room       temp_heat     geq        22 within     4    7
0      room       temp_heat     geq         0
0      room       temp_heat     leq        33
```
_active_   - denotes whether the rule is active(1) or passive(0).
_location_ - the location that the rule applies to, whether that be a room or a specific device.
_property_ - the state property the rule applies to.
Likewise, relation, prefix, and time1/time2 denote the relation, goal state, time prefix, and time(s) the rule applies to respectively.

The first rule in the example is an active rule, used to impose the constraint "room must be greater than 78\% clean before 9".
When a cleanliness rule gets chosen, the associated passive rules get added as well.
Because cleanliness is percentage-based, it cannot go below 0 or above 100.
The actuator for cleanliness is the Roomba. The Roomba has a battery that cannot be below 0 or above 100 percent power, so two additional rules are created.
Rules 6, 7, and 8 are an example of air_temperature rules.

NOTE: prefix and time1/time2 are only necessary if the rule is an active rule. This is because passive rules must ALWAYS be true.
NOTE: time2 is only necessary for 'within' prefix

#### 2 CSV File Format
CSV files are files using a simple delimeter to separate data information so it is easily processed by another program.
They are useful because they take up a lot less space than the JSON files. NOTE: when converted into JSON, they should be identical to the JSON file the was generated.
Our CSV's use a ' ' to separate each element and '\n' to separate objects.
You don't have to worry about parsing the CSV's (unless you find them easier to work with), simply use the "-regenerate" command to create the JSON file for easier parsing. See "2: Running Modes".

This next section will walk through what each value is in case you wish to parse through them yourself;
A third option would be to take my code for parsing them and modify it to return whatever object format your system uses.

There is an example of a CSV file called example2.txt in this directory.
If you use the "-regenerate example2.txt" command, it will give you an identical JSON to example1.json.

The first line is the horizon, granularity, number of agents, number of clusters in that order.
After the first line, the first number represents which agent the row applies to.
If the second number is '-2' the third number is house type (0 = small, 1 = medium, 2 = large).
If the second number is '-1' the third number is a background load;
There should be exactly |horizon| background loads in a row, together they form the double[] of bg loads.


If the second number is any other number, it is an ID that maps to one of the rules and it's corresponding passive rules.
The third number is an indexing variable to make sure that the rule is correctly put back together.
The fourth number/string corresponds to the next part of the rule. Below is an example:
```
1 5 0 geq       <-- agent h1, water_temp, part #0, geq -- rule so far: "1 water_tank water_temp geq"
1 5 1 54                                                               "1 water_tank water_temp geq 54"
1 5 2 before                                                           "1 water_tank water_temp geq 54 before"
1 5 3 11                                                               "1 water_tank water_temp geq 54 before 11"
```
Once an active rule is fully parsed, the associated passive rules are added.

## Description of various functions
#### 1 SHDS.java
This is the main method.
It is responsible for:
    reading the devices
    generating the topologies
    running the necessary generators and parsing user inputs

#### 2 Topology.java
This is where the city, gridLength, and number of clusters come together to create the distribution of houses with their neighbors.

#### 3 Generator.java
Problem generation from parameters or from existing CSV (previously generated file)

#### 4 RuleGenerator.java
Uses timespan and granularity to generate rules, only tested with 60 minute timestep and 12 hour timespan

#### 5 RuleParser.java
Parses through a CSV file, returning a JSONArray with two JSONObjects
- 0: rules
- 1: background load

#### 6 Parameters.java
Defines where settings and deviceDictionary file are located

#### 7 Utilities.java
Some useful utility functions


## Available Datasets
We have included a couple folders full of datasets that you can use if you do not wish to create your own.
They have been compressed to save space on the repo.
There are two folders:
1. datasets/
  Contains the exact datasets used to generate the final table in the paper in json format, ready to use.
  Also Contains a copy of DeviceDictionary.json --- the list of devices used to generate the dataset.
  DeviceDictionary.json is structured as a JSON Array with three JSON Objects (small, medium, and large house types in that order --- these are outlined in the paper). The only difference between each house JSON Object is the change over time for each action of each device.
  Additional datasets can be generated through the java code provided using the command - _extra_ as described in section _2: Running Modes_.
	**NOTE:** All of these datasets were generated using the settings outlined in the Settings.json file. DO NOT modify these settings if you wish to regenerate these files.
      These rules are generated with the following parameters: 60 minute granularity, 12 hour timespan

## Device Dictionary and Settings
There are two files located in the inputs/ folder. If you wish to change parameters such as time granularity or how much affect a device has on its environment, you can edit these.

- DeviceDictionary.json  The devices and sensors currently in this file are how they appear in the paper in Table 3.
If you wish to alter these devices, add new devices / sensor properties, you may need to modify RuleGenerator.java to ensure that it produces schedules that are satisfiable.
- Settings.json	These are the settings that the generator uses when generating the datasets.


## References:
- William Kluegel, Muhammad Aamir Iqbal, Ferdinando Fioretto, William Yeoh, Enrico Pontelli: 
[A Realistic Dataset for the Smart Home Device Scheduling Problem for DCOPs](https://www.cs.nmsu.edu/~wyeoh/OPTMAS2017/). 
In Proceedings of the International Workshop on Optimisation in Multi-Agent Systems (OPTMAS), 2017.	


## Citing
```
@inproceedings{fioretto:OPTMAS-17,
    author    = "William Kluegel and Muhammad Aamir Iqbal and Ferdinando Fioretto and William Yeoh and Enrico Pontelli",
    title     = "Solving {DCOP}s with Distributed Large Neighborhood Search",
    year      = "2017",
    booktitle = "Proceeding of the International Workshop on Optimisation in Multi-Agent Systems {(OPTMAS)}",
}
```

## Contacts
- Ferdinando Fioretto: fioretto@umich.edu
- William Kluegel: wkluegel@cs.nmsu.edu