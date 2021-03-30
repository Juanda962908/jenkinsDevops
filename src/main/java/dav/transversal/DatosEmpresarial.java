package dav.transversal;

import java.util.Date;

import javax.swing.JOptionPane;

import dxc.execution.SettingsRun;
import dxc.util.DXCUtil;

public class DatosEmpresarial {

	// VARIABLES:
	public static boolean ES_EXEC_APP = false;
	public static boolean ESTALOG_WEB = false; // Indica si el logueo del cliente actual estâ en Web
	public static boolean ESTALOG_APP = false; // Indica si el logueo del cliente actual estâ en App
	public static boolean ESTALOGMIDD = false; // Indica si se estâ logueado en Middle (Aplica para Pymes o Empresarial)
	public static Date FECHAHORALOG_WEB; // S�LO APLICA PARA FRONT PYME
	public static Date FECHAHORALOG_APP;
	
	// CONSTANTES:
	public static final int TIME_ACT_PYMES = 45; // TIEMPO DE ACTIVIDAD EN EL PORTAL PYMES, EN MINUTOS
	public static final int TIME_ACT_APP   = 9 ; // TIEMPO DE ACTIVIDAD EN LA APP EMPRESAS, EN MINUTOS
	
	public static final String AMB_PROYECTOS     = "PROYECTOS";
	public static final String AMB_CONTENCION    = "CONTENCION";
	public static final String AMB_OBSOLESCENCIA = "OBSOLESCENCIA";
	
	public static final String PORTAL_FRONT_EMPR  = "EMPRESARIAL";
	public static final String PORTAL_FRONT_PYME  = "PYMES";
	public static final String PORTAL_MIDDLE_EMPR = "MIDDLE EMPRESARIAL";
	public static final String PORTAL_MIDDLE_PYME = "MIDDLE PYMES";

//=======================================================================================================================
	// DATOS RELACIONADOS AL LOGUEO EN PORTAL PYME / EMPRESARIAL:
	
	// CONSTANTES:
	public static final String TOKEN_ESTATIC = "ESTATICO";
	public static final String TOKEN_FISICO  = "FISICO";
	public static final String TOKEN_OTP     = "OTP";
	public static final String TOKEN_VIRTUAL = "VIRTUAL";

	// VARIABLES
	public static String CLI_EMPRESAR = ""; 
	public static String TIPO_ID_LOGUEO = "";
	public static String NUM_ID_LOGUEO = "";
	public static String CLAVE = "";      // CLAVE PERSONAL O VIRTUAL(PARA OTP)
	public static String TIPO_TOKEN = ""; // ESTATICO / FISICO / OTP / VIRTUAL
	public static String DATO_DINAM = ""; // DATO DEL TOKEN EST�TICO, CELULAR PARA LA OTP O SEMILLA PARA VIRTUAL 
	public static String LAST_TOKEN;
	
	// NOMBRE DEL PAR�METRO EN LA DATA DONDE EST� CADA DATO CLAVE
	private static String PARAM_CLI_EMPR = "";
	private static String PARAM_TIPO_ID  = "";
	private static String PARAM_NUM_ID   = "";

	private static String LAST_CLI_EMPRESAR;
	private static String LAST_TIPO_ID_LOGUEO;
	private static String LAST_NUM_ID_LOGUEO;
	
	public static String AMBIENTE_TEST = ""; // AMBIENTE BAJO PRUEBA : Proyectos / Contenci�n / Obsolescencia
	private static String PORTAL_CARGADO;
//=======================================================================================================================
	
