# Preselection and Cosine Similary Calculator
This module gets the user input as ```String``` and saves the **cosine similarity & jaccard distance of the n-grams from the user input to the n-grams from the documents which we define as most important for the classification of plagiarism** in a ```output.txt```. 
- Cosine Similarity: Max & Rene
- Preselection: Steven
- Generator: Claudio

## Getting Started
This repository creates a ```.jar``` in the ```target``` dictionary. After running you should have a ```output.txt``` which the ```CosineJaccardCalc.scala``` created and will be described later on.

connect to the hadoop cluster via ssh - you have to insert your HTW-Student Number here
```bash
ssh s0XXXXX@hadoop05.f4.htw-berlin.de
```

clone the repository
```bash
git clone https://github.com/WikiPlagWS2018/Preselection-and-Cosine-Similary-Calculator.git
```

In order to create a ```.jar``` you need to have **sbt** installed. Just enter following command:
```bash
cd Preselection-and-Cosine-Similary-Calculator
sbt clean assembly
```

load .jar to hadoop cluster via scp **if the cluster doesn't have sbt then skip this**
```bash
scp target/scala-2.11/WikiIDFApp-assembly-0.1.0-SNAPSHOT.jar s0558366@hadoop05.f4.htw-berlin.de:~
```

start .jar with spark (you need to go to the .jar directory first)
```bash
cd target/scala-2.11
spark2-submit --master yarn --executor-memory 10G --driver-memory 6G WikiIDFApp-assembly-0.1.0-SNAPSHOT.jar > std_out.out 2> std_err.err &
```
  
## Preselection
### General Idea
Our plagiarism algorithm wants to compare the user input with Wikipedia articles at runtime and calculate certain key values/features. **In order to reduce the number of documents to be compared, a preselection should run before the actual plagiarism algorithm, which passes on the documents relevant only for the classification to the plagiarism algorithm/other modules.**

The inverse index IDF was formed for each word and stored in the database as a tuple (word-> IDF value).
- IDF = Inverse Document Frequency, tells how many times a word appears in all documents. The higher the value, the more articles have this value.

We use this value to output the "most important" words in the user input and to compare only the input with the articles in which the "most important" words appear.
- That means we define the "relevant documents" as documents in which the "most important" words appear. The "most important" words are determined by the IDF.

**However, the preselection can be further developed e.g. by forming other inverse index or using weights (IDF only counts 0.5 compared to other inverse index.**

### Information about Implementation
```idfGenerator.scala``` creates a IDF-value for every word in our database and adds the calculated value to our database. Following snippet should work:

```scala
new Generator().generate_idf()
```

```Preselector.scala``` gets the user input as ```String``` and returns the documents with the ```N``` most important words as map ```map[String, List[String]]```. This can be done:
```scala
val N = //some integer
val p = new Preselector(userinput)
p.calculateIDF()
val importantDocuments = p.getTopN(N) //returns the map
```
This also means that in order for our ```Preselector``` to work you always need to call ```calculateIDF()``` first.

## Cosine and Jaccard Similarity
### General Idea
Our goal here is to compare the user input with all relevant documents selected during preselection. This is achieved by splitting all documents into [n-grams](https://en.wikipedia.org/wiki/N-gram) and then comparing each n-gram from the user document against each n-gram from the pool of pre-selected comparison files from Wikipedia.The results of these comparisons is then saved into a text file ```output.txt``` which serves as the input for the neural network

For the comparison we decided on two different algorithms. [Cosine Similarity](https://en.wikipedia.org/wiki/Cosine_similarity) and [Jaccard Index](https://en.wikipedia.org/wiki/Jaccard_index)

### Information about Implementation
```CosineJaccardCalc``` compares all documents with each other and creates the output text. 
```scala
new CosineJaccardCalc(importantDocuments)
```
The one parameter is a ```Map[String, List[String]]``` of all documents which are to be compared. This map needs at least 2 entries of which one must be the ``userinput``. As long as these conditions are met the latest output.txt file will be overwritten with all the new comparison values.


## Problems
- **Optimizing Cosine Jaccard Calculation** 
  - The current implementation doesn't use spark to calculate the features
  - In order to fully use our hadoop cluster, we need to rewrite the code
  - **Solution:** Replace current implementation with a spark implementation - that means working with RDD and methods provided by spark
- **Too many documents selected:** The Preselector selects too many Documents for Cosine and Jaccard Similarity 
  - The Cosine Jaccard Calculator creates N-Grams from the Userinput and the "important documents"
  - This leads to a problem because if there are too many documents our current implementation takes too long
  - try to **optimize the cosine jaccard calculator** first before adding more inverse indexes **for a better performance**
  - **Solution:** Add  some more inverse indexes to reduce the amount of important documents to be checked
- **Quality of documents are too poor:** 
  - Some of the tokenized words in the documents are still not correct
  - Symbols like ‚‘ or ; or , are still in the database  
  - Stopwords and unimportant documents are still in there like „2edasdasd“ or „as“ or „123123“
  - **Solution:** The pipeline/preprocessor/parser which adds the tokenized documents to the database has to be updated/changed so that we get a better quality of documents
