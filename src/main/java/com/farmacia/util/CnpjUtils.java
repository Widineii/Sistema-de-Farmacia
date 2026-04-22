package com.farmacia.util;

public final class CnpjUtils {
    private CnpjUtils() {
    }

    public static String apenasDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    public static boolean isValido(String valor) {
        return apenasDigitos(valor).length() == 14;
    }

    public static String formatar(String valor) {
        String digitos = apenasDigitos(valor);
        if (digitos.length() != 14) {
            return valor == null ? "" : valor;
        }
        return digitos.substring(0, 2) + "."
                + digitos.substring(2, 5) + "."
                + digitos.substring(5, 8) + "/"
                + digitos.substring(8, 12) + "-"
                + digitos.substring(12);
    }
}
