// tutorials followed:
// https://medium.com/@ssaurel/create-a-simple-http-web-server-in-java-3fc12b29d5fd, 
// https://javarevisited.blogspot.com/2015/06/how-to-create-http-server-in-java-serversocket-example.html

// Libs
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;


/**
 * Server class
 * 
 * Entry point of the server
 */
public class Server 
{
  /**
   * Main method
   * Entry point of the program
   */
  public static void main(String[] args) throws IOException 
  {
    // Create a server on port 8080
    final ServerSocket server = new ServerSocket(8080);
    System.out.println("listening on port 8080");
    
    // Loop endlessly
    while(true) 
    {
      try 
      {
        // Accept request
        final Socket client = server.accept();
        // Handle new request in seperate thread
        new RequestHandler(client);
      }
      catch(IOException ex) 
      {
        System.err.println("Server connection error: " + ex.getMessage());
      }
    }
  }
}

/**
 * RequestHandler class
 * 
 * Class for handling any requests to the server
 * 
 * @extends Thread
 */
class RequestHandler extends Thread
{
  // Properties, or fields, depending on who you ask
  private Socket connection;
  private String filePath;
  BufferedReader in;
  BufferedOutputStream out;
  PrintWriter printOut;

  /**
   * @constructor
   */
  public RequestHandler(Socket client) throws IOException
  {
    connection = client;
    // Have to do this here so I can declare that this method throws an IOException
    filePath = new File(System.getProperty("user.dir") + "/../static/").getCanonicalPath();
    
    // Start the new thread
    start();
  }

  /**
   * Method for handling HTTP Requests
   * 
   * @override
   */
  @Override
  public void run() 
  {
    try 
    {
      // Grabbing request information
      in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String request = in.readLine();
      StringTokenizer requestParser = new StringTokenizer(request);
      String method = requestParser.nextToken().toUpperCase();
      String fileRequested = requestParser.nextToken().toLowerCase();

      // Creating output streams
      out = new BufferedOutputStream(connection.getOutputStream());
      printOut = new PrintWriter(connection.getOutputStream());
      
      try
      {
        // Make sure the request is a GET request
        if(method.equals("GET")) 
        {
          if(fileRequested.equals("/")) 
          {
            fileRequested = "index.html";
          }
          getRequest(fileRequested);
        }

        // not a GET request, throw an exception to serve them a 404
        throw new FileNotFoundException();
      } 
      catch(FileNotFoundException ex) 
      {
        // File not found, send 404
        notFound();
      }
    }
    catch(Exception ex)
    {
      System.out.println("Exception caught: " + ex);
    }
    finally 
    {
      try 
      {
        // Close all streams
        in.close();
        out.close();
        printOut.close();
        connection.close();
      }
      catch(Exception ex) 
      {
        System.err.println("Error closing streams: " + ex.getMessage());
      }
    }
  }

  /**
   * Method for handling GET requests
   */
  private void getRequest(String fileRequested) throws FileNotFoundException, IOException
  {
    // Variables
    int fileLength = getFileLength(fileRequested);
    byte[] fileContents = getFileContents(fileRequested);
    String contentType = null;
    FileInputStream fileStream = null;
    
    // Set content type
    if(fileRequested.endsWith(".js") || fileRequested.endsWith(".json")) 
    {
      contentType = "application/javascript";
    }
    else if(fileRequested.endsWith(".css")) 
    {
      contentType = "text/css";
    }
    else if(fileRequested.endsWith(".ico")) 
    {
      contentType = "image/x-icon";
    }
    else 
    {
      contentType = "text/html";
    }

    // Send response
    sendResponse(200, "OK", contentType, fileLength, fileContents);
  }

  /**
   * Method for handling requests for files that don't exist
   */
  private void notFound() throws FileNotFoundException, IOException
  {
    String file = "404.html";
    int fileLength = getFileLength(file);
    byte[] fileContents = getFileContents(file);

    sendResponse(404,  "Not Found", "text/html", fileLength, fileContents);
  }

  /**
   * Method for sending a response to the client
   */
  private void sendResponse(int status, String statusMessage, String contentType, int length, byte[] data) throws IOException
  {
    printOut.println("HTTP/1.1 " + status + statusMessage);
    printOut.println("Date: " + new Date());
    printOut.println("Content-Type: " + contentType);
    printOut.println("Content-length: " + length);
    printOut.println();
    printOut.flush();
    out.write(data, 0, length);
    out.flush();
  }

  /**
   * Method for getting the contents of a file
   */
  private byte[] getFileContents(String fileName) throws FileNotFoundException, IOException
  {
    File file = new File(filePath, fileName);
    int fileLength = getFileLength(fileName);
    FileInputStream fileStream = new FileInputStream(file);
    byte[] fileContents = new byte[fileLength];
    fileStream.read(fileContents);
    return fileContents;
  }

  /**
   * Method for getting the length of a file
   */
  private int getFileLength(String fileName) throws FileNotFoundException
  {
    File file = new File(filePath, fileName);
    return (int) file.length();
  }
}
