package gui;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class DBMSFrontEnd extends Application{
	private Scene scene;
	private Pane pane;
	private PreparedStatement stmt;
	private final Connection connection;
	private VBox queryingOptions;
	private Text infoSection;
	
	private static final String DB_PATH = DBMSFrontEnd.class.getResource("PhoneDatabase.sqlite").toString();
	
	public DBMSFrontEnd() throws SQLException{
		pane = new Pane();
		scene = new Scene(pane, 1200, 900);
		
		infoSection = new Text(670, 130, "");
		
		queryingOptions = new VBox(20);
		queryingOptions.setLayoutX(130);
		queryingOptions.setLayoutY(130);
		
		connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
	}
	
	@Override
	public void start(Stage primaryStage) throws SQLException{
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
		
		pane.getChildren().addAll(getResults, queryingOptions);
		
		
		ObservableList<String> tableList = FXCollections.observableArrayList();
		tableList.addAll("Phone", "Carrier", "Frame", "Platform", "Battery", "Color", "Memory", 
				"Internal Storage", "Camera", "Video", "Display", "Launch Time");
		
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
	}
	
	public void addToQuery(String tableName, String attribute){
		VBox attributeSelectionAndTitle = new VBox(15);
		
		Text attributeSelectionTitle = new Text(attribute + "of a" + tableName);
		HBox attributeSelection = new HBox(10);
		
		
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
