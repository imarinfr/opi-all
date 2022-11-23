package org.lei.opi.monitor;

import org.lei.opi.core.CSListener;
import org.lei.opi.core.OpiMachine;
import org.lei.opi.core.OpiManager;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.Optional;

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
import javafx.scene.control.ButtonBar;
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
    private Button btnResetSettings;

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

        // Used as data for tableSettings (settings for {@link currentMachineChoice}).
        // Initially try to get this from the settings file.
        // If the settings file doesn't exist, or the Reset Settings button is 
        // pressed, then get them from the @Paramter annotations in OpiMachine classes.
    private ObservableList<List<StringProperty>> settingsList = FXCollections.observableArrayList();

        // IP and port of the monitor (myself)
    private String myIpAddress;
    private String myPort;
    private ListenerThread myListenerThread;

    private boolean settingsHaveBeenEdited; // true if settings have been edited since last change. 
    private boolean myIpOrPortHaveBeenEdited; // true if myIp or myPort have been edited since last change. 
    private String currentMachineChoice;
    private Object currentSettingsObject;  // an OpiMachine$Settings object

    /**
     * First checks if settings for current selection have been changed and 
     * it is OK to junk them with an Alert dialog.
     * If OK to proceed
     *     If getFromFile, try and get the Settings for machineName from the settings file
     *     If !getFromFile or get from faile fails, get settings from an instance of the class machineName.
     * 
     * @param machineName Name of the OpiMachine class from which to get Settings.
     * @param getFromFile If True, try and get settings from JSON settings file.
     */
    private void fillSettingsData(String machineName, boolean getFromFile) {
        checkSave();

            // Remove all current data
        this.settingsList.removeAll(this.settingsList);
        if (machineName == null) // not quite sure why this is here, but too scared to remove it.
            return;

            // Create an instance of "org.lei.opi.core." + machineName 
            // (might need it later for Save and also checks OpiMachine 
            // subclass actually exists if someone has edited the settings file externally.)
        this.currentSettingsObject = null;
        try {
            @SuppressWarnings("unchecked")
            Class<OpiMachine> cls = (Class<OpiMachine>)Class.forName("org.lei.opi.core." + machineName);
            Constructor<?> cons = cls.getConstructor(boolean.class);
            OpiMachine opiM = (OpiMachine)cons.newInstance(false);
            this.currentSettingsObject = opiM.getSettings();
        } catch (ClassNotFoundException e) {
            System.out.println("Selected machine is not in the jar as a class that is an extension of OpiMachine");
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException |
                 InstantiationException | InvocationTargetException e) {
            System.out.println("Attepting to construct an object of " + machineName + " but it does not have a (boolean) constructor.");
        }

        if (this.currentSettingsObject == null) 
            return;

        boolean gotThem = false;
        if (getFromFile) {
            Gson gson = new Gson();
            HashMap<String, Object> map = OpiMachine.readSettingsFile();
            if (map.containsKey(machineName)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> params = (Map<String, Object>)map.get(machineName);
                for (String p  : params.keySet()) {
                    List<StringProperty> line = new ArrayList<StringProperty>();
                    line.add(new SimpleStringProperty(p));
                    line.add(new SimpleStringProperty(params.get(p).toString()));
                    this.settingsList.add(line);
                }
                gotThem = true;
            } else {
                System.out.println("Could not find " + machineName + " in settings file, resetting to defauts.");
            }
        }

        this.settingsHaveBeenEdited = false;
        
        if (!gotThem) {
                // Populate this.settingsList with the fields and values
            Field[] fields = this.currentSettingsObject.getClass().getFields(); 
            for (Field field : fields) {
                List<StringProperty> line = new ArrayList<StringProperty>();
           
                field.setAccessible(true);
                line.add(new SimpleStringProperty(field.getName()));
                try {
                    line.add(new SimpleStringProperty(field.get(this.currentSettingsObject) .toString()));
                } catch (IllegalAccessException e) {
                    line.add(new SimpleStringProperty("Unknown"));
                }
           
                this.settingsList.add(line);
            }
            this.settingsHaveBeenEdited = true;
        }
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
     * If settings or Ip/Port edited in GUI, prompt for discard or save.
     */
    private void checkSave() {
        if (this.settingsHaveBeenEdited || this.myIpOrPortHaveBeenEdited) {
            ButtonType save = new ButtonType("Save to File", ButtonBar.ButtonData.OK_DONE);
            ButtonType discard = new ButtonType("Not now", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert alert = new Alert(AlertType.CONFIRMATION,
                "The settings for " + currentMachineChoice + " or My Ip/Port have changed. Save them to the settings file?",
                save, discard);

            alert.setTitle("Settings check");
             
            Optional<ButtonType> result = alert.showAndWait();
            if (result.orElse(discard) == save)
                this.saveCurrentSettings();
        }
    }

    /**
     * When Save Settings button {@link btnSave} is pressed, do this.
     *
     * @param event
     */
    @FXML
    void actionBtnSave(ActionEvent event) {
        if (!this.settingsHaveBeenEdited && !this.myIpOrPortHaveBeenEdited) {
            labelMessages.setText("Nothing to save for " + this.currentMachineChoice + "or My Port of My IP Address.");
        } else 
            saveCurrentSettings();
    }

    /**
     * (1) Read whole settings JSON into HashMap keyed by machine name
     * (2) Build a JSON string representing map[this.currentMachineChoice] with values in StringProperty List this.settingsList
     * (3) Convert Json string to object and put it in map.
     * (4) Write map back to file as JSON.
     * (5) Set this.settingsHaveBeenEdited to false
     */
    private void saveCurrentSettings() {
        labelMessages.setText("Saving settings for " + this.currentMachineChoice + "...");

        HashMap<String, Object> map = OpiMachine.readSettingsFile();

        List<Field> allFields = Monitor.getAllFields(new ArrayList<Field>(), this.currentSettingsObject.getClass());

        String jsonString = "{" + 
            String.format("\"%s\":{\"ip\":%s, \"port\":%s},", OpiMachine.GUI_MACHINE_NAME, this.myIpAddress, this.myPort) +
            this.settingsList.stream()
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
        map.put(this.currentMachineChoice, gson.fromJson(jsonString, this.currentSettingsObject.getClass()));

        OpiMachine.writeSettingsFile(map);
        this.settingsHaveBeenEdited = false;
        this.myIpOrPortHaveBeenEdited = false;

        labelMessages.setText("Settings saved for " + this.currentMachineChoice + "and My IP/Port.");
    }

    /**
     * Action when Reset Settings button is pressed.
     *
     * (1) Ask for confirmation
     * (2) If OK, use fillSettings() to get the settings for this.currentMachineChoice
     *
     * @param event
     */
    @FXML
    void actionBtnResetSettings(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Reset Settings");
        String s = "Are you sure you want to reset the settings for " + currentMachineChoice + "?";
        alert.setContentText(s);
             
        if (alert.showAndWait()
                .map(response -> response == ButtonType.OK)
                .orElse(false)
        )
            this.fillSettingsData(this.currentMachineChoice, false);
    }

    /**
     * Fill in bits of the GUI
     * 
     * 1) Attach data {@link settingsList} to {@link tableSettings} to allow updates of table
     *    when list changes.
     * 1.2) Set up column Value to be editable.
     * 2) Read the names of machines from OpiMachine.MACHINES and put them in {@link listMachines}.
     *    When one is clicked, update {@link settingsList}.
     * 3) Get my ip and port from the settings file if they exist.
     * 3.1) Add change listeners to the myport and myip text fields.
     * 4) Create the myListenerThread ready for 'Connect' button
     */
    @FXML
    public void initialize() {
            // (1) Set up table columns to show machine settings
            // cellData is a list (cols) of list of strings (row), 
            // with first row element being field name in Settings and second the value
        colSettingsProperty.setCellValueFactory(cellData -> cellData.getValue().get(0));
        colSettingsValue.setCellValueFactory(cellData -> cellData.getValue().get(1));

            // (1.2) Make Value column editable
        colSettingsValue.setCellFactory(TextFieldTableCell.forTableColumn());
        colSettingsValue.setOnEditStart(e -> {
            labelMessages.setText("Don't forget to press Enter to save edit.");
        });
        colSettingsValue.setOnEditCommit(e -> {
            e.getRowValue().set(1 ,new SimpleStringProperty(e.getNewValue()));
            this.settingsHaveBeenEdited = true;
            labelMessages.setText("");
        });

        tableSettings.setItems(this.settingsList);
        this.settingsHaveBeenEdited = false;

            // (2) Set up list of machines
        ObservableList<String> olMachineList =  FXCollections.observableArrayList(OpiMachine.MACHINES);
        FXCollections.sort(olMachineList);
        listMachines.setItems(olMachineList);
        listMachines.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                fillSettingsData(newValue, true);
            }
        });
        listMachines.getSelectionModel().select(0);

            // (3) Get myIp and myPort from settings file if they exist
        HashMap<String, Object> settings = OpiMachine.readSettingsFile();
        if (settings.containsKey(OpiMachine.GUI_MACHINE_NAME)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> mySettings = (Map<String, Object>)settings.get(OpiMachine.GUI_MACHINE_NAME);
            if (mySettings.containsKey("ip")) {
                this.myIpAddress = (String)mySettings.get("ip");
                this.fieldMyIP.setText(this.myIpAddress);
            }
            if (mySettings.containsKey("port")) {
                this.myPort = String.format("%1.0f", mySettings.get("port"));
                this.fieldMyPort.setText(String.format("%s",this.myPort));
            }
        }

            // 3.1) Add change listeners to the myport and myip text fields.
        this.fieldMyIP.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                myIpAddress = newValue;
                myIpOrPortHaveBeenEdited = true;
            }
        });
        this.fieldMyPort.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                myPort = newValue;
                myIpOrPortHaveBeenEdited = true;
            }
        });

            // 4) Create the listner thread ready for 'Connect' button
        this.myListenerThread = new ListenerThread();
    }

    /**
     * Action when Connect button is pressed.
     *
     * (1) Open a CSListener for myself to get commands from (eg) R
     * (2) Open a CSWriter to the machine slected in {@link listMacines} to pass on commands
     * (3) If succesful, switch to the Scene of the {@link OpiMachine} we have switched to.
     *
     * @param event
     */
    @FXML
    void actionBtnConnect(ActionEvent event) {
        checkSave();

            // (1) Open my own connection to get commands from (eg) R
            // TODO this just assumes localhost will be the IP Address - what if myIPAddress contains something else?
        if (this.myListenerThread.isStopped()) {
            labelMessages.setText("Starting listener on port " + this.myPort);
            System.out.println("Starting listener thread");
            this.myListenerThread.setPort(this.myPort);
            this.myListenerThread.start();
        }

            // (2 & 3)create connection to OPIMachine and switch to its Scene
        labelMessages.setText("Trying to open connection to " + this.currentMachineChoice);

        final Node source = (Node) event.getSource();
        final Stage stage = (Stage) source.getScene().getWindow();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(String.format("resources/%s.fxml", this.currentMachineChoice)));
            OpiMachine mac = new org.lei.opi.core.Display(source.getScene());   // TODo this needs to change based 
            loader.setController(mac);
            Parent root = loader.load();
            Scene scene = new Scene(root, 800, 515);

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            labelMessages.setText("Cannot load FXML GUI for " + this.currentMachineChoice);
            e.printStackTrace();
        } catch (RuntimeException e) {
            labelMessages.setText("Couldn't open create OpiMachine connection for " + this.currentMachineChoice);
            e.printStackTrace();
        }
    }

    /**
     * A thread that will listen in {@link myPort} for commands and pass them to {@link OpiManager}.
     */
    class ListenerThread extends Thread {
        private int port;

        public void setPort(String port) { this.port = Integer.parseInt(port);}

        public boolean isStopped() {
            return (this.getState() == Thread.State.NEW || this.getState() == Thread.State.TERMINATED);
        }

        public void run() {
            CSListener csl = new CSListener(port, new OpiManager());
            System.out.println("Started Listener on " + port);
            while (true) {
                try {
                    Thread.sleep(100);
                } catch(InterruptedException e) {
                    System.out.println("Interrupted Listener");
                    break;
                }
            }
            System.out.println("Closed Listener");
            csl.close();
        }
   };

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
        this.checkSave();

        System.out.println("Stopping");
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(); 
    }
}