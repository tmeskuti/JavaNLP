import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static java.util.stream.Collectors.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LanguageModel {
    private final ConcurrentMap<String, String> languageModels;

    public LanguageModel() {
        languageModels = new ConcurrentHashMap<>();
    }
    // Trains a model on the language specified as a parameter, using the provided language folder
    public void addLanguage(String language, String folderPath) {

        List<Future<List<String>>> futures = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Walk the files in the folder
        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(file -> futures.add(executor.submit(() -> Files.lines(file).collect(toList()))));
        } catch (IOException e) {
            System.out.println("No files found!");
        }

        // wait for all threads to complete and collect the results
        String text = futures.stream().map(future -> {
                    try {
                        return future.get().stream().map(string -> string.replaceAll("[.,!?]*", "")).collect(joining());
                    } catch (InterruptedException | ExecutionException e) {
                        // handle the exception
                        return Collections.emptyList();
                    }
                })
                .collect(toList()).toString();

        languageModels.put(language, text);
        executor.shutdown();
    }
    
    public Map<String, Integer> getNgrams(String text, int n) {
        return IntStream.range(0, text.length() - n + 1)
                .mapToObj(i -> text.substring(i, i + n))
                .collect(toMap(nGram -> nGram, nGram -> 1, Integer::sum));
    }

    // The function below uses cosine similarity to calculate the language closest to the mystery file
    public void calculateDocumentDistance(String mysteryText, Map<String, String> languageModels, int n) {
        Map<String, Integer> mysteryNgrams = getNgrams(mysteryText, n);
        Map<String, Map<String, Integer>> langNgram = new HashMap<>();
        Map<String, Double> langSimilarity = new HashMap<>();

        languageModels.entrySet()
                .forEach(languageSet -> langNgram.put(languageSet.getKey(), getNgrams(String.valueOf(languageSet), n)));

        langNgram.keySet()
                .forEach(stringIntegerMap -> {
                    double firstVector, secondVector = 0, dotProduct = 0;

                    firstVector = mysteryNgrams.values().stream().mapToDouble(count -> count * count).sum();
                    firstVector = Math.sqrt(firstVector);

                    secondVector = mysteryNgrams.values().stream().mapToDouble(count -> count * count).sum();
                    secondVector = Math.sqrt(secondVector);

                    dotProduct = mysteryNgrams.keySet().stream()
                            .mapToDouble(nGram -> mysteryNgrams
                                    .get(nGram) * langNgram
                                    .get(stringIntegerMap)
                                    .getOrDefault(nGram, 0)).sum();

                    langSimilarity.put(stringIntegerMap, dotProduct / (firstVector * secondVector));
                });

        String mostSimilarLanguage = langSimilarity.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();

        System.out.println(mostSimilarLanguage);
    }

    // This function streams the content of the mystery.txt file and passes it as a parameter to the function that does the comparing
    public void classifyText(String folderPath) {
        File mysteryFile = new File(folderPath, "mystery.txt");
        if (!mysteryFile.exists()) {
            throw new IllegalArgumentException("mystery.txt not found in " + folderPath);
        }

        String mysteryText = null;
        try {
            mysteryText = Files.lines(mysteryFile.toPath()).collect(toList()).stream().map(string -> string.replaceAll("[.,!?]*", "")).collect(joining());
        } catch (IOException e) {
            System.out.println("Empty");
        }

        calculateDocumentDistance(mysteryText, languageModels, 3);
    }
}
