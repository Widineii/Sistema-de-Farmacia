package com.farmacia.view;

import com.farmacia.dao.ComandaCaixaDAO;
import com.farmacia.dao.UsuarioDAO;
import com.farmacia.dao.VendaDAO;
import com.farmacia.model.ComandaCaixa;
import com.farmacia.model.Usuario;
import com.farmacia.model.Venda;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RelatorioFinanceiroView extends JFrame {
    private static final Color COR_FUNDO = new Color(236, 242, 248);
    private static final Color COR_CARD = new Color(255, 255, 255);
    private static final Color COR_BORDA = new Color(219, 229, 241);
    private static final Color COR_TITULO = new Color(15, 23, 42);
    private static final Color COR_TEXTO = new Color(71, 85, 105);
    private static final Color COR_AZUL = new Color(14, 165, 233);
    private final Usuario usuarioLogado;
    private final VendaDAO vendaDAO = new VendaDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ComandaCaixaDAO comandaDAO = new ComandaCaixaDAO();

    private JComboBox<String> cbModo;
    private JComboBox<UsuarioItem> cbUsuarios;
    private JButton btnData;
    private JLabel lblTituloContexto;
    private JPanel painelCards;
    private JPanel painelTabelaEquipe;
    private JScrollPane scrollVendas;
    private LocalDate dataSelecionada = LocalDate.now();
    private CompactCalendarPicker calendarioPicker;
    private final DefaultTableModel modeloHistorico = new DefaultTableModel(
            new String[]{"Data", "Comanda", "Tipo", "Vendedor", "Cliente", "Itens", "Total", "Receita"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private JTable tabelaHistorico;
    private JTextArea txtDetalheHistorico;
    private JTextField txtBuscaHistorico;
    private JCheckBox chkApenasReceita;
    private TableRowSorter<DefaultTableModel> sorterHistorico;
    private final List<ComandaCaixa> historicoComandas = new ArrayList<>();

    public RelatorioFinanceiroView(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;

        setTitle("Relatorio Diario de Vendas");
        setSize(1220, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COR_FUNDO);

        add(criarAbas(), BorderLayout.CENTER);
        atualizarEstadoFiltros();
        atualizarRelatorio();
        carregarHistorico();
    }

    private JComponent criarAbas() {
        JTabbedPane abas = new JTabbedPane();
        abas.setFont(new Font("Segoe UI", Font.BOLD, 13));
        abas.setBackground(Color.WHITE);
        abas.addTab("Resumo diario", criarAbaResumo());
        abas.addTab("Historico com receita", criarAbaHistorico());
        return abas;
    }

    private JComponent criarAbaResumo() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.add(criarCabecalho(), BorderLayout.NORTH);
        painel.add(criarConteudo(), BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarCabecalho() {
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(Color.WHITE);
        topo.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COR_BORDA),
                new EmptyBorder(20, 22, 20, 22)
        ));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Relatorio diario da farmacia");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titulo.setForeground(COR_TITULO);

        lblTituloContexto = new JLabel();
        lblTituloContexto.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTituloContexto.setForeground(new Color(100, 116, 139));

        textos.add(titulo);
        textos.add(Box.createRigidArea(new Dimension(0, 6)));
        textos.add(lblTituloContexto);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filtros.setOpaque(false);

        cbModo = new JComboBox<>(usuarioLogado.isChefia()
                ? new String[]{"Venda total da loja", "Venda por usuario", "Entrega por usuario"}
                : new String[]{"Venda por usuario"});
        cbModo.addActionListener(e -> atualizarEstadoFiltros());

        btnData = new JButton();
        btnData.addActionListener(e -> abrirCalendario());
        btnData.setFocusPainted(false);
        estilizarBotaoSecundario(btnData);

        JButton btnHoje = new JButton("Hoje");
        estilizarBotaoSecundario(btnHoje);
        btnHoje.addActionListener(e -> {
            dataSelecionada = LocalDate.now();
            atualizarTextoData();
            atualizarRelatorio();
        });

        JButton btnAplicar = new JButton("Atualizar");
        estilizarBotaoPrimario(btnAplicar);
        btnAplicar.addActionListener(e -> atualizarRelatorio());

        cbUsuarios = new JComboBox<>();
        popularUsuarios();
        cbUsuarios.addActionListener(e -> {
            if (cbUsuarios.isShowing()) {
                atualizarRelatorio();
            }
        });

        filtros.add(new JLabel("Selecao:"));
        filtros.add(cbModo);
        filtros.add(new JLabel("Data:"));
        filtros.add(btnData);
        filtros.add(cbUsuarios);
        filtros.add(btnHoje);
        filtros.add(btnAplicar);

        topo.add(textos, BorderLayout.WEST);
        topo.add(filtros, BorderLayout.EAST);
        atualizarTextoData();
        return topo;
    }

    private JComponent criarConteudo() {
        JPanel painel = new JPanel(new BorderLayout(0, 18));
        painel.setBackground(COR_FUNDO);
        painel.setBorder(new EmptyBorder(18, 22, 22, 22));

        painelCards = new JPanel(new GridLayout(1, 4, 16, 0));
        painelCards.setOpaque(false);

        painelTabelaEquipe = new JPanel(new BorderLayout());
        painelTabelaEquipe.setOpaque(false);

        scrollVendas = new JScrollPane();

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, painelTabelaEquipe, scrollVendas);
        split.setResizeWeight(0.36);
        split.setBorder(null);
        split.setOpaque(false);

        painel.add(painelCards, BorderLayout.NORTH);
        painel.add(split, BorderLayout.CENTER);
        return painel;
    }

    private JComponent criarAbaHistorico() {
        JPanel painel = new JPanel(new BorderLayout(0, 16));
        painel.setBackground(COR_FUNDO);

        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(Color.WHITE);
        cabecalho.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COR_BORDA),
                new EmptyBorder(20, 22, 20, 22)
        ));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Historico de vendas com receita");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titulo.setForeground(COR_TITULO);

        JLabel subtitulo = new JLabel("Todos os perfis podem consultar vendas pagas, detalhes da receita e medicamentos vendidos.");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitulo.setForeground(new Color(100, 116, 139));

        textos.add(titulo);
        textos.add(Box.createRigidArea(new Dimension(0, 6)));
        textos.add(subtitulo);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filtros.setOpaque(false);

        txtBuscaHistorico = new JTextField(22);
        txtBuscaHistorico.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicarFiltroHistorico();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicarFiltroHistorico();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aplicarFiltroHistorico();
            }
        });

        chkApenasReceita = new JCheckBox("Apenas com receita");
        chkApenasReceita.setOpaque(false);
        chkApenasReceita.addActionListener(e -> aplicarFiltroHistorico());

        JButton btnAtualizarHistorico = new JButton("Atualizar historico");
        estilizarBotaoPrimario(btnAtualizarHistorico);
        btnAtualizarHistorico.addActionListener(e -> carregarHistorico());

        filtros.add(new JLabel("Buscar:"));
        filtros.add(txtBuscaHistorico);
        filtros.add(chkApenasReceita);
        filtros.add(btnAtualizarHistorico);

        cabecalho.add(textos, BorderLayout.WEST);
        cabecalho.add(filtros, BorderLayout.EAST);

        tabelaHistorico = new JTable(modeloHistorico);
        tabelaHistorico.setRowHeight(28);
        tabelaHistorico.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelaHistorico.setGridColor(COR_BORDA);
        tabelaHistorico.setSelectionBackground(new Color(224, 242, 254));
        tabelaHistorico.setSelectionForeground(COR_TITULO);
        sorterHistorico = new TableRowSorter<>(modeloHistorico);
        tabelaHistorico.setRowSorter(sorterHistorico);
        tabelaHistorico.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String texto = value == null ? "" : value.toString();
                if (isSelected) {
                    comp.setBackground(table.getSelectionBackground());
                    comp.setForeground(table.getSelectionForeground());
                    return comp;
                }
                if ("Sim".equalsIgnoreCase(texto)) {
                    comp.setBackground(new Color(237, 233, 254));
                    comp.setForeground(new Color(91, 33, 182));
                } else {
                    comp.setBackground(new Color(248, 250, 252));
                    comp.setForeground(new Color(100, 116, 139));
                }
                return comp;
            }
        });
        tabelaHistorico.getSelectionModel().addListSelectionListener(e -> atualizarDetalheHistorico());

        txtDetalheHistorico = new JTextArea();
        txtDetalheHistorico.setEditable(false);
        txtDetalheHistorico.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDetalheHistorico.setLineWrap(true);
        txtDetalheHistorico.setWrapStyleWord(true);
        txtDetalheHistorico.setBorder(new EmptyBorder(14, 14, 14, 14));
        txtDetalheHistorico.setBackground(Color.WHITE);
        txtDetalheHistorico.setForeground(COR_TITULO);

        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                criarScrollTabela(tabelaHistorico),
                criarScrollTexto(txtDetalheHistorico)
        );
        split.setResizeWeight(0.55);
        split.setBorder(null);

        JPanel corpo = new JPanel(new BorderLayout());
        corpo.setOpaque(false);
        corpo.setBorder(new EmptyBorder(18, 22, 22, 22));
        corpo.add(split, BorderLayout.CENTER);

        painel.add(cabecalho, BorderLayout.NORTH);
        painel.add(corpo, BorderLayout.CENTER);
        return painel;
    }

    private void popularUsuarios() {
        cbUsuarios.removeAllItems();
        if (!usuarioLogado.isChefia()) {
            cbUsuarios.addItem(new UsuarioItem(usuarioLogado));
            cbUsuarios.setSelectedIndex(0);
            return;
        }

        for (Usuario usuario : usuarioDAO.listarTodos()) {
            if ("Atendente".equalsIgnoreCase(usuario.getCargo())
                    || "Chefe".equalsIgnoreCase(usuario.getCargo())
                    || "Administrador".equalsIgnoreCase(usuario.getCargo())) {
                cbUsuarios.addItem(new UsuarioItem(usuario));
            }
        }
    }

    private void atualizarEstadoFiltros() {
        String modo = String.valueOf(cbModo.getSelectedItem());
        boolean mostrarUsuario = usuarioLogado.isChefia()
                && ("Venda por usuario".equals(modo) || "Entrega por usuario".equals(modo));
        cbUsuarios.setVisible(mostrarUsuario);
    }

    private void atualizarRelatorio() {
        String modo = String.valueOf(cbModo.getSelectedItem());
        Integer usuarioSelecionado = null;
        String tipoVenda = null;

        if (!usuarioLogado.isChefia()) {
            usuarioSelecionado = usuarioLogado.getId();
            modo = "Venda por usuario";
        } else if ("Venda por usuario".equals(modo)) {
            UsuarioItem usuarioItem = (UsuarioItem) cbUsuarios.getSelectedItem();
            usuarioSelecionado = usuarioItem == null ? null : usuarioItem.usuario.getId();
        } else if ("Entrega por usuario".equals(modo)) {
            UsuarioItem usuarioItem = (UsuarioItem) cbUsuarios.getSelectedItem();
            usuarioSelecionado = usuarioItem == null ? null : usuarioItem.usuario.getId();
            tipoVenda = "Entrega";
        }

        lblTituloContexto.setText(montarSubtitulo(dataSelecionada, modo, usuarioSelecionado));
        atualizarCards(dataSelecionada, usuarioSelecionado, tipoVenda);
        atualizarTabelaEquipe(dataSelecionada, tipoVenda);
        atualizarTabelaVendas(dataSelecionada, usuarioSelecionado, tipoVenda, modo);
    }

    private void atualizarCards(LocalDate dataSelecionada, Integer usuarioSelecionado, String tipoVenda) {
        painelCards.removeAll();

        double faturamento = vendaDAO.buscarFaturamentoFiltrado(dataSelecionada, usuarioSelecionado, tipoVenda);
        double comissao = vendaDAO.buscarComissaoFiltrada(dataSelecionada, usuarioSelecionado, tipoVenda);
        int quantidade = vendaDAO.contarVendasFiltradas(dataSelecionada, usuarioSelecionado, tipoVenda);
        String perfil = tipoVenda != null ? "Entrega" : (usuarioSelecionado != null ? "Usuario" : "Equipe");

        painelCards.add(criarCard("Faturamento do dia", VendaDAO.formatarMoeda(faturamento), "Total da selecao para a data escolhida"));
        painelCards.add(criarCard("Comissao do dia", VendaDAO.formatarMoeda(comissao), "Comissao estimada no mesmo recorte"));
        painelCards.add(criarCard("Quantidade de vendas", String.valueOf(quantidade), "Cupons registrados na data filtrada"));
        painelCards.add(criarCard("Visualizacao", perfil, "Modo atual do relatorio"));

        painelCards.revalidate();
        painelCards.repaint();
    }

    private void atualizarTabelaEquipe(LocalDate dataSelecionada, String tipoVenda) {
        painelTabelaEquipe.removeAll();

        if (!usuarioLogado.isChefia()) {
            painelTabelaEquipe.revalidate();
            painelTabelaEquipe.repaint();
            return;
        }

        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"Vendedor", "Qtde vendas", "Faturamento", "Comissao"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Object[] linha : vendaDAO.listarResumoPorVendedorFiltrado(dataSelecionada, tipoVenda)) {
            modelo.addRow(linha);
        }

        JTable tabela = new JTable(modelo);
        tabela.setRowHeight(28);
        tabela.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabela.setGridColor(COR_BORDA);
        tabela.setSelectionBackground(new Color(224, 242, 254));
        tabela.setSelectionForeground(COR_TITULO);

        JScrollPane scroll = criarScrollTabela(tabela);
        scroll.setBorder(BorderFactory.createTitledBorder("Total da equipe no dia"));
        painelTabelaEquipe.add(scroll, BorderLayout.CENTER);
        painelTabelaEquipe.revalidate();
        painelTabelaEquipe.repaint();
    }

    private void atualizarTabelaVendas(LocalDate dataSelecionada, Integer usuarioSelecionado, String tipoVenda, String modo) {
        List<Venda> vendas = vendaDAO.listarFiltradas(dataSelecionada, usuarioSelecionado, tipoVenda);
        boolean mostrarVendedor = usuarioLogado.isChefia() && usuarioSelecionado == null;

        String[] colunas = mostrarVendedor
                ? new String[]{"Data", "Tipo", "Vendedor", "Itens", "Qtde", "Total", "Comissao"}
                : new String[]{"Data", "Tipo", "Itens", "Qtde", "Total", "Comissao"};

        DefaultTableModel modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Venda venda : vendas) {
            if (mostrarVendedor) {
                modelo.addRow(new Object[]{
                        venda.getDataVenda(),
                        valorOuPadrao(venda.getTipoVenda(), "Balcao"),
                        venda.getUsuarioNome(),
                        venda.getItensResumo(),
                        venda.getQuantidadeItens(),
                        VendaDAO.formatarMoeda(venda.getValorTotal()),
                        VendaDAO.formatarMoeda(venda.getComissao())
                });
            } else {
                modelo.addRow(new Object[]{
                        venda.getDataVenda(),
                        valorOuPadrao(venda.getTipoVenda(), "Balcao"),
                        venda.getItensResumo(),
                        venda.getQuantidadeItens(),
                        VendaDAO.formatarMoeda(venda.getValorTotal()),
                        VendaDAO.formatarMoeda(venda.getComissao())
                });
            }
        }

        JTable tabela = new JTable(modelo);
        tabela.setRowHeight(28);
        tabela.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabela.setGridColor(COR_BORDA);
        tabela.setSelectionBackground(new Color(224, 242, 254));
        tabela.setSelectionForeground(COR_TITULO);

        JScrollPane scroll = criarScrollTabela(tabela);
        scroll.setBorder(BorderFactory.createTitledBorder("Detalhamento - " + modo));
        scrollVendas.setViewportView(tabela);
        scrollVendas.setBorder(scroll.getBorder());
    }

    private JPanel criarCard(String titulo, String valor, String descricao) {
        JPanel card = new JPanel();
        card.setBackground(COR_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(COR_BORDA, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTitulo.setForeground(new Color(100, 116, 139));

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 24));
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

    private JScrollPane criarScrollTabela(JTable tabela) {
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(new LineBorder(COR_BORDA, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private JScrollPane criarScrollTexto(JTextArea area) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(new LineBorder(COR_BORDA, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private void estilizarBotaoPrimario(JButton botao) {
        botao.setBackground(COR_AZUL);
        botao.setForeground(Color.WHITE);
        botao.setFocusPainted(false);
        botao.setBorderPainted(false);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 12));
        botao.setBorder(new EmptyBorder(9, 14, 9, 14));
    }

    private void estilizarBotaoSecundario(JButton botao) {
        botao.setBackground(new Color(248, 250, 252));
        botao.setForeground(COR_TITULO);
        botao.setFocusPainted(false);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 12));
        botao.setBorder(new CompoundBorder(
                new LineBorder(COR_BORDA, 1, true),
                new EmptyBorder(9, 14, 9, 14)
        ));
    }

    private String montarSubtitulo(LocalDate dataSelecionada, String modo, Integer usuarioSelecionado) {
        String dataFormatada = dataSelecionada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        if (!usuarioLogado.isChefia()) {
            return "Resumo diario das suas vendas em " + dataFormatada + ".";
        }
        if ("Venda por usuario".equals(modo) && usuarioSelecionado != null) {
            UsuarioItem item = (UsuarioItem) cbUsuarios.getSelectedItem();
            return "Performance individual em " + dataFormatada + " para " + (item == null ? "usuario selecionado" : item.usuario.getNome()) + ".";
        }
        if ("Entrega por usuario".equals(modo) && usuarioSelecionado != null) {
            UsuarioItem item = (UsuarioItem) cbUsuarios.getSelectedItem();
            return "Somente entregas de " + (item == null ? "usuario selecionado" : item.usuario.getNome()) + " em " + dataFormatada + ".";
        }
        return "Venda total da loja no dia " + dataFormatada + ", com comparacao rapida por vendedor.";
    }

    private String valorOuPadrao(String valor, String padrao) {
        return valor == null || valor.isBlank() ? padrao : valor;
    }

    private void carregarHistorico() {
        historicoComandas.clear();
        historicoComandas.addAll(comandaDAO.listarHistoricoPagas());
        modeloHistorico.setRowCount(0);

        for (ComandaCaixa comanda : historicoComandas) {
            boolean temReceita = possuiReceita(comanda);
            modeloHistorico.addRow(new Object[]{
                    valorOuPadrao(comanda.getDataAbertura(), "-"),
                    comanda.getComanda(),
                    valorOuPadrao(comanda.getTipoVenda(), "Balcao"),
                    valorOuPadrao(comanda.getUsuarioNome(), "-"),
                    valorOuPadrao(comanda.getClienteNome(), "Venda sem cadastro"),
                    comanda.getQuantidadeItens(),
                    VendaDAO.formatarMoeda(comanda.getTotal()),
                    temReceita ? "Sim" : "Nao"
            });
        }

        aplicarFiltroHistorico();
        if (modeloHistorico.getRowCount() > 0) {
            tabelaHistorico.setRowSelectionInterval(0, 0);
            atualizarDetalheHistorico();
        } else if (txtDetalheHistorico != null) {
            txtDetalheHistorico.setText("Nenhuma venda paga encontrada no historico.");
        }
    }

    private void aplicarFiltroHistorico() {
        if (sorterHistorico == null) {
            return;
        }
        String termo = normalizar(txtBuscaHistorico == null ? "" : txtBuscaHistorico.getText());
        boolean apenasReceita = chkApenasReceita != null && chkApenasReceita.isSelected();

        sorterHistorico.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                int row = entry.getIdentifier();
                ComandaCaixa comanda = historicoComandas.get(row);
                boolean temReceita = possuiReceita(comanda);
                if (apenasReceita && !temReceita) {
                    return false;
                }
                if (termo.isBlank()) {
                    return true;
                }
                String alvo = normalizar(
                        valorOuPadrao(comanda.getComanda(), "") + " "
                                + valorOuPadrao(comanda.getUsuarioNome(), "") + " "
                                + valorOuPadrao(comanda.getClienteNome(), "") + " "
                                + valorOuPadrao(comanda.getItensResumo(), "") + " "
                                + valorOuPadrao(comanda.getItensDetalhe(), "") + " "
                                + valorOuPadrao(comanda.getTipoVenda(), "")
                );
                return alvo.contains(termo);
            }
        });
    }

    private void atualizarDetalheHistorico() {
        if (tabelaHistorico == null || txtDetalheHistorico == null) {
            return;
        }
        int linha = tabelaHistorico.getSelectedRow();
        if (linha < 0) {
            txtDetalheHistorico.setText("Selecione uma venda para ver os detalhes.");
            return;
        }
        int linhaModelo = tabelaHistorico.convertRowIndexToModel(linha);
        if (linhaModelo < 0 || linhaModelo >= historicoComandas.size()) {
            txtDetalheHistorico.setText("Selecione uma venda para ver os detalhes.");
            return;
        }

        ComandaCaixa comanda = historicoComandas.get(linhaModelo);
        StringBuilder detalhe = new StringBuilder();
        detalhe.append("Comanda: ").append(valorOuPadrao(comanda.getComanda(), "-"))
                .append("\nData: ").append(valorOuPadrao(comanda.getDataAbertura(), "-"))
                .append("\nStatus: ").append(valorOuPadrao(comanda.getStatus(), "-"))
                .append("\nTipo: ").append(valorOuPadrao(comanda.getTipoVenda(), "Balcao"))
                .append("\nVendedor: ").append(valorOuPadrao(comanda.getUsuarioNome(), "-"))
                .append("\nCliente: ").append(valorOuPadrao(comanda.getClienteNome(), "Venda sem cadastro"))
                .append("\nCPF cliente: ").append(valorOuPadrao(comanda.getClienteCpf(), "-"))
                .append("\nItens: ").append(comanda.getQuantidadeItens())
                .append("\nTotal: ").append(VendaDAO.formatarMoeda(comanda.getTotal()))
                .append("\nDesconto: ").append(VendaDAO.formatarMoeda(comanda.getDescontoTotal()))
                .append("\nComissao: ").append(VendaDAO.formatarMoeda(comanda.getComissao()))
                .append("\nReceita: ").append(possuiReceita(comanda) ? "Sim" : "Nao")
                .append("\n\nDetalhes da venda:\n")
                .append(valorOuPadrao(comanda.getItensDetalhe(), valorOuPadrao(comanda.getItensResumo(), "Sem detalhes.")));

        txtDetalheHistorico.setText(detalhe.toString());
        txtDetalheHistorico.setCaretPosition(0);
    }

    private boolean possuiReceita(ComandaCaixa comanda) {
        String detalhe = valorOuPadrao(comanda.getItensDetalhe(), "");
        return detalhe.toLowerCase(Locale.ROOT).contains("receita controlada");
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.toLowerCase(Locale.ROOT).trim();
    }

    private void abrirCalendario() {
        if (calendarioPicker == null) {
            calendarioPicker = new CompactCalendarPicker();
        }
        calendarioPicker.abrir(btnData, dataSelecionada);
    }

    private void atualizarTextoData() {
        btnData.setText(dataSelecionada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    private static class UsuarioItem {
        private final Usuario usuario;

        private UsuarioItem(Usuario usuario) {
            this.usuario = usuario;
        }

        @Override
        public String toString() {
            return usuario.getNome() + " | " + usuario.getCargoExibicao();
        }
    }

    private class CompactCalendarPicker {
        private final JPopupMenu popup = new JPopupMenu();
        private final JLabel lblMesAno = new JLabel("", SwingConstants.CENTER);
        private final JPanel painelDias = new JPanel(new GridLayout(7, 7, 4, 4));
        private YearMonth mesAtual;

        private CompactCalendarPicker() {
            JPanel conteudo = new JPanel(new BorderLayout(0, 10));
            conteudo.setBorder(new EmptyBorder(12, 12, 12, 12));
            conteudo.setBackground(Color.WHITE);

            JPanel topo = new JPanel(new BorderLayout());
            topo.setOpaque(false);

            JButton btnAnterior = new JButton("<");
            JButton btnProximo = new JButton(">");
            estilizarBotaoCalendario(btnAnterior);
            estilizarBotaoCalendario(btnProximo);

            btnAnterior.addActionListener(e -> {
                mesAtual = mesAtual.minusMonths(1);
                renderizarDias();
            });
            btnProximo.addActionListener(e -> {
                mesAtual = mesAtual.plusMonths(1);
                renderizarDias();
            });

            lblMesAno.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblMesAno.setForeground(new Color(15, 23, 42));

            topo.add(btnAnterior, BorderLayout.WEST);
            topo.add(lblMesAno, BorderLayout.CENTER);
            topo.add(btnProximo, BorderLayout.EAST);

            painelDias.setOpaque(false);
            conteudo.add(topo, BorderLayout.NORTH);
            conteudo.add(painelDias, BorderLayout.CENTER);

            JPanel rodape = new JPanel(new BorderLayout());
            rodape.setOpaque(false);
            JButton btnLimpar = new JButton("Hoje");
            estilizarBotaoCalendario(btnLimpar);
            btnLimpar.addActionListener(e -> {
                dataSelecionada = LocalDate.now();
                mesAtual = YearMonth.from(dataSelecionada);
                atualizarTextoData();
                popup.setVisible(false);
                atualizarRelatorio();
            });
            rodape.add(btnLimpar, BorderLayout.EAST);

            conteudo.add(rodape, BorderLayout.SOUTH);
            popup.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
            popup.add(conteudo);
        }

        private void abrir(Component origem, LocalDate dataBase) {
            mesAtual = YearMonth.from(dataBase);
            renderizarDias();
            popup.show(origem, 0, origem.getHeight());
        }

        private void renderizarDias() {
            painelDias.removeAll();
            Locale locale = new Locale("pt", "BR");
            lblMesAno.setText(mesAtual.getMonth().getDisplayName(TextStyle.FULL, locale) + " de " + mesAtual.getYear());

            String[] cabecalho = {"D", "S", "T", "Q", "Q", "S", "S"};
            for (String dia : cabecalho) {
                JLabel label = new JLabel(dia, SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 11));
                label.setForeground(new Color(100, 116, 139));
                painelDias.add(label);
            }

            LocalDate primeiroDia = mesAtual.atDay(1);
            int deslocamento = primeiroDia.getDayOfWeek().getValue() % 7;
            for (int i = 0; i < deslocamento; i++) {
                painelDias.add(new JLabel(""));
            }

            for (int dia = 1; dia <= mesAtual.lengthOfMonth(); dia++) {
                LocalDate data = mesAtual.atDay(dia);
                JButton botaoDia = new JButton(String.valueOf(dia));
                botaoDia.setMargin(new Insets(6, 0, 6, 0));
                botaoDia.setFocusPainted(false);
                botaoDia.setBorderPainted(false);
                botaoDia.setOpaque(true);
                botaoDia.setBackground(data.equals(dataSelecionada) ? new Color(71, 85, 105) : Color.WHITE);
                botaoDia.setForeground(data.equals(dataSelecionada) ? Color.WHITE : new Color(15, 23, 42));
                botaoDia.addActionListener(e -> {
                    dataSelecionada = data;
                    atualizarTextoData();
                    popup.setVisible(false);
                    atualizarRelatorio();
                });
                painelDias.add(botaoDia);
            }

            int totalCelulas = 7 + deslocamento + mesAtual.lengthOfMonth();
            while (totalCelulas < 49) {
                painelDias.add(new JLabel(""));
                totalCelulas++;
            }

            painelDias.revalidate();
            painelDias.repaint();
        }

        private void estilizarBotaoCalendario(JButton botao) {
            botao.setFocusPainted(false);
            botao.setBorderPainted(false);
            botao.setBackground(Color.WHITE);
            botao.setForeground(new Color(37, 99, 235));
        }
    }
}
