package dxc.execution;

import org.testng.annotations.Test;

import dxc.util.DXCUtil;

import org.testng.annotations.DataProvider;

/*
 * Así se ejecutan:
 * BeforeSuite - BeforeTest - BeforeClass - DataProvider
 * [BeforeMethod - Test - AfterMethod]
 * AfterClass - AfterTest - AfterSuite
 */
public class BaseTestNG { 

//=======================================================================================================================
	// HACE LA CARGA DE LA CONFIGURACIÓN DE LAS EJECUCIONES A REALIZAR...
	@DataProvider(name = "globalData")
	public Object[][] getData() throws Exception {
		
		SettingsRun.loadSetting(1);
		
		// El tamaño de la matriz es [totalExec][1], van los Rows del Excel que se van a ejecutar
		Object[][] data = SettingsRun.loadDataProvider();
		
		// Hace el llamado a la carga del ambiente
		loadEnvironment(); 
		
		return data;
	}

//=======================================================================================================================
	// SE HACE EL LLAMADO AL MÉTODO QUE DA INICIO A LA PRUEBA, SE INDICA QUE SE VA A USAR LA DATA DEL [DataProvider]... 
	@Test(dataProvider = "globalData")
	public void DXCExec(int iteracion) {
		 
		try {
			SettingsRun.loadIteration(iteracion); //PARA QUE CARGUE LA DATA DE LA ITERACI�N CORRESPONDIENTE
			// HACE EL LLAMADO A LAS CONFIGURACIONES INICIALES
			if (SettingsRun.esIteracionInicial()) {
				Reporter.write("\n*** HACIENDO LAS CONFIGURACIONES INICIALES >>> (" + DXCUtil.hourToString("HH:mm:ss") + ")");
				doingConfigurations();
				Reporter.write(  "*** TERMINANDO LAS CONFIGURACIONES INICIALES >>> (" + DXCUtil.hourToString("HH:mm:ss") + ")");
			}
			// EMPIEZAN LAS PRUEBAS
			Reporter.write("\n*** ROW " + iteracion + " >>> (" + DXCUtil.hourToString("HH:mm:ss") + ")");
			doingTest();
		} catch (Exception e) {
			// LO HACE DE ESTA MANERA PORQUE A NULL NO SE LE PUEDE HACER EL .EQUALS
			String msgErr = "null";
			if (e.getMessage() != null) msgErr = e.getMessage();
			
			// SI ES IGUAL A "BaseTestDONE -- exitTestIteration" NO ES EXCEPTION, ES CONTINUAR CON LA SIGUIENTE ITERACI�N
			if (!msgErr.equals("BaseTestDONE -- exitTestIteration")) {
				System.out.println("\n*** CAUSE ---------- " + e.getCause());
				try {
					Reporter.reportEvent(Reporter.MIC_FAIL, msgErr); 
				} catch (Exception e2) {
					System.out.println("Se ignora Exception de Reporter : " + e2.getMessage());
				}
				e.printStackTrace();
			} 
		}
	}
	
//=======================================================================================================================
	/**
	 * Este es el m�todo que debe ser sobre escrito en las clases que hereden de esta.
	 * Es para cargar el ambiente, es llamado por [@DataProvider].
	 */
	public void loadEnvironment() throws Exception {
		System.out.println("Este m�todo se debe sobre escribir si se requiere...");
	}
	/**
	 * Este es el m�todo que se invoca para realizar las configuraciones iniciales. Es invocado en la primera iteraci�n.
	 */
	public void doingConfigurations() throws Exception {
		System.out.println("Este m�todo se debe sobre escribir con las configuraciones iniciales antes de empezar las pruebas...");
	}
	/**
	 * Este es el m�todo que debe ser sobre escrito en las clases que hereden de esta.
	 * Es el inicio de TODAS las pruebas.
	 */
	public void doingTest() throws Exception {
		System.out.println("OBLIGATORIO -- Este m�todo se debe sobre escribir...");
	}

}
