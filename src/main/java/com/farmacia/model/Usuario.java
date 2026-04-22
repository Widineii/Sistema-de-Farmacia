package com.farmacia.model;

public class Usuario {
    private int id;
    private String nome;
    private String login;
    private String senha;
    private String cargo;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public boolean isAdministrador() {
        return cargo != null && (cargo.equalsIgnoreCase("administrador") || cargo.equalsIgnoreCase("admin"));
    }

    public boolean isChefia() {
        return cargo != null
                && (cargo.equalsIgnoreCase("chefe")
                || cargo.equalsIgnoreCase("gerente")
                || cargo.equalsIgnoreCase("administrador")
                || cargo.equalsIgnoreCase("admin"));
    }

    public boolean isAtendente() {
        return cargo != null && cargo.equalsIgnoreCase("atendente");
    }

    public String getCargoExibicao() {
        return cargo == null || cargo.isBlank() ? "Sem cargo" : cargo;
    }
}
