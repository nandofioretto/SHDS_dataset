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
        
### Description of various functions
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


### Available Datasets
We have included a couple folders full of datasets that you can use if you do not wish to create your own.
They have been compressed to save space on the repo.
There are two folders:
1. datasets/
  Contains the exact datasets used to generate the final table in the paper in json format, ready to use. They were split up into 3 parts with identical houses but different device rules (because of the limitations of our solver); the results were added together to create the table in the paper.
2. extra_datasets/
  Contains some extra datasets in CSV format. These datasets can be converted to json by using the '-regenerate <filename>' arguments on the program.
  
	**NOTE:** All of these datasets were generated using the settings outlined in the Settings.json file. DO NOT modify these settings if you wish to regenerate these files.
      These rules are generated with the following parameters: 60 minute granularity, 12 hour timespan

### Device Dictionary and Settings
There are two files located in the inputs/ folder. If you wish to change parameters such as time granularity or how much affect a device has on its environment, you can edit these.

- DeviceDictionary.json  The devices and sensors currently in this file are how they appear in the paper in Table 3.
If you wish to alter these devices, add new devices / sensor properties, you may need to modify RuleGenerator.java to ensure that it produces schedules that are satisfiable.
- Settings.json	These are the settings that the generator uses when generating the datasets.

### References:
- William Kluegel, Muhammad Aamir Iqbal, Ferdinando Fioretto, William Yeoh, Enrico Pontelli: 
[A Realistic Dataset for the Smart Home Device Scheduling Problem for DCOPs](https://www.cs.nmsu.edu/~wyeoh/OPTMAS2017/). 
In Proceedings of the International Workshop on Optimisation in Multi-Agent Systems (OPTMAS), 2017.	


### Please, cite this work as:
```
@inproceedings{fioretto:OPTMAS-17,
    author    = "William Kluegel and Muhammad Aamir Iqbal and Ferdinando Fioretto and William Yeoh and Enrico Pontelli",
    title     = "Solving {DCOP}s with Distributed Large Neighborhood Search",
    year      = "2017",
    booktitle = "Proceeding of the International Workshop on Optimisation in Multi-Agent Systems {(OPTMAS)}",
}
```

### Contacts
- Ferdinando Fioretto: fioretto@umich.edu
- William Kluegel: wkluegel@cs.nmsu.edu
