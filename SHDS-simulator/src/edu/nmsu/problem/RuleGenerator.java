package edu.nmsu.problem;

import edu.nmsu.RuleParser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by nandofioretto on 11/9/16.
 */
public class RuleGenerator {

    private Random rand = new Random();
    private int horizon;// = Parameters.getHorizon()-1;

    private int time_span; // the total time for a schedule      (in hours)
    private int time_gran; // the granularity for each time step (in minutes)
    private JSONArray houses;
    private int hType;
    private double[][] sensorMaxDelta; // this is used to calculate a good upper bounds for passive rules (for each house type)
    private double[][] sensorMinDelta;
    private double[][] sensorValues;

    private int hID = 1;
    private int rID = 0;
    private ArrayList<String> rule_CSV;
    private RuleParser rp;

    public RuleGenerator(int time_span, int time_gran, JSONArray devices) {

        this.time_span = time_span;
        this.time_gran = time_gran;
        this.horizon   = time_span * 60 / time_gran;

        this.houses = devices;
        sensorMaxDelta = new double[3][14]; // we include 13 sensor properties and 3 house sizes, todo make this dynamic
        sensorMinDelta = new double[3][14];
        sensorValues   = new double[3][14];
        for(double[] dd : sensorMaxDelta) { //setting all values to 0
            for(double d : dd) {
                d = 0;
            }
        }
        for(double[] dd : sensorMinDelta) { //setting all values to 0
            for(double d : dd) {
                d = 0;
            }
        }
        for(double[] dd : sensorValues) {
            for(double d : dd) {
                d = 0;
            }
        }
        findBounds();

        rule_CSV = new ArrayList<>();
        rp = new RuleParser();
    }

    public ArrayList<String> getRuleCSV(){ return rule_CSV; }
/*
    public JSONArray generateRules(int nDevices, int hType) {
        this.hType = hType;
        JSONArray jArray = new JSONArray();
        int cycle = 0;
        int rId = 1;


        // hType (house ID) will work as follows:
        // 1) small, 2) medium, 3) large
        // set dev_table = devices[hType] to set whether this is for small, medium, or large house so rules can be dynamic
        Random rand = new Random();
        ArrayList<Integer> list = new ArrayList<>(8);
        for(int i = 1; i <= 8; i++) {
            list.add(i);
        }
        boolean empty = false;
        while(rId <= nDevices)
        {
            if(empty) {
                list = new ArrayList<>(8);
                for(int i = 0; i <= 8; i++) {
                    list.add(i);
                }
                empty = false;
                cycle++;
            }

            for (String rule : rulePicker(list.remove(rand.nextInt(list.size())), cycle))
                jArray.put(rule);
            rId++;
            if(list.size() == 0) empty = true;

        }

        return jArray;
    }
*/

    public void addCSV(RuleParser.rand_var_id rvID, int value){
        rule_CSV.add(hID+" "+rID+" "+rvID.ordinal()+" "+value);
    }

    public void addCSV(RuleParser.rand_var_id rvID, String value){
        rule_CSV.add(hID+" "+rID+" "+rvID.ordinal()+" "+value);
    }


    public JSONArray generateRules(int nDevices, int hType) {
        this.hType = hType;
        JSONArray jArray = new JSONArray();
        int cycle = 0;
        int rId = 1;

        // hType (house ID) will work as follows:
        // 1) small, 2) medium, 3) large
        // set dev_table = devices[hType] to set whether this is for small, medium, or large house so rules can be dynamic
        Random rand = new Random();
        ArrayList<Integer> list = new ArrayList<>(8);
        for(int i = 1; i <= 8; i++) {
            list.add(i);
        }
        boolean empty = false;
        while(rId <= nDevices) {
            if(empty) {
                list = new ArrayList<>(8);
                for(int i = 0; i <= 8; i++) {
                    list.add(i);
                }
                empty = false;
                cycle++;
            }

            for (String rule : rulePicker(list.remove(rand.nextInt(list.size())), cycle))
                jArray.put(rule);
            rId++;
            if(list.size() == 0) empty = true;
        }

        hID++;
        rID = 0;
        return jArray;
    }

