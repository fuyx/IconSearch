/**
 * Created by fuyx on 2/15/16.
 */

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Color;

import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.AutoColorCorrelogram;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.FCTH;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.searchers.GenericFastImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.ImageSearcher;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriter;

public class Retriever {
    static boolean isHTMLFormat=false;
    static boolean OPEN_IMG_DUP_DETECT=false;
    static String tmpImgDir="./tmpImgOutDir/";
    static String tmpImgIndex="./tmpImgIndex";
    static int hitsPerPage = 50;
    static int LINE_SIZE=5;
    static double LIKELIHOOD=10.0;

    private Retriever() {
    }

    public static void main(String[] args) throws Exception {
        String index = "index";
        String field = "content";
        int repeat = 0;
        boolean raw = false;
        String queryString = null;
        String br = "";

        for (int i = 0; i < args.length; i++) {
            if ("-index".equals(args[i])) {
                index = args[i + 1];
                i++;
            } else if ("-field".equals(args[i])) {
                field = args[i + 1];
                i++;
            } else if ("-q".equals(args[i])) {
                queryString = args[i + 1];
                i++;
            } else if ("-raw".equals(args[i])) {
                raw = true;
            } else if ("-web".equals(args[i])) {
                br = "<br>";
                isHTMLFormat=true;
            } else if("-dup".equals(args[i])){
                OPEN_IMG_DUP_DETECT=true;
            }
        }

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);

        Analyzer analyzer = new StandardAnalyzer();
        QueryParser parser = new QueryParser(field, analyzer);

        String[] strs = queryString.split("_");
        String line="";
        for(String str:strs)
            line+=(str+" ");
        if (line == null || line.length() == -1) {
            System.out.println("Please input legal query sentence.");
            return;
        }
        line = line.trim();
        if (line.length() == 0) {
            System.out.println("Please input legal query sentence.");
            return;
        }

        Query query = parser.parse(line);

