package com.dsapkl.backend.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CsvReader {

    public static void readCsv(String csvPath) {
        File file = new File(csvPath);

        if(!file.exists()) {
            System.out.println("File does not exist");
            return;
        }

        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = null;
            while((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading file");
        }
    }

}