    public JSONArray[] generateSeparatedRules(int nDevices, int hType) {
        this.hType = hType;

        JSONArray[] jArrays = new JSONArray[3];
        for(int i = 0; i < jArrays.length; i++) {
            jArrays[i] = new JSONArray();
        }

        int cycle = 0;
        int rId = 1;

        // hType (house ID) will work as follows:
        // 1) small, 2) medium, 3) large
        // set dev_table = devices[hType] to set whether this is for small, medium, or large house so rules can be dynamic
        Random rand = new Random();
        ArrayList<Integer> list = new ArrayList<>(8);
        for(int i = 1; i <= 8; i++) {
            list.add(i);
        }
        boolean empty = false;
        while(rId <= nDevices) {
            if(empty) {
                list = new ArrayList<>(8);
                for(int i = 0; i <= 8; i++) {
                    list.add(i);
                }
                empty = false;
                cycle++;
            }

            int rPick = list.remove(rand.nextInt(list.size()));
            int index;
            if(rPick == 1 || rPick == 7) {
                index = 1;
            } else if(rPick == 2 || rPick == 8) {
                index = 2;
            } else {
                index = 0;
            }
            for (String rule : rulePicker(rPick, cycle))
                jArrays[index].put(rule);
            rId++;
            if(list.size() == 0) empty = true;
        }

        hID++;
        rID = 0;
        return jArrays;
    }


    public JSONArray generateRules(int nDevices, int hType, ArrayList<Integer> rList) {
        this.hType = hType;
        JSONArray jArray = new JSONArray();
        int cycle = 0;
        int rId = 1;


        // hType (house ID) will work as follows:
        // 1) small, 2) medium, 3) large
        // set dev_table = devices[hType] to set whether this is for small, medium, or large house so rules can be dynamic
        Random rand = new Random();
        ArrayList<Integer> list = new ArrayList<>(rList.size());
        for(int i = 0; i < rList.size(); i++) {
            list.add(rList.get(i));
        }
        boolean empty = false;
        while(rId <= nDevices)
        {
            if(empty) {
                list = new ArrayList<>(rList.size());
                for(int i = 0; i < rList.size(); i++) {
                    list.add(rList.get(i));
                }
                empty = false;
                cycle++;
            }

            for (String rule : rulePicker(list.remove(rand.nextInt(list.size())), cycle))
                jArray.put(rule);
            rId++;
            if(list.size() == 0) empty = true;

        }

        return jArray;
    }

    private ArrayList<String> rulePicker(int index, int cycle){
        rID = index;
        switch(index) {
            case 1:
                return generateLaundryWashRules(cycle);
            case 2:
                return generateLaundryDryRules(cycle);
            case 3:
                return generateDishWashRules(cycle);
            case 4:
                return generateBakeRules(cycle);
            case 5:
                return generateWaterTempRules(cycle);
            case 6:
                return generateEVRules(cycle);
            case 7:
                return generateTempHeatRules(cycle);
            case 8:
                return generateCleanlinessRules(cycle);
        }
        return new ArrayList<>();
    }

    /**Returns a random time predicate with spacing around it.*/
    private String randPredicate() {
        return Utilities.genRand(new String[]{"before", "after", "at", "within"});
    }

    /**Generates active rules given some parameters*/
    private String activeGen(String device, String property, String relation, int state, String predicate) {
        String rule = "1 " + device + " " + property + " " + relation + " " + state;
        int time1; int time2;

        int minTime = minTimeToComplete(getSensorID(property), relation, state) == -1 ? 2 : minTimeToComplete(getSensorID(property), relation, state); //todo remove need for this 'bandaid'
        addCSV(RuleParser.rand_var_id.RELATION,   relation);
        addCSV(RuleParser.rand_var_id.STATE,         state);
        addCSV(RuleParser.rand_var_id.PREDICATE, predicate);
        switch(predicate) {
            case "before":
                time1 = Utilities.genRand(minTime + 2, horizon-1);
                addCSV(RuleParser.rand_var_id.TIME1, time1);
                return rule + " before " + time1;
            case "after":
                time1 = Utilities.genRand(1, horizon - minTime - 2);
                addCSV(RuleParser.rand_var_id.TIME1, time1);
                return rule +  " after " + time1;
            case "at":
                time1 = Utilities.genRand(minTime + 2, horizon-1);
                addCSV(RuleParser.rand_var_id.TIME1, time1);
                return rule +     " at " + time1;
            case "within":
                do {
                    time1 = Utilities.genRand(minTime + 2, horizon-1);
                    time2 = Utilities.genRand(minTime + 2, horizon-1);
                } while(time1 == time2);
                addCSV(RuleParser.rand_var_id.TIME1, time1);
                addCSV(RuleParser.rand_var_id.TIME2, time2);
                return rule + " within " + Math.min(time1, time2) + " " + Math.max(time1, time2);
        }
        return null;
    }

