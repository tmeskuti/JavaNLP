
public class Main {
    public static void main(String[] args) {
        // Get the folder path as a CL argument
        String folderPath = args[0];

        // Create and train the NLP model
        LanguageModel model = new LanguageModel();
        model.addLanguage("English", folderPath+"/lang-en");
        model.addLanguage("French", folderPath+"/lang-fr");
        model.addLanguage("Spanish", folderPath+"/lang-es");

        // Classify the mystery file
        model.classifyText(folderPath);
    }
}