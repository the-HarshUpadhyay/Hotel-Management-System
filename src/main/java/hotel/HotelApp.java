package hotel;

import hotel.config.AppText;
import hotel.service.BookingManager;
import hotel.ui.BillingTabController;
import hotel.ui.BookingTab;
import hotel.ui.CustomerTab;
import hotel.ui.RoomTab;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HotelApp extends Application {

    private static final double SIDEBAR_EXPANDED_WIDTH = 260;
    private static final double SIDEBAR_COLLAPSED_WIDTH = 88;

    private final List<NavEntry> navEntries = new ArrayList<>();
    private final AppText text = AppText.get();

    private BookingManager manager;
    private StackPane contentHost;
    private VBox sidebar;
    private Label workspaceTitle;
    private Label workspaceSubtitle;
    private Label brandWordmark;
    private Label brandSupport;
    private Label sidebarSectionLabel;
    private boolean sidebarExpanded = true;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        manager = new BookingManager();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-shell");

        contentHost = new StackPane();
        contentHost.getStyleClass().add("workspace-content");

        VBox centerPane = new VBox(20, buildWorkspaceHeader(), contentHost);
        centerPane.setPadding(new Insets(22, 24, 24, 0));
        VBox.setVgrow(contentHost, Priority.ALWAYS);

        root.setTop(buildHeader());
        root.setLeft(buildSidebar());
        root.setCenter(centerPane);

        initializeNavigation();

        Scene scene = new Scene(root, 1360, 860);
        String css = getClass().getResource("/hotel/styles.css").toExternalForm();
        scene.getStylesheets().add(css);

        primaryStage.setTitle(text.text("APP", "window_title", "Hotel Management System"));
        primaryStage.getIcons().add(new Image(getClass().getResource("/hotel/logo.png").toExternalForm()));
        primaryStage.setMinWidth(1080);
        primaryStage.setMinHeight(720);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Node buildHeader() {
        BorderPane header = new BorderPane();
        header.getStyleClass().add("top-bar");
        header.setPadding(new Insets(20, 28, 20, 28));

        HBox brandRow = new HBox(16);
        brandRow.setAlignment(Pos.CENTER_LEFT);

        VBox brandBox = new VBox(4);
        brandBox.setAlignment(Pos.CENTER_LEFT);

        Label eyebrow = new Label(text.text("HEADER", "brand_eyebrow", "TAJ MAHAL OPERATIONS"));
        eyebrow.getStyleClass().add("brand-eyebrow");

        brandWordmark = new Label(text.text("HEADER", "brand_wordmark_expanded", "Hotel Command Center"));
        brandWordmark.getStyleClass().add("brand-wordmark");

        brandSupport = new Label(text.text("HEADER", "brand_support",
                "Modern front desk workspace for rooms, guests, bookings, and billing"));
        brandSupport.getStyleClass().add("brand-support");
        brandBox.getChildren().addAll(eyebrow, brandWordmark, brandSupport);
        brandRow.getChildren().addAll(buildLogoBadge(58, false), brandBox);

        HBox presenceBox = new HBox(14);
        presenceBox.setAlignment(Pos.CENTER_RIGHT);

        VBox statusBox = new VBox(4);
        statusBox.setAlignment(Pos.CENTER_RIGHT);

        Label currentDate = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        currentDate.getStyleClass().add("meta-label");

        Label operator = new Label(text.text("HEADER", "operator_role", "Administrator"));
        operator.getStyleClass().add("operator-name");

        Label presence = new Label(text.text("HEADER", "presence_status", "\u25cf Online"));
        presence.getStyleClass().add("status-pill");
        statusBox.getChildren().addAll(currentDate, operator, presence);

        StackPane avatar = buildAdministratorBadge(48);

        presenceBox.getChildren().addAll(statusBox, avatar);

        header.setLeft(brandRow);
        header.setRight(presenceBox);
        return header;
    }

    private Node buildWorkspaceHeader() {
        BorderPane workspaceBar = new BorderPane();
        workspaceBar.getStyleClass().add("workspace-bar");
        workspaceBar.setPadding(new Insets(0, 0, 0, 24));

        VBox titleBox = new VBox(4);
        workspaceTitle = new Label();
        workspaceTitle.getStyleClass().add("workspace-title");
        workspaceSubtitle = new Label();
        workspaceSubtitle.getStyleClass().add("workspace-subtitle");
        titleBox.getChildren().addAll(workspaceTitle, workspaceSubtitle);

        workspaceBar.setLeft(titleBox);
        return workspaceBar;
    }

    private Node buildSidebar() {
        sidebar = new VBox(10);
        sidebar.getStyleClass().addAll("sidebar", "expanded");
        sidebar.setPadding(new Insets(20, 14, 20, 14));
        sidebar.setPrefWidth(SIDEBAR_EXPANDED_WIDTH);
        sidebar.setMinWidth(Region.USE_PREF_SIZE);
        sidebar.setMaxWidth(Region.USE_PREF_SIZE);

        sidebarSectionLabel = new Label(text.text("SIDEBAR", "section_label", "Workspace"));
        sidebarSectionLabel.getStyleClass().add("sidebar-section-label");

        Button toggleButton = new Button();
        toggleButton.getStyleClass().addAll("sidebar-toggle", "logo-toggle-button");
        toggleButton.setOnAction(event -> toggleSidebar());
        updateToggleButton(toggleButton);

        BorderPane sidebarHeader = new BorderPane();
        sidebarHeader.getStyleClass().add("sidebar-header");
        sidebarHeader.setLeft(sidebarSectionLabel);
        sidebarHeader.setRight(toggleButton);

        VBox navButtonBox = new VBox(8);
        navButtonBox.getStyleClass().add("nav-button-box");
        VBox.setVgrow(navButtonBox, Priority.ALWAYS);

        sidebar.getChildren().addAll(sidebarHeader, navButtonBox);
        sidebar.setUserData(navButtonBox);
        return sidebar;
    }

    private void initializeNavigation() {
        RoomTab roomTab = new RoomTab(manager);
        BookingTab bookingTab = new BookingTab(manager);
        CustomerTab customerTab = new CustomerTab(manager);

        BillingTabController billingController;
        Node billingView;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hotel/ui/BillingTab.fxml"));
            billingView = loader.load();
            billingController = loader.getController();
            billingController.init(manager);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load billing view", ex);
        }

        navEntries.clear();
        navEntries.add(new NavEntry(
                text.text("WORKSPACES", "rooms_title", "Rooms"),
                text.text("WORKSPACES", "rooms_subtitle", "Inventory, pricing, and availability controls"),
                "data/icons/rooms.png",
                roomTab.getTab().getContent(),
                () -> { }
        ));
        navEntries.add(new NavEntry(
                text.text("WORKSPACES", "bookings_title", "Bookings"),
                text.text("WORKSPACES", "bookings_subtitle", "Assign available rooms and manage active stays"),
                "data/icons/booking.png",
                bookingTab.getTab().getContent(),
                bookingTab::refreshRoomMenu
        ));
        navEntries.add(new NavEntry(
                text.text("WORKSPACES", "customers_title", "Customers"),
                text.text("WORKSPACES", "customers_subtitle", "Search guest history and active reservation records"),
                "data/icons/customer.png",
                customerTab.getTab().getContent(),
                () -> { }
        ));
        navEntries.add(new NavEntry(
                text.text("WORKSPACES", "billing_title", "Billing"),
                text.text("WORKSPACES", "billing_subtitle", "Generate invoices and close out occupied rooms"),
                "data/icons/billing.png",
                billingView,
                billingController::refreshData
        ));

        VBox navButtonBox = (VBox) sidebar.getUserData();
        navButtonBox.getChildren().clear();

        for (NavEntry entry : navEntries) {
            Button button = buildNavButton(entry);
            entry.button = button;
            navButtonBox.getChildren().add(button);
        }

        selectEntry(navEntries.getFirst());
    }

    private Button buildNavButton(NavEntry entry) {
        StackPane iconLabel = new StackPane(createSidebarIcon(entry.icon));
        iconLabel.getStyleClass().add("nav-icon-wrap");

        Label titleLabel = new Label(entry.title);
        titleLabel.getStyleClass().add("nav-title");

        Label subtitleLabel = new Label(entry.subtitle);
        subtitleLabel.getStyleClass().add("nav-subtitle");

        VBox textBox = new VBox(2, titleLabel, subtitleLabel);
        textBox.getStyleClass().add("nav-text-box");

        HBox content = new HBox(14, iconLabel, textBox);
        content.setAlignment(Pos.CENTER_LEFT);

        Button button = new Button();
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setGraphic(content);
        button.setOnAction(event -> selectEntry(entry));
        HBox.setHgrow(textBox, Priority.ALWAYS);

        entry.iconLabel = iconLabel;
        entry.titleLabel = titleLabel;
        entry.subtitleLabel = subtitleLabel;
        entry.textBox = textBox;
        return button;
    }

    private void selectEntry(NavEntry entry) {
        for (NavEntry candidate : navEntries) {
            candidate.button.getStyleClass().remove("active");
        }

        entry.button.getStyleClass().add("active");
        workspaceTitle.setText(entry.title);
        workspaceSubtitle.setText(entry.subtitle);
        contentHost.getChildren().setAll(entry.content);
        entry.onSelect.run();
    }

    private void toggleSidebar() {
        sidebarExpanded = !sidebarExpanded;

        if (sidebarExpanded) {
            sidebar.getStyleClass().remove("collapsed");
            if (!sidebar.getStyleClass().contains("expanded")) {
                sidebar.getStyleClass().add("expanded");
            }
            sidebar.setPrefWidth(SIDEBAR_EXPANDED_WIDTH);
        } else {
            sidebar.getStyleClass().remove("expanded");
            if (!sidebar.getStyleClass().contains("collapsed")) {
                sidebar.getStyleClass().add("collapsed");
            }
            sidebar.setPrefWidth(SIDEBAR_COLLAPSED_WIDTH);
        }

        BorderPane sidebarHeader = (BorderPane) sidebar.getChildren().getFirst();
        sidebarSectionLabel.setVisible(sidebarExpanded);
        sidebarSectionLabel.setManaged(sidebarExpanded);

        brandWordmark.setText(sidebarExpanded
                ? text.text("HEADER", "brand_wordmark_expanded", "Hotel Command Center")
                : text.text("HEADER", "brand_wordmark_collapsed", "Hotel CC"));
        brandSupport.setVisible(sidebarExpanded);
        brandSupport.setManaged(sidebarExpanded);

        for (NavEntry entry : navEntries) {
            entry.textBox.setVisible(sidebarExpanded);
            entry.textBox.setManaged(sidebarExpanded);
            entry.button.setAlignment(sidebarExpanded ? Pos.CENTER_LEFT : Pos.CENTER);
        }

        Button toggleButton = (Button) sidebarHeader.getRight();
        updateToggleButton(toggleButton);
    }

    private void updateToggleButton(Button toggleButton) {
        toggleButton.setGraphic(buildToggleBadge(sidebarExpanded ? 42 : 38));
        toggleButton.setAlignment(Pos.CENTER);
        toggleButton.setText(null);
    }

    private StackPane buildLogoBadge(double size, boolean compact) {
        StackPane badge = new StackPane();
        badge.getStyleClass().add(compact ? "logo-badge-compact" : "logo-badge");
        badge.setPrefSize(size, size);
        badge.setMinSize(size, size);
        badge.setMaxSize(size, size);

        ImageView logoView = new ImageView(loadBrandImage());
        logoView.getStyleClass().add("app-logo");
        logoView.setPreserveRatio(true);
        logoView.setFitWidth(size - (compact ? 14 : 16));
        logoView.setFitHeight(size - (compact ? 14 : 16));
        badge.getChildren().add(logoView);
        return badge;
    }

    private StackPane buildToggleBadge(double size) {
        StackPane badge = new StackPane();
        badge.getStyleClass().add("logo-badge-compact");
        badge.setPrefSize(size, size);
        badge.setMinSize(size, size);
        badge.setMaxSize(size, size);

        String toggleIcon = sidebarExpanded ? "collapse.png" : "expand.png";
        ImageView iconView = new ImageView(new Image(Path.of("data", "icons", toggleIcon).toUri().toString()));
        iconView.getStyleClass().add("collapse-icon");
        iconView.setPreserveRatio(true);
        iconView.setFitWidth(size - 16);
        iconView.setFitHeight(size - 16);
        badge.getChildren().add(iconView);
        return badge;
    }

    private StackPane buildAdministratorBadge(double size) {
        StackPane badge = new StackPane();
        badge.getStyleClass().add("operator-avatar");
        badge.setPrefSize(size, size);
        badge.setMinSize(size, size);
        badge.setMaxSize(size, size);

        Path administratorImage = Path.of("data", "icons", "administrator.png");
        if (Files.exists(administratorImage)) {
            ImageView avatarView = new ImageView(new Image(administratorImage.toUri().toString()));
            avatarView.setPreserveRatio(true);
            avatarView.setFitWidth(size - 8);
            avatarView.setFitHeight(size - 8);
            badge.getChildren().add(avatarView);
            return badge;
        }

        Label fallback = new Label(text.text("HEADER", "operator_avatar", "AD"));
        fallback.getStyleClass().add("operator-avatar");
        badge.getChildren().add(fallback);
        return badge;
    }

    private ImageView createSidebarIcon(String pathContent) {
        ImageView icon = new ImageView(new Image(Path.of(pathContent).toUri().toString()));
        icon.getStyleClass().add("sidebar-icon");
        icon.setFitWidth(20);
        icon.setFitHeight(20);
        icon.setPreserveRatio(true);
        return icon;
    }

    private Image loadBrandImage() {
        Path[] customLogos = {
                Path.of("data", "logo.png"),
                Path.of("data", "unnamed.webp")
        };
        for (Path customLogo : customLogos) {
            try {
                if (Files.exists(customLogo)) {
                    return new Image(customLogo.toUri().toString());
                }
            } catch (Exception ignored) {
            }
        }

        try {
            return new Image(getClass().getResource("/hotel/logo.png").toExternalForm());
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load application logo.", ex);
        }
    }

    private static final class NavEntry {
        private final String title;
        private final String subtitle;
        private final String icon;
        private final Node content;
        private final Runnable onSelect;

        private Button button;
        private StackPane iconLabel;
        private Label titleLabel;
        private Label subtitleLabel;
        private VBox textBox;

        private NavEntry(String title, String subtitle, String icon, Node content, Runnable onSelect) {
            this.title = title;
            this.subtitle = subtitle;
            this.icon = icon;
            this.content = content;
            this.onSelect = onSelect;
        }
    }
}
