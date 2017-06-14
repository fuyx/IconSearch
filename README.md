Icon Search
=============
[Icon Search](www.cs.nyu.edu/~yf899/cgi-bin/wse/project/search.cgi) is a specialized image search engine to search icons.  

Geting Started
=============
There are 5 java file here:
crawler.java<br>
Indexer.java<br>
Retriever.java<br>
LIREIndexer.java<br>
LIRESearcher.java<br>
<br>
To compile:<br>
javac -cp ./lib/*:. crawler.java<br>
javac -cp ./lib/*:. Indexer.java<br>
javac -cp ./lib/*:./lire/*:. Retriever.java<br>
javac -cp ./lib/*:./lire/*:. LIREIndexer.java<br>
javac -cp ./lib/*:./lire/*:. LIRESearcher.java<br>
<br>
( Please ignore the warnings. The warnings generate
because LIRE recommends Java 8. )<br>
<br>
To run:<br>
If you are in one of the CIMS Server:<br>
java -cp ./lib/*:. crawler -m 100<br>
java -cp ./lib/*:. Indexer<br>
/usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java -cp ./lib/*:./lire/*:. Retriever [-web] -q [query]<br>
/usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java -cp ./lib/*:./lire/*:. LIREIndexer icon_imgs<br>
/usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java -cp ./lib/*:./lire/*:. LIRESearcher [-web ] -img [path-to-input-image]<br>
<br>
If you are in your own machine, please first change your java version to JAVA 8, then:<br>
java -cp ./lib/*:. crawler -m 100<br>
java -cp ./lib/*:. Indexer<br>
java -cp ./lib/*:./lire/*:. Retriever [-web] -q [query]<br>
java -cp ./lib/*:./lire/*:. LIREIndexer icon_imgs<br>
java -cp ./lib/*:./lire/*:. LIRESearcher [path-to-input-image]<br>
<br>
There are 2 cgi file, 1 HTML file, and 2 CSS file for the front end.<br>
You don't need to compile them, or run them. Just put them below
/web directory. <br>