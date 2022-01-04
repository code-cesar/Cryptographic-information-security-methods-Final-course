package com.components;

import com.cryptoProtocol.cryptoProtocol;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Connect extends JFrame {
    private JPanel ConnectPanel;
    private JTextField inputIp;
    private JTextField inputPort;
    private JComboBox listLab;
    private JButton bCreate;

    public Connect(){
        setContentPane(ConnectPanel);
        setTitle("CreateLab");
        setSize(662,200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
        bCreate.addActionListener(new start());
    }

    class start implements ActionListener  {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(inputIp.getText().isEmpty() ||  inputPort.getText().isEmpty()){
                JOptionPane.showMessageDialog(null,"Please enter server host name, server port!");
                return;
            }
            int port = Integer.parseInt(inputPort.getText().trim());
            Connect.this.dispose();
            cryptoProtocol.getMenuItems().get(listLab.getSelectedIndex()).action(inputIp.getText(),port);
        }
    }
}
