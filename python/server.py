# tutorial followed: 
# https://daanlenaerts.com/blog/2015/06/03/create-a-simple-http-server-with-python-3/

# Libs
from http.server import BaseHTTPRequestHandler, HTTPServer
import os.path

""" 
  Class simpleHTTPServer

  @extends BaseHTTPRequestHandler
"""
class simpleHTTPServer(BaseHTTPRequestHandler):
  # Base path for files
  BASE_PATH = os.path.join(os.path.dirname(os.path.realpath(__file__)), '..', 'static')

  """ 
    Method for handling GET Requests
  """
  def do_GET(self):
    # Set the file name
    if self.path == '/':
      filename = self.BASE_PATH + 'index.html'
    else:
      filename = self.BASE_PATH + self.path
    
    # Check if the file exists
    if os.path.isfile(filename):
      self.send_response(200)
      
      # Set content type
      if filename[-4:] == '.css':
        content_type = 'text/css'
      elif filename[-5:] == '.json' or filename[-3:] == '.js':
        content_type = 'application/javascript'
      elif filename[-4:] == '.ico':
        content_type = 'image/x-icon'
      else:
        content_type = 'text/html'
      
      # Send headers
      self.send_header('Content-Type', content_type)
      self.end_headers()

      # Read file and send output
      with open(filename, 'rb') as file:
        self.wfile.write(file.read())
    else:
      # 404 handling
      self.send_response(404)
      self.send_header('Content-Type', 'text/html')
      self.end_headers()
      with open(self.BASE_PATH + '/404.html', 'rb') as page:
        self.wfile.write(page.read())

    return
  

""" 
  Function for creating the server
"""
def run():
  # Create server
  server_address = ('127.0.0.1', 8080)
  httpd = HTTPServer(server_address, simpleHTTPServer)
  
  # Listen for requests
  print('Listening on 8080')
  httpd.serve_forever()
  return

# Away we go
if __name__ == '__main__':
  run()
