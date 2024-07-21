package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import javafx.fxml.FXML;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.managers.SceneManager.SceneUi;

/** Controller class for the start room view. */
public class StartController extends Controller {

  @FXML
  private void initialize() throws URISyntaxException {
    if (GameState.isMusicOn.get()) {
      App.playMusic();
    }
    App.speak("Welcome to Space Escape!");
  }

  /**
   * Handles the click on enter game button event.
   *
   * @throws IOException if "src/main/resources/fxml/controlroom.fxml" is not found
   */
  @FXML
  private void onEnterGame() throws IOException {
    App.setRoot(SceneUi.CONTROL_ROOM);
  }
}
