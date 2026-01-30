package alicanteweb.pelisapp.service;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ModeratingAI {

    private static final Set<String> BAD_WORDS = Set.of(
            "puta", "idiota", "imbecil", "estupido", "mierda", "joder", "coño", "gilipollas", "cabron", "tonto"
    );

    /**
     * Analyze text and return a score between 0.0 and 1.0 where 1.0 is clean and 0.0 is fully toxic.
     */
    public double analyzeText(String text) {
        if (text == null || text.isBlank()) return 1.0;
        String lower = text.toLowerCase();
        long matches = BAD_WORDS.stream().filter(lower::contains).count();
        // simple heuristic: 1.0 - penalty per match (capped)
        double score = 1.0 - Math.min(1.0, matches * 0.3);
        return Math.max(0.0, score);
    }
}
