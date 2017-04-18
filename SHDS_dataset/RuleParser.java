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

    public JSONArray readFile(String fileName){
        try {
            Scanner in = new Scanner(new File(fileName));
            JSONObject houses = new JSONObject();
            JSONObject bgList = new JSONObject();
            ArrayList<Double> bgLoads = new ArrayList<>();
            String r = in.nextLine();
            String[] elements = r.split(" ");
            String hIndex = elements[0];
            String rIndex = elements[1];
            ArrayList<String> currHouse = new ArrayList<>();
            ArrayList<String> currRule  = new ArrayList<>();
            
            do {
                String h = elements[0];
                
                if(elements[1].equals("-1")) { //background loads
                    bgLoads.add(Double.parseDouble(elements[2]));
                } else {
                    if(rIndex.equals("-1")) rIndex = elements[1];
                    if(bgLoads.size() != 0) {
                        double[] bgTemp = new double[bgLoads.size()];
                        for(int i = 0; i < bgLoads.size(); i++) {
                            bgTemp[i] = bgLoads.get(i);
                            System.out.println(hIndex + " " + bgTemp[i]);
                        }
                        bgList.put("h"+h, bgTemp);
                        System.out.println(bgTemp.length);
                        bgLoads = new ArrayList<>();
                    }
                    String r_part = elements[2];
                    
                    if(r_part.equals("0") && !rIndex.equals(elements[1])) {
                        //System.out.println("\trI = "+rIndex+"\tr = " + elements[1]);
                        currHouse.addAll(parseRule(Integer.parseInt(rIndex), currRule));
                        currRule = new ArrayList<>();
                        rIndex = elements[1];
                    }
                    
                    if(!hIndex.equals(h)) {
                        //System.out.println("hI = "+hIndex+"\th = " + h);
                        JSONArray jRules = new JSONArray();
                        for(int i = 0; i < currHouse.size(); i++) {
                            jRules.put(currHouse.get(i));
                        }
                        houses.put("h"+hIndex, jRules);
                        hIndex = h;
                        currHouse = new ArrayList<>();
                    }
                    currRule.add(r);
                }
                r = in.nextLine();
                elements = r.split(" ");
            } while(in.hasNextLine());
            //System.out.println(houses.toString(2));
            //for the last house
            /*
            double[] bgTemp = new double[bgLoads.size()];
            for(int i = 0; i < bgLoads.size(); i++) {
                bgTemp[i] = bgLoads.get(i);
            }
            bgList.put("h"+hIndex, bgTemp);*/
            currRule.add(r);
            currHouse.addAll(parseRule(Integer.parseInt(rIndex), currRule));
            JSONArray jRules = new JSONArray();
            for(int i = 0; i < currHouse.size(); i++) {
                jRules.put(currHouse.get(i));
            }
            houses.put("h"+hIndex, jRules);
            JSONArray info = new JSONArray();
            info.put(houses); info.put(bgList);
            return info;
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
                passive.add("0 " + device + " " + property + " leq 55");
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