	/**
	 * Carga los datos de logueo de la hoja de datos Global.
	 * Al mismo tiempo, actualiza la informaci�n del �ltimo cliente logueado.
	 * OJO: Se puede confundir con la informaci�n del �ltimo cliente, si se usa este m�todo para cargar los datos que
	 * corresponden a portales diferentes.
	 */
	public static void loadLoginData(String paramNumCli, String paramTipoDoc, String paramNumDoc, String paramClave,
			String tipoTok, String datoTok) throws Exception { 

		PARAM_CLI_EMPR = paramNumCli;
		PARAM_TIPO_ID  = paramTipoDoc;
		PARAM_NUM_ID   = paramNumDoc;
		
		// CAMBIA LOS DATOS DEL ANTERIOR CLIENTE LOGUEADO:
		DatosEmpresarial.LAST_CLI_EMPRESAR   = DatosEmpresarial.CLI_EMPRESAR;
		DatosEmpresarial.LAST_TIPO_ID_LOGUEO = DatosEmpresarial.TIPO_ID_LOGUEO; 
		DatosEmpresarial.LAST_NUM_ID_LOGUEO  = DatosEmpresarial.NUM_ID_LOGUEO;
	    
		// SETEA LA INFORMACI�N DE LOGUEO QUE SE VA A USAR:
		DatosEmpresarial.CLI_EMPRESAR   = SettingsRun.getGlobalData().getParameter(paramNumCli).trim();
		DatosEmpresarial.TIPO_ID_LOGUEO = SettingsRun.getGlobalData().getParameter(paramTipoDoc).trim();
		DatosEmpresarial.NUM_ID_LOGUEO  = SettingsRun.getGlobalData().getParameter(paramNumDoc).trim();
		DatosEmpresarial.CLAVE          = SettingsRun.getGlobalData().getParameter(paramClave).trim();
		DatosEmpresarial.TIPO_TOKEN     = SettingsRun.getGlobalData().getParameter(tipoTok).trim();
		DatosEmpresarial.DATO_DINAM     = SettingsRun.getGlobalData().getParameter(datoTok).trim();
	}

	public static void loadLoginDataFija(String numCli, String tipoDoc, String numDoc, String clave,
			String tipoTok, String datoTok) throws Exception {

		// CAMBIA LOS DATOS DEL ANTERIOR CLIENTE LOGUEADO:
		DatosEmpresarial.LAST_CLI_EMPRESAR   = DatosEmpresarial.CLI_EMPRESAR;
		DatosEmpresarial.LAST_TIPO_ID_LOGUEO = DatosEmpresarial.TIPO_ID_LOGUEO;
		DatosEmpresarial.LAST_NUM_ID_LOGUEO  = DatosEmpresarial.NUM_ID_LOGUEO;
	    
		// SETEA LA INFORMACI�N DE LOGUEO QUE SE VA A USAR:
		DatosEmpresarial.CLI_EMPRESAR   = numCli;
		DatosEmpresarial.TIPO_ID_LOGUEO = tipoDoc;
		DatosEmpresarial.NUM_ID_LOGUEO  = numDoc;
		DatosEmpresarial.CLAVE          = clave;
		DatosEmpresarial.TIPO_TOKEN     = tipoTok;
		DatosEmpresarial.DATO_DINAM     = datoTok;
	}

	/**
	 * Retorna los datos de Logueo existentes en un Array de String de 6 elementos.
	 * {0-ClienteEmpresarial, 1-tipoId, 2-numId, 3-clave, 4-tipoToken, 5-valor/semilla/celular}
	 * @throws Exception 
	 */
	public static String[] getLoginData() throws Exception {
		if (DatosEmpresarial.TIPO_ID_LOGUEO.equals(""))
			throw new Exception("DatosEmpresarial ERROR -- No se han cargado los datos de logueo...");

		String[] datosLogin = { DatosEmpresarial.CLI_EMPRESAR, DatosEmpresarial.TIPO_ID_LOGUEO,
				DatosEmpresarial.NUM_ID_LOGUEO, DatosEmpresarial.CLAVE, DatosEmpresarial.TIPO_TOKEN,
				DatosEmpresarial.DATO_DINAM };
		return datosLogin;
	}
	
