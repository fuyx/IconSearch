#!/bin/bash                                                                               

echo "Content-type: text/html"
echo ""

echo '<html>'
echo '<head>'
echo '<link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.css">'
echo '<link rel="stylesheet" type="text/css" href="main.css">'
echo '<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">'
echo '<title>Icon Search - Yixian Fu</title>'
echo '</head>'
echo '<body>'
echo '<div class="container">'

echo '<div class="jumbotron">'\
    '<h1>Icon Search</h1>'\
    '<p>Search some icons for your applications.</p>'\
    '<p class="text-muted"><a href="http://www.cs.nyu.edu/~yf899/cgi-bin/wse/project/img_search.cgi">click here</a> to use image to search.'\
    '</p>'\
    '</div>'\
    \
    "<form class=\"form-group\" method=GET action=\"${SCRIPT}\">"\
    '<input type="text" class="form-control" name="query">'\
    '<label class="checkbox-inline"><input type="checkbox" name="option" value="dup">Try image duplicate detection(it may take some time).</lable>'\
    '</form>'\


 #  If no search arguments, exit gracefully now.                                                                                                                                  

IFS='+' 
echo "<p>"
if [ ! -z "$QUERY_STRING" ]; then
  mkdir tmpImgIndex
  mkdir tmpImgOutDir
    # No looping this time, just extract the data you are looking for with sed:   
  option=$(echo "$QUERY_STRING" | sed -n "s/^.*option=\([^&]*\).*$/\1/p")           
  keyword=$(echo "$QUERY_STRING" | sed -n "s/^.*query=\([^&]*\).*$/\1/p" | sed "s/[*|.|<|>]/ /g" |sed "s/%2[6|F]/ /g" | sed "s/%3[F|E]/ /g")
  
# Filters out characters <>&*?./ to block malicious users
  /usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java -cp ./lib/*:./lire/*:. Retriever -web -$option -q "$keyword" 
  yes| rm -d -r tmpImgIndex
  yes| rm -d -r tmpImgOutDir
  
fi


echo '</p>'
echo '</div>'

echo '<footer class="footer">'\
      '<div class="container">'\
        '<p class="text-muted"><a href="http://www.cs.nyu.edu/~yf899/cgi-bin/wse/project/citations.html">CITATIONS</a>.'\
        '<br>Contact <a href="http://fuyx.github.io/">@Yixian Fu</a>.</p>'\
      '</div>'\
    '</footer>'
echo '</body>'
echo '</html>'

exit 0

