//Lets require/import the HTTP module
var http = require('http');

//Lets define a port we want to listen to
const PORT=8080;

//We need a function which handles requests and send response
function handleRequest(req, res){
  console.log('****************************** ' + req.method + ' ****************************** ');
  console.log('======== HEADERS ========');
  console.log(req.headers);
  if (req.method == 'POST') {
    var body = "";
    req.on('data', function (chunk) {
      body += chunk;
    });
    req.on('end', function () {
      console.log('======== BODY START ========');
      console.log('POSTed: ' + body);
      console.log('======== BODY END ========');
    });
  }
  console.log('****************************************************************');
  res.writeHead(200);
  res.end();
}

//Create a server
var server = http.createServer(handleRequest);

//Lets start our server
server.listen(PORT, function(){
  //Callback triggered when server is successfully listening. Hurray!
  console.log("Server listening on: http://localhost:%s", PORT);
});
