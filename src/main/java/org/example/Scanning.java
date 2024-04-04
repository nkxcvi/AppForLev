package org.example;

import java.util.ArrayList;
import java.util.Scanner;

public class Scanning {
    public static ArrayList<String> scan(String someone) {
        ArrayList<String> getGtins = new ArrayList<>();
        Scanner scanner = new Scanner(someone);
        String line = null;
        while (scanner.hasNextLine()) {
             line = scanner.nextLine();
             getGtins.add(line);
        }
        return getGtins;
    }
}
