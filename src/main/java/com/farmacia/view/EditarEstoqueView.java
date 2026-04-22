package com.farmacia.view;

import com.farmacia.dao.ProdutoDAO;
import com.farmacia.model.Produto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EditarEstoqueView extends JFrame {
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final List<Produto> produtos = new ArrayList<>();

    private JTextField txtBuscaProduto;
    private JComboBox<ProdutoItem> cbProdutos;
    private JTextField txtQtd;
    private JRadioButton rbDefinirSaldo;
    private JRadioButton rbAdicionar;
    private JRadioButton rbRetirar;
    private JLabel lblDetalhes;
    private JButton btnSalvar;

    public EditarEstoqueView() {
        setTitle("Ajustar Estoque");
        setSize(620, 310);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        carregarProdutos();
        inicializarTela();
        configurarAtalhos();
    }

    private void inicializarTela() {
        JPanel painel = new JPanel(new BorderLayout(0, 16));
        painel.setBorder(new EmptyBorder(18, 18, 18, 18));
        setContentPane(painel);

        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formulario.add(new JLabel("Buscar produto:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        txtBuscaProduto = new JTextField();
        formulario.add(txtBuscaProduto, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formulario.add(new JLabel("Produto:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        cbProdutos = new JComboBox<>();
        cbProdutos.setMaximumRowCount(12);
        formulario.add(cbProdutos, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        formulario.add(new JLabel("Quantidade:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        txtQtd = new JTextField();
        ((AbstractDocument) txtQtd.getDocument()).setDocumentFilter(new ApenasNumerosFilter());
        formulario.add(txtQtd, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        formulario.add(new JLabel("Operacao:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        formulario.add(criarPainelOperacao(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formulario.add(new JLabel("Detalhes:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        lblDetalhes = new JLabel();
        lblDetalhes.setVerticalAlignment(SwingConstants.TOP);
        formulario.add(lblDetalhes, gbc);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Cancelar");
        btnSalvar = new JButton("Salvar ajuste");

        btnCancelar.addActionListener(e -> dispose());
        btnSalvar.addActionListener(e -> salvarAjuste());

        botoes.add(btnCancelar);
        botoes.add(btnSalvar);

        painel.add(formulario, BorderLayout.CENTER);
        painel.add(botoes, BorderLayout.SOUTH);

        atualizarListaProdutos("");

        txtBuscaProduto.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                atualizarListaProdutos(txtBuscaProduto.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                atualizarListaProdutos(txtBuscaProduto.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                atualizarListaProdutos(txtBuscaProduto.getText());
            }
        });

        cbProdutos.addActionListener(e -> atualizarDetalhesProduto());
        txtQtd.addActionListener(e -> salvarAjuste());

        atualizarDetalhesProduto();
        SwingUtilities.invokeLater(() -> txtQtd.requestFocusInWindow());
    }

    private JPanel criarPainelOperacao() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        painel.setOpaque(false);

        rbDefinirSaldo = new JRadioButton("Definir saldo");
        rbAdicionar = new JRadioButton("Adicionar");
        rbRetirar = new JRadioButton("Retirar");

        rbDefinirSaldo.setOpaque(false);
        rbAdicionar.setOpaque(false);
        rbRetirar.setOpaque(false);

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbDefinirSaldo);
        grupo.add(rbAdicionar);
        grupo.add(rbRetirar);
        rbDefinirSaldo.setSelected(true);

        painel.add(rbDefinirSaldo);
        painel.add(rbAdicionar);
        painel.add(rbRetirar);
        return painel;
    }

    private void configurarAtalhos() {
        JRootPane rootPane = getRootPane();
        rootPane.setDefaultButton(btnSalvar);

        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(esc, "fechar");
        rootPane.getActionMap().put("fechar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });
    }

    private void carregarProdutos() {
        produtos.clear();
        produtos.addAll(produtoDAO.listarTudo());
    }

    private void atualizarListaProdutos(String termo) {
        ProdutoItem selecionado = (ProdutoItem) cbProdutos.getSelectedItem();
        cbProdutos.removeAllItems();

        String filtro = termo == null ? "" : termo.trim().toLowerCase(Locale.ROOT);
        for (Produto produto : produtos) {
            if (filtro.isBlank() || corresponde(produto, filtro)) {
                cbProdutos.addItem(new ProdutoItem(produto));
            }
        }

        if (selecionado != null) {
            for (int i = 0; i < cbProdutos.getItemCount(); i++) {
                ProdutoItem item = cbProdutos.getItemAt(i);
                if (item.produto.getId() == selecionado.produto.getId()) {
                    cbProdutos.setSelectedIndex(i);
                    atualizarDetalhesProduto();
                    return;
                }
            }
        }

        if (cbProdutos.getItemCount() > 0) {
            cbProdutos.setSelectedIndex(0);
        }
        atualizarDetalhesProduto();
    }

    private boolean corresponde(Produto produto, String filtro) {
        return normalizar(produto.getNome()).contains(filtro)
                || normalizar(produto.getCategoria()).contains(filtro)
                || normalizar(produto.getLaboratorio()).contains(filtro)
                || normalizar(produto.getTipoControle()).contains(filtro)
                || normalizar(produto.getCodigo_barras()).contains(filtro);
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.toLowerCase(Locale.ROOT);
    }

    private void atualizarDetalhesProduto() {
        ProdutoItem item = (ProdutoItem) cbProdutos.getSelectedItem();
        if (item == null) {
            lblDetalhes.setText("<html><body>Nenhum produto encontrado para o filtro informado.</body></html>");
            return;
        }

        Produto produto = item.produto;
        lblDetalhes.setText("<html><body style='width:360px'>"
                + "Categoria: <b>" + produto.getCategoria() + "</b><br>"
                + "Laboratorio: <b>" + produto.getLaboratorio() + "</b><br>"
                + "Controle: <b>" + produto.getTipoControle() + "</b><br>"
                + "Estoque atual: <b>" + produto.getQuantidade_estoque() + "</b><br>"
                + "Estoque minimo: <b>" + produto.getEstoqueMinimo() + "</b><br>"
                + "Validade: <b>" + produto.getData_validade() + "</b><br>"
                + "Status: <b>" + produto.getStatusOperacional() + "</b>"
                + "</body></html>");
    }

    private void salvarAjuste() {
        try {
            ProdutoItem item = (ProdutoItem) cbProdutos.getSelectedItem();
            int qtdInformada = Integer.parseInt(txtQtd.getText().trim());

            if (item == null) {
                JOptionPane.showMessageDialog(this, "Selecione um produto.");
                return;
            }

            if (qtdInformada < 0) {
                JOptionPane.showMessageDialog(this, "A quantidade nao pode ser negativa.");
                return;
            }

            int estoqueAtual = item.produto.getQuantidade_estoque();
            int novoSaldo;

            if (rbAdicionar.isSelected()) {
                novoSaldo = estoqueAtual + qtdInformada;
            } else if (rbRetirar.isSelected()) {
                novoSaldo = estoqueAtual - qtdInformada;
                if (novoSaldo < 0) {
                    JOptionPane.showMessageDialog(this, "Nao e possivel retirar mais do que o estoque atual.");
                    return;
                }
            } else {
                novoSaldo = qtdInformada;
            }

            if (produtoDAO.atualizarQuantidadePorId(item.produto.getId(), novoSaldo)) {
                JOptionPane.showMessageDialog(this, "Estoque atualizado com sucesso.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Nao foi possivel atualizar o estoque.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Digite uma quantidade valida.");
        }
    }

    private static class ProdutoItem {
        private final Produto produto;

        private ProdutoItem(Produto produto) {
            this.produto = produto;
        }

        @Override
        public String toString() {
            return produto.getNome() + " | " + produto.getCategoria() + " | atual: " + produto.getQuantidade_estoque();
        }
    }

    private static class ApenasNumerosFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string != null && string.matches("\\d+")) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text == null || text.isEmpty() || text.matches("\\d+")) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }
}
