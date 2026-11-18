import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the grammar file name: ");

        String fileName = scanner.nextLine().trim();
        Path inputPath = Path.of(fileName);

        Map<Character, List<String>> grammar = new LinkedHashMap<>();

        for (String line : Files.readAllLines(inputPath)) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] sides = line.split("->", 2);
            char key = sides[0].trim().charAt(0);
            List<String> productions = grammar.computeIfAbsent(
                key, ignored -> new ArrayList<>()
            );

            for (String value : sides[1].split("\\|")) {
                productions.add(value.trim());
            }
        }

        System.out.println("Original:   " + grammar);

        Simplifier simplifier = new Simplifier(grammar, 'S');
        Map<Character, List<String>> simplifiedGrammar = simplifier.transform();

        System.out.println("Simplified: " + simplifiedGrammar);

        CNFConverter converter = new CNFConverter(simplifiedGrammar, 'S');
        Map<Character, List<String>> cnfGrammar = converter.transform();

        System.out.println("CNF:        " + cnfGrammar);

        GNFConverter gnfConverter = new GNFConverter(cnfGrammar, 'S');
        Map<Character, List<String>> gnfGrammar = gnfConverter.transform();

        System.out.println("GNF:        " + gnfGrammar);
    }
}
