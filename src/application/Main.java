package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/** Client side of an application which collects URL, start and end points to send to Server
 *  which scrapes text and displays top10 (and all) word occurrences to the Client-side JavaFX GUI.
 *  @author derekdileo */
public class Main extends Application {
	
	// Variables for call to QuestionBox.display()
	protected static boolean defaultSite = false;
	protected static String userWebsite = null;
	protected static String sourceHead = null;
	protected static String sourceEnd = null;
	private String defaultWebsite =  "https://www.gutenberg.org/files/1065/1065-h/1065-h.htm";
	private String defaultSourceHead = "<h1>The Raven</h1>";
	private String defaultSourceEnd = "<!--end chapter-->";
	private String title = "Word Frequency Analyzer";
	private String instruction = "Enter a URL to count frequency of each word.";
	private String siteLabel = "Website to Parse";
	private String sitePlaceholder = "Enter a website to evaluate";
	private String startLabel = "Where to start.";
	private String startPlaceholder= "Text from first line";
	private String endLabel = "Where to finish.";
	private String endPlaceholder = "Text from last line.";
	private String[] defaultEntries = {defaultWebsite, defaultSourceHead, defaultSourceEnd};
	private String[] questionBoxPrompts = {title, instruction, siteLabel, sitePlaceholder, startLabel, startPlaceholder, endLabel, endPlaceholder};
	
	// QuestionBox.display now accepts a third string array to pass to an AlertBox when it launches.
	// This function allows app instructions to be presented to the user. 
	private String appIntroTitle = "Welcome to Word Frequency Counter";
	private String appIntroMessage = "For best results, right-click and inspect the text you'd like to parse. \nThen, copy and paste the elements into the start and finish boxes.";
	private String[] appIntro = {appIntroTitle, appIntroMessage};
	
	// String array to hold QuestionBox.display() responses.
	protected static String[] userResponses;
	
	// Varibles used to show / hide text on GUI
	private StringBuilder sbTen;
	private StringBuilder sbAll;
	
	// These are accessed by the four Controller classes to print to GUI 
	protected static String sbTenString;
	protected static String sbAllString;
	
	/** Main method calls launch() to start JavaFX GUI.
	 *  @param args mandatory parameters for command line method call */
	public static void main(String[] args) {
		// Create wordsTable if it doesn't exist?
		// Server side should handle this functionality. 
		launch();
	}
	
	// Declare stage (window) outside of start() method
	// so it is accessible to closeProgram() method
	protected static Stage window;
	