    private String[] activeGen(String device, String property, int state1, int state2, String predicate) { //this version is used to keep values between two values for a number of timesteps
        String rule1 = "1 " + device + " " + property + " ";
        String rule2 = rule1;
        int time1; int time2;
        switch(predicate) {
            case "before":
                time1 = Utilities.genRand(minTimeToComplete(getSensorID(property), "geq", state1) + 1, horizon);
                rule1 += "geq" + state1 + " before " + time1;
                rule2 += "leq" + state2 + " before " + time1;
                return new String[]{rule1, rule2};
            case "after":
                time1 = Utilities.genRand(1, horizon - minTimeToComplete(getSensorID(property), "geq", state1) - 1);
                rule1 += "geq" + state1 + " after " + time1;
                rule2 += "leq" + state2 + " after " + time1;
                return new String[]{rule1, rule2};
            case "at":
                time1 = Utilities.genRand(minTimeToComplete(getSensorID(property), "geq", state1), horizon);
                rule1 += "geq" + state1 + " at " + time1;
                rule2 += "leq" + state2 + " at " + time1;
                return new String[]{rule1, rule2};
            case "within":
                do {
                    time1 = Utilities.genRand(minTimeToComplete(getSensorID(property), "geq", state1), horizon);
                    time2 = Utilities.genRand(minTimeToComplete(getSensorID(property), "geq", state1), horizon);
                } while(time1 == time2);
                rule1 += "geq" + state1 + " within " + Math.min(time1, time2) + " " + Math.max(time1, time2);
                rule2 += "leq" + state2 + " within " + Math.min(time1, time2) + " " + Math.max(time1, time2);
                return new String[]{rule1, rule2};
        }
        return null;
    }

    private ArrayList<String> generateLaundryWashRules(int i) {
        ArrayList<String> rules = new ArrayList<>();
        String device = "LG_WM2016CW" + ((i>0) ? ("_" + i) : "");
        String property = "laundry_wash";

        int state = Utilities.genRand(new int[]{45, 60}); // goal state
        String relation = "geq";

        rules.add(activeGen(device, property, relation, state, randPredicate()));
        rules.add("0 " + device + " " + property + " geq 0");
        rules.add("0 " + device + " " + property + " leq 60");
        return rules;
    }

    private ArrayList<String> generateLaundryDryRules(int i) {
        ArrayList<String> rules = new ArrayList<>();
        String device = "GE_WSM2420D3WW" + ((i>0) ? ("_" + i) : "");
        String property = "laundry_dry";

        int state = Utilities.genRand(new int[]{45, 60});
        String relation = "geq";

        rules.add(activeGen(device, property, relation, state, randPredicate()));
        rules.add("0 " + device + " " + property + " geq 0");
        rules.add("0 " + device + " " + property + " leq 60");
        return rules;
    }

    private ArrayList<String> generateDishWashRules(int i) {
        ArrayList<String> rules = new ArrayList<>();
        String device = "Kenmore_665.13242K900" + ((i>0) ? ("_" + i) : "");
        String property = "dish_wash";

        int state = Utilities.genRand(new int[]{45, 60});
        String relation = "geq";

        rules.add(activeGen(device, property, relation, state, randPredicate()));
        rules.add("0 " + device + " " + property + " geq 0");
        rules.add("0 " + device + " " + property + " leq 60");
        return rules;
    }

    private ArrayList<String> generateBakeRules(int i) {
        ArrayList<String> rules = new ArrayList<>();
        String device = "Kenmore_790.91312013" + ((i>0) ? ("_" + i) : "");
        String property = "bake";

        int state = Utilities.genRand(new int[]{60, 75, 120, 150});
        String relation = "eq";

        rules.add(activeGen(device, property, relation, state, randPredicate()));
        rules.add("0 " + device + " " + property + " geq 0");
        rules.add("0 " + device + " " + property + " leq " + state);
        return rules;
    }

