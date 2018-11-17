package Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class CSVparser<K,V> {

    private String csvFile;

    public CSVparser(String csvFile) {
        this.csvFile = csvFile;
    }

    public HashMap<String, String> getMap() {

        HashMap<String, String> map = new HashMap<>();
        Scanner fileReader = null;
        try {
            fileReader = new Scanner(new File(csvFile));

            while (fileReader.hasNextLine()) {
                String line = fileReader.nextLine();
                String[] splittedLine = line.split(",");

                if (splittedLine.length >= 2) {
                    String key = splittedLine[0];
                    String value = splittedLine[1];
                    map.put((String) key,(String) value);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return map;
    }

}
