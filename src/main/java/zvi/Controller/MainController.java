package zvi.Controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import zvi.ImageProcessing.Histogram;
import zvi.Main;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController {

    @FXML
    public MenuItem fileOpen;

    @FXML
    public ImageView loadedImageView;

    public void FileChooser(){
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilterBMP = new FileChooser.ExtensionFilter("BMP files (*.bmp)", "*.BMP");
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        fileChooser.getExtensionFilters().addAll(extFilterBMP, extFilterJPG, extFilterPNG);

        fileChooser.setTitle("Open Resource File");
        File loadedImage = fileChooser.showOpenDialog(Main.parentWindow);

        try {
            BufferedImage bufferedImage = ImageIO.read(loadedImage);
//            convertToGrayScale(bufferedImage);
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            loadedImageView.setImage(image);

            Histogram imageHistogram = new Histogram(bufferedImage);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }


    }
}
