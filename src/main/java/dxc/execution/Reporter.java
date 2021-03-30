package dxc.execution;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import dxc.util.DXCUtil;
import dxc.util.ExcelFile;

public class Reporter {

	private static final String TPT_RESULT = "./src/main/resources/Template/TPT_ResultadoExec.xlsx";
	private static HashMap<Integer,Integer> resultsExec; //key=numExec/Row, value=fila en excel
	private static int lastRowToWrite; // ÚLTIMA FILA EN LA QUE SE PUEDE ESCRIBIR
	public static final String HEADER_TESTID = "testConfigId"; // NOMBRE DEL HEADER QUE DARÁ EL NOMBRE AL DIRECTORIO DE EVIDENCIAS
	
	public static final int MIC_HEADER = -1;
	public static final int MIC_PASS = 0;
	public static final int MIC_FAIL = 1;
	public static final int MIC_DONE = 2;
	public static final int MIC_WARNING = 3;
	public static final int MIC_NOEXEC  = 4;
	public static final int MIC_INFO    = 5;
	public static final int MIC_NOT_COMPLETED = 6;
	
	// EL NOMBRE DEL EVENT STATUS COINCIDE CON LA POSICI�N AL QUE CORRESPONDE EN EL ARRAY:
	private static final String[] ARR_NB_EVENT_STATUS = {"PASSED", "FAILED", "DONE", "WARNING", "NO EXEC", "INFO", "NOT COMPLETED"};
	
	private static String nbArchRepResults = ""; // NOMBRE DEL ARCHIVO EN DONDE SE EST� GENERANDO EL REPORTE
	
	/**
	 * M�todo para escribir en la consola el [msg]. Tambi�n escribe en el LOG de la ejecuci�n.
	 */
	public static void write(String msg) {
		try {
			Reporter.write(msg, false);
		} catch (Exception e) {
			// HACE NADA, PORQUE NO TIENE POR QU� SALIR EXCEPTION
		}
	}
	
	/**
	 * M�todo para escribir en la consola el [msg]. Tambi�n escribe en el LOG de la ejecuci�n.<br>
	 * Cuando [nextIteration] es true sale de la iteraci�n actual.
	 */
	public static void write(String msg, boolean nextIteration) throws Exception {
		System.out.println(msg);
		writeMessageInLog(msg);
		if (nextIteration) SettingsRun.exitTestIteration();
	}

	/**
	 * M�todo para escribir en la consola el [msg] como un error. Tambi�n escribe en el LOG de la ejecuci�n.<br>
	 * Cuando [nextIteration] es true sale de la iteraci�n actual.
	 */
	public static void writeErr(String msg, boolean nextIteration) throws Exception {
		System.err.println(msg);
		writeMessageInLog("ERROR --- " + msg);
		if (nextIteration) SettingsRun.exitTestIteration();
	}

