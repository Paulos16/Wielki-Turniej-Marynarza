
// Created December 1, 2018 by Pawel Saniewski


import java.util.Scanner;

public class Main {

    public static Scanner scan = new Scanner(System.in);

    public static void main(String[] args) {

        String name, iip, response;
        int iport;


        System.out.print("Type in your name: ");
        name = Main.scan.nextLine();

        System.out.println("Type:\n\t'1' to start a new tournament,\n\t'2' to join an existing tournament.");
        response = Main.scan.nextLine();


        switch (response) {

            case "1":
                new PlayerNode(name);
                break;

            case "2":
                System.out.print("Please, type in the ip you wish to connect to: ");
                iip = Main.scan.nextLine();

                System.out.print("Please, type in the port you wish to connect to: ");
                iport = Main.scan.nextInt();

                new PlayerNode(name, iip, iport);
                break;

            default:
                System.out.println("Goodbye " + name + "!");
        }

        //GUI.createAndShowGUI();

    }

}
