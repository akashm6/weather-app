import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * A user-driven weather application that uses data on wunderground.com to accurately report a
 * variety of weather information given an inputted longitude and latitude. This is a personaly
 * project that I've been working on in order to better understand web APIs, screen-scraping, and
 * the internal components of websites.
 * 
 * @author Akash Mohan
 *
 */
public class Weather {

  private WebClient client;
  private HtmlPage page;

  /**
   * Constructor for the Weather class. Creates a new Weather object. Initializes data fields.
   * Throws an IOException if any problems occur trying to receive the webpage from the client using
   * the url.
   */
  public Weather() {
    this.client = new WebClient();
    this.client.getOptions().setCssEnabled(false);
    this.client.getOptions().setJavaScriptEnabled(false);
    try {
      this.page = this.client.getPage("https://www.wunderground.com/");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  /**
   * Sets up the weather application so that it can be correctly used without error.
   * 
   * @param longitude the user-inputted longitude
   * @param latitude  the user-inputted latitude
   * @return true if the weather application is correctly set up, false otherwise
   */
  public boolean setWeatherApp(double longitude, double latitude) {
    // if the wunderground page doesn't connect correctly given the longitude and latitude for any
    // reason, return false
    if (!isValidPage(longitude, latitude)) {
      return false;
    }
    HtmlPage errorpage = null;
    try {
      errorpage =
          this.client.getPage("https://www.wunderground.com/weather/" + longitude + "," + latitude);
    } catch (Exception e) {
      e.printStackTrace();
    }
    // If the wunderground page has a soft 404 error, and doesn't have a page for that specific
    // location, the asText() method will contain "Location: , undefined" whilst wunderground pages
    // with actual information for a specific location will not. We can use this to differentiate
    // when wunderground doesn't have information on a specific location.
    if (errorpage.asText().contains("Location: , undefined")) {
      return false;
    }

    try {
      this.page =
          this.client.getPage("https://www.wunderground.com/weather/" + longitude + "," + latitude);
    }
    // catches any final unexpected exceptions
    catch (Exception e) {
      return false;
    }

    // return true if no problems occur
    return true;

  }

  /**
   * Checks if a wunderground page is a valid page given longitude and latitude. A wunderground page
   * is valid if it has a valid HttpURLConnection.
   * 
   * @param longitude, the user-inputted longitude
   * @param latitude,  the user-inputted latitude
   * @return true if the wunderground page is valid, false otherwise
   */
  public boolean isValidPage(double longitude, double latitude) {
    try {
      URL url = new URL("https://www.wunderground.com/weather/" + longitude + "," + latitude);
      HttpURLConnection c = (HttpURLConnection) url.openConnection();
      c.setRequestMethod("GET");
      c.connect();
      if (c.getResponseCode() != 200) {
        return false;
      }
    }

    catch (Exception e) {
      e.printStackTrace();
      return false;
    }

    return true;
  }

  /**
   * Returns the city at the coordinates provided by the user by searching for HTML elements at
   * specific wunderground XPaths
   * 
   * @return the city at the coordinates provided by the user
   */
  public String getCity() {

    HtmlElement city = page
        .getFirstByXPath("//*[@id=\"inner-content\"]/div[2]/lib-city-header/div[1]/div/h1/span[1]");

    // Some wunderground pages are formatted as "City Weather Conditions" while others are simply
    // "City Conditions". This makes sure no errors are thrown in either case.
    if (city.asText().contains("Weather")) {
      return city.asText().substring(0, city.asText().indexOf("Weather")).trim();
    }
    // returns the text formatted version of the html element that contains the city information
    return city.asText().substring(0, city.asText().indexOf("Conditions")).trim();

  }

  /**
   * returns the time and date at the coordinates provided by the user by searching for HTML
   * elements at specific wunderground XPaths
   * 
   * @return A formatted message listing the time and date at the city at the coordinates provided
   *         by the user
   */
  public String getTimeandDate() {
    HtmlElement time = page.getFirstByXPath(
        "//*[@id=\"inner-content\"]/div[3]/div[1]/div/div[1]/div[1]/lib-city-current-conditions/div/div[1]/p/span[1]/strong");
    return time.asText().trim();
  }

  /**
   * returns the temperature and perceived temperature at the coordinates provided by the user by
   * searching for HTML elements at specific wunderground XPaths
   * 
   * @return the temperature and perceived temperature at the user-provided coordinates
   */
  public String getTemp() {
    HtmlElement feelsLike = page.getFirstByXPath(
        "//*[@id=\"inner-content\"]/div[3]/div[1]/div/div[1]/div[1]/lib-city-current-conditions/div/div[2]/div/div/div[3]");
    HtmlElement temp = page.getFirstByXPath(
        "//*[@id=\"inner-content\"]/div[3]/div[1]/div/div[1]/div[1]/lib-city-current-conditions/div/div[2]/div/div/div[2]/lib-display-unit/span/span[1]");
    return temp.asText() + "°F" + "\n" + "Feels " + feelsLike.asText();
  }

  /**
   * returns the short weather forecast at the coordinates provided by the user by searching for
   * HTML elements at specific wunderground XPaths
   * 
   * @return the short weather forecast at the user provided coordinates
   */
  public String getForecast() {

    HtmlElement temp = page.getFirstByXPath(
        "//*[@id=\"inner-content\"]/div[3]/div[1]/div/div[1]/div[1]/lib-city-current-conditions/div/div[3]/div/div[1]/p");

    return temp.asText();
  }


  /**
   * returns the list of additional conditions provided by wunderground at the coordinates provided
   * by the user by searching for HTML elements at specific wunderground XPaths
   * 
   * @return A formatted message listing the additional conditions at the user provided coordinates
   */
  public String getAdditionalConditions() {
    HtmlElement conditions = page.getFirstByXPath(
        "//*[@id=\"inner-content\"]/div[3]/div[2]/div/div[1]/div[1]/lib-additional-conditions/lib-item-box/div/div[2]/div");
    // creates an array of all strings contained in the additional conditions information
    String[] cArray = conditions.asText().split("\n");


    String message = "";
    // uses a loop to format a message that lists pressure, visibility, clouds, dew point, humidity,
    // rainfall, and snow depth respectively, all separated by a line
    for (int x = 0; x < cArray.length; x += 2) {
      if (cArray[x + 1].contains("°")) {
        int index = cArray[x + 1].indexOf("°");
        cArray[x + 1] = cArray[x + 1].substring(0, index - 1) + " "
            + cArray[x + 1].substring(index + 1, cArray[x + 1].length());
      }
      message += cArray[x] + ": " + cArray[x + 1] + "\n";
    }

    // return the formatted message listing the additional conditions
    return "Additional Conditions:\n" + message.trim();
  }

  /**
   * returns the general weather forecast at the coordinates provided by the user by searching for
   * HTML elements at specific wunderground XPaths
   * 
   * @return the general weather forecast at the user provided coordinates
   */
  public String getGeneralForecast() {
    HtmlElement forecast = page.getFirstByXPath(
        "//*[@id=\"inner-content\"]/div[3]/div[1]/div/div[3]/div/lib-city-today-forecast/div/div[1]/div/div/div/a[2]");

    return forecast.asText();
  }

  /**
   * returns the air quality information at the coordinates provided by the user by searching for
   * HTML elements at specific wunderground XPaths
   * 
   * @param longitude, the user-provided longitude
   * @param latitude,  the user-provided latitude
   * @return A formatted message listing air quality information in the city at the coordinates
   *         provided by the user
   */
  public String getAirQuality(double longitude, double latitude) {
    // Because the air quality info in wunderground is on a separate page, we have to create a new
    // HtmlPage object as to not alter instance variables
    HtmlPage airPage = null;
    try {
      airPage = this.client.getPage("https://www.wunderground.com/health/" + longitude + ","
          + latitude + "?cm_ven=localwx_modaq");
    } catch (Exception e) {
      e.printStackTrace();
    }
    HtmlElement air = airPage
        .getFirstByXPath("//*[@id=\"airqualityindex_section\"]/div/div/div/div[1]/div[2]/div[2]");

    HtmlElement index = airPage.getFirstByXPath(
        "//*[@id=\"airqualityindex_section\"]/div/div/div/div[2]/div[2]/div[1]/div[2]");

    String airQualityMessage = "The air quality in " + getCity().trim() + " is " + air.asText()
        + ". The air quality index is " + index.asText() + ".";
    return airQualityMessage;
  }

  /**
   * Creates an interactive interface for the user to input their own coordinates, and view weather
   * information to their own discretion. Prints out messages depending on what the user inputs. The
   * interactive interface will go on until the user explicitly inputs "end".
   */
  public void useApplication() {
    Scanner s = null;
    try {
      s = new Scanner(System.in);
    } catch (Exception e) {
      e.printStackTrace();
    }
    int x = 0;
    double fullLong = 0;
    double fullLat = 0;

    while (x == 0) {

      System.out.println("Welcome to this weather application! Please input a longitude: " + "\n"
          + "(Type \"End\" to stop.)");

      String input = s.next();
      if (input.equalsIgnoreCase("end")) {
        System.out.println("Thank you for using this application. See you soon!");
        return;
      }

      double longitude = Double.parseDouble(input);
      if (longitude <= -180 || longitude >= 180) {
        System.out.println("Invalid longitude! Must be between -180 and 180");
        continue;
      }
      System.out.println("Please input a latitude: ");
      input = s.next();
      if (input.equalsIgnoreCase("end")) {
        System.out.println("Thank you for using this application. See you soon!");
        return;
      }
      double latitude = Double.parseDouble(input);

      if (latitude <= -90 || latitude >= 90) {
        System.out.println("Invalid latitude! Must be between -90 and 90");
        continue;
      }

      if (setWeatherApp(longitude, latitude) == false) {
        System.out.println("Sorry! This location doesn't exist on this webpage. Please try again.");
        continue;
      } else {
        fullLong = longitude;
        fullLat = latitude;
        break;
      }
    }
    while (x == 0) {
      System.out.println("The city you've selected is: " + getCity() + ". It is currently "
          + getTimeandDate()
          + ".\nPress \"T\" to get the temperature.\nPress \"F\" for the forecast.\nPress \"G\" for the general forecast."
          + "\nPress \"A\" for Air Quality information.\nPress \"X\" for Additional Conditions.\nType \"End\" to stop the program.");
      String input = s.next();
      if (input.equalsIgnoreCase("T")) {
        System.out.println(getTemp() + "\n");

      } else if (input.equalsIgnoreCase("F")) {
        System.out.println(getForecast() + "\n");

      } else if (input.equalsIgnoreCase("G")) {
        System.out.println(getGeneralForecast() + "\n");

      } else if (input.equalsIgnoreCase("A")) {
        System.out.println(getAirQuality(fullLong, fullLat) + "\n");

      } else if (input.equalsIgnoreCase("X")) {
        System.out.println(getAdditionalConditions() + "\n");
      } else if (input.equalsIgnoreCase("end")) {
        System.out.println("Thank you for using this application. See you soon!");
        break;
      } else {
        System.out.println("Invalid input. Please try again.");
        continue;
      }

    }

  }

  /**
   * main method, runs the application
   * 
   * @param args, command line arguments
   */
  public static void main(String[] args) {
    Weather w = new Weather();

    w.useApplication();


  }


}
