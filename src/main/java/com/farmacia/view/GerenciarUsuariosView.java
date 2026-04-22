package com.farmacia.view;

import com.farmacia.dao.UsuarioDAO;
import com.farmacia.model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class GerenciarUsuariosView extends JFrame {
    private final Usuario usuarioLogado;
    private final UsuarioDAO dao = new UsuarioDAO();
    private final DefaultTableModel modelo;
    private final JTable tabela;

    public GerenciarUsuariosView(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;

        setTitle("Gerenciar Funcionarios - FarmaSys");
        setSize(760, 470);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel painelTopo = new JPanel();
        painelTopo.setBackground(new Color(15, 23, 42));
        JLabel lblTitulo = new JLabel("Funcionarios Cadastrados");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        painelTopo.add(lblTitulo);
        add(painelTopo, BorderLayout.NORTH);

        String[] colunas = {"ID", "Nome Completo", "Login (Usuario)", "Nivel de Acesso"};
        modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabela = new JTable(modelo);
        tabela.setRowHeight(25);
        tabela.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        recarregarTabela();

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(scroll, BorderLayout.CENTER);

        JButton btnExcluir = new JButton("Excluir funcionario");
        btnExcluir.setEnabled(usuarioLogado != null && usuarioLogado.isChefia());
        btnExcluir.addActionListener(e -> excluirFuncionarioSelecionado());

        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dispose());

        JPanel painelBotao = new JPanel();
        painelBotao.add(btnExcluir);
        painelBotao.add(btnFechar);
        add(painelBotao, BorderLayout.SOUTH);
    }

    private void recarregarTabela() {
        modelo.setRowCount(0);
        List<Usuario> usuarios = dao.listarTodos();
        for (Usuario u : usuarios) {
            modelo.addRow(new Object[]{
                    u.getId(),
                    u.getNome(),
                    u.getLogin(),
                    u.getCargo()
            });
        }
    }

    private void excluirFuncionarioSelecionado() {
        if (usuarioLogado == null || !usuarioLogado.isChefia()) {
            JOptionPane.showMessageDialog(this, "A exclusao e permitida apenas para chefe ou administrador.");
            return;
        }

        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um funcionario para excluir.");
            return;
        }

        int usuarioId = (int) modelo.getValueAt(linha, 0);
        String nome = String.valueOf(modelo.getValueAt(linha, 1));
        String login = String.valueOf(modelo.getValueAt(linha, 2));
        String cargo = String.valueOf(modelo.getValueAt(linha, 3));

        if (usuarioLogado.getId() == usuarioId) {
            JOptionPane.showMessageDialog(this, "Voce nao pode excluir o proprio usuario logado.");
            return;
        }

        if ("Chefe".equalsIgnoreCase(cargo) || "Administrador".equalsIgnoreCase(cargo) || "Admin".equalsIgnoreCase(cargo)) {
            JOptionPane.showMessageDialog(this, "Usuarios chefe e admin sao permanentes e nao podem ser excluidos.");
            return;
        }

        int confirmacao = JOptionPane.showConfirmDialog(
                this,
                "Deseja realmente excluir o funcionario " + nome + " (" + login + ")?",
                "Confirmar exclusao",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }

        if (dao.excluirPorId(usuarioId)) {
            JOptionPane.showMessageDialog(this, "Funcionario excluido com sucesso.");
            recarregarTabela();
        } else {
            JOptionPane.showMessageDialog(this, "Nao foi possivel excluir o funcionario.");
        }
    }
}
