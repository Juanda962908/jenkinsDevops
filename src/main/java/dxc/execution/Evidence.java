package dxc.execution;

import java.io.File;

import dxc.util.DXCUtil;

public class Evidence {

	public  static String EVIDIR_PREF = "";    // PREFIJO PARA LA CARPETA DE LAS EVIDENCIAS
	private static boolean doEvidence = false; // NO TOMA EVIDENCIA
	private static boolean doByRow    = false; // SE ALMACENA DIRECTAMENTE
	private static String evidStratus = "";
	private static String evidMiddle  = "";

	/**
	 * Garantiza que NO se haga toma de evidencias.
	 */
	public static void noTomarla() {
		Evidence.doEvidence = false;
	}
	/**
	 * Garantiza que SI se haga toma de evidencias.
	 */
	public static void siTomarla() {
		Evidence.doEvidence = true;
		Evidence.doByRow = false; // TOMA LA EVIDENCIA EN LA CARPETA DE RESULTADOS
	}
	public static void siTomarla(boolean byRow) {
		Evidence.doEvidence = true;
		Evidence.doByRow = byRow;
	}
	
	public static void setEvidStratus(String evidStratus) {
		Evidence.evidStratus = evidStratus;
	}
	public static void setEvidMiddle(String evidMiddle) {
		Evidence.evidMiddle = evidMiddle;
	}

	public static String save(String nbEvidence, BasePageWeb page) throws Exception {
		if (!Evidence.doEvidence) return ""; // SI NO SE TOMAN EVIDENCIAS SALE DEL MÉTODO
		// SE TOMA LA EVIDENCIA:
		String totalNbEvidence = armarNombreEvidencia(nbEvidence);
		page.saveScreenshot(totalNbEvidence);
		return totalNbEvidence;
	}
	
	public static String saveFullPage(String nbEvidence, BasePageWeb page) throws Exception {
		if (!Evidence.doEvidence) return ""; // SI NO SE TOMAN EVIDENCIAS SALE DEL MÉTODO
		// SE TOMA LA EVIDENCIA:
		String totalNbEvidence = armarNombreEvidencia(nbEvidence);
		page.saveFullScreenshot(totalNbEvidence);
		return totalNbEvidence;
	}
	
	public static void saveFile(String nbFilePath) throws Exception {
		if (!Evidence.doEvidence) return; // SI NO SE TOMAN EVIDENCIAS SALE DEL MÉTODO
		// SE MUEVE EL ARCHIVO AL DIRECTORIO DE EVIDENCIAS PONI�NDOLE EL PREFIJO [yyyymmdd-HHmmss]:
		File file = new File(nbFilePath);
		String nbFileEvidence = getStartEvidenceName() + file.getName(); //directorioEvidencia\yyyymmdd-HHmmss nbFile
		DXCUtil.moveFile(nbFilePath, nbFileEvidence);
	}
	
	/**
	 * Retorna el inicio que debe tener toda evidencia incluyendo las carpetas iniciales. Garantizando que las carpetas
	 * padre existen.<br>
	 * La estructura del retorno es [directorioEvidencia\yyyymmdd-HHmmss ]
	 */
	public static String getStartEvidenceName() throws Exception {
		// RUTA COMPLETA DE EVIDENCIAS PARA CADA ITERACI�N
		String evidenceDir = getNbEvidenceDirectory();
		
		// SI NO SE HA GENERADO CARPETA DE EVIDENCIAS PARA LA ITERACI�N ACTUAL, SE CREA:
		if (!DXCUtil.directoryExist(evidenceDir)) {
			File directory = new File(evidenceDir);
			directory.mkdirs();
		}
		// [directorioEvidencia\yyyymmdd-HHmmss ] :
		return ( evidenceDir + System.getProperty("file.separator") + DXCUtil.dateToString("yyyymmdd") +
				"-" + DXCUtil.hourToString("HHmmss") + " " );
	}

//=======================================================================================================================
	// M�TODOS PRIVADOS:
	
	// ARMA EL NOMBRE DE LA EVIDENCIA CON DIRECTORIOS Y EL FORMATO .PNG
	private static String armarNombreEvidencia(String nbEvidence) throws Exception {
		// VALIDA EL NOMBRE DE LA EVIDENCIA: 9 CARACTERES QUE NO PERMITE \ ? | < > * : / "
    	String nbEvidenciaFinal = nbEvidence.replace("\\", "").replaceAll("[?|<>*:/\"]", ""); 
    	// [directorioEvidencia\yyyymmdd-HHmmss nbEvidencia.png]
		return ( getStartEvidenceName() + nbEvidenciaFinal + ".png" );
	}
	
	// RETORNA EL NOMBRE DEL DIRECTORIO DE EVIDENCIAS, EVALUANDO SI SE HACE POR ROW Y SI ES DatosEmpresarial.ES_EXEC_APP
	// O SI ES EVIENCIA MIDDLE O DE STRATUS
	private static String getNbEvidenceDirectory() throws Exception {
		String rutaEvidencia = SettingsRun.RESULT_DIR;
		
		if (Evidence.doByRow) { // SI SE DEBE HACER POR ROW, SE ALMACENA EN OTRA CARPETA
			String testConfig = "";
			if (SettingsRun.getGlobalData() != null)
				if (SettingsRun.getGlobalData().parameterExist(Reporter.HEADER_TESTID))
					testConfig = " (" + SettingsRun.getGlobalData().getParameter(Reporter.HEADER_TESTID) + ")";
			
			/* ZEA: CAMBIAR EL PREFIJO POR ITERACI�N EN APP EMPRESAS:
			String pref = ""; // SIEMPRE IR� VAC�O EL PREFIJO
			//SI ES EJECUCI�N DE APP SE PONE COMO PREFIJO EL [EMP] O [PYM] DEP�NDIENDO DEL PORTAL AL QUE CORRESPONDE EL TOKEN
			if (DatosEmpresarial.ES_EXEC_APP) {
				pref = "EMP "; // ASUME QUE ES EMPRESARIAL
				if (SettingsRun.getGlobalData().getParameter("portalToken").contains("PYME")) pref = "PYM ";
			}
			*/
			
			String name = "Exec-";
			if (SettingsRun.getGlobalData() != null) name = "Row-";
			// NOMBRE DE LA CARPETA DE EVIDENCIAS PARA CADA ITERACI�N:
			String nbCarpeta = EVIDIR_PREF + name + SettingsRun.getCurrentIteration() + testConfig;
			// SI LA EVIDENCIA ES PARA MIDDLE, LA CARPETA SE CAMBIA (ES FIJA):
			if (!Evidence.evidMiddle.equals(""))  nbCarpeta = "Row-0 (" + Evidence.evidMiddle + ")";
			// SI LA RUTA ES PARA STRATUS CAMBIA LA RUTA:
			if (!Evidence.evidStratus.equals("")) nbCarpeta = "EVIDENC_STRAT";
			
			// RETORNA LA RUTA COMPLETA PARA LAS EVIDENCIAS DE LA ITERACI�N ACTUAL
			rutaEvidencia = ( SettingsRun.RESULT_DIR + System.getProperty("file.separator") + nbCarpeta );
		}
		return rutaEvidencia;
	}
	
}
