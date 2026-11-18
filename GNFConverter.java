import java.util.*;

/** Converts a simplified/CNF grammar into Greibach Normal Form. */
public class GNFConverter {
    private static final String LAMBDA = "λ";

    private final Map<Character, List<String>> originalGrammar;
    private final char startSymbol;
    private final Set<Character> variables;
    private final Map<Character, Character> terminalVariables = new HashMap<>();
    private char nextVariable = 'A';

    public GNFConverter(Map<Character, List<String>> grammar, char startSymbol) {
        this.originalGrammar = grammar;
        this.startSymbol = startSymbol;
        this.variables = new LinkedHashSet<>(grammar.keySet());

        while (variables.contains(nextVariable)) {
            nextVariable++;
        }
    }

    public Map<Character, List<String>> transform() {
        Map<Character, List<String>> grammar = copyGrammar(originalGrammar);
        eliminateLeftRecursion(grammar);

        Map<Character, List<String>> result = new LinkedHashMap<>();
        for (char variable : grammar.keySet()) {
            result.put(variable, new ArrayList<>());
        }

        for (Map.Entry<Character, List<String>> entry : grammar.entrySet()) {
            for (String production : entry.getValue()) {
                if (production.equals(LAMBDA)) {
                    if (entry.getKey() == startSymbol) {
                        add(result.get(entry.getKey()), LAMBDA);
                    }
                    continue;
                }

                for (String expanded : expandLeading(
                        production, grammar, new HashSet<>())) {
                    if (expanded.equals(LAMBDA)) {
                        if (entry.getKey() == startSymbol) {
                            add(result.get(entry.getKey()), LAMBDA);
                        }
                    } else {
                        add(result.get(entry.getKey()),
                            replaceLaterTerminals(expanded, result));
                    }
                }
            }
        }

        return result;
    }

    /*
     * Uses the standard ordered substitution step, followed by removal of
     * immediate left recursion. Helper productions have the form:
     * A -> beta R, R -> alpha R | alpha
     */
    private void eliminateLeftRecursion(Map<Character, List<String>> grammar) {
        List<Character> ordered = new ArrayList<>(grammar.keySet());

        for (int i = 0; i < ordered.size(); i++) {
            char current = ordered.get(i);

            for (int j = 0; j < i; j++) {
                char earlier = ordered.get(j);
                List<String> replaced = new ArrayList<>();

                for (String production : grammar.get(current)) {
                    if (startsWithVariable(production, earlier)) {
                        String suffix = production.substring(1);
                        for (String earlierProduction : grammar.get(earlier)) {
                            if (!earlierProduction.equals(LAMBDA)) {
                                add(replaced, earlierProduction + suffix);
                            }
                        }
                    } else {
                        add(replaced, production);
                    }
                }
                grammar.put(current, replaced);
            }

            List<String> recursive = new ArrayList<>();
            List<String> nonRecursive = new ArrayList<>();

            for (String production : grammar.get(current)) {
                if (startsWithVariable(production, current)) {
                    recursive.add(production.substring(1));
                } else {
                    nonRecursive.add(production);
                }
            }

            if (!recursive.isEmpty()) {
                char helper = freshVariable();
                ordered.add(helper);
                grammar.put(helper, new ArrayList<>());

                List<String> currentProductions = grammar.get(current);
                currentProductions.clear();
                for (String beta : nonRecursive) {
                    if (!beta.equals(LAMBDA)) {
                        add(currentProductions, beta + helper);
                    }
                }

                for (String alpha : recursive) {
                    if (!alpha.isEmpty()) {
                        add(grammar.get(helper), alpha + helper);
                        add(grammar.get(helper), alpha);
                    }
                }
            }
        }
    }

    private boolean startsWithVariable(String production, char variable) {
        return !production.isEmpty() && production.charAt(0) == variable;
    }

    private List<String> expandLeading(String production,
                                       Map<Character, List<String>> grammar,
                                       Set<Character> expanding) {
        if (production.equals(LAMBDA) || production.isEmpty()) {
            return List.of(production);
        }

        char first = production.charAt(0);
        if (!variables.contains(first)) {
            return List.of(production);
        }

        if (!expanding.add(first)) {
            throw new IllegalArgumentException(
                "Left-recursive grammar could not be converted: " + production
            );
        }

        List<String> expanded = new ArrayList<>();
        String suffix = production.substring(1);
        for (String replacement : grammar.get(first)) {
            for (String result : expandLeading(
                    replacement + suffix, grammar, expanding)) {
                add(expanded, result);
            }
        }

        expanding.remove(first);
        return expanded;
    }

    private String replaceLaterTerminals(String production,
                                          Map<Character, List<String>> result) {
        StringBuilder converted = new StringBuilder();
        converted.append(production.charAt(0)); // GNF's leading terminal.

        for (int i = 1; i < production.length(); i++) {
            char symbol = production.charAt(i);
            if (variables.contains(symbol)) {
                converted.append(symbol);
            } else {
                char helper = terminalVariable(symbol, result);
                converted.append(helper);
            }
        }
        return converted.toString();
    }

    private char terminalVariable(char terminal,
                                  Map<Character, List<String>> result) {
        Character existing = terminalVariables.get(terminal);
        if (existing != null) return existing;

        char helper = freshVariable();
        terminalVariables.put(terminal, helper);
        result.put(helper, new ArrayList<>());
        add(result.get(helper), String.valueOf(terminal));
        return helper;
    }

    private char freshVariable() {
        while (variables.contains(nextVariable)) {
            nextVariable++;
        }

        char fresh = nextVariable++;
        variables.add(fresh);
        return fresh;
    }

    private Map<Character, List<String>> copyGrammar(
            Map<Character, List<String>> source) {
        Map<Character, List<String>> copy = new LinkedHashMap<>();
        for (Map.Entry<Character, List<String>> entry : source.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    private void add(List<String> productions, String production) {
        if (!productions.contains(production)) {
            productions.add(production);
        }
    }
}
