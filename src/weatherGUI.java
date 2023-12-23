import org.json.simple.JSONObject;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

//Frontend Class that displays all information received from Backend
public class weatherGUI extends JFrame {
    private JSONObject data;
    private double windspeed; // Declared here so that can be used in both action listeners
    public weatherGUI(){
        super("Weather Application"); // Name of executable that pop ups
        setDefaultCloseOperation(EXIT_ON_CLOSE); // GUI ends program when closed
        setSize(500, 750); // Size in pixels
        setLocationRelativeTo(null); // Comes up on centre of screen
        setLayout(null); // Layout done through coordinates
        setResizable(false); // Doesn't let user resize
        createGUI(); // Calls function to create design on GUI
    }

    private void createGUI(){
        // Creating searchbar and text on it on startup
        JTextField searchbar = new JTextField("Type location here");
        searchbar.setBounds(30, 30, 350, 60);
        searchbar.setFont(new Font("Courier New", Font.PLAIN, 24));
        searchbar.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchbar.getText().equals("Type location here")) searchbar.setText("");

            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchbar.getText().isEmpty()) searchbar.setText("Type location here");

            }
        });
        add(searchbar);

        // Image to show weather condition
        JLabel weatherConditionImg = new JLabel();
        weatherConditionImg.setBounds(125, 125, 250, 250);
        add(weatherConditionImg);

        // Temperature
        JLabel temperatureText = new JLabel("Weather App!"); // Text for startup
        temperatureText.setBounds(0, 400, 500, 54);
        temperatureText.setFont(new Font("Courier New", Font.BOLD, 48));
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        // Weather condition
        JLabel weatherConditionText = new JLabel("-");
        weatherConditionText.setBounds(0, 450, 500, 36);
        weatherConditionText.setFont(new Font("Courier New", Font.PLAIN, 32));
        weatherConditionText.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionText);

        // Humidity
        JLabel humidityText = new JLabel("<html><b>Humidity</b> %</html>");
        humidityText.setBounds(90, 500, 85, 55);
        humidityText.setFont(new Font("Courier New", Font.PLAIN, 16));
        add(humidityText);

        // Wind speed
        JLabel windspeedText = new JLabel("<html><b>Windspeed</b> km/h</html>");
        windspeedText.setBounds(90, 600, 100, 55);
        windspeedText.setFont(new Font("Courier New", Font.PLAIN, 16));
        add(windspeedText);

        // Search button
        JButton searchButton = new JButton(loadScaledImage("logos/search.JPG",60,60));
        JToggleButton unitSwitch = new JToggleButton("kmh"); // Switch made here so that its Action Listener can be triggered
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(400, 30, 60, 60);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Grab user input from searchbar
                String userInput = searchbar.getText();
                // Remove white space to see if user actually has inputted something
                if(userInput.replaceAll("\\s", "").length() <= 0){
                    return;
                }
                // Get data using location
                data = weatherData.getWeatherData(userInput);
                // Updating GUI with new information
                String weatherCondition = (String) data.get("weather_condition");
                // Switch case depending on weather condition
                switch(weatherCondition){
                    case "Clear":
                        weatherConditionImg.setIcon(loadScaledImage("logos/sunny.png",250,250));
                        break;
                    case "Cloudy":
                        weatherConditionImg.setIcon(loadScaledImage("logos/cloudy.png",250,250));
                        break;
                    case "Raining":
                        weatherConditionImg.setIcon(loadScaledImage("logos/rainy.png",250,250));
                        break;
                    case "Snowing":
                        weatherConditionImg.setIcon(loadScaledImage("logos/snowy.png",250,250));
                        break;
                }
                //Updating Text
                double temperature = (double) data.get("temperature");
                temperatureText.setText(temperature + "°C");
                weatherConditionText.setText(weatherCondition);
                long humidity = (long) data.get("humidity");
                humidityText.setText("<html><b>Humidity</b> " + humidity + "%</html>");
                windspeed = (double) data.get("windspeed");
                // Triggering other listener so that speed is updated instantly with other data
                unitSwitch.getActionListeners()[0].actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        });
        searchButton.setFocusable(true);
        searchButton.requestFocusInWindow();
        add(searchButton);

        //Switch for changing units of wind speed
        unitSwitch.setBounds(200,600,100,60);
        unitSwitch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isPressed = unitSwitch.isSelected();
                if (isPressed){
                    unitSwitch.setText("mph");
                    windspeedText.setText(String.format("<html><b>Windspeed</b>  %.1f mph</html>",(windspeed * 0.62137119)));
                }
                else{
                    unitSwitch.setText("km/h");
                    windspeedText.setText("<html><b>Windspeed</b> " + windspeed + " km/h</html>");
                }
            }
        });
        add(unitSwitch);
    }

    // Function that grabs and scales image from resource path given
    private ImageIcon loadScaledImage(String resourcePath, int imgWidth, int imgHeight){
        try{
            // Reads and scales image
            BufferedImage image = ImageIO.read(new File(resourcePath));
            Image scaledImage = image.getScaledInstance(imgWidth, imgHeight, image.SCALE_SMOOTH);
            BufferedImage bufferedImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.drawImage(scaledImage, 0, 0, null);
            g2d.dispose();
            return new ImageIcon(bufferedImage);
        }catch(IOException e){
            e.printStackTrace();
        }
        System.out.println("Doesn't exist in given resource path given");
        return null;
    }
}