	/**
	 * Indica si el cliente actual es igual al anterior.
	 */
	public static boolean currentAndLastCustomerIsEqual() {
		return ( DatosEmpresarial.CLI_EMPRESAR.equals(DatosEmpresarial.LAST_CLI_EMPRESAR) &&
				 DatosEmpresarial.TIPO_ID_LOGUEO.equals(DatosEmpresarial.LAST_TIPO_ID_LOGUEO) &&
				 DatosEmpresarial.NUM_ID_LOGUEO.equals(DatosEmpresarial.LAST_NUM_ID_LOGUEO) );
	}
	
	/**
	 * Indica si el cliente actual es igual al siguiente.
	 */
	public static boolean currentAndNextCustomerIsEqual() throws Exception {
		
		int nextRow = SettingsRun.getNextIteration();
		String nextCliEmpr   = SettingsRun.getGlobalData().getParameterByRow(PARAM_CLI_EMPR, nextRow).trim();
		String nextTipoIdLog = SettingsRun.getGlobalData().getParameterByRow(PARAM_TIPO_ID, nextRow).trim();
		String nextNumIdLog  = SettingsRun.getGlobalData().getParameterByRow(PARAM_NUM_ID, nextRow).trim();

		return ( DatosEmpresarial.CLI_EMPRESAR.equals(nextCliEmpr) &&
				 DatosEmpresarial.TIPO_ID_LOGUEO.equals(nextTipoIdLog) &&
				 DatosEmpresarial.NUM_ID_LOGUEO.equals(nextNumIdLog) );
	}
	
