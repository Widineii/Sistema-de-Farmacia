package com.farmacia.util;

import com.farmacia.model.ItemNotaFiscalEntrada;
import com.farmacia.model.NotaFiscalEntrada;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class NotaFiscalXmlParser {

    public NotaFiscalEntrada ler(File arquivoXml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(arquivoXml);
        document.getDocumentElement().normalize();

        NotaFiscalEntrada nota = new NotaFiscalEntrada();
        Element emit = primeiro(document, "emit");
        Element ide = primeiro(document, "ide");
        Element total = primeiro(document, "ICMSTot");

        nota.setFornecedorNome(texto(emit, "xNome"));
        nota.setFornecedorCnpj(CnpjUtils.apenasDigitos(texto(emit, "CNPJ")));
        nota.setNumeroNota(texto(ide, "nNF"));
        nota.setValorTotal(parseDouble(texto(total, "vNF")));

        Element infNfe = primeiro(document, "infNFe");
        if (infNfe != null) {
            String id = infNfe.getAttribute("Id");
            nota.setChaveNfe(id == null ? "" : id.replace("NFe", ""));
        }

        NodeList itens = document.getElementsByTagNameNS("*", "det");
        for (int i = 0; i < itens.getLength(); i++) {
            Node node = itens.item(i);
            if (!(node instanceof Element det)) {
                continue;
            }
            Element prod = primeiro(det, "prod");
            if (prod == null) {
                continue;
            }

            ItemNotaFiscalEntrada item = new ItemNotaFiscalEntrada();
            item.setCodigo(texto(prod, "cProd"));
            item.setCodigoBarras(CnpjUtils.apenasDigitos(texto(prod, "cEANTrib")));
            item.setDescricao(texto(prod, "xProd"));
            item.setQuantidade((int) Math.round(parseDouble(texto(prod, "qCom"))));
            item.setValorUnitario(parseDouble(texto(prod, "vUnCom")));
            Element med = primeiro(det, "med");
            Element rastro = primeiro(det, "rastro");
            item.setLaboratorio(texto(med, "xFab"));
            item.setLote(texto(rastro, "nLote"));
            item.setDataValidade(formatarDataNota(texto(rastro, "dVal")));
            nota.getItens().add(item);
        }

        return nota;
    }

    private String texto(Document document, String tag) {
        Element element = primeiro(document, tag);
        return element == null ? "" : element.getTextContent().trim();
    }

    private String texto(Element parent, String tag) {
        if (parent == null) {
            return "";
        }
        Element element = primeiro(parent, tag);
        return element == null ? "" : element.getTextContent().trim();
    }

    private Element primeiro(Document document, String tag) {
        NodeList nodes = document.getElementsByTagNameNS("*", tag);
        if (nodes.getLength() == 0 || !(nodes.item(0) instanceof Element element)) {
            return null;
        }
        return element;
    }

    private Element primeiro(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagNameNS("*", tag);
        if (nodes.getLength() == 0 || !(nodes.item(0) instanceof Element element)) {
            return null;
        }
        return element;
    }

    private double parseDouble(String valor) {
        try {
            return Double.parseDouble(valor.replace(",", "."));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String formatarDataNota(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        try {
            return LocalDate.parse(valor, DateTimeFormatter.ISO_DATE).toString();
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(valor.substring(0, 10), DateTimeFormatter.ISO_DATE).toString();
            } catch (Exception ignored) {
                return valor;
            }
        }
    }
}
