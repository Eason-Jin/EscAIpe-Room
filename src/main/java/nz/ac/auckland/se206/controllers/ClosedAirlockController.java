package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.GameState.NextTask;
import nz.ac.auckland.se206.managers.SceneManager.SceneUi;

/** Controller class for the closed airlock view. */
public class ClosedAirlockController extends Controller {

  @FXML private Rectangle controlConsole;
  @FXML private Rectangle closeButton;
  @FXML private Rectangle submitButton;
  @FXML private Label text;
  @FXML private TextField firstDigit;
  @FXML private TextField secondDigit;
  @FXML private TextField thirdDigit;
  @FXML private TextField fourthDigit;
  @FXML private Pane consolePane;
  @FXML private Rectangle shuttle;
  @FXML private Label wrongCodeMessage;
  @FXML private Rectangle arrow;
  @FXML private Rectangle door;
  @FXML private Text subtitle;
  @FXML private Rectangle controlScreen;
  @FXML private ImageView fixMe;
  @FXML private Label titleText;

  /**
   * Initializes the airlock view. The TextFields inside the control console are set up with
   * relevant events and filters.
   */
  @FXML
  private void initialize() {
    createNewTextToSpeech();
    hideConsole();
    wrongCodeMessage.setOpacity(0);
    // set the label text
    setLabelText();

    // set the event filter for single character input
    setSingleCharacterInput(null, firstDigit, secondDigit);
    setSingleCharacterInput(firstDigit, secondDigit, thirdDigit);
    setSingleCharacterInput(secondDigit, thirdDigit, fourthDigit);
    setSingleCharacterInput(thirdDigit, fourthDigit, null);

    // set the event filter for deleting single character input
    moveSingleCharacterInput(firstDigit);
    moveSingleCharacterInput(secondDigit);
    moveSingleCharacterInput(thirdDigit);
    moveSingleCharacterInput(fourthDigit);

    fixMe.visibleProperty().bind(GameState.wireGameSolvedProperty.not());
  }

  /**
   * Handles the click event on the control console.
   *
   * @param event the mouse event
   */
  @FXML
  private void clickControlConsole(MouseEvent event) {
    if (GameState.wireGameSolvedProperty.get()) {
      showConsole();
      // disable the arrow
      arrow.setOpacity(0);
      arrow.setMouseTransparent(true);
    } else {
      appendTextTo(subtitle, "The control console has no power...");
    }
  }

  /**
   * Handles the click event to close the console.
   *
   * @param event the mouse event
   */
  @FXML
  private void clickCloseButton(MouseEvent event) {
    hideConsole();
    // enable the arrow
    arrow.setOpacity(1);
    arrow.setMouseTransparent(false);
  }

  /**
   * Handles the click event to submit the code.
   *
   * @param event the mouse event
   * @throws IOException if "src/main/resources/fxml/openedAirlock.fxml" is not found
   * @throws URISyntaxException if the sound effect cannot be found
   */
  @FXML
  private void clickSubmitButton(MouseEvent event) throws IOException, URISyntaxException {
    checkCode();
  }

  /**
   * Handles the click event on the Control Screen.
   *
   * @param event the mouse event
   * @throws IOException if "src/main/resources/fxml/wireGame.fxml" is not found
   */
  @FXML
  private void clickControlScreen(MouseEvent event) throws IOException {
    if (!GameState.wireGameSolvedProperty.get()) {
      App.setRoot(SceneUi.GAME);
    } else {
      appendTextTo(subtitle, "The wires have already been fixed...");
    }
  }

  /** Shows the wrong code message for 1 second. */
  private void showWrongCodeMessage() {
    text.setOpacity(0);
    titleText.setOpacity(0);
    wrongCodeMessage.setOpacity(1);

    // hide the message after 2 seconds
    Timeline timeline =
        new Timeline(
            new KeyFrame(
                Duration.seconds(1),
                event -> {
                  wrongCodeMessage.setOpacity(0);
                  text.setOpacity(1);
                  titleText.setOpacity(1);
                }));
    timeline.setCycleCount(1);
    timeline.play();
  }

  /** Shows the control console from the view. */
  private void showConsole() {
    consolePane.setOpacity(1);
    consolePane.setDisable(false);
  }

