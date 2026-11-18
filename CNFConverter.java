import java.util.*;

/** Converts a simplified grammar into Chomsky Normal Form. */
public class CNFConverter {
    private static final String LAMBDA = "λ";

    private final Map<Character, List<String>> simplifiedGrammar;
    private final char startSymbol;
    private final Set<Character> variables;
    private final Map<Character, Character> terminalVariables = new HashMap<>();
    private char nextVariable = 'A';

    public CNFConverter(Map<Character, List<String>> simplifiedGrammar,
                        char startSymbol) {
        this.simplifiedGrammar = simplifiedGrammar;
        this.startSymbol = startSymbol;
        this.variables = new LinkedHashSet<>(simplifiedGrammar.keySet());

        while (variables.contains(nextVariable)) {
            nextVariable++;
        }
    }

    public Map<Character, List<String>> transform() {
        Map<Character, List<String>> cnf = new LinkedHashMap<>();

        for (Map.Entry<Character, List<String>> entry : simplifiedGrammar.entrySet()) {
            cnf.put(entry.getKey(), new ArrayList<>());
        }

        for (Map.Entry<Character, List<String>> entry : simplifiedGrammar.entrySet()) {
            char left = entry.getKey();

            for (String production : entry.getValue()) {
                if (production.equals(LAMBDA)) {
                    if (left == startSymbol) {
                        add(cnf.get(left), LAMBDA);
                    }
                    continue;
                }

                if (production.length() == 1) {
                    if (variables.contains(production.charAt(0))) {
                        throw new IllegalArgumentException(
                            "Unit production remains: " + left + " -> " + production
                        );
                    }
                    add(cnf.get(left), production);
                    continue;
                }

                String replaced = replaceTerminals(production, cnf);
                addInBinaryForm(cnf, left, replaced);
            }
        }

        return cnf;
    }

    // Terminals may only appear by themselves in CNF.
    private String replaceTerminals(String production,
                                    Map<Character, List<String>> cnf) {
        StringBuilder result = new StringBuilder();

        for (char symbol : production.toCharArray()) {
            if (variables.contains(symbol)) {
                result.append(symbol);
            } else {
                char terminalVariable = terminalVariable(symbol, cnf);
                result.append(terminalVariable);
            }
        }

        return result.toString();
    }

    private char terminalVariable(char terminal,
                                  Map<Character, List<String>> cnf) {
        if (terminalVariables.containsKey(terminal)) {
            return terminalVariables.get(terminal);
        }

        char variable = freshVariable();
        terminalVariables.put(terminal, variable);
        cnf.put(variable, new ArrayList<>());
        add(cnf.get(variable), String.valueOf(terminal));
        return variable;
    }

    // Converts ABCD into A -> A X, X -> B Y, Y -> C D.
    private void addInBinaryForm(Map<Character, List<String>> cnf,
                                 char left,
                                 String production) {
        char currentLeft = left;

        for (int index = 0; index < production.length() - 2; index++) {
            char newVariable = freshVariable();
            cnf.put(newVariable, new ArrayList<>());

            String pair = "" + production.charAt(index) + newVariable;
            add(cnf.get(currentLeft), pair);
            currentLeft = newVariable;
        }

        String finalPair = production.substring(production.length() - 2);
        add(cnf.get(currentLeft), finalPair);
    }

    private char freshVariable() {
        while (variables.contains(nextVariable)) {
            nextVariable++;
        }

        char fresh = nextVariable++;
        variables.add(fresh);
        return fresh;
    }

    private void add(List<String> productions, String production) {
        if (!productions.contains(production)) {
            productions.add(production);
        }
    }
}