	/**
	 * Escribe en el LOG de la ejecuci�n el [message]
	 */
	private static void writeMessageInLog(String message) {
		
		final String LOG_FILE = SettingsRun.RESULT_DIR + System.getProperty("file.separator") + "_LOG_EXEC.txt";
		File archivo = new File(LOG_FILE);
		try {
			if (!archivo.exists()) archivo.createNewFile(); // SI NO EXISTE SE CREA
			FileWriter fw = new FileWriter(archivo, true);  // EL [true] ES PARA UBICAR EL ARCHIVO AL FINAL
			BufferedWriter bw = new BufferedWriter(fw);     // DA UN MEJOR PERFORMANCE A LA ESCRITURA
			bw.write(message + "\n");
			bw.close();
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}

	/**
	 * Reporta un evento en reporte de resultados.
	 */
	public static void reportEvent(int eventStatus, String reportMsg) throws Exception {
		
		String inicio = "";
		if (eventStatus != Reporter.MIC_HEADER) inicio = DXCUtil.rightComplete(ARR_NB_EVENT_STATUS[eventStatus]+" ", 16, '-') + " ";
		// HACE EL PRINT EN CONSOLA SIEMPRE Y CUANDO HAYA ALGO PARA ESCRIBIR
		if (!reportMsg.equals("") && !reportMsg.equals("N/A")) {
			if (eventStatus == MIC_FAIL) Reporter.writeErr("*** "+inicio+reportMsg, false);
			else Reporter.write("*** "+inicio+reportMsg, false);
		}
		
		// EVAL�A SI EXISTE EN LA HOJA DE DATOS GLOBAL EL DATO CON ENCABEZADO [Reporter.HEADER_TESTID]
		String testConfig = "NO EXISTE";
		if (SettingsRun.getGlobalData() != null)
			if (SettingsRun.getGlobalData().parameterExist(Reporter.HEADER_TESTID))
				testConfig = SettingsRun.getGlobalData().getParameter(Reporter.HEADER_TESTID);

		//if (eventStatus == Reporter.MIC_DONE || testConfig.equals("NO EXISTE")) return; // SALE DEL M�TODO
		if (eventStatus == Reporter.MIC_DONE) return; // SALE DEL M�TODO, S�LO ESCRIBE EN LA CONSOLA
		
		// ESCRIBE EL REPORTE EN EL ARCHIVO:
		Reporter.writeReportInFile(SettingsRun.getCurrentIteration(), testConfig, eventStatus, reportMsg, inicio);
	}

//=======================================================================================================================
	// M�TODOS PRIVADOS:
	
	// ESCRIBE EN EL ARCHIVO DE EXCEL QUE SE USA PARA EL REPORTE DE RESULTADOS LA INFORMACI�N RESPECTIVA AL EVENTO
	private static void writeReportInFile(int numRow, String testConfig, int eventStatus, String reportMsg, String inicio) throws Exception {
		
		final int COL_ROW = 1; // # ROW / ITERACI�N
		final int COL_TCI = 2; // ID DEL HEADER
		final int COL_RES = 3; // RESULTADO
		final int COL_COM = 4; // COMENTARIO
		
		ExcelFile excelFile = loadReportFile(); // CARGA EL ARCHIVO CON EL REPORTE DE RESULTADOS
		// --------------------------------------------------------------------------------------------------------------
		// DETERMINA EN QU� FILA DEL EXCEL SE VA A ESCRIBIR, REVISANDO QUE SI EL [numRow] YA EXISTE EN [resultsExec] ES
		// PORQUE YA SE HA ESCRITO ANTERIORMENTE, POR ENDE SE DEBE USAR LA MISMA FILA DEL EXCEL:
		int filaExcel = lastRowToWrite;
		if (resultsExec.containsKey(numRow)) filaExcel = resultsExec.get(numRow);
		else {
			resultsExec.put(numRow, filaExcel);
			lastRowToWrite++; // INCREMENTA LA FILA PARA ESCRIBIR
			excelFile.setNumberCellValue(filaExcel, COL_ROW, numRow);
			excelFile.setStringCellValue(filaExcel, COL_TCI, testConfig);
		}
		// --------------------------------------------------------------------------------------------------------------
		// ALMACENA EL RESULTADO: SEG�N LAS SIGUIENTES CONDICIONES
		if (eventStatus != MIC_HEADER && eventStatus != MIC_DONE && eventStatus != MIC_INFO) {
			String lastResult = excelFile.getStringCellValue(filaExcel, COL_RES);
			if ( lastResult.equals("") ||
			    (lastResult.equals(ARR_NB_EVENT_STATUS[MIC_PASS])) ||
			    (lastResult.equals(ARR_NB_EVENT_STATUS[MIC_WARNING]) && ( eventStatus==MIC_FAIL || eventStatus==MIC_NOT_COMPLETED)) ||
			    (lastResult.equals(ARR_NB_EVENT_STATUS[MIC_NOT_COMPLETED]) && eventStatus == MIC_FAIL) ) {
				
				excelFile.setStringCellValue(filaExcel, COL_RES, ARR_NB_EVENT_STATUS[eventStatus]);
			}
		}
		// --------------------------------------------------------------------------------------------------------------
		// ALMACENA EL COMENTARIO, CONCATEN�NDOLO A LO QUE YA EST�:
		String commentSave = excelFile.getStringCellValue(filaExcel, COL_COM);
		if (commentSave.equals("N/A")) commentSave = ""; // PARA QUE BORRE EL N/A
		if (!commentSave.equals("") && !reportMsg.equals("\n")) commentSave += "\n"; // PARA QUE NO DE DOBLE ENTER
		if (commentSave.equals("") && reportMsg.equals("\n")) commentSave = ""; // PARA QUE NO DE EL ENTER
		
		String connector = inicio;
		if (eventStatus == MIC_NOEXEC || reportMsg.equals("") || reportMsg.equals("N/A")) connector = "";
		
		excelFile.setStringCellValue(filaExcel, COL_COM, commentSave+connector+reportMsg);
		
		// SETEA EL FORMATO REQUERIDO A LO QUE SE ESCRIBI�:
		Font font = excelFile.getWorkBook().createFont();
		font.setFontName("Courier New");
		font.setFontHeightInPoints((short)9);
		CellStyle style = excelFile.getWorkBook().createCellStyle();
		style.setFont(font);
		style.setWrapText(true);
		style.setVerticalAlignment(VerticalAlignment.TOP);
		excelFile.setCellStyle(String.valueOf(filaExcel), "1:4", style);

		excelFile.saveAndCloseFile();
	}
	
	// CARGA EL ARCHIVO EXCEL DE REPORTE, RETORNA UN [ExcelFile]
	private static ExcelFile loadReportFile() throws Exception {
		
		if (nbArchRepResults.equals("")) { // NO EXISTE EL ARCHIVO, SE CREA:
			// ARMA EL NOMBRE DEL ARCHIVO DE REPORTE DE RESULTADOS:
			String extension = DXCUtil.dateToString("yyyymmdd") + DXCUtil.hourToString("HHmm") + ".xlsx";
			nbArchRepResults = SettingsRun.RESULT_DIR + System.getProperty("file.separator") + "_RESULTADO_EXEC" + extension; 
			// COPIA EL ARCHIVO CUYA BASE VIENE DEL TEMPLATE [TPT_RESULT]
			DXCUtil.copyFile(TPT_RESULT, nbArchRepResults);
			resultsExec = new HashMap<Integer,Integer>();
			lastRowToWrite = 2; // SE EMPIEZA A ERSCRIBIR EN LA FILA 2
		}
		ExcelFile excelFile = new ExcelFile(nbArchRepResults, ExcelFile.OPEN);
		excelFile.selectSheet("ReporteResultado");
		return excelFile;
	}

}
