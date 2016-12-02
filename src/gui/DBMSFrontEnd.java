package gui;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class DBMSFrontEnd extends Application{
	private Scene scene;
	private Pane pane;
	private HBox allDropDownMenus;
	private final Connection connection;
	
	private static final String DB_PATH = DBMSFrontEnd.class.getResource("PhoneDatabase.sqlite").toString();
	
	public DBMSFrontEnd() throws SQLException{
		pane = new Pane();
		scene = new Scene(pane, 1200, 900);
		allDropDownMenus = new HBox(15);
		connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
	}
	
	@Override
	public void start(Stage primaryStage) throws SQLException{
/*		ObservableList<String> phones = FXCollections.observableArrayList();
		phones.add("Fuck");
		phones.add("Hello");
		
		ComboBox<String> comboBox = new ComboBox<>(phones);
		
		allDropDownMenus.getChildren().add(comboBox);
		allDropDownMenus.setAlignment(Pos.CENTER);
		pane.setCenter(allDropDownMenus);
		
		comboBox.setOnAction(e->{
			allDropDownMenus.getChildren().add(createANewDropDownMenu());
		});
*/

		final String sql = "SELECT Phone.PhoneName as phone_name FROM Phone";
		
		final PreparedStatement stmt = connection.prepareStatement(sql);
		final ResultSet res = stmt.executeQuery();
		
		while (res.next()){
			System.out.println(res.getString("phone_name"));
		}
		
		primaryStage.setTitle("Test");
		primaryStage.setScene(scene);
		primaryStage.show();
		
		primaryStage.setMaxHeight(920);
		primaryStage.setMinHeight(920);
		primaryStage.setMaxWidth(1210);
		primaryStage.setMaxWidth(1210);
	}
/*
	public ComboBox<String> createANewDropDownMenu(){
		ObservableList<String> newList = FXCollections.observableArrayList();
		newList.add("Fuck");
		newList.add("Hello");
		
		ComboBox<String> newDropDownMenu = new ComboBox<>(newList);
		
		newDropDownMenu.setOnAction(e->{
			allDropDownMenus.getChildren().add(createANewDropDownMenu());
		});
		
		return newDropDownMenu;
	}
*/
	public static void main(String args[]){
		launch(args);
	}
}
