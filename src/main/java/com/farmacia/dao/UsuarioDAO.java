package com.farmacia.dao;

import com.farmacia.model.Usuario;
import com.farmacia.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public Usuario autenticar(String login, String senha) {
        String sql = "SELECT * FROM usuarios WHERE LOWER(login) = LOWER(?) AND senha = ?";

        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            stmt.setString(2, senha);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setNome(rs.getString("nome"));
                    u.setLogin(rs.getString("login"));
                    u.setCargo(rs.getString("cargo"));
                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao autenticar: " + e.getMessage());
        }
        return null;
    }

    public boolean cadastrar(String nome, String login, String senha, String cargo) {
        String sql = "INSERT INTO usuarios (nome, login, senha, cargo) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, login);
            stmt.setString(3, senha);
            stmt.setString(4, cargo);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean existeLogin(String login) {
        String sql = "SELECT 1 FROM usuarios WHERE LOWER(login) = LOWER(?)";
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, nome, login, cargo FROM usuarios";
        try (Connection conn = ConnectionFactory.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setNome(rs.getString("nome"));
                u.setLogin(rs.getString("login"));
                u.setCargo(rs.getString("cargo"));
                lista.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean excluirPorId(int usuarioId) {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir usuario: " + e.getMessage());
            return false;
        }
    }
}
