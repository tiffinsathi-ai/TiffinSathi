package com.tiffin_sathi;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class VendorRegistrationTest {

    WebDriver driver;

    @BeforeMethod
    public void setup() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @Test
    public void vendorRegister() throws InterruptedException {

        driver.get("http://localhost:3000/vendor-signup");
        String time = String.valueOf(System.currentTimeMillis());

        // ===== Step 1: Business Info =====
        driver.findElement(By.name("businessName")).sendKeys("Test Tiffin " + time);
        driver.findElement(By.name("ownerName")).sendKeys("Test Owner");
        driver.findElement(By.name("email")).sendKeys("vendor" + time + "@mail.com");
        driver.findElement(By.name("phone")).sendKeys("9876543210");
        driver.findElement(By.name("yearsInBusiness")).sendKeys("3");
        driver.findElement(By.xpath("//button[contains(text(),'Next')]")).click();

        Thread.sleep(500);

        // ===== Step 2: Address =====
        driver.findElement(By.name("address")).sendKeys("Kathmandu");
        driver.findElement(By.name("city")).sendKeys("Kathmandu");
        driver.findElement(By.name("state")).sendKeys("Bagmati");
        driver.findElement(By.name("pincode")).sendKeys("44600");
        driver.findElement(By.xpath("//button[contains(text(),'Next')]")).click();

        Thread.sleep(500);

        // ===== Step 3: Service =====
        driver.findElement(By.xpath("//input[@type='checkbox']")).click();
        driver.findElement(By.name("capacity")).sendKeys("30");
        driver.findElement(By.name("priceRange")).sendKeys("100-150");
        driver.findElement(By.xpath("//button[contains(text(),'Next')]")).click();

        Thread.sleep(500);

        // ===== Step 4: Bank & Legal =====
        driver.findElement(By.name("bankName")).sendKeys("Test Bank");
        driver.findElement(By.name("accountNumber")).sendKeys("1234567890");
        driver.findElement(By.name("ifscCode")).sendKeys("TEST0001");
        driver.findElement(By.name("panNumber")).sendKeys("ABCDE1234F");
        driver.findElement(By.xpath("//button[contains(text(),'Next')]")).click();

        Thread.sleep(500);

        // ===== Step 5: Documents =====
        driver.findElement(By.id("fssaiLicense"))
                .sendKeys("C:\\Users\\nepac\\OneDrive\\Documents\\fssai.pdf");
        driver.findElement(By.id("panCard"))
                .sendKeys("C:\\Users\\nepac\\OneDrive\\Documents\\pan.pdf");

        driver.findElement(By.id("terms")).click();
        driver.findElement(By.xpath("//button[contains(text(),'Submit')]")).click();
    }

    @AfterMethod
    public void tearDown() {
        driver.quit();
    }
}
