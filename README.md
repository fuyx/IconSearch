#Icon Search
#####[Icon Search](www.cs.nyu.edu/~yf899/cgi-bin/wse/project/search.cgi) is a specialized image search engine to search icons.  
<br>
##Geting Started
First, make sure you are in one of the following server:
(for CPU and memory intensive processes)
Hostname	
crunchy1.cims.nyu.edu	
crunchy3.cims.nyu.edu	
crunchy4.cims.nyu.edu	
crunchy5.cims.nyu.edu	
crunchy6.cims.nyu.edu	

Then, make sure all of the following jar files are in ./lib and ./lire directories.
├── lib
│   ├── commons-io-2.4.jar
│   ├── jsoup-1.9.1.jar
│   ├── jsoup-1.9.1-javadoc.jar
│   ├── jsoup-1.9.1-sources.jar
│   ├── lucene-analyzers-common-5.2.1.jar
│   ├── lucene-analyzers-common-5.4.1.jar
│   ├── lucene-core-5.2.1.jar
│   ├── lucene-core-5.4.1.jar
│   └── lucene-queryparser-5.4.1.jar
├── lire
│   └── lire.jar


There are 5 java file here:
crawler.java
Indexer.java
Retriever.java
LIREIndexer.java
LIRESearcher.java

To compile:
javac -cp ./lib/*:. crawler.java
javac -cp ./lib/*:. Indexer.java
javac -cp ./lib/*:./lire/*:. Retriever.java
javac -cp ./lib/*:./lire/*:. LIREIndexer.java
javac -cp ./lib/*:./lire/*:. LIRESearcher.java

( Please ignore the warnings. The warnings generate 
because LIRE recommends Java 8. )

To run:
If you are in one of the CIMS Server:
java -cp ./lib/*:. crawler -m 100
java -cp ./lib/*:. Indexer
/usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java -cp ./lib/*:./lire/*:. Retriever [-web] -q [query]
/usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java -cp ./lib/*:./lire/*:. LIREIndexer icon_imgs
/usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java -cp ./lib/*:./lire/*:. LIRESearcher [-web ] -img [path-to-input-image]

If you are in your own machine, please first change your java version to JAVA 8, then:
java -cp ./lib/*:. crawler -m 100
java -cp ./lib/*:. Indexer
java -cp ./lib/*:./lire/*:. Retriever [-web] -q [query]
java -cp ./lib/*:./lire/*:. LIREIndexer icon_imgs
java -cp ./lib/*:./lire/*:. LIRESearcher [path-to-input-image]

There are 2 cgi file, 1 HTML file, and 2 CSS file for the front end.
You don't need to compile them, or run them. Just put them below 
/web directory.