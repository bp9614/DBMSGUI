package gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DBMSFrontEnd extends Application{
	private Scene scene;
	private Pane pane;
	private PreparedStatement stmt;
	private final Connection connection;
	private VBox queryingOptions;
	private TextFlow infoSection;
	private ArrayList<String> attributeInfo;
	private String[][] resultsQuery;
	private Timeline timeline;
	
	private static final String DB_PATH = DBMSFrontEnd.class.getResource("PhoneDatabase.sqlite").toString();
	
	public DBMSFrontEnd() throws SQLException{
		pane = new Pane();
		pane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
		
		scene = new Scene(pane, 1200, 900);
		
		infoSection = new TextFlow();
		infoSection.setLayoutX(670);
		infoSection.setLayoutY(130);
		
		queryingOptions = new VBox(20);
		queryingOptions.setLayoutX(50);
		queryingOptions.setLayoutY(130);
		
		attributeInfo = new ArrayList<>();
		storeAttributeInformation();
		
		resultsQuery = new String[6][2];
		
		connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
	}
	
	@Override
	public void start(Stage primaryStage){
		firstStartingScreen();
		
		primaryStage.setTitle("Test");
		primaryStage.setScene(scene);
		primaryStage.show();
		
		primaryStage.setMaxHeight(920);
		primaryStage.setMinHeight(920);
		primaryStage.setMaxWidth(1210);
		primaryStage.setMaxWidth(1210);
		
		scene.setOnMouseClicked(e->{
			System.out.println("X: " + e.getX() + ", Y: " + e.getY());
		});
	}

	public void firstStartingScreen(){
		firstStartingScreenTimeline();
		timeline.play();
	}
	
	public void firstStartingScreenTimeline(){
		Text firstStartingScreenText = new Text(300, 500, "Hello");
		firstStartingScreenText.setFont(Font.font(null, FontWeight.BOLD, 250));
		firstStartingScreenText.setOpacity(0);
		
		TextFlow secondStartingScreenText = new TextFlow();
		secondStartingScreenText.getChildren().addAll(new Text("Welcome\n"), new Text("   to the phone\n      database"));
		((Text)secondStartingScreenText.getChildren().get(0)).setFont(Font.font(null, FontWeight.BOLD, 250));
		((Text)secondStartingScreenText.getChildren().get(1)).setFont(Font.font(null, FontWeight.BOLD, 150));
		
		secondStartingScreenText.setLayoutX(45);
		secondStartingScreenText.setLayoutY(50);
		secondStartingScreenText.setOpacity(0);
		
		EventHandler<ActionEvent> sayHello = new EventHandler<ActionEvent>(){
			String currentText = "First/FadeIn";
			int delay = 0;
			
			@Override
			public void handle(ActionEvent e) {
				if(currentText.equals("First/FadeIn")){
					firstStartingScreenText.setOpacity(firstStartingScreenText.getOpacity() + 0.004);
					if(firstStartingScreenText.getOpacity() >= 1){
						currentText = "First/Delay";
					}
				}
				else if(currentText.equals("First/Delay")){
					delay++;
					if(delay == 60){
						currentText = "First/FadeAway";
						delay = 0;
					}
				}
				else if(currentText.equals("First/FadeAway")){
					firstStartingScreenText.setOpacity(firstStartingScreenText.getOpacity() - 0.004);
					if(firstStartingScreenText.getOpacity() <= .2){
						currentText = "Second/FadeIn";
					}
				}
				else if(currentText.equals("Second/FadeIn")){
					firstStartingScreenText.setOpacity(firstStartingScreenText.getOpacity() - 0.004);
					secondStartingScreenText.setOpacity(secondStartingScreenText.getOpacity() + 0.004);
					if(secondStartingScreenText.getOpacity() >= 1){
						currentText = "Second/Delay";
					}
				}
				else if(currentText.equals("Second/Delay")){
					delay++;
					if(delay == 60){
						currentText = "Second/FadeAway";
					}
				}
				else if(currentText.equals("Second/FadeAway")){
					secondStartingScreenText.setOpacity(secondStartingScreenText.getOpacity() - 0.004);
					if(secondStartingScreenText.getOpacity() <= 0){
						pane.getChildren().clear();
						queryingScreen();
						timeline.stop();
					}
				}
			}
			
		};
		
		pane.getChildren().addAll(firstStartingScreenText, secondStartingScreenText);
		timeline = new Timeline(new KeyFrame(Duration.millis(1000/60.0), sayHello));
		timeline.setCycleCount(Timeline.INDEFINITE);
	}
	
	public void queryingScreen(){
		Button getResults = new Button("Find Me A Phone");
		getResults.setLayoutX(535);
		getResults.setLayoutY(760);
		
		pane.getChildren().addAll(getResults, queryingOptions, infoSection);
		
		
		ObservableList<String> tableList = FXCollections.observableArrayList();
		tableList.addAll("Phone", "Carrier", "Frame", "Platform", "Battery", "Color", "Memory", 
				"InternalStorage", "Camera", "Video", "Display", "Launch");
		FXCollections.sort(tableList);
		
		ComboBox<String> chooseTable = new ComboBox<>(tableList);
		
		
		VBox tableSelectionAndTitle = new VBox(15);
		
		Text tableSelectionTitle = new Text("Add options to search for:");
		HBox tableSelection = new HBox(10);
		tableSelection.getChildren().add(chooseTable);
		
		
		tableSelectionAndTitle.getChildren().addAll(tableSelectionTitle, tableSelection);
		queryingOptions.getChildren().add(tableSelectionAndTitle);
		
		chooseTable.setOnAction(e->{
			try {
				if(tableSelection.getChildren().size() > 1){
					tableSelection.getChildren().remove(2);
					tableSelection.getChildren().remove(1);
				}
				
				ResultSetMetaData resmd = executeQuery(chooseTable.getValue(), null).getMetaData();
				
				ObservableList<String> chooseFrom = FXCollections.observableArrayList();
				for(int i = 1; i <= resmd.getColumnCount(); i++){
					if(!resmd.getColumnName(i).equals("PhoneName") && !resmd.getColumnName(i).contains("ID")){
						chooseFrom.add(resmd.getColumnName(i));
					}
				}
				FXCollections.sort(chooseFrom);
				
				ComboBox<String> secondaryBox = new ComboBox<>(chooseFrom);
				
				Button addToQueryList = new Button("Add");
				addToQueryList.setOnAction(e1->{
					if(queryingOptions.getChildren().size() > 1){
						for(int i = 1; i < queryingOptions.getChildren().size(); i++){
							if(((Text)((TextFlow)((VBox) queryingOptions.getChildren().get(i)).getChildren(
									).get(0)).getChildren().get(0)).getText().contains(
									secondaryBox.getValue())){
								 return;
							}
						}
					}
					
					if(secondaryBox.getValue() != null && queryingOptions.getChildren().size() < 7){
						addToQuery(chooseTable.getValue(), secondaryBox.getValue());
					}
				});
				
				tableSelection.getChildren().addAll(secondaryBox, addToQueryList);
				
				stmt.clearBatch();
			} catch (SQLException e1) {
				System.out.println("Something happened... Not good though.");
			}
		});
		
		getResults.setOnAction(e->{
/*			for(int i = 1; i < queryingOptions.getChildren().size(); i++){
				if(((ComboBox<String>)((HBox)((VBox)queryingOptions.getChildren().get(i)).getChildren().get(1)).getChildren().get(0)).getValue() == null){
					return;
				}
			}
*/			
			if(queryingOptions.getChildren().size() > 1){
				try {
					pane.getChildren().clear();
					displayResultsScreen();
					infoSection.getChildren().clear();
					queryingOptions.getChildren().clear();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}			
			}
		});
	}
	
	public void addToQuery(String tableName, String attribute) {		
		try {			
			VBox attributeSelectionAndTitle = new VBox(15);
			
			TextFlow attributeSelectionTitle = new TextFlow();
			attributeSelectionTitle.getChildren().addAll(new Text(attribute + " of a phone "),
					new Text(tableName + "," + attribute));
			attributeSelectionTitle.getChildren().get(1).setOpacity(0);
			
			HBox attributeSelection = new HBox(10);
			
			
			ResultSet res = executeQuery(tableName, attribute);
			ObservableList<String> attributeList = FXCollections.observableArrayList();
			while(res.next()){
				if(res.getString(attribute) != null && !attributeList.contains(res.getString(attribute))
						&& !res.getString(attribute).contains("NULL")){
					if(attribute.equals("Price")){
						if((res.getString(attribute).indexOf(".")) == (res.getString(attribute).length() - 2)){
								attributeList.add("$" + res.getString(attribute) + "0");
						}
						else{
							attributeList.add("$" + res.getString(attribute));
						}
					}
					else{
						attributeList.add(res.getString(attribute));
					}
				}
			}
			FXCollections.sort(attributeList);
			
			ComboBox<String> allPossibleAttributes = new ComboBox<>(attributeList);
			
			ImageView showAttributeInfo = new ImageView(new Image("Transparent_QuestionMark.png"));
			
			Button remove = new Button("Remove Option");
			
			
			attributeSelection.getChildren().addAll(allPossibleAttributes, showAttributeInfo, remove);
			attributeSelectionAndTitle.getChildren().addAll(attributeSelectionTitle, attributeSelection);
			queryingOptions.getChildren().add(attributeSelectionAndTitle);
			
			
			remove.setOnAction(e->{
				queryingOptions.getChildren().remove(attributeSelectionAndTitle);
			});
			
			showAttributeInfo.setOnMouseClicked(e->{
				if(attributeInfo.contains("New Attribute Information - " + tableName + ": " + attribute)){
					infoSection.getChildren().clear();
					
					Text title = new Text(attribute + "\n");
					title.setFont(Font.font("", FontWeight.BOLD, 30));
					title.setUnderline(true);
					
					String attributeText = "";
					for(int i = attributeInfo.indexOf("New Attribute Information - " + tableName + ": " + attribute) + 1; 
							(i < attributeInfo.size()) && !(attributeInfo.get(i).contains("New Attribute Information")); 
							i++){
						attributeText = attributeText + attributeInfo.get(i) + "\n";
					}
					
					infoSection.getChildren().addAll(title, new Text(attributeText));
				}
				else{
					System.out.println("No");
				}
			});
		} catch (SQLException e1) {
			System.out.println("Something went wrong...");
		}
	}
	
	public void storeAttributeInformation(){
		try {
			Scanner getAttributeInformation = new Scanner(new File("AttributeInformation.txt"));
			
			while(getAttributeInformation.hasNextLine()){
				attributeInfo.add(getAttributeInformation.nextLine());
			}
			
			getAttributeInformation.close();
		} catch (FileNotFoundException e) {
			System.out.println("Something went wrong...");
		}
	}
	
	public ResultSet executeQuery(String tableName, String attribute) throws SQLException{
		if(attribute == null){
			attribute = "*";
		}
		
		String queryThis;
		if(tableName.equals("InternalStorage")){
			queryThis = "SELECT " + attribute + " FROM Memory_InternalStorage";
		}
		else if(tableName.equals("Launch")){
			queryThis = "SELECT " + attribute + " FROM LaunchInformation";
		}
		else if(tableName.equals("Video")){
			queryThis = "SELECT " + attribute + " FROM Camera_Video";
		}
		else if(tableName.equals("Color")){
			queryThis = "SELECT " + attribute + " FROM Frame_Color";
		}
		else{
			queryThis = "SELECT " + attribute + " FROM " + tableName;
		}
		
		stmt = connection.prepareStatement(queryThis);
		ResultSet res = stmt.executeQuery();
		
		stmt.clearBatch();
		
		return res;
	}
	
	public ResultSet getResults() throws SQLException{
		String select = "SELECT Phone.PhoneName, ";
		String from = "FROM Phone";
		String where = "WHERE ";
		
		for(int i = 1; i < queryingOptions.getChildren().size(); i++){
			String tableAndAtt = ((Text)((TextFlow)((VBox)queryingOptions.getChildren().get(
					i)).getChildren().get(0)).getChildren().get(1)).getText();
			
			if(tableAndAtt.substring(0, tableAndAtt.indexOf(",")).contains("Carrier")){
				select = select + "CarrierSellsPhones";
				
				if(!from.contains("CarrierSellsPhones")){
					from = from + " INNER JOIN CarrierSellsPhones ON CarrierSellsPhones.PhoneName = Phone.PhoneName";
				}
			}
			else{
				if(tableAndAtt.substring(0, tableAndAtt.indexOf(",")).contains("Color")){
					select = select + "Frame_Color";
					
					if(!from.contains(" Frame ")){
						from = from + " INNER JOIN Frame ON Frame.PhoneName = Phone.PhoneName";
					}
					
					if(!from.contains("Frame_Color")){
						from = from + " INNER JOIN Frame_Color ON Frame_Color.FrameID = Frame.FrameID";
					}
				}
				else if(tableAndAtt.substring(0, tableAndAtt.indexOf(",")).contains("Video")){
					select = select + "Camera_Video";
					
					if(!from.contains(" Camera ")){
						from = from + " INNER JOIN Camera ON Camera.PhoneName = Phone.PhoneName";
					}
					
					if(!from.contains("Camera_Video")){
						from = from + " INNER JOIN Camera_Video ON Camera_Video.CameraID"
								+ " = Camera.CameraID";
					}
				}
				else if(tableAndAtt.substring(0, tableAndAtt.indexOf(",")).contains("InternalStorage")){
					select = select + "Memory_InternalStorage";
					
					if(!from.contains(" Memory ")){
						from = from + " INNER JOIN Memory ON Memory.PhoneName = Phone.PhoneName";
					}
					
					if(!from.contains("Memory_InternalStorage")){
						from = from + " INNER JOIN Memory_InternalStorage ON Memory_InternalStorage.MemoryID"
								+ " = Memory.MemoryID";
					}
				}
				else if(tableAndAtt.substring(0, tableAndAtt.indexOf(",")).contains("Launch")){
					select = select + "LaunchInformation";
					
					if(!from.contains("LaunchInformation")){
						from = from + " INNER JOIN LaunchInformation ON LaunchInformation.PhoneName = Phone.PhoneName";
					}
				}
				else{
					select = select + tableAndAtt.substring(0, tableAndAtt.indexOf(","));
					
					if(!from.contains(tableAndAtt.substring(0, tableAndAtt.indexOf(",")))){
						from = from + " INNER JOIN " + tableAndAtt.substring(0, tableAndAtt.indexOf(",")) 
							+ " ON " + tableAndAtt.substring(0, tableAndAtt.indexOf(",")) 
							+ ".PhoneName = Phone.PhoneName";
					}
				}
			}
			
			select = select + "." + tableAndAtt.substring(tableAndAtt.indexOf(",") + 1);
			where = where + tableAndAtt.substring(tableAndAtt.indexOf(",") + 1) + " = '" 
					+ ((ComboBox<String>)((HBox)((VBox)queryingOptions.getChildren().get(
					i)).getChildren().get(1)).getChildren().get(0)).getValue() + "'";
			
			if((i + 1) < queryingOptions.getChildren().size()){
				select = select + ", ";
				where = where + " AND ";
			}
		}
		
		if(select.lastIndexOf(",") != -1 && select.substring(select.lastIndexOf(",")).equals(", ")){
			select = select.substring(0, select.lastIndexOf(","));
		}
		
		if(where.lastIndexOf("AND ") != -1 && where.substring(where.lastIndexOf("AND ")).equals("AND ")){
			where = where.substring(0, where.lastIndexOf("AND "));
		}
		
		String queryThis = select + " " + from;
		if(!where.equals("WHERE ")){
			queryThis = queryThis + " " + where;
		}
		
		System.out.println(queryThis);
		
		stmt = connection.prepareStatement(queryThis);
		ResultSet res = stmt.executeQuery();
		
		stmt.clearBatch();
		
		return res;
	}
	
	public void displayResultsScreen() throws SQLException{
		ResultSet res = getResults();
		
		Button previous = new Button("<");
		previous.setLayoutX(563);
		previous.setLayoutY(720);
		
		Button next = new Button(">");
		next.setLayoutX(600);
		next.setLayoutY(720);
		
		
		Button newQuery = new Button("New Search");
		newQuery.setLayoutX(550);
		newQuery.setLayoutY(760);
		
		pane.getChildren().addAll(previous, next, newQuery);
	}
	
	public static void main(String args[]){
		launch(args);
	}
}
