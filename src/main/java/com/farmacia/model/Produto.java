package com.farmacia.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Produto {
    private int id;
    private String nome;
    private String codigo_barras;
    private int quantidade_estoque;
    private String data_validade;
    private double preco;
    private String categoria;
    private String laboratorio;
    private String tipoControle;
    private int estoqueMinimo;
    private String classeComercial;
    private double precoCusto;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCodigo_barras() { return codigo_barras; }
    public void setCodigo_barras(String codigo_barras) { this.codigo_barras = codigo_barras; }

    public int getQuantidade_estoque() { return quantidade_estoque; }
    public void setQuantidade_estoque(int quantidade_estoque) { this.quantidade_estoque = quantidade_estoque; }

    public String getData_validade() { return data_validade; }
    public void setData_validade(String data_validade) { this.data_validade = data_validade; }

    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getLaboratorio() { return laboratorio; }
    public void setLaboratorio(String laboratorio) { this.laboratorio = laboratorio; }

    public String getTipoControle() { return tipoControle; }
    public void setTipoControle(String tipoControle) { this.tipoControle = tipoControle; }

    public int getEstoqueMinimo() { return estoqueMinimo; }
    public void setEstoqueMinimo(int estoqueMinimo) { this.estoqueMinimo = estoqueMinimo; }

    public String getClasseComercial() { return classeComercial; }
    public void setClasseComercial(String classeComercial) { this.classeComercial = classeComercial; }

    public double getPrecoCusto() { return precoCusto; }
    public void setPrecoCusto(double precoCusto) { this.precoCusto = precoCusto; }

    public boolean isEstoqueBaixo() {
        return quantidade_estoque <= estoqueMinimo;
    }

    public long diasParaVencimento() {
        try {
            LocalDate validade = LocalDate.parse(data_validade);
            return ChronoUnit.DAYS.between(LocalDate.now(), validade);
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    public boolean isProximoDoVencimento() {
        long dias = diasParaVencimento();
        return dias >= 0 && dias <= 90;
    }

    public boolean geraComissao() {
        return true;
    }

    public boolean isOriginal() {
        return "Original".equalsIgnoreCase(classeComercial);
    }

    public boolean isGenerico() {
        return "Generico".equalsIgnoreCase(classeComercial);
    }

    public boolean isSimilar() {
        return "Similar".equalsIgnoreCase(classeComercial);
    }

    public boolean isPerfumaria() {
        return "Perfumaria".equalsIgnoreCase(classeComercial);
    }

    public boolean isControlado() {
        return "Controlado".equalsIgnoreCase(tipoControle)
                || "Antibiotico".equalsIgnoreCase(tipoControle);
    }

    public double getPercentualDescontoCliente() {
        return isOriginal() ? 0.12 : 0.0;
    }

    public String getStatusOperacional() {
        if (diasParaVencimento() < 0) {
            return "Vencido";
        }
        if (isEstoqueBaixo() && isProximoDoVencimento()) {
            return "Estoque baixo e vence logo";
        }
        if (isEstoqueBaixo()) {
            return "Estoque baixo";
        }
        if (isProximoDoVencimento()) {
            return "Validade proxima";
        }
        return "OK";
    }
}
