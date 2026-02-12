package alicanteweb.pelisapp.service.moderation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Analizador de contenido para detectar palabras y patrones inapropiados.
 * Aplica principio de responsabilidad única (SRP).
 */
@Component
@Slf4j
public class ContentAnalyzer {

    private static final String[] PROHIBITED_WORDS = {
        "puta", "puto", "idiota", "imbecil", "imbécil", "estupido", "estúpido", "mierda",
        "joder", "coño", "gilipollas", "cabron", "cabrón", "tonto", "subnormal",
        "retrasado", "marica", "maricon", "maricón", "hijo de puta", "hijoputa",
        "pendejo", "pendeja", "mamada", "mamadas", "verga", "pinche", "chinga",
        "perra", "perro", "zorra", "zorro", "rata", "basura",

        // Variaciones con caracteres especiales
        "p*ta", "p@ta", "est*pido", "est@pido", "idi*ta", "idi@ta", "m1erda",
        "j0der", "c0ño", "ton+o", "t0nto",

        // Inglés básico
        "fuck", "fucking", "shit", "damn", "bitch", "asshole", "motherfucker",
        "bastard", "stupid", "idiot", "moron", "dumb", "dumbass", "retard",
        "whore", "slut", "crap", "bullshit",

        // Insultos suaves que también son inapropiados
        "tonto del culo", "que asco", "que mierda", "vete a la mierda",
        "me da asco", "da pena", "que pena", "que triste", "patético", "patética",
        "ridículo", "ridícula", "menudo idiota", "eres un", "sois unos"
    };

    private static final Set<String> HIGH_SEVERITY_WORDS = Set.of(
        "puta", "fuck", "maric", "hijoputa", "motherfucker"
    );

    /**
     * Analiza el texto en busca de contenido inapropiado.
     */
    public ContentAnalysisResult analyze(String text) {
        if (text == null || text.isBlank()) {
            return new ContentAnalysisResult(0, "", 1.0, 0.0);
        }

        String normalizedText = normalizeText(text);

        int badWordCount = 0;
        StringBuilder detectedWords = new StringBuilder();
        double severityMultiplier = 1.0;

        for (String badWord : PROHIBITED_WORDS) {
            if (normalizedText.contains(badWord)) {
                badWordCount++;
                appendDetectedWord(detectedWords, badWord);

                if (HIGH_SEVERITY_WORDS.stream().anyMatch(badWord::contains)) {
                    severityMultiplier = 1.5;
                }
            }
        }

        // Detectar patrones sospechosos
        if (hasSuspiciousPatterns(normalizedText, text)) {
            badWordCount++;
            appendDetectedWord(detectedWords, "patrón sospechoso");
        }

        double toxicityScore = calculateToxicityScore(badWordCount, severityMultiplier);

        return new ContentAnalysisResult(
            badWordCount,
            detectedWords.toString(),
            severityMultiplier,
            toxicityScore
        );
    }

    /**
     * Normaliza el texto eliminando caracteres especiales y espacios extra.
     */
    private String normalizeText(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-záéíóúñüç0-9\\s]", "") // Eliminar caracteres especiales
                .replaceAll("\\s+", " "); // Normalizar espacios
    }

    /**
     * Detecta patrones sospechosos en el texto.
     */
    private boolean hasSuspiciousPatterns(String normalizedText, String originalText) {
        return normalizedText.matches(".*\\b\\w*[4@]\\w*\\b.*") || // Palabras con números/símbolos
               normalizedText.matches(".*\\b\\w{1,2}\\*+\\w*\\b.*") || // Palabras censuradas con *
               originalText.length() < 3 || // Comentarios muy cortos sospechosos
               (originalText.toUpperCase().equals(originalText) && originalText.length() > 10); // Todo en mayúsculas
    }

    /**
     * Añade una palabra detectada a la lista.
     */
    private void appendDetectedWord(StringBuilder detectedWords, String word) {
        if (!detectedWords.isEmpty()) {
            detectedWords.append(", ");
        }
        detectedWords.append(word);
    }

    /**
     * Calcula la puntuación de toxicidad basada en problemas detectados.
     */
    private double calculateToxicityScore(int badWordCount, double severityMultiplier) {
        double baseToxicity = Math.min(1.0, badWordCount * 0.4); // Aumentado de 0.3 a 0.4
        return Math.min(1.0, baseToxicity * severityMultiplier);
    }

    /**
     * Resultado del análisis de contenido.
     */
    public record ContentAnalysisResult(
        int badWordCount,
        String detectedWords,
        double severityMultiplier,
        double toxicityScore
    ) {}
}
