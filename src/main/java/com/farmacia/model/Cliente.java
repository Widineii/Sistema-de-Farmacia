package com.farmacia.model;

import java.math.BigDecimal;

public class Cliente {
    private Integer id;
    private String nome;
    private String cpf;
    private String telefone;
    private BigDecimal limite_credito;

    public Cliente(){}

    public Integer getId() {return id;}
    public void setId(Integer id) {this.id = id;}

    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}

    public String getCpf() {return cpf;}
    public void setCpf(String cpf) {this.cpf = cpf;}

    public String getTelefone() {return telefone;}
    public void setTelefone(String telefone) {this.telefone = telefone;}

    public BigDecimal getLimite_credito() {return limite_credito;}
    public void setLimite_credito(BigDecimal limite_credito) {this.limite_credito = limite_credito;}

    @Override
    public String toString() {
        return nome + " | CPF: " + cpf;
    }
}
