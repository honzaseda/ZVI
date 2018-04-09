package zvi.Controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;;
import javafx.stage.FileChooser;
import zvi.ImageProcessing.Histogram;
import zvi.ImageProcessing.ImageHandler;
import zvi.ImageProcessing.ImageSegmentation;
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
    public ImageView loadedImageView, segmentedImageView;

    @FXML
    public BarChart histogramChart;

    @FXML
    public ChoiceBox segmentationMethod;

    @FXML
    public AnchorPane optionsPane;

    @FXML
    public CheckBox automaticThreshold, filterOption;

    @FXML
    public TextField manualThresholdCount;

    @FXML
    public Button thresholdSegmentationBtn;

    private ImageHandler imageHandler;

    @FXML
    protected void initialize() {
        segmentationMethod.setItems(FXCollections.observableArrayList("Prahování", "Matice sousednosti", "Rozplavování"));
        segmentationMethod.getSelectionModel().select(0);
        automaticThreshold.setSelected(true);
        manualThresholdCount.setDisable(true);

        segmentationMethod.getSelectionModel().selectedIndexProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        drawMethodOptions(newValue.intValue());
                    }
                }
        );
    }

    private void drawMethodOptions(int selectedMethod){
        optionsPane.setVisible(false);
        switch (selectedMethod) {
            case 0:
                optionsPane.setVisible(true);
                break;
            case 1:
                break;
            case 2:
                break;
        }
    }

    public void FileChooser() {
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilterBMP = new FileChooser.ExtensionFilter("Soubory BMP (*.bmp)", "*.BMP");
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("Soubory JPG (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("Soubory PNG (*.png)", "*.PNG");
        fileChooser.getExtensionFilters().addAll(extFilterBMP, extFilterJPG, extFilterPNG);

        fileChooser.setTitle("Otevřít soubor");
        File loadedFile = fileChooser.showOpenDialog(Main.parentWindow);

        try {
            if(loadedFile != null) {
                BufferedImage bufferedImage = ImageIO.read(loadedFile);
                imageHandler = new ImageHandler(bufferedImage);

                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                loadedImageView.setImage(image);
            }
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.INFO, "Chyba při načítání souboru.", ex);
        }
    }

    public void AutoSegmentation() {
        Histogram imageHistogram = new Histogram(imageHandler, filterOption.isSelected());
        drawHistogramChart(imageHistogram.getImageHistogram());

//        loadedImage.AutoThresholdSegmentation();
    }

    private void drawHistogramChart(int[] histogramValues) {
        final XYChart.Series<String, Number> histogramSeries = new XYChart.Series<String, Number>();
        histogramChart.getData().clear();
        for (int i = 0; i < histogramValues.length; i++) {
            histogramSeries.getData().add(new XYChart.Data<String, Number>(Integer.toString(i), histogramValues[i]));
        }
        histogramChart.getData().add(histogramSeries);
    }
}
