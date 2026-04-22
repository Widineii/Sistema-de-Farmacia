package com.farmacia.view;

import com.farmacia.dao.ProdutoDAO;
import com.farmacia.dao.VendaDAO;
import com.farmacia.model.Usuario;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MenuView extends JFrame {
    private final Usuario usuarioLogado;
    private final VendaDAO vendaDAO = new VendaDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private static final Color COR_FUNDO = new Color(236, 242, 248);
    private static final Color COR_LATERAL = new Color(12, 22, 38);
    private static final Color COR_LATERAL_CARD = new Color(28, 40, 63);
    private static final Color COR_CARD = new Color(255, 255, 255);
    private static final Color COR_BORDA = new Color(219, 229, 241);
    private static final Color COR_TITULO = new Color(15, 23, 42);
    private static final Color COR_TEXTO = new Color(71, 85, 105);
    private static final Color COR_AZUL = new Color(14, 165, 233);
    private static final Color COR_AZUL_HOVER = new Color(3, 105, 161);
    private static final Color COR_VERDE = new Color(22, 163, 74);
    private static final Color COR_ROXO = new Color(109, 40, 217);

    public MenuView(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;

        setTitle("FarmaSys Pro - Painel Operacional");
        setSize(1320, 900);
        setMinimumSize(new Dimension(1280, 820));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(COR_FUNDO);

        add(criarLateral(), BorderLayout.WEST);
        add(criarConteudo(), BorderLayout.CENTER);
    }

    private JPanel criarLateral() {
        JPanel lateral = new JPanel();
        lateral.setBackground(COR_LATERAL);
        lateral.setPreferredSize(new Dimension(270, 0));
        lateral.setLayout(new BorderLayout());
        lateral.setBorder(new EmptyBorder(24, 18, 24, 18));

        JPanel topo = new JPanel();
        topo.setOpaque(false);
        topo.setLayout(new BoxLayout(topo, BoxLayout.Y_AXIS));

        JLabel marca = new JLabel("FarmaSys Pro");
        marca.setForeground(Color.WHITE);
        marca.setFont(new Font("Segoe UI", Font.BOLD, 25));

        JLabel selo = new JLabel("PAINEL OPERACIONAL");
        selo.setOpaque(true);
        selo.setBackground(new Color(30, 58, 138));
        selo.setForeground(new Color(219, 234, 254));
        selo.setFont(new Font("Segoe UI", Font.BOLD, 10));
        selo.setBorder(new EmptyBorder(6, 10, 6, 10));
        selo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel perfil = new JLabel(usuarioLogado.getNome());
        perfil.setForeground(new Color(125, 211, 252));
        perfil.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JLabel cargo = new JLabel("Perfil: " + usuarioLogado.getCargoExibicao());
        cargo.setForeground(new Color(203, 213, 225));
        cargo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        topo.add(marca);
        topo.add(Box.createRigidArea(new Dimension(0, 10)));
        topo.add(selo);
        topo.add(Box.createRigidArea(new Dimension(0, 20)));
        topo.add(perfil);
        topo.add(Box.createRigidArea(new Dimension(0, 4)));
        topo.add(cargo);

        JPanel botoes = new JPanel();
        botoes.setOpaque(false);
        botoes.setLayout(new GridLayout(7, 1, 0, 10));
        botoes.setBorder(new EmptyBorder(26, 0, 0, 0));

        JButton btnVenda = criarBotao("Caixa / PDV");
        JButton btnCaixaPagamento = criarBotao("Caixa de Pagamento");
        JButton btnEstoque = criarBotao("Controle de Estoque");
        JButton btnRelatorios = criarBotao("Relatorios");
        JButton btnClientes = criarBotao("Gestao de Clientes");
        JButton btnEquipe = criarBotao("Equipe");
        JButton btnSair = criarBotao("Sair do Sistema");

        btnVenda.addActionListener(e -> abrirTela(new VendaView(usuarioLogado)));
        btnCaixaPagamento.addActionListener(e -> abrirTela(new CaixaPagamentoView(usuarioLogado)));
        btnEstoque.addActionListener(e -> abrirTela(new EstoqueView(usuarioLogado)));
        btnRelatorios.addActionListener(e -> abrirTela(new RelatorioFinanceiroView(usuarioLogado)));
        btnClientes.addActionListener(e -> abrirMenuClientes());
        btnEquipe.addActionListener(e -> abrirMenuEquipe());
        btnSair.addActionListener(e -> {
            new LoginView().setVisible(true);
            dispose();
        });

        btnEquipe.setEnabled(usuarioLogado.isChefia());

        botoes.add(btnVenda);
        botoes.add(btnCaixaPagamento);
        botoes.add(btnEstoque);
        botoes.add(btnRelatorios);
        botoes.add(btnClientes);
        botoes.add(btnEquipe);
        botoes.add(btnSair);

        lateral.add(topo, BorderLayout.NORTH);
        lateral.add(botoes, BorderLayout.CENTER);
        return lateral;
    }

    private JPanel criarConteudo() {
        JPanel conteudo = new JPanel(new BorderLayout());
        conteudo.setBackground(COR_FUNDO);

        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(Color.WHITE);
        cabecalho.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COR_BORDA),
                new EmptyBorder(24, 28, 24, 28)
        ));

        JLabel titulo = new JLabel("Painel operacional da farmacia");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titulo.setForeground(COR_TITULO);

        JLabel subtitulo = new JLabel(montarSubtituloPerfil());
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitulo.setForeground(new Color(100, 116, 139));

        JPanel blocoTitulo = new JPanel();
        blocoTitulo.setOpaque(false);
        blocoTitulo.setLayout(new BoxLayout(blocoTitulo, BoxLayout.Y_AXIS));
        blocoTitulo.add(titulo);
        blocoTitulo.add(Box.createRigidArea(new Dimension(0, 6)));
        blocoTitulo.add(subtitulo);

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        direita.setOpaque(false);
        direita.add(criarBadgeCabecalho("Hoje " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), COR_VERDE));
        direita.add(criarBadgeCabecalho(usuarioLogado.getCargoExibicao(), COR_ROXO));

        cabecalho.add(blocoTitulo, BorderLayout.WEST);
        cabecalho.add(direita, BorderLayout.EAST);

        JPanel corpo = new JPanel();
        corpo.setOpaque(false);
        corpo.setBorder(new EmptyBorder(24, 28, 28, 28));
        corpo.setLayout(new BorderLayout(0, 24));

        corpo.add(criarResumo(), BorderLayout.NORTH);
        corpo.add(criarPainelAtalhos(), BorderLayout.CENTER);

        conteudo.add(cabecalho, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(corpo);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(COR_FUNDO);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        conteudo.add(scroll, BorderLayout.CENTER);
        return conteudo;
    }

    private JPanel criarResumo() {
        JPanel resumo = new JPanel(new GridLayout(1, 4, 16, 0));
        resumo.setOpaque(false);

        double vendasDoUsuario = vendaDAO.buscarFaturamentoDoDiaPorUsuario(usuarioLogado.getId());
        double comissaoDoUsuario = vendaDAO.buscarComissaoDoDiaPorUsuario(usuarioLogado.getId());
        double vendasTotais = usuarioLogado.isChefia() ? vendaDAO.buscarFaturamentoDoDia() : vendasDoUsuario;
        int quantidadeVendas = usuarioLogado.isChefia() ? vendaDAO.contarVendasDoDia() : vendaDAO.contarVendasDoDiaPorUsuario(usuarioLogado.getId());
        int totalProdutos = produtoDAO.listarTudo().size();

        resumo.add(criarCardIndicador("Faturamento de hoje", VendaDAO.formatarMoeda(vendasDoUsuario), "Fechamento diario do usuario logado"));
        resumo.add(criarCardIndicador("Comissao de hoje", VendaDAO.formatarMoeda(comissaoDoUsuario), "Estimativa diaria com taxa padrao de 5%"));
        resumo.add(criarCardIndicador(usuarioLogado.isChefia() ? "Loja hoje" : "Vendas de hoje", usuarioLogado.isChefia() ? VendaDAO.formatarMoeda(vendasTotais) : String.valueOf(quantidadeVendas), usuarioLogado.isChefia() ? "Faturamento total do dia da farmacia" : "Quantidade de cupons fechados hoje"));
        resumo.add(criarCardIndicador("Produtos ativos", String.valueOf(totalProdutos), "Catalogo pronto para o caixa"));

        return resumo;
    }

    private JPanel criarPainelAtalhos() {
        JPanel painel = new JPanel(new GridLayout(3, 2, 18, 18));
        painel.setOpaque(false);

        painel.add(criarCardAcao(
                "Caixa / PDV",
                "Tela direta para registrar vendas, montar carrinho e concluir atendimento.",
                "Abrir caixa",
                e -> abrirTela(new VendaView(usuarioLogado))
        ));

        painel.add(criarCardAcao(
                "Caixa de Pagamento",
                "Atalho exclusivo para receber comandas fechadas, conferir itens e finalizar no caixa com F2 e F3.",
                "Abrir caixa",
                e -> abrirTela(new CaixaPagamentoView(usuarioLogado))
        ));

        painel.add(criarCardAcao(
                "Controle de Estoque",
                usuarioLogado.isChefia()
                        ? "Consulta completa com acesso rapido para atualizar estoque."
                        : "Consulta rapida de estoque para apoiar o atendimento no balcao.",
                "Abrir estoque",
                e -> abrirTela(new EstoqueView(usuarioLogado))
        ));

        painel.add(criarCardAcao(
                "Relatorios",
                usuarioLogado.isChefia()
                        ? "Analise vendas totais, desempenho por vendedor e comissoes."
                        : "Veja apenas suas vendas, seu faturamento e sua comissao estimada.",
                "Abrir relatorios",
                e -> abrirTela(new RelatorioFinanceiroView(usuarioLogado))
        ));

        painel.add(criarCardAcao(
                "Operacao",
                usuarioLogado.isChefia()
                        ? "Cadastre clientes, acompanhe a equipe e mantenha a operacao organizada."
                        : "Cadastre clientes, consulte a lista de clientes e mantenha o atendimento mais agil.",
                usuarioLogado.isChefia() ? "Abrir gestao" : "Abrir clientes",
                e -> {
                    if (usuarioLogado.isChefia()) {
                        abrirMenuEquipe();
                    } else {
                        abrirMenuClientes();
                    }
                }
        ));

        return painel;
    }

    private JPanel criarCardIndicador(String titulo, String valor, String descricao) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COR_CARD);
        card.setBorder(criarBordaCard(true));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTitulo.setForeground(new Color(100, 116, 139));

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblValor.setForeground(COR_TITULO);

        JLabel lblDescricao = new JLabel("<html><body style='width:220px'>" + descricao + "</body></html>");
        lblDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDescricao.setForeground(COR_TEXTO);

        JPanel faixa = new JPanel();
        faixa.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
        faixa.setPreferredSize(new Dimension(80, 4));
        faixa.setBackground(COR_AZUL);
        faixa.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(faixa);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(lblTitulo);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(lblValor);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(lblDescricao);
        return card;
    }

    private JPanel criarCardAcao(String titulo, String descricao, String textoBotao, java.awt.event.ActionListener action) {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(COR_CARD);
        card.setBorder(criarBordaCard(false));
        card.setPreferredSize(new Dimension(420, 220));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        JPanel linhaTopo = new JPanel(new BorderLayout());
        linhaTopo.setOpaque(false);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(COR_TITULO);

        JLabel chip = new JLabel(gerarChipSecao(titulo));
        chip.setOpaque(true);
        chip.setBackground(new Color(239, 246, 255));
        chip.setForeground(new Color(30, 64, 175));
        chip.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chip.setBorder(new EmptyBorder(6, 10, 6, 10));

        JLabel lblDescricao = new JLabel("<html><body style='width:340px'>" + descricao + "</body></html>");
        lblDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDescricao.setForeground(COR_TEXTO);

        linhaTopo.add(lblTitulo, BorderLayout.WEST);
        linhaTopo.add(chip, BorderLayout.EAST);

        JPanel faixa = new JPanel();
        faixa.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));
        faixa.setPreferredSize(new Dimension(100, 5));
        faixa.setBackground(COR_AZUL);
        faixa.setAlignmentX(Component.LEFT_ALIGNMENT);

        textos.add(faixa);
        textos.add(Box.createRigidArea(new Dimension(0, 14)));
        textos.add(linhaTopo);
        textos.add(Box.createRigidArea(new Dimension(0, 10)));
        textos.add(lblDescricao);

        JButton botao = new JButton(textoBotao);
        botao.setBackground(COR_AZUL);
        botao.setForeground(Color.WHITE);
        botao.setFocusPainted(false);
        botao.setBorderPainted(false);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 14));
        botao.setBorder(new EmptyBorder(10, 16, 10, 16));
        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                botao.setBackground(COR_AZUL_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(COR_AZUL);
            }
        });
        botao.addActionListener(action);

        card.add(textos, BorderLayout.CENTER);
        card.add(botao, BorderLayout.SOUTH);
        return card;
    }

    private JButton criarBotao(String texto) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 14));
        botao.setForeground(Color.WHITE);
        botao.setBackground(COR_LATERAL_CARD);
        botao.setFocusPainted(false);
        botao.setBorderPainted(false);
        botao.setHorizontalAlignment(SwingConstants.LEFT);
        botao.setBorder(new CompoundBorder(
                new LineBorder(new Color(41, 55, 82), 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (botao.isEnabled()) {
                    botao.setBackground(new Color(37, 99, 235));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(botao.isEnabled() ? COR_LATERAL_CARD : new Color(31, 41, 55));
            }
        });
        return botao;
    }

    private void abrirTela(JFrame frame) {
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    private void abrirMenuClientes() {
        String[] opcoes = {"Cadastrar cliente", "Listar clientes"};
        int escolha = JOptionPane.showOptionDialog(
                this,
                "Escolha uma acao para clientes:",
                "Gestao de Clientes",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                opcoes,
                opcoes[0]
        );

        if (escolha == 0) {
            abrirTela(new CadastroClienteView());
        } else if (escolha == 1) {
            abrirTela(new ListaClientesView());
        }
    }

    private void abrirMenuEquipe() {
        if (!usuarioLogado.isChefia()) {
            JOptionPane.showMessageDialog(this, "Esse modulo e exclusivo para chefia.");
            return;
        }

        String[] opcoes = {"Cadastrar funcionario", "Listar equipe"};
        int escolha = JOptionPane.showOptionDialog(
                this,
                "Escolha uma acao para equipe:",
                "Equipe",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                opcoes,
                opcoes[0]
        );

        if (escolha == 0) {
            abrirTela(new CadastroFuncionarioView());
        } else if (escolha == 1) {
            abrirTela(new GerenciarUsuariosView(usuarioLogado));
        }
    }

    private String montarSubtituloPerfil() {
        if (usuarioLogado.isChefia()) {
            return "Visao de lideranca com faturamento diario, equipe, comissoes e operacao de loja.";
        }
        return "Perfil de atendimento focado em caixa, consulta de estoque e desempenho diario individual.";
    }

    private JLabel criarBadgeCabecalho(String texto, Color corBase) {
        JLabel badge = new JLabel(texto);
        badge.setOpaque(true);
        badge.setBackground(corBase);
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setBorder(new EmptyBorder(8, 12, 8, 12));
        return badge;
    }

    private Border criarBordaCard(boolean compacto) {
        return new CompoundBorder(
                new LineBorder(COR_BORDA, 1, true),
                new EmptyBorder(compacto ? 18 : 22, compacto ? 18 : 22, compacto ? 18 : 22, compacto ? 18 : 22)
        );
    }

    private String gerarChipSecao(String titulo) {
        if (titulo.contains("Caixa")) {
            return "ATENDIMENTO";
        }
        if (titulo.contains("Estoque")) {
            return "OPERACAO";
        }
        if (titulo.contains("Relatorio")) {
            return "ANALISE";
        }
        return "GESTAO";
    }
}
