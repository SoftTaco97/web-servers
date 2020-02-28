// tutorial followed: 
// none, I am currently a Node.js dev, so this was easiest server to make : ^ )

// Libs
const { createServer } = require('http');
const { join } = require('path');
const { readFile } = require('fs').promises;
const { parse } = require('url');

// Views constants
const VIEWS = join(__dirname, '..', 'static');

/**
 * Method for handling HTTP requests
 * @param { Request } req 
 * @param { Response } res 
 * @return { void }
 */
const requestHandler = async (req, res) => {
  // Variables
  const { pathname } = parse(req.url);
  const filename = `${VIEWS}${(pathname === '/') ? '/index.html' : pathname}`;
  const extension = filename.split('.').pop();
  let contentType;
  let data;
  let statusCode;

  // set content type
  switch(extension) {
    case '.js':
    case '.json':
      contentType = 'application/json'
      break;
    case '.ico':
      contentType = 'image/x-icon';
      break;
    default:
      contentType = `text/${extension}`
  }

  // Construct response
  try {
    data = await readFile(filename);
    statusCode = 200;
  } catch(e) {
    data = await readFile(`${VIEWS}/404.html`);
    contentType = 'text/html';
    statusCode = 404;
  } finally {
    res.writeHead(statusCode, { 'Content-Type': contentType });
    res.write(data);
    res.end();
  }
}

// Start server
const server = createServer(requestHandler);
server.listen(8080, () => console.log('Server listening on 8080'));
