package com.farmacia.model;

public class Medico {
    private Integer id;
    private String nome;
    private String tipoRegistro;
    private String numeroRegistro;
    private String ufRegistro;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipoRegistro() {
        return tipoRegistro;
    }

    public void setTipoRegistro(String tipoRegistro) {
        this.tipoRegistro = tipoRegistro;
    }

    public String getNumeroRegistro() {
        return numeroRegistro;
    }

    public void setNumeroRegistro(String numeroRegistro) {
        this.numeroRegistro = numeroRegistro;
    }

    public String getUfRegistro() {
        return ufRegistro;
    }

    public void setUfRegistro(String ufRegistro) {
        this.ufRegistro = ufRegistro;
    }

    public String getRegistroFormatado() {
        String uf = ufRegistro == null || ufRegistro.isBlank() ? "" : "/" + ufRegistro.toUpperCase();
        return (tipoRegistro == null ? "" : tipoRegistro.toUpperCase()) + " " + (numeroRegistro == null ? "" : numeroRegistro) + uf;
    }

    @Override
    public String toString() {
        return nome + " | " + getRegistroFormatado();
    }
}
