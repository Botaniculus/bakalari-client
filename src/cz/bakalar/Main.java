package cz.bakalar;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in, "Windows-1250");
        Bakal bakal = new Bakal();
        //Set master parameters
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        String output[] = bakal.login(username, password);

        System.out.println(bakal.timetable());
    }
}
