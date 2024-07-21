package nz.ac.auckland.se206.custom;

import java.io.IOException;
import java.net.URISyntaxException;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.managers.SceneManager.SceneUi;
import nz.ac.auckland.se206.managers.TimeManager;

/** Controller class for the Game Master custom component. */
public class GameMaster extends Pane {

  /** Represents the different faces the Game Master can have. */
  public enum GameMasterFace {
    TIME,
    HAPPY,
    SAD
  }

  @FXML private ImageView imageView;
  @FXML private Label label;
  @FXML private Rectangle rectangle;

  /**
   * Default constructor to build the Game Master.
   *
   * @throws URISyntaxException if "src/main/resources/fxml/gamemaster.fxml" is not found
   */
  public GameMaster() throws URISyntaxException {

    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/gamemaster.fxml"));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);

    try {
      fxmlLoader.load();
    } catch (IOException e) {
      System.err.println("src/main/resources/fxml/gamemaster.fxml was not found");
      e.printStackTrace();
    }

    label.setLayoutX(20);
    label.setLayoutY(58);
    updateTimeText();

    // create and play animations
    translateX(imageView);
    translateY(imageView);
    translateX(label);
    translateY(label);
    translateX(rectangle);
    translateY(rectangle);

    // update time remaining every second
    TimeManager.timeRemaining.addListener(
        (observable, oldValue, newValue) ->
            Platform.runLater(
                () -> {
                  try {
                    updateTimeText();
                  } catch (URISyntaxException e) {
                    e.printStackTrace();
                  }
                }));
  }

  /**
   * Transports the player to the chat to talk with the Game Master.
   *
   * @throws IOException if "src/main/resources/fxml/chat.fxml" is not found
   */
  @FXML
  private void onClickGameMaster() throws IOException {
    App.setRoot(SceneUi.CHAT);
  }

  /**
   * Sets the text of the label in the Game Master.
   *
   * @param value the string of text that will be displayed within the label
   */
  public final void setText(String value) {
    label.setText(value);
  }

  /**
   * Sets the face of the Game Master to one of the presets. Useful for having all properties of
   * Game Master in different environments.
   *
   * @param face the face the Game Master should have
   */
  public final void setFace(GameMasterFace face) {
    switch (face) {
      case TIME:
        // blank face with label for time remaining
        imageView.setImage(new Image("/images/gamemaster/game_master.png"));
        label.setVisible(true);
        break;
      case HAPPY:
        imageView.setImage(new Image("/images/gamemaster/game_master_happy.png"));
        label.setVisible(false);
        break;
      case SAD:
        imageView.setImage(new Image("/images/gamemaster/game_master_sad.png"));
        label.setVisible(false);
        break;
    }
  }

  /**
   * Sets the visibility of the clickable rectangle within the Game Master. Useful for if Game
   * Master is in the chat or riddle view already.
   *
   * @param clickable whether the player can click the Game Master to go to the chat view
   */
  public final void setClickable(boolean clickable) {
    rectangle.setVisible(clickable);
  }

  /**
   * Helper method to create and play a horizontal translate transition to a node.
   *
   * @param node the node to be translated horizontally
   */
  private void translateX(Node node) {
    TranslateTransition translateNodeX = new TranslateTransition(Duration.millis(800), node);
    translateNodeX.setCycleCount(TranslateTransition.INDEFINITE);
    translateNodeX.setAutoReverse(true);
    translateNodeX.setByX(5);
    translateNodeX.play();
  }

  /**
   * Helper method to create and play a vertical translate transition to a node.
   *
   * @param node the node to be translated vertically
   */
  private void translateY(Node node) {
    TranslateTransition translateNodeY = new TranslateTransition(Duration.millis(400), node);
    translateNodeY.setCycleCount(TranslateTransition.INDEFINITE);
    translateNodeY.setAutoReverse(true);
    translateNodeY.setByY(2.5);
    translateNodeY.play();
  }

  /**
   * Helper method to set the text in the label to the current time remaining.
   *
   * @throws URISyntaxException if the sound file is not found
   */
  private void updateTimeText() throws URISyntaxException {
    int time = TimeManager.timeRemaining.get();
    int minutes = time / 60;
    int seconds = time % 60;

    label.setText(
        ((minutes < 10) ? "0" : "") + minutes + ":" + ((seconds < 10) ? "0" : "") + seconds);

    // play warning sound every 2 seconds when time is less than 10 seconds
    if (GameState.isSoundEffectOn.get() && time <= 10 && time % 2 == 0 && time > 0) {
      Media sound = new Media(App.class.getResource("/sounds/warning.mp3").toURI().toString());
      Task<Void> playerTask =
          new Task<Void>() {

            @Override
            protected Void call() throws Exception {
              MediaPlayer player = new MediaPlayer(sound);
              player.play();
              return null;
            }
          };
      Thread playerThread = new Thread(playerTask);
      playerThread.setDaemon(true);
      playerThread.start();
    }

    // change text to red if running out of time
    label.setStyle("-fx-text-fill: " + ((time <= 10) ? "red" : "white") + ";");
  }
}
