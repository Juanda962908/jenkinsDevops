package launchTest.empresarial.web;

import java.io.File;

import org.testng.annotations.BeforeClass;

import dav.application.PortalPyme;
import dav.execution.BaseTestNG_PortalPyme;
import dav.transversal.DatosEmpresarial;
import dxc.execution.BasePageWeb;
import dxc.execution.Evidence;
import dxc.execution.Reporter;
import dxc.execution.SettingsRun;

public class LogueoPyme extends BaseTestNG_PortalPyme {

	PortalPyme portalPyme = null;

	@BeforeClass
	public void beforeClass() throws Exception {
		SettingsRun.EXEC_CLASS = this.getClass().getSimpleName(); // OBLIGATORIO - NOMBRE DE LA CARPETA DE EVIDENCIAS
		SettingsRun.RESULT_DIR = SettingsRun.getResultDir();
		File directory = new File(SettingsRun.RESULT_DIR);
		directory.mkdirs();
		
		Evidence.siTomarla(true); // TOMAR EVIDENCIAS [true] = POR ROW
		System.out.println("\n*** PRUEBA LOGUEO FRONT PYMES ***");
	}

	public void doingConfigurations() throws Exception {
		Reporter.write("NO HAY CONFIGURACIONES INICIALES");
	}

	public void doingTest() throws Exception {

		// String navegador = SettingsRun.getGlobalData().getParameter("Navegador");
		// DatosEmpresarial.loadLoginData("CliEmpresarial", "TipoDoc", "NumeroDoc",
		// "Clave", "TipoToken", "Valor/Semilla/Celular");
		String navegador = BasePageWeb.CHROME;
		DatosEmpresarial.loadLoginDataFija("802", "CÈDULA DE CIUDADANÎA", "852", "123456",
				DatosEmpresarial.TOKEN_ESTATIC, "66710818");
		
		// CREA INSTANCIA DE [PortalPyme] : SI NO SE HA HECHO O SI NO ESTÂ LOGUEADO EN WEB, PORQUE SI NO ESTÂ LOGUEADO
		// EL NAVEGADOR PARA EL PORTAL PYME NO EST� ABIERTO
		if (portalPyme == null || !DatosEmpresarial.ESTALOG_WEB) {
			portalPyme = new PortalPyme(navegador);
			portalPyme.maximizeBrowser();
		}
		
		// SI NO ES LA ITERACI�N INICIAL PERO EL BROWSER NO EST� HABILITADO, SE CREA DE
		// NUEVO LA INSTANCIA
		// if (!logueo.browserIsEnabled()) logueo = new PortalPyme(navegador);
		
		portalPyme.loginFront();
		portalPyme.closePopupActiveCtas(); // EN CASO QUE EXISTA EL POPUP DE ACTIVACI�N DE CUENTAS LO CIERRA
		Evidence.saveFullPage("Exito", portalPyme);
		Reporter.reportEvent(Reporter.MIC_PASS, "Logueo Exitoso");
		
		if (portalPyme.isTimeCloseSession())
			portalPyme.closeSession();

		/*
		 * logueo = new PortalPyme(BasePageWeb.IEXPLORE); JavascriptExecutor js; By
		 * locAyuda = By.linkText("Ayuda"); logueo.navigate("http://google.com"); do {
		 * WebElement el = null; do { el = logueo.findElement(By.name("q")); } while (el
		 * == null); logueo.write(el, "automatizaci�n"); el.submit();
		 * 
		 * System.out.println("caso"); js = (JavascriptExecutor) logueo.getDriver();
		 * js.executeScript("window.scrollBy(0,1000)");
		 * 
		 * WebElement element = logueo.findElement(locAyuda);
		 * js.executeScript("arguments[0].scrollIntoView", element);
		 * 
		 * js.executeScript("window.scrollTo(0, 0");
		 * 
		 * js.executeScript("window.scrollTo(0, document.body.scrollHeight"); } while
		 * (true);
		 */

	}

}
