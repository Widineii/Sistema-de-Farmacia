package com.farmacia.util;

public final class CpfUtils {
    private CpfUtils() {
    }

    public static String apenasDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    public static boolean isValido(String cpf) {
        String digits = apenasDigitos(cpf);
        if (digits.length() != 11 || digits.chars().distinct().count() == 1) {
            return false;
        }

        int digito1 = calcularDigito(digits.substring(0, 9), 10);
        int digito2 = calcularDigito(digits.substring(0, 9) + digito1, 11);
        return digits.equals(digits.substring(0, 9) + digito1 + digito2);
    }

    public static String formatar(String cpf) {
        String digits = apenasDigitos(cpf);
        if (digits.length() != 11) {
            return digits;
        }
        return digits.substring(0, 3) + "."
                + digits.substring(3, 6) + "."
                + digits.substring(6, 9) + "-"
                + digits.substring(9, 11);
    }

    private static int calcularDigito(String base, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < base.length(); i++) {
            soma += (base.charAt(i) - '0') * (pesoInicial - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
