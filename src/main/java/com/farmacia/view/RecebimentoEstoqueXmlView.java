package com.farmacia.view;

import com.farmacia.dao.FornecedorDAO;
import com.farmacia.dao.LoteDAO;
import com.farmacia.dao.ProdutoDAO;
import com.farmacia.dao.RecebimentoEstoqueDAO;
import com.farmacia.model.Fornecedor;
import com.farmacia.model.ItemNotaFiscalEntrada;
import com.farmacia.model.NotaFiscalEntrada;
import com.farmacia.model.Produto;
import com.farmacia.util.CnpjUtils;
import com.farmacia.util.NotaFiscalXmlParser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.MaskFormatter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class RecebimentoEstoqueXmlView extends JFrame {
    private final FornecedorDAO fornecedorDAO = new FornecedorDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final LoteDAO loteDAO = new LoteDAO();
    private final RecebimentoEstoqueDAO recebimentoDAO = new RecebimentoEstoqueDAO();
    private final NotaFiscalXmlParser xmlParser = new NotaFiscalXmlParser();
    private final List<ItemConferencia> itensConferencia = new ArrayList<>();

    private JTextField txtFornecedorBusca;
    private JComboBox<Fornecedor> cbFornecedores;
    private JLabel lblFornecedorSelecionado;
    private JLabel lblResumoNota;
    private JLabel lblStatusLancamento;
    private JButton btnLancar;
    private File arquivoSelecionado;
    private NotaFiscalEntrada notaAtual;

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Codigo", "Produto da nota", "Lote", "Validade", "Laboratorio", "Qtde", "Codigo de barras", "Custo NF", "Custo atual", "Ajuste custo", "Produto no sistema", "Estoque atual", "Conferencia"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public RecebimentoEstoqueXmlView() {
        setTitle("Recebimento de Estoque por XML");
        setSize(1260, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        inicializarTela();
        configurarAtalhos();
        atualizarSugestoesFornecedor("");
    }

    private void inicializarTela() {
        JPanel painel = new JPanel(new BorderLayout(0, 16));
        painel.setBorder(new EmptyBorder(18, 18, 18, 18));
        painel.setBackground(Color.WHITE);
        setContentPane(painel);

        painel.add(criarTopo(), BorderLayout.NORTH);
        painel.add(criarTabela(), BorderLayout.CENTER);
        painel.add(criarRodape(), BorderLayout.SOUTH);
    }

    private JPanel criarTopo() {
        JPanel topo = new JPanel(new BorderLayout(0, 14));
        topo.setOpaque(false);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Recebimento de Estoque por XML");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 25));
        titulo.setForeground(new Color(15, 23, 42));

        JLabel subtitulo = new JLabel("Leia a NF-e, confira os itens da nota e lance no estoque apenas quando tudo estiver certo.");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitulo.setForeground(new Color(100, 116, 139));

        textos.add(titulo);
        textos.add(Box.createRigidArea(new Dimension(0, 6)));
        textos.add(subtitulo);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        formulario.add(new JLabel("Fornecedor:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        txtFornecedorBusca = new JTextField();
        formulario.add(txtFornecedorBusca, gbc);

        gbc.gridx = 2;
        gbc.weightx = 1;
        cbFornecedores = new JComboBox<>();
        cbFornecedores.setMaximumRowCount(12);
        formulario.add(cbFornecedores, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0;
        JButton btnUsarFornecedor = new JButton("Usar fornecedor");
        btnUsarFornecedor.addActionListener(e -> aplicarFornecedorSelecionado());
        formulario.add(btnUsarFornecedor, gbc);

        gbc.gridx = 4;
        JButton btnCadastrarFornecedor = new JButton("Cadastrar distribuidora");
        btnCadastrarFornecedor.addActionListener(e -> cadastrarDistribuidora());
        formulario.add(btnCadastrarFornecedor, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formulario.add(new JLabel("Arquivo XML:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        lblResumoNota = new JLabel("Nenhuma nota fiscal carregada.");
        lblResumoNota.setForeground(new Color(71, 85, 105));
        formulario.add(lblResumoNota, gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        JButton btnCarregarXml = new JButton("Carregar XML da nota");
        btnCarregarXml.addActionListener(e -> carregarXml());
        formulario.add(btnCarregarXml, gbc);

        txtFornecedorBusca.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                atualizarSugestoesFornecedor(txtFornecedorBusca.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                atualizarSugestoesFornecedor(txtFornecedorBusca.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                atualizarSugestoesFornecedor(txtFornecedorBusca.getText());
            }
        });
        txtFornecedorBusca.addActionListener(e -> aplicarFornecedorSelecionado());
        cbFornecedores.addActionListener(e -> atualizarFornecedorSelecionadoLabel());

        topo.add(textos, BorderLayout.NORTH);
        topo.add(formulario, BorderLayout.CENTER);
        return topo;
    }

    private JScrollPane criarTabela() {
        JTable tabela = new JTable(modelo);
        tabela.setRowHeight(28);
        tabela.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return new JScrollPane(tabela);
    }

    private JPanel criarRodape() {
        JPanel rodape = new JPanel(new BorderLayout(12, 0));
        rodape.setOpaque(false);

        JPanel status = new JPanel();
        status.setOpaque(false);
        status.setLayout(new BoxLayout(status, BoxLayout.Y_AXIS));

        lblFornecedorSelecionado = new JLabel("Fornecedor pronto para recebimento: nenhum selecionado.");
        lblFornecedorSelecionado.setForeground(new Color(30, 64, 175));

        lblStatusLancamento = new JLabel("Carregue a NF-e para visualizar os medicamentos da nota antes do lancamento.");
        lblStatusLancamento.setForeground(new Color(100, 116, 139));

        status.add(lblFornecedorSelecionado);
        status.add(Box.createRigidArea(new Dimension(0, 4)));
        status.add(lblStatusLancamento);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botoes.setOpaque(false);

        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dispose());

        btnLancar = new JButton("Lancar recebimento no estoque");
        btnLancar.setEnabled(false);
        btnLancar.addActionListener(e -> lancarRecebimento());

        botoes.add(btnFechar);
        botoes.add(btnLancar);

        rodape.add(status, BorderLayout.CENTER);
        rodape.add(botoes, BorderLayout.EAST);
        return rodape;
    }

    private void configurarAtalhos() {
        getRootPane().setDefaultButton(btnLancar);

        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(esc, "fechar");
        getRootPane().getActionMap().put("fechar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });
    }

    private void atualizarSugestoesFornecedor(String termo) {
        List<Fornecedor> fornecedores = fornecedorDAO.buscarInteligente(termo);
        cbFornecedores.removeAllItems();
        for (Fornecedor fornecedor : fornecedores) {
            cbFornecedores.addItem(fornecedor);
        }
        if (cbFornecedores.getItemCount() > 0) {
            cbFornecedores.setSelectedIndex(0);
        }
        atualizarFornecedorSelecionadoLabel();
    }

    private void atualizarFornecedorSelecionadoLabel() {
        Fornecedor fornecedor = (Fornecedor) cbFornecedores.getSelectedItem();
        if (fornecedor == null) {
            lblFornecedorSelecionado.setText("Fornecedor pronto para recebimento: nenhum selecionado.");
            return;
        }
        lblFornecedorSelecionado.setText("Fornecedor pronto para recebimento: "
                + fornecedor.getNome() + " | CNPJ " + CnpjUtils.formatar(fornecedor.getCnpj()));
    }

    private void aplicarFornecedorSelecionado() {
        Fornecedor fornecedor = (Fornecedor) cbFornecedores.getSelectedItem();
        if (fornecedor == null && CnpjUtils.isValido(txtFornecedorBusca.getText())) {
            fornecedor = fornecedorDAO.buscarPorCnpj(txtFornecedorBusca.getText());
        }
        if (fornecedor == null) {
            JOptionPane.showMessageDialog(this, "Selecione um fornecedor valido na busca inteligente.");
            return;
        }
        txtFornecedorBusca.setText(fornecedor.getNome() + " | " + CnpjUtils.formatar(fornecedor.getCnpj()));
        atualizarSugestoesFornecedor(fornecedor.getCnpj());
    }

    private void carregarXml() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecionar XML da nota fiscal");
        chooser.setFileFilter(new FileNameExtensionFilter("Arquivos XML", "xml"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        arquivoSelecionado = chooser.getSelectedFile();
        try {
            notaAtual = xmlParser.ler(arquivoSelecionado);
            preencherFornecedorDaNota();
            montarConferenciaItens();
            atualizarResumoNota();
            if (modelo.getRowCount() > 0) {
                lblStatusLancamento.setText("XML carregado. Os medicamentos da nota ja foram abertos para conferencia.");
            }
        } catch (Exception e) {
            notaAtual = null;
            arquivoSelecionado = null;
            modelo.setRowCount(0);
            btnLancar.setEnabled(false);
            lblStatusLancamento.setText("Nao foi possivel ler o XML selecionado.");
            JOptionPane.showMessageDialog(this, "Erro ao ler XML: " + e.getMessage());
        }
    }

    private void preencherFornecedorDaNota() {
        if (notaAtual == null) {
            return;
        }

        Fornecedor fornecedor = null;
        if (CnpjUtils.isValido(notaAtual.getFornecedorCnpj())) {
            fornecedor = fornecedorDAO.salvarSeNaoExistir(notaAtual.getFornecedorNome(), notaAtual.getFornecedorCnpj());
        }

        if (fornecedor != null) {
            txtFornecedorBusca.setText(fornecedor.getNome() + " | " + CnpjUtils.formatar(fornecedor.getCnpj()));
            atualizarSugestoesFornecedor(fornecedor.getCnpj());
        } else {
            txtFornecedorBusca.setText(notaAtual.getFornecedorNome());
            atualizarSugestoesFornecedor(notaAtual.getFornecedorNome());
        }
    }

    private void cadastrarDistribuidora() {
        JPanel painel = new JPanel(new GridLayout(2, 2, 10, 10));
        JTextField txtNome = new JTextField();
        JFormattedTextField txtCnpj = criarCampoCnpj();

        String buscaAtual = txtFornecedorBusca.getText().trim();
        if (!buscaAtual.isBlank() && !CnpjUtils.isValido(buscaAtual)) {
            txtNome.setText(buscaAtual);
        }

        painel.add(new JLabel("Nome da distribuidora:"));
        painel.add(txtNome);
        painel.add(new JLabel("CNPJ:"));
        painel.add(txtCnpj);

        int opcao = JOptionPane.showConfirmDialog(
                this,
                painel,
                "Cadastrar distribuidora",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (opcao != JOptionPane.OK_OPTION) {
            return;
        }

        String nome = txtNome.getText().trim();
        String cnpj = txtCnpj.getText().trim();

        if (nome.isBlank()) {
            JOptionPane.showMessageDialog(this, "Informe o nome da distribuidora.");
            return;
        }
        if (!CnpjUtils.isValido(cnpj)) {
            JOptionPane.showMessageDialog(this, "Informe um CNPJ valido com 14 numeros.");
            return;
        }

        Fornecedor fornecedor = fornecedorDAO.salvarSeNaoExistir(nome, cnpj);
        if (fornecedor == null) {
            JOptionPane.showMessageDialog(this, "Nao foi possivel cadastrar a distribuidora.");
            return;
        }

        txtFornecedorBusca.setText(fornecedor.getNome() + " | " + CnpjUtils.formatar(fornecedor.getCnpj()));
        atualizarSugestoesFornecedor(fornecedor.getCnpj());
        JOptionPane.showMessageDialog(this, "Distribuidora cadastrada e selecionada com sucesso.");
    }

    private JFormattedTextField criarCampoCnpj() {
        try {
            MaskFormatter mascara = new MaskFormatter("##.###.###/####-##");
            mascara.setPlaceholderCharacter('_');
            JFormattedTextField campo = new JFormattedTextField(mascara);
            campo.setColumns(18);
            campo.setFocusLostBehavior(JFormattedTextField.PERSIST);
            return campo;
        } catch (ParseException e) {
            return new JFormattedTextField();
        }
    }

    private void montarConferenciaItens() {
        itensConferencia.clear();
        modelo.setRowCount(0);

        if (notaAtual == null) {
            btnLancar.setEnabled(false);
            return;
        }

        for (ItemNotaFiscalEntrada item : notaAtual.getItens()) {
            Produto produto = produtoDAO.buscarPorCodigoBarras(item.getCodigoBarras());
            if (produto == null) {
                produto = produtoDAO.buscarMelhorCorrespondenciaPorNome(item.getDescricao());
            }

            ItemConferencia conferencia = new ItemConferencia(item, produto);
            itensConferencia.add(conferencia);

            modelo.addRow(new Object[]{
                    item.getCodigo(),
                    item.getDescricao(),
                    valorOuTraco(item.getLote()),
                    valorOuTraco(item.getDataValidade()),
                    valorOuTraco(item.getLaboratorio()),
                    item.getQuantidade(),
                    valorOuTraco(item.getCodigoBarras()),
                    VendaView.formatarMoeda(item.getValorUnitario()),
                    produto == null ? "-" : VendaView.formatarMoeda(produto.getPrecoCusto()),
                    conferencia.getResumoCusto(),
                    produto == null ? "Novo cadastro pelo XML" : produto.getNome(),
                    produto == null ? "0" : produto.getQuantidade_estoque(),
                    conferencia.getStatus()
            });
        }

        boolean possuiBloqueio = itensConferencia.stream().anyMatch(item -> !item.podeLancar());
        btnLancar.setEnabled(!itensConferencia.isEmpty() && !possuiBloqueio);
        lblStatusLancamento.setText(possuiBloqueio
                ? "Existem itens bloqueados sem codigo de barras ou com quantidade invalida. Eles nao entram no estoque."
                : "Conferencia pronta. Produtos novos da nota serao cadastrados automaticamente no estoque.");
    }

    private void atualizarResumoNota() {
        if (notaAtual == null) {
            lblResumoNota.setText("Nenhuma nota fiscal carregada.");
            return;
        }

        String arquivo = arquivoSelecionado == null ? "" : arquivoSelecionado.getName();
        lblResumoNota.setText("<html>Arquivo: <b>" + arquivo + "</b> | NF: <b>" + valorOuTraco(notaAtual.getNumeroNota())
                + "</b> | Itens: <b>" + notaAtual.getItens().size()
                + "</b> | Total: <b>" + VendaView.formatarMoeda(notaAtual.getValorTotal())
                + "</b> | Emitente: <b>" + valorOuTraco(notaAtual.getFornecedorNome())
                + "</b> | CNPJ: <b>" + valorOuTraco(CnpjUtils.formatar(notaAtual.getFornecedorCnpj())) + "</b></html>");
    }

    private void lancarRecebimento() {
        Fornecedor fornecedorSelecionado = (Fornecedor) cbFornecedores.getSelectedItem();
        if (notaAtual == null || arquivoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Carregue um XML valido antes de lancar o recebimento.");
            return;
        }
        if (fornecedorSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione o fornecedor do recebimento.");
            return;
        }
        for (ItemConferencia item : itensConferencia) {
            if (!item.podeLancar()) {
                JOptionPane.showMessageDialog(this, "A nota ainda tem item sem codigo de barras valido ou com quantidade invalida.");
                return;
            }
        }

        if (CnpjUtils.isValido(notaAtual.getFornecedorCnpj())
                && !CnpjUtils.apenasDigitos(notaAtual.getFornecedorCnpj()).equals(CnpjUtils.apenasDigitos(fornecedorSelecionado.getCnpj()))) {
            int opcao = JOptionPane.showConfirmDialog(
                    this,
                    "O fornecedor escolhido nao bate com o emitente do XML. Deseja continuar assim mesmo?",
                    "Fornecedor divergente",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (opcao != JOptionPane.YES_OPTION) {
                return;
            }
        }

        Integer recebimentoId = recebimentoDAO.registrarRecebimento(
                fornecedorSelecionado,
                notaAtual.getChaveNfe(),
                notaAtual.getNumeroNota(),
                arquivoSelecionado.getAbsolutePath(),
                notaAtual.getItens().size(),
                notaAtual.getValorTotal()
        );

        if (recebimentoId == null) {
            JOptionPane.showMessageDialog(this, "Nao foi possivel registrar o recebimento da nota.");
            return;
        }

        for (ItemConferencia item : itensConferencia) {
            Produto produtoAlvo = item.produtoSistema;
            boolean produtoCriadoAgora = false;
            if (produtoAlvo == null) {
                produtoAlvo = produtoDAO.cadastrarProdutoPorXml(
                        item.itemNota.getDescricao(),
                        item.itemNota.getCodigoBarras(),
                        item.itemNota.getQuantidade(),
                        item.itemNota.getDataValidade(),
                        item.itemNota.getValorUnitario(),
                        item.getLaboratorioFinal()
                );
                produtoCriadoAgora = true;
            }

            if (produtoAlvo == null) {
                JOptionPane.showMessageDialog(this, "Nao foi possivel cadastrar um produto novo vindo do XML.");
                return;
            }

            boolean estoqueAtualizado = produtoCriadoAgora
                    || produtoDAO.adicionarQuantidadePorId(produtoAlvo.getId(), item.itemNota.getQuantidade());
            boolean dadosAtualizados = produtoDAO.atualizarDadosRecebimento(
                    produtoAlvo.getId(),
                    item.getLaboratorioFinal(),
                    item.itemNota.getDataValidade(),
                    item.itemNota.getValorUnitario()
            );
            boolean loteAtualizado = loteDAO.registrarOuAtualizarLote(
                    produtoAlvo.getId(),
                    item.itemNota.getLote(),
                    item.itemNota.getQuantidade(),
                    item.itemNota.getDataValidade(),
                    item.getLaboratorioFinal(),
                    item.itemNota.getValorUnitario()
            );
            boolean itemRegistrado = recebimentoDAO.registrarItemRecebido(
                    recebimentoId,
                    produtoAlvo.getId(),
                    item.itemNota.getDescricao(),
                    item.itemNota.getCodigo(),
                    item.itemNota.getQuantidade(),
                    item.itemNota.getValorUnitario(),
                    item.itemNota.getLote(),
                    item.itemNota.getDataValidade(),
                    item.getLaboratorioFinal(),
                    item.getCustoAnterior(),
                    item.itemNota.getValorUnitario(),
                    item.houveAlteracaoCusto()
            );
            if (!estoqueAtualizado || !dadosAtualizados || !loteAtualizado || !itemRegistrado) {
                JOptionPane.showMessageDialog(this,
                        "O recebimento foi iniciado, mas houve falha ao gravar um dos itens da nota. Revise o estoque antes de seguir.");
                dispose();
                return;
            }
        }

        JOptionPane.showMessageDialog(this,
                "Recebimento lancado com sucesso.\nFornecedor: " + fornecedorSelecionado.getNome()
                        + "\nItens conferidos: " + itensConferencia.size()
                        + "\nValor total da nota: " + VendaView.formatarMoeda(notaAtual.getValorTotal()));
        dispose();
    }

    private String valorOuTraco(String valor) {
        return valor == null || valor.isBlank() ? "-" : valor;
    }

    private static class ItemConferencia {
        private final ItemNotaFiscalEntrada itemNota;
        private final Produto produtoSistema;

        private ItemConferencia(ItemNotaFiscalEntrada itemNota, Produto produtoSistema) {
            this.itemNota = itemNota;
            this.produtoSistema = produtoSistema;
        }

        private boolean temQuantidadeValida() {
            return itemNota.getQuantidade() > 0;
        }

        private boolean temCodigoBarrasValido() {
            return itemNota.getCodigoBarras() != null && !itemNota.getCodigoBarras().isBlank();
        }

        private boolean podeLancar() {
            return temQuantidadeValida() && temCodigoBarrasValido();
        }

        private String getStatus() {
            if (itemNota.getQuantidade() <= 0) {
                return "Quantidade invalida na nota";
            }
            if (!temCodigoBarrasValido()) {
                return "Bloqueado: sem codigo de barras";
            }
            if (produtoSistema == null) {
                return "Sera cadastrado pelo XML";
            }
            if (codigoBarrasDivergente()) {
                return "Estoque somado, codigo antigo mantido";
            }
            if (itemNota.getLote() == null || itemNota.getLote().isBlank()) {
                return "Pronto, sem lote no XML";
            }
            return "Pronto para lancar";
        }

        private double getCustoAnterior() {
            return produtoSistema == null ? 0.0 : produtoSistema.getPrecoCusto();
        }

        private boolean houveAlteracaoCusto() {
            return produtoSistema != null && Math.abs(produtoSistema.getPrecoCusto() - itemNota.getValorUnitario()) > 0.009;
        }

        private String getResumoCusto() {
            if (produtoSistema == null) {
                return "Novo produto";
            }
            double custoAtual = produtoSistema.getPrecoCusto();
            double custoNota = itemNota.getValorUnitario();
            if (Math.abs(custoAtual - custoNota) <= 0.009) {
                return "Sem alteracao";
            }
            return custoNota > custoAtual ? "Custo subiu" : "Custo baixou";
        }

        private String getLaboratorioFinal() {
            if (itemNota.getLaboratorio() != null && !itemNota.getLaboratorio().isBlank()) {
                return itemNota.getLaboratorio();
            }
            return produtoSistema == null ? "" : produtoSistema.getLaboratorio();
        }

        private boolean codigoBarrasDivergente() {
            if (produtoSistema == null) {
                return false;
            }
            String codigoSistema = produtoSistema.getCodigo_barras() == null ? "" : produtoSistema.getCodigo_barras().trim();
            String codigoNota = itemNota.getCodigoBarras() == null ? "" : itemNota.getCodigoBarras().trim();
            return !codigoSistema.isBlank() && !codigoNota.isBlank() && !codigoSistema.equals(codigoNota);
        }
    }
}
