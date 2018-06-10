package zvi.Controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import zvi.ImageProcessing.MatrixSegmentation;
import zvi.ImageProcessing.ThresholdSegmentation;
import zvi.ImageProcessing.ImageHandler;
import zvi.Main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController {

    @FXML
    public MenuItem fileOpen;

    @FXML
    public ImageView loadedImageView, segmentedImageView;

    @FXML
    public AreaChart histogramChart;

    @FXML
    public ChoiceBox segmentationMethod;

    @FXML
    public AnchorPane manualThresholdOptions, matrixOptions, automaticThresholdOptions;

    @FXML
    public CheckBox filterOption, normalizeOption;

    @FXML
    public TextField manualThresholdValue, thresholdVicinity, thresholdDistance, matrixSegments;

    @FXML
    public Button createHistogram, matrixSegmentationBtn;

    @FXML
    public ToggleGroup neighbours;

    @FXML
    public RadioButton fourNeighbours, eightNeighbours;

    @FXML
    public Label manualThresholdError, detectedThresholds, manualThresholds;

    private BufferedImage loadedImage;
    private ThresholdSegmentation thresholdSegmentation;
    private MatrixSegmentation matrixSegmentation;

    @FXML
    protected void initialize() {
        segmentationMethod.setItems(FXCollections.observableArrayList("Automatické Prahování", "Ruční prahování", "Přebarvování", "Rozplavování"));
        segmentationMethod.getSelectionModel().select(0);

        fourNeighbours.setUserData("four");
        eightNeighbours.setUserData("eight");


        //-- Events

        loadedImageView.setOnMouseClicked(event -> {
            if (loadedImage != null) {
                BufferedImage bufferedImage = new BufferedImage((int) loadedImageView.getImage().getWidth(), (int) loadedImageView.getImage().getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                openImageWindow("original", loadedImageView.getImage(), loadedImage);

            }
        });

        segmentedImageView.setOnMouseClicked(event -> {
            if (matrixSegmentation != null || thresholdSegmentation != null) {
                BufferedImage bufferedImage = new BufferedImage((int) segmentedImageView.getImage().getWidth(), (int) segmentedImageView.getImage().getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                openImageWindow("segmented", segmentedImageView.getImage(), bufferedImage);
            }
        });
        segmentationMethod.getSelectionModel().selectedIndexProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        drawMethodOptions(newValue.intValue());
                    }
                }
        );
    }

    private void drawMethodOptions(int selectedMethod) {
        automaticThresholdOptions.setVisible(false);
        manualThresholdOptions.setVisible(false);
        matrixOptions.setVisible(false);
        switch (selectedMethod) {
            case 0:
                automaticThresholdOptions.setVisible(true);
                break;
            case 1:
                manualThresholdOptions.setVisible(true);
                break;
            case 2:
                matrixOptions.setVisible(true);
                break;
            case 3:
                break;
        }
    }

    public void fileChooser() {
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilterBMP = new FileChooser.ExtensionFilter("Soubory BMP (*.bmp)", "*.BMP");
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("Soubory JPG (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("Soubory PNG (*.png)", "*.PNG");
        fileChooser.getExtensionFilters().addAll(extFilterBMP, extFilterJPG, extFilterPNG);

        fileChooser.setTitle("Otevřít soubor");
        File loadedFile = fileChooser.showOpenDialog(Main.parentWindow);

        try {
            if (loadedFile != null) {
                loadedImage = ImageIO.read(loadedFile);

                Image image = SwingFXUtils.toFXImage(ImageHandler.getGrayScaleImage(loadedImage), null);
                loadedImageView.setImage(image);
                System.out.println("created new image handler from " + loadedImage.toString());
            }
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.INFO, "Chyba při načítání souboru.", ex);
        }
    }

    private void openImageWindow(String name, Image image, BufferedImage bufferedImage) {
        try {
            File f = new File(name + "Image.jpg");
            ImageIO.write(SwingFXUtils.fromFXImage(image, bufferedImage), "png", f);
            Desktop dt = Desktop.getDesktop();
            dt.open(f);
        } catch (IOException e) {
            //empty try catch lul
        }
    }

    public void createHistogram() {
        thresholdSegmentation = new ThresholdSegmentation(loadedImage, filterOption.isSelected());
        drawHistogramChart(thresholdSegmentation.getImageHistogram());
        histogramChart.setVisible(true);
    }

    private void drawHistogramChart(int[] histogramValues) {


        final XYChart.Series<String, Number> histogramSeries = new XYChart.Series<String, Number>();
        histogramChart.getData().clear();
        for (int i = 0; i < histogramValues.length; i++) {
            histogramSeries.getData().add(new XYChart.Data<String, Number>(Integer.toString(i), histogramValues[i]));
        }
        histogramChart.setData(FXCollections.observableArrayList(histogramSeries));
    }

    public void matrixSegmentation() {
        histogramChart.setVisible(false);
        boolean useAllNeighbours = true;

        if (neighbours.getSelectedToggle() != null) {
            if (neighbours.getSelectedToggle().getUserData().toString().equals("four")) {
                useAllNeighbours = false;
            }
        }
//        System.out.println(useAllNeighbours);
        matrixSegmentation = new MatrixSegmentation(loadedImage, useAllNeighbours, 3);
        Image image = SwingFXUtils.toFXImage(matrixSegmentation.getSegmentedImage(), null);
        segmentedImageView.setImage(image);
    }

    @FXML
    public void manualSegmentation() {
        manualThresholdError.setVisible(false);
        if (manualThresholdValue.getText() != null && !manualThresholdValue.getText().isEmpty()) {
            thresholdSegmentation.setThreshold(Integer.parseInt(manualThresholdValue.getText()));
            Image image = SwingFXUtils.toFXImage(thresholdSegmentation.segmentation(), null);
            segmentedImageView.setImage(image);
        } else {
            manualThresholdError.setText("Nebyl zadán práh segmentace");
            manualThresholdError.setVisible(true);
        }

    }

    @FXML
    public void addManualThreshold(){
        thresholdSegmentation.addThreshold(Integer.parseInt(manualThresholdValue.getText()));
        manualThresholds.setText(thresholdSegmentation.getThresholds().toString());
    }

    @FXML
    public void removeManualThreshold(){
        thresholdSegmentation.removeThreshold(Integer.parseInt(manualThresholdValue.getText()));
        manualThresholds.setText(thresholdSegmentation.getThresholds().toString());
    }

    public void automaticSegmentation() {
        ArrayList<Integer> thresholds = thresholdSegmentation.findThresholds(Integer.parseInt(thresholdVicinity.getText()), Integer.parseInt(thresholdDistance.getText()));
        String asdf = "";
        for (int t : thresholds) {
            asdf = asdf + ", " + t;
        }
        detectedThresholds.setText(asdf);
    }
}
