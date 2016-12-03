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

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class DBMSFrontEnd extends Application{
	private Scene scene;
	private Pane pane;
	private PreparedStatement stmt;
	private final Connection connection;
	private VBox queryingOptions;
	private TextFlow infoSection;
	private ArrayList<String> attributeInfo;
	
	private static final String DB_PATH = DBMSFrontEnd.class.getResource("PhoneDatabase.sqlite").toString();
	
	public DBMSFrontEnd() throws SQLException{
		pane = new Pane();
		scene = new Scene(pane, 1200, 900);
		
		infoSection = new TextFlow();
		infoSection.setLayoutX(670);
		infoSection.setLayoutY(130);
		
		queryingOptions = new VBox(20);
		queryingOptions.setLayoutX(100);
		queryingOptions.setLayoutY(130);
		
		attributeInfo = new ArrayList<>();
		storeAttributeInformation();
		
		connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
	}
	
	@Override
	public void start(Stage primaryStage){
		queryingScreen();
		
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

	public void queryingScreen(){
		Button getResults = new Button("Find Me A Phone");
		getResults.setLayoutX(535);
		getResults.setLayoutY(760);
		
		pane.getChildren().addAll(getResults, queryingOptions, infoSection);
		
		
		ObservableList<String> tableList = FXCollections.observableArrayList();
		tableList.addAll("Phone", "Carrier", "Frame", "Platform", "Battery", "Color", "Memory", 
				"Internal Storage", "Camera", "Video", "Display", "Launch Time");
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
				addToQueryList.setOnAction(event->{
					if(secondaryBox.getValue() != null){
						addToQuery(chooseTable.getValue(), secondaryBox.getValue());
						tableSelection.getChildren().remove(2);
						tableSelection.getChildren().remove(1);
					}
				});
				
				tableSelection.getChildren().addAll(secondaryBox, addToQueryList);
				
				stmt.clearBatch();
			} catch (SQLException e1) {
				System.out.println("Something happened... Not good though.");
			}
		});
		
		getResults.setOnAction(e->{
			infoSection.getChildren().clear();
			queryingOptions.getChildren().clear();
			pane.getChildren().clear();
		});
	}
	
	public void addToQuery(String tableName, String attribute) {		
		try {
			VBox attributeSelectionAndTitle = new VBox(15);
			
			Text attributeSelectionTitle = new Text(attribute + " of a phone");
			HBox attributeSelection = new HBox(10);
			
			
			ResultSet res = executeQuery(tableName, attribute);
			ObservableList<String> attributeList = FXCollections.observableArrayList();
			while(res.next()){
				if(res.getString(attribute) != null && !attributeList.contains(res.getString(attribute))){
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
			
			ComboBox allPossibleAttributes = new ComboBox(attributeList);
			
			ImageView showAttributeInfo = new ImageView(new Image("Transparent_QuestionMark.png"));
			
			Button remove = new Button("Remove Search Option");
			
			
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
		if(tableName.equals("Internal Storage")){
			queryThis = "SELECT " + attribute + " FROM Memory_InternalStorage";
		}
		else if(tableName.equals("Launch Time")){
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
	
	public static void main(String args[]){
		launch(args);
	}
}
