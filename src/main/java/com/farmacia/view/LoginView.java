package com.farmacia.view;

import com.farmacia.dao.UsuarioDAO;
import com.farmacia.model.Usuario;
import com.farmacia.util.ConnectionFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginView extends JFrame {

    private JComboBox<String> cbNivel;
    private JTextField txtLogin;
    private JPasswordField txtSenha;
    private JButton btnEntrar;

    private final Color azulPetroleo = new Color(8, 47, 73);
    private final Color verdeSistema = new Color(22, 101, 52);
    private final Color azulClaro = new Color(14, 165, 233);

    public LoginView() {
        setTitle("FarmaSys Pro - Login");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel fundo = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(236, 253, 245), getWidth(), getHeight(), new Color(224, 242, 254));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(new Color(186, 230, 253, 120));
                g2.fillOval(-80, -40, 320, 320);
                g2.setColor(new Color(187, 247, 208, 110));
                g2.fillOval(getWidth() - 300, 80, 260, 260);
                g2.setColor(new Color(191, 219, 254, 100));
                g2.fillOval(getWidth() / 2 - 120, getHeight() - 220, 280, 280);
            }
        };
        setContentPane(fundo);

        JPanel shell = new JPanel(new GridLayout(1, 2, 0, 0));
        shell.setPreferredSize(new Dimension(980, 560));
        shell.setBorder(new EmptyBorder(0, 0, 0, 0));

        shell.add(criarPainelMarca());
        shell.add(criarPainelLogin());

        fundo.add(shell);
        getRootPane().setDefaultButton(btnEntrar);
    }

    private JPanel criarPainelMarca() {
        JPanel painel = new JPanel();
        painel.setBackground(azulPetroleo);
        painel.setBorder(new EmptyBorder(42, 42, 42, 42));
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));

        JLabel badge = new JLabel("FARMA SYS PRO");
        badge.setOpaque(true);
        badge.setBackground(new Color(255, 255, 255, 35));
        badge.setForeground(Color.WHITE);
        badge.setBorder(new EmptyBorder(8, 12, 8, 12));
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titulo = new JLabel("<html>Gestao inteligente<br>para farmacia</html>");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 34));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel("<html>Login profissional para atendimento, chefia e administracao com foco em operacao de loja, caixa e controle de estoque.</html>");
        subtitulo.setForeground(new Color(191, 219, 254));
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel cards = new JPanel(new GridLayout(3, 1, 0, 12));
        cards.setOpaque(false);
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        cards.add(criarInfoCard("Caixa agil", "Atendentes vendem rapido com cliente identificado e busca de medicamentos."));
        cards.add(criarInfoCard("Visao gerencial", "Chefia acompanha faturamento, comissao e alertas do estoque."));
        cards.add(criarInfoCard("Perfis claros", "Cada usuario entra com o nivel certo e ve apenas o que precisa."));

        painel.add(badge);
        painel.add(Box.createRigidArea(new Dimension(0, 26)));
        painel.add(titulo);
        painel.add(Box.createRigidArea(new Dimension(0, 14)));
        painel.add(subtitulo);
        painel.add(Box.createRigidArea(new Dimension(0, 30)));
        painel.add(cards);
        return painel;
    }

    private JPanel criarInfoCard(String titulo, String texto) {
        JPanel card = new JPanel();
        card.setOpaque(true);
        card.setBackground(new Color(255, 255, 255, 28));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JLabel lblTexto = new JLabel("<html><body style='width:300px'>" + texto + "</body></html>");
        lblTexto.setForeground(new Color(219, 234, 254));
        lblTexto.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        card.add(lblTitulo);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(lblTexto);
        return card;
    }

    private JPanel criarPainelLogin() {
        JPanel painel = new JPanel();
        painel.setBackground(Color.WHITE);
        painel.setBorder(new EmptyBorder(48, 48, 48, 48));
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Acesso ao sistema");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titulo.setForeground(new Color(15, 23, 42));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel("Entre com seu perfil para continuar.");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitulo.setForeground(new Color(100, 116, 139));
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbNivel = new JComboBox<>(new String[]{"Atendente", "Gerente/Chefe", "Administrador"});
        txtLogin = new JTextField();
        txtSenha = new JPasswordField();
        btnEntrar = new JButton("Entrar no painel");

        estilizarCampo(cbNivel);
        estilizarCampo(txtLogin);
        estilizarCampo(txtSenha);

        btnEntrar.setBackground(verdeSistema);
        btnEntrar.setForeground(Color.WHITE);
        btnEntrar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnEntrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btnEntrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEntrar.setBorderPainted(false);
        btnEntrar.setFocusPainted(false);
        btnEntrar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnEntrar.addActionListener(e -> autenticar());

        JPanel aviso = new JPanel(new BorderLayout());
        aviso.setBackground(new Color(240, 249, 255));
        aviso.setBorder(new EmptyBorder(12, 14, 12, 14));
        aviso.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblOrientacao = new JLabel("<html><body>Use seu usuario individual e selecione o perfil correto para acessar o sistema.</body></html>");
        lblOrientacao.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblOrientacao.setForeground(azulPetroleo);
        aviso.add(lblOrientacao, BorderLayout.CENTER);

        painel.add(titulo);
        painel.add(Box.createRigidArea(new Dimension(0, 6)));
        painel.add(subtitulo);
        painel.add(Box.createRigidArea(new Dimension(0, 26)));
        adicionarComLabel(painel, "Nivel de acesso", cbNivel);
        adicionarComLabel(painel, "Usuario", txtLogin);
        adicionarComLabel(painel, "Senha", txtSenha);
        painel.add(Box.createRigidArea(new Dimension(0, 18)));
        painel.add(btnEntrar);
        painel.add(Box.createRigidArea(new Dimension(0, 16)));
        painel.add(aviso);
        return painel;
    }

    private void autenticar() {
        String loginDigitado = txtLogin.getText().trim();
        String senhaDigitada = new String(txtSenha.getPassword()).trim();
        String nivelSelecionado = String.valueOf(cbNivel.getSelectedItem());

        Usuario user = new UsuarioDAO().autenticar(loginDigitado, senhaDigitada);

        if (user == null) {
            JOptionPane.showMessageDialog(this, "Usuario ou senha incorretos.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!acessoPermitido(nivelSelecionado, user)) {
            JOptionPane.showMessageDialog(
                    this,
                    "O usuario existe, mas o nivel selecionado nao combina com o cargo salvo: " + user.getCargoExibicao(),
                    "Acesso nao permitido",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        new MenuView(user).setVisible(true);
        dispose();
    }

    private boolean acessoPermitido(String nivelSelecionado, Usuario user) {
        if ("Administrador".equals(nivelSelecionado)) {
            return user.isAdministrador();
        }
        if ("Gerente/Chefe".equals(nivelSelecionado)) {
            return user.isChefia();
        }
        return user.isAtendente();
    }

    private void estilizarCampo(JComponent c) {
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        c.setBackground(Color.WHITE);
    }

    private void adicionarComLabel(JPanel p, String texto, JComponent comp) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(71, 85, 105));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(label);
        p.add(Box.createRigidArea(new Dimension(0, 6)));
        p.add(comp);
        p.add(Box.createRigidArea(new Dimension(0, 16)));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        ConnectionFactory.getConexao();
        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }
}
