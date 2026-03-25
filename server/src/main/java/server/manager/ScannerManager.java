package server.manager;

import java.util.Scanner;

public class ScannerManager {
    private Scanner consoleScanner;
    
    public ScannerManager() {
        this.consoleScanner = new Scanner(System.in);
    }
     
    public String readLine() {
        return consoleScanner.nextLine();
    }
    
    public boolean hasNextLine() {
        return consoleScanner.hasNextLine();
    }
    public void close() {
        consoleScanner.close();
    }
}