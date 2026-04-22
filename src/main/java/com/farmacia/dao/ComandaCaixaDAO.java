package com.farmacia.dao;

import com.farmacia.model.Cliente;
import com.farmacia.model.ComandaCaixa;
import com.farmacia.model.Usuario;
import com.farmacia.util.ConnectionFactory;
import com.farmacia.util.CpfUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ComandaCaixaDAO {

    public boolean registrarComanda(Usuario usuario, Cliente cliente, boolean vendaSemCadastro,
                                    String comanda, String itensResumo, String itensDetalhe,
                                    int quantidadeItens, double total, double descontoTotal,
                                    double comissao, String tipoVenda, String status,
                                    String endereco, String bairro, String cidade,
                                    double taxaEntrega, String dataAbertura) {
        String sql = """
                INSERT INTO comandas_caixa
                (comanda, usuario_id, usuario_nome, cliente_id, cliente_nome, cliente_cpf, venda_sem_cadastro,
                 itens_resumo, itens_detalhe, quantidade_itens, total, desconto_total, comissao, tipo_venda,
                 status, data_abertura, endereco, bairro, cidade, taxa_entrega)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, comanda);
            stmt.setInt(2, usuario.getId());
            stmt.setString(3, usuario.getNome());
            if (cliente != null) {
                stmt.setInt(4, cliente.getId());
                stmt.setString(5, cliente.getNome());
                stmt.setString(6, CpfUtils.apenasDigitos(cliente.getCpf()));
            } else {
                stmt.setNull(4, Types.INTEGER);
                stmt.setString(5, "Venda sem cadastro");
                stmt.setNull(6, Types.VARCHAR);
            }
            stmt.setInt(7, vendaSemCadastro ? 1 : 0);
            stmt.setString(8, itensResumo);
            stmt.setString(9, itensDetalhe);
            stmt.setInt(10, quantidadeItens);
            stmt.setDouble(11, total);
            stmt.setDouble(12, descontoTotal);
            stmt.setDouble(13, comissao);
            stmt.setString(14, tipoVenda);
            stmt.setString(15, status);
            stmt.setString(16, dataAbertura);
            stmt.setString(17, endereco);
            stmt.setString(18, bairro);
            stmt.setString(19, cidade);
            stmt.setDouble(20, taxaEntrega);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao registrar comanda: " + e.getMessage());
            return false;
        }
    }

    public List<ComandaCaixa> listarPendentesCaixa() {
        List<ComandaCaixa> comandas = new ArrayList<>();
        String sql = """
                SELECT * FROM comandas_caixa
                WHERE status <> 'Paga'
                ORDER BY datetime(data_abertura) DESC, comanda DESC
                """;
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                comandas.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar comandas pendentes: " + e.getMessage());
        }
        return comandas;
    }

    public List<ComandaCaixa> listarHistoricoPagas() {
        List<ComandaCaixa> comandas = new ArrayList<>();
        String sql = """
                SELECT * FROM comandas_caixa
                WHERE status = 'Paga'
                ORDER BY datetime(data_abertura) DESC, comanda DESC
                """;
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                comandas.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar historico de comandas pagas: " + e.getMessage());
        }
        return comandas;
    }

    public ComandaCaixa buscarPorComanda(String comanda) {
        String sql = "SELECT * FROM comandas_caixa WHERE comanda = ? AND status <> 'Paga'";
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, comanda);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar comanda: " + e.getMessage());
        }
        return null;
    }

    public boolean existeComandaAberta(String comanda) {
        String sql = "SELECT 1 FROM comandas_caixa WHERE comanda = ? AND status <> 'Paga'";
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, comanda);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean marcarComoPaga(int id) {
        String sql = "UPDATE comandas_caixa SET status = 'Paga' WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao marcar comanda como paga: " + e.getMessage());
            return false;
        }
    }

    public boolean atualizarStatusEntrega(int id, String status, boolean precisaTroco, double trocoPara) {
        String sql = """
                UPDATE comandas_caixa
                SET status = ?, precisa_troco_entrega = ?, troco_para = ?
                WHERE id = ?
                """;
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, precisaTroco ? 1 : 0);
            stmt.setDouble(3, trocoPara);
            stmt.setInt(4, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar status da entrega: " + e.getMessage());
            return false;
        }
    }

    private ComandaCaixa mapear(ResultSet rs) throws SQLException {
        ComandaCaixa comanda = new ComandaCaixa();
        comanda.setId(rs.getInt("id"));
        comanda.setComanda(rs.getString("comanda"));
        comanda.setUsuarioId(rs.getInt("usuario_id"));
        comanda.setUsuarioNome(rs.getString("usuario_nome"));
        int clienteId = rs.getInt("cliente_id");
        comanda.setClienteId(rs.wasNull() ? null : clienteId);
        comanda.setClienteNome(rs.getString("cliente_nome"));
        comanda.setClienteCpf(rs.getString("cliente_cpf"));
        comanda.setVendaSemCadastro(rs.getInt("venda_sem_cadastro") == 1);
        comanda.setItensResumo(rs.getString("itens_resumo"));
        comanda.setItensDetalhe(rs.getString("itens_detalhe"));
        comanda.setQuantidadeItens(rs.getInt("quantidade_itens"));
        comanda.setTotal(rs.getDouble("total"));
        comanda.setDescontoTotal(rs.getDouble("desconto_total"));
        comanda.setComissao(rs.getDouble("comissao"));
        comanda.setTipoVenda(rs.getString("tipo_venda"));
        comanda.setStatus(rs.getString("status"));
        comanda.setDataAbertura(rs.getString("data_abertura"));
        comanda.setEndereco(rs.getString("endereco"));
        comanda.setBairro(rs.getString("bairro"));
        comanda.setCidade(rs.getString("cidade"));
        comanda.setTaxaEntrega(rs.getDouble("taxa_entrega"));
        comanda.setPrecisaTrocoEntrega(rs.getInt("precisa_troco_entrega") == 1);
        comanda.setTrocoPara(rs.getDouble("troco_para"));
        return comanda;
    }
}
