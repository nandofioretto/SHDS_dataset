package edu.nmsu;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by wklue_000 on 3/26/2017.
 */
public class RuleParser {
    public RuleParser() {

    }

    public enum rand_var_id{
        RELATION,
        STATE,
        PREDICATE,
        TIME1,
        TIME2
    }
    /*
    String[] random_var_id = {
            //0
            //1
            //2
            //3
            //4
            //5
            //6
            //7
            //8
            //9
            //10
            //11
            //12
            //13
            //14
            //15
            //16
            //17
            //18
            //19
            //20
    };*/

    public JSONObject readFile(String fileName){
        try {
            Scanner in = new Scanner(new File(fileName));
            JSONObject houses = new JSONObject();
            String r = in.nextLine();
            String[] elements = r.split(" ");
            String hIndex = elements[0];
            String rIndex = elements[1];
            ArrayList<String> currHouse = new ArrayList<>();
            ArrayList<String> currRule  = new ArrayList<>();
            currRule.add(r);
            while (in.hasNextLine()) {
                r = in.nextLine();
                elements = r.split(" ");
                String h = elements[0];
                if(!hIndex.equals(h)){
                    JSONArray jRules = new JSONArray();
                    for(int i = 0; i < currHouse.size(); i++) {
                        jRules.put(currHouse.get(i));
                    }
                    houses.put("h"+h, jRules);
                    hIndex = h;
                    currHouse = new ArrayList<>();
                }

                if(elements[1].equals(rIndex)) {
                    currRule.add(r);
                } else {
                    currHouse.addAll(parseRule(Integer.parseInt(rIndex), currRule));
                    currRule = new ArrayList<>();
                    rIndex = elements[1];
                }
            }

            return houses;
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<String> parseRule(int rID, ArrayList<String> list) {
        String rule = "1";
        rule += " " + getLocation(rID) + " " + getProperty(rID);
        int state = 0;
        for(String s : list) {
            String[] elements = s.split(" ");
            rule += " " + elements[3];
            if(Integer.parseInt(elements[2]) == rand_var_id.STATE.ordinal()) state = Integer.parseInt(elements[3]);
        }

        ArrayList<String> all = new ArrayList<>();
        all.add(rule);
        all.addAll(getPassiveRules(rID, state));

        return all;
    }

    public String getLocation(int loc) {
        switch(loc) {
            case 1:
                return "LG_WM2016CW";
            case 2:
                return "GE_WSM2420D3WW";
            case 3:
                return "Kenmore_665.13242K900";
            case 4:
                return "Kenmore_790.91312013";
            case 5:
                return "water_tank";
            case 6:
                return "Tesla_S";
            case 7:
                return "room";
            case 8:
                return "room";
        }
        return "";
    }

    public String getProperty(int prop) {
        switch(prop) {
            case 1:
                return "laundry_wash";
            case 2:
                return "laundry_dry";
            case 3:
                return "dish_wash";
            case 4:
                return "bake";
            case 5:
                return "water_temp";
            case 6:
                return "charge";
            case 7:
                return "temperature_heat";
            case 8:
                return "cleanliness";
        }
        return "";
    }

    public ArrayList<String> getPassiveRules(int index, int state) {
        ArrayList<String> passive = new ArrayList<>();
        String device   = getLocation(index);
        String property = getProperty(index);
        switch(index) {
            case 1:
                passive.add("0 " + device + " " + property + " geq 0");
                passive.add("0 " + device + " " + property + " leq 60");
                break;
            case 2:
                passive.add("0 " + device + " " + property + " geq 0");
                passive.add("0 " + device + " " + property + " leq 60");
                break;
            case 3:
                passive.add("0 " + device + " " + property + " geq 0");
                passive.add("0 " + device + " " + property + " leq 60");
                break;
            case 4:
                passive.add("0 " + device + " " + property + " geq 0");
                passive.add("0 " + device + " " + property + " leq " + state);
                break;
            case 5:
                passive.add("0 " + device + " " + property + " geq 10");
                passive.add("0 " + device + " " + property + " leq 50");
                break;
            case 6:
                passive.add("0 " + device + " " + property + " geq 0");
                passive.add("0 " + device + " " + property + " leq 100");
                break;
            case 7:
                passive.add("0 " + device + " " + property + " geq 0");
                passive.add("0 " + device + " " + property + " leq 33");
                break;
            case 8:
                passive.add("0 " + device + " " + property + " geq 0");
                passive.add("0 " + device + " " + property + " leq 100");
                break;
        }
        return passive;
    }
}
