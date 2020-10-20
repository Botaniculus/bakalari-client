package cz.botaniculus.bakalari;

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
        //login
        bakal.login(username, password);

        //timetable
        System.out.println(bakal.timetable());
    }
}