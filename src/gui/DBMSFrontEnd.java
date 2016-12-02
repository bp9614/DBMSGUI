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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class DBMSFrontEnd extends Application{
	private Scene scene;
	private Pane pane;
	private PreparedStatement stmt;
	private final Connection connection;
	private VBox queryingOptions;
	
	private static final String DB_PATH = DBMSFrontEnd.class.getResource("PhoneDatabase.sqlite").toString();
	
	public DBMSFrontEnd() throws SQLException{
		pane = new Pane();
		scene = new Scene(pane, 1200, 900);
		queryingOptions = new VBox(15);
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
		HBox chooseATable = new HBox(10);
		chooseATable.setLayoutX(380);
		chooseATable.setLayoutY(730);
		
		ObservableList<String> tableSelection = FXCollections.observableArrayList();
		tableSelection.addAll("Phone", "Carrier", "Frame", "Platform", "Battery", "Color", "Memory", 
				"Internal Storage", "Camera", "Video", "Display", "Launch Time");
		
		
		ComboBox<String> chooseQueries = new ComboBox<>(tableSelection);
		
		chooseATable.getChildren().add(chooseQueries);
		pane.getChildren().add(chooseATable);
		
		chooseQueries.setOnAction(e->{
			try {
				if(chooseATable.getChildren().size() > 1){
					chooseATable.getChildren().remove(2);
					chooseATable.getChildren().remove(1);
				}
				
				
				String queryThis;
				if(chooseQueries.getValue().equals("Internal Storage")){
					queryThis = "SELECT * FROM Memory_InternalStorage";
				}
				else if(chooseQueries.getValue().equals("Launch Time")){
					queryThis = "SELECT * FROM LaunchInformation";
				}
				else if(chooseQueries.getValue().equals("Video")){
					queryThis = "SELECT * FROM Camera_Video";
				}
				else if(chooseQueries.getValue().equals("Color")){
					queryThis = "SELECT * FROM Frame_Color";
				}
				else{
					queryThis = "SELECT * FROM " + chooseQueries.getValue();
				}
				
				stmt = connection.prepareStatement(queryThis);
				ResultSet res = stmt.executeQuery();
				ResultSetMetaData resmd = res.getMetaData();
				
				ObservableList<String> chooseFrom = FXCollections.observableArrayList();
				for(int i = 1; i <= resmd.getColumnCount(); i++){
					if(!resmd.getColumnName(i).equals("PhoneName") && !resmd.getColumnName(i).contains("ID")){
						chooseFrom.add(resmd.getColumnName(i));
					}
				}
				
				ComboBox<String> secondaryBox = new ComboBox<>(chooseFrom);
				
				Button addToQueryList = new Button("Add");
				addToQueryList.setOnAction(event->{
					if(addToQueryList != null){
						chooseATable.getChildren().remove(2);
						chooseATable.getChildren().remove(1);
					}
				});
				
				chooseATable.getChildren().addAll(secondaryBox, addToQueryList);
				
				stmt.clearBatch();
			} catch (SQLException e1) {
				System.out.println("Something happened... Not good though.");
			}
		});

	}
	
	public static void main(String args[]){
		launch(args);
	}
}
