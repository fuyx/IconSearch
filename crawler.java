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

public class crawler {

    // data set holding important info of a URL
    class URLInfo {
        public URL url;
        public int score;
        public String anchor;
        public String url_string;
        long timestamp;

        public URLInfo(URL url, String a, String u) {
            this(url);
            anchor = a;
            url_string = url.toString();
            score = 0;

            if (DEBUG) System.out.println("anchor = " + anchor + " url_string = " + url_string);
        }

        public URLInfo(URL url) {
            this.url = url;
            timestamp = System.currentTimeMillis();
        }

        public void Score(String[] query) {
//            if(DEBUG) System.out.println("counts map = "+word_counts.toString());
            this.score = computeScore(query);
//            System.out.println("calling Score(): score = " + this.score);
        }

        private int computeScore(String[] query) {
            if (query == null || query.length == 0) {
                if (DEBUG) System.out.println("query == null||query.length==0");
                return 0;
            }

            int k = 0;
            for (String q : query) {
                if (anchor.toLowerCase().contains(q)) {
                    k++;
                    if (DEBUG)
                        System.out.println("computeScore for " + url_string + " : anchor contains query word: " + q);
                }
            }
            if (k > 0)
                return k * 50;

            for (String q : query) {
                if (url.toString().toLowerCase().contains(q))
                    return 40;
            }
            return 0;
        }

    }

    public static final String DISALLOW = "Disallow:";
    public static final String USERAGENT = "User-agent:";
    public static final String AGENT = "*";
    public static final int MAXSIZE = 200000; // Max size of file
    public static final String output_file="urls";
    // print debug info?
    private boolean DEBUG = true;
    // print trace log?
    public boolean TRACE = false;
    // ignore robots.txt
    public boolean IGNORE=false;
    // URLs to be searched
    PriorityQueue<URLInfo> newURLs;
    HashMap<URL, URLInfo> newURLMap;
    // Known URLs
    HashSet<URL> visitedURLs;
    // max number of pages to download
    int maxPages = 20;
    // input query tokens
    String[] query;
    // whether Allowed by a robots.txt
    private HashMap<String, Boolean> isAllowed;
    // output directory
    private String outDir = "./output/";

    ArrayList<String> url_list = new ArrayList<>();

    // Top-level procedure. Keep popping a url off newURLs, download it, and accumulate new URLs
    public void run(String[] args) {
        initialize(args);
        while (visitedURLs.size() < maxPages && newURLs.size()<maxPages*5 && !newURLs.isEmpty()) {
            URLInfo url_info = newURLs.poll();
            newURLMap.remove(url_info.url);
            URL url = url_info.url;
            if (DEBUG) System.out.println("Searching " + url.getFile());
            if (checkRobotExclusion(url)) {
                String page = getpage(url, url_info.score);
                if (page.length() == 0)
                    continue;
                url_list.add(url.toString());
                // exits after downloaded the final page
                if (visitedURLs.size() == maxPages - 1)
                    break;
                visitedURLs.add(url);
                processpage(url, page);
                if (TRACE) System.out.println();
                if(url_list.size()>1000)
                    writeOuputeFile();
            }
        }
        writeOuputeFile();
        if (DEBUG) System.out.println("Search complete.");
    }


    // initializes data structures.  argv is the command line arguments.
    public void initialize(String[] args) {
        String usage = "java crawler -u [URL] -q [query] -docs [doc path] -m [#page] [-t]";
        visitedURLs = new HashSet<>();
        isAllowed = new HashMap<>();
        String[] start_urls_string = {
                "http://www.iconarchive.com/show/android-settings-icons-by-graphicloads/msg-icon.html",
                "https://www.iconfinder.com/icons/378441/find_house_icon",
                "http://findicons.com/icon/75245/credit_card",
                "http://www.iconarchive.com/show/colorful-long-shadow-icons-by-graphicloads/Coffee-cup-icon.html", 
                "http://www.iconarchive.com/show/100-flat-icons-by-graphicloads/phone-icon.html", 
                "https://icons8.com/web-app/2795/high-volume",
                "http://iconmonstr.com/sound-wave-2", 
                "https://www.iconfinder.com/icons/285662/map_icon",
                "https://www.iconfinder.com/icons/386456/browser_chrome_icon",
                "http://iconmonstr.com/video-14/",
                "http://findicons.com/icon/64873/download",
                "http://findicons.com/icon/177338/currency_dollar_blue?id=276794",
                "http://www.iconseeker.com/search-icon/iphone/chat-5.html",
                "http://www.iconseeker.com/search-icon/openphone/calendar-1.html",
                "http://www.flaticon.com/free-icon/bucket_114594",
                "http://www.flaticon.com/free-icon/finance-symbol-of-four-currencies-on-a-hand_49695",
                "http://findicons.com/icon/129657/apple_folder?id=129762"};
        String q = "";
        boolean isQuery = false;
        ArrayList<URL> start_urls=new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            if ("-q".equals(args[i])) {
                q = args[i + 1];
                i++;
                isQuery = true;
            } else if ("-docs".equals(args[i])) {
                outDir = args[i + 1] + "/";
                i++;
                isQuery = false;
            } else if ("-m".equals(args[i])) {
                maxPages = Integer.parseInt(args[i + 1]);
                i++;
                isQuery = false;
            } else if ("-t".equals(args[i])) {
                TRACE = true;
                isQuery = false;
            } else if (isQuery) {
                q += " " + args[i];
            }
        }

