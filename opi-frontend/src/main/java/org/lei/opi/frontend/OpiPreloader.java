package org.lei.opi.frontend;

import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

/**
 *
 * OPI preloader
 *
 * @since 0.0.1
 */
public class OpiPreloader extends Preloader {

  private Stage stage;
  private Scene scene;

  /**
   *
   * Initialize preloader
   *
   * @since 0.0.1
   */
  @Override
  public void init() throws IOException {
    Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("splashScreen.fxml")));
    scene = new Scene(parent);
    // scene = new Scene(parent, 700, 400, Color.TRANSPARENT);
    // root1.setStyle("-fx-background-color: transparent;");
  }

  /**
   *
   * Start preloader
   *
   * @param stage Stages, whatever
   *
   * @since 0.0.1
   */
  @Override
  public void start(Stage stage) {
    this.stage = stage;
    // preloaderStage.initStyle(StageStyle.TRANSPARENT);
    // Set preloader scene and show stage.
    stage.setScene(scene);
    stage.initStyle(StageStyle.UNDECORATED);
    stage.show();
  }

  /**
   *
   * Handle app notifications
   *
   * @param info preloader notifications
   *
   * @since 0.0.1
   */
  public void appNotifications(PreloaderNotification info) {
    if (info instanceof ProgressNotification) {
      // FXMLDocumentController.label.setText("Loading "+((ProgressNotification)
      // info).getProgress()*100 + "%");
      System.out.println("Value@ :" + ((ProgressNotification) info).getProgress());
      // FXMLDocumentController.statProgressBar.setProgress(((ProgressNotification)
      // info).getProgress());
    }
  }

  /**
   *
   * Handle state changes
   *
   * @param info preloader notifications
   *
   * @since 0.0.1
   */
  public void stateChanges(StateChangeNotification info) {
    StateChangeNotification.Type type = info.getType();
    if (type == StateChangeNotification.Type.BEFORE_START) {
      System.out.println("BEFORE_START");
      stage.hide();
    }
  }

}
