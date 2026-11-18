import java.util.*;

public class Simplifier {
    private static final String LAMBDA = "λ";
    private final Map<Character, List<String>> grammar;
    private final char startSymbol;

    public Simplifier(Map<Character, List<String>> grammar, char startSymbol) {
        this.grammar = grammar;
        this.startSymbol = startSymbol;
    }

    public Map<Character, List<String>> transform() {
        Map<Character, List<String>> noLambda = removeLambdaProductions(grammar);
        Map<Character, List<String>> noUnits = removeUnitProductions(noLambda);
        return removeUselessProductions(noUnits);
    }

    private Map<Character, List<String>> removeLambdaProductions(
            Map<Character, List<String>> source) {
        Set<Character> nullable = new HashSet<>();
        boolean changed;

        do {
            changed = false;
            for (Map.Entry<Character, List<String>> entry : source.entrySet()) {
                for (String production : entry.getValue()) {
                    if (production.equals(LAMBDA)
                            || allNullable(production, nullable, source.keySet())) {
                        changed |= nullable.add(entry.getKey());
                    }
                }
            }
        } while (changed);

        Map<Character, List<String>> result = emptyGrammar(source.keySet());
        for (Map.Entry<Character, List<String>> entry : source.entrySet()) {
            for (String production : entry.getValue()) {
                if (!production.equals(LAMBDA)) {
                    addCombinations(result.get(entry.getKey()), production,
                        nullable, source.keySet(), 0, "");
                }
            }
        }

        if (nullable.contains(startSymbol)) {
            addIfMissing(result.get(startSymbol), LAMBDA);
        }
        return result;
    }

    private boolean allNullable(String production, Set<Character> nullable,
                                Set<Character> variables) {
        if (production.equals(LAMBDA) || production.isEmpty()) return true;
        for (char symbol : production.toCharArray()) {
            if (!variables.contains(symbol) || !nullable.contains(symbol)) return false;
        }
        return true;
    }

    private void addCombinations(List<String> output, String production,
                                 Set<Character> nullable, Set<Character> variables,
                                 int index, String current) {
        if (index == production.length()) {
            if (!current.isEmpty()) addIfMissing(output, current);
            return;
        }

        char symbol = production.charAt(index);
        addCombinations(output, production, nullable, variables,
            index + 1, current + symbol);

        if (variables.contains(symbol) && nullable.contains(symbol)) {
            addCombinations(output, production, nullable, variables,
                index + 1, current);
        }
    }

    private Map<Character, List<String>> removeUnitProductions(
            Map<Character, List<String>> source) {
        Map<Character, List<String>> result = emptyGrammar(source.keySet());

        for (char variable : source.keySet()) {
            Set<Character> reachableByUnit = new LinkedHashSet<>();
            Queue<Character> queue = new ArrayDeque<>();
            reachableByUnit.add(variable);
            queue.add(variable);

            while (!queue.isEmpty()) {
                char current = queue.remove();
                for (String production : source.get(current)) {
                    if (isUnit(production, source.keySet())
                            && reachableByUnit.add(production.charAt(0))) {
                        queue.add(production.charAt(0));
                    }
                }
            }

            for (char reachable : reachableByUnit) {
                for (String production : source.get(reachable)) {
                    if (!isUnit(production, source.keySet())) {
                        addIfMissing(result.get(variable), production);
                    }
                }
            }
        }
        return result;
    }

    private boolean isUnit(String production, Set<Character> variables) {
        return production.length() == 1 && variables.contains(production.charAt(0));
    }

    private Map<Character, List<String>> removeUselessProductions(
            Map<Character, List<String>> source) {
        Set<Character> generating = new HashSet<>();
        boolean changed;

        do {
            changed = false;
            for (Map.Entry<Character, List<String>> entry : source.entrySet()) {
                for (String production : entry.getValue()) {
                    if (production.equals(LAMBDA)
                            || allGenerating(production, generating, source.keySet())) {
                        changed |= generating.add(entry.getKey());
                    }
                }
            }
        } while (changed);

        Map<Character, List<String>> generatingGrammar = emptyGrammar(generating);
        for (char variable : generating) {
            for (String production : source.get(variable)) {
                if (production.equals(LAMBDA)
                        || allGenerating(production, generating, source.keySet())) {
                    addIfMissing(generatingGrammar.get(variable), production);
                }
            }
        }

        Set<Character> reachable = new LinkedHashSet<>();
        Queue<Character> queue = new ArrayDeque<>();
        if (generating.contains(startSymbol)) {
            reachable.add(startSymbol);
            queue.add(startSymbol);
        }

        while (!queue.isEmpty()) {
            char current = queue.remove();
            for (String production : generatingGrammar.get(current)) {
                for (char symbol : production.toCharArray()) {
                    if (generating.contains(symbol) && reachable.add(symbol)) {
                        queue.add(symbol);
                    }
                }
            }
        }

        Map<Character, List<String>> result = emptyGrammar(reachable);
        for (char variable : reachable) {
            for (String production : generatingGrammar.get(variable)) {
                addIfMissing(result.get(variable), production);
            }
        }
        return result;
    }

    private boolean allGenerating(String production, Set<Character> generating,
                                  Set<Character> variables) {
        for (char symbol : production.toCharArray()) {
            if (variables.contains(symbol) && !generating.contains(symbol)) return false;
        }
        return true;
    }

    private Map<Character, List<String>> emptyGrammar(Collection<Character> variables) {
        Map<Character, List<String>> result = new LinkedHashMap<>();
        for (char variable : variables) result.put(variable, new ArrayList<>());
        return result;
    }

    private void addIfMissing(List<String> productions, String production) {
        if (!productions.contains(production)) productions.add(production);
    }
}
