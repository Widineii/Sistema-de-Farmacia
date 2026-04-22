package com.farmacia.dao;

import com.farmacia.model.Fornecedor;
import com.farmacia.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RecebimentoEstoqueDAO {

    public Integer registrarRecebimento(Fornecedor fornecedor, String chaveNfe, String numeroNota,
                                        String xmlOrigem, int itens, double total) {
        String sql = """
                INSERT INTO recebimentos_estoque
                (fornecedor_id, fornecedor_nome, fornecedor_cnpj, chave_nfe, numero_nota, xml_origem, quantidade_itens, valor_total)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setObject(1, fornecedor != null ? fornecedor.getId() : null);
            stmt.setString(2, fornecedor != null ? fornecedor.getNome() : null);
            stmt.setString(3, fornecedor != null ? fornecedor.getCnpj() : null);
            stmt.setString(4, chaveNfe);
            stmt.setString(5, numeroNota);
            stmt.setString(6, xmlOrigem);
            stmt.setInt(7, itens);
            stmt.setDouble(8, total);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao registrar recebimento: " + e.getMessage());
            return null;
        }
    }

    public boolean registrarItemRecebido(int recebimentoId, int produtoId, String produtoNota,
                                         String codigoNota, int quantidade, double valorUnitario,
                                         String lote, String dataValidade, String laboratorioNota,
                                         double custoAnterior, double custoNovo, boolean houveAlteracaoCusto) {
        String sql = """
                INSERT INTO recebimentos_estoque_itens
                (recebimento_id, produto_id, produto_nota, codigo_nota, quantidade, valor_unitario,
                 lote, data_validade, laboratorio_nota, custo_anterior, custo_novo, houve_alteracao_custo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, recebimentoId);
            stmt.setInt(2, produtoId);
            stmt.setString(3, produtoNota);
            stmt.setString(4, codigoNota);
            stmt.setInt(5, quantidade);
            stmt.setDouble(6, valorUnitario);
            stmt.setString(7, lote);
            stmt.setString(8, dataValidade);
            stmt.setString(9, laboratorioNota);
            stmt.setDouble(10, custoAnterior);
            stmt.setDouble(11, custoNovo);
            stmt.setInt(12, houveAlteracaoCusto ? 1 : 0);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao registrar item recebido: " + e.getMessage());
            return false;
        }
    }
}
