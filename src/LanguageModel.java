import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LanguageModel {
    private ConcurrentMap<String, Map<String, Integer>> languageModels;

    public LanguageModel() {
        languageModels = new ConcurrentHashMap<>();
    }

    public void addLanguage(String language, String folderPath) {
        File[] files = new File(folderPath).listFiles();
        if (files == null) {
            throw new IllegalArgumentException("No files found in " + folderPath);
        }

        ExecutorService executor = Executors.newFixedThreadPool(8); // create a thread pool with 8 threads
        List<Future<List<String>>> futures = new ArrayList<>();

        for (File file : files) {
            if (file.isDirectory()) {
                // submit a task to read the .txt files in the language subfolder concurrently
                futures.add(executor.submit(() -> {
                    File[] subfolderFiles = file.listFiles();
                    if (subfolderFiles == null) {
                        return Collections.emptyList();
                    }
                    return Arrays.stream(subfolderFiles)
                            .filter(subfolderFile -> subfolderFile.getName().endsWith(".txt"))
                            .flatMap(subfolderFile -> {
                                try {
                                    return Files.lines(subfolderFile.toPath());
                                } catch (IOException e) {
                                    // handle the exception
                                    return Stream.empty();
                                }
                            })
                            .collect(Collectors.toList());
                }));
            }
        }

        // wait for all tasks to complete and collect the results
        List<String> text = (List<String>) (List<String>) futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        // handle the exception
                        return Collections.emptyList();
                    }
                })
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // Construct n-gram model for the given language
        Map<String, Integer> model = new HashMap<>();
        IntStream.range(0, text.size() - 2)
                .forEach(i -> {
                    String nGram = text.get(i) + " " + text.get(i + 1) + " " + text.get(i + 2);
                    model.put(nGram, model.getOrDefault(nGram, 0) + 1);
                });

        // Add the model to the languageModels map
        languageModels.put(language, model);
    }




    public String classifyText(String folderPath) {
        File mysteryFile = new File(folderPath, "mystery.txt");
        if (!mysteryFile.exists()) {
            throw new IllegalArgumentException("mystery.txt not found in " + folderPath);
        }

        List<String> mysteryText = null;
        try {
            mysteryText = Files.lines(mysteryFile.toPath())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            // handle the exception
        }

        List<String> finalMysteryText = mysteryText;
        return languageModels.entrySet().stream()
                .max((entry1, entry2) -> computeSimilarity(entry1.getValue(), finalMysteryText) - computeSimilarity(entry2.getValue(), finalMysteryText))
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }


    private int computeSimilarity(Map<String, Integer> model, List<String> text) {
        return (int) IntStream.range(0, text.size() - 2)
                .mapToObj(i -> text.get(i) + " " + text.get(i + 1) + " " + text.get(i + 2))
                .filter(model::containsKey)
                .count();
    }
}