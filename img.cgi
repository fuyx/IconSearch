#!/bin/bash                                                                               

echo "Content-type: text/html"
echo ""

echo '<html>'
echo '<head>'
echo '<link rel="stylesheet" type="text/css" href="bootstrap.css">'
echo '<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">'
echo '<title>Welcom to the Search Page of Yixian Fu</title>'
echo '</head>'
echo '<body>'

echo "<h1>Welcom to the Search Page of Yixian Fu</h1>"\
    \
    "<form method=POST action=\"${SCRIPT}\" enctype=\"multipart/form-data\">"\
      '<table nowrap>'\
    'upload a .png image'\
          '<tr><td> </TD><TD><input type="file" name="upload" size=20></td></tr>'\
          '</tr></table>'\
    '<br><input type="submit" value="Search"></form>'





 #  If no search arguments, exit gracefully now.                                                                                                                                  

IFS='+'
TMPOUT=input.png

cat >$TMPOUT
 
  # Get the line count
  LINES=$(wc -l $TMPOUT | cut -d ' ' -f 1)
 
  # Remove the first four lines
  tail -$((LINES - 4)) $TMPOUT >$TMPOUT.1
 
  # Remove the last line
  head -$((LINES - 5)) $TMPOUT.1 >$TMPOUT

  rm $TMPOUT.1

 
echo "<p>"
if [  -z "$CONTENT_LENGTH"  ]; then
  exit 0
else
# Filters out characters <>&*?./ to block malicious users
  q=$(/usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java -cp ./lib/*:./lire/*:. LIRESearcher "$TMPOUT" |  head -1 | cut -d/ -f8)
  java -cp ./lib/*:. Retriever -web -q "$q"
fi
echo "</p>"
echo '</body>'
echo '</html>'

exit 0

