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
    '<p class="text-muted"><a href="http://www.cs.nyu.edu/~yf899/cgi-bin/wse/project/search.cgi">click here</a> to use text to search.'\
    '</p>'\
    '</div>'\
    \
    '<div class="form-group">'\
    "<form method=POST action=\"${SCRIPT}\" enctype=\"multipart/form-data\">"\
    'please upload a PNG format icon image'\
    '<input type="file" name="upload">'\
    '<br>'\
    '<input type="submit" class="btn btn-primary" value="Search"></form>'\
    '</div>'\

 #  If no search arguments, exit gracefully now.                                                                                                                                  

IFS='+'
 
echo "<p>"
if [ !  -z "$CONTENT_LENGTH"  ]; then
  TMPOUT=input

  cat >$TMPOUT
  SUFFIX=$(cat $TMPOUT |  head -2|tail -1|cut -d"\"" -f4|cut -d. -f2)

  # Get the line count
  LINES=$(wc -l $TMPOUT | cut -d ' ' -f 1)
  
  # Remove the first four lines
  tail -$((LINES - 4)) $TMPOUT >$TMPOUT.1
 
  # Remove the last line
  head -$((LINES - 5)) $TMPOUT.1 >$TMPOUT.$SUFFIX

  rm $TMPOUT.1
  rm $TMPOUT

# Filters out characters <>&*?./ to block malicious users
  q=$(/usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java -cp ./lib/*:./lire/*:. LIRESearcher -web -img $TMPOUT.$SUFFIX)
  # echo $q
  if [ ! -z "$q" ]; then
    /usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java -cp ./lib/*:./lire/*:. Retriever -web -q "$q"
  else
    echo "Opps, no similar image has been found in standard icon image gallery."
  fi
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

