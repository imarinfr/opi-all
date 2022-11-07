package org.lei.opi.monitor;

import org.lei.opi.core.OpiMachine;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.beans.value.ObservableValue;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;

public class Monitor extends Application {
    @FXML
    private Button btnConnect;

    @FXML
    private Button btnSave;

    @FXML
    private TextField fieldMyIP;

    @FXML
    private TextField fieldMyPort;

    @FXML
    private Label labelMessages;

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

    private boolean settingsHaveBeenEdited; // true if settings have been edited since last change. 
    private String currentMachineChoice;
    private Object currentSettings;  // an OpiMachine$Settings object

    /**
     * First checks if settings for current selection have been changed and 
     * it is OK to junk them with an Alert dialog.
     * If OK to proceed, gets the Settings from an instance of the class machineName.
     * 
     * @param machineName Name of the OpiMachine class from which to get Settings.
     */
    private void fillSettingsData(String machineName) {
            // If things have been edited (maybe not changed!), check OK to proceed.
        boolean discard = true;
        if (this.settingsHaveBeenEdited) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Settings check");
            String s = "The settings for " + currentMachineChoice + " have changed. Discard the changes?";
            alert.setContentText(s);
             
            discard = alert.showAndWait()
                .map(response -> response == ButtonType.OK)
                .orElse(false);
        }
        if (!discard) return;

            // Remove all current data, create an instance of "org.lei.opi.core." + machineName        
        this.settingsData.removeAll(this.settingsData);
        if (machineName == null) 
            return;

        this.currentSettings = null;
        try {
            @SuppressWarnings("unchecked")
            Class<OpiMachine> cls = (Class<OpiMachine>)Class.forName("org.lei.opi.core." + machineName);
            Constructor<?> cons = cls.getConstructor(boolean.class);
            OpiMachine opiM = (OpiMachine)cons.newInstance(false);
            this.currentSettings = opiM.getSettings();
        } catch (ClassNotFoundException e) {
            System.out.println("listMachines contains a name that is not an extension of OpiMachine");
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException |
                 InstantiationException | InvocationTargetException e) {
            System.out.println("Attepting to construct an object of " + machineName + " but it does not have a (boolean) constructor.");
        }

        if (this.currentSettings == null) 
            return;

            // Now populate this.settingsData with the fields and values
        Field[] fields = this.currentSettings.getClass().getFields(); 
        for (Field field : fields) {
            List<StringProperty> line = new ArrayList<StringProperty>();
    
            field.setAccessible(true);
            line.add(new SimpleStringProperty(field.getName()));
            try {
                line.add(new SimpleStringProperty(field.get(this.currentSettings) .toString()));
            } catch (IllegalAccessException e) {
                line.add(new SimpleStringProperty("Unknown"));
            }
    
            this.settingsData.add(line);
        }

