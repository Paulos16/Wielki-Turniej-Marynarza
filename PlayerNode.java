
// Created December 1, 2018 by Pawel Saniewski


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlayerNode implements Serializable {

    private String name;    // Nazwa agenta
    private int port;       // Nr portu komunikacyjno-nasluchowego
    private String iip;     // Adres IP agenta wprowadzajacego
    private int iport;      // Nr portu agenta wprowadzajacego
    private List<PlayerNode> contacts;

    private transient Map<PlayerNode, String> gameResults;

    public PlayerNode(String name) {
        this.name = name;
        this.port = assignPortNumber();
        this.iip = "127.0.0.1";
        this.iport = this.port;
        this.contacts = new ArrayList<>();
        this.contacts.add(this);
        this.gameResults = new HashMap<>();

        defineQuitProcedure();

        openNodeSocket();
    }

    public PlayerNode(String name, String iip, int iport) {
        this.name = name;
        this.iip = iip;
        this.iport = iport;
        this.port = assignPortNumber();
        this.gameResults = new HashMap<>();

        requestContacts();

        defineQuitProcedure();

        sendNumbers();

        broadcastNewContact();

        this.contacts.add(this);

        openNodeSocket();
    }

    private void openNodeSocket() {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println(InetAddress.getLocalHost()
                                .toString() + " (" + this.name + ") is listening on port: " + this.port);

            while (true) {
                Socket client = server.accept();

                new Thread(() -> {
                    try (
                            ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                            ObjectInputStream in = new ObjectInputStream(client.getInputStream())
                            ) {

                        String msg = in.readUTF();

                        switch (msg) {

                            case "Contacts":
                                out.writeObject(this.contacts);
                                out.flush();
                                //System.out.println("Contacts sent");
                                break;


                            case "Play":
                                PlayerNode opponent = (PlayerNode) in.readObject();

                                System.out.println("Playing with " + opponent.name);
                                System.out.print("Type in a number from 1 to 10: ");

                                int number = Main.scan.nextInt();
                                int response = in.read();


                                if ((number + response)%2 == 0) {
                                    System.out.println("You win!");

                                    this.gameResults.put(opponent, "Won!");

                                    out.writeUTF("You lose!");
                                    out.flush();

                                } else {
                                    System.out.println("You lose!");

                                    this.gameResults.put(opponent, "Lost!");

                                    out.writeUTF("You win!");
                                    out.flush();

                                }
                                break;


                            case "NewContact":
                                PlayerNode newContact = (PlayerNode) in.readObject();
                                this.contacts.add(newContact);
                                //System.out.println(newContact.name + " added to contacts");
                                break;


                            case "Quit":
                                PlayerNode player = (PlayerNode) in.readObject();
                                PlayerNode toRemove = null;

                                for (PlayerNode contact : this.contacts) {
                                    if (contact.port == player.port) {
                                        toRemove = contact;
                                        break;
                                    }
                                }

                                this.contacts.remove(toRemove);
                                //System.out.println(player.name + " removed from contacts");

                                for (PlayerNode contact : this.gameResults.keySet()) {
                                    if (contact.port == player.port) {
                                        toRemove = contact;
                                        break;
                                    }
                                }
                                this.gameResults.remove(toRemove);
                                //System.out.println(player.name + " removed from game results");
                                break;


                            default:
                                System.out.println(">>\t" + msg);
                                break;
                        }


                    } catch (IOException ioex) {
                        ioex.printStackTrace();
                    } catch (ClassNotFoundException cnfex) {
                        cnfex.printStackTrace();
                    }

                }).start();
            }

        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    private void requestContacts() {
        try (
                Socket client = new Socket(this.iip, this.iport);
                ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(client.getInputStream())
        ) {
            //System.out.println("Connected");

            out.writeUTF("Contacts");
            out.flush();

            TimeUnit.SECONDS.sleep(1);

            this.contacts = (List<PlayerNode>) in.readObject();
            //System.out.println("Contacts received");

            for (PlayerNode p : this.contacts) {
                System.out.println(p.name);
            }

        } catch (IOException ioex) {
            ioex.printStackTrace();
        } catch (InterruptedException interrex) {
            interrex.printStackTrace();
        } catch (ClassNotFoundException cnfex) {
            cnfex.printStackTrace();
        }
    }

    private void sendNumbers() {
        for (PlayerNode player : this.contacts) {
            try (
                    Socket client = new Socket(player.iip, player.port);
                    ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(client.getInputStream())
            ) {

                System.out.println("Playing with " + player.name);
                System.out.print("Type in a number from 1 to 10:\t");
                int number = Main.scan.nextInt();

                out.writeUTF("Play");
                out.flush();

                out.writeObject(this);
                out.flush();

                TimeUnit.SECONDS.sleep(1);

                out.write(number);
                out.flush();

                while (in.available() == 0) {
                    TimeUnit.SECONDS.sleep(2);
                }

                String result = in.readUTF();

                if (result.equals("You win!")) {
                    this.gameResults.put(player, "Won");

                } else this.gameResults.put(player, "Lost");

                System.out.println(result);

            } catch (IOException ioex) {
                System.err.println(player.name + " is no longer available");
                ioex.printStackTrace();
            } catch (InterruptedException interrex) {
                interrex.printStackTrace();
            }
        }
    }

    private void broadcastNewContact() {

        for (PlayerNode player : this.contacts) {

            try (
                    Socket client = new Socket(player.iip, player.port);
                    ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(client.getInputStream())
                    ) {

                out.writeUTF("NewContact");
                out.flush();

                out.writeObject(this);
                out.flush();

            } catch (IOException ioex) {
                ioex.printStackTrace();
            }
        }
    }

    private void defineQuitProcedure() {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            this.contacts.remove(this);

            for (PlayerNode player : this.contacts) {

                try (
                        Socket client = new Socket(player.iip, player.port);
                        ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                        ObjectInputStream in = new ObjectInputStream(client.getInputStream())
                ) {

                    out.writeUTF("Quit");
                    out.flush();

                    out.writeObject(this);
                    out.flush();

                } catch (IOException ioex) {
                    ioex.printStackTrace();
                }
            }
        }));
    }

    private int assignPortNumber() {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();

        } catch (IOException ioex) {
            System.err.println("Cannot generate port number!");
            ioex.printStackTrace();
        }
        return 0;
    }

}