    private ArrayList<String> generateWaterTempRules(int i) {
        ArrayList<String> rules = new ArrayList<>();
        String device = "water_tank" + ((i>0) ? ("_" + i) : "");
        String property = "water_temp";

        int state = Utilities.genRand(15, 40);
        String relation = Utilities.genRand(new String[]{"geq", "gt"});

        //int state1 = Utilities.genRand(10+(int)sensorMinDelta[hType][getSensorID(property)], 40 - (int)sensorMaxDelta[hType][getSensorID(property)]);
        //int state2 = Utilities.genRand(state1+(int)sensorMaxDelta[hType][getSensorID(property)], 42);
        //String[] temp_rules = activeGen(device, property, state1, state2, randPredicate()); // an upper and lower temperature preference
        //rules.add(temp_rules[0]); rules.add(temp_rules[1]);

        rules.add(activeGen(device, property, relation, state, randPredicate()));
        rules.add("0 " + device + " " + property + " geq 10");
        rules.add("0 " + device + " " + property + " leq 55");
        return rules;
    }

    private ArrayList<String> generateEVRules(int i) {
        ArrayList<String> rules = new ArrayList<>();
        String device = "Tesla_S" + ((i>0) ? ("_" + i) : "");
        String property = "charge";

        int state = Utilities.genRand(50, 65);
        String relation = Utilities.genRand(new String[]{"geq", "gt"});
        String timePred = Utilities.genRand(new String[]{"before", "after", "at"});
        int time1, time2;
        String rule = "1 " + device + " " + property + " " + relation + " " + state;
        double start_state = 10; double delta = 10.2;
        int minTime = (int) ((state - start_state) / delta) + 1;
        switch(timePred) {
            case "before":
                time1 = Utilities.genRand(minTime + 2, horizon-1);
                rule += " before " + time1;
                break;
            case "after":
                time1 = Utilities.genRand(1, horizon - minTime - 2);
                rule +=  " after " + time1;
                break;
            case "at":
                time1 = Utilities.genRand(minTime + 2, horizon-1);
                rule +=     " at " + time1;
                break;
            case "within":
                do {
                    time1 = Utilities.genRand(minTime + 2, horizon-1);
                    time2 = Utilities.genRand(minTime + 2, horizon-1);
                } while(time1 == time2);
                rule += " within " + Math.min(time1, time2) + " " + Math.max(time1, time2);
                break;
        }
        rules.add(rule);
        addCSV(RuleParser.rand_var_id.RELATION,   relation);
        addCSV(RuleParser.rand_var_id.STATE,         state);
        addCSV(RuleParser.rand_var_id.PREDICATE,  timePred);
        rules.add("0 " + device + " " + property + " geq 0");
        rules.add("0 " + device + " " + property + " leq 100");
        return rules;
    }

    private ArrayList<String> generateTempHeatRules(int i) {
        ArrayList<String> rules = new ArrayList<>();
        String device = "room" + ((i>0) ? ("_" + i) : "");
        String property = "temperature_heat";

        int state = Utilities.genRand(17, 24);
        String relation = Utilities.genRand(new String[]{"geq", "gt"});

        rules.add(activeGen(device, property, relation, state, randPredicate()));
        rules.add("0 " + device + " " + property + " geq 0");
        rules.add("0 " + device + " " + property + " leq 33");
        return rules;
    }

    private ArrayList<String> generateCleanlinessRules(int i) {
        ArrayList<String> rules = new ArrayList<>();
        String device = "room" + ((i>0) ? ("_" + i) : "");
        String property = "cleanliness";

        int state = Utilities.genRand(50, 80);
        String relation = Utilities.genRand(new String[]{"geq", "gt"});

        rules.add(activeGen(device, property, relation, state, randPredicate()));
        rules.add("0 " + device + " " + property + " geq 0");
        rules.add("0 " + device + " " + property + " leq 100");
        //rules.add("0 iRobot_651" + ((i>0) ? ("_" + i) : "") + " charge geq 0");
        //rules.add("0 iRobot_651" + ((i>0) ? ("_" + i) : "") + " charge leq 100");
        return rules;
    }