        doPagingSearch(searcher, query, hitsPerPage, raw, br);
        reader.close();
    }

    public static void doPagingSearch(IndexSearcher searcher, Query query,
                                      int hitsPerPage, boolean raw, String br) throws IOException {

        // BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

        TopDocs results = searcher.search(query, 10000);
        ArrayList<ScoreDoc> hits=getUniqScoreDoc(searcher,results.scoreDocs);

        // System.out.println(hits.length);

        int start = 0;
        int end = Math.min(hits.size(), hitsPerPage);

        // System.out.println(hits.length);
        for (int i = start; i < end; i++) {
            Document doc = searcher.doc(hits.get(i).doc);

            String host_url = doc.get("url");
            if(host_url==null)  host_url="";
            String img_src = doc.get("src");
            if(img_src==null)  img_src="";
            String alts = doc.get("alt");
            if(alts==null)  alts="";

            if(isHTMLFormat){
                if(i%LINE_SIZE==0)
                    System.out.println("<div class=\"row\">");
                System.out.println(getHTMLString(img_src,host_url,alts));
                if((i+1)%LINE_SIZE==0)
                    System.out.println("</div>");
            }else{
                if (img_src != null) {
                    System.out.println((i + 1) + ". " + img_src + br);
                } else {
                    System.out.println((i + 1) + ". null" + br);
                }
                if (host_url != null) {
                    System.out.println("\t" + host_url + br);
                } else {
                    System.out.println("\t" + "No path for this document" + br);
                }
            }
        }
    }

    public static ArrayList<ScoreDoc> getUniqScoreDoc(IndexSearcher searcher, ScoreDoc[] hits) throws IOException{
        ArrayList<ScoreDoc> temp=new ArrayList<>(), res=new ArrayList<>();
        HashMap<String,ScoreDoc> scoreDocMap=new HashMap<>();
        HashSet<String> shownURL=new HashSet<>();
        for(ScoreDoc hit:hits){
            Document doc = searcher.doc(hit.doc);
            String path = doc.get("url");
            if(!shownURL.contains(path)) {
                shownURL.add(path);
                temp.add(hit);
            }
        }
        if(temp.size()<2||!OPEN_IMG_DUP_DETECT)
            return temp;

        // remove duplicate retrieved images
        GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder(CEDD.class);
        globalDocumentBuilder.addExtractor(FCTH.class);
        globalDocumentBuilder.addExtractor(AutoColorCorrelogram.class);
        IndexWriterConfig conf = new IndexWriterConfig(new WhitespaceAnalyzer());
        IndexWriter iw = new IndexWriter(FSDirectory.open(Paths.get(tmpImgIndex)), conf);
        int count=0;
        for(ScoreDoc hit:temp){
            if(count>=hitsPerPage)
                break;
            String img_src=searcher.doc(hit.doc).get("src");
            scoreDocMap.put(getImg(img_src,tmpImgDir),hit);
            count++;
        }
        // Process p;
        // try {
        //     // ProcessBuilder pb = new ProcessBuilder("mogrify -alpha off "+tmpImgDir+"*.png");
        //     // Process p = pb.start();     // Start the process.
        //     // p.waitFor();                // Wait for the process to finish.
        //     p = Runtime.getRuntime().exec("mogrify -alpha off "+tmpImgDir+"*.png");
        //     p.waitFor();
        //     // p = Runtime.getRuntime().exec("mogrify -alpha off "+tmpImgDir+"*.jpg");
        //     // p.waitFor();
        //     // System.out.println("Script executed successfully");
        // }catch (Exception e) {
        //     e.printStackTrace();
        // }
        ArrayList<String> images = FileUtils.getAllImages(new File(tmpImgDir), true);
        for (Iterator<String> it = images.iterator(); it.hasNext(); ) {
            String imageFilePath = it.next();
            try {
                BufferedImage img = ImageIO.read(new FileInputStream(imageFilePath));
                Document document = globalDocumentBuilder.createDocument(img, imageFilePath);
                iw.addDocument(document);
            } catch (Exception e) {
                System.err.println("Error reading image or indexing it.");
                e.printStackTrace();
            }
        }
        iw.close();

        HashSet<String> uniqImg=new HashSet<>();
        for(String fileName:scoreDocMap.keySet()){
            BufferedImage img = null;
            boolean passed=false;
            File f = new File(tmpImgDir+fileName);
            if (f.exists()) {
                try {
                    img = ImageIO.read(f);
                    passed = true;
                } catch (IOException e) {
                    e.printStackTrace(); 
                }
            }
            if(!passed) continue;
            IndexReader ir = DirectoryReader.open(FSDirectory.open(Paths.get(tmpImgIndex)));
            ImageSearcher dup_searcher = new GenericFastImageSearcher(30, CEDD.class);
            ImageSearchHits dup_hits = dup_searcher.search(img, ir);
            String closetfileAbsoluteName = ir.document(dup_hits.documentID(1)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            String[] strs=closetfileAbsoluteName.split("/");
            String closetfileName=strs[strs.length-1];
            // System.out.println(fileName+"("+searcher.doc(scoreDocMap.get(fileName).doc).get("src")+")"+": the most silimar image is "+closetfileName+" with score "+dup_hits.score(1));
            if(dup_hits.score(1)<LIKELIHOOD){
                if(uniqImg.contains(closetfileName))
                    continue;
                else
                    uniqImg.add(fileName);
            }
            res.add(scoreDocMap.get(fileName));
        }
        return res;
    }

    public static String getImg(String src,String outDir) throws IOException{
        String[] strs=src.split("/");
        String outFileName=strs[strs.length-1];
        strs = outFileName.split("\\.");
        String suffix=strs[strs.length-1];
        URL url = new URL(src);

        BufferedImage img = ImageIO.read(url);
        BufferedImage copy = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = copy.createGraphics();
        g2d.setColor(Color.BLACK); // Or what ever fill color you want...
        g2d.fillRect(0, 0, 32,32);
        g2d.drawImage(img, 0, 0, 32,32,null);
        // g2d.dispose();

        File file = new File(outDir+outFileName);
        int i=0;
        while(file.exists()){
            i++;
            file = new File(outDir+i+outFileName);
        }
        ImageIO.write(copy, suffix, file);
        return i==0?outFileName:(i+outFileName);
    }

    private static String getHTMLString(String img_src, String host_url, String alts){
        StringBuffer sb=new StringBuffer();
        sb.append("<div class=\"col-md-2\"> ").append(" <a title=\"").append(alts).append("\" ").append(" href=\"").append(host_url)
        .append("\" class=\"thumbnail\"> \n").append(" <img alt=\"").append(alts).append("\" height=128 width=128 src=\" ")
        .append(img_src).append("\"").append("/> </a> </div>");
        return sb.toString();
    }

}

