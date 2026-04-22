package com.farmacia.dao;

import com.farmacia.model.Fornecedor;
import com.farmacia.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FornecedorDAO {

    public List<Fornecedor> buscarInteligente(String nomeOuCnpj) {
        List<Fornecedor> fornecedores = new ArrayList<>();
        String termo = nomeOuCnpj == null ? "" : nomeOuCnpj.trim();
        String cnpj = termo.replaceAll("\\D", "");

        String sql = """
                SELECT * FROM fornecedores
                WHERE nome LIKE ? OR cnpj LIKE ?
                ORDER BY nome
                LIMIT 12
                """;

        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + termo + "%");
            stmt.setString(2, "%" + cnpj + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    fornecedores.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar fornecedores: " + e.getMessage());
        }
        return fornecedores;
    }

    public Fornecedor buscarPorCnpj(String cnpj) {
        String sql = "SELECT * FROM fornecedores WHERE cnpj = ?";
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cnpj.replaceAll("\\D", ""));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar fornecedor por CNPJ: " + e.getMessage());
        }
        return null;
    }

    public Fornecedor salvarSeNaoExistir(String nome, String cnpj) {
        String cnpjNumerico = cnpj.replaceAll("\\D", "");
        Fornecedor existente = buscarPorCnpj(cnpjNumerico);
        if (existente != null) {
            return existente;
        }

        String sql = "INSERT INTO fornecedores (nome, cnpj) VALUES (?, ?)";
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nome);
            stmt.setString(2, cnpjNumerico);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                Fornecedor fornecedor = new Fornecedor();
                fornecedor.setNome(nome);
                fornecedor.setCnpj(cnpjNumerico);
                if (rs.next()) {
                    fornecedor.setId(rs.getInt(1));
                }
                return fornecedor;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao salvar fornecedor: " + e.getMessage());
            return null;
        }
    }

    private Fornecedor mapear(ResultSet rs) throws SQLException {
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setId(rs.getInt("id"));
        fornecedor.setNome(rs.getString("nome"));
        fornecedor.setCnpj(rs.getString("cnpj"));
        return fornecedor;
    }
}
