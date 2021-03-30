package dav.application;

import java.util.ArrayList;
import java.util.Date;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import dav.transversal.DS_DaviviendaUrls;
import dav.transversal.DatosEmpresarial;
import dav.transversal.DavUtil;
import dxc.execution.BasePageWeb;
import dxc.execution.Evidence;
import dxc.execution.Reporter;
import dxc.execution.SettingsRun;
import dxc.util.DXCUtil;

public class PortalPyme extends BasePageWeb {
	
	// SE USA PARA DETERMINAR LAS URLS A USAR EN LA PARTE WEB, SI SE REQUIERE:
	protected static String URL_FRONT  = "";
	protected static String URL_MIDDLE = "";
	
	protected String portalCargado = "";

	public PortalPyme(String navegador) {
		super(navegador);
		try {
			this.cargarUrls(); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public PortalPyme(String navegador, String downloadFilePath) {
		super(navegador, downloadFilePath);
		try {
			this.cargarUrls();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void destroy() {
		this.closeAllBrowsers();
		this.portalCargado = "";
	}

	/**
	 * Método que carga las URLs de Front y Middle si no se han cargado.
	 */
	private void cargarUrls() throws Exception {
		if (DatosEmpresarial.AMBIENTE_TEST.equals(""))
			throw new Exception("PortalPyme ERROR -- No se ha cargado el ambiente de trabajo...");

		// CARGA LAS URLs SI NO SE HAN CARGADO:
		if (PortalPyme.URL_FRONT.equals("") || PortalPyme.URL_MIDDLE.equals("")) {
			DS_DaviviendaUrls datosUrl = new DS_DaviviendaUrls();
			PortalPyme.URL_FRONT  = datosUrl.getUrl(DatosEmpresarial.AMBIENTE_TEST, "PYMES FRONT");
			PortalPyme.URL_MIDDLE = datosUrl.getUrl(DatosEmpresarial.AMBIENTE_TEST, "PYMES MIDDLE");
		}
	}
	
	/**
	 * Método para hacer el logueo en el portal Front Pyme. 
	 * Retorna un String que indica si fue exitoso el logueo: cuando retorna "" es porque fue exitoso, en caso contrario
	 * retorna un mensaje que indica lo que sucedió.
	 * Requiere que se haya hecho la carga de los datos de login de [DatosEmpresarial].
	 */
	public void loginFront() throws Exception {
		if (!DatosEmpresarial.ESTALOG_WEB) {
			this.portalCargado = DatosEmpresarial.PORTAL_FRONT_PYME;
			// true = PARA QUE HAGA REINTENTO EN CASO DE FALLA, VAC�O INDICA QUE ES CON CLAVE CORRECTA
			this.login(true, "");
			// SI LLEGA ESTE PUNTO ES PORQUE SE HIZO EL LOGUEO
			DatosEmpresarial.ESTALOG_WEB = true;
			DatosEmpresarial.FECHAHORALOG_WEB = new Date();
		}
	}

	/**
	 * M�todo para hacer el logueo en el portal Front Pyme para Motor de Riesgo. 
	 * Retorna un String que indica si fue exitoso el logueo: cuando retorna "" es porque fue exitoso, en caso contrario
	 * retorna un mensaje que indica lo que sucedi�.
	 * Requiere que se haya hecho la carga de los datos de login de [DatosEmpresarial].
	 */
	public String loginFrontMR(boolean conReintento, String condicion) throws Exception {
		this.portalCargado = DatosEmpresarial.PORTAL_FRONT_PYME;
		return this.login(conReintento, condicion);
	}
	
	/**
	 * M�todo para hacer el logueo en el portal Middle Pyme. 
	 * Retorna un String que indica si fue exitoso el logueo: cuando retorna "" es porque fue exitoso, en caso contrario
	 * retorna un mensaje que indica lo que sucedi�.
	 * Requiere que se haya hecho la carga de los datos de login de [DatosEmpresarial].
	 */
	public void loginMiddle() throws Exception {
		if (!DatosEmpresarial.ESTALOGMIDD) {
			this.portalCargado = DatosEmpresarial.PORTAL_MIDDLE_PYME;
			this.login(true, ""); // VAC�O INDICA QUE ES CON CLAVE CORRECTA
			// SI LLEGA ESTE PUNTO ES PORQUE SE HIZO EL LOGUEO
			DatosEmpresarial.ESTALOGMIDD = true;
		}
	}
	
	/**
	 * Est� m�todo termina la iteraci�n actual que se est� realizando en PortalPyme.
	 * Si es tiempo de cerrar sesi�n, la cierra.
	 */
	public void terminarIteracion(String message) throws Exception {
		
		if (this.isTimeCloseSession())
			this.closeSession();
		Reporter.write(message, true);
	}

	/**
	 * Indica si es tiempo de cerrar sesi�n en el FRONT.<br>
	 * Es tiempo de cerrar sesi�n, si se cumple alguna de las siguientes condiciones:<br>
	 * 1. El tiempo de logueo permitido ha expirado.<br>
	 * 2. Es la prueba final.<br>
	 * 3. El usuario a loguear en la siguiente prueba es diferente al actual.<br>
	 * 4. El navegador a usar en la siguiente prueba es diferente al actual.
	 * @throws Exception 
	 */
	public boolean isTimeCloseSession() throws Exception {  
		boolean isTime = false;
		
		if (DavUtil.tiempoActividadExpirado("Pyme"))
			isTime = true; // CONDICI�N 1
		
		else if (SettingsRun.esIteracionFinal())
			isTime = true; // CONDICI�N 2
		
		else if (!DatosEmpresarial.currentAndNextCustomerIsEqual())
			isTime = true; // CONDICI�N 3
		
		else {
			int nextRow = SettingsRun.getNextIteration();
			try { // SE PUEDE GENERAR EXCEPCI�N POR NO TENER EL PAR�METRO "Navegador" EN LA HOJA DE DATOS
				String currentNavegador = SettingsRun.getGlobalData().getParameter("Navegador").trim();
				String nextNavegador = SettingsRun.getGlobalData().getParameterByRow("Navegador", nextRow).trim();
				isTime = !currentNavegador.equals(nextNavegador); // CONDICI�N 4
			} catch (Exception e) {
				// HACE NADA DEJA EL VALOR DE [isTime]
			}
		}
		return isTime;
	}

	/**
	 * M�todo que retorna el mensaje de alerta si este existe. Si el retorno es "" es porque NO existe un mensaje de alerta.
	 * Se recibe por par�metro un listado de los posibles id de las alertas que se pueden presentar.
	 */
	public String getMsgAlertIfExist(String... idsAlerta) {
		String msgAlert = "";
		for (String id : idsAlerta) {
			By locMessage = By.id(id);
			if (this.isDisplayed(locMessage)) {
				msgAlert = this.getText(locMessage).trim();
				if (!msgAlert.equals("")) break; // PARA TERMINAR EL CICLO
			}
		}
		return msgAlert;
	}

	/**
	 * Se encarga de darle click al elemento de CERRAR SESI�N.
	 */
	public void closeSession() {
		this.click(cmCerrSes); // DA CLICK EN EL LOCATOR DE CERRAR SESI�N
		
		boolean portalAbierto;
		do { // SE DEBE ESPERAR MIENTRAS EL PORTAL EST� ABIERTO, SE SABE:
			 // PARA EL CASO DE MIDDLE O EXPLORER - PORQUE EL CAMPO DE CERRAR SESI�N SE PRESENTA
			 // PARA EL CASO DE FRONT QUE NO ES EXPLORER - PORQUE EL BROWSER EST� ABIERTO 
			if (this.getNavegador().equals(BasePageWeb.IEXPLORE) || this.portalCargado.equals(DatosEmpresarial.PORTAL_MIDDLE_PYME))
				portalAbierto = this.isDisplayed(cmCerrSes);
			else
				portalAbierto = this.browserIsEnabled();
		} while (portalAbierto);

		// INDICA QUE YA NO EST� LOGUEADO Y GARANTIZA EL CIERRE DEL NAVEGADOR
		if (this.portalCargado.equals(DatosEmpresarial.PORTAL_FRONT_PYME)) {
			DatosEmpresarial.ESTALOG_WEB = false;
			if (this.getNavegador().equals(BasePageWeb.IEXPLORE)) this.closeAllBrowsers();
		} else {
			DatosEmpresarial.ESTALOGMIDD = false;
			this.closeAllBrowsers(); // MIDDLE DEJA UN BROWSER ABIERTO
		}
	}

	/**
	 * Primero revisa si el Popup de ACTIVE SUS CUENTAS se est� presentando, si es as�, lo cierra. 
	 */
	public void closePopupActiveCtas() {
		WebElement objPopopActiveCtas = this.element(By.id("cphCuerpo_LikActivar"));
		if (objPopopActiveCtas != null) this.click(By.cssSelector("button[class=close]"));
	}
	
//=======================================================================================================================
	// ESTOS LOCATOR FUNCIONAN TANTO PARA FRONT COMO PARA MIDDLE
	By cmCliente = By.id("divNumerClienteEmpresarial");
	By cmTipoId  = By.id("divTipoIdentificacion");
	By cmNumDocu = By.id("divNumeroDocumento");
	By cmClavePV = By.id("divClavePersonal"); // PUEDE SER CLAVE PERSONAL O VIRTUAL
	By cmNumTok  = By.id("divClaveToken");
	By cmBtIngr  = By.cssSelector("input[value=Ingresar]");
	By cmBtAcept = By.cssSelector("a[class=boldcn][aria-label=Close]"); // BOT�N ACEPTAR DE POPUPS
	
	By cmTexto   = By.cssSelector("input[type=text]");     // PARA CLIENTE EMPRESARIAL Y N�MERO DE DOCUMENTO
	By cmPassw   = By.cssSelector("input[type=password]"); // PARA CLAVE PERSONALoVIRTUAL Y N�MERO DE TOKEN
	By cmSelect  = By.cssSelector("select"); // PARA EL TIPO DE DOCUMENTO
	By cmCerrSes = By.cssSelector("a[id='CerrarSesion']"); // EN FRONT Y MIDDLE SE PRESENTA
//=======================================================================================================================
	
	/**
	 * M�todo para hacer el logueo en el portal Front Pyme o Middle Pyme, depende de lo indicado por [this.portalCargado]
	 * Requiere que se haya hecho la carga de los datos de login de [DatosEmpresarial].
	 * @param conReintento - Indica que se realice reintento si despu�s de ingresar las claves no entra al portal.
	 *                       En este caso, se asume que se espera el logueo se realice. Si no se requiere reintento y hay
	 *                       error, se retorna el mensaje presentado.
	 * @param condicion - Condici�n del logueo a realizar.
	 */
	private String login(boolean conReintento, String condicion) throws Exception {
		
		// {0-ClienteEmpresarial, 1-tipoId, 2-numId, 3-clave, 4-tipoToken}
		String[] datosLogin = DatosEmpresarial.getLoginData();
		Reporter.write("Datos de Logueo [" + DXCUtil.arrayToString(datosLogin, " - ") + "]");
		String numCliEmp = datosLogin[0];
		String tipoDoc   = datosLogin[1];
		String numDoc    = datosLogin[2];
		String clave     = datosLogin[3];
		String tipoTok   = datosLogin[4];

		// DETERMINA LA URL A CARGAR:
		String urlCargar = PortalPyme.URL_FRONT;
		if (this.portalCargado.equals(DatosEmpresarial.PORTAL_MIDDLE_PYME)) urlCargar = PortalPyme.URL_MIDDLE;
		
		// EMPIEZA A NAVEGAR:
		this.navigate(urlCargar);
		
		String msgAlerta;
		int intento = 0; // PARA SABER CU�NTOS INTENTOS PARA HACER EL INGRESO, S�LO SE PERMITIR� 2 INTENTOS
		do {
			
			do { // INGRESO DE DATOS INICIALES: MIENTRAS SALGA MENSAJE QUE DIGA QUE FALTA INGRESAR ALGO
				msgAlerta = this.ingresarDatosIniciales(numCliEmp, tipoDoc, numDoc);
			} while (msgAlerta.toUpperCase().contains("INGRESE"));
			
			
			// SI HAY MENSAJE DE ALERTA EN EL INGRESO DE LOS DATOS INICIALES: SE DEBE TERMINAR LA PRUEBA, PORQUE NO SE
			// PUDO REALIZAR EL LOGUEO
			if (!msgAlerta.equals("")) {
				String msgError = "\nRevise la data, debe haber algo ERRADO.";
				if (DXCUtil.containsIgnoreCaseAndAccents(msgAlerta, "SU CLAVE VIRTUAL EMPRESARIAL HA EXPIRADO"))
					msgError = "\nRecuerde cambiar la data con la nueva clave y si tiene otros clientes, revise sus claves antes de reiniciar la Automatizaci�n.";
				this.closeAllBrowsers();
				SettingsRun.exitTest(msgAlerta + msgError);
			}
			
			// SI LLEGA A ESTE PUNTO, PUDO REALIZAR EL INGRESO DE LOS DATOS INICIALES, INGRESA CLAVE Y TOKEN
			// (si se requiere):
			msgAlerta = this.ingresarClaveToken(clave, tipoTok, condicion);
			intento++;
			if (!msgAlerta.isEmpty()) this.navigate(urlCargar); // DEJA LA PANTALLA EN LA PANTALLA DE LOGUEO
		} while (!msgAlerta.isEmpty() && intento < 2 && conReintento); // CUANDO [msgAlerta] ES VAC�A ES PORQUE PUDO INGRESAR AL PORTAL

		if (!msgAlerta.isEmpty() && conReintento) {
			// HAY ERROR Y POR SER CON REINTENTOS, SE ASUME QUE SE ESPERABA HACER EL LOGUEO, PERO COMO PAS� EL N�MERO DE
			// INTENTOS : YA NO SE PUEDE INGRESAR LA INFORMACI�N POR TEMOR A BLOQUEOS, TERMINA LA PRUEBA ACTUAL
			Reporter.reportEvent(Reporter.MIC_NOEXEC, "[ERROR DE DATA] Revisar los datos de logueo: " + msgAlerta);
			this.closeAllBrowsers();
			SettingsRun.exitTestIteration();
		}
		return msgAlerta;
	}

	/**
	 * Retorna "" (VAC�O) si el ingreso de los datos iniciales fue exitoso, en caso contrario retorna el error presentado
	 * dejando evidencia de esto.
	 */
	private String ingresarDatosIniciales(String numCliEmp, String tipoDoc, String numDoc) throws Exception {
		
		boolean logueoShown;
		do { // ESPERA MIENTRAS NO SE MUESTRE EL CAMPO DEL TIPO DE DOCUMENTO
			logueoShown = this.isDisplayed(cmTipoId);
		} while (!logueoShown);
		
		boolean isFront = ( this.portalCargado.equals(DatosEmpresarial.PORTAL_FRONT_PYME) );
		// S�LO EN EL FRONT : INGRESA EL N�MERO DE CLIENTE EMPRESARIAL
		if (isFront) {
			this.write(this.element(cmCliente).findElement(cmTexto), numCliEmp);
		}
		
		// SELECCIONA EL TIPO DE IDENTIFICACI�N
		String msgError = this.selectListItem(this.element(cmTipoId).findElement(cmSelect), tipoDoc);
		if (!msgError.equals("")) {
			this.closeAllBrowsers();
			Reporter.writeErr("No se encuentra el tipo de documento: " + msgError, true);
		}
		
		// INGRESA EL N�MERO DE DOCUMENTO DE IDENTIFICACI�N
		this.write(this.element(cmNumDocu).findElement(cmTexto), numDoc);
		
		// S�LO EN FRONT Y SI NO SE HA DESPLEGADO EL CAMPO DE LA CLAVE : DA CLICK EN INGRESAR
		String msgAlerta = "";
		if ( isFront && !this.isDisplayed(cmClavePV) ) {
			this.click(cmBtIngr);
			boolean cmClaveShown;
			do { // ESPERA MIENTRAS NO SE MUESTRE EL CAMPO DE LA CLAVE O UN MENSAJE DE ALERTA
				cmClaveShown = this.isDisplayed(cmClavePV);
				msgAlerta = this.getMsgAlertIfExist("LbMensaje", "mensajeModal");
			} while (!cmClaveShown && msgAlerta.equals(""));
			
			if (!msgAlerta.equals("")) { // HAY MENSAJE DE ALERTA
				Evidence.save("ErrorData", this);
				if (this.isDisplayed(cmBtAcept)) this.click(cmBtAcept); // CIERRA EL MENSAJE PRESENTADO SI ES UN POPUP
			} // else : SI NO HAY ALERTA, SE PRESENT� EL CAMPO PARA INGRESAR LA CLAVE
		}
		return msgAlerta;
	}
	
	/**
	 * Retorna "" (VAC�O) si el ingreso de la clave y el token fue exitoso, en caso contrario retorna el error presentado
	 * dejando evidencia de esto.
	 */
	private String ingresarClaveToken(String clave, String tipoTok, String condicion) throws Exception {
		
		// INGRESA LA CLAVE PERSONAL O VIRTUAL
		this.write(this.element(cmClavePV).findElement(cmPassw), clave);
		
		// SI NO ES OTP : SE INGRESA EL VALOR DEL TOKEN
		if (!tipoTok.equals(DatosEmpresarial.TOKEN_OTP)) {
			String valToken = DatosEmpresarial.getValorToken(condicion);
			this.write(this.element(cmNumTok).findElement(cmPassw), valToken);
		}
		
		// DA CLICK EN INGRESAR: AVECES SE PRESENTA ERROR PORQUE SE ABRE POPUP QUE BLOQUEA EL BOT�N 
		try {
			this.click(cmBtIngr);
		} catch (Exception e) {
			this.closePopupSnippetCliente();
			this.click(cmBtIngr);
		}
		
		String msgAlerta;
		boolean entroPortal;
		do { // ESPERA MIENTRAS NO MUESTRE EL CERRAR SESI�N O SE PRESENTE UN MENSAJE DE ALERTA
			try {
				entroPortal = this.accedioAlPortal();
				msgAlerta   = this.getMsgAlertIfExist("LbMensaje", "lblMasterAlerta", "mensajeModal");
			} catch (Exception e) { // GARANTIZA QUE INGRESE DE NUEVO AL CICLO 
				entroPortal = false;
				msgAlerta = "";
			}
		} while (!entroPortal && msgAlerta.equals(""));
		
		if (entroPortal)
			msgAlerta = ""; // AVECES PUEDE ENCONTRAR UN MENSAJE DE ALERTA DENTRO DEL PORTAL, SE DEJA EN VAC�O
		else if (!msgAlerta.equals("")) { // HAY MENSAJE DE ALERTA
			Evidence.save("ErrorLogueo", this); // GUARDA EVIDENCIA DEL ERROR PRESENTADO
			if (this.isDisplayed(cmBtAcept)) this.click(cmBtAcept); // CIERRA EL MENSAJE PRESENTADO SI ES UN POPUP
		} // else: SI NO HAY ALERTA, SE INGRES� AL PORTAL
		
		return msgAlerta;
	}

// ======================================================================================================================
	
	/**
	 * Primero revisa si el PopUp en el logueo que opaca la ventana se est� presentando, si es as� lo cierra.
	 * Se reconoce porque se est� presentando el elemento de volver la p�gina opaca. 
	 */
	private void closePopupSnippetCliente() {
		WebElement objOpacity = this.element(By.id("backgroundOpacity"));
		if (objOpacity != null)
			this.click(By.id("closeButton"));
	}
	
	/**
	 * Retorna TRUE si se encuentra en la sesi�n del portal. Se reconoce porque est� el Elementod e CERRAR SESI�N.
	 * Se maneja en un m�todo, ya que el logueo puede abrir ventanas emergentes.
	 */
	private boolean accedioAlPortal() {
		
		ArrayList<String> idsWind = this.getIdWindows();
		String portalWind  = idsWind.get(idsWind.size()-1);
		this.changeWindow(portalWind);
		boolean ingreso = this.isDisplayed(cmCerrSes);
		if (ingreso) { // SI HUBO INGRESO, SE REVISA SI HAY M�S VENTANAS ABIERTAS PARA CERRARLAS
			idsWind = this.getIdWindows();
			for (int i = 0; i < idsWind.size()-1; i++) {
				this.changeWindow(idsWind.get(i));
				this.closeCurrentBrowser();
			}
			this.changeWindow(portalWind);
		}
		return ingreso;
	}

}
