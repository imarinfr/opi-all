package org.lei.opi.frontend;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class OpiController {
  @FXML
  private Label welcomeText;

  @FXML
  protected void onHelloButtonClick() {
    welcomeText.setText("Welcome to JavaFX Application!");
  }
}