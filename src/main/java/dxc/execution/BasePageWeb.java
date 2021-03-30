package dxc.execution;

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;


import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.net.URL;

import dxc.util.DXCUtil;

public class BasePageWeb {

	//Navegadores que se puede usar en las páginas:
	public static final String IEXPLORE = "IEXPLORE";
	public static final String CHROME   = "CHROME";
	public static final String FIREFOX  = "FIREFOX";
	
	private WebDriver driver;
	private JavascriptExecutor jse; // Para IExplorer se requiere
	private String navegador;
	private String downloadFilePath; //Ruta donde se descargan archivos, si está vacío la página no descarga archivos  
	private String frameActual = ""; // INDICA EL FRAME ACTUAL, SI EST� VAC�O EST� EN EL DEFAULT
	
	//Constructor que recibe el navegador a usar:
	public BasePageWeb(String navegador) {
		this.downloadFilePath = "";
		this.constructorBasePageWeb(navegador);
	}
	
	//Constructor que recibe el navegador a usar y la ruta donde se har� descarga de archivos:
	public BasePageWeb(String navegador, String downloadFilePath) {
		this.downloadFilePath = downloadFilePath;
		this.constructorBasePageWeb(navegador);
	}
//-----------------------------------------------------------------------------------------------------------------------
	private void constructorBasePageWeb(String navegador) {
		this.navegador = navegador;
		if (navegador.equals(IEXPLORE))     this.iExplorerDriverConnection();
		else if (navegador.equals(CHROME))  this.chromeDriverConnection();
		else if (navegador.equals(FIREFOX)) this.firefoxDriverConnection();
		else throw new WebDriverException ("Navegador [" + navegador + "] NO contemplado");
		
		do { // ESPERA MIENTRAS EL BROWSER NO EST� DISPONIBLE
			DXCUtil.wait(1);
		} while (!this.browserIsEnabled());
		jse = (JavascriptExecutor)this.driver;
	}
	
	private void chromeDriverConnection()  {
		// String resource = "./src/main/resources/WebDriver/chromedriver84.exe";
		String pathResource = "WebDriver/chromedriver84.exe";
		String resource = DXCUtil.PATH_RESOURCES + "/" + pathResource;
		System.setProperty("webdriver.chrome.driver", this.getClass().getResource(resource).getFile());
		
		if (this.downloadFilePath.equals("")) {
		    try{
		    DesiredCapabilities cap = new DesiredCapabilities();
		    cap.setBrowserName("chrome");
		    this.driver = new RemoteWebDriver(new URL("http://selenium-v2-selenium-hub.selenium.svc.cluster.local:4444/wd/hub"),cap);
		    }catch (Exception e){
		       e.printStackTrace();
		    }
		    
			//this.driver = new ChromeDriver();
		} else {
			HashMap<String, Object> profile = new HashMap<String, Object>();
			/* 1-Se indica la carpeta de download.
			 * 2-Se apaga la ventana que pregunta d�nde ubicar y c�mo llamar el archivo por descargar.
			 * 3-Opci�n para que chrome pueda crear la ruta de descarga si es que no existe.
			 * 4-Navegaci�n segura activada (opcional).
			 * 5-Para abrir PDFs externamente.
			 * 6-Desactiva todas las extensiones de Chrome para evitar que alg�n plugin del navegador intente abrir PDFs.
			 * 7-Desactiva la vista de impresi�n para evitar nuevamente que algo intente abrir PDFs.
			 * 8-Desactiva el bloqueo de pantallas emergentes.
			 */
			profile.put("download.default_directory", this.downloadFilePath); //1#
			profile.put("download.prompt_for_download", false); //2#
			profile.put("download.directory_upgrade", true); //3#
			profile.put("safebrowsing.enabled", true); //4#
			profile.put("plugins.always_open_pdf_externally", true); //5#
			//profile.put("profile.default_content_settings.popups", 0);
			ChromeOptions options = new ChromeOptions(); //hereda de MutableCapabilities 
			options.setExperimentalOption("prefs", profile);
			options.addArguments("--disable-extensions", "--disable-print-preview"); //6# y 7# 8# "--disable-popup-blocking"
			this.driver = new ChromeDriver(options);
		}
	}
	/**
	 * Este m�todo funciona para el ingreso de caracteres en las cajas de texto para Firefox57, si se usa el driver
	 * V0.25 � V0.26.
	 */
	private void firefoxDriverConnection() {
		System.setProperty("webdriver.gecko.driver", "./src/main/resources/WebDriver/geckodriver.exe");
		if (this.downloadFilePath.equals("")) {
			this.driver = new FirefoxDriver();
		} else {
			FirefoxProfile profile = new FirefoxProfile();
			/* 1-Se indica que debe utilizar la carpeta especificada en el paso siguiente.
			 * 2-Se indica la carpeta de download.
			 * 3-Se activa el uso del directorio asignado en el paso anterior.
			 * 4-Se apaga la animaci�n de comienzo de descarga (opcional).
			 * 5-Paso importante, se pasa como par�metro los MIME type que quieren descargar sin preguntar.
			 * 6-Desactivar animaci�n de descarga completa (opcional).
			 * 7-Deshabilitar la ventana de descarga de firefox.
			 * 8-Apagar el plugin por defecto de firefox para leer archivos PDF.
			 */
			String apps = "application/octet-stream;image/jpeg;application/pdf";
			profile.setPreference("browser.download.folderList", 2); //#1
			profile.setPreference("browser.download.dir", this.downloadFilePath); //#2
			profile.setPreference("browser.download.useDownloadDir", true); //#3
			profile.setPreference("browser.download.manager.showWhenStarting", false); //4#
			profile.setPreference("browser.helperApps.neverAsk.saveToDisk", apps); //5#
			//profile.setPreference("browser.helperApps.alwaysAsk.force", false); //#5
			profile.setPreference("browser.download.manager.showAlertOnComplete", false); //6#
			profile.setPreference("browser.download.manager.useWindow", false); //7#
			profile.setPreference("pdfjs.disabled", true); //8#
			FirefoxOptions options = new FirefoxOptions().setProfile(profile);
			this.driver = new FirefoxDriver(options);
		}
	}
	/**
	 * Este m�todo funciona para el ingreso de caracteres en las cajas de texto para IE11, si se usa el driver para 32 bits
	 * versi�n 2.53.1 = requerido setear el capability NATIVE_EVENTS en false.
	 */
	@SuppressWarnings("deprecation")
	private void iExplorerDriverConnection() {
		System.setProperty("webdriver.ie.driver", "./src/main/resources/WebDriver/IEDriverServer32_2.53.1.exe");
		if (this.downloadFilePath.equals("")) {
			DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
			capabilities.setCapability("IntroduceInstabilityByIgnoringProtectedModeSettings", true);
			capabilities.setCapability(InternetExplorerDriver.NATIVE_EVENTS, false); // MANUAL DICE TRUE PERO FALSE POR DEMORA DEL WRITE
			capabilities.setCapability("browserFocus", true);
			capabilities.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true); // "ignoreZoomSetting"
			capabilities.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, true);
			capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, false); // "ignoreProtectedModeSettings"
			//capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, 1);
			//capabilities.setCapability("browserstack.ie.enablePopups", "true");
			//capabilities.setCapability("disable-popup-blocking", false);
			this.driver = new InternetExplorerDriver(capabilities);
		} else {
			System.out.println("***** OJO ---- No se ha parametrizado *****");
			DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
			capabilities.setCapability(InternetExplorerDriver.NATIVE_EVENTS, false);
			this.driver = new InternetExplorerDriver(capabilities);
		}
	}