        StringTokenizer st = new StringTokenizer(q);
        int i = 0;
        query = new String[st.countTokens()];
        while (st.hasMoreTokens())
            query[i++] = st.nextToken().toLowerCase();
        Arrays.sort(query, new Comparator<String>() {
            public int compare(String s1, String s2) {
                return s2.length() - s1.length();
            }
        });

        try {
            for(String str:start_urls_string)
                start_urls.add(new URL(str));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }

        newURLs = new PriorityQueue<URLInfo>(maxPages, new Comparator<URLInfo>() {
            public int compare(URLInfo i1, URLInfo i2) {
                if (i2.score != i1.score)
                    return i2.score - i1.score;
                else
                    return (int) (i1.timestamp - i2.timestamp);
            }
        });
        newURLMap = new HashMap<>();

        for(URL u:start_urls){
            URLInfo start_url_info = new URLInfo(u);
            newURLs.add(start_url_info);
            newURLMap.put(u, start_url_info);
        }

        String[] dirs = outDir.split("/");
        String path = "";
        for (String d : dirs) {
            path += d + "/";
            File dir = new File(path);
            dir.mkdir();
        }

    /*Behind a firewall set your proxy and port here!
    */
        Properties props = new Properties(System.getProperties());
        props.put("http.proxySet", "true");
        props.put("http.proxyHost", "webcache-cup");
        props.put("http.proxyPort", "8080");

