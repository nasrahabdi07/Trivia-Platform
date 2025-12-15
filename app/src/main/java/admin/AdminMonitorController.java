package admin;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AdminMonitorController {

    @FXML
    private Label regUsersLabel;
    @FXML
    private Label onlineUsersLabel;
    @FXML
    private Label networkLabel;
    @FXML
    private ProgressBar memoryBar;
    @FXML
    private Label memoryLabel;
    @FXML
    private ProgressBar cpuBar;
    @FXML
    private Label cpuLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private LineChart<String, Number> activityChart;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean running = true;

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5050;
    private static final String ADMIN_SECRET = "SuperSecretAdmin2025";

    private XYChart.Series<String, Number> onlineSeries;
    private XYChart.Series<String, Number> networkSeries;

    public void initialize() {
        // Setup Chart
        onlineSeries = new XYChart.Series<>();
        onlineSeries.setName("Online Players");
        networkSeries = new XYChart.Series<>();
        networkSeries.setName("Network Traffic (KB/s)");
        activityChart.getData().addAll(java.util.Arrays.asList(onlineSeries, networkSeries));

        // Connect to server in background
        new Thread(this::connectToServer).start();
    }

    private void connectToServer() {
        try {
            Platform.runLater(() -> statusLabel.setText("Connecting..."));
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Authenticate
            out.println("AUTH_ADMIN_" + ADMIN_SECRET);

            Platform.runLater(() -> {
                statusLabel.setText("Connected");
                statusLabel.setStyle("-fx-text-fill: #34d399; -fx-font-weight: bold; -fx-font-size: 16px;");
            });

            String line;
            while (running && (line = in.readLine()) != null) {
                if (line.startsWith("STATS|")) {
                    parseStats(line);
                }
            }

        } catch (Exception e) {
            Platform.runLater(() -> {
                statusLabel.setText("Disconnected: " + e.getMessage());
                statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 14px;");
            });
            e.printStackTrace();
        }
    }

    private void parseStats(String line) {
        try {
            // STATS|Reg:123|Online:45|Mem:456/1024MB|CPU:23.5%|Net:67KB/s
            String[] parts = line.split("\\|");

            // Skip "STATS" at index 0
            String regStr = parts[1].split(":")[1];
            String onlineStr = parts[2].split(":")[1];

            String memPart = parts[3].split(":")[1]; // 456/1024MB
            String usedMemStr = memPart.split("/")[0];
            String maxMemStr = memPart.split("/")[1].replace("MB", "");

            String cpuStr = parts[4].split(":")[1].replace("%", "");
            String netStr = parts[5].split(":")[1].replace("KB/s", "");

            Platform.runLater(() -> updateUI(regStr, onlineStr, usedMemStr, maxMemStr, cpuStr, netStr));

        } catch (Exception e) {
            System.err.println("Error parsing stats: " + line);
        }
    }

    private void updateUI(String reg, String online, String usedMem, String maxMem, String cpu, String net) {
        regUsersLabel.setText(reg);
        onlineUsersLabel.setText(online);
        networkLabel.setText(net + " KB/s");

        // Memory
        double usedM = Double.parseDouble(usedMem);
        double maxM = Double.parseDouble(maxMem);
        double memProgress = usedM / maxM;
        memoryBar.setProgress(memProgress);
        memoryLabel.setText(usedMem + " / " + maxMem + " MB");
        updateBarStyle(memoryBar, memProgress);

        // CPU
        double cpuVal = Double.parseDouble(cpu);
        double cpuProgress = cpuVal / 100.0;
        cpuBar.setProgress(cpuProgress);
        cpuLabel.setText(cpu + "%");
        updateBarStyle(cpuBar, cpuProgress);

        // Chart
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        onlineSeries.getData().add(new XYChart.Data<>(time, Integer.parseInt(online)));
        networkSeries.getData().add(new XYChart.Data<>(time, Integer.parseInt(net)));

        // Keep chart clean (last 20 points)
        if (onlineSeries.getData().size() > 20) {
            onlineSeries.getData().remove(0);
            networkSeries.getData().remove(0);
        }
    }

    private void updateBarStyle(ProgressBar bar, double progress) {
        if (progress < 0.5) {
            bar.setStyle("-fx-accent: #34d399;"); // Green
        } else if (progress < 0.8) {
            bar.setStyle("-fx-accent: #fbbf24;"); // Yellow
        } else {
            bar.setStyle("-fx-accent: #ef4444;"); // Red
        }
    }

    public void stop() {
        running = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
