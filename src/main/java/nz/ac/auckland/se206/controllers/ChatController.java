package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult.Choice;
import nz.ac.auckland.se206.managers.SceneManager.SceneUi;
import nz.ac.auckland.se206.managers.TimeManager;

/** Controller class for the chat view. */
public class ChatController extends AiController {

  @FXML private Button riddleButton;

  /**
   * Initializes the AI view, loading the chat.
   *
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  @FXML
  private void initialize() throws ApiProxyException {
    super.initialize(GptPromptEngineering.startChat());

    // disable player moving to riddle if failed
    TimeManager.timeRemaining.addListener(
        (observable, oldValue, newValue) -> {
          if (TimeManager.timeRemaining.get() == 0) {
            Platform.runLater(() -> riddleButton.setDisable(true));
          }
        });
  }

  /**
   * Navigates to riddle view.
   *
   * @param event the action event triggered by the go back button
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onClickRiddle(ActionEvent event) throws IOException {
    App.setRoot(SceneUi.RIDDLE);
  }

  @Override
  protected ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {
    if (!super.chatCompletionRequest.getMessages().isEmpty()) {
      // get last 'assistant' message
      ChatMessage lastResponse = super.chatCompletionRequest.getMessages().get(1);

      // re-create messages array to remove any previous hints
      super.chatCompletionRequest.getMessages().clear();
      super.chatCompletionRequest
          .addMessage("system", GptPromptEngineering.startChat())
          .addMessage(lastResponse);
    }

    return super.runGpt(msg);
  }

  @Override
  protected Task<Void> createGptTask(ChatMessage msg) {
    // create task to run GPT in separate thread
    Task<Void> task =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            lastMsg = runGpt(msg);
            return null;
          }
        };
    return task;
  }

  @Override
  protected void updateChatTextArea(String pastEntry, Choice result) {
    enableInputText();
    super.updateChatTextArea(pastEntry, result);
  }
}
