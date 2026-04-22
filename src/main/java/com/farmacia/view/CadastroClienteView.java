package com.farmacia.view;

import com.farmacia.dao.ClienteDAO;
import com.farmacia.model.Cliente;
import com.farmacia.util.CpfUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;

public class CadastroClienteView extends JFrame {
    private JTextField txtNome;
    private JFormattedTextField txtCpf;
    private JTextField txtTelefone;
    private JLabel lblResumo;
    private final ClienteDAO clienteDAO = new ClienteDAO();

    public CadastroClienteView() {
        setTitle("Cadastro de Cliente");
        setSize(640, 430);
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

        JLabel titulo = new JLabel("Cadastro de Cliente");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(new Color(15, 23, 42));

        JLabel subtitulo = new JLabel("Registre clientes com CPF valido para agilizar o atendimento no caixa.");
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

        JPanel campos = new JPanel(new GridLayout(3, 2, 16, 16));
        campos.setOpaque(false);

        txtNome = new JTextField();
        txtCpf = criarCampoCpf();
        txtTelefone = new JTextField();

        campos.add(criarCampo("Nome completo", txtNome));
        campos.add(criarCampo("CPF", txtCpf));
        campos.add(criarCampo("Telefone", txtTelefone));

        JPanel painelCampos = new JPanel(new GridLayout(1, 1));
        painelCampos.setOpaque(false);
        painelCampos.add(campos);

        lblResumo = new JLabel("<html><body style='width:220px'>"
                + "Boas práticas:<br>"
                + "- use o nome completo do cliente<br>"
                + "- informe um CPF real e válido<br>"
                + "- deixe o telefone pronto para contato"
                + "</body></html>");
        lblResumo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblResumo.setForeground(new Color(71, 85, 105));

        JPanel lateral = new JPanel(new BorderLayout());
        lateral.setOpaque(false);
        lateral.setBorder(new EmptyBorder(8, 12, 8, 12));
        lateral.add(lblResumo, BorderLayout.NORTH);

        card.add(painelCampos, BorderLayout.CENTER);
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
        JButton btnSalvar = new JButton("Salvar cliente");

        btnCancelar.addActionListener(e -> dispose());
        btnSalvar.setBackground(new Color(14, 165, 233));
        btnSalvar.setForeground(Color.BLACK);
        btnSalvar.addActionListener(e -> salvarCliente());

        rodape.add(btnCancelar);
        rodape.add(btnSalvar);
        getRootPane().setDefaultButton(btnSalvar);
        return rodape;
    }

    private JFormattedTextField criarCampoCpf() {
        try {
            MaskFormatter mascara = new MaskFormatter("###.###.###-##");
            mascara.setPlaceholderCharacter('_');
            JFormattedTextField campo = new JFormattedTextField(mascara);
            campo.setColumns(14);
            return campo;
        } catch (ParseException e) {
            return new JFormattedTextField();
        }
    }

    private void salvarCliente() {
        String nome = txtNome.getText().trim();
        String cpf = txtCpf.getText().trim();
        String telefone = txtTelefone.getText().trim();

        if (nome.isBlank()) {
            JOptionPane.showMessageDialog(this, "Informe o nome do cliente.");
            return;
        }

        if (!CpfUtils.isValido(cpf)) {
            JOptionPane.showMessageDialog(this, "Informe um CPF valido.");
            return;
        }

        if (clienteDAO.existeCpf(cpf)) {
            JOptionPane.showMessageDialog(this, "Ja existe um cliente com esse CPF.");
            return;
        }

        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setCpf(CpfUtils.apenasDigitos(cpf));
        cliente.setTelefone(telefone);

        if (clienteDAO.salvar(cliente)) {
            JOptionPane.showMessageDialog(this, "Cliente salvo com sucesso.");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao salvar o cliente.");
        }
    }
}
