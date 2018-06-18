package zvi;

import zvi.Controller.MainController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    public static Stage parentWindow;

    @Override
    public void start(Stage loginStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Stage/Main.fxml"));
        Parent root = fxmlLoader.load();
        loginStage.setTitle("Image Segmentation");
        loginStage.setScene(new Scene(root, 800, 672));
        loginStage.getIcons().add(new Image("/Public/Img/icon.png"));
        loginStage.show();
        Main.parentWindow = loginStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
