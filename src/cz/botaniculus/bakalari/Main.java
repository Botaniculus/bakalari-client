package cz.botaniculus.bakalari;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in, "Windows-1250");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        Bakal bakal = new Bakal("https://www.gvp.cz/info");

        //-----login---------------------------------
        //If you have no refresh token:
        bakal.login(username, password, false);

        //If you have refresh token
        bakal.login(username, password, true);

        //user info
        System.out.println(bakal.getUserInfo());

        //timetable
        System.out.println(bakal.getTimetable(date()[0], date()[1], date()[2]));
        //System.out.println(bakal.getTimetable(7, 10, 2020));

        //marks
        System.out.println(bakal.getMarks());
    }
    public static int[] date(){
        int[] output = new int[3];
        Date date = new Date();
        SimpleDateFormat formatter;
        //day
        formatter = new SimpleDateFormat("dd");
        output[0] = Integer.parseInt(formatter.format(date));

        //month
        formatter = new SimpleDateFormat("MM");
        output[1] = Integer.parseInt(formatter.format(date));

        //year
        formatter = new SimpleDateFormat("YYYY");
        output[2] = Integer.parseInt(formatter.format(date));

        return output;
    }
}
