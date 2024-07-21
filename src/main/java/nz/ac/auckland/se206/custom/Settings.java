package nz.ac.auckland.se206.custom;

import java.io.IOException;
import java.net.URISyntaxException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.Pane;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.managers.SceneManager;
import nz.ac.auckland.se206.managers.SceneManager.SceneUi;

/** Controller class for the Settings custom component. */
public class Settings extends Pane {

  @FXML private Pane settingPane;
  @FXML private RadioButton soundOnButton;
  @FXML private RadioButton soundOffButton;
  @FXML private RadioButton musicOnButton;
  @FXML private RadioButton musicOffButton;
  @FXML private RadioButton soundEffectOnButton;
  @FXML private RadioButton soundEffectOffButton;
  @FXML private Button restartButton;

  /**
   * Default constructor to build the Settings view.
   *
   * @throws URISyntaxException if "src/main/resources/fxml/settings.fxml" is not found
   */
  public Settings() throws URISyntaxException {

    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/settings.fxml"));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);

    try {
      fxmlLoader.load();
    } catch (IOException e) {
      System.err.println("src/main/resources/fxml/settings.fxml was not found");
      e.printStackTrace();
    }

    disableSettingWindow();
  }

  /** Ensures the correct radio buttons are clicked when the window is opened. */
  public final void openSettingWindow() {
    System.out.println("Setting window opened");
    enableSettingWindow();

    // check sound and music state
    if (GameState.isTextToSpeechOn.get()) {
      soundOnButton.setSelected(true);
    } else {
      soundOffButton.setSelected(true);
    }
    if (GameState.isMusicOn.get()) {
      musicOnButton.setSelected(true);
    } else {
      musicOffButton.setSelected(true);
    }
    if (GameState.isSoundEffectOn.get()) {
      soundEffectOnButton.setSelected(true);
    } else {
      soundEffectOffButton.setSelected(true);
    }
  }

  /** Disables the restart button. Useful for when the game has not yet started. */
  public final void disableRestart() {
    restartButton.setDisable(true);
  }

  /** Enables the restart button. Useful for enabling after initially being disabled. */
  public final void enableRestart() {
    restartButton.setDisable(false);
  }

  /** Handles the click event on a sound radio button. */
  @FXML
  private void onUpdateTextToSpeech() {
    if (soundOnButton.isSelected()) {
      GameState.isTextToSpeechOn.set(true);
      // verbally tell player their selection
      App.speak("text to speech on");
    } else if (soundOffButton.isSelected()) {
      GameState.isTextToSpeechOn.set(false);
    }
    System.out.println("Sound: " + GameState.isTextToSpeechOn);
  }

  /**
   * Handles the click event on a music radio button.
   *
   * @throws URISyntaxException if the sound file is not found
   */
  @FXML
  private void onUpdateMusic() throws URISyntaxException {
    if (musicOnButton.isSelected()) {
      // change music state
      GameState.isMusicOn.set(true);
      if (GameState.isTextToSpeechOn.get()) {
        // verbally tell player their selection
        App.speak("background music on");
      }
      App.playMusic();
    } else if (musicOffButton.isSelected()) {
      GameState.isMusicOn.set(false);
      if (GameState.isTextToSpeechOn.get()) {
        // verbally tell player their selection
        App.speak("background music off");
      }
      App.stopMusic();
    }
    System.out.println("Music: " + GameState.isMusicOn);
  }

  /** Handles the click event on the button to close the settings window. */
  @FXML
  private void closeSettingWindow() {
    System.out.println("Setting window closed");
    disableSettingWindow();
  }

  /** Handles the click event on the button to restart the game. */
  @FXML
  private void restartGame() throws IOException {
    SceneManager.clearSceneParentMap();
    App.setRoot(SceneUi.CONTROL_ROOM);
  }

  /** Handles the click event on the exit button. */
  @FXML
  private void exit() {
    App.logout();
  }

  /** Handles the click event on a sound effect radio button. */
  @FXML
  private void onUpdateSoundEffect() {
    if (soundEffectOnButton.isSelected()) {
      GameState.isSoundEffectOn.set(true);
      if (GameState.isTextToSpeechOn.get()) {
        // verbally tell player their selection
        App.speak("sound effects on");
      }
    } else if (soundEffectOffButton.isSelected()) {
      GameState.isSoundEffectOn.set(false);
      if (GameState.isTextToSpeechOn.get()) {
        // verbally tell player their selection
        App.speak("sound effects off");
      }
    }
    System.out.println("Sound Effects: " + GameState.isSoundEffectOn);
  }

  /** Helper method to close the settings window. */
  private void disableSettingWindow() {
    settingPane.setVisible(false);
    settingPane.setDisable(true);
  }

  /** Helper method to open the settings window. */
  private void enableSettingWindow() {
    settingPane.setVisible(true);
    settingPane.setDisable(false);
  }
}
