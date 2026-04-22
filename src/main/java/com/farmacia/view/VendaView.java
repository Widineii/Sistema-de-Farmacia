package com.farmacia.view;

import com.farmacia.dao.ClienteDAO;
import com.farmacia.dao.ComandaCaixaDAO;
import com.farmacia.dao.MedicoDAO;
import com.farmacia.dao.ProdutoDAO;
import com.farmacia.dao.VendaDAO;
import com.farmacia.model.Cliente;
import com.farmacia.model.Medico;
import com.farmacia.model.Produto;
import com.farmacia.model.Usuario;
import com.farmacia.util.CpfUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DefaultFormatter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VendaView extends JFrame {
    private static final Color COR_FUNDO = new Color(236, 242, 248);
    private static final Color COR_CARD = new Color(255, 255, 255);
    private static final Color COR_BORDA = new Color(219, 229, 241);
    private static final Color COR_TITULO = new Color(15, 23, 42);
    private static final Color COR_TEXTO = new Color(71, 85, 105);
    private static final Color COR_AZUL = new Color(14, 165, 233);
    private static final Color COR_AZUL_HOVER = new Color(3, 105, 161);
    private static final Color COR_VERDE = new Color(22, 163, 74);
    private final Usuario usuarioLogado;
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final VendaDAO vendaDAO = new VendaDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final MedicoDAO medicoDAO = new MedicoDAO();
    private final ComandaCaixaDAO comandaCaixaDAO = new ComandaCaixaDAO();
    private final List<ItemCarrinho> carrinho = new ArrayList<>();
    private Cliente clienteSelecionado;
    private boolean atualizandoClienteUI;

    private final DefaultTableModel modeloProdutos = new DefaultTableModel(
            new String[]{"ID", "Produto", "Estoque", "Preco", "Codigo"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final DefaultTableModel modeloCarrinho = new DefaultTableModel(
            new String[]{"Produto", "Qtd", "Unitario", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private JTable tabelaProdutos;
    private JTable tabelaCarrinho;
    private JTextField txtBuscaProduto;
    private JSpinner spQuantidade;
    private JTextField txtClienteNome;
    private JTextField txtClienteCpf;
    private JComboBox<Cliente> cbSugestoesCliente;
    private JCheckBox chkSemCadastro;
    private JCheckBox chkEntrega;
    private JLabel lblClienteSelecionado;
    private JLabel lblItens;
    private JLabel lblTotal;
    private JLabel lblDesconto;
    private JLabel lblComissao;
    private TableRowSorter<DefaultTableModel> sorterProdutos;

    public VendaView(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;

        setTitle("Caixa / PDV");
        setSize(1180, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(18, 18));
        getContentPane().setBackground(COR_FUNDO);

        add(criarCabecalho(), BorderLayout.NORTH);
        add(criarConteudo(), BorderLayout.CENTER);
        carregarProdutos();
        atualizarResumo();
    }

    private JPanel criarCabecalho() {
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(Color.WHITE);
        topo.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COR_BORDA),
                new EmptyBorder(18, 22, 18, 22)
        ));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Caixa / PDV");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titulo.setForeground(COR_TITULO);

        JLabel subtitulo = new JLabel("Operador: " + usuarioLogado.getNome() + " | perfil: " + usuarioLogado.getCargoExibicao());
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitulo.setForeground(new Color(100, 116, 139));

        textos.add(titulo);
        textos.add(Box.createRigidArea(new Dimension(0, 6)));
        textos.add(subtitulo);

        topo.add(textos, BorderLayout.WEST);
        topo.add(criarResumoVenda(), BorderLayout.EAST);
        return topo;
    }

    private JPanel criarResumoVenda() {
        JPanel resumo = new JPanel(new GridLayout(1, 4, 10, 0));
        resumo.setOpaque(false);

        lblItens = criarMiniCard("Itens");
        lblTotal = criarMiniCard("Total");
        lblDesconto = criarMiniCard("Desconto");
        lblComissao = criarMiniCard("Comissao");

        resumo.add((Component) lblItens.getParent());
        resumo.add((Component) lblTotal.getParent());
        resumo.add((Component) lblDesconto.getParent());
        resumo.add((Component) lblComissao.getParent());
        return resumo;
    }

    private JLabel criarMiniCard(String titulo) {
        JPanel card = new JPanel();
        card.setBackground(COR_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(COR_BORDA, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setForeground(new Color(100, 116, 139));
        lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JLabel lblValor = new JLabel("-");
        lblValor.setForeground(COR_TITULO);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 18));

        card.add(lblTitulo);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(lblValor);
        return lblValor;
    }

    private JPanel criarConteudo() {
        JPanel conteudo = new JPanel(new BorderLayout(0, 18));
        conteudo.setOpaque(false);
        conteudo.setBorder(new EmptyBorder(0, 18, 18, 18));

        conteudo.add(criarPainelCliente(), BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridLayout(1, 2, 18, 18));
        centro.setOpaque(false);
        centro.add(criarPainelProdutos());
        centro.add(criarPainelCarrinho());
        conteudo.add(centro, BorderLayout.CENTER);
        return conteudo;
    }

    private JPanel criarPainelCliente() {
        JPanel painel = new JPanel(new BorderLayout(12, 12));
        painel.setBackground(COR_CARD);
        painel.setBorder(criarBordaCard(18, 20));

        JPanel formulario = new JPanel(new GridLayout(4, 4, 12, 10));
        formulario.setOpaque(false);

        txtClienteNome = new JTextField();
        txtClienteCpf = new JTextField();
        cbSugestoesCliente = new JComboBox<>();
        cbSugestoesCliente.setMaximumRowCount(8);
        chkSemCadastro = new JCheckBox("Venda sem cadastro");
        chkEntrega = new JCheckBox("Entrega");
        chkSemCadastro.setOpaque(false);
        chkEntrega.setOpaque(false);
        lblClienteSelecionado = new JLabel("Cliente ainda nao selecionado");
        lblClienteSelecionado.setForeground(COR_TEXTO);

        JButton btnBuscar = new JButton("Buscar cliente");
        estilizarBotaoSecundario(btnBuscar);
        btnBuscar.addActionListener(e -> selecionarSugestaoOuBuscar());
        JButton btnUsarSugestao = new JButton("Usar sugestao");
        estilizarBotaoSecundario(btnUsarSugestao);
        btnUsarSugestao.addActionListener(e -> selecionarClienteDaSugestao());

        formulario.add(new JLabel("Nome do cliente"));
        formulario.add(new JLabel("CPF do cliente"));
        formulario.add(new JLabel(""));
        formulario.add(new JLabel(""));
        formulario.add(txtClienteNome);
        formulario.add(txtClienteCpf);
        formulario.add(btnBuscar);
        formulario.add(chkSemCadastro);
        formulario.add(new JLabel("Sugestoes"));
        formulario.add(cbSugestoesCliente);
        formulario.add(btnUsarSugestao);
        formulario.add(chkEntrega);
        formulario.add(new JLabel(""));
        formulario.add(new JLabel(""));
        formulario.add(new JLabel(""));
        formulario.add(new JLabel(""));

        DocumentListener limparSelecaoListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                limparClienteSelecionadoSeNecessario();
                atualizarSugestoesCliente();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                limparClienteSelecionadoSeNecessario();
                atualizarSugestoesCliente();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                limparClienteSelecionadoSeNecessario();
                atualizarSugestoesCliente();
            }
        };

        txtClienteNome.getDocument().addDocumentListener(limparSelecaoListener);
        txtClienteCpf.getDocument().addDocumentListener(limparSelecaoListener);
        txtClienteNome.addActionListener(e -> selecionarSugestaoOuBuscar());
        txtClienteCpf.addActionListener(e -> selecionarSugestaoOuBuscar());
        cbSugestoesCliente.addActionListener(e -> atualizarPreviewSugestao());
        chkSemCadastro.addActionListener(e -> atualizarModoCliente());
        chkEntrega.addActionListener(e -> atualizarModoEntrega());
        atualizarModoCliente();

        painel.add(formulario, BorderLayout.CENTER);
        painel.add(lblClienteSelecionado, BorderLayout.SOUTH);
        return painel;
    }

    private JPanel criarPainelProdutos() {
        JPanel painel = new JPanel(new BorderLayout(0, 14));
        painel.setBackground(COR_CARD);
        painel.setBorder(criarBordaCard(20, 20));

        JPanel topo = new JPanel(new BorderLayout(0, 10));
        topo.setOpaque(false);

        JLabel titulo = new JLabel("Produtos disponiveis");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setForeground(COR_TITULO);

        txtBuscaProduto = new JTextField();
        txtBuscaProduto.setToolTipText("Pesquise por nome ou codigo de barras");
        txtBuscaProduto.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicarFiltroProdutos();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicarFiltroProdutos();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aplicarFiltroProdutos();
            }
        });

        JPanel busca = new JPanel(new BorderLayout(8, 0));
        busca.setOpaque(false);
        busca.add(new JLabel("Busca inteligente:"), BorderLayout.WEST);
        busca.add(txtBuscaProduto, BorderLayout.CENTER);
        topo.add(titulo, BorderLayout.NORTH);
        topo.add(busca, BorderLayout.SOUTH);

        tabelaProdutos = new JTable(modeloProdutos);
        tabelaProdutos.setRowHeight(28);
        tabelaProdutos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelaProdutos.setGridColor(COR_BORDA);
        tabelaProdutos.setSelectionBackground(new Color(224, 242, 254));
        tabelaProdutos.setSelectionForeground(COR_TITULO);
        sorterProdutos = new TableRowSorter<>(modeloProdutos);
        tabelaProdutos.setRowSorter(sorterProdutos);
        tabelaProdutos.removeColumn(tabelaProdutos.getColumnModel().getColumn(4));

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        rodape.setOpaque(false);

        spQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        JSpinner.DefaultEditor editorQuantidade = (JSpinner.DefaultEditor) spQuantidade.getEditor();
        editorQuantidade.getTextField().setEditable(true);
        ((DefaultFormatter) editorQuantidade.getTextField().getFormatter()).setAllowsInvalid(false);
        JButton btnAdicionar = new JButton("Adicionar ao carrinho");
        JButton btnAtualizar = new JButton("Atualizar lista");
        estilizarBotaoPrimario(btnAdicionar, COR_AZUL, COR_AZUL_HOVER);
        estilizarBotaoSecundario(btnAtualizar);

        btnAdicionar.addActionListener(e -> adicionarSelecionado());
        btnAtualizar.addActionListener(e -> carregarProdutos());

        rodape.add(new JLabel("Quantidade:"));
        rodape.add(spQuantidade);
        rodape.add(btnAdicionar);
        rodape.add(btnAtualizar);

        painel.add(topo, BorderLayout.NORTH);
        painel.add(criarScrollTabela(tabelaProdutos), BorderLayout.CENTER);
        painel.add(rodape, BorderLayout.SOUTH);
        return painel;
    }

    private JPanel criarPainelCarrinho() {
        JPanel painel = new JPanel(new BorderLayout(0, 14));
        painel.setBackground(COR_CARD);
        painel.setBorder(criarBordaCard(20, 20));

        JLabel titulo = new JLabel("Carrinho da venda");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setForeground(COR_TITULO);

        tabelaCarrinho = new JTable(modeloCarrinho);
        tabelaCarrinho.setRowHeight(28);
        tabelaCarrinho.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelaCarrinho.setGridColor(COR_BORDA);
        tabelaCarrinho.setSelectionBackground(new Color(224, 242, 254));
        tabelaCarrinho.setSelectionForeground(COR_TITULO);

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acoes.setOpaque(false);

        JButton btnRemover = new JButton("Remover item");
        JButton btnLimpar = new JButton("Limpar carrinho");
        JButton btnFinalizar = new JButton("Finalizar venda");
        estilizarBotaoSecundario(btnRemover);
        estilizarBotaoSecundario(btnLimpar);
        estilizarBotaoPrimario(btnFinalizar, COR_VERDE, new Color(21, 128, 61));

        btnRemover.addActionListener(e -> removerItem());
        btnLimpar.addActionListener(e -> limparCarrinho());
        btnFinalizar.addActionListener(e -> finalizarVenda());

        acoes.add(btnRemover);
        acoes.add(btnLimpar);
        acoes.add(btnFinalizar);

        painel.add(titulo, BorderLayout.NORTH);
        painel.add(criarScrollTabela(tabelaCarrinho), BorderLayout.CENTER);
        painel.add(acoes, BorderLayout.SOUTH);
        return painel;
    }

    private void carregarProdutos() {
        modeloProdutos.setRowCount(0);
        for (Produto p : produtoDAO.listarTudo()) {
            String nomeExibicao = p.getNome() + " | " + p.getClasseComercial();
            modeloProdutos.addRow(new Object[]{
                    p.getId(),
                    nomeExibicao,
                    p.getQuantidade_estoque(),
                    formatarMoeda(p.getPreco()),
                    p.getCodigo_barras()
            });
        }
        aplicarFiltroProdutos();
    }

    private void adicionarSelecionado() {
        if (!chkSemCadastro.isSelected() && clienteSelecionado == null) {
            JOptionPane.showMessageDialog(this,
                    "Antes de vender, selecione um cliente valido ou marque a opcao de venda sem cadastro.");
            return;
        }

        int linha = tabelaProdutos.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um produto na lista.");
            return;
        }

        int linhaModelo = tabelaProdutos.convertRowIndexToModel(linha);
        int produtoId = Integer.parseInt(String.valueOf(modeloProdutos.getValueAt(linhaModelo, 0)));
        int quantidade = (Integer) spQuantidade.getValue();
        Produto produto = produtoDAO.buscarPorId(produtoId);

        if (produto == null) {
            JOptionPane.showMessageDialog(this, "Produto nao encontrado.");
            return;
        }

        if (quantidade > produto.getQuantidade_estoque()) {
            JOptionPane.showMessageDialog(this, "Estoque insuficiente para essa quantidade.");
            return;
        }

        ItemCarrinho itemExistente = encontrarNoCarrinho(produtoId);
        if (itemExistente == null) {
            carrinho.add(new ItemCarrinho(produto, quantidade));
        } else {
            int novaQtd = itemExistente.quantidade + quantidade;
            if (novaQtd > produto.getQuantidade_estoque()) {
                JOptionPane.showMessageDialog(this, "O carrinho ultrapassa o estoque disponivel.");
                return;
            }
            itemExistente.quantidade = novaQtd;
        }

        renderizarCarrinho();
    }

    private ItemCarrinho encontrarNoCarrinho(int produtoId) {
        for (ItemCarrinho item : carrinho) {
            if (item.produto.getId() == produtoId) {
                return item;
            }
        }
        return null;
    }

    private void renderizarCarrinho() {
        modeloCarrinho.setRowCount(0);
        for (ItemCarrinho item : carrinho) {
            modeloCarrinho.addRow(new Object[]{
                    item.produto.getNome() + " | " + item.produto.getClasseComercial(),
                    item.quantidade,
                    formatarMoeda(item.getPrecoUnitarioEfetivo(clienteSelecionado != null && !chkSemCadastro.isSelected())),
                    formatarMoeda(item.getSubtotal(clienteSelecionado != null && !chkSemCadastro.isSelected()))
            });
        }
        atualizarResumo();
    }

    private void atualizarResumo() {
        int itens = carrinho.stream().mapToInt(item -> item.quantidade).sum();
        boolean clienteComDesconto = clienteSelecionado != null && !chkSemCadastro.isSelected();
        double total = carrinho.stream().mapToDouble(item -> item.getSubtotal(clienteComDesconto)).sum();
        double desconto = carrinho.stream().mapToDouble(item -> item.getDescontoTotal(clienteComDesconto)).sum();
        double subtotalComissionavel = carrinho.stream().mapToDouble(item -> item.getSubtotalComissionavel(clienteComDesconto)).sum();
        double comissao = VendaDAO.calcularComissao(subtotalComissionavel);

        lblItens.setText(String.valueOf(itens));
        lblTotal.setText(formatarMoeda(total));
        lblDesconto.setText(formatarMoeda(desconto));
        lblComissao.setText(formatarMoeda(comissao));
    }

    private void removerItem() {
        int linha = tabelaCarrinho.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um item do carrinho.");
            return;
        }
        carrinho.remove(linha);
        renderizarCarrinho();
    }

    private void limparCarrinho() {
        carrinho.clear();
        renderizarCarrinho();
    }

    private void finalizarVenda() {
        if (carrinho.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Adicione pelo menos um item antes de finalizar.");
            return;
        }

        if (!chkSemCadastro.isSelected() && clienteSelecionado == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um cliente por nome ou CPF, ou marque a opcao de venda sem cadastro.");
            return;
        }

        if (chkEntrega.isSelected() && clienteSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Entrega exige cliente cadastrado e selecionado.");
            return;
        }

        if (possuiItensControlados() && clienteSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Medicamento controlado exige cliente cadastrado e receita preenchida.");
            return;
        }

        for (ItemCarrinho item : carrinho) {
            Produto produtoAtual = produtoDAO.buscarPorId(item.produto.getId());
            if (produtoAtual == null || produtoAtual.getQuantidade_estoque() < item.quantidade) {
                JOptionPane.showMessageDialog(this,
                        "O estoque mudou antes da finalizacao. Atualize a tela e tente novamente.");
                carregarProdutos();
                return;
            }
        }

        int totalItens = carrinho.stream().mapToInt(item -> item.quantidade).sum();
        boolean clienteComDesconto = clienteSelecionado != null && !chkSemCadastro.isSelected();
        double total = carrinho.stream().mapToDouble(item -> item.getSubtotal(clienteComDesconto)).sum();
        double descontoTotal = carrinho.stream().mapToDouble(item -> item.getDescontoTotal(clienteComDesconto)).sum();
        double subtotalComissionavel = carrinho.stream().mapToDouble(item -> item.getSubtotalComissionavel(clienteComDesconto)).sum();
        double comissaoTotal = VendaDAO.calcularComissao(subtotalComissionavel);
        String resumoItens = montarResumoItens();
        String data = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        EntregaInfo entregaInfo = null;
        ReceitaControladaInfo receitaInfo = null;
        double totalComTaxa = total;
        String tipoVenda = chkEntrega.isSelected() ? "Entrega" : "Balcao";
        String statusComanda = chkEntrega.isSelected() ? "Aguardando entrega" : "Aberta";

        if (chkEntrega.isSelected()) {
            entregaInfo = abrirDadosEntrega(total);
            if (entregaInfo == null) {
                return;
            }
            totalComTaxa += entregaInfo.taxaEntrega;
        }

        if (possuiItensControlados()) {
            receitaInfo = abrirReceitaControlada(totalComTaxa);
            if (receitaInfo == null) {
                return;
            }
        }

        String comanda = abrirFechamentoComanda(totalComTaxa);
        if (comanda == null) {
            return;
        }

        for (ItemCarrinho item : carrinho) {
            produtoDAO.baixarEstoque(item.produto.getId(), item.quantidade);
        }

        if (comandaCaixaDAO.registrarComanda(usuarioLogado, clienteSelecionado, chkSemCadastro.isSelected(),
                comanda, resumoItens, montarDetalheItens(totalComTaxa, entregaInfo, receitaInfo), totalItens, totalComTaxa,
                descontoTotal, comissaoTotal, tipoVenda, statusComanda,
                entregaInfo == null ? null : entregaInfo.endereco,
                entregaInfo == null ? null : entregaInfo.bairro,
                entregaInfo == null ? null : entregaInfo.cidade,
                entregaInfo == null ? 0.0 : entregaInfo.taxaEntrega,
                data)) {
            JOptionPane.showMessageDialog(this,
                    "Comanda fechada com sucesso.\nComanda: " + comanda
                            + "\nTipo: " + tipoVenda
                            + "\nTotal aguardando pagamento: " + formatarMoeda(totalComTaxa)
                            + "\nDesconto aplicado: " + formatarMoeda(descontoTotal)
                            + "\nComissao estimada: " + formatarMoeda(comissaoTotal));
            limparCarrinho();
            limparClientePosVenda();
            carregarProdutos();
        } else {
            for (ItemCarrinho item : carrinho) {
                produtoDAO.adicionarQuantidadePorId(item.produto.getId(), item.quantidade);
            }
            JOptionPane.showMessageDialog(this, "Nao foi possivel fechar a comanda.");
        }
    }

    private boolean possuiItensControlados() {
        return carrinho.stream().anyMatch(item -> item.produto.isControlado());
    }

    private List<ItemCarrinho> listarItensControlados() {
        List<ItemCarrinho> controlados = new ArrayList<>();
        for (ItemCarrinho item : carrinho) {
            if (item.produto.isControlado()) {
                controlados.add(item);
            }
        }
        return controlados;
    }

    private void selecionarSugestaoOuBuscar() {
        if (cbSugestoesCliente.getItemCount() > 0 && cbSugestoesCliente.getSelectedItem() != null) {
            selecionarClienteDaSugestao();
            return;
        }
        buscarCliente();
    }

    private void buscarCliente() {
        if (chkSemCadastro.isSelected()) {
            JOptionPane.showMessageDialog(this, "Desmarque 'Venda sem cadastro' para buscar um cliente.");
            return;
        }

        String nome = txtClienteNome.getText().trim();
        String cpf = txtClienteCpf.getText().trim();

        if (!cpf.isBlank()) {
            if (!CpfUtils.isValido(cpf)) {
                JOptionPane.showMessageDialog(this, "Informe um CPF valido para a busca.");
                return;
            }

            Cliente cliente = clienteDAO.buscarPorCpf(cpf);
            if (cliente == null) {
                JOptionPane.showMessageDialog(this, "Nenhum cliente encontrado com esse CPF.");
                return;
            }

            selecionarCliente(cliente);
            return;
        }

        if (nome.isBlank()) {
            JOptionPane.showMessageDialog(this, "Informe o nome ou o CPF do cliente.");
            return;
        }

        List<Cliente> encontrados = clienteDAO.buscarPorNomeParcial(nome);
        if (encontrados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum cliente encontrado com esse nome.");
            return;
        }

        if (encontrados.size() == 1) {
            selecionarCliente(encontrados.get(0));
            return;
        }

        Cliente escolhido = (Cliente) JOptionPane.showInputDialog(
                this,
                "Selecione o cliente:",
                "Clientes encontrados",
                JOptionPane.PLAIN_MESSAGE,
                null,
                encontrados.toArray(),
                encontrados.get(0)
        );

        if (escolhido != null) {
            selecionarCliente(escolhido);
        }
    }

    private void atualizarSugestoesCliente() {
        if (chkSemCadastro.isSelected()) {
            cbSugestoesCliente.removeAllItems();
            return;
        }

        String nome = txtClienteNome.getText().trim();
        String cpf = txtClienteCpf.getText().trim();

        if (nome.isBlank() && cpf.isBlank()) {
            cbSugestoesCliente.removeAllItems();
            return;
        }

        List<Cliente> encontrados = clienteDAO.buscarInteligente(nome, cpf);
        Object selecionadoAtual = cbSugestoesCliente.getSelectedItem();
        cbSugestoesCliente.removeAllItems();
        for (Cliente cliente : encontrados) {
            cbSugestoesCliente.addItem(cliente);
        }

        if (selecionadoAtual instanceof Cliente clienteAtual) {
            for (int i = 0; i < cbSugestoesCliente.getItemCount(); i++) {
                Cliente item = cbSugestoesCliente.getItemAt(i);
                if (item.getId().equals(clienteAtual.getId())) {
                    cbSugestoesCliente.setSelectedIndex(i);
                    break;
                }
            }
        }

        atualizarPreviewSugestao();
    }

    private void atualizarPreviewSugestao() {
        if (clienteSelecionado != null || chkSemCadastro.isSelected()) {
            return;
        }
        Cliente sugestao = (Cliente) cbSugestoesCliente.getSelectedItem();
        if (sugestao != null) {
            lblClienteSelecionado.setText("Sugestao: " + sugestao.getNome() + " | CPF: " + CpfUtils.formatar(sugestao.getCpf()));
        } else {
            lblClienteSelecionado.setText("Cliente ainda nao selecionado");
        }
    }

    private void selecionarClienteDaSugestao() {
        Cliente cliente = (Cliente) cbSugestoesCliente.getSelectedItem();
        if (cliente != null) {
            selecionarCliente(cliente);
        }
    }

    private void selecionarCliente(Cliente cliente) {
        atualizandoClienteUI = true;
        txtClienteNome.setText(cliente.getNome());
        txtClienteCpf.setText(CpfUtils.formatar(cliente.getCpf()));
        atualizandoClienteUI = false;
        this.clienteSelecionado = cliente;
        lblClienteSelecionado.setText("Cliente selecionado: " + cliente.getNome()
                + " | CPF: " + CpfUtils.formatar(cliente.getCpf())
                + " | Originais com 12% de desconto");
        atualizarResumo();
    }

    private void atualizarModoCliente() {
        boolean semCadastro = chkSemCadastro.isSelected();
        txtClienteNome.setEnabled(!semCadastro);
        txtClienteCpf.setEnabled(!semCadastro);

        if (semCadastro) {
            chkEntrega.setSelected(false);
            chkEntrega.setEnabled(false);
            clienteSelecionado = null;
            txtClienteNome.setText("");
            txtClienteCpf.setText("");
            cbSugestoesCliente.removeAllItems();
            cbSugestoesCliente.setEnabled(false);
            lblClienteSelecionado.setText("Venda configurada sem cadastro de cliente.");
        } else if (clienteSelecionado == null) {
            chkEntrega.setEnabled(true);
            cbSugestoesCliente.setEnabled(true);
            lblClienteSelecionado.setText("Cliente ainda nao selecionado");
        } else {
            chkEntrega.setEnabled(true);
        }
        atualizarResumo();
    }

    private void atualizarModoEntrega() {
        if (chkEntrega.isSelected()) {
            chkSemCadastro.setSelected(false);
            chkSemCadastro.setEnabled(false);
            if (clienteSelecionado == null) {
                lblClienteSelecionado.setText("Entrega exige cliente cadastrado. Selecione um cliente antes de finalizar.");
            }
        } else {
            chkSemCadastro.setEnabled(true);
            if (clienteSelecionado == null && !chkSemCadastro.isSelected()) {
                lblClienteSelecionado.setText("Cliente ainda nao selecionado");
            }
        }
    }

    private void limparClienteSelecionadoSeNecessario() {
        if (atualizandoClienteUI || clienteSelecionado == null || chkSemCadastro.isSelected()) {
            return;
        }

        String cpfAtual = CpfUtils.apenasDigitos(txtClienteCpf.getText());
        String nomeAtual = txtClienteNome.getText().trim();
        if (!nomeAtual.equalsIgnoreCase(clienteSelecionado.getNome())
                || !cpfAtual.equals(CpfUtils.apenasDigitos(clienteSelecionado.getCpf()))) {
            clienteSelecionado = null;
            lblClienteSelecionado.setText("Cliente alterado. Faca uma nova busca para validar.");
        }
    }

    private void limparClientePosVenda() {
        chkSemCadastro.setSelected(false);
        chkSemCadastro.setEnabled(true);
        chkEntrega.setSelected(false);
        chkEntrega.setEnabled(true);
        clienteSelecionado = null;
        txtClienteNome.setText("");
        txtClienteCpf.setText("");
        cbSugestoesCliente.removeAllItems();
        lblClienteSelecionado.setText("Cliente ainda nao selecionado");
        atualizarModoCliente();
    }

    private void aplicarFiltroProdutos() {
        if (sorterProdutos == null) {
            return;
        }

        String termo = normalizar(txtBuscaProduto == null ? "" : txtBuscaProduto.getText());
        if (termo.isBlank()) {
            sorterProdutos.setRowFilter(null);
            return;
        }

        sorterProdutos.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String nome = normalizar(String.valueOf(entry.getValue(1)));
                String codigo = normalizar(String.valueOf(entry.getValueCount() > 4 ? entry.getValue(4) : ""));
                return nome.contains(termo) || codigo.contains(termo);
            }
        });
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.toLowerCase(Locale.ROOT).trim();
    }

    private String montarResumoItens() {
        StringBuilder resumo = new StringBuilder();
        for (int i = 0; i < carrinho.size(); i++) {
            ItemCarrinho item = carrinho.get(i);
            if (i > 0) {
                resumo.append(" | ");
            }
            resumo.append(item.produto.getNome())
                    .append(" [").append(item.produto.getClasseComercial()).append("]")
                    .append(" x").append(item.quantidade);
        }
        return resumo.toString();
    }

    public static String formatarMoeda(double valor) {
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(valor);
    }

    private JScrollPane criarScrollTabela(JTable tabela) {
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(new LineBorder(COR_BORDA, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private CompoundBorder criarBordaCard(int vertical, int horizontal) {
        return new CompoundBorder(
                new LineBorder(COR_BORDA, 1, true),
                new EmptyBorder(vertical, horizontal, vertical, horizontal)
        );
    }

    private void estilizarBotaoPrimario(JButton botao, Color base, Color hover) {
        botao.setBackground(base);
        botao.setForeground(Color.WHITE);
        botao.setFocusPainted(false);
        botao.setBorderPainted(false);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 13));
        botao.setBorder(new EmptyBorder(10, 16, 10, 16));
        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (botao.isEnabled()) {
                    botao.setBackground(hover);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(base);
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

    private String abrirFechamentoComanda(double total) {
        JDialog dialog = new JDialog(this, "Caixa de Pagamento", true);
        dialog.setSize(430, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(0, 14));

        JPanel conteudo = new JPanel(new GridBagLayout());
        conteudo.setBorder(new EmptyBorder(18, 18, 18, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtComanda = new JTextField();
        ((AbstractDocument) txtComanda.getDocument()).setDocumentFilter(new ApenasTresDigitosFilter());
        JLabel lblTotal = new JLabel(formatarMoeda(total));
        JLabel lblOrientacao = new JLabel("<html>Comanda: use leitor de codigo de barras ou digite manualmente de <b>001</b> ate <b>999</b>.</html>");
        lblOrientacao.setForeground(new Color(71, 85, 105));

        gbc.gridx = 0;
        gbc.gridy = 0;
        conteudo.add(new JLabel("Comanda:"), gbc);
        gbc.gridx = 1;
        conteudo.add(txtComanda, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        conteudo.add(new JLabel("Valor total:"), gbc);
        gbc.gridx = 1;
        conteudo.add(lblTotal, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        conteudo.add(lblOrientacao, gbc);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnConfirmar = new JButton("Fechar comanda");
        final String[] resultado = {null};

        btnCancelar.addActionListener(e -> dialog.dispose());
        btnConfirmar.addActionListener(e -> {
            String comandaFormatada = normalizarComanda(txtComanda.getText());
            if (comandaFormatada == null) {
                JOptionPane.showMessageDialog(dialog, "Informe uma comanda valida de 001 ate 999.");
                return;
            }
            if (comandaCaixaDAO.existeComandaAberta(comandaFormatada)) {
                JOptionPane.showMessageDialog(dialog, "Essa comanda ja esta aberta no caixa.");
                return;
            }
            resultado[0] = comandaFormatada;
            dialog.dispose();
        });

        botoes.add(btnCancelar);
        botoes.add(btnConfirmar);

        dialog.add(conteudo, BorderLayout.CENTER);
        dialog.add(botoes, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(btnConfirmar);
        SwingUtilities.invokeLater(txtComanda::requestFocusInWindow);
        dialog.setVisible(true);
        return resultado[0];
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

    private EntregaInfo abrirDadosEntrega(double totalAtual) {
        JDialog dialog = new JDialog(this, "Dados da entrega", true);
        dialog.setSize(520, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(0, 12));

        JPanel formulario = new JPanel(new GridLayout(6, 2, 10, 10));
        formulario.setBorder(new EmptyBorder(18, 18, 18, 18));

        JTextField txtEndereco = new JTextField();
        JTextField txtBairro = new JTextField();
        JTextField txtCidade = new JTextField();
        JCheckBox chkTaxa = new JCheckBox("Aplicar taxa de entrega");
        JTextField txtTaxa = new JTextField("0,00");
        txtTaxa.setEnabled(false);
        JLabel lblTotalAtual = new JLabel(formatarMoeda(totalAtual));

        chkTaxa.addActionListener(e -> txtTaxa.setEnabled(chkTaxa.isSelected()));

        formulario.add(new JLabel("Endereco:"));
        formulario.add(txtEndereco);
        formulario.add(new JLabel("Bairro:"));
        formulario.add(txtBairro);
        formulario.add(new JLabel("Cidade:"));
        formulario.add(txtCidade);
        formulario.add(chkTaxa);
        formulario.add(txtTaxa);
        formulario.add(new JLabel("Valor atual da venda:"));
        formulario.add(lblTotalAtual);
        formulario.add(new JLabel(""));
        formulario.add(new JLabel("A entrega so pode seguir com cliente cadastrado."));

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnConfirmar = new JButton("Salvar entrega");
        final EntregaInfo[] resultado = {null};

        btnCancelar.addActionListener(e -> dialog.dispose());
        btnConfirmar.addActionListener(e -> {
            if (txtEndereco.getText().trim().isBlank()
                    || txtBairro.getText().trim().isBlank()
                    || txtCidade.getText().trim().isBlank()) {
                JOptionPane.showMessageDialog(dialog, "Preencha endereco, bairro e cidade da entrega.");
                return;
            }

            EntregaInfo info = new EntregaInfo();
            info.endereco = txtEndereco.getText().trim();
            info.bairro = txtBairro.getText().trim();
            info.cidade = txtCidade.getText().trim();
            info.taxaEntrega = chkTaxa.isSelected() ? parseValorDigitado(txtTaxa.getText()) : 0.0;
            resultado[0] = info;
            dialog.dispose();
        });

        botoes.add(btnCancelar);
        botoes.add(btnConfirmar);
        dialog.add(formulario, BorderLayout.CENTER);
        dialog.add(botoes, BorderLayout.SOUTH);
        dialog.setVisible(true);
        return resultado[0];
    }

    private double parseValorDigitado(String valor) {
        try {
            String texto = valor == null ? "0" : valor.trim();
            return Double.parseDouble(texto.replace(".", "").replace(",", "."));
        } catch (Exception e) {
            return 0.0;
        }
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

    private static class ItemCarrinho {
        private final Produto produto;
        private int quantidade;

        private ItemCarrinho(Produto produto, int quantidade) {
            this.produto = produto;
            this.quantidade = quantidade;
        }

        private double getPrecoUnitarioEfetivo(boolean clienteIdentificado) {
            if (clienteIdentificado && produto.isOriginal()) {
                return produto.getPreco() * (1 - produto.getPercentualDescontoCliente());
            }
            return produto.getPreco();
        }

        private double getSubtotal(boolean clienteIdentificado) {
            return getPrecoUnitarioEfetivo(clienteIdentificado) * quantidade;
        }

        private double getDescontoTotal(boolean clienteIdentificado) {
            if (clienteIdentificado && produto.isOriginal()) {
                return produto.getPreco() * produto.getPercentualDescontoCliente() * quantidade;
            }
            return 0;
        }

        private double getSubtotalComissionavel(boolean clienteIdentificado) {
            if (!produto.geraComissao()) {
                return 0;
            }
            return getSubtotal(clienteIdentificado);
        }
    }

    private String montarDetalheItens(double totalComTaxa, EntregaInfo entregaInfo, ReceitaControladaInfo receitaInfo) {
        StringBuilder detalhe = new StringBuilder();
        boolean clienteComDesconto = clienteSelecionado != null && !chkSemCadastro.isSelected();
        for (ItemCarrinho item : carrinho) {
            if (detalhe.length() > 0) {
                detalhe.append("\n");
            }
            detalhe.append(item.produto.getNome())
                    .append(" | qtd ").append(item.quantidade)
                    .append(" | unit ").append(formatarMoeda(item.getPrecoUnitarioEfetivo(clienteComDesconto)))
                    .append(" | subtotal ").append(formatarMoeda(item.getSubtotal(clienteComDesconto)));
        }
        if (receitaInfo != null) {
            detalhe.append("\n\nReceita controlada")
                    .append("\nPaciente: ").append(clienteSelecionado == null ? "Nao informado" : clienteSelecionado.getNome())
                    .append("\nDocumento comprador: ").append(receitaInfo.tipoDocumentoComprador)
                    .append(" | ").append(receitaInfo.documentoComprador)
                    .append("\nTelefone comprador: ").append(receitaInfo.telefoneComprador)
                    .append("\nEndereco comprador: ").append(receitaInfo.enderecoComprador)
                    .append(", ").append(receitaInfo.numeroComprador)
                    .append(" - ").append(receitaInfo.bairroComprador)
                    .append(" - ").append(receitaInfo.cidadeComprador)
                    .append("\nTipo de receita: ").append(receitaInfo.tipoReceita)
                    .append("\nUso do medicamento: ").append(receitaInfo.usoMedicamento)
                    .append("\nData da receita: ").append(receitaInfo.dataReceita)
                    .append("\nNumero da receita: ").append(receitaInfo.numeroReceita)
                    .append("\nQtd receitada: ").append(receitaInfo.quantidadeReceitada)
                    .append("\nMedico: ").append(receitaInfo.nomeMedico)
                    .append("\nRegistro: ").append(receitaInfo.tipoRegistro).append(" ").append(receitaInfo.numeroRegistro)
                    .append(receitaInfo.ufRegistro.isBlank() ? "" : "/" + receitaInfo.ufRegistro)
                    .append("\nEndereco do prescritor: ").append(receitaInfo.enderecoMedico)
                    .append(", ").append(receitaInfo.numeroEnderecoMedico)
                    .append(" - ").append(receitaInfo.bairroMedico)
                    .append(" - ").append(receitaInfo.cidadeMedico)
                    .append("\nItens controlados:");
            for (ItemCarrinho itemControlado : listarItensControlados()) {
                detalhe.append("\n- ").append(itemControlado.produto.getNome())
                        .append(" | qtd ").append(itemControlado.quantidade);
            }
            if (receitaInfo.observacao != null && !receitaInfo.observacao.isBlank()) {
                detalhe.append("\nObservacao da receita: ").append(receitaInfo.observacao);
            }
        }
        if (entregaInfo != null) {
            detalhe.append("\n\nTipo: Entrega")
                    .append("\nEndereco: ").append(entregaInfo.endereco)
                    .append("\nBairro: ").append(entregaInfo.bairro)
                    .append("\nCidade: ").append(entregaInfo.cidade)
                    .append("\nTaxa de entrega: ").append(formatarMoeda(entregaInfo.taxaEntrega))
                    .append("\nTotal com entrega: ").append(formatarMoeda(totalComTaxa));
        }
        return detalhe.toString();
    }

    private static class EntregaInfo {
        private String endereco;
        private String bairro;
        private String cidade;
        private double taxaEntrega;
    }

    private ReceitaControladaInfo abrirReceitaControlada(double totalAtual) {
        JDialog dialog = new JDialog(this, "Receita de medicamento controlado", true);
        dialog.setSize(1040, 720);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(0, 10));

        JPanel topo = new JPanel(new BorderLayout(12, 12));
        topo.setBackground(Color.WHITE);
        topo.setBorder(new EmptyBorder(16, 18, 8, 18));
        JLabel titulo = new JLabel("<html><b>Preencha os dados da receita controlada</b><br>Confira os produtos da venda, o prescritor e os dados do comprador antes de confirmar.</html>");
        titulo.setForeground(new Color(15, 23, 42));
        JLabel lblCliente = new JLabel("Cliente: " + clienteSelecionado.getNome() + " | CPF: " + CpfUtils.formatar(clienteSelecionado.getCpf()));
        lblCliente.setForeground(new Color(71, 85, 105));
        JPanel blocoTitulo = new JPanel();
        blocoTitulo.setOpaque(false);
        blocoTitulo.setLayout(new BoxLayout(blocoTitulo, BoxLayout.Y_AXIS));
        blocoTitulo.add(titulo);
        blocoTitulo.add(Box.createRigidArea(new Dimension(0, 6)));
        blocoTitulo.add(lblCliente);
        topo.add(blocoTitulo, BorderLayout.WEST);

        JTable tabelaItens = new JTable(new DefaultTableModel(
                new String[]{"Produto", "Qtd", "Controle", "Situacao"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        DefaultTableModel modeloItens = (DefaultTableModel) tabelaItens.getModel();
        for (ItemCarrinho item : carrinho) {
            modeloItens.addRow(new Object[]{
                    item.produto.getNome(),
                    item.quantidade,
                    item.produto.getTipoControle(),
                    item.produto.isControlado() ? "Controlado" : "Livre"
            });
        }
        tabelaItens.setRowHeight(26);
        JScrollPane scrollItens = new JScrollPane(tabelaItens);
        scrollItens.setBorder(BorderFactory.createTitledBorder("Produtos da venda"));
        scrollItens.setPreferredSize(new Dimension(960, 160));

        JTextField txtBuscaMedico = new JTextField();
        JComboBox<Medico> cbMedicos = new JComboBox<>();
        cbMedicos.setMaximumRowCount(8);
        JButton btnCadastrarMedico = new JButton("Cadastrar medico");
        Icon icon = UIManager.getIcon("FileView.fileIcon");
        if (icon != null) {
            btnCadastrarMedico.setIcon(icon);
        }

        JComboBox<String> cbTipoReceita = new JComboBox<>(new String[]{
                "Branca comum", "Azul B", "Amarela A", "Antimicrobiano", "Controle especial"
        });
        cbTipoReceita.setSelectedItem(inferirTipoReceitaPorMedicamento());
        JComboBox<String> cbUsoMedicamento = new JComboBox<>(new String[]{
                "1 - Humano", "2 - Veterinario", "3 - Uso continuo"
        });
        JComboBox<String> cbTipoRegistro = new JComboBox<>(new String[]{"CRM", "CRO", "RMS", "RQE", "CRBM"});
        JTextField txtNumeroRegistro = new JTextField();
        JTextField txtUfRegistro = new JTextField();
        JTextField txtNumeroReceita = new JTextField();
        JTextField txtDataReceita = new JTextField(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        JTextField txtQuantidadeReceitada = new JTextField(String.valueOf(totalItensControlados()));
        JTextField txtNomeComprador = new JTextField(clienteSelecionado.getNome());
        JTextField txtTelefoneComprador = new JTextField(clienteSelecionado.getTelefone() == null ? "" : clienteSelecionado.getTelefone());
        JComboBox<String> cbTipoDocumentoComprador = new JComboBox<>(new String[]{"CPF", "RG", "CNH", "Passaporte"});
        JTextField txtDocumentoComprador = new JTextField(CpfUtils.formatar(clienteSelecionado.getCpf()));
        JTextField txtEnderecoComprador = new JTextField();
        JTextField txtNumeroComprador = new JTextField();
        JTextField txtBairroComprador = new JTextField();
        JTextField txtCidadeComprador = new JTextField();
        JTextField txtEnderecoMedico = new JTextField();
        JTextField txtNumeroEnderecoMedico = new JTextField();
        JTextField txtBairroMedico = new JTextField();
        JTextField txtCidadeMedico = new JTextField();
        final boolean[] atualizandoMedicoUI = {false};
        JLabel lblTotal = new JLabel("Total da comanda: " + formatarMoeda(totalAtual));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotal.setForeground(new Color(91, 33, 182));
        JTextArea txtObservacao = new JTextArea(3, 20);
        txtObservacao.setLineWrap(true);
        txtObservacao.setWrapStyleWord(true);

        txtBuscaMedico.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!atualizandoMedicoUI[0]) {
                    atualizarSugestoesMedico(txtBuscaMedico, cbMedicos);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!atualizandoMedicoUI[0]) {
                    atualizarSugestoesMedico(txtBuscaMedico, cbMedicos);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!atualizandoMedicoUI[0]) {
                    atualizarSugestoesMedico(txtBuscaMedico, cbMedicos);
                }
            }
        });

        DocumentListener buscaRegistroListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!atualizandoMedicoUI[0]) {
                    atualizarSugestoesMedicoPorRegistro(txtBuscaMedico, cbMedicos, cbTipoRegistro, txtNumeroRegistro, txtUfRegistro);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!atualizandoMedicoUI[0]) {
                    atualizarSugestoesMedicoPorRegistro(txtBuscaMedico, cbMedicos, cbTipoRegistro, txtNumeroRegistro, txtUfRegistro);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!atualizandoMedicoUI[0]) {
                    atualizarSugestoesMedicoPorRegistro(txtBuscaMedico, cbMedicos, cbTipoRegistro, txtNumeroRegistro, txtUfRegistro);
                }
            }
        };
        txtNumeroRegistro.getDocument().addDocumentListener(buscaRegistroListener);
        txtUfRegistro.getDocument().addDocumentListener(buscaRegistroListener);
        cbTipoRegistro.addActionListener(e -> {
            if (!atualizandoMedicoUI[0]) {
                atualizarSugestoesMedicoPorRegistro(txtBuscaMedico, cbMedicos, cbTipoRegistro, txtNumeroRegistro, txtUfRegistro);
            }
        });

        cbMedicos.addActionListener(e -> {
            Medico medico = (Medico) cbMedicos.getSelectedItem();
            if (medico != null) {
                atualizandoMedicoUI[0] = true;
                txtBuscaMedico.setText(medico.getNome());
                cbTipoRegistro.setSelectedItem(medico.getTipoRegistro());
                txtNumeroRegistro.setText(medico.getNumeroRegistro());
                txtUfRegistro.setText(medico.getUfRegistro());
                atualizandoMedicoUI[0] = false;
            }
        });

        btnCadastrarMedico.addActionListener(e -> {
            Medico cadastrado = abrirCadastroMedicoRapido(dialog);
            if (cadastrado != null) {
                txtBuscaMedico.setText(cadastrado.getNome());
                atualizarSugestoesMedico(txtBuscaMedico, cbMedicos);
                for (int i = 0; i < cbMedicos.getItemCount(); i++) {
                    Medico item = cbMedicos.getItemAt(i);
                    if (item.getId() != null && item.getId().equals(cadastrado.getId())) {
                        cbMedicos.setSelectedIndex(i);
                        break;
                    }
                }
            }
        });

        JPanel painelPrescritor = criarSecaoReceita("Dados do prescritor");
        adicionarCampoSecao(painelPrescritor, "Conselho", cbTipoRegistro, 0, 0, 1);
        adicionarCampoSecao(painelPrescritor, "Registro do prescritor", txtNumeroRegistro, 0, 1, 1);
        adicionarCampoSecao(painelPrescritor, "UF cons.", txtUfRegistro, 0, 2, 1);
        adicionarCampoSecao(painelPrescritor, "Buscar medico", txtBuscaMedico, 1, 0, 2);
        adicionarCampoSecao(painelPrescritor, "", btnCadastrarMedico, 1, 2, 1);
        adicionarCampoSecao(painelPrescritor, "Sugestoes", cbMedicos, 2, 0, 3);
        adicionarCampoSecao(painelPrescritor, "Endereco", txtEnderecoMedico, 3, 0, 1);
        adicionarCampoSecao(painelPrescritor, "Numero", txtNumeroEnderecoMedico, 3, 1, 1);
        adicionarCampoSecao(painelPrescritor, "Bairro", txtBairroMedico, 3, 2, 1);
        adicionarCampoSecao(painelPrescritor, "Cidade", txtCidadeMedico, 4, 0, 3);

        JPanel painelComprador = criarSecaoReceita("Dados do comprador / paciente");
        adicionarCampoSecao(painelComprador, "Nome / razao do comprador", txtNomeComprador, 0, 0, 2);
        adicionarCampoSecao(painelComprador, "Celular / Telefone", txtTelefoneComprador, 0, 2, 1);
        adicionarCampoSecao(painelComprador, "Endereco", txtEnderecoComprador, 1, 0, 2);
        adicionarCampoSecao(painelComprador, "Numero", txtNumeroComprador, 1, 2, 1);
        adicionarCampoSecao(painelComprador, "Tipo de documento", cbTipoDocumentoComprador, 2, 0, 1);
        adicionarCampoSecao(painelComprador, "Numero do documento", txtDocumentoComprador, 2, 1, 1);
        adicionarCampoSecao(painelComprador, "Cidade", txtCidadeComprador, 2, 2, 1);
        adicionarCampoSecao(painelComprador, "Bairro", txtBairroComprador, 3, 0, 3);

        JPanel painelReceita = criarSecaoReceita("Dados da receita e medicamento");
        adicionarCampoSecao(painelReceita, "Tipo de receituario", cbTipoReceita, 0, 0, 1);
        adicionarCampoSecao(painelReceita, "Uso medicamento", cbUsoMedicamento, 0, 1, 1);
        adicionarCampoSecao(painelReceita, "Nr receita", txtNumeroReceita, 0, 2, 1);
        adicionarCampoSecao(painelReceita, "Data da receita", txtDataReceita, 1, 0, 1);
        adicionarCampoSecao(painelReceita, "Qtd receitada", txtQuantidadeReceitada, 1, 1, 1);
        adicionarCampoSecao(painelReceita, "Valor da venda", lblTotal, 1, 2, 1);
        adicionarCampoSecao(painelReceita, "Observacao", new JScrollPane(txtObservacao), 2, 0, 3);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botoes.setBackground(Color.WHITE);
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnConfirmar = new JButton("Salvar receita");
        final ReceitaControladaInfo[] resultado = {null};

        btnCancelar.addActionListener(e -> dialog.dispose());
        btnConfirmar.addActionListener(e -> {
            String tipoRegistro = String.valueOf(cbTipoRegistro.getSelectedItem());
            String numeroRegistro = txtNumeroRegistro.getText().trim();
            String ufRegistro = txtUfRegistro.getText().trim().toUpperCase(Locale.ROOT);
            String numeroReceita = txtNumeroReceita.getText().trim();
            String dataReceita = txtDataReceita.getText().trim();
            String quantidadeReceitada = txtQuantidadeReceitada.getText().trim();
            Medico medicoSelecionado = (Medico) cbMedicos.getSelectedItem();
            String nomeMedico = medicoSelecionado != null ? medicoSelecionado.getNome() : txtBuscaMedico.getText().trim();

            if (nomeMedico.isBlank() || numeroRegistro.isBlank() || numeroReceita.isBlank()
                    || dataReceita.isBlank() || txtNomeComprador.getText().trim().isBlank()
                    || quantidadeReceitada.isBlank()) {
                mostrarAvisoSistema(dialog,
                        "Receita controlada incompleta",
                        "Preencha medico, registro, comprador, numero da receita, quantidade receitada e data da receita.");
                return;
            }

            ReceitaControladaInfo info = new ReceitaControladaInfo();
            info.tipoReceita = String.valueOf(cbTipoReceita.getSelectedItem());
            info.usoMedicamento = String.valueOf(cbUsoMedicamento.getSelectedItem());
            info.dataReceita = dataReceita;
            info.numeroReceita = numeroReceita;
            info.quantidadeReceitada = quantidadeReceitada;
            info.nomeMedico = nomeMedico;
            info.tipoRegistro = tipoRegistro;
            info.numeroRegistro = numeroRegistro;
            info.ufRegistro = ufRegistro;
            info.enderecoMedico = txtEnderecoMedico.getText().trim();
            info.numeroEnderecoMedico = txtNumeroEnderecoMedico.getText().trim();
            info.bairroMedico = txtBairroMedico.getText().trim();
            info.cidadeMedico = txtCidadeMedico.getText().trim();
            info.nomeComprador = txtNomeComprador.getText().trim();
            info.telefoneComprador = txtTelefoneComprador.getText().trim();
            info.tipoDocumentoComprador = String.valueOf(cbTipoDocumentoComprador.getSelectedItem());
            info.documentoComprador = txtDocumentoComprador.getText().trim();
            info.enderecoComprador = txtEnderecoComprador.getText().trim();
            info.numeroComprador = txtNumeroComprador.getText().trim();
            info.bairroComprador = txtBairroComprador.getText().trim();
            info.cidadeComprador = txtCidadeComprador.getText().trim();
            info.observacao = txtObservacao.getText().trim();
            resultado[0] = info;
            dialog.dispose();
        });

        botoes.add(btnCancelar);
        botoes.add(btnConfirmar);

        JPanel formulario = new JPanel();
        formulario.setBackground(Color.WHITE);
        formulario.setBorder(new EmptyBorder(0, 18, 0, 18));
        formulario.setLayout(new BoxLayout(formulario, BoxLayout.Y_AXIS));
        formulario.add(scrollItens);
        formulario.add(Box.createRigidArea(new Dimension(0, 10)));
        formulario.add(painelPrescritor);
        formulario.add(Box.createRigidArea(new Dimension(0, 10)));
        formulario.add(painelComprador);
        formulario.add(Box.createRigidArea(new Dimension(0, 10)));
        formulario.add(painelReceita);

        JScrollPane scrollFormulario = new JScrollPane(formulario);
        scrollFormulario.setBorder(null);
        scrollFormulario.getVerticalScrollBar().setUnitIncrement(16);

        dialog.getContentPane().setBackground(Color.WHITE);
        dialog.add(topo, BorderLayout.NORTH);
        dialog.add(scrollFormulario, BorderLayout.CENTER);
        dialog.add(botoes, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(btnConfirmar);
        SwingUtilities.invokeLater(txtBuscaMedico::requestFocusInWindow);
        dialog.setVisible(true);
        return resultado[0];
    }

    private void atualizarSugestoesMedico(JTextField txtBuscaMedico, JComboBox<Medico> cbMedicos) {
        String termo = txtBuscaMedico.getText().trim();
        Object selecionadoAtual = cbMedicos.getSelectedItem();
        cbMedicos.removeAllItems();
        if (termo.isBlank()) {
            return;
        }
        for (Medico medico : medicoDAO.buscarInteligente(termo)) {
            cbMedicos.addItem(medico);
        }
        if (selecionadoAtual instanceof Medico medicoAtual) {
            for (int i = 0; i < cbMedicos.getItemCount(); i++) {
                Medico item = cbMedicos.getItemAt(i);
                if (item.getId() != null && item.getId().equals(medicoAtual.getId())) {
                    cbMedicos.setSelectedIndex(i);
                    break;
                }
            }
        } else if (cbMedicos.getItemCount() > 0) {
            cbMedicos.setSelectedIndex(0);
        }
    }

    private void atualizarSugestoesMedicoPorRegistro(JTextField txtBuscaMedico, JComboBox<Medico> cbMedicos,
                                                     JComboBox<String> cbTipoRegistro, JTextField txtNumeroRegistro,
                                                     JTextField txtUfRegistro) {
        String numero = txtNumeroRegistro.getText().trim();
        String uf = txtUfRegistro.getText().trim();
        String tipo = String.valueOf(cbTipoRegistro.getSelectedItem());
        if (numero.isBlank()) {
            return;
        }

        StringBuilder termo = new StringBuilder();
        if (tipo != null && !tipo.isBlank()) {
            termo.append(tipo).append(' ');
        }
        termo.append(numero);
        if (!uf.isBlank()) {
            termo.append(' ').append(uf);
        }

        atualizarSugestoesMedicoComTermo(txtBuscaMedico, cbMedicos, termo.toString());
    }

    private void atualizarSugestoesMedicoComTermo(JTextField txtBuscaMedico, JComboBox<Medico> cbMedicos, String termo) {
        Object selecionadoAtual = cbMedicos.getSelectedItem();
        cbMedicos.removeAllItems();
        if (termo == null || termo.isBlank()) {
            return;
        }
        for (Medico medico : medicoDAO.buscarInteligente(termo)) {
            cbMedicos.addItem(medico);
        }
        if (selecionadoAtual instanceof Medico medicoAtual) {
            for (int i = 0; i < cbMedicos.getItemCount(); i++) {
                Medico item = cbMedicos.getItemAt(i);
                if (item.getId() != null && item.getId().equals(medicoAtual.getId())) {
                    cbMedicos.setSelectedIndex(i);
                    return;
                }
            }
        }
        if (cbMedicos.getItemCount() == 1) {
            cbMedicos.setSelectedIndex(0);
        } else if (cbMedicos.getItemCount() > 0) {
            cbMedicos.setSelectedIndex(0);
        }
        if (txtBuscaMedico.getText().trim().isBlank() && cbMedicos.getItemCount() == 1) {
            Medico unico = cbMedicos.getItemAt(0);
            txtBuscaMedico.setText(unico.getNome());
        }
    }

    private Medico abrirCadastroMedicoRapido(Component parent) {
        JDialog dialog = new JDialog(this, "Cadastrar medico", true);
        dialog.setSize(430, 300);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout(0, 12));

        JPanel formulario = new JPanel(new GridLayout(4, 2, 10, 10));
        formulario.setBorder(new EmptyBorder(18, 18, 18, 18));

        JTextField txtNome = new JTextField();
        JComboBox<String> cbTipoRegistro = new JComboBox<>(new String[]{"CRM", "CRO", "RMS", "RQE", "CRBM"});
        JTextField txtNumeroRegistro = new JTextField();
        JTextField txtUf = new JTextField();

        formulario.add(new JLabel("Nome"));
        formulario.add(txtNome);
        formulario.add(new JLabel("Tipo de registro"));
        formulario.add(cbTipoRegistro);
        formulario.add(new JLabel("Numero do registro"));
        formulario.add(txtNumeroRegistro);
        formulario.add(new JLabel("UF"));
        formulario.add(txtUf);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnSalvar = new JButton("Salvar medico");
        final Medico[] resultado = {null};

        btnCancelar.addActionListener(e -> dialog.dispose());
        btnSalvar.addActionListener(e -> {
            String nome = txtNome.getText().trim();
            String tipo = String.valueOf(cbTipoRegistro.getSelectedItem());
            String numero = txtNumeroRegistro.getText().trim();
            String uf = txtUf.getText().trim().toUpperCase(Locale.ROOT);

            if (nome.isBlank() || numero.isBlank()) {
                JOptionPane.showMessageDialog(dialog, "Preencha nome e numero do registro.");
                return;
            }
            if (medicoDAO.existeRegistro(tipo, numero, uf)) {
                JOptionPane.showMessageDialog(dialog, "Esse registro ja esta cadastrado.");
                return;
            }

            Medico medico = medicoDAO.cadastrar(nome, tipo, numero, uf);
            if (medico == null) {
                JOptionPane.showMessageDialog(dialog, "Nao foi possivel cadastrar o medico.");
                return;
            }
            resultado[0] = medico;
            dialog.dispose();
        });

        botoes.add(btnCancelar);
        botoes.add(btnSalvar);
        dialog.add(formulario, BorderLayout.CENTER);
        dialog.add(botoes, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(btnSalvar);
        dialog.setVisible(true);
        return resultado[0];
    }

    private void mostrarAvisoSistema(Component parent, String titulo, String mensagem) {
        JDialog dialog = new JDialog(this, titulo, true);
        dialog.setSize(540, 200);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(new Color(54, 104, 33));
        cabecalho.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        cabecalho.add(lblTitulo, BorderLayout.WEST);

        JPanel corpo = new JPanel(new BorderLayout(16, 0));
        corpo.setBackground(Color.WHITE);
        corpo.setBorder(new EmptyBorder(24, 20, 20, 20));

        JLabel lblIcone = new JLabel(UIManager.getIcon("OptionPane.informationIcon"));
        corpo.add(lblIcone, BorderLayout.WEST);

        JTextArea txtMensagem = new JTextArea(mensagem);
        txtMensagem.setEditable(false);
        txtMensagem.setLineWrap(true);
        txtMensagem.setWrapStyleWord(true);
        txtMensagem.setOpaque(false);
        txtMensagem.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtMensagem.setForeground(new Color(15, 23, 42));
        corpo.add(txtMensagem, BorderLayout.CENTER);

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        rodape.setBackground(Color.WHITE);
        rodape.setBorder(new EmptyBorder(0, 0, 18, 0));

        JButton btnOk = new JButton("OK");
        btnOk.setPreferredSize(new Dimension(110, 32));
        btnOk.addActionListener(e -> dialog.dispose());
        rodape.add(btnOk);

        dialog.add(cabecalho, BorderLayout.NORTH);
        dialog.add(corpo, BorderLayout.CENTER);
        dialog.add(rodape, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(btnOk);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    private JPanel criarSecaoReceita(String titulo) {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(titulo),
                new EmptyBorder(10, 10, 10, 10)
        ));
        return painel;
    }

    private void adicionarCampoSecao(JPanel painel, String rotulo, Component campo, int linha, int coluna, int largura) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = coluna;
        gbc.gridy = linha * 2;
        gbc.gridwidth = largura;
        if (rotulo != null && !rotulo.isBlank()) {
            JLabel label = new JLabel(rotulo);
            label.setForeground(new Color(71, 85, 105));
            painel.add(label, gbc);
        }

        GridBagConstraints gbcCampo = new GridBagConstraints();
        gbcCampo.insets = new Insets(0, 6, 8, 6);
        gbcCampo.fill = GridBagConstraints.HORIZONTAL;
        gbcCampo.weightx = 1;
        gbcCampo.gridx = coluna;
        gbcCampo.gridy = (linha * 2) + 1;
        gbcCampo.gridwidth = largura;
        painel.add(campo, gbcCampo);
    }

    private int totalItensControlados() {
        return listarItensControlados().stream().mapToInt(item -> item.quantidade).sum();
    }

    private String inferirTipoReceitaPorMedicamento() {
        boolean possuiAntibiotico = carrinho.stream()
                .anyMatch(item -> "Antibiotico".equalsIgnoreCase(item.produto.getTipoControle()));
        if (possuiAntibiotico) {
            return "Antimicrobiano";
        }

        for (ItemCarrinho item : listarItensControlados()) {
            String nome = normalizar(item.produto.getNome());
            if (nome.contains("morf") || nome.contains("codein") || nome.contains("metilfenid") || nome.contains("fentan")) {
                return "Amarela A";
            }
            if (nome.contains("alprazolam") || nome.contains("clonazepam") || nome.contains("diazepam")
                    || nome.contains("bromazepam") || nome.contains("zolpidem")) {
                return "Azul B";
            }
        }

        if (possuiItensControlados()) {
            return "Controle especial";
        }
        return "Branca comum";
    }

    private static class ReceitaControladaInfo {
        private String tipoReceita;
        private String usoMedicamento;
        private String dataReceita;
        private String numeroReceita;
        private String quantidadeReceitada;
        private String nomeMedico;
        private String tipoRegistro;
        private String numeroRegistro;
        private String ufRegistro;
        private String enderecoMedico;
        private String numeroEnderecoMedico;
        private String bairroMedico;
        private String cidadeMedico;
        private String nomeComprador;
        private String telefoneComprador;
        private String tipoDocumentoComprador;
        private String documentoComprador;
        private String enderecoComprador;
        private String numeroComprador;
        private String bairroComprador;
        private String cidadeComprador;
        private String observacao;
    }
}
