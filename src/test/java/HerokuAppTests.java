import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.List;

public class HerokuAppTests {

    // ------------- BROWSER SET-UP (START & QUIT)
    private WebDriver driver;

    @BeforeMethod
    public void startDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        options.addArguments("--remote-allow-origins=*");
    }

    @AfterMethod
    public void quitDriver() {
        driver.close();
        driver.quit();
    }


    // ------------- TESTS

    @Test
    public void AddRemoveElementsTest() {
        int numberOfElements = 30;

        //to go correct page
        driver.get("https://the-internet.herokuapp.com/add_remove_elements/");
        Assert.assertEquals(driver.findElement(By.tagName("h3")).getText().trim(), "Add/Remove Elements");

        //add elements
        for(int i = 0; i < numberOfElements; i++) {
            driver.findElement(By.cssSelector("#content > div > button")).click();
        }

        //confirm correct number of elements have been added to the page
        List<WebElement> addedElements = driver.findElements(By.cssSelector("#elements > button"));
        Assert.assertEquals(addedElements.size(), numberOfElements);

        //remove elements
        for(int i = numberOfElements; i > 0; i--) {
            driver.findElement(By.cssSelector("button.added-manually:nth-child(" + i + ")")).click();
        }

        //confirm all elements have been deleted successfully
        List<WebElement> deletedElements = driver.findElements(By.cssSelector("#elements > button"));
        Assert.assertEquals(deletedElements.size(), 0);
    }

    @Test
    public void BasicAuthTest() {
        //login credentials
        String USERNAME = "admin";
        String PASSWORD = "admin";

        //go to page & authenticate at the same time
        driver.get("https://" + USERNAME + ":" + PASSWORD + "@the-internet.herokuapp.com/basic_auth");

        //apply assertion checks
        Assert.assertEquals(driver.findElement(By.tagName("h3")).getText().trim(), "Basic Auth");
        Assert.assertEquals(driver.findElement(By.cssSelector("#content > div > p")).getText().trim(), "Congratulations! You must have the proper credentials.");
        Assert.assertTrue(driver.getCurrentUrl().contains("/basic_auth"));
    }

    @Test
    public void DraggableTest() {
        //go to page
        driver.get("https://jqueryui.com/droppable/");

        //enter the iframe
        WebElement iframe = driver.findElement(By.xpath("//*[@class='demo-frame']"));
        driver.switchTo().frame(iframe);

        Actions action = new Actions(driver);
        WebElement draggable = driver.findElement(By.id("draggable"));
        WebElement droppable = driver.findElement(By.id("droppable"));
        action.dragAndDrop(draggable, droppable).perform();

        Assert.assertEquals(droppable.getText(), "Dropped!");
        Assert.assertTrue(hasClass(droppable, "ui-state-highlight"));
    }

    // method helping us check the classes of elements
    public boolean hasClass(WebElement element, String className) {
        String classes = element.getAttribute("class");
        for (String c : classes.split(" ")) {
            if (c.equals(className)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void DragAndDropTest() throws Exception {
        //go to page
        driver.get("https://the-internet.herokuapp.com/drag_and_drop");

        //locate the web elements
        WebElement objectA = driver.findElement(By.id("column-a"));
        WebElement objectB = driver.findElement(By.id("column-b"));

        //use JavaScript executor method
        dragAndDropJS(objectA, objectB, driver);
        Assert.assertEquals(driver.findElement(By.cssSelector("#columns div:nth-child(2)")).getText(), "A");
    }

    public void dragAndDropJS(WebElement drag, WebElement drop, WebDriver driver) throws Exception {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("function createEvent(typeOfEvent) {\n" +"var event =document.createEvent(\"CustomEvent\");\n" +"event.initCustomEvent(typeOfEvent,true, true, null);\n" +"event.dataTransfer = {\n" +"data: {},\n" +"setData: function (key, value) {\n" +"this.data[key] = value;\n" +"},\n" +"getData: function (key) {\n" +"return this.data[key];\n" +"}\n" +"};\n" +"return event;\n" +"}\n" +"\n" +"function dispatchEvent(element, event,transferData) {\n" +"if (transferData !== undefined) {\n" +"event.dataTransfer = transferData;\n" +"}\n" +"if (element.dispatchEvent) {\n" + "element.dispatchEvent(event);\n" +"} else if (element.fireEvent) {\n" +"element.fireEvent(\"on\" + event.type, event);\n" +"}\n" +"}\n" +"\n" +"function simulateHTML5DragAndDrop(element, destination) {\n" +"var dragStartEvent =createEvent('dragstart');\n" +"dispatchEvent(element, dragStartEvent);\n" +"var dropEvent = createEvent('drop');\n" +"dispatchEvent(destination, dropEvent,dragStartEvent.dataTransfer);\n" +"var dragEndEvent = createEvent('dragend');\n" +"dispatchEvent(element, dragEndEvent,dropEvent.dataTransfer);\n" +"}\n" +"\n" +"var source = arguments[0];\n" +"var destination = arguments[1];\n" +"simulateHTML5DragAndDrop(source,destination);",drag, drop);
        Thread.sleep(1000);
    }

    @Test
    public void NestedFramesTest() {
        //go to page
        driver.get("https://the-internet.herokuapp.com/nested_frames");
        //get into the top iframe
        WebElement top = driver.findElement(By.name("frame-top"));
        driver.switchTo().frame(top);
        //go into another iframe, within the top iframe
        WebElement left = driver.findElement(By.name("frame-left"));
        driver.switchTo().frame(left);
        Assert.assertEquals(driver.findElement(By.xpath("/html/body")).getText().trim(), "LEFT");
        //go backwards to the top frame
        driver.switchTo().parentFrame();
        //now go into the next iframe
        WebElement middle = driver.findElement(By.name("frame-middle"));
        driver.switchTo().frame(middle);
        Assert.assertEquals(driver.findElement(By.xpath("/html/body")).getText().trim(), "MIDDLE");
        //go backwards to the top frame
        driver.switchTo().parentFrame();
        //go into the right iframe within the top frame
        WebElement right = driver.findElement(By.name("frame-right"));
        driver.switchTo().frame(right);
        Assert.assertEquals(driver.findElement(By.xpath("/html/body")).getText().trim(), "RIGHT");
        //go backwards and out of the top frame
        driver.switchTo().defaultContent();
        //go into the bottom iframe
        WebElement bottom = driver.findElement(By.name("frame-bottom"));
        driver.switchTo().frame(bottom);
        Assert.assertEquals(driver.findElement(By.xpath("/html/body")).getText().trim(), "BOTTOM");
    }

    @Test
    public void RedirectTest() {
        //go to page & save the URL in a variable
        driver.get("https://the-internet.herokuapp.com/redirector");
        String originalURL = driver.getCurrentUrl();
        //perform action and save the new URL
        driver.findElement(By.id("redirect")).click();
        String newURL = driver.getCurrentUrl();
        //compare URLs and confirm the current URL is the expected one
        Assert.assertNotEquals(newURL, originalURL);
        Assert.assertEquals(driver.getCurrentUrl(), "https://the-internet.herokuapp.com/status_codes");
        Assert.assertEquals(driver.findElement(By.tagName("h3")).getText().trim(), "Status Codes");
    }

    @Test
    public void CheckboxesTest() {
        // going to the checkbox page
        driver.get("https://the-internet.herokuapp.com/checkboxes");
        // locate the web elements
        WebElement checkbox1 = driver.findElement(By.xpath("/html/body/div[2]/div/div/form/input[1]"));
        WebElement checkbox2 = driver.findElement(By.xpath("/html/body/div[2]/div/div/form/input[2]"));
        // perform initial checks
        String currentTitle = driver.findElement(By.cssSelector(".example > h3")).getText().trim();
        Assert.assertEquals(currentTitle, "Checkboxes");
        Assert.assertTrue(checkbox2.isSelected());
        Assert.assertFalse(checkbox1.isSelected());
        // do an action
        checkbox1.click();
        checkbox2.click();
        //confirm changes after action
        Assert.assertFalse(checkbox2.isSelected());
        Assert.assertTrue(checkbox1.isSelected());
    }

    @Test
    public void ContextMenuTest() {
        //go to page & confirm page title
        driver.get("https://the-internet.herokuapp.com/context_menu");
        Assert.assertEquals(driver.findElement(By.xpath("//*[@id=\"content\"]/div/h3")).getText().trim(), "Context Menu");
        //right click inside the hot spot element
        Actions actions = new Actions(driver);
        actions.contextClick(driver.findElement(By.id("hot-spot"))).perform();
        //confirm you got an alert with the correct message inside
        Alert alert = driver.switchTo().alert();
        Assert.assertEquals(alert.getText().trim(), "You selected a context menu");
        //close alert
        alert.accept();
    }

    @Test
    public void DropdownTest() {
        //go to page & confirm page title
        driver.get("https://the-internet.herokuapp.com/dropdown");
        Assert.assertEquals(driver.findElement(By.xpath("//*[@id=\"content\"]/div/h3")).getText().trim(), "Dropdown List");
        //confirm that the placeholder text is currently selected
        getTextOfSelectedOption("Please select an option");
        //select a different option
        Select select = new Select(driver.findElement(By.id("dropdown")));
        select.selectByValue("1");
        //assert that the correct option was selected
        getTextOfSelectedOption("Option 1");
        //select a different option
        select.selectByVisibleText("Option 2");
        //assert that the correct option was selected
        getTextOfSelectedOption("Option 2");
    }

    //method to get the text of a selected option
    public void getTextOfSelectedOption(String expectedOption) {
        boolean selectedOptionIsCorrect = false;
        List<WebElement> allOptions = driver.findElements(By.cssSelector("#dropdown > option"));
        for (WebElement we: allOptions) {
            if ( doesAttributeExist(we, "selected") && we.getText().trim().contains(expectedOption) ) {
                selectedOptionIsCorrect = true;
            }
        }
        Assert.assertTrue(selectedOptionIsCorrect);
    }

    //method to confirm if element has a certain attribute
    public boolean doesAttributeExist(WebElement element, String attribute) {
        boolean result = false;
        try {
            String value = element.getAttribute(attribute);
            if (value != null){
                result = true;
            }
        } catch (Exception e) {}
        return result;
    }

}
