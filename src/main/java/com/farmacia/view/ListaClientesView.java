package com.farmacia.view;

import com.farmacia.dao.ClienteDAO;
import com.farmacia.model.Cliente;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ListaClientesView extends JFrame {
    public ListaClientesView() {
        setTitle("Clientes Cadastrados");
        setSize(600, 400);
        setLocationRelativeTo(null);

        String[] colunas = {"ID", "Nome", "CPF", "Telefone"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0);
        JTable tabela = new JTable(modelo);

        List<Cliente> clientes = new ClienteDAO().listarTodosParaTabela();
        for (Cliente c : clientes) {
            modelo.addRow(new Object[]{c.getId(), c.getNome(), c.getCpf(), c.getTelefone()});
        }

        add(new JScrollPane(tabela), BorderLayout.CENTER);
    }
}