        this.settingsHaveBeenEdited = false;
        this.currentMachineChoice = machineName;
    }

    /**
     * Get all fields in this and superclass objects.
     * Taken from dfa's amnswer to https://stackoverflow.com/questions/1042798/retrieving-the-inherited-attribute-names-values-using-java-reflection
     * @param fields
     * @param type
     * @return List of all fields in supplied object and its superclasses. 
     */
    public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));
    
        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }
    
        return fields;
    }

    /**
     * When Sace Settings button {@link btnSave} is pressed, do this.
     * (1) Read whole settings JSON into HashMap keyed by machine name
     * (2) Build a JSON string representing map[this.currentMachineChoice] with values in this.settingsData
     * (3) Convert Json string to object and put it in map.
     * (4) Write map it to file as JSON.
     * (5) Set this.settingsHaveBeenEdited to false
     *
     * @param event
     */
    @FXML
    void actionBtnSave(ActionEvent event) {
        if (!this.settingsHaveBeenEdited) {
            labelMessages.setText("Nothing to save for " + this.currentMachineChoice);
            return;
        }

        labelMessages.setText("Saving settings for " + this.currentMachineChoice + "...");

        HashMap<String, Object> map = OpiMachine.readSettingsFile();

        List<Field> allFields = Monitor.getAllFields(new ArrayList<Field>(), this.currentSettings.getClass());

        String jsonString = "{" + 
            this.settingsData.stream()
                .map((List<StringProperty> row) -> {
                    String fieldName = row.get(0).getValue();
                    String fieldValue = row.get(1).getValue();
                    String result = "";
                    Field f = allFields.stream()
                        .filter((Field ff) -> ff.getName().equals(fieldName))
                        .findAny()
                        .orElse(null);

                    if (f == null)
                        System.out.println("That's wierd, cannot find " + fieldName + " in field list for " + this.currentMachineChoice);
                    else {
                        if (f.getType() != String.class)
                            result = String.format("\"%s\" : %s ", fieldName, fieldValue);
                        else
                            result = String.format("\"%s\" : \"%s\" ", fieldName, fieldValue);
                    }
                    return result;
                })
                .collect(Collectors.joining(","))
            + "}";

        Gson gson = new Gson();
        map.put(this.currentMachineChoice, gson.fromJson(jsonString, this.currentSettings.getClass()));

        OpiMachine.writeSettingsFile(map);
        this.settingsHaveBeenEdited = false;

        labelMessages.setText("Settings saved for " + this.currentMachineChoice);
    }

    /**
     * Fill in bits of the GUI
     * 
     * 1) Attach data {@link settingsData} to {@link tableSettings} to allow updates of table
     *    when list changes.
     * 2) Set up column Value to be editable.
     * 3) Read the names of machines from OpiMachine.MACHINES and put them in {@link listMachines}.
     *    When one is clicked, update {@link settingsData}.
     */
    @FXML
    public void initialize() {
            // (1) Set up table columns to show machine settings
            // cellData is a list (cols) of list of strings (row), 
            // with first row element being field name in Settings and second the value
        colSettingsProperty.setCellValueFactory(cellData -> cellData.getValue().get(0));
        colSettingsValue.setCellValueFactory(cellData -> cellData.getValue().get(1));

            // (2) Make Value column editable
        colSettingsValue.setCellFactory(TextFieldTableCell.forTableColumn());
        colSettingsValue.setOnEditStart(e -> {
            labelMessages.setText("Don't forget to press Enter to save edit.");
        });
        colSettingsValue.setOnEditCommit(e -> {
            e.getRowValue().set(1 ,new SimpleStringProperty(e.getNewValue()));
            this.settingsHaveBeenEdited = true;
            labelMessages.setText("");
        });

        tableSettings.setItems(this.settingsData);
        this.settingsHaveBeenEdited = false;

            // (3) Set up list of machines
        ObservableList<String> olMachineList =  FXCollections.observableArrayList(OpiMachine.MACHINES);
        FXCollections.sort(olMachineList);
        listMachines.setItems(olMachineList);
        listMachines.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                fillSettingsData(newValue);
            }
        });
        listMachines.getSelectionModel().select(0);
    }

    /**
     * Action when Connect button is pressed.
     *
     * (1) Open a CSWriter to the machine slected in {@link listMacines}
     * (2) If succesful, switch to the Scene of the {@link OpiMachine} we have switched to.
     *
     * @param event
     */
    @FXML
    void actionBtnConnect(ActionEvent event) {
        labelMessages.setText("Trying to open connection to " + this.currentMachineChoice);

        final Node source = (Node) event.getSource();
        final Stage stage = (Stage) source.getScene().getWindow();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/Display.fxml"));
            loader.setController(new org.lei.opi.core.Display(source.getScene()));    // TODO: noSocket controller for testing. Need to add back controller into FXML
            Parent root = loader.load();
            Scene scene = new Scene(root, 800, 515);

            stage.setScene(scene);
            stage.show();
        } catch (javafx.fxml.LoadException e) {
            labelMessages.setText("Cannot open connection to " + this.currentMachineChoice);
            System.out.println(e);
        } catch (IOException e) {
            System.out.println("Couldn't open Temp.fxml");
            e.printStackTrace();
        }
    }

    /**
     * Launch the main window, etc
     */
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("resources/Monitor.fxml"));
    
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