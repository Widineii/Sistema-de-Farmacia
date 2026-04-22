package com.farmacia.dao;

import com.farmacia.model.Medico;
import com.farmacia.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MedicoDAO {

    public List<Medico> buscarInteligente(String termo) {
        List<Medico> medicos = new ArrayList<>();
        String filtro = normalizar(termo);
        if (filtro.isBlank()) {
            return medicos;
        }

        String sql = "SELECT * FROM medicos ORDER BY nome";
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Medico medico = mapear(rs);
                String alvo = normalizar(medico.getNome() + " " + medico.getTipoRegistro() + " "
                        + medico.getNumeroRegistro() + " " + medico.getUfRegistro());
                if (alvo.contains(filtro)) {
                    medicos.add(medico);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar medicos: " + e.getMessage());
        }
        return medicos;
    }

    public boolean existeRegistro(String tipoRegistro, String numeroRegistro, String ufRegistro) {
        String sql = """
                SELECT id FROM medicos
                WHERE UPPER(tipo_registro) = UPPER(?)
                  AND numero_registro = ?
                  AND UPPER(COALESCE(uf_registro, '')) = UPPER(COALESCE(?, ''))
                """;
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tipoRegistro);
            stmt.setString(2, numeroRegistro);
            stmt.setString(3, ufRegistro);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar registro medico: " + e.getMessage());
            return false;
        }
    }

    public Medico cadastrar(String nome, String tipoRegistro, String numeroRegistro, String ufRegistro) {
        String sql = """
                INSERT INTO medicos (nome, tipo_registro, numero_registro, uf_registro)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nome);
            stmt.setString(2, tipoRegistro == null ? "" : tipoRegistro.toUpperCase(Locale.ROOT));
            stmt.setString(3, numeroRegistro);
            stmt.setString(4, ufRegistro == null ? "" : ufRegistro.toUpperCase(Locale.ROOT));
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return buscarPorId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao cadastrar medico: " + e.getMessage());
        }
        return null;
    }

    public Medico buscarPorId(int id) {
        String sql = "SELECT * FROM medicos WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar medico por id: " + e.getMessage());
        }
        return null;
    }

    private Medico mapear(ResultSet rs) throws SQLException {
        Medico medico = new Medico();
        medico.setId(rs.getInt("id"));
        medico.setNome(rs.getString("nome"));
        medico.setTipoRegistro(rs.getString("tipo_registro"));
        medico.setNumeroRegistro(rs.getString("numero_registro"));
        medico.setUfRegistro(rs.getString("uf_registro"));
        return medico;
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[^a-zA-Z0-9]+", " ")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