//-----------------------------------------------------------------------------------------------------------------------
	public void maximizeBrowser() {
		this.driver.manage().window().maximize();
	}
	
	/**
	 * Almacena la imagen como se encuentra de la p�gina Web.
	 * @param nbFilePath : Ruta completa con el nombre del archivo, debe ir con extensi�n de imagen.
	 */
	public void saveScreenshot(String nbFilePath) {
        File src = ((TakesScreenshot)this.driver).getScreenshotAs(OutputType.FILE);
        try {
        	FileHandler.copy(src, new File(nbFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	/**
	 * Almacena la imagen de TODA la p�gina Web.
	 * OJO aveces no funciona, porque las coordenadas X o Y al restarlas con el ancho o alto deseado, da muy grande y se
	 * presenta una [RasterFormatException] e invoca a [saveScreenshot]  
	 * @param nbFilePath : Ruta completa con el nombre del archivo, debe ir con extensi�n de imagen.
	 */
	public void saveFullScreenshot(String nbFilePath) {
        WebElement element = this.driver.findElement(By.tagName("html")); 
        
        int width = element.getSize().getWidth();
        int windowWidth = this.driver.manage().window().getSize().getWidth();
        if (windowWidth < width) width = windowWidth;
		
        int height = element.getSize().getHeight();
        int windowHeight = this.driver.manage().window().getSize().getHeight();
        if (windowHeight < height) height = windowHeight;
        
        int x = this.driver.manage().window().getPosition().getX();
        int y = this.driver.manage().window().getPosition().getY();
        
        File src = ((TakesScreenshot)this.driver).getScreenshotAs(OutputType.FILE);
        try {
	        BufferedImage img  = ImageIO.read(src);
	        BufferedImage dest = img.getSubimage(x, y, width, height); //Toma desde el X=0 y Y=0
	        ImageIO.write(dest, "png", src); //Debe ser PNG para que haga el corte
	        FileHandler.copy(src, new File(nbFilePath));
        } catch (RasterFormatException re) {
        	System.out.println("Se present� un [RasterFormatException] se hace un [saveScreenshot]...");
            this.saveScreenshot(nbFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public void deleteCookies() {
		this.driver.manage().deleteAllCookies();
	}
//-----------------------------------------------------------------------------------------------------------------------
	//MANEJO DE DI�LOGOS - ALERTAS DE LAS P�GINAS WEB:
	/**
	 * Indica si existe un di�logo (pantalla de alerta) en la p�gina web. 
	 * @return boolean
	 */
	public boolean existDialog() {
		boolean existeAlerta = true;
		try {
			this.driver.switchTo().alert(); //Se genera excepci�n si no hay alerta 
		} catch (NoAlertPresentException e) {
			existeAlerta = false;
		}
		return existeAlerta;
	}
	/**
	 * Espera hasta que se presente el di�logo (alerta) en la p�gina Web.
	 * @param secondsWait : Segundos m�ximo de espera, si viene <= 0 toma un valor de 10 por defecto.
	 */
	public void waitDialog(int secondsWait) {
		int maximoEspera = secondsWait;
		if (secondsWait <= 0) maximoEspera = 10;
		
		WebDriverWait wait = new WebDriverWait(this.driver, maximoEspera);
		//Wait for the alert to be displayed and store it in a variable
		wait.until(ExpectedConditions.alertIsPresent());
	}
	/**
	 * Aceptar el di�logo (alerta) presentado en la p�gina Web.
	 * Requerido que exista el di�logo, si no existe se presentar� una excepci�n "NoAlertPresentException".
	 */
	public void acceptDialog() {
		this.driver.switchTo().alert().accept();
	}
	/**
	 * Recahzar el di�logo (alerta) presentado en la p�gina Web.
	 * Requerido que exista el di�logo, si no existe se presentar� una excepci�n "NoAlertPresentException".
	 */
	public void rejectDialog() {
		this.driver.switchTo().alert().dismiss();
	}
	/**
	 * Retorna el mensaje presentado en el di�logo (alerta) presentado en la p�gina Web.
	 * Requerido que exista el di�logo, si no existe se presentar� una excepci�n "NoAlertPresentException".
	 */
	public String getMessageDialog() {
		return this.driver.switchTo().alert().getText();
	}
//-----------------------------------------------------------------------------------------------------------------------
	public WebDriver getDriver( ) {
		return this.driver;
	}
	
	public void setDriver(WebDriver driverSet) {
		this.driver = driverSet;
	}
	
	public String getNavegador() {
		return this.navegador;
	}
//-----------------------------------------------------------------------------------------------------------------------
	public void navigate(String url) {
		this.driver.get(url);
	}
	
	public void closeAllBrowsers( ) {
		this.driver.quit();
	}
	
	public void closeCurrentBrowser() {
		this.driver.close();
	}
	
	public void refresh() {
		this.driver.navigate().refresh();
	}
	
	public String getTitle() {
		return this.driver.getTitle();
	}
	
	public String getIdWindow() {
		return this.driver.getWindowHandle();
	}
	
	public ArrayList<String> getIdWindows() {
		return new ArrayList<String>(this.driver.getWindowHandles());
	}
	
	public void changeWindow(String idWindow) {
		this.driver.switchTo().window(idWindow);
	}

	/**
	 * Cambia al frame por default de la p�gina, dej�ndolo en el PRINCIPAL.
	 */
	public void changeFrame() {
		//ZEA if (this.frameActual.isEmpty()) return; // YA SE ENCUENTRA EN EL FRAME DEFAULT
		this.driver.switchTo().defaultContent();
		this.frameActual = "";
	}
	/**
	 * Cambia al frame cuyo WebElement se identifica con [locatorFrame]
	 */
	public void changeFrame(By locatorFrame) {
		this.changeFrame(this.element(locatorFrame));
	}
	/**
	 * Cambia al frame cuyo WebElement corresponde a [element]
	 */
	public void changeFrame(WebElement element) {
		this.driver.switchTo().frame(element);
	}
	/**
	 * Select a frame by its name or ID. Frames located by matching name attributes are always given precedence over
	 * those matched by ID. [nameIdFrame] the name of the frame window, the id of the frame.
	 * This driver focused on the given frame
	 */
	public void changeFrame(String nameIdFrame) {
		if (this.frameActual.equals(nameIdFrame)) return; // YA SE ENCUENTRA EN EL FRAME DESEADO
		
		if (nameIdFrame.isEmpty()) this.changeFrame(); // CAMBIA AL POR DEFECTO
		else {
			try {
				this.driver.switchTo().frame(nameIdFrame); // CAMBIA AL INDICADO
				this.frameActual = nameIdFrame;
			} catch (Exception e) { // SALE EXCEPCI�N PORQUE EL FRAME NO SE ENCUENTRA
				// HACE NADA!!!
			}
		}
	}

	public void printAllElements(String xpath) {
		if (xpath.isEmpty()) xpath = "//*"; // Busca todo
		List<WebElement> elements = this.findElements(By.xpath(xpath));
		System.out.println("Se encontraron [" + elements.size() + "] elementos...");
		int cont = 1;
		String tagName;
		for (WebElement element : elements) {
			tagName = element.getAttribute("tagName");
			try {
				if (tagName.equalsIgnoreCase("SPAN"))
					System.out.println(cont++ + " [" + tagName + " - " + element.getAttribute("class") + "]");
				else if (tagName.equalsIgnoreCase("A"))
					System.out.println(cont++ + " [" + tagName + " - " + element.getText() + "]");
				else
					System.out.println(cont++ + " [" + tagName + " - " + element.getAttribute("id") + "]");
			} catch (Exception e) {
				System.out.println(cont++ + " [" + tagName + "]");
			}
		}
	}
//-----------------------------------------------------------------------------------------------------------------------
	public void focus(By locator) {
		this.focus(this.element(locator));
	}
	public void focus(WebElement element) {
		new Actions(this.driver).moveToElement(element).build().perform();
		//new Actions(this.driver).moveToElement(element).perform();
		/*
		if (element.getTagName().equals("input")) {
			element.sendKeys(Keys.SHIFT);
		} else {
			new Actions(this.driver).moveToElement(element).perform();
		}
		******************************************************************
        WebElement txtUsername = driver.findElement(By.id("email")); 
        Actions builder = new Actions(driver); 
        Action seriesOfActions = builder 
        .moveToElement(txtUsername) 
        .click() 
        .keyDown(txtUsername,Keys.SHIFT) 
        .sendKeys(txtUsername,"hello") 
        .keyUp(txtUsername,Keys.SHIFT) 
        .doubleClick() 
        .build(); 
        seriesOfActions.perform(); 
		******************************************************************
		*/
	}
	
	public void unFocus(By locator) {
		this.unFocus(this.element(locator));
	}
	public void unFocus(WebElement element) {
		jse.executeScript("arguments[0].blur()", element);
	}
	
	public void clearInputbox(By locator) {
		this.clearInputbox(this.element(locator));
	}
	public void clearInputbox(WebElement element) {
		if (!element.getText().isEmpty()) element.clear(); // LO LIMPIA SI NO EST� VAC�O
	}
	
	/**
	 * Escribe en el elemento identificado con [locator] el valor [inputText], si el elemento contiene informaci�n S�
	 * borra lo que contiene.
	 */
	public void write(By locator, String inputText) {
		this.write(this.element(locator), inputText);
	}
	/**
	 * Escribe en el [element] el valor [inputText], si el elemento contiene informaci�n S� borra lo que contiene.
	 */
	public void write(WebElement element, String inputText) {
		this.clearInputbox(element);
		element.sendKeys(inputText);
		if (this.navegador.equals(BasePageWeb.IEXPLORE)) this.unFocus(element);
	}
	
	/**
	 * Escribe en el elemento identificado con [locator] el valor [inputText], si el elemento contiene informaci�n NO
	 * borra lo que contiene.
	 */
	public void writeNoClear(By locator, String inputText) {
		this.writeNoClear(this.element(locator), inputText);
	}
	/**
	 * Escribe en el [element] el valor [inputText], si el elemento contiene informaci�n NO borra lo que contiene.
	 */
	public void writeNoClear(WebElement element, String inputText) {
		element.sendKeys(inputText);
		if (this.navegador.equals(BasePageWeb.IEXPLORE)) this.unFocus(element);
	}
	
	public void submit(By locator) {
		this.submit(this.element(locator));
	}
	public void submit(WebElement element) {
		element.submit();
	}
	
	public void click(By locator) {
		this.click(this.element(locator));
	}
	public void click(WebElement element) {
		try {
			if (this.navegador.equals(BasePageWeb.IEXPLORE))
				jse.executeScript("arguments[0].click()", element);
			else
				element.click();
		} catch (ElementClickInterceptedException e) {
			// HACE SCROLL HASTA QUE EL ELEMENTO SEA ENCONTRADO:
			jse.executeScript("arguments[0].scrollIntoView();", element);
			element.click();
		}
	}
	/**
	 * Da click en el bot�n cuyo nombre es [nameButton].
	 * Funciona para los button que son INPUT y con type='submit'.
	 */
	public void clickButton(String nameButton) throws Exception {
		this.clickButton(nameButton, 0);
	}
	/**
	 * Da click en el bot�n cuyo nombre es [nameButton] y el �ndice corresponde a [indexButton].
	 * Funciona para los button que son INPUT y con type='submit'.
	 */
	public void clickButton(String nameButton, int indexButton) throws Exception {
		String xpath = "//input[@type='submit'][@value='" + nameButton + "']";
		List<WebElement> elements = this.findElements(By.xpath(xpath));
		if (indexButton >= elements.size() )
			throw new Exception("BasePageWeb ERROR -- No se encuentra el button [" + nameButton + " - " + indexButton + "]");
		this.click(elements.get(indexButton));
	}

	public void checkCheckBox(By locator) {
		this.checkCheckBox(this.element(locator));
	}
	public void checkCheckBox(WebElement element) {
		if (! this.isSelected(element)) this.click(element);
	}

	public void uncheckCheckBox(By locator) {
		this.uncheckCheckBox(this.element(locator));
	}
	public void uncheckCheckBox(WebElement element) {
		if (this.isSelected(element)) this.click(element);
	}
	
	//El index empieza en cero (0): Se sugiere que el locatorRadioGroup contenga todos los RadioButton
	public void selectRadioButtonByIndex(By locatorRadioGroup, int index) {
		List<WebElement> radioButtons = this.findElements(locatorRadioGroup);
		int totalRBs = radioButtons.size(); 
		if (totalRBs == 0)
			throw new NoSuchElementException("Locator not found");
		else if (index > totalRBs - 1) 
			throw new NoSuchElementException("Radio button with index [" + index + "] not found");
		else
			this.click(radioButtons.get(index));
	}
	
	//Se sugiere que el locatorRadioGroup contenga todos los RadioButton
	public void selectRadioButtonByValue(By locatorRadioGroup, String value) {
		List<WebElement> radioButtons = this.findElements(locatorRadioGroup);
		int totalRBs = radioButtons.size();
		boolean encontrado = false;
		String textoRadio = "";
		if (totalRBs == 0)
			throw new NoSuchElementException("Locator not found");
		else {
			for (WebElement radio : radioButtons) {
				textoRadio = this.getText(radio).trim(); //Quita los espacios iniciales y finales
				if (textoRadio.equalsIgnoreCase(value)) {
					this.click(radio);
					encontrado = true;
				}
			}
		}
		if (!encontrado) 
			throw new NoSuchElementException("Radio button with value [" + value + "] not found");
	}
	
	//Forma de seleccionar un elemento en una lista: HTML <select> y <option>
	public void selectItemExacto_OLD(By locator, String item) {
		
		WebElement dropDownList = this.element(locator);
		List<WebElement> options = dropDownList.findElements(By.tagName("option"));
		for (WebElement option : options) {
			if (getText(option).equals(item)) {
				click(option);
				break;
			}
		}
		
	}
	
	//Forma de seleccionar con objeto "Select": HTML <select> y <option>
	public void selectListItemByIndex(By locator, int index) {
		this.selectListItemByIndex(this.element(locator), index);
	}
	public void selectListItemByIndex(WebElement element, int index) {
		Select selectList = new Select(element);
		selectList.selectByIndex(index);
	}
	
	public void selectListItemExacto(By locator, String item) {
		this.selectListItemExacto(this.element(locator), item);
	}
	public void selectListItemExacto(WebElement element, String item) {
		Select selectList = new Select(element);
		selectList.selectByVisibleText(item);
	}
	
	/**
	 * Selecciona de forma aletoria uno de los elementos existentes en el elemento identificado con [locator]
	 * [with1stItem] indica si se debe incluir en la aleatoriedad el primer elemento de la lista, aveces es un item
	 * que no deber�a ser seleccionado.
	 */
	public void selectListItemRandom(By locator, boolean with1stItem) {
		this.selectListItemRandom(this.element(locator), with1stItem);
	}
	/**
	 * Selecciona de forma aletoria uno de los elementos existentes en el WebElement [element]
	 * [with1stItem] indica si se debe incluir en la aleatoriedad el primer elemento de la lista, aveces es un item
	 * que no deber�a ser seleccionado.
	 */
	public void selectListItemRandom(WebElement element, boolean with1stItem) {
		Select selectList = new Select(element);
		List<WebElement> options = selectList.getOptions();
		
		int firstIndex = 1;
		if (with1stItem) firstIndex = 0;
		int indexRandom = DXCUtil.enteroRandom(firstIndex, options.size()-1);
		
		this.selectListItemByIndex(element, indexRandom);
	}
	
	/**
	 * Selecciona de la lista que se encuentra en el campo identificado por [locator] el [item]
	 * La b�squeda del elemento NO es case sensitive NI tiene en cuenta acentos, y selecciona el elemnto si est� contenido.
	 * @param locator - Debe ser un [select]
	 * @param item - Elemento a seleccionar
	 * @return Mensaje de error en caso que no haya encontrao el elemento. El retorno es VACIO si se pudo seleccionar
	 */
	public String selectListItem(By locator, String item) {
		return selectListItem(this.element(locator), item);
	}					
	/**
	 * Selecciona de la lista que se encuentra en el campo identificado por [element] el [item]
	 * La b�squeda del elemento NO es case sensitive NI tiene en cuenta acentos, y selecciona el elemento si est� contenido.
	 * @param element - Debe ser un [select]
	 * @param item - Elemento a seleccionar
	 * @return Mensaje de error en caso que no haya encontrado el elemento. El retorno es VACIO si se pudo seleccionar
	 */
	public String selectListItem(WebElement element, String item) {
		
		Select selectList = new Select(element);
		String valActual = this.getText(selectList.getFirstSelectedOption());
		if (DXCUtil.equalsIgnoreCaseAndAccents(valActual, item)) return ""; // YA EST� SELECCIONADO
		
		else if (DXCUtil.containsIgnoreCaseAndAccents(valActual, item) || DXCUtil.containsIgnoreCaseAndAccents(item, valActual)) {
			System.out.println("Valor ya seleccionado [" + valActual + "] - no es igual a [" + item + "] pero se deja por estar contenido.");
			return ""; // YA EST� SELECCIONADO
		}
		
		List<WebElement> options = selectList.getOptions();
		return this.getItemInOptions(options, item);
	}
	
	public void selectMultipleItems(By locator, String... items) {
		this.selectMultipleItems(this.element(locator), items);
	}
	public void selectMultipleItems(WebElement element, String... items) {
		Select selectList = new Select(element);
		if (!selectList.isMultiple())
			throw new NoSuchElementException("Select Element is not Multiple");
		else {
			for (String item : items) {
				selectList.selectByVisibleText(item);
			}
		}
	}
	
	public void selectMultipleItems(By locator, int... indexes) {
		this.selectMultipleItems(this.element(locator), indexes);
	}
	public void selectMultipleItems(WebElement element, int... indexes){
		Select selectList = new Select(element);
		if (!selectList.isMultiple())
			throw new NoSuchElementException("Select Element is not Multiple");
		else {
			for (int index : indexes) {
				selectList.deselectByIndex(index);
			}
		}
	}
	
	//Ejemplo sacado de >> https://react-bootstrap.github.io/components/dropdowns/
	//Con HTML diferente a <select> y <option>
	public String selectListItem(By locatorDropDown, By locatorOptions, String item) {
		click(locatorDropDown);
		List<WebElement> options;
		do { //Espera mientras no se carguen las opciones
			this.waitInPage(1);
			options = this.findElements(locatorOptions);
		} while (options.size() == 0);
		
		return this.getItemInOptions(options, item);
	}
	
	// M�todo usado por los m�todos [selectItem]
	private String getItemInOptions(List<WebElement> options, String item) {
		
		String msgRetorno = "Elemento [" + item + "] NO presentado en la lista, tampoco hay un valor parecido."; // VALOR POR DEFECTO
		String itemBuscar = item.trim(); //Por si tiene espacios, se le quita.
		String itemInList = "";
		boolean itemEncontrado;
		for (WebElement option : options) {
			itemInList = this.getText(option).trim(); //Se le quitan los espacios por si tiene.
			itemEncontrado = DXCUtil.containsIgnoreCaseAndAccents(itemInList, itemBuscar) ||
					DXCUtil.containsIgnoreCaseAndAccents(itemBuscar, itemInList);
			if (itemEncontrado) {
				this.click(option);
				System.out.println("Valor seleccionado [" + itemInList + "] - si no es igual a [" + itemBuscar + "] se selecciona porque es similar.");
				msgRetorno = "";
				break;
			}
		}
		return msgRetorno;
	}
	
	/**
	 * Retorna el dato existente en la celda del elemento identificado con [locator] de la [fila] y [columna] indicados.
	 */
	public String getCellValue(By locator, int fila, int columna) {
		return this.getCellValue(this.element(locator), fila, columna);
	}
	/**
	 * Retorna el dato existente en la celda del elemento [elementWebTable] de la [fila] y [columna] indicados.
	 */
	public String getCellValue(WebElement elementWebTable, int fila, int columna) {
		By locCell = By.xpath("//tr[" + fila + "]/td[" + columna + "]");
		return this.getText(elementWebTable.findElement(locCell));
		// return elementWebTable.findElement(locCell).getText();
	}
	
	/**
	 * Retorna el n�mero de filas que tiene el elemento identificado con [locator].
	 */
	public int getRowCount(By locator) {
		return this.getRowCount(this.element(locator));
	}
	/**
	 * Retorna el n�mero de filas que tiene el elemento [elementWebTable].
	 */
	public int getRowCount(WebElement elementWebTable) {
		return this.findElements(elementWebTable, By.tagName("tr")).size();
		// return elementWebTable.findElements(By.tagName("tr")).size();
	}
	
	/**
	 * Retorna el n�mero de columnas que tiene el elemento identificado con [locator] en la [fila] indicada.
	 */
	public int getColumnCount(By locator, int fila) {
		return this.getColumnCount(this.element(locator), fila);
	}
	/**
	 * Retorna el n�mero de columnas que tiene el elemento [elementWebTable] en la [fila] indicada.
	 */
	public int getColumnCount(WebElement elementWebTable, int fila) {
		//By locFila = By.xpath("//tr[" + fila + "]");
		//return elementWebTable.findElement(locFila).findElements(By.tagName("td")).size();
		By locCols = By.xpath("//tr[" + fila + "]/td");
		//By locCols = By.xpath("//tr[" + fila + "]"); OJO NO S� CU�L USAR VALIDARLO DONDE HAYAN M�S TABLAS
		List<WebElement> elems = elementWebTable.findElements(locCols);
		return elems.size()-1;
	}
	
	/**
	 * Este m�todo s�lo funciona si el elemento a localizar en la p�gina est� de la forma <input type="file">
	 * @param locator : elemento a buscar en la p�gina web.
	 * @param nbFilePath : Ruta absoluta con ruta y extensi�n del arcivo a cargar.
	 */
	public void uploadFile(By locatorInputFile, String nbFilePath, By locatorButtonUpload) {
		this.write(locatorInputFile, nbFilePath);
		this.click(locatorButtonUpload);
	}
	
	public void uploadFileOpeningWin(By locatorButtOpenUploadWind, String nbFilePath, By locatorButtonUpload) {

       //Put path to your file in a clipboard
       StringSelection ss = new StringSelection(nbFilePath);
       Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);

       //Imitate mouse events like ENTER, CTRL+C, CTRL+V
       try {
    	   //Open upload window
	   	   this.click(locatorButtOpenUploadWind);
           
	   	   Robot robot = new Robot();
           robot.delay(250);
           robot.keyPress(KeyEvent.VK_ENTER);
           robot.keyRelease(KeyEvent.VK_ENTER);
           robot.keyPress(KeyEvent.VK_CONTROL);
           robot.keyPress(KeyEvent.VK_V);
           robot.keyRelease(KeyEvent.VK_V);
           robot.keyRelease(KeyEvent.VK_CONTROL);
           robot.keyPress(KeyEvent.VK_ENTER);
           robot.delay(50);
           robot.keyRelease(KeyEvent.VK_ENTER);
           
           this.click(locatorButtonUpload);
           
       } catch (Exception e) {
    	   e.printStackTrace();
       }
	}
	
//-----------------------------------------------------------------------------------------------------------------------
	public WebElement element(By locator) {
		WebElement elementRet = null;
		try {
			elementRet = this.driver.findElement(locator);
		} catch (NoSuchElementException e) {
			elementRet = null;
		}
		return elementRet;
	}
	/**
	 * M�todo que retorna el WebElement que cumpla con la expresi�n [expressionXpath] esta expresi�n es para buscar
	 * por locator XPATH todos los elementos que cumplen, deber�a haber s�lo 1, pero si no hay o hay muchos retorna
	 * [null]
	 */
	public WebElement element(String expressionXpath) {
		WebElement elementRet = null;
		List<WebElement> elements = this.findElements(By.xpath(expressionXpath));
		int totalElements = elements.size();
		// SI NO HAY ELEMENTOS O HAY MUCHOS EL RETORNO ES [null]
		if (totalElements == 1) elementRet = elements.get(0);
		else if (totalElements > 1) System.err.println("BasePageWe ERROR -- Hay varios elementos [" + expressionXpath + "]");
		return elementRet;
	}
	
	/**
	 * Retorna el [WebElement] que se encuentre presente en la p�gina, que corresponde al tag[tagName] y que adicional
	 * cuenta con las propiedades que se reciben. En caso que [tagName] sea null o vac�o busca en todos los TAGs en la
	 * p�gina. Las [properties] se reciben en el formato [atributo:=valorAtributo]
	 * Si el elemento NO existe o HAY MUCHOS retorna el objeto en null.
	 */
	public WebElement element(String tagName, String... properties) {
		
		WebElement elementRet = null;
		// ARMA EL VALOR DEL XPATH:
		String xpath = "//" + tagName;
		String addTag = "";
		if (tagName == null) xpath = "//*";
		else if (tagName.isEmpty()) xpath = "//*";
		else if (tagName.equals("table")) addTag = "/tbody";
		
		for (String property : properties) {
			String[] fields = property.split(":="); //0:atributo, 1:valor
			xpath += "[@" + fields[0] + "='" + fields[1] + "']";
		}
		xpath += addTag;
		// BUSCA EL ELEMENTO:
		List<WebElement> elements = this.findElements(By.xpath(xpath));
		if (elements.size() == 1) elementRet = elements.get(0);
		return elementRet;
	}
	
	public List<WebElement> findElements(By locator) {
		return this.driver.findElements(locator); 
	} 
	// Busca el locator indicado dentro del 'element' recibido: OJO ele [elemnt] debe existir
	public List<WebElement> findElements(WebElement element, By locator) {
		return element.findElements(locator);
	}
	/**
	 * Retorna una lista de [WebElement] que se encuentren presente en la p�gina, que correspondan al tag[tagName] y que
	 * adicional cuentan con las propiedades que se reciben. En caso que [tagName] sea null o vac�o busca en todos los
	 * TAGs en la p�gina. Las [properties] se reciben en el formato [atributo:=valorAtributo]
	 */
	public List<WebElement> findElements(String tagName, String... properties) {
		
		// ARMA EL VALOR DEL XPATH:
		String xpath = "//" + tagName;
		if (tagName == null) xpath = "//*";
		else if (tagName.isEmpty()) xpath = "//*";
		
		for (String property : properties) {
			String[] fields = property.split(":="); //0:atributo, 1:valor
			xpath += "[@" + fields[0] + "='" + fields[1] + "']";
		}
		// RETORNA EL LISTADO DE ELEMENTOS:
		return this.findElements(By.xpath(xpath));
	}
//-----------------------------------------------------------------------------------------------------------------------
	/**
	 * S�lo sirve para hacer espera en la p�gina Web, si hay una alerta en la p�ina Web se genera Excepci�n. 
	 * @param segundos
	 */
	public void waitInPage(int segundos) {
		this.driver.manage().timeouts().implicitlyWait(segundos, TimeUnit.SECONDS);
	}
	
	public String getText(By locator) {
		return this.getText(this.element(locator));
	}
	public String getText(WebElement element) {
		return element.getText();
	}
	
	public Boolean isDisplayed(By locator) {
		boolean valRetorno = false;
		if (this.element(locator) != null) valRetorno = this.isDisplayed(this.element(locator));
		return valRetorno;
	}
	public Boolean isDisplayed(WebElement element) {
		boolean isDisplay;
		try {
			isDisplay = element.isDisplayed();
		} catch (Exception e) {
			isDisplay = false; // EN CASO QUE HAYA DESAPARECIDO EL [WebElement]
		}
		return isDisplay;
	}
	
	public Boolean isSelected(By locator) {
		boolean valRetorno = false;
		if (this.element(locator) != null) valRetorno = this.isSelected(this.element(locator));
		return valRetorno;
	}
	public Boolean isSelected(WebElement element) {
		boolean isSelect;
		try {
			isSelect = element.isSelected();
		} catch (Exception e) {
			isSelect = false; // EN CASO QUE HAYA DESAPARECIDO EL [WebElement]
		}
		return isSelect;
	}
	
	public Boolean isEnabled(By locator) {
		boolean valRetorno = false;
		if (this.element(locator) != null) valRetorno = this.isEnabled(this.element(locator));
		return valRetorno;
	}
	public Boolean isEnabled(WebElement element) {
		boolean isEnab;
		try {
			isEnab = element.isEnabled();
		} catch (Exception e) {
			isEnab = false; // EN CASO QUE HAYA DESAPARECIDO EL [WebElement]
		}
		return isEnab;
	}
	// Tiene muchos m�todos, incluir los que se necesiten

//-----------------------------------------------------------------------------------------------------------------------
	/**
	 * M�todo que indica si el Browser correspondiente al driver actual, cuenta con una ventana de Browser abierta.
	 * Se usa para crear de nuevo el objeto, o para darle espera a que se abra.
	 */
	public boolean browserIsEnabled() {
		try {
			this.driver.manage().window().getSize(); // SE HACE ESTE LLAMADO PARA VER SI ALCANZA LA VENTANA
			return true;
		} catch (WebDriverException e) {
			return false;
		}
	}
	
//-----------------------------------------------------------------------------------------------------------------------
	/**
	 * Hace scroll al inicio : teniendo como referencia el [element] el cual debe ser un [android.widget.ListView] 
	 */
	public void downloadIE(WebElement element) throws InterruptedException{
	    try {
	        //get the focus on the element..don't use click since it stalls the driver          
	        //this.click(element);
	        //Thread.sleep(2000);
	        
	    	Robot robot = new Robot();
	    	System.out.println("Creando robot...");
	    	//simulate pressing enter            
	    	robot.keyPress(KeyEvent.VK_ALT);
	    	System.out.println("Envi� el ALT");
	    	robot.delay(10000);
	    	System.out.println("Termin� espera de 10");
	    	robot.keyPress(KeyEvent.VK_S);
	    	System.out.println("Envi� la S");
	    	robot.keyRelease(KeyEvent.VK_S);
	    	System.out.println("Suelta la S");
	    	robot.keyRelease(KeyEvent.VK_ALT);
	    	System.out.println("Suelta el ALT");
	    } catch (java.awt.AWTException e) {
	            e.printStackTrace();
	    }
	}
	
	public static void OJOParaStratus(WebElement element) throws InterruptedException{
	    try {
	    	Robot robot = new Robot();
	        //get the focus on the element..don't use click since it stalls the driver          
	        element.sendKeys("");
	        //simulate pressing enter            
	        robot.keyPress(KeyEvent.VK_ENTER);
	        robot.keyRelease(KeyEvent.VK_ENTER);
	        //wait for the modal dialog to open            
	        Thread.sleep(2000);
	        //press s key to save            
	        robot.keyPress(KeyEvent.VK_S);
	        robot.keyRelease(KeyEvent.VK_S);
	        Thread.sleep(2000);
	        //press enter to save the file with default name and in default location
	        robot.keyPress(KeyEvent.VK_ENTER);
	        robot.keyRelease(KeyEvent.VK_ENTER);
	    } catch (java.awt.AWTException e) {
	            e.printStackTrace();
	    }
	    String text = "Hello World";
	    StringSelection stringSelection = new StringSelection(text);
	    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    clipboard.setContents(stringSelection, stringSelection);
	}
	
}


/*
Uso de Id. De cuadro (solo debe usarse si conoce el id del iframe).
driver.switchTo().frame("imgboxId"); //imgboxId - Id of the frame

Usando el nombre del marco (debe usarse solo si conoce el nombre del iframe).
driver.switchTo().frame("imgboxName"); //imgboxName - Name of the frame

Uso del �ndice de cuadros (debe usarse solo si no tiene el ID o el nombre del iframe), donde el �ndice define la posici�n del iframe entre todos los cuadros.
driver.switchTo().frame(0); //0 - Index of the frame
Nota: Si tiene tres cuadros en la p�gina, entonces el primer cuadro estar� en el �ndice 0, el segundo en el �ndice 1 y el tercero en el �ndice 2.

Usar el elemento web ubicado anteriormente (debe usarse solo si ya ha localizado el marco y lo ha devuelto como un elemento WebElement ).
driver.switchTo().frame(frameElement); //frameElement - webelement that is the frame

Entonces, para hacer clic en el ancla de la Red Ball :
driver.switchTo().frame("imgboxId");
driver.findElement(By.linkText("Red Ball")).Click();





*
*/
