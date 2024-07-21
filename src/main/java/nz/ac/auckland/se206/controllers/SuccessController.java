package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.custom.GameMaster;
import nz.ac.auckland.se206.custom.GameMaster.GameMasterFace;
import nz.ac.auckland.se206.managers.SceneManager;
import nz.ac.auckland.se206.managers.SceneManager.SceneUi;
import nz.ac.auckland.se206.managers.TimeManager;

/** Controller class for the success view. */
public class SuccessController extends Controller {

  @FXML private Button buttonRestartGame;
  @FXML private Label labelInGameText;
  @FXML private GameMaster gameMaster;

  /**
   * Initialises the success view. The player will be informed of their achievement of winning the
   * game.
   *
   * @throws URISyntaxException if the sound file cannot be found
   */
  @FXML
  private void initialize() throws URISyntaxException {
    // ensure Game Master doesn't display time remaining
    gameMaster.setFace(GameMasterFace.HAPPY);
    playSound("win", false);
    createNewTextToSpeech();
    ttsSpeak("Well done, you made to earth in " + getTimeTaken() + " seconds.");
    labelInGameText.setText(
        "Congratulations, you escaped the abandoned space station and made it back to Earth in the"
            + " space shuttle!\n\n"
            + "Time taken to escape: "
            + getTimeTaken());
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

  /**
   * Gets the time taken to complete the game.
   *
   * @return the correctly formatted time remaining
   */
  private String getTimeTaken() {
    int secondsTaken = GameState.timeLimit - TimeManager.timeRemaining.get();
    int minutes = secondsTaken / 60;
    int seconds = secondsTaken % 60;
    return ((minutes < 10) ? "0" : "") + minutes + ":" + ((seconds < 10) ? "0" : "") + seconds;
  }
}
