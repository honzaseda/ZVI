package zvi.Controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import zvi.ImageProcessing.ImageHandler;
import zvi.ImageProcessing.MatrixSegmentation;
import zvi.ImageProcessing.ThresholdSegmentation;
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
    public ChoiceBox segmentationMethod;

    @FXML
    public AnchorPane manualThresholdOptions, recoloringOptions, automaticThresholdOptions, sequentialOptions, manualThresholdField, histogramPane, oneDirectionOptions;

    @FXML
    public CheckBox automaticFilterOption, manualFilterOption, automaticReferenceBrightness;

    @FXML
    public TextField manualThresholdValue, thresholdSegments, matrixSegments, referenceBrightness, sequentialDepth;

    @FXML
    public Button createHistogram, matrixSegmentationBtn, manualSegmentationBtn;

    @FXML
    public ToggleGroup neighbours, direction, oneDirectionOption;

    @FXML
    public RadioButton fourNeighbours, eightNeighbours, bothDirection, oneDirection, oneDirectionBrighten, oneDirectionDarken;

    @FXML
    public Label manualThresholdError, detectedThresholds, manualThresholds, reportDialog;

    @FXML
    public ListView manualThresholdList, automaticThresholdList;

    private String fileName;
    private BufferedImage loadedImage;
    private ThresholdSegmentation thresholdSegmentation;
    private MatrixSegmentation matrixSegmentation;
    private ObservableList<Integer> manualObservableList = FXCollections.observableArrayList();
    private ObservableList<Integer> automaticObservableList = FXCollections.observableArrayList();

    @FXML
    protected void initialize() {
        segmentationMethod.setItems(FXCollections.observableArrayList("Automatické Prahování", "Ruční prahování", "Přebarvování", "Rozplavování"));
        segmentationMethod.getSelectionModel().select(0);

        fourNeighbours.setUserData("four");
        eightNeighbours.setUserData("eight");

        bothDirection.setUserData("both");
        oneDirection.setUserData("one");

        oneDirectionBrighten.setUserData("brighten");
        oneDirectionDarken.setUserData("darken");


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

        automaticReferenceBrightness.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                referenceBrightness.setDisable(newValue);
            }
        });

        direction.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if(newValue == oneDirection){
                    oneDirectionOptions.setVisible(true);
                }
                else {
                    oneDirectionOptions.setVisible(false);
                }
            }
        });

        manualObservableList.addListener(new ListChangeListener() {

            @Override
            public void onChanged(ListChangeListener.Change change) {
                if (manualObservableList.size() > 0) {
                    manualSegmentationBtn.setDisable(false);
                } else {
                    manualSegmentationBtn.setDisable(true);
                }
            }
        });


    }

    private void drawMethodOptions(int selectedMethod) {
        automaticThresholdOptions.setVisible(false);
        manualThresholdOptions.setVisible(false);
        recoloringOptions.setVisible(false);
        sequentialOptions.setVisible(false);
        switch (selectedMethod) {
            case 0:
                automaticThresholdOptions.setVisible(true);
                break;
            case 1:
                manualThresholdOptions.setVisible(true);
                break;
            case 2:
                recoloringOptions.setVisible(true);
                break;
            case 3:
                sequentialOptions.setVisible(true);
                break;
            default:
                automaticThresholdOptions.setVisible(true);
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
                fileName = loadedFile.getName();
                Image image = SwingFXUtils.toFXImage(loadedImage, null);
                loadedImageView.setImage(image);

                System.out.println("Otevřen obázek " + fileName + " ze zdroje: " + loadedImage.toString());
                reportDialog.setText("Otevřen obrázek " + fileName);
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
            System.out.println("Nelze zobrazit náhled obrázku. Nepodařilo se otevřít systémový prohlížeč obrázků.");
            reportDialog.setText("Nelze zobrazit náhled obrázku. Nepodařilo se otevřít systémový prohlížeč obrázků");
        }
    }

    public void saveSegmentedFile() {
        if (segmentedImageView.getImage() != null) {
            FileChooser fileChooser = new FileChooser();

            FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("Soubory PNG (*.png)", "*.png");
            fileChooser.getExtensionFilters().addAll(extFilterPNG);

            fileChooser.setTitle("Uložit segmentovaný obraz");
            fileChooser.setInitialFileName("segmented-" + fileName);
            File saveFile = fileChooser.showSaveDialog(Main.parentWindow);
            if (saveFile != null) {
                try {
                    System.out.println("Ukládám obrázek");

                    BufferedImage saveImage = SwingFXUtils.fromFXImage(segmentedImageView.getImage(),null);
                    ImageIO.write( saveImage, "png", saveFile);
                } catch (IOException ex) {
                    System.out.println("Nepodařilo se uložit segmentovaný obraz.");
                    reportDialog.setText("Nepodařilo se uložit segmentovaný obraz.");
                }
            }
        } else {
            reportDialog.setText("Není segmentován žádný obrázek");
        }
    }

    public void createHistogram() {
        if (loadedImageView.getImage() != null) {
            boolean filter = true;
            if(manualThresholdOptions.isVisible()){
                filter = manualFilterOption.isSelected();
            }
            else if(automaticThresholdOptions.isVisible()) {
                filter = automaticFilterOption.isSelected();
            }
            manualThresholdList.setItems(null);
            automaticThresholdList.setItems(null);
            thresholdSegmentation = new ThresholdSegmentation(loadedImage, filter);
            drawHistogramChart(thresholdSegmentation.getImageHistogram());
            segmentedImageView.setImage(null);
        } else {
            System.out.println("Nelze vytvořit histogram. Není načten výchozí obrázek.");
            reportDialog.setText("Nelze vytvořit histogram. Není načten výchozí obrázek.");
        }
    }

    private void drawHistogramChart(int[] histogramValues) {

        final NumberAxis xAxis = new NumberAxis(0, 255, 20);
        final NumberAxis yAxis = new NumberAxis();
        AreaChart<Number, Number> histogram = new AreaChart<Number, Number>(xAxis, yAxis);
        histogram.setCreateSymbols(false);
        histogram.prefHeight(200);
        histogram.prefWidth(500);
        histogram.setMaxHeight(200);
        xAxis.setLabel(null);
        histogram.setLegendVisible(false);

        xAxis.setLabel("jas");
        yAxis.setLabel("četnost");

        final XYChart.Series<Number, Number> histogramSeries = new XYChart.Series<Number, Number>();
        for (int i = 0; i < histogramValues.length; i++) {
            histogramSeries.getData().add(new XYChart.Data<Number, Number>(i, histogramValues[i]));
        }
        histogram.setData(FXCollections.observableArrayList(histogramSeries));


        histogramPane.getChildren().set(0, histogram);

        //ZOOMING

        final double SCALE_DELTA = 1.1;
        histogram.setOnScroll(new EventHandler<ScrollEvent>() {
            public void handle(ScrollEvent event) {
                event.consume();

                if (event.getDeltaY() == 0) {
                    return;
                }

                double scaleFactor = (event.getDeltaY() > 0) ? SCALE_DELTA : 1 / SCALE_DELTA;
                yAxis.setAutoRanging(false);
                yAxis.setLowerBound(0);
                yAxis.setUpperBound(yAxis.getUpperBound() * scaleFactor);
                yAxis.setTickUnit(yAxis.getUpperBound() / 20);
//                histogramChart.setScaleY(histogramChart.getScaleY() * scaleFactor);

            }
        });

        histogram.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    yAxis.setAutoRanging(true);
//                    histogramChart.setScaleX(1.0);
                }
            }
        });
    }

    public void recoloringSegmentation() {
        if (loadedImageView.getImage() != null) {
            segmentedImageView.setImage(null);
//        histogramChart.setVisible(false);
            try {
                histogramPane.getChildren().set(0, new Pane());
            } catch (IndexOutOfBoundsException e) {

            }
            boolean useAllNeighbours = true;

            if (neighbours.getSelectedToggle() != null) {
                if (neighbours.getSelectedToggle().getUserData().toString().equals("four")) {
                    useAllNeighbours = false;
                }
            }


            int segments = 3;
            try {
                if (Integer.parseInt(matrixSegments.getText()) >= 2) {
                    segments = Integer.parseInt(matrixSegments.getText());
                } else {
                    System.out.println("Neplatný parametr. Bude použito základní nastavení na 3 segmenty.");
                    reportDialog.setText("Neplatný parametr. Bude použito základní nastavení na 3 segmenty.");
                }
            } catch (Exception e) {

            }

            System.out.println("Probíhá segmentace přebarvováním. Počet segmentů: " + segments);
            reportDialog.setText("Probíhá segmentace přebarvováním. Počet segmentů: " + segments);
            matrixSegmentation = new MatrixSegmentation(loadedImage, segments);
            matrixSegmentation.createCoocurencyMatrix(useAllNeighbours);
            matrixSegmentation.recoloringSegmentation();
//            Image image = SwingFXUtils.toFXImage(ImageHandler.getGrayScaleImage(loadedImage), null);
            Image image = SwingFXUtils.toFXImage(matrixSegmentation.getSegmentedImage(), null);
            segmentedImageView.setImage(image);
        } else {
            System.out.println("Nelze spustit segmentaci. Není načten výchozí obrázek.");
            reportDialog.setText("Nelze spustit segmentaci. Není načten výchozí obrázek.");
        }
    }

    @FXML
    public void manualSegmentation() {
        if (thresholdSegmentation.getThresholds() != null) {
            reportDialog.setText("Probíhá segmentace ručním prahováním.");
            Image image = SwingFXUtils.toFXImage(thresholdSegmentation.segmentation(), null);
            segmentedImageView.setImage(image);
        } else {
            reportDialog.setText("Nebyl zadán práh segmentace");
        }

    }

    @FXML
    public void addManualThreshold() {
        try {
            try {
                int threshold = Integer.parseInt(manualThresholdValue.getText());
                if (threshold >= 0 && threshold <= 255) {
                    thresholdSegmentation.addThreshold(Integer.parseInt(manualThresholdValue.getText()));
                    manualObservableList.setAll(thresholdSegmentation.getThresholds());
                    FXCollections.sort(manualObservableList);
                    manualThresholdList.setItems(manualObservableList);
                } else {
                    System.out.println("Nelze přidat práh, neplatná hodnota.");
                    reportDialog.setText("Nelze přidat práh, neplatná hodnota.");
                }
            } catch (Exception e) {
                System.out.println("Nelze přidat práh, neplatná hodnota.");
                reportDialog.setText("Nelze přidat práh, neplatná hodnota.");
            }

        } catch (NullPointerException e) {
            System.out.println("Nelze přidat práh, nebyl vytvořen histogram.");
            reportDialog.setText("Nelze přidat práh, nebyl vytvořen histogram.");
        }
    }

    @FXML
    public void removeManualThreshold() {
        try {
            thresholdSegmentation.removeThreshold(Integer.parseInt(manualThresholdValue.getText()));
            manualObservableList.setAll(thresholdSegmentation.getThresholds());
            FXCollections.sort(manualObservableList);
            manualThresholdList.setItems(manualObservableList);
        } catch (NullPointerException e) {
            System.out.println("Nelze odebrat práh, nebyl vytvořen histogram.");
            reportDialog.setText("Nelze odebrat práh, nebyl vytvořen histogram");
        } catch (NumberFormatException e){

        }
    }

    public void automaticSegmentation() {
        int segments = Integer.parseInt(thresholdSegments.getText());
        int epsilon = ImageHandler.getEpsilon();
        int distance = 256 / segments;
        ArrayList<Integer> t;
        boolean smallerVicinity = false;
        reportDialog.setText("Probíhá segmentace automatickým prahováním. Počet zadaných segmentů: " + segments);
        do {
            t = thresholdSegmentation.findThresholds(epsilon, distance);

            distance = distance - 10;
            if (distance < 10) {
                if (!smallerVicinity) {
                    epsilon = epsilon / 2;
                    distance = 256 / segments;
                    smallerVicinity = true;
                } else {
                    break;
                }
            }
        } while (t.size() != segments - 1);
        if (t.size() == 0) {
            System.out.println("V histogramu obrázku nebyl nalezen žádný možný práh.");
            reportDialog.setText("V histogramu obrázku nebyl nalezen žádný možný práh.");
        } else {
            if (t.size() != segments - 1) {
                System.out.println("Pro zadaný počet segmentů se nepodařilo nalézt prahy. Nalezený počet segmentů: " + (t.size() + 1));
                reportDialog.setText("Pro zadaný počet segmentů se nepodařilo nalézt prahy. Nalezený počet segmentů: " + (t.size() + 1));
            }
            automaticObservableList.setAll(thresholdSegmentation.getThresholds());
            FXCollections.sort(automaticObservableList);
            automaticThresholdList.setItems(automaticObservableList);

            Image image = SwingFXUtils.toFXImage(thresholdSegmentation.segmentation(), null);
            segmentedImageView.setImage(image);
        }
    }


    public void sequentialRecolorizing() {
        if (loadedImageView.getImage() != null) {
            segmentedImageView.setImage(null);
//        histogramChart.setVisible(false);
            try {
                histogramPane.getChildren().set(0, new Pane());
            } catch (IndexOutOfBoundsException e) {

            }
            boolean useBothDirections = true;

            if (direction.getSelectedToggle() != null) {
                if (direction.getSelectedToggle().getUserData().toString().equals("one")) {
                    useBothDirections = false;
                }
            }

            int r = -1;
            int k = -1;

            try {
                if(automaticReferenceBrightness.isSelected()){
                    r = 255;
                }
                else {
                    r = Integer.parseInt(referenceBrightness.getText());
                }
                k = Integer.parseInt(sequentialDepth.getText());
            } catch (Exception e) {
                System.out.println("Zadané neplatné parametry rozplavu.");
                reportDialog.setText("Zadané neplatné parametry rozplavu.");
            }



            if (r >= 0 && r <= 255 && k >= 0 && k <= 255) {
                System.out.println("Probíhá segmentace rozplavováním.");
                reportDialog.setText("Probíhá segmentace rozplavováním.");
                matrixSegmentation = new MatrixSegmentation(loadedImage, 0);

                if(!useBothDirections){
                    matrixSegmentation.setBrighten(oneDirectionOption.getSelectedToggle().getUserData().toString().equals("brighten"));
                }
                matrixSegmentation.sequentialRecoloringSegmentation(useBothDirections, r, k, automaticReferenceBrightness.isSelected());

                Image image = SwingFXUtils.toFXImage(matrixSegmentation.getSegmentedImage(), null);
                segmentedImageView.setImage(image);
            } else {
                System.out.println("Zadané neplatné parametry rozplavu.");
                reportDialog.setText("Zadané neplatné parametry rozplavu.");
            }
        } else {
            System.out.println("Nelze spustit segmentaci. Není načten výchozí obrázek.");
            reportDialog.setText("Nelze spustit segmentaci. Není načten výchozí obrázek.");
        }
    }

    @FXML
    private void openHelp() {
        Label text = new Label();
        text.setText("O programu \n\n Segmentace snímků III – automatické prahování podle tvaru histogramu pro lokální " +
                "minima a maxima na definovaný počet prahů, ověření vlastností metody s užitím filtrace histogramu, " +
                "metody založené na matici sousednosti a rozplavování (+ ruční prahování).\n");
        text.setMaxWidth(450);
        text.setWrapText(true);
        text.wrapTextProperty();
        BorderPane pane = new BorderPane();
        pane.setCenter(text);
        Scene scene = new Scene(pane);
        pane.setMinHeight(150);
        pane.setMinWidth(500);

        Stage openHelpStage = new Stage();
        openHelpStage.setScene(scene);
        openHelpStage.setMinWidth(520);
        openHelpStage.setMinHeight(180);
        openHelpStage.setTitle("O programu");

        openHelpStage.setOnCloseRequest(
                e -> {
                    e.consume();
                    openHelpStage.close();
                }
        );
        openHelpStage.showAndWait();
    }

    @FXML
    private void openSetMax() {
        VBox pane = new VBox();

        Label text = new Label("Nastavení velikosti oblasti <-e, e> v histogramu, ve kterém je jas lokálním maximem.");
        text.setWrapText(true);
        text.setPrefWidth(320);

        String defaultValue = Integer.toString(ImageHandler.getEpsilon());
        TextField textField = new TextField();
        textField.setText(defaultValue);
        textField.setMaxWidth(100);

        Button save = new Button();
        save.setText("Uložit nastavení");


        Scene scene = new Scene(pane);
        pane.setPrefWidth(320);
        pane.setPadding(new Insets(20, 20, 20, 20));
        pane.setSpacing(10);
        pane.setAlignment(Pos.CENTER);

        pane.getChildren().addAll(text, textField, save);

        Stage openSetMaxStage = new Stage();
        openSetMaxStage.setScene(scene);
        openSetMaxStage.setMinWidth(320);
        openSetMaxStage.setMinHeight(120);
        openSetMaxStage.setTitle("Nastavení hledání maxima");

        save.setOnAction(event -> {
            try {
                int max = Integer.parseInt(textField.getText());
                if (max > 1 && max < 33) {
                    ImageHandler.setEpsilon(max);
                    reportDialog.setText("Epsilon nastaveno na " + max);
                }
                else {
                    reportDialog.setText("Neplatná hodnota nastavení epsilon. povolené hodnoty 2 - 32");
                }
            } catch (Exception e) {
                System.out.println("Neplatná hodnota nastavení.");
            } finally {
                event.consume();
                openSetMaxStage.close();
            }
        });

        openSetMaxStage.setOnCloseRequest(
                e -> {
                    e.consume();
                    openSetMaxStage.close();
                }
        );
        openSetMaxStage.showAndWait();
    }
}
