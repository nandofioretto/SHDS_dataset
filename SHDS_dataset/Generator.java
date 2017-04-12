import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates SHDS problems
 */
public class Generator {

    private final int timeHorizon = Parameters.getHorizon();
    private final double[] priceSchema = Parameters.getPriceSchema();
    private int nDevices;
    private RuleGenerator ruleGenerator;
    private Topology topology;
    private int[] houseRatio;

    private ArrayList<String> ruleCSV = new ArrayList<>();

    public Generator(Topology topology, RuleGenerator ruleGenerator, int nDevices, int[] houseRatio) {
        this.topology = topology;
        this.ruleGenerator = ruleGenerator;
        this.nDevices = nDevices;
        this.houseRatio = houseRatio;
    }


    private double[] generateBackgroundLoad() {
        double[] bg = new double[timeHorizon];
        for (int i = 0; i < timeHorizon; i++) {
            bg[i] = ThreadLocalRandom.current().nextDouble(0, 0.3);
        }
        return bg;
    }

    private int[] numEachHouse() {
        int[] numEach = new int[3];
        int total     = houseRatio[0] + houseRatio[1] + houseRatio[2];
        int mult      = topology.getNumAgents() / total;
        int remainder = topology.getNumAgents() % total;
        numEach[0] = houseRatio[0] * mult;
        numEach[1] = houseRatio[1] * mult;
        numEach[2] = houseRatio[2] * mult;
        for(int i = 0; i < remainder; i++) {
            numEach[i%3]++;
        }
        return numEach;
    }
    
    public JSONObject generate(String fileName) {
        // All agents within a cluster share a constraint
        JSONObject jExperiment = new JSONObject();

        try {
            jExperiment.put("horizon", timeHorizon);
            jExperiment.put("priceSchema", priceSchema);

            JSONObject jAgents = new JSONObject();

            int aCount = 0;
            int hType = 0;
            int[] numEachType = numEachHouse();
            for (String agtName : topology.getAgents()) {

                //shifts to the next type of house (small, medium, or large)
                while(aCount == numEachType[hType] && hType < 3) {
                    hType++;
                    aCount = 0;
                }

                JSONObject jAgent = new JSONObject();

                // Create array of neighbors
                JSONArray jNeighbors = new JSONArray();

                for (String neigName : topology.getNeighbors(agtName)) {
                    if (neigName.compareTo(agtName) != 0)
                        jNeighbors.put(neigName);
                }

                jAgent.put("houseType", hType);
                jAgent.put("neighbors", jNeighbors);
                jAgent.put("backgroundLoad", generateBackgroundLoad());
                jAgent.put("rules", ruleGenerator.generateRules(nDevices, hType));
                jAgents.put(agtName, jAgent);
                aCount++;
            }
            jExperiment.put("agents", jAgents);

            ruleCSV = ruleGenerator.getRuleCSV();
            try {
                FileWriter fileOut = new FileWriter(fileName, false); //trick to erase contents of file before starting
                fileOut.write("");
                fileOut.flush();
                fileOut.close();
                fileOut = new FileWriter(fileName, true);
                for(String r : ruleCSV) {
                    fileOut.write(r + "\n");
                }
                fileOut.flush();
                fileOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jExperiment;
    }

    //generate only rules from the list
    public JSONObject generate(ArrayList<Integer> rList, String fileName) {
        // All agents within a cluster share a constraint
        JSONObject jExperiment = new JSONObject();

        try {
            jExperiment.put("horizon", timeHorizon);
            jExperiment.put("priceSchema", priceSchema);

            JSONObject jAgents = new JSONObject();

            int aCount = 0;
            int hType = 0;
            int[] numEachType = numEachHouse();
            for (String agtName : topology.getAgents()) {

                //shifts to the next type of house (small, medium, or large)
                while(aCount == numEachType[hType] && hType < 3) {
                    hType++;
                    aCount = 0;
                }

                JSONObject jAgent = new JSONObject();

                // Create array of neighbors
                JSONArray jNeighbors = new JSONArray();

                for (String neigName : topology.getNeighbors(agtName)) {
                    if (neigName.compareTo(agtName) != 0)
                        jNeighbors.put(neigName);
                }

                jAgent.put("houseType", hType);
                jAgent.put("neighbors", jNeighbors);
                jAgent.put("backgroundLoad", generateBackgroundLoad());
                jAgent.put("rules", ruleGenerator.generateRules(rList.size(), hType, rList));
                jAgents.put(agtName, jAgent);
                aCount++;
            }
            jExperiment.put("agents", jAgents);

            ruleCSV = ruleGenerator.getRuleCSV();
            try {
                FileWriter fileOut = new FileWriter(fileName, false); //trick to erase contents of file before starting
                fileOut.write("");
                fileOut.flush();
                fileOut.close();
                fileOut = new FileWriter(fileName, true);
                for(String r : ruleCSV) {
                    fileOut.write(r + "\n");
                }
                fileOut.flush();
                fileOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jExperiment;
    }

    
    //regenerate the file with the saved rule data from a CSV
    public JSONObject regenerate(String fileName) {
        // All agents within a cluster share a constraint
        JSONObject jExperiment = new JSONObject();
        RuleParser parse = new RuleParser();
        JSONObject jRules = parse.readFile(fileName);

        try {
            jExperiment.put("horizon", timeHorizon);
            jExperiment.put("priceSchema", priceSchema);

            JSONObject jAgents = new JSONObject();

            int aCount = 0;
            int hType = 0;
            int[] numEachType = numEachHouse();
            for (String agtName : topology.getAgents()) {

                //shifts to the next type of house (small, medium, or large)
                while(aCount == numEachType[hType] && hType < 3) {
                    hType++;
                    aCount = 0;
                }

                JSONObject jAgent = new JSONObject();

                // Create array of neighbors
                JSONArray jNeighbors = new JSONArray();

                for (String neigName : topology.getNeighbors(agtName)) {
                    if (neigName.compareTo(agtName) != 0)
                        jNeighbors.put(neigName);
                }

                jAgent.put("houseType", hType);
                jAgent.put("neighbors", jNeighbors);
                jAgent.put("backgroundLoad", generateBackgroundLoad());
                jAgent.put("rules", jRules.getJSONArray(agtName));
                System.out.println("agtName: " + agtName);
                System.out.println(jRules.getJSONArray(agtName).toString(2));
                jAgents.put(agtName, jAgent);
                aCount++;
            }
            jExperiment.put("agents", jAgents);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jExperiment;
    }

}
