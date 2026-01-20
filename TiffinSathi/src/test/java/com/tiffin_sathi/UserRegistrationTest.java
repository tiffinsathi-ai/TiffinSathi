package com.tiffin_sathi;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UserRegistrationTest {

    WebDriver driver;

    @BeforeMethod
    public void setup() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @Test
    public void userRegister() {
        driver.get("http://localhost:3000/signup");

        String time = String.valueOf(System.currentTimeMillis());

        driver.findElement(By.name("name")).sendKeys("Test User");
        driver.findElement(By.name("email")).sendKeys("test" + time + "@mail.com");
        driver.findElement(By.name("phone")).sendKeys("9877543210");
        driver.findElement(By.name("address")).sendKeys("Kathmandu");
        driver.findElement(By.name("password")).sendKeys("Test@123");
        driver.findElement(By.name("confirmPassword")).sendKeys("Test@123");

        driver.findElement(By.xpath("//button[@type='submit']")).click();
    }

    @AfterMethod
    public void teardown() {
        driver.quit();
    }
}
