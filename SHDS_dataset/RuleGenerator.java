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
        sensorMaxDelta = new double[3][14]; // we include 14 sensor properties and 3 house sizes, todo make this dynamic
        sensorMinDelta = new double[3][14];
        sensorValues   = new double[3][14];

        for(int h = 0; h < 3; h++) { // for each of the 3 house sizes
            for(int sp = 0; sp < 14; sp++){ //for each sensor property
                sensorMaxDelta[h][sp] = 0;
                sensorMinDelta[h][sp] = 0;
                sensorValues[h][sp] = 0;
            }
        }
        findBounds();

        rule_CSV = new ArrayList<>();
        rp = new RuleParser();
    }

    public ArrayList<String> getRuleCSV(){ return rule_CSV; }
    public int getSpan() {
        return time_span;
    }
    public int getGran() {
        return time_gran;
    }

    public void addCSV(RuleParser.rand_var_id rvID, int value){
        rule_CSV.add(hID+" "+rID+" "+rvID.ordinal()+" "+value);
    }

    public void addCSV(RuleParser.rand_var_id rvID, String value){
        rule_CSV.add(hID+" "+rID+" "+rvID.ordinal()+" "+value);
    }

    public void addBG(double[] bgLoads){
        for(double d : bgLoads) {
            rule_CSV.add(hID+" "+-1+" "+d);
            //System.out.println(hID+" "+-1+" "+d);
        }
    }

    public void addProperties(int horizon, int granularity, int agents, int clusters) {
        rule_CSV.add(horizon+" "+granularity+" "+agents+" "+clusters);
    }

    public void addHouseType(int houseType){
        rule_CSV.add(hID+" "+-2+" "+houseType);
    }

    public JSONArray generateRules(int nDevices, int hType, String season) {
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
                for(int i = 1; i <= 8; i++) {
                    list.add(i);
                }
                empty = false;
                cycle++;
            }

            for (String rule : rulePicker(list.remove(rand.nextInt(list.size())), cycle, season))
                jArray.put(rule);
            rId++;
            if(list.size() == 0) empty = true;
        }

        hID++;
        rID = 0;
        return jArray;
    }

    public JSONArray[] generateSeparatedRules(int nDevices, int hType, String season) {
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
                for(int i = 1; i <= 8; i++) {
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
            for (String rule : rulePicker(rPick, cycle, season))
                jArrays[index].put(rule);
            rId++;
            if(list.size() == 0) empty = true;
        }

        hID++;
        rID = 0;
        return jArrays;
    }

    public JSONArray generateRules(int nDevices, int hType, ArrayList<Integer> rList, String season) {
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

            for (String rule : rulePicker(list.remove(rand.nextInt(list.size())), cycle, season))
                jArray.put(rule);
            rId++;
            if(list.size() == 0) empty = true;

        }

        return jArray;
    }

    public JSONArray generateRulesHVAC(int nDevices, int hType, String season) {
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
        // places HVAC into system
        for(String rule : rulePicker(list.remove(6), cycle, season)) {
            jArray.put(rule);
        }
        rId++;
        while(rId <= nDevices) {
            if(empty) {
                list = new ArrayList<>(8);
                for(int i = 1; i <= 8; i++) {
                    list.add(i);
                }
                empty = false;
                cycle++;
            }

            for (String rule : rulePicker(list.remove(rand.nextInt(list.size())), cycle, season))
                jArray.put(rule);
            rId++;
            if(list.size() == 0) empty = true;
        }

        hID++;
        rID = 0;
        return jArray;
    }

    // returns a list of rules for the given device index
    private ArrayList<String> rulePicker(int index, int cycle, String season){
        rID = index;
        ArrayList<String> rules = new ArrayList<>();
        String device = getRuleLocation(index, cycle);
        String property = getRuleProp(index, season);
        int state;
        String relation;
        String predicate;

        switch(index) {
            // Washer
            case 1:
                relation = "eq";
                state = Utilities.genRand(new int[]{60}); // goal state
                predicate = Utilities.genRand(new String[]{"before", "after", "at"});
                break;

            // Dryer
            case 2:
                relation = "eq";
                state = Utilities.genRand(new int[]{60}); // goal state
                predicate = Utilities.genRand(new String[]{"before", "after", "at"});
                break;

            // Dishwasher
            case 3:
                relation = "eq";
                state = Utilities.genRand(new int[]{60}); // goal state
                predicate = Utilities.genRand(new String[]{"before", "after", "at"});
                break;

            // Oven
            case 4:
                state = Utilities.genRand(new int[]{60, 75, 120, 150});
                relation = "eq";
                predicate = Utilities.genRand(new String[]{"before", "after", "at"});
                break;

            // Water Heater
            case 5:
                state = Utilities.genRand(50, 65);
                relation = Utilities.genRand(new String[]{"lt", "leq", "geq", "gt"});
                predicate = Utilities.genRand(new String[]{"before", "after"});
                break;

            // Electric Vehicle
            case 6:
                state = Utilities.genRand(50, 80);
                relation = Utilities.genRand(new String[]{"lt", "leq", "geq", "gt"});
                predicate = Utilities.genRand(new String[]{"before", "after"});
                break;

            // Heater
            case 7:
                if(season.equals("summer")) {
                    state = Utilities.genRand(70, 74);
                    relation = Utilities.genRand(new String[]{"lt", "leq"});
                } else {
                    state = Utilities.genRand(70, 74);
                    relation = Utilities.genRand(new String[]{"geq", "gt"});
                }
                predicate = randPredicate();
                break;

            // Vacuum Robot
            case 8:
                state = Utilities.genRand(50, 80);
                relation = Utilities.genRand(new String[]{"geq", "gt"});
                predicate = Utilities.genRand(new String[]{"before", "after"});
                break;

            // If device index is out of bounds
            default:
                System.err.println("Error: Device index out of bounds (during rule generation).");
                device = "NULL";
                property = "NULL";
                relation = "NULL";
                state = -1;
                predicate = "NULL";
        }

        rules.add(activeGen(device, property, relation, state, predicate));
        rules.addAll(getPassiveRules(index, cycle, state, season));
        return rules;
    }


    public static ArrayList<String> getPassiveRules(int index, int cycle, int state, String season){
        ArrayList<String> rules = new ArrayList<>();
        String device = getRuleLocation(index, cycle);
        String property = getRuleProp(index, season);
        switch (index){
            case 1:
                rules.add("0 " + device + " " + property + " geq 0");
                rules.add("0 " + device + " " + property + " leq " + state);
                break;
            case 2:
                rules.add("0 " + device + " " + property + " geq 0");
                rules.add("0 " + device + " " + property + " leq " + state);
                break;
            case 3:
                rules.add("0 " + device + " " + property + " geq 0");
                rules.add("0 " + device + " " + property + " leq " + state);
                break;
            case 4:
                rules.add("0 " + device + " " + property + " geq 0");
                rules.add("0 " + device + " " + property + " leq " + state);
                break;
            case 5:
                rules.add("0 " + device + " " + property + " geq 37");
                rules.add("0 " + device + " " + property + " leq 78");
                break;
            case 6:
                rules.add("0 " + device + " " + property + " geq 0");
                rules.add("0 " + device + " " + property + " leq 100");
                break;
            case 7:
                if(season.equals("summer")){
                    rules.add("0 " + device + " " + property + " geq 48");
                    rules.add("0 " + device + " " + property + " leq 80");
                } else {
                    rules.add("0 " + device + " " + property + " geq 65");
                    rules.add("0 " + device + " " + property + " leq 85");
                }
                break;
            case 8:
                rules.add("0 " + device + " " + property + " geq 0");
                rules.add("0 " + device + " " + property + " leq 100");
                rules.add("0 Roomba_880" + ((cycle>0) ? ("_" + cycle) : "") + " charge geq 0");
                rules.add("0 Roomba_880" + ((cycle>0) ? ("_" + cycle) : "") + " charge leq 100");
                break;
        }
        return rules;
    }

    public static ArrayList<String> getPassiveRules(int index, int cycle, int state){
        ArrayList<String> rules = new ArrayList<>();
        String device = getRuleLocation(index, cycle);
        String property = getRuleProp(index, "summer");
        switch (index){
            case 1:
                rules.add("0 " + device + " " + property + " geq 0");
                rules.add("0 " + device + " " + property + " leq " + state);
                break;
            case 2:
                rules.add("0 " + device + " " + property + " geq 0");
                rules.add("0 " + device + " " + property + " leq " + state);
                break;
            case 3:
                rules.add("0 " + device + " " + property + " geq 0");
                rules.add("0 " + device + " " + property + " leq " + state);
                break;
            case 4:
                rules.add("0 " + device + " " + property + " geq 0");
                rules.add("0 " + device + " " + property + " leq " + state);
                break;
            case 5:
                rules.add("0 " + device + " " + property + " geq 37");
                rules.add("0 " + device + " " + property + " leq 78");
                break;
            case 6:
                rules.add("0 " + device + " " + property + " geq 0");
                rules.add("0 " + device + " " + property + " leq 100");
                break;
            case 7:
                if(property.equals("summer")){
                    rules.add("0 " + device + " " + property + " geq 52");
                    rules.add("0 " + device + " " + property + " leq 96");
                } else {
                    rules.add("0 " + device + " " + property + " geq 65");
                    rules.add("0 " + device + " " + property + " leq 80");
                }
                break;
            case 8:
                rules.add("0 " + device + " " + property + " geq 0");
                rules.add("0 " + device + " " + property + " leq 100");
                rules.add("0 Roomba_880" + ((cycle>0) ? ("_" + cycle) : "") + " charge geq 0");
                rules.add("0 Roomba_880" + ((cycle>0) ? ("_" + cycle) : "") + " charge leq 100");
                break;
        }
        return rules;
    }



    /**Returns a random time predicate with spacing around it.*/
    private String randPredicate() {
        return Utilities.genRand(new String[]{"before", "after"});//, "at", "within"});
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
                    time1 = Utilities.genRand(minTime + 2, Math.min(minTime + 5, horizon-1));
                    time2 = Utilities.genRand(minTime + 2, Math.min(minTime + 5, horizon-1));
                } while(time1 == time2);
                addCSV(RuleParser.rand_var_id.TIME1, Math.min(time1, time2));
                addCSV(RuleParser.rand_var_id.TIME2, Math.max(time1, time2));
                return rule + " within " + Math.min(time1, time2) + " " + Math.max(time1, time2);
        }
        return null;
    }

    //TODO: add locations (i.e. there are two charge sensor properties, one on the Roomba and one on the Tesla_S
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
                            if(sID == 4 && !d_n.equals("Tesla_S")) { // quick fix for EV charge, we want it to be considered the 'max' delta for charge since the roomba also has a charge SP
                                break;
                            }
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
        // FOR NOW, since we use two separate sensor properties for air_temperature
        // (to create a logical separation between the cooling and heating unit since it wouldn't make sense to use both in the same model)
        /*
        00 air temperature cool
        01 air temperature heat
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

    public static String getRuleProp(int index, String season) {
        switch(index) {
            case 1: return  "laundry_wash";
            case 2: return  "laundry_dry";
            case 3: return  "dish_wash";
            case 4: return  "bake";
            case 5: return  "water_temp";
            case 6: return  "charge";
            case 7:
                if(season.equals("summer"))
                    return "temperature_cool";
                else
                    return  "temperature_heat";
            case 8: return  "cleanliness";
            default: return "NULL";
        }
    }

    public static String getRuleLocation(int index, int cycle) {
        String loc;
        switch(index) {
            case 1:
                loc = "GE_WSM2420D3WW_wash";
                break;
            case 2:
                loc = "GE_WSM2420D3WW_dry";
                break;
            case 3:
                loc = "Kenmore_665.13242K900";
                break;
            case 4:
                loc = "Kenmore_790.91312013";
                break;
            case 5:
                loc = "water_tank";
                break;
            case 6:
                loc = "Tesla_S";
                break;
            case 7:
                loc = "room";
                break;
            case 8:
                loc = "room";
                break;
            default:
                loc = "NULL";
        }
        return loc  + ((cycle>0) ? ("_" + cycle) : "");
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
                while(prog > goal) {
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
