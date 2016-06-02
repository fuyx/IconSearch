/**
 * Created by fuyx on 2/15/16.
 */

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

import org.jsoup.Jsoup;
import org.jsoup.*;
import org.jsoup.parser.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import org.apache.commons.io.*;

public class Indexer {
    static private HashSet<String> isIndexed=new HashSet<>();

    private Indexer() {
    }

    public static void main(String[] args) {
        String indexPath = "index";
        String docsPath = "urls";
        Indexer indexer=new Indexer();
        for (int i = 0; i < args.length; i++) {
            if ("-index".equals(args[i])) {
                indexPath = args[i + 1];
                i++;
            } else if ("-docs".equals(args[i])) {
                docsPath = args[i + 1];
                i++;
            }
        }

        try {
            Directory dir = FSDirectory.open(Paths.get(indexPath));

            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(OpenMode.CREATE);

            ArrayList<String> urls=indexer.readURLs(docsPath);

            IndexWriter writer = new IndexWriter(dir, iwc);
            indexer.indexDocs(writer, urls);
            writer.close();

            System.out.println("Create index for " + docsPath + " succeed!");
        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    ArrayList<String> readURLs(String path) throws IOException{
        File dir = new File(path);
        if (dir.isDirectory())
            return null;
        BufferedReader br = new BufferedReader(new FileReader(path));
        ArrayList<String> urls=new ArrayList<>(); 
        String line = br.readLine();
        while (line != null) {
            urls.add(line);
            line = br.readLine();
        }
        br.close();
        return urls;
    }

    // Indexes all the docs in the path
    void indexDocs(final IndexWriter writer, ArrayList<String> urls) throws IOException {
        for (String line:urls) {
            if (line.length()>0)
                indexDoc(writer, line);
        }
    }

    // Index single doc
    void indexDoc(IndexWriter writer, String url_string) throws IOException {
        try {
            String page=getpage(new URL(url_string));
            org.jsoup.nodes.Document jDoc = Jsoup.parse(page);
            
            Elements h1s = jDoc.getElementsByTag("h1");
            StringBuffer sb = new StringBuffer();
            for (Element e : h1s)
                sb.append(e.ownText());
            String titles = sb.toString();

            Elements img = jDoc.getElementsByTag("img");
            URL host=new URL(url_string);
            for (Element el : img){
                URL url=new URL(host, el.attr("src"));
                String lSrc_string=url.toString().toLowerCase();
                Element parent = el.parent();
                if(!lSrc_string.endsWith(".png")&&!lSrc_string.endsWith(".jpg")&&!lSrc_string.endsWith(".jpeg")&&!lSrc_string.endsWith(".svg"))
                    continue;
                org.apache.lucene.document.Document lDoc = new org.apache.lucene.document.Document();
                if(el.attr("alt").length()>0 && !isIndexed.contains(url.toString())){
                    lDoc.add(new StringField("url", url_string, Field.Store.YES));
                    lDoc.add(new StringField("src", url.toString(), Field.Store.YES));
                    lDoc.add(new TextField("content", el.attr("alt") + "\n" + url.toString() + "\n" + url_string, Field.Store.NO));
                    System.out.println("Indexing " + url.toString());
                    writer.addDocument(lDoc);
                    isIndexed.add(url.toString());
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally{
            System.gc();
        }
    }

    public String getpage(URL url)

    {
        try {
            // try opening the URL
            URLConnection urlConnection = url.openConnection();

            urlConnection.setAllowUserInteraction(false);

            InputStream urlStream = url.openStream();

            String page = IOUtils.toString(urlStream, "UTF-8");
            return page;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
