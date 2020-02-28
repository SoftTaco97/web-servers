# tutorials followed:
# https://www.rubyguides.com/2016/08/build-your-own-web-server/

# Libs
require 'socket';

# Path constant
BASE_PATH = File.join(__dir__, '../static/');


# Function for parsing a request
def requestParser(request)
  # Grab the method and path of the request
  method, path = request.lines[0].split;
  
  # Return the path, method and the parsed headers
  { path: path, method: method, headers: parseHeaders(request) }
end

# Function for parsing the request headers
def parseHeaders(request) 
  headers = {};

  # Loop through headers
  request.lines[1..-1].each do |line|
    # If we are moving to the next line, return what we currently have
    return headers if line == "\r\n"

    # Structure data
    header, value = line.split
    header = header.gsub(":", "").downcase.to_sym
    headers[header] = value
  end
end

# Function for getting the contents of a file
def getFileContents(file)
  fileContent = "";
  File.open(file).each do |line|
    fileContent += line;
  end
  fileContent;
end

# Function for structuring the response
def structureResponse(code, codeMessage, file, contentType)
  # grab file contents
  data = getFileContents(file);

  # Return response
  "HTTP/1.1 #{code}\r\n" +
  "Content-Length: #{data.size}\r\n" +
  "\r\n" +
  "#{data}\r\n";
end

# Function for processing the request
def processRequest(clientRequest)
  # Parse the request
  request = requestParser(clientRequest);

  # Construct file path
  file = BASE_PATH + ((request.fetch(:path) == "/") ? "index.html" : request.fetch(:path));

  # Check if file exists
  if File.exists?(file)
    # set content type
    if file.end_with?(".css")
      contentType =  "text/css";
    elsif file.end_with?(".js") || file.end_with?(".json")
      contentType = "application/json";
    elsif file.end_with?(".ico") 
      contentType = "image/x-icon";
    else
      contentType = "text/html";
    end
    
    # Return success response
    return structureResponse(200, "OK", file, contentType);
  else
    # Return not found response
    return structureResponse(404, "Not Found", BASE_PATH + "404.html", "text/html");
  end
end

# Start server
server = TCPServer.new('localhost', 8080);
puts "listening on 8080";

# can we appreciate this 'loop' keyword syntax?
# god Ruby is so weird
loop {
  # accept request
  client = server.accept;

  # Grab request from the client
  request = client.readpartial(2048);

  # Process request and construct a response
  response = processRequest(request);

  # Deliver response to client
  client.write(response);
}
