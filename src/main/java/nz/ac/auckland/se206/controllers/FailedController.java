package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.custom.GameMaster;
import nz.ac.auckland.se206.custom.GameMaster.GameMasterFace;
import nz.ac.auckland.se206.managers.SceneManager;
import nz.ac.auckland.se206.managers.SceneManager.SceneUi;

/** Controller class for the failed view. */
public class FailedController extends Controller {

  @FXML private Button buttonRestartGame;
  @FXML private Button buttonQuitGame;
  @FXML private GameMaster gameMaster;

  /**
   * Initialises the failed view. The game master will no longer display the time remaining. The
   * player will be notified they lost by sound effects and text-to-speech.
   *
   * @throws URISyntaxException if the sound file cannot be found
   */
  @FXML
  private void initialize() throws URISyntaxException {
    gameMaster.setFace(GameMasterFace.SAD);
    playSound("power-down", false);
    createNewTextToSpeech();
    ttsSpeak("The power ran out, you failed.");
  }

  /**
   * Handles the click event on the restart button.
   *
   * @param event the mouse event
   * @throws IOException if "src/main/resources/fxml/controlroom.fxml" is not found
   */
  @FXML
  private void restart(MouseEvent event) throws IOException {
    SceneManager.clearSceneParentMap();
    App.setRoot(SceneUi.CONTROL_ROOM);
  }

  /**
   * Handles the click event on the exit button.
   *
   * @param event the mouse event
   */
  @FXML
  private void exit(MouseEvent event) {
    App.logout();
  }
}
