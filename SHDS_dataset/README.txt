============= SHDS Dataset Generator =============================

-- Table of Contents --
 1) Compiling
 2) Running Modes
 3) Description of various functions
 

-- 1: Compiling --
All code was written for Java version 8 so your version is up to date.
The only outside library that is used is org.json for JSON reading and writing.
To compile, simply type this into the console:

    javac -cp json-20160810.jar;. *.java
    
-- 2: Running Modes --
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
        
-- 3: Description of various functions --
1 - SHDS.java
2 - Topology.java
3 - Generator.java
4 - RuleGenerator.java
5 - RuleParser.java
6 - Parameters.java
7 - Utilities.java
8 - inputs/DeviceDictionary.json
9 - inputs/Settings.json

______ 1 - SHDS.java ______________
This is the main method.
It is responsible for:
    reading the devices
    generating the topologies
    running the necessary generators and parsing user inputs
    
______ 2 - Topology.java __________
This is where the city, gridLength, and number of clusters come together to create the distribution of houses with their neighbors.