    //TODO: make this better
    private void findBounds() {
        for(int i = 0; i < houses.length(); i++) {
            JSONObject devices = houses.getJSONObject(i);
            Iterator<?> d_names = devices.keys();
            while (d_names.hasNext()) {
                String d_n = (String) d_names.next();
                if (devices.getJSONObject(d_n).getString("type").equals("actuator")) { // TODO split Device dictionary into list of actuators and list of sensors
                    JSONObject actions = devices.getJSONObject(d_n).getJSONObject("actions");
                    Iterator<?> keys = actions.keys();
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        JSONArray effects = actions.getJSONObject(key).getJSONArray("effects");
                        for (int j = 0; j < effects.length(); j++) {
                            JSONObject e = effects.getJSONObject(j);
                            int sID = getSensorID(e.getString("property"));
                            sensorMaxDelta[i][sID] = Math.max(sensorMaxDelta[i][sID], e.getDouble("delta"));
                            sensorMinDelta[i][sID] = Math.min(sensorMinDelta[i][sID], e.getDouble("delta"));
                        }
                    }
                } else { // if it's a sensor, put value on list of sensor values
                    JSONArray props = devices.getJSONObject(d_n).getJSONArray("sensing_properties");
                    for(int j = 0; j < props.length(); j++) {
                        int sID = getSensorID(props.getString(j)); //TODO: multiple values for the potentially different 'sensing properties'
                        sensorValues[i][sID] = devices.getJSONObject(d_n).getDouble("current_state");
                    }
                }
            }
        }
    }

    private int getSensorID(String sp) {
        // FOR NOW, since we use two separate sensor properties for air_temperature, I'm going to make temperature_cool be 0 until we decide if it is staying.
        /*
        01 air temperature
        02 ﬂoor cleanliness (dust)
        03 temperature
        04 battery charge
        05 bake
        06 laundry wash
        07 laundry dry
        08 dish cleanliness
        09 air humidity
        10 luminosity
        11 occupancy
        12 movement
        13 smoke detector
        */
        switch(sp) {
            case "temperature_cool":    return  0;
            case "temperature_heat":    return  1;
            case "cleanliness":         return  2;
            case "water_temp":          return  3;
            case "charge":              return  4;
            case "bake":                return  5;
            case "laundry_wash":        return  6;
            case "laundry_dry":         return  7;
            case "dish_wash":           return  8;
            case "air_humidity":        return  9;
            case "luminosity":          return 10;
            case "occupancy":           return 11;
            case "movement":            return 12;
            case "smoke_detector":      return 13;
            default:                    return -1;
        }
    }

    private int minTimeToComplete(int sID, double goal) { // minimum time needed to complete an objective with only the device that affects a property the most
        int time = 0;
        double prog = 0;
        while(prog < goal){ prog += sensorMaxDelta[hType][sID]; time++;}
        return time;
    }


    private int minTimeToComplete(int sID, String relation, double goal) { // minimum time needed to complete an objective with only the device that affects a property the most
        int time = 0;
        double prog = sensorValues[hType][sID];
        switch(relation) {
            case "lt":
                while(prog >= goal) {
                    prog += sensorMinDelta[hType][sID];
                    time++;
                    if(time > horizon) return -1;
                }
                return time;
            case "leq":
                while(prog >  goal) {
                    prog += sensorMinDelta[hType][sID];
                    time++;
                    if(time > horizon) return -1;
                }
                return time;
            case "eq":
                while(prog < goal) {
                    prog += sensorMaxDelta[hType][sID];
                    time++;
                    if(time > horizon) {time = 0; prog = sensorValues[hType][sID]; break;}
                }
                while(prog > goal) {
                    prog += sensorMinDelta[hType][sID];
                    time++;
                    if(time > horizon) return -1;
                }
                return time;
            case "geq":
                while(prog < goal) {
                    prog += sensorMaxDelta[hType][sID];
                    time++;
                    if(time > horizon) return -1;
                }
                return time;
            case "gt":
                while(prog <= goal) {
                    prog += sensorMaxDelta[hType][sID];
                    time++;
                    if(time > horizon) return -1;
                }
                return time;
        }
        return -2;
    }
}