        Properties newprops = new Properties(props);
        System.setProperties(newprops);
    }

    // check robot exclusion by analyzing robots.txt
    public boolean checkRobotExclusion(URL url) {
        if(IGNORE)
            return true;
        String url_string = url.toString();
        if (isAllowed.containsKey(url_string))
            return isAllowed.get(url_string);
        String strHost = url.getHost();

        // form URL of the robots.txt file
        String strRobot = "http://" + strHost + "/robots.txt";
        URL urlRobot;
        try {
            urlRobot = new URL(strRobot);
        } catch (MalformedURLException e) {
            // something weird is happening, so don't trust it
            isAllowed.put(url_string, false);
            return false;
        }

        if (DEBUG) System.out.println("Checking robot protocol " +
                urlRobot.toString());
        String strCommands;
        try {
            InputStream urlRobotStream = urlRobot.openStream();

            // read in entire file
            byte b[] = new byte[1000];
            int numRead = urlRobotStream.read(b);
            strCommands = (numRead != -1) ? new String(b, 0, numRead) : "";
            while (numRead != -1) {
                numRead = urlRobotStream.read(b);
                if (numRead != -1) {
                    String newCommands = new String(b, 0, numRead);
                    strCommands += newCommands;
                }
            }
            urlRobotStream.close();
        } catch (IOException e) {
            // if there is no robots.txt file, it is OK to search
            isAllowed.put(url_string, true);
            return true;
        }

        if (DEBUG) System.out.println("Begin analyzing robot.txt");
        // assume that this robots.txt refers to us and
        // search for "User-agent:" and "Disallow:" commands.
        String strURL = url.getFile();
        int index = 0;
        while ((index = strCommands.indexOf(USERAGENT, index)) != -1) {
            index += USERAGENT.length();
            String strPath = strCommands.substring(index);
            StringTokenizer st = new StringTokenizer(strPath);

            if (!st.hasMoreTokens())
                break;

            String agent = st.nextToken();
            if (agent.equals(AGENT)) {
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if (token.equals(DISALLOW))
                        break;
                }
                String strBadPath = "";
                if (st.hasMoreTokens())
                    strBadPath = st.nextToken();
                else
                    break;

                // if the URL starts with a disallowed path, it is not safe
                if (strURL.indexOf(strBadPath) == 0) {
                    isAllowed.put(url_string, false);
                    return false;
                }
            }
        }
        isAllowed.put(url_string, true);
        return true;
    }

    // adds new URL to the queue. Accept only new URL's that end in
    // htm or html. oldURL is the context, newURLString is the link
    // (either an absolute or a relative URL).
    public void addnewurl(URL oldURL, String newUrlString, String newUrlAnchor) {
        URL url;
        try {
            url = new URL(oldURL, newUrlString);
            if (!visitedURLs.contains(url)) {
                String filename = url.getFile();
                // System.out.println("filename = " + filename);
                int iSuffix = filename.lastIndexOf("htm");
                String doc = getPageName(filename);
                int dSuffix = doc.lastIndexOf(".");
                if ((iSuffix == filename.length() - 3) ||
                        (iSuffix == filename.length() - 4) ||
                        (dSuffix == -1)) {
                    URLInfo url_info = new URLInfo(url, newUrlAnchor, newUrlString);
                    url_info.Score(query);
                    if (!newURLMap.containsKey(url)) {
                        newURLs.add(url_info);
                        newURLMap.put(url, url_info);
                        if (TRACE)
                            System.out.println("Adding to queue: " + url.toString() + ". Score = " + url_info.score);
                    } else {
                        URLInfo temp = newURLMap.get(url);
                        newURLs.remove(temp);
                        if (TRACE)
                            System.out.println("Adding " + url_info.score + " to score of " + url.toString() + ".");
                        temp.score+=url_info.score;
                        newURLs.add(temp);
                    }
                }
            }

        } catch (MalformedURLException e) {
            return;
        }
    }


    // Download contents of URL
    public String getpage(URL url, int score)

    {
        try {
            // try opening the URL
            URLConnection urlConnection = url.openConnection();
            if (TRACE) System.out.println("Downloading " + url.toString() + ". Score = " + score);

            urlConnection.setAllowUserInteraction(false);

            InputStream urlStream = url.openStream();
            // search the input stream for links
            // first, read in the entire URL
            byte b[] = new byte[1000];
            int numRead = urlStream.read(b);
            if (numRead == -1)
                return "";
            String content = new String(b, 0, numRead);
            while ((numRead != -1) && (content.length() < MAXSIZE)) {
                numRead = urlStream.read(b);
                if (numRead != -1) {
                    String newContent = new String(b, 0, numRead);
                    content += newContent;
                }
            }

            if (TRACE) System.out.println("Received: " + url.toString() + ".");
            urlStream.close();
            return content;

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
            System.err.println("StringIndexOutOfBoundsException: " + url.toString());
            return "";
        }
    }

    // Go through page finding links to URLs.  A link is signalled
    // by <a href=" ...   It ends with a close angle bracket, preceded
    // by a close quote, possibly preceded by a hatch mark (marking a
    // fragment, an internal page marker)
    public void processpage(URL url, String page)

    {
        if (DEBUG) System.out.println("start processing " + url);

        try{
            Document doc = Jsoup.parse(page);
            Elements elements = doc.getElementsByTag("a");
            // System.out.println("elements.size() = "+elements.size());
            for (Element el : elements) {
                String newUrlString = el.attr("href");
                int iHatchMark = newUrlString.indexOf("#");
                if(iHatchMark>-1)
                    newUrlString = newUrlString.substring(0,iHatchMark);
                String newUrlAnchor = el.text();
                addnewurl(url, newUrlString, newUrlAnchor);
                // System.out.println("image tag: " + el.attr("href") + "\tcontent: " + el.text());
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        if (DEBUG) System.out.println("finish processing " + url);
    }

    // get the name(the string after the last "/") of the url
    private String getPageName(String page_path) {
        if (page_path.endsWith("/"))
            page_path = page_path.substring(0, page_path.length() - 1);
        String[] strs = page_path.split("/");
        return strs[strs.length - 1];
    }

    private void writeOuputeFile(){
        // System.out.println("writeOuputeFile");
        try {
            FileWriter writer = new FileWriter(output_file,true);
            StringBuffer sb=new StringBuffer();
            for(String str:url_list)
                sb.append(str).append("\n");

            writer.write(sb.toString());
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        url_list.clear();
    }

    public static void main(String[] args) {

        crawler crawler = new crawler();
        crawler.run(args);
    }
}