	/** The start method (which is called on the JavaFX Application Thread) 
	 * is the entry point for this application and is called after the init 
	 * method has returned- most of the application logic takes place here. 
	 * @throws UnsupportedEncodingException */
	@Override
	public void start(Stage primaryStage) throws UnsupportedEncodingException {
		
		// Connect to Server on localhost using port 8000 
		try(Socket socket = new Socket("localhost", 8000)) {
			
			// Set ten second timeout value
			socket.setSoTimeout(10000);
			
			// Get user input for website, startLine & endLine from QuestionBox
			// Or set to default values if none are entered by user
			// (or if hidden testButton is pressed)
			userResponses = processUserInput();
			
			// This boolean is used to determine which scene is loaded 
			// (with or without EAP's The Raven graphic elements) 
			if (userResponses[0].equals(defaultWebsite)) {
				defaultSite = true;
			}
			
			// Print defaultSite value to console for troubleshooting purposes 
			System.out.println("Default site: " + defaultSite);
			
			// Wrap input stream with a buffered reader
			BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			// Wrap output stream with a print writer
			// true = auto-flush output stream to ensure data is sent
			PrintWriter toServer = new PrintWriter(socket.getOutputStream(), true);
			
			// Send userResponses to Server so it execute program
			toServer.println(userResponses[0]);
			toServer.println(userResponses[1]);
			toServer.println(userResponses[2]);
			toServer.println(userResponses[3]);
			
			// Rename stage to window for sanity
			window = primaryStage;
			
			// Set stage title
			window.setTitle("Word Frequency Analyzer");
			
			// Handle close button request. 
			// Launch ConfirmBox to confirm if user wishes to quit
			window.setOnCloseRequest(e -> {
				// Consume the event to allow closeProgram() to do its job
				e.consume();
				closeProgram();
			});
			
			// StringBuilder Object to hold Top Ten Results
			sbTen = new StringBuilder();
			
			// Receive Top Ten Results from Server
			while(true) {

				// Read input from Server (likely contains more than one "line")
				String str = fromServer.readLine();
				
				// Create String array to hold each line of results 
				// which are split using "," as delimiter
				String[] lines = str.split(",");
				
				// Determine current size of String array
				int size = lines.length;
				
				// Search for and remove "pause" if part of current readLine()
				for (int i = 0; i < size; i ++) {
					if(lines[i].equals("pause...")) {
						lines[i] = "";
					}
				} 
				
				// Remove commas, add each line to sbAll
				for (String line : lines) {
					line.replace(",", "\n\n");
					sbTen.append(line + "\n");
				} 
				
				// "pause..." sent from Server when data transmission complete
				if(str.equals("pause...")) {
					break;
				}
				
			}
			
			// StringBuilder Object to hold All Results
			sbAll = new StringBuilder();
			
			// Receive All Results from Server
			while(true) {

				// Read input from Server (likely contains more than one "line")
				String str = fromServer.readLine();
				
				// Create String array to hold each line of results 
				// which are split using "," as delimiter
				String[] lines = str.split(",");
				
				// Determine current size of String array
				int size = lines.length;
				
				// Search for and remove "pause" if part of current readLine()
				for (int i = 0; i < size; i ++) {
					if(lines[i].equals("pause...")) {
						lines[i] = "";
					}
				} 
				
				// Remove commas, add each line to sbAll
				for (String line : lines) {
					line.replace(",", "\n\n");
					sbAll.append(line + "\n");
				} 
				
				// "pause..." sent from Server when data transmission complete
				if(str.equals("pause...")) {
					break;
				}
				
			}

			// Convert StringBuilder Objects to Strings which are called from either:
			// MainC-, MainDefaultC-, AllResultsC- or AllResultsDefaultC- ontrollers to push to GUI
			sbTenString = sbTen.toString();
			sbAllString = sbAll.toString();
			
			// Load GUI
			try {
				if (defaultSite) {
					BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("Main.fxml"));				
					Scene scene = new Scene(root,800,600);
					scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
					window.setScene(scene);
					window.show();
				} else {
					BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("MainDefault.fxml"));				
					Scene scene = new Scene(root,800,600);
					scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
					window.setScene(scene);
					window.show();
				}
			} catch(Exception e) {
				System.out.println("Cannot execute Client window.show()");
				e.printStackTrace();
			} finally {
				// Tell Server to drop database table
				// associated with this Client
				toServer.println("exit...");
			}
		} catch (SocketTimeoutException e) {
			System.out.println("The Socket timed out: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error in Client Socket");
			e.printStackTrace();
		} 
		
	}
	
	/** Method calls QuestionBox to ask user for a website to parse as well as
	 *  where the parsing should start and end.
	 *  @return a String array with responses (site, startPoint, endPoint) to pass to WebScrape.parseSite() Method on Server. */
	private String[] processUserInput() {
		// Gather responses and return to caller
		return QuestionBox.display(questionBoxPrompts, defaultEntries, appIntro);
	}
		
	/** closeProgram() Method uses ConfirmBox class to confirm is user wants to quit */
	protected static void closeProgram() {
       // Ask if user wants to exit (no title necessary, leave blank)
       Boolean answer = ConfirmBox.display("", "Are you sure you want to quit?");
       if (answer) {
           // Run any necessary code before window closes:
		   System.out.println("Window Closed!");
		   window.close();
       }
       
	}
	
}
