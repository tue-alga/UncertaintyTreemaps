package com.eduardovernier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Parser {

    public static List<List<Entity>> parseCSVs(String directory) {

        File[] fileList = new File(directory).listFiles();
        List<String> fileNames = new ArrayList<>();

        if (fileList != null) {
            for (File file : fileList) {
                if (file.isFile()) {
                    try {
                        fileNames.add(file.getCanonicalPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.err.println("Invalid dir path.");
            System.exit(-1);
        }

        fileNames.sort(String::compareTo);
        fileNames.sort((o1, o2) -> Integer.valueOf(o1.length()).compareTo(Integer.valueOf(o2.length())));

        // Parse files in two passes
        // First to count number of unique items and initialize data structures with the correct size
        // And second to fill the values correctly
        List<List<Entity>> revisionList = new LinkedList<>();
        try {

            List<String> uniqueItems = new ArrayList<>();

            int numberOfRevisions = fileNames.size();
            for (String fileName : fileNames) {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
                String currentLine = bufferedReader.readLine();
                String[] header = currentLine.split(",");

                if (!header[0].equals("id") || !header[1].equals("weight")) {
                    System.err.println("Error parsing header - " + fileName);
                    System.exit(-1);
                }

                while ((currentLine = bufferedReader.readLine()) != null) {
                    String[] split = currentLine.split(",");
                    if (split.length != 2) {
                        System.err.println("Error parsing csv file");
                        System.exit(-1);
                    } else {
                        String id = split[0];
                        if (!uniqueItems.contains(id)) {
                            uniqueItems.add(id);
                        }
                    }
                }
            }

            for (int revision = 0; revision < numberOfRevisions; ++revision) {
                List<Entity> entityList = new ArrayList<>(uniqueItems.size());
                for (String uniqueItem : uniqueItems) {
                    entityList.add(new Entity(uniqueItem, 0.0));
                }
                revisionList.add(entityList);
            }

            for (int revision = 0; revision < numberOfRevisions; ++revision) {

                List<Entity> entityList = revisionList.get(revision);
                BufferedReader bufferedReader = new BufferedReader(new FileReader(fileNames.get(revision)));
                String currentLine = bufferedReader.readLine();
                String[] header = currentLine.split(",");

                if (!header[0].equals("id") || !header[1].equals("weight")) {
                    System.err.println("Error parsing header - " + fileNames.get(revision));
                    System.exit(-1);
                }

                while ((currentLine = bufferedReader.readLine()) != null) {

                    String[] split = currentLine.split(",");
                    if (split.length != 2) {
                        System.err.println("Error parsing csv file");
                        System.exit(-1);
                    } else {
                        String id = split[0];
                        double weight = Double.parseDouble(split[1]);
                        int index = uniqueItems.indexOf(id);
                        entityList.get(index).weight = weight;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Shuffle
        for (List<Entity> entityList : revisionList) {
            Collections.shuffle(entityList, new Random(42));
        }

        return revisionList;
    }
}
