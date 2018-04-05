package zvi.Controller;

import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
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

    @FXML
    public BarChart histogramChart;

    @FXML
    public ChoiceBox segmentationMethod;

    @FXML
    protected void initialize(){
        segmentationMethod.setItems(FXCollections.observableArrayList("Automatické prahování"));
        segmentationMethod.getSelectionModel().select(0);
    }

    public void FileChooser(){
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilterBMP = new FileChooser.ExtensionFilter("Soubory BMP (*.bmp)", "*.BMP");
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("Soubory JPG (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("Soubory PNG (*.png)", "*.PNG");
        fileChooser.getExtensionFilters().addAll(extFilterBMP, extFilterJPG, extFilterPNG);

        fileChooser.setTitle("Otevřít soubor");
        File loadedImage = fileChooser.showOpenDialog(Main.parentWindow);

        try {
            BufferedImage bufferedImage = ImageIO.read(loadedImage);
            LoadImage(bufferedImage);

        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.INFO, "Chyba při načítání souboru.", ex);
        }


    }

    private void LoadImage(BufferedImage bufferedImage){
        Image image = SwingFXUtils.toFXImage(bufferedImage, null);
        loadedImageView.setImage(image);

        Histogram imageHistogram = new Histogram(bufferedImage);
        drawHistogramChart(imageHistogram.getImageHistogram());
    }

    public void drawHistogramChart(int[] histogramValues){
        final XYChart.Series<String, Number> histogramSeries = new XYChart.Series<String, Number>();
        histogramChart.getData().clear();
        for(int i = 0; i < histogramValues.length; i++){
            histogramSeries.getData().add(new XYChart.Data<String, Number>(Integer.toString(i), histogramValues[i]));
        }
        histogramChart.getData().add(histogramSeries);
    }
}