  /** Hides the control console from the view. */
  private void hideConsole() {
    consolePane.setOpacity(0);
    consolePane.setDisable(true);
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
   * Sets the event filter for single character input.
   *
   * @param previousTextField the TextField previous to the current one
   * @param textField the current TextField
   * @param nextTextField the TextField proceeding the current one
   */
  private void setSingleCharacterInput(
      TextField previousTextField, TextField textField, TextField nextTextField) {
    textField.setOnKeyReleased(
        event -> {
          // check if enter key is pressed
          if (event.getCode() == KeyCode.ENTER) {
            try {
              checkCode();
            } catch (IOException | URISyntaxException e) {
              e.printStackTrace();
            }
          }
          // check if delete key is pressed
          if (event.getCode() == KeyCode.BACK_SPACE) {
            event.consume();
            if (textField.getText().isEmpty() && previousTextField != null) {
              previousTextField.requestFocus();
              deselectNumber(previousTextField);
            } else {
              String text = textField.getText();
              if (!text.isEmpty()) {
                // only display first character of input
                textField.setText(text.substring(0, text.length() - 1));
              }
            }
          }
        });

    textField.addEventFilter(
        KeyEvent.KEY_TYPED,
        event -> {
          String input = event.getCharacter();
          // input must be single character
          if (!input.isEmpty() && textField.getText().length() >= 1) {
            event.consume();
            if (nextTextField != null) {
              nextTextField.requestFocus();
            }
          }
        });
  }

  /**
   * Helper method to ensure the next or previous TextField is selected depending on user input.
   *
   * @param textField the TextField to add the requirement to
   */
  private void moveSingleCharacterInput(TextField textField) {
    textField.setOnKeyPressed(
        event -> {
          // check if left or right arrow key is pressed
          if (event.getCode() == KeyCode.LEFT) {
            navigateLeft(event);
          } else if (event.getCode() == KeyCode.RIGHT) {
            navigateRight(event);
          }
        });
  }

  /**
   * Pressing the left arrow key will move the cursor to the left.
   *
   * @param event the key event
   */
  private void navigateLeft(KeyEvent event) {
    TextField source = (TextField) event.getSource();
    // move to the left
    if (source == fourthDigit) {
      thirdDigit.requestFocus();
      deselectNumber(thirdDigit);
    } else if (source == thirdDigit) {
      secondDigit.requestFocus();
      deselectNumber(secondDigit);
    } else if (source == secondDigit) {
      firstDigit.requestFocus();
      deselectNumber(firstDigit);
    }
    event.consume();
  }

  /**
   * Pressing the right arrow key will move the cursor to the right.
   *
   * @param event the key event
   */
  private void navigateRight(KeyEvent event) {
    TextField source = (TextField) event.getSource();
    // move to the right
    if (source == firstDigit) {
      secondDigit.requestFocus();
      deselectNumber(secondDigit);
    } else if (source == secondDigit) {
      thirdDigit.requestFocus();
      deselectNumber(thirdDigit);
    } else if (source == thirdDigit) {
      fourthDigit.requestFocus();
      deselectNumber(fourthDigit);
    }
    event.consume();
  }

  /**
   * Handles the click event on the door.
   *
   * @param event the mouse event
   */
  @FXML
  private void clickDoor(MouseEvent event) {
    appendTextTo(subtitle, "The door is locked...");
  }

  /**
   * Deselects the number in the text field and moves the cursor to the end of the text field.
   *
   * @param textField the TextField to deselect the number within
   */
  private void deselectNumber(TextField textField) {
    textField.deselect();
    textField.positionCaret(textField.getLength());
  }

  /** Helper method to set the text inside the label. */
  private void setLabelText() {
    text.setText("Enter the code to open the airlock.");
  }

  /**
   * Validates the user-entered lock code and performs corresponding actions based on the validation
   * result.
   *
   * @throws IOException if an exception occurs while handling input/output operations
   * @throws URISyntaxException if the sound effect cannot be found
   */
  private void checkCode() throws IOException, URISyntaxException {
    String code =
        firstDigit.getText() + secondDigit.getText() + thirdDigit.getText() + fourthDigit.getText();
    // check if the code is correct
    if (code.equals(String.valueOf(GameState.lockCode))) {
      GameState.isAirlockOpen = true;
      GameState.nextTask = NextTask.ESCAPE;
      App.setRoot(SceneUi.OPEN_AIRLOCK);
    } else {
      showWrongCodeMessage();
    }
  }
}
