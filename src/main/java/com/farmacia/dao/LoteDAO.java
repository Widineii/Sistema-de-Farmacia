package com.farmacia.dao;

import com.farmacia.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LoteDAO {

    public boolean registrarOuAtualizarLote(int produtoId, String numeroLote, int quantidade,
                                            String dataValidade, String laboratorio, double precoCusto) {
        String sql = """
                INSERT INTO lotes (produto_id, numero_lote, quantidade_atual, data_validade, laboratorio, preco_custo)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(produto_id, numero_lote) DO UPDATE SET
                    quantidade_atual = lotes.quantidade_atual + excluded.quantidade_atual,
                    data_validade = COALESCE(NULLIF(excluded.data_validade, ''), lotes.data_validade),
                    laboratorio = COALESCE(NULLIF(excluded.laboratorio, ''), lotes.laboratorio),
                    preco_custo = CASE WHEN excluded.preco_custo > 0 THEN excluded.preco_custo ELSE lotes.preco_custo END
                """;

        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, produtoId);
            stmt.setString(2, numeroLote == null || numeroLote.isBlank() ? "SEM LOTE INFORMADO" : numeroLote);
            stmt.setInt(3, quantidade);
            stmt.setString(4, dataValidade);
            stmt.setString(5, laboratorio);
            stmt.setDouble(6, precoCusto);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao registrar lote: " + e.getMessage());
            return false;
        }
    }
}
