package com.farmacia.view;

import com.farmacia.dao.ProdutoDAO;
import com.farmacia.model.Produto;
import com.farmacia.model.Usuario;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Locale;

public class EstoqueView extends JFrame {
    private static final Color COR_FUNDO = new Color(236, 242, 248);
    private static final Color COR_CARD = new Color(255, 255, 255);
    private static final Color COR_BORDA = new Color(219, 229, 241);
    private static final Color COR_TITULO = new Color(15, 23, 42);
    private static final Color COR_TEXTO = new Color(71, 85, 105);
    private static final Color COR_AZUL = new Color(14, 165, 233);
    private static final Color COR_AZUL_HOVER = new Color(3, 105, 161);
    private final Usuario usuarioLogado;
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Produto", "Categoria", "Laboratorio", "Controle", "Estoque", "Minimo", "Validade", "Status", "Preco", "Codigo de barras"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private JTextField txtBusca;
    private JComboBox<String> cbFiltro;
    private JLabel lblResumo;
    private JLabel lblCardTotal;
    private JLabel lblCardBaixo;
    private JLabel lblCardValidade;
    private JLabel lblCardControlado;
    private JTable tabela;
    private TableRowSorter<DefaultTableModel> sorter;

    public EstoqueView(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;

        setTitle("Controle de Estoque");
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COR_FUNDO);

