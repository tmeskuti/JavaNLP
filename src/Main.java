import java.io.File;

public class Main {
    public static void main(String[] args) {
       // String path = System.getProperty("user.dir");

        String folderPath = args[0];
       // String fullPath = path+folderName;

        LanguageModel model = new LanguageModel();
        model.addLanguage("english", folderPath+"/lang-en");
        model.addLanguage("french", folderPath+"/lang-fr");
        model.addLanguage("spanish", folderPath+"/lang-es");



        System.out.println(model.classifyText(folderPath));
    }
}