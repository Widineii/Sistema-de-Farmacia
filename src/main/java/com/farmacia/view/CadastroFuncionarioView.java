package com.farmacia.view;

import com.farmacia.dao.UsuarioDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CadastroFuncionarioView extends JFrame {
    private JTextField txtNome;
    private JTextField txtLogin;
    private JPasswordField txtSenha;
    private JComboBox<String> cbCargo;
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public CadastroFuncionarioView() {
        setTitle("Cadastro de Funcionario");
        setSize(680, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(new Color(241, 245, 249));
        raiz.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(raiz);

        raiz.add(criarCabecalho(), BorderLayout.NORTH);
        raiz.add(criarFormulario(), BorderLayout.CENTER);
        raiz.add(criarRodape(), BorderLayout.SOUTH);
    }

    private JPanel criarCabecalho() {
        JPanel topo = new JPanel();
        topo.setBackground(Color.WHITE);
        topo.setBorder(new EmptyBorder(18, 20, 18, 20));
        topo.setLayout(new BoxLayout(topo, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Cadastro de Funcionario");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(new Color(15, 23, 42));

        JLabel subtitulo = new JLabel("Crie acessos para atendimento, chefia e administracao com controle de perfil.");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitulo.setForeground(new Color(100, 116, 139));

        topo.add(titulo);
        topo.add(Box.createRigidArea(new Dimension(0, 6)));
        topo.add(subtitulo);
        return topo;
    }

    private JPanel criarFormulario() {
        JPanel card = new JPanel(new BorderLayout(18, 18));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
        grid.setOpaque(false);

        txtNome = new JTextField();
        txtLogin = new JTextField();
        txtSenha = new JPasswordField();
        cbCargo = new JComboBox<>(new String[]{"Atendente", "Chefe", "Administrador"});

        grid.add(criarCampo("Nome completo", txtNome));
        grid.add(criarCampo("Login de acesso", txtLogin));
        grid.add(criarCampo("Senha inicial", txtSenha));
        grid.add(criarCampo("Perfil", cbCargo));

        JLabel painelLateral = new JLabel("<html><body style='width:220px'>"
                + "Sugestoes para cadastro:<br>"
                + "- use login curto e sem espacos<br>"
                + "- defina senha inicial facil de repassar<br>"
                + "- escolha o perfil correto para liberar so o necessario"
                + "</body></html>");
        painelLateral.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        painelLateral.setForeground(new Color(71, 85, 105));

        JPanel lateral = new JPanel(new BorderLayout());
        lateral.setOpaque(false);
        lateral.setBorder(new EmptyBorder(8, 12, 8, 12));
        lateral.add(painelLateral, BorderLayout.NORTH);

        card.add(grid, BorderLayout.CENTER);
        card.add(lateral, BorderLayout.EAST);
        return card;
    }

    private JPanel criarCampo(String titulo, JComponent campo) {
        JPanel painel = new JPanel();
        painel.setOpaque(false);
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(titulo);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(71, 85, 105));

        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        painel.add(label);
        painel.add(Box.createRigidArea(new Dimension(0, 6)));
        painel.add(campo);
        return painel;
    }

    private JPanel criarRodape() {
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rodape.setOpaque(false);

        JButton btnCancelar = new JButton("Cancelar");
        JButton btnSalvar = new JButton("Cadastrar funcionario");

        btnCancelar.addActionListener(e -> dispose());
        btnSalvar.setBackground(new Color(15, 23, 42));
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.addActionListener(e -> salvarFuncionario());

        rodape.add(btnCancelar);
        rodape.add(btnSalvar);
        getRootPane().setDefaultButton(btnSalvar);
        return rodape;
    }

    private void salvarFuncionario() {
        String nome = txtNome.getText().trim();
        String login = txtLogin.getText().trim().toLowerCase();
        String senha = new String(txtSenha.getPassword()).trim();
        String cargo = String.valueOf(cbCargo.getSelectedItem());

        if (nome.isBlank()) {
            JOptionPane.showMessageDialog(this, "Informe o nome do funcionario.");
            return;
        }

        if (login.isBlank() || login.contains(" ")) {
            JOptionPane.showMessageDialog(this, "Use um login sem espacos.");
            return;
        }

        if (senha.length() < 4) {
            JOptionPane.showMessageDialog(this, "A senha inicial precisa ter pelo menos 4 caracteres.");
            return;
        }

        if (usuarioDAO.existeLogin(login)) {
            JOptionPane.showMessageDialog(this, "Ja existe um usuario com esse login.");
            return;
        }

        if (usuarioDAO.cadastrar(nome, login, senha, cargo)) {
            JOptionPane.showMessageDialog(this, "Funcionario cadastrado com sucesso.");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao cadastrar funcionario.");
        }
    }
}
