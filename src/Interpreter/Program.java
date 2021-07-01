package Interpreter;

import javafx.application.Application;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Program {
    static Integer lineNumber = 0;
    static ArrayList<String> codes = null;
    static String commentPattern=" [/]{2}.*";
    static String logicPattern="^\\b([\\w|\\$]+[ ][=][ ]([\\d]|[\\w|\\$]+))( [\\-+*/] ([\\d]|[\\w|\\$]+))?\\b( [/]{2}.*)?$";
    static String forPattern="^for [1-9]( [/]{2}.*)?$";
    static String endforPattern="^end for( [/]{2}.*)?$";
    static String printPattern="^print .+( [/]{2}.*)?$";
    //Main Method ... ********************************************************************
    public Program() throws IOException {
        if (Graphics.path != null) {
            File file = new File(Graphics.path);
            if (!file.exists()) {
                throw new IOException("File does not exist!");
            } else {
                if (file.isDirectory()) {
                    throw new IllegalArgumentException("there is a directory...");
                } else if (file.isFile()) {
                    readFile(file);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Application.launch(Graphics.class, args);
//        File file = new File("src//Interpreter//TextFiles//ali.txt");
//        readFile(file);
    }
    //Other methods ... *******************************************************************
    public static void readFile(File f) throws IOException {
        Boolean faz1 = true; //true -> faz1, false -> faz2
        Scanner sc = new Scanner(f);
        try {
            while (faz1) {
                String pattern="^\\b(((int )|(float ))[\\w|\\$]+([ ][=][ ](([\\-]?[\\d]+([\\.][\\d]*)?)|[\\w|\\$]+))?)|^\\b([\\w|\\$]+[ ][=][ ](([\\-]?[\\d]+([\\.][\\d]*)?)|[\\w|\\$]+))?( [/]{2}.+)?$";
                String line = sc.nextLine();
                lineNumber++;
                line = line.trim();
                line = line.replaceAll("//", " //");
                line = line.replaceAll("([ ]+|[\\t]+)+", " ");
                String[] tokens = line.split(" ");
                if (line.isEmpty()||line.matches(commentPattern)) continue;
                if (line.equals("%%")) {
                    faz1 = false; //jump to faz2
                } else if(line.matches(pattern)){
                    GiveValue giveValue = new GiveValue(tokens);
                }else {
                    throw new IllegalArgumentException("this line is NOT valied!!!(at line: " + lineNumber + ")");
                }
            }
            //start faz2
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                lineNumber++;
                line = line.trim();
                line = line.replaceAll("([ ]+|[\\t]+)+", " ");
                String[] tokens = line.split(" ");
                if (tokens[0].equals("for")) gotoEnd(sc);
                if (makeTokens(line)==-1)continue;
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            sc.close();
        }
    }

    public static int makeTokens(String line) throws IOException {
        line = line.replaceAll("//", " //");
        line = line.replaceAll("([ ]+|[\\t]+)+", " ");
        String[] tokens = line.split(" ");
        if (line.matches(logicPattern)) {
            if (tokens.length==3) {
                GiveValue giveValue = new GiveValue(tokens);
            } else if (tokens[3].matches("[+|\\/|\\-|\\*]")){
                Logic logic = new Logic(tokens);
            }
        } else if (line.matches(forPattern)) {
            codes = new ArrayList();
            int start = Program.lineNumber;
            int finish = search(start, codes);
            Program.lineNumber = finish + 1;
            Loop loop = new Loop(tokens, start, finish, codes);
        } else if (line.matches(printPattern)) {
            Print print = new Print(tokens);
            return print.getCharNumber();
        } else if (line.isEmpty() || line.matches(commentPattern)) return -1;
        else
            System.err.println("this lines is not valid (at line: " + lineNumber + ") " + "--- The program is Stopped.");

        return 0;
    }
    private static int search(int start, ArrayList codes) throws IOException {
        int lineCount=0;
        int forCounter = 0;
        int endCounter = 0;
        int counter = start + 1;
        boolean sw = true;
        while (sw) {
            String line = getLine(counter);
            lineCount++;
            if ( line == null &&forCounter == endCounter ){
                throw new IllegalArgumentException("Loop does not have any 'end for' or the syntax is NOT correct!(at line: " + (lineNumber+lineCount) + ")");
            }
            line = line.trim();
            line = line.replaceAll("([ ]+|[\\t]+)+", " ");
            if (line.matches(forPattern))
                forCounter++;
            else if (line.matches(endforPattern) && (endCounter < forCounter))
                endCounter++;
            else if (line.matches(endforPattern) && (endCounter == forCounter))
                return (counter - 1);
            codes.add(line);
            counter++;
        }
        return 0;
    }

    public static String getLine(int lineNum) throws IOException {
        String line;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(Graphics.path));
            for (int i = 0; i < lineNum - 1; i++) {
                bufferedReader.readLine();
            }
            line = bufferedReader.readLine();
            return line;
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public static void gotoEnd(Scanner sc) {
        String line = null;
        int forCount = 1;
        int endCount = 0;
        String[] tokens = null;
        while (sc.hasNextLine()) {
            line = sc.nextLine();
            line = line.trim();
            line = line.replaceAll("([ ]+|[\\t]+)+", " ");
            tokens = line.split(" ");
            if (tokens[0].equals("for")) forCount++;
            if (tokens[0].equals("end")) endCount++;
            if (endCount >= forCount) break;
        }
    }
}