        add(criarTopo(), BorderLayout.NORTH);
        add(criarTabela(), BorderLayout.CENTER);
        carregarProdutos();
    }

    private JPanel criarTopo() {
        JPanel container = new JPanel(new BorderLayout(0, 16));
        container.setBackground(Color.WHITE);
        container.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COR_BORDA),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Controle de Estoque");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(COR_TITULO);

        JLabel subtitulo = new JLabel(usuarioLogado.isChefia()
                ? "Leitura operacional com risco visual, controle farmaceutico e filtros rapidos."
                : "Consulta rapida com alertas claros para apoiar a venda no balcao.");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitulo.setForeground(new Color(100, 116, 139));

        textos.add(titulo);
        textos.add(Box.createRigidArea(new Dimension(0, 6)));
        textos.add(subtitulo);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botoes.setOpaque(false);

        JButton btnAtualizar = new JButton("Atualizar lista");
        estilizarBotaoSecundario(btnAtualizar);
        btnAtualizar.addActionListener(e -> carregarProdutos());
        botoes.add(btnAtualizar);

        if (usuarioLogado.isChefia()) {
            JButton btnRecebimento = new JButton("Receber XML");
            estilizarBotaoSecundario(btnRecebimento);
            btnRecebimento.addActionListener(e -> abrirRecebimentoXml());
            botoes.add(btnRecebimento);

            JButton btnEditar = new JButton("Ajustar estoque");
            estilizarBotaoPrimario(btnEditar);
            btnEditar.addActionListener(e -> abrirEdicao());
            botoes.add(btnEditar);
        }

        topo.add(textos, BorderLayout.WEST);
        topo.add(botoes, BorderLayout.EAST);

        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);
        lblCardTotal = criarCard(cards, "Itens");
        lblCardBaixo = criarCard(cards, "Estoque Baixo");
        lblCardValidade = criarCard(cards, "Validade Proxima");
        lblCardControlado = criarCard(cards, "Controlados");

        JPanel filtros = new JPanel(new BorderLayout(12, 8));
        filtros.setOpaque(false);

        txtBusca = new JTextField();
        txtBusca.addActionListener(e -> aplicarFiltros());
        txtBusca.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicarFiltros();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicarFiltros();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aplicarFiltros();
            }
        });

        cbFiltro = new JComboBox<>(new String[]{"Todos", "Estoque baixo", "Validade proxima", "Controlado", "Antibiotico", "Vencido"});
        cbFiltro.addActionListener(e -> aplicarFiltros());

        JButton btnFiltrar = new JButton("Filtrar");
        estilizarBotaoSecundario(btnFiltrar);
        btnFiltrar.addActionListener(e -> aplicarFiltros());

        lblResumo = new JLabel("Carregando resumo...");
        lblResumo.setForeground(COR_TEXTO);

        JPanel esquerda = new JPanel(new GridLayout(1, 3, 12, 0));
        esquerda.setOpaque(false);
        esquerda.add(txtBusca);
        esquerda.add(cbFiltro);
        esquerda.add(btnFiltrar);

        filtros.add(esquerda, BorderLayout.CENTER);
        filtros.add(lblResumo, BorderLayout.SOUTH);

        container.add(topo, BorderLayout.NORTH);
        container.add(cards, BorderLayout.CENTER);
        container.add(filtros, BorderLayout.SOUTH);
        return container;
    }

    private JLabel criarCard(JPanel container, String titulo) {
        JPanel card = new JPanel();
        card.setBackground(COR_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(COR_BORDA, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitulo.setForeground(new Color(100, 116, 139));

        JLabel lblValor = new JLabel("0");
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValor.setForeground(COR_TITULO);

        JPanel faixa = new JPanel();
        faixa.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
        faixa.setPreferredSize(new Dimension(80, 4));
        faixa.setBackground(COR_AZUL);
        faixa.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(faixa);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(lblTitulo);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(lblValor);
        container.add(card);
        return lblValor;
    }

    private JScrollPane criarTabela() {
        tabela = new JTable(modelo);
        tabela.setRowHeight(30);
        tabela.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabela.setGridColor(COR_BORDA);
        tabela.setSelectionBackground(new Color(224, 242, 254));
        tabela.setSelectionForeground(COR_TITULO);
        tabela.setDefaultRenderer(Object.class, new EstoqueCellRenderer());
        sorter = new TableRowSorter<>(modelo);
        tabela.setRowSorter(sorter);
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(new CompoundBorder(
                new EmptyBorder(16, 16, 16, 16),
                new LineBorder(COR_BORDA, 1, true)
        ));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private void carregarProdutos() {
        modelo.setRowCount(0);
        List<Produto> produtos = produtoDAO.listarTudo();

        int estoqueBaixo = 0;
        int validadeProxima = 0;
        int controlados = 0;
        int vencidos = 0;

        for (Produto p : produtos) {
            if (p.isEstoqueBaixo()) {
                estoqueBaixo++;
            }
            if (p.isProximoDoVencimento()) {
                validadeProxima++;
            }
            if (p.diasParaVencimento() < 0) {
                vencidos++;
            }
            if ("Controlado".equalsIgnoreCase(p.getTipoControle())) {
                controlados++;
            }

            modelo.addRow(new Object[]{
                    p.getId(),
                    p.getNome(),
                    p.getCategoria(),
                    p.getLaboratorio(),
                    p.getTipoControle(),
                    p.getQuantidade_estoque(),
                    p.getEstoqueMinimo(),
                    p.getData_validade(),
                    p.getStatusOperacional(),
                    VendaView.formatarMoeda(p.getPreco()),
                    p.getCodigo_barras()
            });
        }

        lblCardTotal.setText(String.valueOf(produtos.size()));
        lblCardBaixo.setText(String.valueOf(estoqueBaixo));
        lblCardValidade.setText(String.valueOf(validadeProxima));
        lblCardControlado.setText(String.valueOf(controlados));

        lblResumo.setText("Itens: " + produtos.size()
                + " | Estoque baixo: " + estoqueBaixo
                + " | Validade proxima: " + validadeProxima
                + " | Vencidos: " + vencidos
                + " | Controlados: " + controlados);
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        String busca = txtBusca == null ? "" : txtBusca.getText().trim().toLowerCase(Locale.ROOT);
        String filtro = cbFiltro == null ? "Todos" : String.valueOf(cbFiltro.getSelectedItem());

        sorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String produto = String.valueOf(entry.getValue(1)).toLowerCase(Locale.ROOT);
                String categoria = String.valueOf(entry.getValue(2)).toLowerCase(Locale.ROOT);
                String laboratorio = String.valueOf(entry.getValue(3)).toLowerCase(Locale.ROOT);
                String controle = String.valueOf(entry.getValue(4)).toLowerCase(Locale.ROOT);
                String status = String.valueOf(entry.getValue(8)).toLowerCase(Locale.ROOT);

                boolean textoOk = busca.isBlank()
                        || produto.contains(busca)
                        || categoria.contains(busca)
                        || laboratorio.contains(busca)
                        || controle.contains(busca);

                if (!textoOk) {
                    return false;
                }

                return switch (filtro) {
                    case "Estoque baixo" -> status.contains("estoque baixo");
                    case "Validade proxima" -> status.contains("validade proxima") || status.contains("vence logo");
                    case "Controlado" -> controle.equals("controlado");
                    case "Antibiotico" -> controle.equals("antibiotico");
                    case "Vencido" -> status.equals("vencido");
                    default -> true;
                };
            }
        });
    }

    private void abrirEdicao() {
        EditarEstoqueView view = new EditarEstoqueView();
        view.setVisible(true);
    }

    private void abrirRecebimentoXml() {
        RecebimentoEstoqueXmlView view = new RecebimentoEstoqueXmlView();
        view.setVisible(true);
    }

    private class EstoqueCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (isSelected) {
                component.setBackground(new Color(37, 99, 235));
                component.setForeground(Color.WHITE);
                return component;
            }

            int modelRow = table.convertRowIndexToModel(row);
            String status = String.valueOf(modelo.getValueAt(modelRow, 8));
            String controle = String.valueOf(modelo.getValueAt(modelRow, 4));

            component.setForeground(new Color(15, 23, 42));

            if ("Vencido".equalsIgnoreCase(status)) {
                component.setBackground(new Color(254, 226, 226));
            } else if (status.contains("Estoque baixo") && status.contains("vence logo")) {
                component.setBackground(new Color(254, 240, 138));
            } else if (status.contains("Estoque baixo")) {
                component.setBackground(new Color(255, 237, 213));
            } else if (status.contains("Validade proxima")) {
                component.setBackground(new Color(254, 249, 195));
            } else if ("Controlado".equalsIgnoreCase(controle)) {
                component.setBackground(new Color(237, 233, 254));
            } else {
                component.setBackground(Color.WHITE);
            }

            return component;
        }
    }

    private void estilizarBotaoPrimario(JButton botao) {
        botao.setBackground(COR_AZUL);
        botao.setForeground(Color.WHITE);
        botao.setFocusPainted(false);
        botao.setBorderPainted(false);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 12));
        botao.setBorder(new EmptyBorder(10, 14, 10, 14));
        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (botao.isEnabled()) {
                    botao.setBackground(COR_AZUL_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(COR_AZUL);
            }
        });
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
}
