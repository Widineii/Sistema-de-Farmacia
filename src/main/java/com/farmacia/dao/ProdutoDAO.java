package com.farmacia.dao;

import com.farmacia.model.Produto;
import com.farmacia.util.ConnectionFactory;

import java.text.Normalizer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ProdutoDAO {

    public List<Produto> listarTudo() {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT * FROM produtos ORDER BY nome";

        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                produtos.add(mapearProduto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar produtos: " + e.getMessage());
        }
        return produtos;
    }

    public Produto buscarPorId(int id) {
        String sql = "SELECT * FROM produtos WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearProduto(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar produto: " + e.getMessage());
        }
        return null;
    }

    public Produto buscarPorCodigoBarras(String codigoBarras) {
        String codigoNormalizado = codigoBarras == null ? "" : codigoBarras.replaceAll("\\D", "");
        if (codigoNormalizado.isBlank()) {
            return null;
        }

        String sql = "SELECT * FROM produtos WHERE codigo_barras = ?";
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigoNormalizado);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearProduto(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar produto por codigo de barras: " + e.getMessage());
        }
        return null;
    }

    public boolean atualizarQuantidade(String nomeProduto, int novaQtd) {
        String sql = "UPDATE produtos SET quantidade_estoque = ? WHERE nome = ?";

        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, novaQtd);
            stmt.setString(2, nomeProduto);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar estoque: " + e.getMessage());
            return false;
        }
    }

    public boolean atualizarQuantidadePorId(int produtoId, int novaQtd) {
        String sql = "UPDATE produtos SET quantidade_estoque = ? WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, novaQtd);
            stmt.setInt(2, produtoId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar estoque por id: " + e.getMessage());
            return false;
        }
    }

    public boolean baixarEstoque(int produtoId, int quantidade) {
        String sql = """
                UPDATE produtos
                SET quantidade_estoque = quantidade_estoque - ?
                WHERE id = ? AND quantidade_estoque >= ?
                """;
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantidade);
            stmt.setInt(2, produtoId);
            stmt.setInt(3, quantidade);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao baixar estoque: " + e.getMessage());
            return false;
        }
    }

    public boolean adicionarQuantidadePorId(int produtoId, int quantidade) {
        String sql = """
                UPDATE produtos
                SET quantidade_estoque = quantidade_estoque + ?
                WHERE id = ?
                """;
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantidade);
            stmt.setInt(2, produtoId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao adicionar estoque: " + e.getMessage());
            return false;
        }
    }

    public Produto buscarMelhorCorrespondenciaPorNome(String descricaoNota) {
        if (descricaoNota == null || descricaoNota.isBlank()) {
            return null;
        }

        String descricaoNormalizada = normalizar(descricaoNota);
        for (Produto produto : listarTudo()) {
            String nomeProduto = normalizar(produto.getNome());
            if (descricaoNormalizada.equals(nomeProduto)
                    || descricaoNormalizada.contains(nomeProduto)
                    || nomeProduto.contains(descricaoNormalizada)) {
                return produto;
            }
        }

        Produto melhorProduto = null;
        int melhorPontuacao = 0;

        for (Produto produto : listarTudo()) {
            int pontuacao = calcularPontuacao(descricaoNormalizada, produto);
            if (pontuacao > melhorPontuacao) {
                melhorPontuacao = pontuacao;
                melhorProduto = produto;
            }
        }

        return melhorPontuacao >= 45 ? melhorProduto : null;
    }

    private Produto mapearProduto(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.setId(rs.getInt("id"));
        p.setNome(rs.getString("nome"));
        p.setCodigo_barras(rs.getString("codigo_barras"));
        p.setQuantidade_estoque(rs.getInt("quantidade_estoque"));
        p.setData_validade(rs.getString("data_validade"));
        p.setPreco(rs.getDouble("preco"));
        p.setPrecoCusto(rs.getDouble("preco_custo"));
        p.setCategoria(rs.getString("categoria"));
        p.setLaboratorio(rs.getString("laboratorio"));
        p.setTipoControle(rs.getString("tipo_controle"));
        p.setEstoqueMinimo(rs.getInt("estoque_minimo"));
        p.setClasseComercial(rs.getString("classe_comercial"));
        return p;
    }

    public boolean atualizarDadosRecebimento(int produtoId, String laboratorio, String validade, double precoCusto) {
        String sql = """
                UPDATE produtos
                SET laboratorio = COALESCE(NULLIF(?, ''), laboratorio),
                    data_validade = COALESCE(NULLIF(?, ''), data_validade),
                    preco_custo = CASE WHEN ? > 0 THEN ? ELSE preco_custo END
                WHERE id = ?
                """;
        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, laboratorio);
            stmt.setString(2, validade);
            stmt.setDouble(3, precoCusto);
            stmt.setDouble(4, precoCusto);
            stmt.setInt(5, produtoId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar dados do recebimento: " + e.getMessage());
            return false;
        }
    }

    public Produto cadastrarProdutoPorXml(String nome, String codigoBarras, int quantidadeInicial,
                                          String validade, double precoCusto, String laboratorio) {
        String sql = """
                INSERT INTO produtos
                (nome, codigo_barras, quantidade_estoque, data_validade, preco, preco_custo,
                 categoria, laboratorio, tipo_controle, estoque_minimo, classe_comercial)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String categoria = inferirCategoria(nome);
        String tipoControle = inferirTipoControle(nome);
        String classeComercial = inferirClasseComercial(nome);
        int estoqueMinimo = inferirEstoqueMinimo(nome);
        double precoVenda = ajustarPrecoPorClasse(Math.max(precoCusto, 0.01), classeComercial);

        try (Connection conn = ConnectionFactory.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nome);
            stmt.setString(2, codigoBarras == null ? "" : codigoBarras.replaceAll("\\D", ""));
            stmt.setInt(3, Math.max(0, quantidadeInicial));
            stmt.setString(4, validade);
            stmt.setDouble(5, precoVenda);
            stmt.setDouble(6, Math.max(precoCusto, 0.01));
            stmt.setString(7, categoria);
            stmt.setString(8, laboratorio == null ? "" : laboratorio);
            stmt.setString(9, tipoControle);
            stmt.setInt(10, estoqueMinimo);
            stmt.setString(11, classeComercial);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return buscarPorId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao cadastrar produto pelo XML: " + e.getMessage());
        }
        return null;
    }

    private int calcularPontuacao(String descricaoNormalizada, Produto produto) {
        String nomeProduto = normalizar(produto.getNome());

        if (descricaoNormalizada.equals(nomeProduto)) {
            return 1000;
        }
        if (nomeProduto.contains(descricaoNormalizada) || descricaoNormalizada.contains(nomeProduto)) {
            return 700;
        }

        Set<String> tokensDescricao = tokenizar(descricaoNormalizada);
        Set<String> tokensProduto = tokenizar(nomeProduto);
        int comuns = 0;

        for (String token : tokensDescricao) {
            if (tokensProduto.contains(token)) {
                comuns++;
            }
        }

        int pontuacao = comuns * 14;

        if (!tokensDescricao.isEmpty() && !tokensProduto.isEmpty()) {
            String primeiroDescricao = tokensDescricao.iterator().next();
            if (tokensProduto.contains(primeiroDescricao)) {
                pontuacao += 10;
            }
        }

        String codigo = normalizar(produto.getCodigo_barras());
        if (!codigo.isBlank() && descricaoNormalizada.contains(codigo)) {
            pontuacao += 30;
        }

        return pontuacao;
    }

    private Set<String> tokenizar(String valor) {
        Set<String> tokens = new LinkedHashSet<>();
        for (String token : valor.split("\\s+")) {
            if (token.length() < 3) {
                continue;
            }
            if (Set.of("com", "sem", "oral", "uso", "para", "dos", "das", "und", "ml").contains(token)) {
                continue;
            }
            tokens.add(token);
        }
        return tokens;
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        String base = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
        return base.replaceAll("[^a-z0-9]+", " ").trim();
    }

    private String inferirCategoria(String nome) {
        String item = normalizar(nome);
        if (item.contains("amoxicilina") || item.contains("azitromicina") || item.contains("cefalexina")
                || item.contains("ciprofloxacino") || item.contains("claritromicina")
                || item.contains("levofloxacino") || item.contains("metronidazol")
                || item.contains("norfloxacino") || item.contains("sulfametoxazol")) {
            return "Antibioticos";
        }
        if (item.contains("loratadina") || item.contains("cetirizina") || item.contains("dexclorfeniramina")
                || item.contains("fexofenadina") || item.contains("hidroxizina") || item.contains("montelucaste")) {
            return "Antialergicos";
        }
        if (item.contains("dipirona") || item.contains("paracetamol") || item.contains("ibuprofeno")
                || item.contains("nimesulida") || item.contains("naproxeno") || item.contains("diclofenaco")
                || item.contains("meloxicam") || item.contains("tramadol")) {
            return "Analgesicos e Anti-inflamatorios";
        }
        if (item.contains("losartana") || item.contains("atenolol") || item.contains("enalapril")
                || item.contains("carvedilol") || item.contains("captopril") || item.contains("furosemida")
                || item.contains("hidroclorotiazida") || item.contains("anlodipino")
                || item.contains("metoprolol") || item.contains("nifedipino")) {
            return "Cardiovascular";
        }
        if (item.contains("omeprazol") || item.contains("esomeprazol") || item.contains("pantoprazol")
                || item.contains("antiacido") || item.contains("lactulona") || item.contains("domperidona")
                || item.contains("metoclopramida") || item.contains("ondansetrona") || item.contains("loperamida")) {
            return "Gastrointestinal";
        }
        if (item.contains("metformina") || item.contains("glibenclamida") || item.contains("gliclazida")
                || item.contains("levotiroxina") || item.contains("tiamazol")) {
            return "Endocrino e Diabetes";
        }
        if (item.contains("sertralina") || item.contains("escitalopram") || item.contains("fluoxetina")
                || item.contains("venlafaxina") || item.contains("amitriptilina") || item.contains("quetiapina")
                || item.contains("risperidona") || item.contains("alprazolam") || item.contains("clonazepam")
                || item.contains("diazepam") || item.contains("bromazepam")) {
            return "Saude Mental";
        }
        if (item.contains("vitamina") || item.contains("complexo b") || item.contains("acido folico")
                || item.contains("carbonato de calcio")) {
            return "Vitaminas e Suplementos";
        }
        if (item.contains("creme") || item.contains("shampoo") || item.contains("pomada")
                || item.contains("locao") || item.contains("spray nasal") || item.contains("sabonete")
                || item.contains("condicionador") || item.contains("desodorante") || item.contains("hidratante")
                || item.contains("serum") || item.contains("repelente") || item.contains("fralda")
                || item.contains("absorvente") || item.contains("escova dental") || item.contains("fio dental")
                || item.contains("creme dental") || item.contains("antisseptico bucal") || item.contains("lenco")
                || item.contains("algodao") || item.contains("curativo") || item.contains("esparadrapo")
                || item.contains("atadura") || item.contains("gaze") || item.contains("barbear")
                || item.contains("vaselina") || item.contains("acetona") || item.contains("esmalte")
                || item.contains("talco") || item.contains("depilatorio") || item.contains("touca")
                || item.contains("papel higienico") || item.contains("cinta termica")
                || item.contains("bolsa termica") || item.contains("termometro")) {
            return "Dermocosmeticos";
        }
        if (item.contains("soro fisiologico") || item.contains("salbutamol") || item.contains("budesonida")) {
            return "Respiratorio";
        }
        return "Clinica Geral";
    }

    private String inferirTipoControle(String nome) {
        String item = normalizar(nome);
        if (item.contains("alprazolam") || item.contains("clonazepam") || item.contains("diazepam")
                || item.contains("bromazepam") || item.contains("tramadol") || item.contains("pregabalina")
                || item.contains("carbamazepina") || item.contains("fenobarbital") || item.contains("fenitoina")
                || item.contains("quetiapina") || item.contains("risperidona") || item.contains("acido valproico")
                || item.contains("levodopa") || item.contains("memantina")) {
            return "Controlado";
        }
        if (item.contains("amoxicilina") || item.contains("azitromicina") || item.contains("cefalexina")
                || item.contains("ciprofloxacino") || item.contains("claritromicina")
                || item.contains("metronidazol") || item.contains("levofloxacino")
                || item.contains("norfloxacino") || item.contains("sulfametoxazol")) {
            return "Antibiotico";
        }
        return "Livre";
    }

    private int inferirEstoqueMinimo(String nome) {
        String item = normalizar(nome);
        if (item.contains("amoxicilina") || item.contains("azitromicina") || item.contains("cefalexina")
                || item.contains("losartana") || item.contains("metformina") || item.contains("dipirona")
                || item.contains("paracetamol") || item.contains("ibuprofeno")) {
            return 20;
        }
        return 12;
    }

    private String inferirClasseComercial(String nome) {
        String item = normalizar(nome);
        if (item.contains("creme") || item.contains("shampoo") || item.contains("pomada") || item.contains("locao")
                || item.contains("spray nasal") || item.contains("sabonete") || item.contains("condicionador")
                || item.contains("desodorante") || item.contains("hidratante") || item.contains("serum")
                || item.contains("repelente") || item.contains("fralda") || item.contains("absorvente")
                || item.contains("escova dental") || item.contains("fio dental") || item.contains("creme dental")
                || item.contains("antisseptico bucal") || item.contains("algodao") || item.contains("curativo")
                || item.contains("esparadrapo") || item.contains("atadura") || item.contains("gaze")
                || item.contains("barbear") || item.contains("vaselina") || item.contains("acetona")
                || item.contains("esmalte") || item.contains("talco") || item.contains("depilatorio")
                || item.contains("touca") || item.contains("papel higienico") || item.contains("cinta termica")
                || item.contains("bolsa termica") || item.contains("termometro")) {
            return "Perfumaria";
        }
        if (item.contains("amoxicilina") || item.contains("azitromicina") || item.contains("cefalexina")
                || item.contains("ciprofloxacino") || item.contains("claritromicina") || item.contains("norfloxacino")
                || item.contains("metronidazol") || item.contains("dipirona") || item.contains("paracetamol")
                || item.contains("ibuprofeno") || item.contains("losartana") || item.contains("metformina")
                || item.contains("atorvastatina") || item.contains("valsartana") || item.contains("ramipril")
                || item.contains("sitagliptina") || item.contains("vildagliptina") || item.contains("empagliflozina")
                || item.contains("dapagliflozina") || item.contains("nitazoxanida") || item.contains("mesalazina")
                || item.contains("sumatriptana") || item.contains("acetilcisteina") || item.contains("ambroxol")
                || item.contains("carbocisteina") || item.contains("cetoprofeno") || item.contains("ezetimiba")
                || item.contains("finasterida") || item.contains("nebivolol") || item.contains("olmesartana")
                || item.contains("glimepirida") || item.contains("levocetirizina") || item.contains("guaifenesina")) {
            return "Generico";
        }
        return "Similar";
    }

    private double ajustarPrecoPorClasse(double precoBase, String classeComercial) {
        if ("Original".equalsIgnoreCase(classeComercial)) {
            return precoBase * 1.24;
        }
        if ("Perfumaria".equalsIgnoreCase(classeComercial)) {
            return precoBase * 1.18;
        }
        if ("Similar".equalsIgnoreCase(classeComercial)) {
            return precoBase * 1.08;
        }
        return precoBase;
    }
}
