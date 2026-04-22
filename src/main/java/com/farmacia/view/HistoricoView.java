package com.farmacia.view;

import javax.swing.*;
import java.awt.*;

public class HistoricoView extends JFrame {
    public HistoricoView() {
        setTitle("Histórico de Vendas");
        setSize(600, 400);
        setLocationRelativeTo(null);

        JLabel lbl = new JLabel("Histórico de Vendas Online", SwingConstants.CENTER);
        add(lbl);
    }
}