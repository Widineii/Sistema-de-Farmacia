package com.farmacia.dao;

import com.farmacia.model.Cliente;
import com.farmacia.model.ComandaCaixa;
import com.farmacia.model.Usuario;
import com.farmacia.model.Venda;
import com.farmacia.util.ConnectionFactory;
import com.farmacia.util.CpfUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VendaDAO {
    public static final double TAXA_COMISSAO = 0.05;

    public boolean registrarVenda(Usuario usuario, Cliente cliente, boolean vendaSemCadastro,
                                  String itensResumo, int quantidadeItens, double total, double descontoTotal,
                                  double comissaoTotal, String data, String comanda,
                                  String formaPagamento, double valorRecebido, double troco) {
        return registrarVendaInterna(
                usuario.getId(),
                usuario.getNome(),
                cliente != null ? cliente.getId() : null,
                cliente != null ? cliente.getNome() : "Venda sem cadastro",
                cliente != null ? CpfUtils.apenasDigitos(cliente.getCpf()) : null,
                vendaSemCadastro,
                itensResumo,
                quantidadeItens,
                total,
                descontoTotal,
                comissaoTotal,
                data,
                "Balcao",
                comanda,
                formaPagamento,
                valorRecebido,
                troco
        );
    }

    public boolean registrarVendaDeComanda(ComandaCaixa comanda, String data,
                                           String formaPagamento, double valorRecebido, double troco) {
        return registrarVendaInterna(
                comanda.getUsuarioId(),
                comanda.getUsuarioNome(),
                comanda.getClienteId(),
                comanda.getClienteNome(),
                comanda.getClienteCpf(),
                comanda.isVendaSemCadastro(),
                comanda.getItensResumo(),
                comanda.getQuantidadeItens(),
                comanda.getTotal(),
                comanda.getDescontoTotal(),
                comanda.getComissao(),
                data,
                comanda.getTipoVenda(),
                comanda.getComanda(),
                formaPagamento,
                valorRecebido,
                troco
        );
    }

    private boolean registrarVendaInterna(int usuarioId, String usuarioNome, Integer clienteId, String clienteNome,
                                          String clienteCpf, boolean vendaSemCadastro, String itensResumo,
                                          int quantidadeItens, double total, double descontoTotal, double comissaoTotal,
                                          String data, String tipoVenda, String comanda, String formaPagamento,
                                          double valorRecebido, double troco) {
        String sql = """
                INSERT INTO vendas (usuario_id, usuario_nome, cliente_id, cliente_nome, cliente_cpf, venda_sem_cadastro,
                                    itens_resumo, quantidade_itens, total, desconto_total, comissao, data_venda, tipo_venda,
                                    comanda, forma_pagamento, valor_recebido, troco)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setString(2, usuarioNome);
            if (clienteId != null) {
                stmt.setInt(3, clienteId);
                stmt.setString(4, clienteNome);
                stmt.setString(5, clienteCpf == null ? null : CpfUtils.apenasDigitos(clienteCpf));
            } else {
                stmt.setNull(3, Types.INTEGER);
                stmt.setString(4, clienteNome == null ? "Venda sem cadastro" : clienteNome);
                stmt.setNull(5, Types.VARCHAR);
            }
            stmt.setInt(6, vendaSemCadastro ? 1 : 0);
            stmt.setString(7, itensResumo);
            stmt.setInt(8, quantidadeItens);
            stmt.setDouble(9, total);
            stmt.setDouble(10, descontoTotal);
            stmt.setDouble(11, comissaoTotal);
            stmt.setString(12, data);
            stmt.setString(13, tipoVenda);
            stmt.setString(14, comanda);
            stmt.setString(15, formaPagamento);
            stmt.setDouble(16, valorRecebido);
            stmt.setDouble(17, troco);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao registrar venda: " + e.getMessage());
            return false;
        }
    }

    public List<Venda> listarTodas() {
        return listarPorFiltro(null);
    }

    public List<Venda> listarPorUsuario(int usuarioId) {
        return listarPorFiltro(usuarioId);
    }

    public List<Venda> listarFiltradas(LocalDate data, Integer usuarioId, String tipoVenda) {
        List<Venda> vendas = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM vendas WHERE substr(data_venda, 1, 10) = ?");
        List<Object> parametros = new ArrayList<>();
        parametros.add(data.toString());

        if (usuarioId != null) {
            sql.append(" AND usuario_id = ?");
            parametros.add(usuarioId);
        }
        if (tipoVenda != null && !tipoVenda.isBlank()) {
            sql.append(" AND tipo_venda = ?");
            parametros.add(tipoVenda);
        }
        sql.append(" ORDER BY datetime(data_venda) DESC, id DESC");

        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            preencherParametros(stmt, parametros);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vendas.add(mapearVenda(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar vendas filtradas: " + e.getMessage());
        }
        return vendas;
    }

    public double buscarFaturamentoTotal() {
        return buscarValor("SELECT COALESCE(SUM(total), 0) FROM vendas", null);
    }

    public double buscarFaturamentoDoDia() {
        return buscarValorPorData("SELECT COALESCE(SUM(total), 0) FROM vendas WHERE substr(data_venda, 1, 10) = ?", null, LocalDate.now());
    }

    public double buscarComissaoTotal() {
        return buscarValor("SELECT COALESCE(SUM(comissao), 0) FROM vendas", null);
    }

    public double buscarComissaoDoDia() {
        return buscarValorPorData("SELECT COALESCE(SUM(comissao), 0) FROM vendas WHERE substr(data_venda, 1, 10) = ?", null, LocalDate.now());
    }

    public double buscarDescontoTotal() {
        return buscarValor("SELECT COALESCE(SUM(desconto_total), 0) FROM vendas", null);
    }

    public double buscarFaturamentoPorUsuario(int usuarioId) {
        return buscarValor("SELECT COALESCE(SUM(total), 0) FROM vendas WHERE usuario_id = ?", usuarioId);
    }

    public double buscarFaturamentoDoDiaPorUsuario(int usuarioId) {
        return buscarValorPorData("SELECT COALESCE(SUM(total), 0) FROM vendas WHERE usuario_id = ? AND substr(data_venda, 1, 10) = ?", usuarioId, LocalDate.now());
    }

    public double buscarComissaoPorUsuario(int usuarioId) {
        return buscarValor("SELECT COALESCE(SUM(comissao), 0) FROM vendas WHERE usuario_id = ?", usuarioId);
    }

    public double buscarComissaoDoDiaPorUsuario(int usuarioId) {
        return buscarValorPorData("SELECT COALESCE(SUM(comissao), 0) FROM vendas WHERE usuario_id = ? AND substr(data_venda, 1, 10) = ?", usuarioId, LocalDate.now());
    }

    public double buscarDescontoPorUsuario(int usuarioId) {
        return buscarValor("SELECT COALESCE(SUM(desconto_total), 0) FROM vendas WHERE usuario_id = ?", usuarioId);
    }

    public int contarVendasPorUsuario(int usuarioId) {
        String sql = "SELECT COUNT(*) FROM vendas WHERE usuario_id = ?";
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao contar vendas: " + e.getMessage());
            return 0;
        }
    }

    public int contarVendasDoDiaPorUsuario(int usuarioId) {
        String sql = "SELECT COUNT(*) FROM vendas WHERE usuario_id = ? AND substr(data_venda, 1, 10) = ?";
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setString(2, LocalDate.now().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao contar vendas do dia: " + e.getMessage());
            return 0;
        }
    }

    public int contarVendasDoDia() {
        String sql = "SELECT COUNT(*) FROM vendas WHERE substr(data_venda, 1, 10) = ?";
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, LocalDate.now().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao contar vendas do dia: " + e.getMessage());
            return 0;
        }
    }

    public List<Object[]> listarResumoPorVendedor() {
        List<Object[]> resumo = new ArrayList<>();
        String sql = """
                SELECT usuario_nome,
                       COUNT(*) AS qtd_vendas,
                       COALESCE(SUM(total), 0) AS faturamento,
                       COALESCE(SUM(comissao), 0) AS comissao_total
                FROM vendas
                GROUP BY usuario_id, usuario_nome
                ORDER BY faturamento DESC
                """;

        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                resumo.add(new Object[]{
                        rs.getString("usuario_nome"),
                        rs.getInt("qtd_vendas"),
                        formatarMoeda(rs.getDouble("faturamento")),
                        formatarMoeda(rs.getDouble("comissao_total"))
                });
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar resumo por vendedor: " + e.getMessage());
        }
        return resumo;
    }

    public List<Object[]> listarResumoPorVendedorFiltrado(LocalDate data, String tipoVenda) {
        List<Object[]> resumo = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT usuario_nome,
                       COUNT(*) AS qtd_vendas,
                       COALESCE(SUM(total), 0) AS faturamento,
                       COALESCE(SUM(comissao), 0) AS comissao_total
                FROM vendas
                WHERE substr(data_venda, 1, 10) = ?
                """);
        List<Object> parametros = new ArrayList<>();
        parametros.add(data.toString());

        if (tipoVenda != null && !tipoVenda.isBlank()) {
            sql.append(" AND tipo_venda = ?");
            parametros.add(tipoVenda);
        }
        sql.append("""
                
                GROUP BY usuario_id, usuario_nome
                ORDER BY faturamento DESC
                """);

        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            preencherParametros(stmt, parametros);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resumo.add(new Object[]{
                            rs.getString("usuario_nome"),
                            rs.getInt("qtd_vendas"),
                            formatarMoeda(rs.getDouble("faturamento")),
                            formatarMoeda(rs.getDouble("comissao_total"))
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar resumo por vendedor filtrado: " + e.getMessage());
        }
        return resumo;
    }

    public double buscarFaturamentoFiltrado(LocalDate data, Integer usuarioId, String tipoVenda) {
        return buscarValorFiltrado("SELECT COALESCE(SUM(total), 0) FROM vendas WHERE substr(data_venda, 1, 10) = ?",
                data, usuarioId, tipoVenda);
    }

    public double buscarComissaoFiltrada(LocalDate data, Integer usuarioId, String tipoVenda) {
        return buscarValorFiltrado("SELECT COALESCE(SUM(comissao), 0) FROM vendas WHERE substr(data_venda, 1, 10) = ?",
                data, usuarioId, tipoVenda);
    }

    public int contarVendasFiltradas(LocalDate data, Integer usuarioId, String tipoVenda) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM vendas WHERE substr(data_venda, 1, 10) = ?");
        List<Object> parametros = new ArrayList<>();
        parametros.add(data.toString());
        if (usuarioId != null) {
            sql.append(" AND usuario_id = ?");
            parametros.add(usuarioId);
        }
        if (tipoVenda != null && !tipoVenda.isBlank()) {
            sql.append(" AND tipo_venda = ?");
            parametros.add(tipoVenda);
        }
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            preencherParametros(stmt, parametros);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao contar vendas filtradas: " + e.getMessage());
            return 0;
        }
    }

    public List<Object[]> listarResumoDiario(int limiteDias) {
        List<Object[]> resumo = new ArrayList<>();
        String sql = """
                SELECT substr(data_venda, 1, 10) AS dia,
                       COUNT(*) AS qtd_vendas,
                       COALESCE(SUM(total), 0) AS faturamento,
                       COALESCE(SUM(comissao), 0) AS comissao_total
                FROM vendas
                GROUP BY substr(data_venda, 1, 10)
                ORDER BY dia DESC
                LIMIT ?
                """;

        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limiteDias);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resumo.add(new Object[]{
                            rs.getString("dia"),
                            rs.getInt("qtd_vendas"),
                            formatarMoeda(rs.getDouble("faturamento")),
                            formatarMoeda(rs.getDouble("comissao_total"))
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar resumo diario: " + e.getMessage());
        }
        return resumo;
    }

    public static double calcularComissao(double subtotalComissionavel) {
        return subtotalComissionavel * TAXA_COMISSAO;
    }

    public static String formatarMoeda(double valor) {
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(valor);
    }

    private List<Venda> listarPorFiltro(Integer usuarioId) {
        List<Venda> vendas = new ArrayList<>();
        String sql = usuarioId == null
                ? "SELECT * FROM vendas ORDER BY datetime(data_venda) DESC, id DESC"
                : "SELECT * FROM vendas WHERE usuario_id = ? ORDER BY datetime(data_venda) DESC, id DESC";

        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (usuarioId != null) {
                stmt.setInt(1, usuarioId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vendas.add(mapearVenda(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar vendas: " + e.getMessage());
        }
        return vendas;
    }

    private double buscarValor(String sql, Integer usuarioId) {
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (usuarioId != null) {
                stmt.setInt(1, usuarioId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar indicador de vendas: " + e.getMessage());
            return 0;
        }
    }

    private double buscarValorPorData(String sql, Integer usuarioId, LocalDate data) {
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (usuarioId != null) {
                stmt.setInt(1, usuarioId);
                stmt.setString(2, data.toString());
            } else {
                stmt.setString(1, data.toString());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar indicador diario de vendas: " + e.getMessage());
            return 0;
        }
    }

    private double buscarValorFiltrado(String sqlBase, LocalDate data, Integer usuarioId, String tipoVenda) {
        StringBuilder sql = new StringBuilder(sqlBase);
        List<Object> parametros = new ArrayList<>();
        parametros.add(data.toString());
        if (usuarioId != null) {
            sql.append(" AND usuario_id = ?");
            parametros.add(usuarioId);
        }
        if (tipoVenda != null && !tipoVenda.isBlank()) {
            sql.append(" AND tipo_venda = ?");
            parametros.add(tipoVenda);
        }
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            preencherParametros(stmt, parametros);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar valor filtrado: " + e.getMessage());
            return 0;
        }
    }

    private void preencherParametros(PreparedStatement stmt, List<Object> parametros) throws SQLException {
        for (int i = 0; i < parametros.size(); i++) {
            Object valor = parametros.get(i);
            if (valor instanceof Integer inteiro) {
                stmt.setInt(i + 1, inteiro);
            } else {
                stmt.setString(i + 1, String.valueOf(valor));
            }
        }
    }

    private Venda mapearVenda(ResultSet rs) throws SQLException {
        Venda venda = new Venda();
        venda.setId(rs.getInt("id"));
        venda.setUsuarioId(rs.getInt("usuario_id"));
        venda.setUsuarioNome(rs.getString("usuario_nome"));
        int clienteId = rs.getInt("cliente_id");
        venda.setClienteId(rs.wasNull() ? null : clienteId);
        venda.setClienteNome(rs.getString("cliente_nome"));
        venda.setClienteCpf(rs.getString("cliente_cpf"));
        venda.setVendaSemCadastro(rs.getInt("venda_sem_cadastro") == 1);
        venda.setItensResumo(rs.getString("itens_resumo"));
        venda.setQuantidadeItens(rs.getInt("quantidade_itens"));
        venda.setValorTotal(rs.getDouble("total"));
        venda.setDescontoTotal(rs.getDouble("desconto_total"));
        venda.setComissao(rs.getDouble("comissao"));
        venda.setDataVenda(rs.getString("data_venda"));
        venda.setComanda(rs.getString("comanda"));
        venda.setFormaPagamento(rs.getString("forma_pagamento"));
        venda.setValorRecebido(rs.getDouble("valor_recebido"));
        venda.setTroco(rs.getDouble("troco"));
        try {
            venda.setTipoVenda(rs.getString("tipo_venda"));
        } catch (SQLException ignored) {
            venda.setTipoVenda("Balcao");
        }
        return venda;
    }
}
