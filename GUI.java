
// Created December 1, 2018 by Pawel Saniewski


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GUI extends JFrame {

    public GUI(String title) {
        super(title);
    }

    private static JPanel addComponentsToPane() {
        JPanel jp = new JPanel();
        JPanel jpTop = new JPanel();
        JPanel jpBtm = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
        jpTop.setLayout(new BoxLayout(jpTop, BoxLayout.X_AXIS));
        jpBtm.setLayout(new BoxLayout(jpBtm, BoxLayout.X_AXIS));

        JButton playRequest = new JButton("Play!");
        playRequest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //
            }
        });


        jp.add(jpTop);
        jp.add(jpBtm);

        return jp;
    }

    public static void createAndShowGUI() {
        GUI frame = new GUI("SKJTournament");

        frame.setSize(1000,700);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setContentPane(addComponentsToPane());
        frame.setVisible(true);
    }

}
