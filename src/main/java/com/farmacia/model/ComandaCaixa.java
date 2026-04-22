package com.farmacia.model;

public class ComandaCaixa {
    private int id;
    private String comanda;
    private int usuarioId;
    private String usuarioNome;
    private Integer clienteId;
    private String clienteNome;
    private String clienteCpf;
    private boolean vendaSemCadastro;
    private String itensResumo;
    private String itensDetalhe;
    private int quantidadeItens;
    private double total;
    private double descontoTotal;
    private double comissao;
    private String tipoVenda;
    private String status;
    private String dataAbertura;
    private String endereco;
    private String bairro;
    private String cidade;
    private double taxaEntrega;
    private boolean precisaTrocoEntrega;
    private double trocoPara;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getComanda() {
        return comanda;
    }

    public void setComanda(String comanda) {
        this.comanda = comanda;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNome() {
        return usuarioNome;
    }

    public void setUsuarioNome(String usuarioNome) {
        this.usuarioNome = usuarioNome;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }

    public String getClienteCpf() {
        return clienteCpf;
    }

    public void setClienteCpf(String clienteCpf) {
        this.clienteCpf = clienteCpf;
    }

    public boolean isVendaSemCadastro() {
        return vendaSemCadastro;
    }

    public void setVendaSemCadastro(boolean vendaSemCadastro) {
        this.vendaSemCadastro = vendaSemCadastro;
    }

    public String getItensResumo() {
        return itensResumo;
    }

    public void setItensResumo(String itensResumo) {
        this.itensResumo = itensResumo;
    }

    public String getItensDetalhe() {
        return itensDetalhe;
    }

    public void setItensDetalhe(String itensDetalhe) {
        this.itensDetalhe = itensDetalhe;
    }

    public int getQuantidadeItens() {
        return quantidadeItens;
    }

    public void setQuantidadeItens(int quantidadeItens) {
        this.quantidadeItens = quantidadeItens;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getDescontoTotal() {
        return descontoTotal;
    }

    public void setDescontoTotal(double descontoTotal) {
        this.descontoTotal = descontoTotal;
    }

    public double getComissao() {
        return comissao;
    }

    public void setComissao(double comissao) {
        this.comissao = comissao;
    }

    public String getTipoVenda() {
        return tipoVenda;
    }

    public void setTipoVenda(String tipoVenda) {
        this.tipoVenda = tipoVenda;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(String dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public double getTaxaEntrega() {
        return taxaEntrega;
    }

    public void setTaxaEntrega(double taxaEntrega) {
        this.taxaEntrega = taxaEntrega;
    }

    public boolean isPrecisaTrocoEntrega() {
        return precisaTrocoEntrega;
    }

    public void setPrecisaTrocoEntrega(boolean precisaTrocoEntrega) {
        this.precisaTrocoEntrega = precisaTrocoEntrega;
    }

    public double getTrocoPara() {
        return trocoPara;
    }

    public void setTrocoPara(double trocoPara) {
        this.trocoPara = trocoPara;
    }
}
