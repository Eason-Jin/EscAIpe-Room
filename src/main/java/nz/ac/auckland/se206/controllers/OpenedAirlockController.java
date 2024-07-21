package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.GameState.NextTask;
import nz.ac.auckland.se206.managers.SceneManager.SceneUi;
import nz.ac.auckland.se206.managers.TimeManager;

/** Controller class for the opened airlock view. */
public class OpenedAirlockController extends Controller {

  @FXML private Rectangle shuttle;
  @FXML private Rectangle arrow;
  @FXML private Rectangle controlConsole;
  @FXML private Rectangle controlScreen;
  @FXML private Text subtitle;
  @FXML private ImageView imageViewDoorLeft;
  @FXML private ImageView imageViewDoorRight;
  @FXML private ImageView imageViewHiddenLeft;
  @FXML private ImageView imageViewHiddenRight;

  /**
   * Initializes the airlock view. A text-to-speech instance will be created.
   *
   * @throws URISyntaxException if the sound effect cannot be found
   */
  @FXML
  public void initialize() throws URISyntaxException {
    createNewTextToSpeech();
    openDoors();
  }

  /**
   * Handles the click event on the shuttle.
   *
   * @param event the mouse event
   * @throws IOException if "src/main/resources/fxml/actA.fxml" is not found
   */
  @FXML
  private void clickShuttle(MouseEvent event) throws IOException {
    TimeManager.stopTime();
    ttsSpeak("Launching space shuttle.");
    GameState.nextTask = NextTask.SUCCESS;
    App.setRoot(SceneUi.ACT_A);
  }

  /**
   * Transports the player to the control room.
   *
   * @param event the mouse event
   * @throws IOException if "src/main/resources/fxml/controlroom.fxml" is not found
   */
  @FXML
  private void goControlRoom(MouseEvent event) throws IOException {
    ttsSpeak("Entering control room.");
    App.setRoot(SceneUi.CONTROL_ROOM);
  }

  /**
   * Handles the click event on the control console.
   *
   * @param event the mouse event
   */
  @FXML
  private void clickControlConsole(MouseEvent event) {
    appendTextTo(subtitle, "The shuttle is ready to launch!");
  }

  /**
   * Handles the click event on the control screen.
   *
   * @param event the mouse event
   */
  @FXML
  private void clickControlScreen(MouseEvent event) {
    appendTextTo(subtitle, "The wires have already been fixed");
  }

  /**
   * Helper method to create an animation to open the doors of the airlock.
   *
   * @throws URISyntaxException if the sound effect cannot be found
   */
  private void openDoors() throws URISyntaxException {
    playSound("air", false);

    // create animations
    TranslateTransition translateDoorLeft =
        new TranslateTransition(Duration.seconds(2), imageViewDoorLeft);
    translateDoorLeft.setByX(-98);
    TranslateTransition translateDoorRight =
        new TranslateTransition(Duration.seconds(2), imageViewDoorRight);
    translateDoorRight.setByX(95);

    translateDoorLeft.setOnFinished(
        e -> {
          // remove doors and door-hiders from view
          imageViewDoorLeft.setVisible(false);
          imageViewDoorRight.setVisible(false);
          imageViewHiddenLeft.setVisible(false);
          imageViewHiddenRight.setVisible(false);

          // turn on selectable items
          shuttle.setVisible(true);
          arrow.setVisible(true);
          controlConsole.setVisible(true);
          controlScreen.setVisible(true);
        });

    translateDoorLeft.play();
    translateDoorRight.play();
  }
}
