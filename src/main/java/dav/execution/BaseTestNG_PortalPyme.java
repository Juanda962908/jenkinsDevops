package dav.execution;

import javax.swing.JOptionPane;

import org.testng.annotations.AfterClass;

import dav.transversal.DatosEmpresarial;
import dxc.execution.BaseTestNG;
import dxc.execution.Reporter;
import dxc.execution.SettingsRun;
import dxc.util.DXCUtil;

public class BaseTestNG_PortalPyme extends BaseTestNG {

	//private PortalPyme page; 
	
    @AfterClass
	public void afterClass() {
    	Reporter.write("\n*** HORA DE CIERRE (" + DXCUtil.hourToString("HH:mm:ss") + ")");
    	SettingsRun.liberarDataGlobal(); // LIBERA [DataGobal]
    	JOptionPane.showMessageDialog(null, "VER EVIDENCIAS EN LA CARPETA\n" + SettingsRun.RESULT_DIR);
    	// BaseWindowsApp.destroyWinAppDriver();
	}

    // MÉTODO REQUERIDO PARA CARGAR EL AMBIENTE : INVOCADO POR @DataProvider PARA CARGAR EL AMBIENTE
    public void loadEnvironment() throws Exception {
    	DatosEmpresarial.AMBIENTE_TEST = DatosEmpresarial.AMB_PROYECTOS;// DavUtil.seleccionAmbiente();
    }
    	

}
