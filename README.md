# NLP using the n-gram model
This program uses basic NLP to determine the language of a mystery text file. You are expected to provide as a command-line argument, the path of a folder (similiar to [this](https://github.com/tmeskuti/JavaNLP/tree/master/lang)), that contains subdirectories with files in a specific language. These subdirectories are used to train a model, which then is used to determine the language of the **_mystery.txt_** file.

## To run the code
```bash
cd src
javac Main.java
java Main.java <YOUR_FOLDER_PATH>
```