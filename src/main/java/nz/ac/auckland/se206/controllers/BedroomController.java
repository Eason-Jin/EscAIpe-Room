package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.GameState.NextTask;
import nz.ac.auckland.se206.GameState.RiddleObjects;
import nz.ac.auckland.se206.managers.SceneManager.SceneUi;

/** Controller class for the bedroom view. */
public class BedroomController extends Controller {

  @FXML private Circle chair;
  @FXML private Rectangle blanket;
  @FXML private Rectangle box;
  @FXML private Rectangle exit;
  @FXML private Rectangle drawer;
  @FXML private Rectangle mug;
  @FXML private AnchorPane paperPane;
  @FXML private Text number;
  @FXML private Text subtitle;

  /** Initializes the bedroom view. The paper is hidden from the player. */
  @FXML
  private void initialize() {
    createNewTextToSpeech();

    // initially hide numbers
    hidePaper();
    number.setText(Integer.toString(GameState.lockCode));
    subtitle.setText("");
  }

  /**
   * Handles the box press event.
   *
   * @param event the mouse event
   */
  @FXML
  private void clickBox(MouseEvent event) {
    checkObject(RiddleObjects.BOX);
  }

  /**
   * Handles the blanket press event.
   *
   * @param event the mouse event
   */
  @FXML
  private void clickBlanket(MouseEvent event) {
    checkObject(RiddleObjects.BLANKET);
  }

  /**
   * Handles the chair press event.
   *
   * @param event the mouse event
   */
  @FXML
  private void clickChair(MouseEvent event) {
    checkObject(RiddleObjects.CHAIR);
  }

  /**
   * Handles the drawer press event.
   *
   * @param event the mouse event
   */
  @FXML
  private void clickDrawer(MouseEvent event) {
    checkObject(RiddleObjects.STORAGE);
  }

  /**
   * Handles the mug press event.
   *
   * @param event the mouse event
   */
  @FXML
  private void clickMug(MouseEvent event) {
    checkObject(RiddleObjects.MUG);
  }

  /**
   * Handles the object press event.
   *
   * @param event the mouse event
   */
  @FXML
  private void checkObject(RiddleObjects obj) {
    // Prevent interaction before the riddle is solved
    if (!GameState.isRiddleSolved.get()) {
      appendTextTo(subtitle, "Such a mess here... What should I be looking for?");
      return;
    }

    if (GameState.riddleObject.equals(obj)) {
      showPaper();
    } else {
      hidePaper();
      appendTextTo(subtitle, "Nothing here...");
    }
  }

  /**
   * Handles the exit press event.
   *
   * @param event the mouse event
   * @throws IOException if "src/main/resources/fxml/controlroom.fxml" is not found
   */
  @FXML
  private void clickExit(MouseEvent event) throws IOException {
    ttsSpeak("Entering control room.");
    App.setRoot(SceneUi.CONTROL_ROOM);
  }

  /**
   * Press paper to exit paper view.
   *
   * @param event the mouse event
   */
  @FXML
  private void clickPaperPane(MouseEvent event) {
    hidePaper();
  }

  /** Helper method to hide the paper from the player. */
  private void hidePaper() {
    paperPane.setOpacity(0);
  }

  /**
   * Helper method to show the paper, write the message to the screen, and update next task in the
   * game.
   */
  private void showPaper() {
    paperPane.setOpacity(1);
    appendTextTo(subtitle, "Oh! There is a piece of paper!");
    if (GameState.wireGameSolvedProperty.get()) {
      GameState.nextTask = NextTask.CONTROL_CONSOLE;
    } else if (GameState.nextTask.ordinal() < NextTask.WIRES.ordinal()) {
      GameState.nextTask = NextTask.WIRES;
    }
  }
}
