package com.farmacia.view;

import com.farmacia.dao.ComandaCaixaDAO;
import com.farmacia.dao.VendaDAO;
import com.farmacia.model.ComandaCaixa;
import com.farmacia.model.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CaixaPagamentoView extends JFrame {
    private final Usuario usuarioLogado;
    private final ComandaCaixaDAO comandaDAO = new ComandaCaixaDAO();
    private final VendaDAO vendaDAO = new VendaDAO();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Comanda", "Tipo", "Status", "Atendente", "Cliente", "Itens", "Total"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private JTable tabela;
    private JTextArea txtDetalhes;
    private JLabel lblComanda;
    private JLabel lblTotal;
    private JTextField txtBuscaComanda;
    private ComandaCaixa comandaSelecionada;

    public CaixaPagamentoView(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;

        setTitle("Caixa de Pagamento");
        setSize(1180, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(16, 16));
        getContentPane().setBackground(new Color(241, 245, 249));

        add(criarCabecalho(), BorderLayout.NORTH);
        add(criarConteudo(), BorderLayout.CENTER);
        configurarAtalhos();
        carregarComandas();
    }

    private JPanel criarCabecalho() {
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(Color.WHITE);
        topo.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Caixa de Pagamento");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titulo.setForeground(new Color(15, 23, 42));

        JLabel subtitulo = new JLabel("F2 abre ou atualiza as comandas. F3 avanca o fluxo da comanda selecionada.");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitulo.setForeground(new Color(100, 116, 139));

        textos.add(titulo);
        textos.add(Box.createRigidArea(new Dimension(0, 6)));
        textos.add(subtitulo);

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        direita.setOpaque(false);

        txtBuscaComanda = new JTextField(8);
        ((AbstractDocument) txtBuscaComanda.getDocument()).setDocumentFilter(new ApenasTresDigitosFilter());
        txtBuscaComanda.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                buscarComandaDigitada();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                buscarComandaDigitada();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                buscarComandaDigitada();
            }
        });
        txtBuscaComanda.addActionListener(e -> buscarComandaDigitada());

        JButton btnAtualizar = new JButton("Atualizar comandas");
        btnAtualizar.addActionListener(e -> abrirComandasPendentes());

        topo.add(textos, BorderLayout.WEST);
        direita.add(new JLabel("Comanda:"));
        direita.add(txtBuscaComanda);
        direita.add(btnAtualizar);
        topo.add(direita, BorderLayout.EAST);
        return topo;
    }

    private JComponent criarConteudo() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, criarListaComandas(), criarPainelDetalhes());
        split.setResizeWeight(0.55);
        split.setBorder(null);
        return split;
    }

    private JComponent criarListaComandas() {
        JPanel painel = new JPanel(new BorderLayout(0, 12));
        painel.setBackground(Color.WHITE);
        painel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel titulo = new JLabel("Comandas pendentes");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));

        tabela = new JTable(modelo);
        tabela.setRowHeight(28);
        tabela.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabela.getSelectionModel().addListSelectionListener(e -> selecionarLinhaAtual());

        painel.add(titulo, BorderLayout.NORTH);
        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        return painel;
    }

    private JComponent criarPainelDetalhes() {
        JPanel painel = new JPanel(new BorderLayout(0, 12));
        painel.setBackground(Color.WHITE);
        painel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel topo = new JPanel(new GridLayout(2, 1, 0, 4));
        topo.setOpaque(false);
        lblComanda = new JLabel("Comanda: nenhuma selecionada");
        lblComanda.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTotal = new JLabel("Total: R$ 0,00");
        lblTotal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTotal.setForeground(new Color(71, 85, 105));
        topo.add(lblComanda);
        topo.add(lblTotal);

        txtDetalhes = new JTextArea();
        txtDetalhes.setEditable(false);
        txtDetalhes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDetalhes.setLineWrap(true);
        txtDetalhes.setWrapStyleWord(true);

        JButton btnFinalizar = new JButton("Avancar comanda (F3)");
        btnFinalizar.addActionListener(e -> finalizarComandaSelecionada());

        painel.add(topo, BorderLayout.NORTH);
        painel.add(new JScrollPane(txtDetalhes), BorderLayout.CENTER);
        painel.add(btnFinalizar, BorderLayout.SOUTH);
        return painel;
    }

    private void configurarAtalhos() {
        JRootPane rootPane = getRootPane();
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "abrirComandas");
        rootPane.getActionMap().put("abrirComandas", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                abrirComandasPendentes();
            }
        });

        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "finalizarComanda");
        rootPane.getActionMap().put("finalizarComanda", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                finalizarComandaSelecionada();
            }
        });
    }

    private void abrirComandasPendentes() {
        carregarComandas();
        txtBuscaComanda.requestFocusInWindow();
        if (modelo.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Nao ha comandas abertas no momento.");
        }
    }

    private void carregarComandas() {
        modelo.setRowCount(0);
        List<ComandaCaixa> comandas = comandaDAO.listarPendentesCaixa();
        for (ComandaCaixa comanda : comandas) {
            modelo.addRow(new Object[]{
                    comanda.getComanda(),
                    comanda.getTipoVenda(),
                    comanda.getStatus(),
                    comanda.getUsuarioNome(),
                    comanda.getClienteNome(),
                    comanda.getQuantidadeItens(),
                    VendaDAO.formatarMoeda(comanda.getTotal())
            });
        }
        if (modelo.getRowCount() > 0) {
            tabela.setRowSelectionInterval(0, 0);
            selecionarLinhaAtual();
        } else {
            comandaSelecionada = null;
            lblComanda.setText("Comanda: nenhuma selecionada");
            lblTotal.setText("Total: R$ 0,00");
            txtDetalhes.setText("");
        }
    }

    private void buscarComandaDigitada() {
        String comanda = normalizarComanda(txtBuscaComanda.getText());
        if (comanda == null) {
            return;
        }

        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (comanda.equals(String.valueOf(modelo.getValueAt(i, 0)))) {
                tabela.setRowSelectionInterval(i, i);
                tabela.scrollRectToVisible(tabela.getCellRect(i, 0, true));
                selecionarLinhaAtual();
                return;
            }
        }

        ComandaCaixa encontrada = comandaDAO.buscarPorComanda(comanda);
        if (encontrada != null) {
            carregarComandas();
            for (int i = 0; i < modelo.getRowCount(); i++) {
                if (comanda.equals(String.valueOf(modelo.getValueAt(i, 0)))) {
                    tabela.setRowSelectionInterval(i, i);
                    tabela.scrollRectToVisible(tabela.getCellRect(i, 0, true));
                    selecionarLinhaAtual();
                    return;
                }
            }
        }
    }

    private void selecionarLinhaAtual() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            return;
        }
        String numeroComanda = String.valueOf(modelo.getValueAt(linha, 0));
        comandaSelecionada = comandaDAO.buscarPorComanda(numeroComanda);
        if (comandaSelecionada == null) {
            return;
        }
        lblComanda.setText("Comanda: " + comandaSelecionada.getComanda() + " | Cliente: " + comandaSelecionada.getClienteNome());
        lblTotal.setText("Total: " + VendaDAO.formatarMoeda(comandaSelecionada.getTotal()));
        txtDetalhes.setText(montarDetalhesComanda());
        txtDetalhes.setCaretPosition(0);
    }

    private void finalizarComandaSelecionada() {
        if (comandaSelecionada == null) {
            JOptionPane.showMessageDialog(this, "Selecione uma comanda aberta.");
            return;
        }

        if ("Entrega".equalsIgnoreCase(comandaSelecionada.getTipoVenda())
                && "Aguardando entrega".equalsIgnoreCase(comandaSelecionada.getStatus())) {
            prepararEntrega();
            return;
        }

        PagamentoComanda pagamento = abrirPagamento(comandaSelecionada);
        if (pagamento == null) {
            return;
        }

        String data = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (vendaDAO.registrarVendaDeComanda(
                comandaSelecionada, data, pagamento.formaResumo, pagamento.totalPago, pagamento.troco)) {
            comandaDAO.marcarComoPaga(comandaSelecionada.getId());
            JOptionPane.showMessageDialog(this,
                    "Comanda paga com sucesso.\nComanda: " + comandaSelecionada.getComanda()
                            + "\nTotal: " + VendaDAO.formatarMoeda(comandaSelecionada.getTotal())
                            + "\nRecebido: " + VendaDAO.formatarMoeda(pagamento.totalPago));
            carregarComandas();
        } else {
            JOptionPane.showMessageDialog(this, "Nao foi possivel finalizar essa comanda.");
        }
    }

    private PagamentoComanda abrirPagamento(ComandaCaixa comanda) {
        JDialog dialog = new JDialog(this, comanda.getComanda() + " Pagamento da Venda", true);
        dialog.setSize(620, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(0, 10));
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel cabecalho = new JPanel(new GridLayout(2, 2, 8, 8));
        cabecalho.setBackground(new Color(56, 19, 114));
        cabecalho.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel lblValorVendaTitulo = criarLabelPagamento("Valor da Venda:");
        JLabel lblValorVenda = criarValorPagamento(VendaDAO.formatarMoeda(comanda.getTotal()));
        JLabel lblTotalPagarTitulo = criarLabelPagamento("Total a Pagar:");
        JLabel lblTotalPagar = criarValorPagamento(VendaDAO.formatarMoeda(comanda.getTotal()));

        cabecalho.add(lblValorVendaTitulo);
        cabecalho.add(lblValorVenda);
        cabecalho.add(lblTotalPagarTitulo);
        cabecalho.add(lblTotalPagar);

        DefaultTableModel modeloPagamento = new DefaultTableModel(
                new String[]{"Tipo de Pagamento", "Valor Pago"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };
        String[] formas = {
                "Dinheiro",
                "Pix",
                "Cartao Debito",
                "Cartao Credito",
                "Cartao Credito 2X",
                "Cartao Credito 3X",
                "Cartao Credito 4X",
                "ExcCard",
                "Farmaseg",
                "Convenio",
                "Outro"
        };
        for (String forma : formas) {
            modeloPagamento.addRow(new Object[]{forma, "0,00"});
        }

        JTable tabelaPagamento = new JTable(modeloPagamento);
        tabelaPagamento.setRowHeight(26);
        tabelaPagamento.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelaPagamento.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableColumn colunaValor = tabelaPagamento.getColumnModel().getColumn(1);
        colunaValor.setPreferredWidth(120);

        JLabel lblTotalPago = new JLabel("R$ 0,00");
        lblTotalPago.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTotalPago.setForeground(Color.WHITE);
        JLabel lblTroco = new JLabel("R$ 0,00");
        lblTroco.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTroco.setForeground(Color.WHITE);

        Runnable atualizarResumo = () -> atualizarResumoPagamentoDialog(
                modeloPagamento, comanda.getTotal(), lblTotalPago, lblTroco);
        modeloPagamento.addTableModelListener(e -> atualizarResumo.run());

        String orientacao = "Informe so a forma usada. Exemplo: se foi Pix, apague 0,00 e digite o total.";
        if ("Entrega".equalsIgnoreCase(comanda.getTipoVenda()) && comanda.isPrecisaTrocoEntrega()) {
            orientacao = "Entrega marcada com troco para " + VendaDAO.formatarMoeda(comanda.getTrocoPara()) + ". Confira antes de receber.";
        }
        JLabel lblOrientacao = new JLabel(orientacao);
        lblOrientacao.setBorder(new EmptyBorder(0, 18, 0, 18));
        lblOrientacao.setForeground(new Color(71, 85, 105));

        JPanel painelResumo = new JPanel(new GridLayout(2, 2, 10, 6));
        painelResumo.setBackground(new Color(56, 19, 114));
        painelResumo.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel lblTotalPagoTitulo = new JLabel("Total Pago:");
        lblTotalPagoTitulo.setForeground(Color.WHITE);
        lblTotalPagoTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JLabel lblTrocoTitulo = new JLabel("Troco:");
        lblTrocoTitulo.setForeground(Color.WHITE);
        lblTrocoTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        painelResumo.add(lblTotalPagoTitulo);
        painelResumo.add(lblTotalPago);
        painelResumo.add(lblTrocoTitulo);
        painelResumo.add(lblTroco);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botoes.setBackground(Color.WHITE);
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnConfirmar = new JButton("Confirmar (F3)");
        final PagamentoComanda[] resultado = {null};

        btnCancelar.addActionListener(e -> dialog.dispose());
        btnConfirmar.addActionListener(e -> {
            if (tabelaPagamento.isEditing()) {
                tabelaPagamento.getCellEditor().stopCellEditing();
            }

            double totalPago = somarPagamentos(modeloPagamento);
            if (totalPago + 0.001 < comanda.getTotal()) {
                JOptionPane.showMessageDialog(dialog, "O total recebido precisa cobrir o valor da comanda.");
                return;
            }

            PagamentoComanda pagamento = new PagamentoComanda();
            pagamento.totalPago = totalPago;
            pagamento.troco = calcularTroco(modeloPagamento, comanda.getTotal());
            pagamento.formaResumo = montarResumoPagamento(modeloPagamento);
            resultado[0] = pagamento;
            dialog.dispose();
        });

        botoes.add(btnCancelar);
        botoes.add(btnConfirmar);

        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.setBackground(Color.WHITE);
        centro.setBorder(new EmptyBorder(0, 18, 0, 18));
        centro.add(new JScrollPane(tabelaPagamento), BorderLayout.CENTER);
        centro.add(painelResumo, BorderLayout.SOUTH);

        JPanel topo = new JPanel(new BorderLayout(0, 8));
        topo.setBackground(Color.WHITE);
        topo.add(cabecalho, BorderLayout.NORTH);
        topo.add(lblOrientacao, BorderLayout.SOUTH);

        dialog.add(topo, BorderLayout.NORTH);
        dialog.add(centro, BorderLayout.CENTER);
        dialog.add(botoes, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(btnConfirmar);
        dialog.getRootPane().registerKeyboardAction(e -> preencherLinhaSelecionadaComTotal(
                        tabelaPagamento, modeloPagamento, comanda.getTotal(), atualizarResumo),
                KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.getRootPane().registerKeyboardAction(e -> btnConfirmar.doClick(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        if (tabelaPagamento.getRowCount() > 0) {
            tabelaPagamento.setRowSelectionInterval(0, 0);
        }
        atualizarResumo.run();
        dialog.setVisible(true);
        return resultado[0];
    }

    private JLabel criarLabelPagamento(String texto) {
        JLabel label = new JLabel(texto);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        return label;
    }

    private JLabel criarValorPagamento(String texto) {
        JLabel label = new JLabel(texto, SwingConstants.RIGHT);
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setForeground(new Color(31, 41, 55));
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        label.setBorder(new EmptyBorder(6, 10, 6, 10));
        return label;
    }

    private void preencherLinhaSelecionadaComTotal(JTable tabelaPagamento, DefaultTableModel modeloPagamento,
                                                   double totalComanda, Runnable atualizarResumo) {
        int linha = tabelaPagamento.getSelectedRow();
        if (linha < 0) {
            linha = 0;
        }
        if (tabelaPagamento.isEditing()) {
            tabelaPagamento.getCellEditor().stopCellEditing();
        }
        double outrosPagamentos = 0;
        for (int i = 0; i < modeloPagamento.getRowCount(); i++) {
            if (i == linha) {
                continue;
            }
            outrosPagamentos += parseValor(String.valueOf(modeloPagamento.getValueAt(i, 1)));
        }
        double valorLinha = Math.max(0, totalComanda - outrosPagamentos);
        modeloPagamento.setValueAt(formatarValorDigitavel(valorLinha), linha, 1);
        atualizarResumo.run();
    }

    private void atualizarResumoPagamentoDialog(DefaultTableModel modeloPagamento, double totalComanda,
                                                JLabel lblTotalPago, JLabel lblTroco) {
        double totalPago = somarPagamentos(modeloPagamento);
        lblTotalPago.setText(VendaDAO.formatarMoeda(totalPago));
        lblTroco.setText(VendaDAO.formatarMoeda(calcularTroco(modeloPagamento, totalComanda)));
    }

    private double somarPagamentos(DefaultTableModel modeloPagamento) {
        double total = 0;
        for (int i = 0; i < modeloPagamento.getRowCount(); i++) {
            total += parseValor(String.valueOf(modeloPagamento.getValueAt(i, 1)));
        }
        return total;
    }

    private double calcularTroco(DefaultTableModel modeloPagamento, double totalComanda) {
        double dinheiro = 0;
        double outros = 0;
        for (int i = 0; i < modeloPagamento.getRowCount(); i++) {
            String forma = String.valueOf(modeloPagamento.getValueAt(i, 0));
            double valor = parseValor(String.valueOf(modeloPagamento.getValueAt(i, 1)));
            if ("Dinheiro".equalsIgnoreCase(forma)) {
                dinheiro += valor;
            } else {
                outros += valor;
            }
        }
        return Math.max(0, dinheiro - Math.max(0, totalComanda - outros));
    }

    private String montarResumoPagamento(DefaultTableModel modeloPagamento) {
        List<String> partes = new ArrayList<>();
        for (int i = 0; i < modeloPagamento.getRowCount(); i++) {
            String nome = String.valueOf(modeloPagamento.getValueAt(i, 0));
            double valor = parseValor(String.valueOf(modeloPagamento.getValueAt(i, 1)));
            if (valor <= 0) {
                continue;
            }
            partes.add(nome + ": " + VendaDAO.formatarMoeda(valor));
        }
        return partes.isEmpty() ? "Nao informado" : String.join(" | ", partes);
    }

    private String formatarValorDigitavel(double valor) {
        String texto = String.format(Locale.US, "%.2f", valor);
        return texto.replace(".", ",");
    }

    private void adicionarForma(StringBuilder resumo, String nome, double valor) {
        if (valor <= 0) {
            return;
        }
        if (resumo.length() > 0) {
            resumo.append(" | ");
        }
        resumo.append(nome).append(": ").append(VendaDAO.formatarMoeda(valor));
    }

    private double parseValor(String valor) {
        try {
            String texto = valor == null ? "0" : valor.trim();
            return Double.parseDouble(texto.replace(".", "").replace(",", "."));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String normalizarComanda(String valor) {
        String digitos = valor == null ? "" : valor.replaceAll("\\D", "");
        if (digitos.isBlank()) {
            return null;
        }
        int numero = Integer.parseInt(digitos);
        if (numero < 1 || numero > 999) {
            return null;
        }
        return String.format("%03d", numero);
    }

    private void prepararEntrega() {
        JCheckBox chkTroco = new JCheckBox("Precisa de troco na entrega");
        JTextField txtTrocoPara = new JTextField("0,00");
        txtTrocoPara.setEnabled(false);
        chkTroco.addActionListener(e -> txtTrocoPara.setEnabled(chkTroco.isSelected()));

        JPanel painel = new JPanel(new GridLayout(3, 2, 10, 10));
        painel.add(new JLabel("Endereco:"));
        painel.add(new JLabel(comandaSelecionada.getEndereco() + " | " + comandaSelecionada.getBairro() + " | " + comandaSelecionada.getCidade()));
        painel.add(chkTroco);
        painel.add(txtTrocoPara);
        painel.add(new JLabel("Acao:"));
        painel.add(new JLabel("Conferir medicamentos e marcar como entregue"));

        int escolha = JOptionPane.showConfirmDialog(
                this,
                painel,
                "Entrega da comanda " + comandaSelecionada.getComanda(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (escolha != JOptionPane.OK_OPTION) {
            return;
        }

        double trocoPara = chkTroco.isSelected() ? parseValor(txtTrocoPara.getText()) : 0.0;
        if (chkTroco.isSelected() && trocoPara < comandaSelecionada.getTotal()) {
            JOptionPane.showMessageDialog(this, "O troco precisa ser para um valor maior ou igual ao total da entrega.");
            return;
        }

        if (comandaDAO.atualizarStatusEntrega(comandaSelecionada.getId(), "Entregue", chkTroco.isSelected(), trocoPara)) {
            JOptionPane.showMessageDialog(this, "Entrega marcada como entregue. Agora a comanda fica pronta para baixa final no caixa.");
            carregarComandas();
        } else {
            JOptionPane.showMessageDialog(this, "Nao foi possivel atualizar o status da entrega.");
        }
    }

    private String montarDetalhesComanda() {
        if (comandaSelecionada == null) {
            return "";
        }
        StringBuilder detalhes = new StringBuilder();
        detalhes.append("Tipo: ").append(comandaSelecionada.getTipoVenda())
                .append("\nStatus: ").append(comandaSelecionada.getStatus())
                .append("\nAtendente: ").append(comandaSelecionada.getUsuarioNome())
                .append("\nCliente: ").append(comandaSelecionada.getClienteNome())
                .append("\n\nItens:\n").append(comandaSelecionada.getItensDetalhe());

        if ("Entrega".equalsIgnoreCase(comandaSelecionada.getTipoVenda())) {
            detalhes.append("\n\nEndereco de entrega:\n")
                    .append(comandaSelecionada.getEndereco())
                    .append("\nBairro: ").append(comandaSelecionada.getBairro())
                    .append("\nCidade: ").append(comandaSelecionada.getCidade())
                    .append("\nTaxa de entrega: ").append(VendaDAO.formatarMoeda(comandaSelecionada.getTaxaEntrega()));
            if (comandaSelecionada.isPrecisaTrocoEntrega()) {
                detalhes.append("\nTroco para: ").append(VendaDAO.formatarMoeda(comandaSelecionada.getTrocoPara()));
            }
        }
        return detalhes.toString();
    }

    private static class PagamentoComanda {
        private String formaResumo;
        private double totalPago;
        private double troco;
    }

    private static class ApenasTresDigitosFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            replace(fb, offset, 0, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            String atual = fb.getDocument().getText(0, fb.getDocument().getLength());
            String novo = new StringBuilder(atual).replace(offset, offset + length, text == null ? "" : text).toString();
            String digitos = novo.replaceAll("\\D", "");
            if (digitos.length() <= 3) {
                fb.replace(offset, length, text == null ? "" : text.replaceAll("\\D", ""), attrs);
            }
        }
    }
}
