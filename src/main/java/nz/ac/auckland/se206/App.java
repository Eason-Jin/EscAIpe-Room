package nz.ac.auckland.se206;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import nz.ac.auckland.se206.managers.SceneManager;
import nz.ac.auckland.se206.managers.SceneManager.SceneUi;
import nz.ac.auckland.se206.speech.TextToSpeech;

/**
 * This is the entry point of the JavaFX application, while you can change this class, it should
 * remain as the class that runs the JavaFX application.
 */
public class App extends Application {

  private static Scene scene;
  private static Stage primaryStage;
  private static TextToSpeech textToSpeech;
  private static AtomicBoolean isSpeaking;
  private static MediaPlayer player;

  public static void main(final String[] args) {
    textToSpeech = new TextToSpeech();
    isSpeaking = new AtomicBoolean(false);
    launch();
  }

  /**
   * Sets the root of the scene to the specified room unless there is no current scene, in which
   * case the scene will be created in the {@code start} method.
   *
   * @param sceneUi the room the root will be switched to
   * @return the root of the scene to be updated
   * @throws IOException if the string scene UI is not found
   */
  public static Parent setRoot(SceneUi sceneUi) throws IOException {

    Parent root = SceneManager.getParentSceneUi(sceneUi);

    if (root == null) {
      // scene has not been loaded previously
      root = loadFxml(SceneManager.getStringSceneUi(sceneUi));
      if (sceneUi == SceneUi.CONTROL_ROOM) {
        // load all rooms in background (including chat and riddle)
        SceneManager.addParentSceneUi(sceneUi, root);
        SceneManager.addParentSceneUi(
            SceneUi.CLOSED_AIRLOCK,
            loadFxml(SceneManager.getStringSceneUi(SceneUi.CLOSED_AIRLOCK)));
        SceneManager.addParentSceneUi(
            SceneUi.BEDROOM, loadFxml(SceneManager.getStringSceneUi(SceneUi.BEDROOM)));
        SceneManager.addParentSceneUi(
            SceneUi.CHAT, loadFxml(SceneManager.getStringSceneUi(SceneUi.CHAT)));
        SceneManager.addParentSceneUi(
            SceneUi.RIDDLE, loadFxml(SceneManager.getStringSceneUi(SceneUi.RIDDLE)));
        SceneManager.addParentSceneUi(
            SceneUi.GAME, loadFxml(SceneManager.getStringSceneUi(SceneUi.GAME)));
      } else if (sceneUi == SceneUi.SUCCESS
          || sceneUi == SceneUi.FAILED
          || sceneUi == SceneUi.OPEN_AIRLOCK) {
        SceneManager.addParentSceneUi(sceneUi, root);
      }
    }

    if (scene != null) {
      if (sceneUi != SceneUi.CHAT && sceneUi != SceneUi.RIDDLE) {
        // update current scene for back button in AI views
        GameState.currentScene = sceneUi;
      }
      scene.setRoot(root);
    }

    return root;
  }

  /**
   * Starts the text-to-speech speaking the specified text.
   *
   * @param text the text to be spoken
   */
  public static void speak(String text) {
    if (!isSpeaking.get()) {
      Task<Void> taskSpeak =
          new Task<>() {

            @Override
            protected Void call() throws Exception {
              isSpeaking.set(true);
              textToSpeech.speak(text);
              return null;
            }
          };

      // reset isSpeaking when task finished
      taskSpeak.setOnSucceeded(event -> isSpeaking.set(false));
      taskSpeak.setOnCancelled(event -> isSpeaking.set(false));
      taskSpeak.setOnFailed(event -> isSpeaking.set(false));

      Thread threadSpeak = new Thread(taskSpeak, "Text to Speech");
      threadSpeak.start();
    }
  }

  /** This method handles the closing of the application. */
  public static void logout() {

    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setTitle("Quit");
    alert.setHeaderText("If you quit, all progress will be lost.");
    alert.setContentText("Are you sure you want to quit?");

    // only close window if 'OK' was pressed
    if (alert.showAndWait().get() == ButtonType.OK) {
      textToSpeech.terminate();
      primaryStage.close();
      Platform.exit();
      System.exit(0);
    }
  }

  /**
   * Plays the background music for the game.
   *
   * @throws URISyntaxException if "/sounds/space-noise.mp3" is not found
   */
  public static void playMusic() throws URISyntaxException {
    Media sound = new Media(App.class.getResource("/sounds/space-noise.mp3").toURI().toString());

    if (player == null) {
      player = new MediaPlayer(sound);
      player.play();

      // repeat music on finish to ensure constant background music
      player.setOnEndOfMedia(
          () -> {
            player.seek(Duration.ZERO);
            player.play();
          });
    }
  }

  /** Stops the background music for the game. */
  public static void stopMusic() {
    if (player != null) {
      player.stop();
      player.dispose();
      player = null;
    }
  }

  /**
   * Returns the node associated to the input file. The method expects that the file is located in
   * "src/main/resources/fxml".
   *
   * @param fxml The name of the FXML file (without extension).
   * @return The node of the input file.
   * @throws IOException If the file is not found.
   */
  private static Parent loadFxml(final String fxml) throws IOException {
    return new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml")).load();
  }

  /**
   * This method is invoked when the application starts. It loads and shows the "Start" scene.
   *
   * @param stage the primary stage of the application
   * @throws IOException if "src/main/resources/fxml/start.fxml" is not found
   */
  @Override
  public void start(final Stage stage) throws IOException {
    addRooms();

    Parent root = setRoot(SceneUi.START);
    scene = new Scene(root, 960, 540);
    scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());

    stage.setTitle("SpAIce EscAIpe");
    stage.setScene(scene);
    stage.setResizable(false);
    stage.show();

    stage.setOnCloseRequest(
        event -> {
          event.consume(); // prevent window from closing when 'Cancel' is pressed
          logout();
        });

    root.requestFocus();
    primaryStage = stage;
  }

  /**
   * This method adds each room to the SceneManager so they can be accessed throughout the game.
   *
   * @throws IOException if "src/main/resources/fxml/_.fxml" is not found
   */
  private void addRooms() throws IOException {
    // store string of location of each view
    SceneManager.addStringSceneUi(SceneUi.START, "start");
    SceneManager.addStringSceneUi(SceneUi.CHAT, "chat");
    SceneManager.addStringSceneUi(SceneUi.RIDDLE, "riddle");
    SceneManager.addStringSceneUi(SceneUi.OPEN_AIRLOCK, "openedAirlock");
    SceneManager.addStringSceneUi(SceneUi.CLOSED_AIRLOCK, "closedAirlock");
    SceneManager.addStringSceneUi(SceneUi.CONTROL_ROOM, "controlroom");
    SceneManager.addStringSceneUi(SceneUi.BEDROOM, "bedroom");
    SceneManager.addStringSceneUi(SceneUi.SUCCESS, "success");
    SceneManager.addStringSceneUi(SceneUi.FAILED, "failed");
    SceneManager.addStringSceneUi(SceneUi.ACT_A, "actA");
    SceneManager.addStringSceneUi(SceneUi.ACT_B, "actB");
    SceneManager.addStringSceneUi(SceneUi.GAME, "wireGame");
  }
}
