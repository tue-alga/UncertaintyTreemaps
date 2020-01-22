/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JsonConverter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Takes the input JSON file and converts it to a csv file
 *
 * @author msondag
 */
public class JsonConverter {
    
    File inputFile;
    File outputFile;
    boolean hasSd = true;
    
    public static void main(String[] args) {
        try {
            JsonConverter converter = new JsonConverter(args);
            converter.convert();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JsonConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public JsonConverter(String[] args) {
        parseArguments(args);
    }
    
    private void parseArguments(String args[]) {
        List<String> argumentList = Arrays.asList(args);
        ListIterator<String> it = argumentList.listIterator();
        while (it.hasNext()) {
            String arg = it.next();
            System.out.println("arg = " + arg);
            switch (arg) {
                case "-sd":
                    hasSd = Boolean.parseBoolean(it.next());
                    System.out.println("hasSd = " + hasSd);
                    break;
                case "-inputFile":
                    inputFile = new File(it.next());
                    System.out.println("inputFolder = " + inputFile);
                    break;
                case "-outputFile":
                    outputFile = new File(it.next());
                    System.out.println("outputFolder = " + outputFile);
                    break;
            }
        }
    }
    
    public class TreeMapData {
        
        public String id;
        public double size;
        public double sd;
        public String parent;
        
        public TreeMapData(String id, double size, double sd, String parent) {
            this.id = id;
            this.size = size;
            this.sd = sd;
            this.parent = parent;
        }
        
        @Override
        public String toString() {
            return id + "," + parent + "," + size + "," + sd;
        }
    }
    
    private void convert() throws FileNotFoundException {
        JsonObject obj = readJsonFile();
        System.out.println("obj = " + obj);
        
        String parent = "root";
        
        List<TreeMapData> data = convertData(parent, obj);
        for (TreeMapData tmd : data) {
            System.out.println("tmd = " + tmd);
        }
    }
    
    private List<TreeMapData> convertData(String parent, JsonObject currentData) {
        List<TreeMapData> dataList = new ArrayList();

        //get the data from the current object.
        String title = currentData.get("title").getAsString();
        title = title.replaceAll(",", ";");
        double sd = currentData.get("se").getAsDouble();
        double size = currentData.get("size").getAsDouble();
 
        
        TreeMapData tmd = new TreeMapData(title, size, sd, parent);
        dataList.add(tmd);
        //recurse in the children
        JsonArray children = currentData.getAsJsonArray("children");
        if (children != null) {
            for (JsonElement child : children) {
                dataList.addAll(convertData(title, (JsonObject) child));
            }
        }
        return dataList;
    }
    
    private JsonObject readJsonFile() throws FileNotFoundException {
        Gson gson = new Gson();
        System.out.println("file = " + inputFile.getAbsolutePath());
        JsonReader reader = new JsonReader(new FileReader(inputFile));
        JsonObject jobj = new Gson().fromJson(reader, JsonObject.class);
        return jobj;
    }
}
