# Preselection and Cosine Similary Calculator
This module gets the user input as ```String``` and returns the **cosine similarity & jaccard distance of the user input to the documents which we define as most important for the classification of plagiarism**(from the preselector).

## Getting Started
In order to create a ```.jar``` you need to have **sbt** installed. Just enter following command:
```bash
sbt clean assembly
```
This creates a ```.jar``` in the ```target``` dictionary. After running you should have a ```output.txt``` which the ```CosineJaccardCalc.scala``` created and will be described later on.

connect to the hadoop cluster via ssh
```bash
ssh s0558366@hadoop05.f4.htw-berlin.de
```

load .jar to hadoop cluster via scp **if the cluster doesn't have sbt**
```bash
scp target/scala-2.11/WikiIDFApp-assembly-0.1.0-SNAPSHOT.jar s0558366@hadoop05.f4.htw-berlin.de:~
```

start .jar with spark (you need to go to the .jar directory first)
```bash
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
new MainClass().generate_idf()
```

```Preselector.scala``` gets the user input as ```String``` and returns the documents with the ```N``` most important words as map ```map[String, List[String]]```. This can be done:
```scala
val N = //some integer
val p = new Preselector(userinput)
p.calculateIDF()
val importantDocuments = p.getTopN(N) //returns the map
```
This also means that in order for our ```Preselector``` to work you always need to call ```calculateIDF()``` first.

### Problems

- The Preselector selects too many Documents for Cosine and Jaccard Similarity
  - Add  some more inverse indexes to reduce the amount of important documents to be checked
  - Quick and easy: just select the first 10 documents and trash the others
- The tokenized words in the documents are still not correct
  - , symbol is still in the database  
  - stopwords and unimportant documents are still in there
  - The pipeline which adds the tokenized documents to the database has to be updated/changed so that we get a better quality of documents

## Cosine Similarity

