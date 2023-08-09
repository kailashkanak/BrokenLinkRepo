package testbrokenlink;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


import com.opencsv.CSVWriter;

public class BrokenLinkTest {
	WebDriver driver;
	
	@BeforeTest
	public void launchBrowser()
	{
		System.setProperty("webdriver.chrome.driver", "/Users/kailash.k/Downloads/chromedriver_mac64 (4)/chromedriver");
		driver = new ChromeDriver();
		Date d = new Date();
		System.out.println("Test Executed on : " + d.toString());
	}
	
	@Test
	public void findBrokenLink()
	{
		try 
		{
			FileInputStream fs = new FileInputStream("/Users/kailash.k/Documents/CompassWebsitePagesLink.xlsx");
			XSSFWorkbook wb = new XSSFWorkbook(fs);
			XSSFSheet sheet = wb.getSheetAt(0);
			int total_rows = sheet.getLastRowNum();
			int first_row = sheet.getFirstRowNum();
			
			FileWriter outputfile = new FileWriter("/Users/kailash.k/Documents/Compass-Broken-links.csv");
			CSVWriter writer = new CSVWriter(outputfile);
			String[] header = {"Page link","Href link","Response Code","Link status"};
			writer.writeNext(header);
			
			for(int i=first_row;i<=total_rows;i++)
			{
				driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
				Row row = sheet.getRow(i);
				Cell cell = row.getCell(0);
				String pageLink = cell.getStringCellValue();
				driver.get(pageLink);
				int responseCode = 0;
				List<WebElement> links = driver.findElements(By.tagName("a"));
				System.out.println("Total links available on : " + pageLink + " : " + links.size());
				String validation_messg="";
					for(WebElement myLink : links)
					{
						String url = myLink.getAttribute("href");
						if (url != null && !url.isEmpty()) {
			                try {
			                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			                    connection.setRequestMethod("HEAD");
			                    connection.connect();
			                    responseCode = connection.getResponseCode();
			                   if (responseCode >= 400) 
			                    {
			                    	
			                        System.out.println("Broken link found: " + url + " - Response code: " + responseCode);
			                    } 
			                    else 
			                    {
			                    	
			                        System.out.println("Valid link: " + url);
			                    }
			                   
			                   
			                    if(responseCode==403)
			                    {
			                    	validation_messg = "A 403 response code suggests that the server knows about the resource, but the client lacks the necessary permissions to access it";
			                    }
			                    else if(responseCode==404)
			                    {
			                    	validation_messg = "Page Not Found";
			                    }
			                    else if(responseCode==500)
			                    {
			                    	validation_messg = "Internal Server Error";
			                    }
			                    else if(responseCode==999)
			                    {
			                    	validation_messg = "A response code of 999 is not a standard HTTP status code and is not recognized as part of the HTTP specification.";
			                    }
			                    else
			                    {
			                    	validation_messg = "Good";
			                    }
			                } 
			                catch (IOException e)
			                {
			                    System.out.println("Error while checking link: " + url);
			                    e.printStackTrace();
			                }
			                
			                    
					}
				String response_code = Integer.toString(responseCode);
				String[] data1 = {pageLink,url,response_code,validation_messg};
				writer.writeNext(data1);
			}
		}
			wb.close();
			writer.close(); 
		}
		catch(Exception e) 
		{
			
		}
	}
	
	@AfterTest
	public void close()
	{
		driver.quit();
	}
}
