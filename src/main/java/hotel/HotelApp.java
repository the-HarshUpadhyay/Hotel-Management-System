package hotel;

import hotel.service.BookingManager;
import hotel.ui.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * HotelApp — Main JavaFX Application Entry Point.
 *
 * Responsibilities:
 *   - Bootstrap JavaFX runtime
 *   - Create the shared BookingManager (single instance, passed to all tabs)
 *   - Assemble the TabPane-based UI
 *   - Load CSS stylesheet
 *   - Save data on window close
 *
 * Architecture: Controller / Facade pattern — HotelApp wires together
 * all UI components without containing business logic itself.
 */
public class HotelApp extends Application {

    private BookingManager manager;

    // ---- Entry Point ----

    public static void main(String[] args) {
        launch(args);
    }

    // ---- JavaFX Lifecycle ----

    @Override
    public void start(Stage primaryStage) {
        // Shared service layer (single source of truth)
        manager = new BookingManager();

        // Build UI
        BorderPane root = new BorderPane();
        root.setTop(buildHeader());
        root.setCenter(buildTabPane());

        // Scene & stylesheet
        Scene scene = new Scene(root, 1150, 780);
        try {
            String css = getClass().getResource("/hotel/styles.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (NullPointerException e) {
            System.err.println("[Warning] CSS file not found — running with default styles.");
        }

        // Stage config
        primaryStage.setTitle("🏨  Hotel Management System");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.setScene(scene);

        // Save state when window closes
        primaryStage.setOnCloseRequest(e -> {
            System.out.println("[Info] Data saved dynamically to DB. Goodbye!");
        });

        primaryStage.show();
    }

    // ----------------------------------------------------------------
    //  Header
    // ----------------------------------------------------------------

    private Region buildHeader() {
        BorderPane header = new BorderPane();
        header.setPadding(new Insets(24, 32, 24, 32));
        header.getStyleClass().add("app-header");

        // --- Left Side: Logo and Titles ---
        HBox leftBox = new HBox(20);
        leftBox.setAlignment(Pos.CENTER_LEFT);

        // Modern, auto-scaling CSS-based Logo (Glowing Tile)
        StackPane logoPane = new StackPane();
        logoPane.setPrefSize(100, 56);
        logoPane.setMinSize(100, 56);
        logoPane.setMaxSize(100, 56);
        logoPane.setStyle("-fx-background-color: transparent;");
        
        javafx.scene.image.ImageView logoView = new javafx.scene.image.ImageView();
        try {
            logoView.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("/hotel/logo.png")));
        } catch (Exception e) {}
        logoView.setFitWidth(100);
        logoView.setFitHeight(56);
        logoView.setPreserveRatio(true);
        logoPane.getChildren().add(logoView);

        // Titles
        VBox titleBox = new VBox(2);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("THE TAJ MAHAL");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("Hotel Management System  •  Front Desk Console");
        subtitle.getStyleClass().add("app-subtitle");

        titleBox.getChildren().addAll(title, subtitle);
        leftBox.getChildren().addAll(logoPane, titleBox);

        // --- Right Side: User Profile / Context Info ---
        HBox rightBox = new HBox(14);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        
        VBox userBox = new VBox(0);
        userBox.setAlignment(Pos.CENTER_RIGHT);
        Label lblUser = new Label("Administrator");
        lblUser.setStyle("-fx-text-fill: #e2e8f0; -fx-font-weight: 700; -fx-font-size: 14.5px;");
        Label lblStatus = new Label("● Online");
        lblStatus.setStyle("-fx-text-fill: #4facfe; -fx-font-weight: 700; -fx-font-size: 12px;");
        userBox.getChildren().addAll(lblUser, lblStatus);

        StackPane avatar = new StackPane();
        avatar.setPrefSize(44, 44);
        avatar.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 22; -fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 22;");
        Label avatarText = new Label("👨‍💼");
        avatarText.setStyle("-fx-font-size: 22px;");
        avatar.getChildren().add(avatarText);

        rightBox.getChildren().addAll(userBox, avatar);

        header.setLeft(leftBox);
        header.setRight(rightBox);

        return header;
    }

    // ----------------------------------------------------------------
    //  Tab Pane
    // ----------------------------------------------------------------

    private TabPane buildTabPane() {
        // Instantiate tab controllers (each builds its own JavaFX content)
        RoomTab     roomTab     = new RoomTab(manager);
        BookingTab  bookingTab  = new BookingTab(manager);
        CustomerTab customerTab = new CustomerTab(manager);

        TabPane tabPane = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hotel/ui/BillingTab.fxml"));
            ScrollPane billingContent = loader.load();
            BillingTabController billingController = loader.getController();
            billingController.init(manager);
            billingController.getTab().setContent(billingContent);

            tabPane = new TabPane(
                    roomTab.getTab(),
                    bookingTab.getTab(),
                    customerTab.getTab(),
                    billingController.getTab()
            );
        } catch (Exception e) {
            System.err.println("Failed to load FXML: " + e.getMessage());
            e.printStackTrace();
            tabPane = new TabPane(roomTab.getTab(), bookingTab.getTab(), customerTab.getTab());
        }

        tabPane.setTabMinWidth(160);
        tabPane.getStyleClass().add("main-tab-pane");

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, old, newTab) -> {
            if (newTab == bookingTab.getTab()) {
                bookingTab.refreshRoomMenu();
            }
        });

        return tabPane;
    }
}
