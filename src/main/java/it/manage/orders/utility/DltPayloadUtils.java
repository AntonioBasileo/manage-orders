package it.manage.orders.utility;

import lombok.NoArgsConstructor;

/**
 * Utility per normalizzare i payload DLT prima della persistenza e del riprocessamento.
 */
@NoArgsConstructor
public final class DltPayloadUtils {

    /**
     * Rimuove byte NUL e caratteri di controllo non compatibili con colonne testuali UTF-8.
     */
    public static String sanitizePersistableText(String input) {
        if (input == null) {
            return null;
        }

        return input.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
    }

    /**
     * Estrae la prima porzione JSON object da una stringa rumorosa (es. prefisso/suffisso binario).
     */
    public static String extractJsonObjectCandidate(String input) {
        if (input == null) {
            return null;
        }

        int start = input.indexOf('{');
        int end = input.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return input.substring(start, end + 1);
        }

        return input;
    }

    /**
     * Applica sanitizzazione e tentativo di estrazione del JSON utile al reprocessing.
     */
    public static String normalizeDltPayload(String payload) {
        return extractJsonObjectCandidate(sanitizePersistableText(payload));
    }

    /**
     * Corregge il JSON double-escaped (es. {@code {\"campo\":\"valore\"}}) prodotto
     * da una serializzazione errata via {@code ObjectMapper.writeValueAsString(String)}.
     * Se il payload contiene sequenze {@code \"} che renderebbero il JSON non valido,
     * sostituisce {@code \"} con {@code "} e rimuove eventuali apici iniziali/finali.
     */
    public static String unescapeIfDoubleEncoded(String input) {
        if (input == null) {
            return null;
        }

        String trimmed = input.trim();
        // Payload corretto: inizia con { e non contiene \" come prima sequenza di escape
        if (trimmed.startsWith("{") && !trimmed.contains("\\\"")) {
            return trimmed;
        }

        // Rimuove eventuali apici esterni prodotti dalla doppia serializzazione
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }

        return trimmed.replace("\\\"", "\"");
    }
}
