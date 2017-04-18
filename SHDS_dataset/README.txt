============= SHDS Dataset Generator =============================

-- Table of Contents --
 1) Compiling
 2) Running Modes
 3) Description of various functions
 4) Available Datasets
 5) Device Dictionary and Settings
 

-- 1: Compiling ------------------------------------------------------------------
All code was written for Java version 8 so your version is up to date.
The only outside library that is used is org.json for JSON reading and writing.
To compile, simply type this into the console:

    javac -cp json-20160810.jar;. *.java
    
-- 2: Running Modes --------------------------------------------------------------
To run the code, simply type:

    java -cp json-20160810.jar;. SHDS <mode>

where <mode> is one of the following (without the quotation marks):

    "-datasets"
        generates datasets with same settings used in paper

    "-extras"
        generates extra datasets mentioned in paper

    "-generate <cityID> <gridLength> <clusterDiv> <numDevices>"
        generates a custom dataset with extra arguments as settings
        
        cityID --- City identifier. We have 3 cities:
            0) Des Moines
            1) Boston
            2) San Francisco
        clusters --- Number of clusters in the problem (each cluster is a fully connected graph of houses, a grouping).
            Each cluster is connected to one other cluster, connecting the whole graph together.
        gridLength --- Chunk of the city covered by the problem, this is in meters^2

        example input:
        -generate 0 10 1 4
            The above will generate a dataset file for cID 0 (which is Des Moines) with a gridLength of 10 and 1 cluster (meaning all agents are in the same group)

    "-regenerate <fileName>"
        regenerates a dataset from a CSV file (provided from generating a dataset, there are also some provided in the repository)
	the reason we created these CSV files (which are used to encapsulate all of the random variables) is to save space on the repo -- they are much shorter than JSONs
        
-- 3: Description of various functions -------------------------------------------
1 - SHDS.java
2 - Topology.java
3 - Generator.java
4 - RuleGenerator.java
5 - RuleParser.java
6 - Parameters.java
7 - Utilities.java

__ 1 - SHDS.java ______________
This is the main method.
It is responsible for:
    reading the devices
    generating the topologies
    running the necessary generators and parsing user inputs
    
__ 2 - Topology.java __________
This is where the city, gridLength, and number of clusters come together to create the distribution of houses with their neighbors.

__ 3 - Generator.java _________
Problem generation from parameters or from existing CSV (previously generated file)

__ 4 - RuleGenerator.java _____
Uses timespan and granularity to generate rules, only tested with 60 minute timestep and 12 hour timespan

__ 5 - RuleParser.java ________
Parses through a CSV file, returning a JSONArray with two JSONObjects
[0] -- rules
[1] -- background load

__ 6 - Parameters.java ________
Defines where settings and deviceDictionary file are located

__ 7 - Utilities.java _________
Some useful utility functions

-- 4: Available Datasets --------------------------------------------------------
We have included a couple folders full of datasets that you can use if you do not wish to create your own.
They have been compressed to save space on the repo.
There are two folders:
 1) datasets/
    --- Contains the exact datasets used to generate the final table in the paper in json format, ready to use.
        They were split up into 3 parts with identical houses but different device rules (because of the limitations of our solver); the results were added together to create the table in the paper.
 2) extra_datasets/
    --- Contains some extra datasets in CSV format. These datasets can be converted to json by using the '-regenerate <filename>' arguments on the program.
NOTE: All of these datasets were generated using the settings outlined in the Settings.json file. DO NOT modify these settings if you wish to regenerate these files.
      These rules are generated with the following parameters: 60 minute granularity, 12 hour timespan

-- 5: Device Dictionary and Settings --------------------------------------------
There are two files located in the inputs/ folder. If you wish to change parameters such as time granularity or how much affect a device has on its environment, you can edit these.
DeviceDictionary.json --- The devices and sensors currently in this file are how they appear in the paper in Table 3.
                          If you wish to alter these devices, add new devices / sensor properties, you may need to modify RuleGenerator.java to ensure that it produces schedules that are satisfiable.
Settings.json         --- These are the settings that the generator uses when generating the datasets.

** For more information about this dataset generator,
** please find our paper on OPTMAS-17 website located here: https://www.cs.nmsu.edu/~wyeoh/OPTMAS2017/