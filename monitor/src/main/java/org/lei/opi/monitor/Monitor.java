package org.lei.opi.monitor;

import org.lei.opi.core.OpiMachine;

import java.util.List;
import java.util.ArrayList;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.beans.value.ObservableValue;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class Monitor extends Application {


    @FXML
    private Button btnConnect;

    @FXML
    private TextField fieldMyIP;

    @FXML
    private TextField fieldMyPort;

    @FXML
    private ListView<String> listMachines;

    @FXML
    private AnchorPane splashScreen;

    @FXML
    private TableView<List<StringProperty>> tableSettings;

    @FXML
    private TableColumn<List<StringProperty>, String> colSettingsProperty;

    @FXML
    private TableColumn<List<StringProperty>, String> colSettingsValue;

    // used as data for tableSettings 
    private ObservableList<List<StringProperty>> settingsData = FXCollections.observableArrayList();

    private void fillSettingsData(String machineName) {
        this.settingsData.removeAll(this.settingsData);
        if (machineName == null) 
            return;

        Object settings = null;
        try {
            @SuppressWarnings("unchecked")
            Class<OpiMachine> cls = (Class<OpiMachine>)Class.forName("org.lei.opi.core." + machineName);
            Constructor<?> cons = cls.getConstructor(boolean.class);
            OpiMachine opiM = (OpiMachine)cons.newInstance(false);
            settings = opiM.getSettings();
        } catch (ClassNotFoundException e) {
            System.out.println("listMachines contains a name that is not an extension of OpiMachine");
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException |
                 InstantiationException | InvocationTargetException e) {
            System.out.println("Attepting to construct an object of " + machineName + " but it does not have a (boolean) constructor.");
        }

        if (settings == null) 
            return;

        Field[] fields = settings.getClass().getFields(); 
        for (Field field : fields) {
            List<StringProperty> line = new ArrayList<StringProperty>();
    
            line.add(new SimpleStringProperty(field.getName()));
            line.add(new SimpleStringProperty(field.toString()));
    
            this.settingsData.add(line);
        }
    }

    /**
     * Fill in bits of the GUI
     * 
     * 1) Attach data {@link settingsData} to {@link tableSettings} to allow updates of table.
     * 2) Read the names of machines from OpiMachine.MACHINES and put them in {@link listMachines}.
     *    When one is clicked, update {@link settingsData}.
     */
    @FXML
    public void initialize() {
            // (1) Set up table columns to show machine settings
            // data is a list (cols) of list of strings (row), with first being field name in Settings and second the value
        colSettingsProperty.setCellValueFactory(cellData -> cellData.getValue().get(0));
        colSettingsValue.setCellValueFactory(cellData -> cellData.getValue().get(1));
        tableSettings.setItems(this.settingsData);

            // (2) Set up list of machines
        ObservableList<String> olMachineList =  FXCollections.observableArrayList(OpiMachine.MACHINES);
        FXCollections.sort(olMachineList);
        listMachines.setItems(olMachineList);
        listMachines.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println("Changed list");
                fillSettingsData(newValue);
            }
        });
        listMachines.getSelectionModel().select(0);
    }

    /**
     * Launch the main window, etc
     */
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Monitor.fxml"));
    
        Scene scene = new Scene(root, 800, 515);
    
        stage.setTitle("OPI Monitor (v3.0 2022)");
        stage.setScene(scene);

        stage.show();
    }

    /**
     * Prompt to save any settings that may have changed.
     */
    @Override
    public void stop() throws Exception {
        // TODO prompt to save settings file
        System.out.println("Stopping");
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(); 
    }
}