	/**
	 * Retorna el valor del TOKEN correspondiente al usuario que est� logueado en Empresarial.
	 */
	public static String getValorToken() throws Exception {
	    
		String valorToken;
	    boolean esOTP = DatosEmpresarial.TIPO_TOKEN.equals(DatosEmpresarial.TOKEN_OTP);
	    // --------------------------------------------------------------------------------------------------------------
	    // OBTIENE EL VALOR DEL TOKEN
	    if (DatosEmpresarial.TIPO_TOKEN.equals(DatosEmpresarial.TOKEN_ESTATIC)) valorToken = DatosEmpresarial.DATO_DINAM;
	    // --------------------------------------------------------------------------------------------------------------
	    // EL TOKEN ES OTP Y SE CONOCE EL N�MERO DE CELULAR AL QUE LE LLEGA LA OTP:
	    else if (esOTP && !DatosEmpresarial.DATO_DINAM.equals("")) {
	    	valorToken = "0";
	    }
	    // --------------------------------------------------------------------------------------------------------------
	    // EL TOKEN ES FISICO / OTP Y SE DEBE PEDIR SU VALOR AL EJECUTOR DE LA PRUEBA:
	    else if (DatosEmpresarial.TIPO_TOKEN.equals(DatosEmpresarial.TOKEN_FISICO) ||
	    		(esOTP && DatosEmpresarial.DATO_DINAM.equals("")) ) {
	        
	    	String dato = "DIN�MICA";
	    	if (esOTP) dato = "OTP";
	        
	        String titleBox  = "SOLICITUD DE " + dato + "...";
	        String pideToken = "Ingrese el valor de la " + dato + " para portal : " + DatosEmpresarial.PORTAL_CARGADO +
	                    "\nCliente Empresarial [" + DatosEmpresarial.CLI_EMPRESAR   + "]" +
	                    "\nTipo Identificaci�n [" + DatosEmpresarial.TIPO_ID_LOGUEO + "]" +
	                    "\nN�m. Identificaci�n [" + DatosEmpresarial.NUM_ID_LOGUEO  + "]";
	        
	        if (!DatosEmpresarial.LAST_TOKEN.equals(""))
	        	pideToken += "\n�ltima " + dato + " usada  [" + DatosEmpresarial.LAST_TOKEN + "]\n";
			
	        do { //Solicita la din�mica del Token, hasta que se ingrese una con el tama�o permitido que es de 6 d�gitos
    	        do {
    	        	valorToken = JOptionPane.showInputDialog(null, titleBox, pideToken, JOptionPane.INFORMATION_MESSAGE);
    	        } while (valorToken == null); //Es null si cancela
	    	    valorToken = valorToken.trim();
	    	    if (valorToken.length() != 6) titleBox = "LA LONGITUD DEBE SER DE 6 D�GITOS, REVISE...";
			} while (valorToken.length() != 6);
	    }
	    // --------------------------------------------------------------------------------------------------------------
	    // ES TOKEN VIRTUAL SE OBTIENE POR RSA
	    else {
	    	String semillaRequerida = DatosEmpresarial.DATO_DINAM;
	    	valorToken = "PENDIENTE " + semillaRequerida;
	    	/*
	    	String lastToken = DatosEmpresarial.LAST_TOKEN;
		    Set cb = CreateObject("Mercury.Clipboard") 'Objeto Clipboard
		    cb.Clear 'Para limpiar el ClipBoard
		    SystemUtil.Run(RUTA_RSA) 'Abre el RSA
		    Window("regexpwndtitle:=RSA SecurID Token").Activate
		    Call seleccionar_semillaRSA(semillaRequerida) 'Selecciona la semillaRequerida
		    Do
		        timeRest = tiempo_restante_tokenCode()
		        If timeRest <= 5 Then Wait timeRest 'Si s�lo restan 5 segundos o menos, se espera ese tiempo para darle click al copiar
		        Window("regexpwndtitle:=RSA SecurID Token").Activate
		        'Window("regexpwndtitle:=RSA SecurID Token").HighLight
		        Window("regexpwndtitle:=RSA SecurID Token").WinObject("regexpwndtitle:=Copy button").Click 'Da click al bot�n "Copy"
		        valorToken = cb.GetText 'Captura el valor del clipboard
		        If valorToken = lastToken Then Wait timeRest 'Si el valorToken es igual al �ltimo, espera el tiempo restante (para no estar copiando a cada rato)
		    Loop While valorToken = lastToken
		    
		    Set cb = nothing 'Libera memoria del objeto clipboard
		    */
	    }
	    DatosEmpresarial.LAST_TOKEN = valorToken; // PARA INDICAR QUE AL USAR ESTE DATO, AHORA ES EL �LTIMO VALOR USADO
		return valorToken;
	}
	
	/**
	 * Retorna el valor del TOKEN correspondiente al usuario que est� logueado en Empresarial, cumpliendo la [condicion].
	 * Los posibles valores de [condicion] son: "CORRECTO" / "CP ERRADA" / "ERRADO" / "CORTO" / "NO VALOR" / "CEROS" /
	 * "UNOS" / "ESPACIOS".
	 */
	public static String getValorToken(String condicion) throws Exception {
	    
		String numToken = "";
		switch (condicion) {
			case "CORRECTO": case "CP ERRADA": case "":
				numToken = getValorToken(); // EL CORRECTO - SIN CONDICI�N
				break;
	
			case "ERRADO":
	            String auxNumT  = getValorToken(); // EL CORRECTO - SIN CONDICI�N
	            // '123456 = 123457 / 456789 = 456780
	            numToken = DXCUtil.left(auxNumT, auxNumT.length()-1) + DXCUtil.right(String.valueOf(Integer.valueOf(DXCUtil.right(auxNumT,1))+1), 1);
	            break;
	            
			case "CORTO":    numToken = "123";    break;
			case "NO VALOR": numToken = "";       break;
			case "CEROS":    numToken = "000000"; break;
			case "UNOS":     numToken = "111111"; break;
			case "ESPACIOS": numToken = "      "; break;
		}
	    DatosEmpresarial.LAST_TOKEN = numToken;
	    return numToken;
	}

}
