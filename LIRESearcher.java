/*
* @Authore yf899@nyu.edu
*/
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.searchers.GenericFastImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.ImageSearcher;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class LIRESearcher {
    static boolean WEB=false;
    static double THRESHOLD=40.0;
    static double MAX_DIFF=10.0;
    public static void main(String[] args) throws IOException {
        // Checking if arg[0] is there and if it is an image.
        BufferedImage img = null;
        boolean passed = false;

        for (int i = 0; i < args.length; i++) {
            if ("-img".equals(args[i])) {
                File f = new File(args[i + 1]);
                if (f.exists()) {
                    try {
                        img = ImageIO.read(f);
                        passed = true;
                    } catch (IOException e) {
                        e.printStackTrace(); 
                    }
                }
            } else if ("-web".equals(args[i])) {
                WEB=true;
            } 
        }

        if (args.length > 0) {
            File f = new File(args[0]);
            if (f.exists()) {
                try {
                    img = ImageIO.read(f);
                    passed = true;
                } catch (IOException e) {
                    e.printStackTrace(); 
                }
            }
        }

        if (!passed) {
            System.out.println("No image given as first argument.");
            System.out.println("Run \"Searcher <query image>\" to search for <query image>.");
            System.exit(1);
        }

        IndexReader ir = DirectoryReader.open(FSDirectory.open(Paths.get("LIREindex")));
        ImageSearcher searcher = new GenericFastImageSearcher(30, CEDD.class);
//        ImageSearcher searcher = new GenericFastImageSearcher(30, AutoColorCorrelogram.class);

        // searching with a image file ...
        ImageSearchHits hits = searcher.search(img, ir);
        // searching with a Lucene document instance ...
//        ImageSearchHits hits = searcher.search(ir.document(0), ir);
        if(WEB){
            StringBuffer sb=new StringBuffer();
            double prev_score=0.0;
            for (int i = 0; i < hits.length(); i++) {
                if(hits.score(i)>THRESHOLD)
                    break;
                if(i>0){
                    double diff=hits.score(i)-prev_score;
                    if(diff>MAX_DIFF)
                        break;
                    sb.append("_");
                }
                String fileName = ir.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                String[] strs=fileName.split("/");
                String raw=strs[strs.length-1];
                strs=raw.split("\\.");
                sb.append(strs[0]);
                prev_score=hits.score(i);
            }
            System.out.println(sb.toString());
        }else{
            for (int i = 0; i < hits.length(); i++) {
                String fileName = ir.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                System.out.println(hits.score(i) + ": \t" + fileName);
            }
        }
    }
}
