package dxc.execution;

import java.io.File;
import java.util.HashMap;

import dxc.util.DXCUtil;

/**
 * Clase que maneja la información de las ejecuciones a realizar.
 * Usa la clase [DataDriven] para cargar el archivo de datos que se usará como data base.
 * @author DXC-szea
 */

public class SettingsRun {

	public static final String INIC_RES = "Res";
	public static String EXEC_CLASS = "Temp"; // NOMBRE POR DEFECTO DE LA CLASE QUE EJECUTA, SI NO SE LO SETEAN 
	public static String RESULT_DIR = ""; 
	protected static DataDriven globalData = null;
	protected static int[] rowsExec = {1}; // ARREGLO DE ENTEROS CON LOS ROW QUE SER�N LANZADOS
	protected static int totalExec = 1;    // TOTAL DE EJECUCIONES A REALIZAR (=rowsExec.length)
	protected static int currentExec;      // EJECUCI�N ACTUAL, SE USA CUANDO NO SE CUENTA CON [globalData]
	protected static HashMap<Integer,Integer> dicRow_Pos = new HashMap<Integer,Integer>(); // ROW, SU POSICI�N EN [rowsExec]
	
	/**
	 * ESTE CONSTRUCTOR ES PARA LAS CLASES QUE HEREDEN DE ESTA CLASE, PERO YA EST� DEFINIDO EL [globalDataSheet]
	 * @throws Exception 
	 */
	/*
	public SettingsRun() throws Exception {
		//Si no se ha realizado la carga de la configuraci�n 
		if (totalExec == 0)
			throw new Exception ("BaseTestERROR -- Unknown execution range...");
	}
	*/
	
	/**
	 * Carga la configuraci�n inicial para identificar las ejecuciones que se van a lanzar y el DATASHEET
	 * [globalDataSheet] que se usar� como base para extraer la data, el HEADER empieza en la primera l�nea del Excel.
	 */
	public static void loadSetting(int rowIni, int rowFin, String sourceGlobal) throws Exception {
		rowIni = validarRowIniConHeader(1, rowIni); //Valida que la fila inicial sea diferente a la fila del Header
		calcularTotalExecs(rowIni, rowFin);
		globalData = new DataDriven(sourceGlobal);
		globalData.setCurrentRow(rowIni);
		if (rowFin > globalData.getLastRow() ) {
			Reporter.write("DXCWarning -- Se modifica el Row final de ejecuci�n, porque el marcado superaba lo existente en la data...");
			rowFin = globalData.getLastRow();
			totalExec = rowFin - rowIni + 1; //Cambia el total de ejecuciones
		}
		//Inicia el Array de ejecuciones, va desde el Row indicado por rowIni hasta las veces requeridas [totalExec]
		iniciarArrayExec(rowIni);
		Reporter.write("*** ARCHIVO DE DATOS CON HEADER EN ROW [1] - SE EJECUTA DEL ROW [" + rowIni + "] AL [" + rowFin + "]...");
	}

	/**
	 * Carga la configuraci�n inicial para identificar las ejecuciones que se van a lanzar y el DATASHEET
	 * [globalDataSheet] que se usar� como base para extraer la data, el HEADER empieza en una l�nea del Excel diefrente
	 * a la primera.
	 */
	public static void loadSetting(int rowIni, int rowFin, String sourceGlobal, int rowHeader) throws Exception {
		rowIni = validarRowIniConHeader(rowHeader, rowIni); //Valida que la fila inicial sea diferente a la fila del Header
		calcularTotalExecs(rowIni, rowFin);
		globalData = new DataDriven(sourceGlobal, rowHeader);
		globalData.setCurrentRow(rowIni);
		if (rowFin > globalData.getLastRow() ) {
			Reporter.write("DXCWarning -- Se modifica el Row final de ejecuci�n, porque el marcado superaba lo existente en la data...");
			rowFin = globalData.getLastRow();
			totalExec = rowFin - rowIni + 1; //Cambia el total de ejecuciones
		}
		//Inicia el Array de ejecuciones, va desde el Row indicado por rowIni hasta las veces requeridas [totalExec]
		iniciarArrayExec(rowIni);
		Reporter.write("*** ARCHIVO DE DATOS CON HEADER EN ROW [" + rowHeader + "] - SE EJECUTA DEL ROW [" + rowIni + "] AL [" + rowFin + "]...");
	}
	
	/**
	 * Carga la configuraci�n inicial para identificar las ejecuciones que se van a lanzar, las cuales se reciben en
	 * un array que contiene los ROWS a ejecutar. Se recibe el DATASHEET.
	 * [globalDataSheet] que se usar� como base para extraer la data, el HEADER empieza en la primera l�nea del Excel.
	 */
	public static void loadSetting(int[] rowsEx, String sourceGlobal) throws Exception {
		calcularTotalExecs(rowsEx[0], rowsEx[rowsEx.length-1]);
		totalExec = rowsEx.length; //Sobreescribe [totalExec]
		globalData = new DataDriven(sourceGlobal);
		globalData.setCurrentRow(rowsEx[0]);
		if (rowsEx[totalExec-1] > globalData.getLastRow() )
			throw new Exception ("BaseTestERROR -- Rango superior [" + rowsEx[totalExec-1] + "] de la data NO existe...");
		
		// SI NO HAY ERRORES : CARGA EL ARRAY DE EJECUCIONES Y POBLA EL [dicRow_Pos]
		rowsExec = rowsEx;
		for (int i = 0; i < rowsEx.length; i++) {
			dicRow_Pos.put(rowsExec[i], i); // ROW Y SU POSICI�N EN EL [rowsExec]
		}
		Reporter.write("*** ARCHIVO DE DATOS CON HEADER EN ROW [1] - SE EJECUTAN LOS ROWS [" + DXCUtil.arrayToString(rowsEx, ", ") + "]...");
	}

	/**
	 * Carga la configuraci�n inicial para identificar las ejecuciones que se van a lanzar y el DATASHEET
	 * [globalDataSheet] que se usar� como base para extraer la data, el HEADER empieza en una l�nea del Excel diefrente
	 * a la primera.
	 */
	public static void loadSetting(int[] rowsEx, String sourceGlobal, int rowHeader) throws Exception {
		calcularTotalExecs(rowsEx[0], rowsEx[rowsEx.length-1]);
		totalExec = rowsEx.length; //Sobreescribe [totalExec]
		globalData = new DataDriven(sourceGlobal, rowHeader);
		globalData.setCurrentRow(rowsEx[0]);
		if (rowsEx[totalExec-1] > globalData.getLastRow() )
			throw new Exception ("BaseTestERROR -- Rango superior [" + rowsEx[totalExec-1] + "] de la data NO existe...");
		
		// SI NO HAY ERRORES : CARGA EL ARRAY DE EJECUCIONES Y POBLA EL [dicRow_Pos]
		rowsExec = rowsEx;
		for (int i = 0; i < rowsEx.length; i++) {
			dicRow_Pos.put(rowsExec[i], i); // ROW Y SU POSICI�N EN EL [rowsExec]
		}
		Reporter.write("*** ARCHIVO DE DATOS CON HEADER EN ROW [" + rowHeader + "] - SE EJECUTAN LOS ROWS [" + DXCUtil.arrayToString(rowsEx, ", ") + "]...");
	}
	
	/**
	 * Carga la configuraci�n inicial para identificar el n�mero de ejecuciones que se van a lanzar. No hay DATASHEET
	 * [globalDataSheet] que se use como base para extraer data.
	 */
	public static void loadSetting(int numExecs) throws Exception {
		calcularTotalExecs(1, numExecs);
		//Inicia el Array de ejecuciones, va desde el Row indicado por rowIni hasta las veces requeridas [totalExec]
		iniciarArrayExec(1);
		Reporter.write("*** SIN ARCHIVO DE DATOS - SE REALIZAN [" + numExecs + "] EJECUCIONES...");
	}

	//Valida que la fila inicial no sea igual a la fila del Header, si lo es, se asume que es una fila m�s. 
	private static int validarRowIniConHeader(int rowHeader, int rowIni) {
		int rowRetorno = rowIni;
		if (rowHeader == rowIni) {
			Reporter.write("DXCWarning -- Se incrementa en 1 el Row inicial de ejecuci�n, porque NO puede ser el mismo del Header...");
			rowRetorno++;
		}
		return rowRetorno;
	}
	
	//Valida que la fila inicial y la final sean coherentes = inicial no puede ser mayor a la final
	//Y si es v�lido, carga el total de ejecuciones a realizar.
	private static void calcularTotalExecs(int rowIni, int rowFin) throws Exception {
		if (rowIni > rowFin)
			throw new Exception ("BaseTestERROR -- Wrong execution range...");
		totalExec = rowFin - rowIni + 1;
	}
	
	//Carga en el arreglo de Rows a lanzar el n�mero del Row del Excel de datos que se tendr� en cuenta.
	//Va desde [rowIni] e incrementa de 1 en 1 hasta que se sumen [totalExec] veces. 
	private static void iniciarArrayExec(int rowIni) {
		rowsExec = new int[totalExec];
		for (int i = 0; i < totalExec; i++) {
			rowsExec[i] = rowIni + i;
			dicRow_Pos.put(rowsExec[i], i); // ROW Y SU POSICI�N EN EL [rowsExec]
		}
	}
	
	/**
	 * Carga la iteraci�n del [rowExcelIteration] indicado, como si fuera la iteraci�n actual 
	 */
	public static void loadIteration(int rowExcelIteration) {
		currentExec = rowExcelIteration;
		if (globalData != null)
			globalData.setCurrentRow(rowExcelIteration);
	}
	
	/**
	 * Retorna el valor del Excel de la datalobal que se est� ejecutando
	 */
	public static int getCurrentIteration() {
		int valRetorno = currentExec;
		if (globalData != null)
			valRetorno = globalData.getCurrentRow();
		return valRetorno;
	}

	/**
	 * Retorna el valor del Row del Excel de la dataGlobal que se ejecutar�a en el siguiente lanzamiento.
	 */
	public static int getNextIteration() {
		int valRetorno = 0;
		if (!SettingsRun.esIteracionFinal()) {
			if (dicRow_Pos.containsKey(currentExec)) {
				int posRowExecCurrent = dicRow_Pos.get(currentExec);
				valRetorno = rowsExec[posRowExecCurrent + 1];
			}
		}
		return valRetorno;
	}

	/**
	 * Retorna el valor del Row que corresponde a la primera iteraci�n (row) que se configur� para ejecutar.
	 */
	public static int getStartIteration() {
		return rowsExec[0];
	}
	
	/**
	 * Retorna el valor del Row que corresponde a la �ltima iteraci�n (row) que se configur� para ejecutar.
	 */
	public static int getEndIteration() {
		return rowsExec[rowsExec.length-1];
	}
	
	/**
	 * Retorna el DataDriven de la data Global.
	 */
	public static DataDriven getGlobalData() { 
		return globalData;  
	}
	
	/**
	 * Este m�todo termina la ejecuci�n actual de la prueba y contin�a con la siguiente prueba, seg�n la informaci�n
	 * de las ejecuciones a realizar, la cual se encuentra en [rowsExec].
	 * Si la prueba actual es la �ltima, termina la ejecuci�n total.
	 */
	public static void exitTestIteration() throws Exception {
		throw new Exception ("BaseTestDONE -- exitTestIteration"); //Para indicar el �xito del [exitTestIteration]
	}
	
	/**
	 * M�todo que termina la ejecuci�n de la prueba, de forma total.
	 */
	public static void exitTest() {
		SettingsRun.liberarDataGlobal(); // SALVA Y LIBERA [DataGobal]
		System.exit(0);
	}
	
	/**
	 * M�todo que termina la ejecuci�n de la prueba, de forma total, presentando como un error [msgError].
	 * @throws Exception 
	 */
	public static void exitTest(String msgError) throws Exception {
		Reporter.write(msgError, false);
		SettingsRun.exitTest();
	}
	
	/**
	 * M�todo que retorna una matriz de objetos, del tama�o de la data. Retorna las iteraciones a ejecutar.
	 * Util para manejo de pruebas con TestNG.
	 */
	public static Object[][] loadDataProvider() {
		Object[][]data = new Object[totalExec][1];
		for (int row = 0; row < totalExec; row++) {
			data[row][0] = rowsExec[row];
		}
		return data;
	}

	public static void liberarDataGlobal() {
		if (globalData != null) globalData.liberarData();
	}

	/**
	 * Retorna el nombre del directorio en donde se pueden almacenar archivos relacionados a lo que deja la prueba:
	 * Es <b>user.home / DXC_Evidence / nbClase / </b><br>
	 * Note que S� lleva el �ltimo separador.
	 */
	public static String getTestFilesDir() {
		String separator  = System.getProperty("file.separator");
		String pathTestFilesDir = System.getProperty("user.home") + separator + "DXC_Evidence" + separator + EXEC_CLASS + separator;
		return pathTestFilesDir;
	}
	
	/**
	 * Retorna el siguiente directorio de evidencias que se deber�a generar en una ejecuci�n.
	 */
	public static String getResultDir() {
		
		// ALISTA LA CARPETA EN DONDE SE DEBEN ALMACENAR LAS EVIDENCIAS
		String dirEvid = SettingsRun.getTestFilesDir();
		File directory = new File(dirEvid);
		if (!directory.exists()) directory.mkdir();
		
		File dirTemp;
		String numberRes;
		int result = 1;
		
		// RECORRE LA INFORMACI�N DE ARCHIVOS Y CAPERTAS EXISTENTES, PARA DETECTAR CU�L ES EL SIGUIENTE [Res] A GENERAR
		String[] listado = directory.list();
		if (listado != null) {
			for (int i=0; i< listado.length; i++) {
				if (listado[i].startsWith(SettingsRun.INIC_RES)) {
					numberRes = DXCUtil.right(listado[i], listado[i].length()-SettingsRun.INIC_RES.length());
					if (DXCUtil.isInteger(numberRes)) {
						dirTemp = new File(dirEvid+listado[i]);
						if (dirTemp.isDirectory()) {
							if (result <= Integer.valueOf(numberRes) ) result = Integer.valueOf(numberRes) + 1;
						}
					}
				}
			}
		}
		return ( dirEvid + SettingsRun.INIC_RES + result );
	}
	
	/**
	 * Indica si se est� en la primera iteraci�n.
	 */
	public static boolean esIteracionInicial() {
		return (SettingsRun.getCurrentIteration() == SettingsRun.getStartIteration());
	}

	/**
	 * Indica si se est� en la �ltima iteraci�n.
	 */
	public static boolean esIteracionFinal() {
		return (SettingsRun.getCurrentIteration() == SettingsRun.getEndIteration());
	}
	
	/**
	 * Cambia los datos existentes en el par�metro [nameParameter] por [newValue], desde el row actual hasta el �ltimo,
	 * desde que el registro cuente con los valores indicados en [valCondicion] en sus par�metros [paramsCondicion]<br>
	 * [paramsCondicion] y [valCondicion] deben ser Arrays del mismo tama�o, el uno representa los par�metros a buscar
	 * y el otro los valores a buscar.
	 */
	public static void changeData(String[] paramsCondicion, String[] valCondicion, String nameParameter, String newValue) throws Exception {
		if (globalData == null) return;
		
		int row, posRowExecCurrent = dicRow_Pos.get(currentExec); // POSICI�N DESDE DONDE DEBE RECORRER LA DATA
		String valueData;
		boolean rowCumpleCond;
		for (int posRowExec = posRowExecCurrent; posRowExec < rowsExec.length; posRowExec++) {
			row = rowsExec[posRowExec];
			rowCumpleCond = true;
			for (int posArr = 0; posArr < paramsCondicion.length; posArr++) {
				valueData = globalData.getParameterByRow(paramsCondicion[posArr], row);
				rowCumpleCond = rowCumpleCond && valueData.equals(valCondicion[posArr]);
				if (!rowCumpleCond) break; // ROMPE EL CICLO
			}
			// SI EL ROW CUMPLE LAS CONDICIONES, DEEB HACER EL CAMBIO DEL DATO
			if (rowCumpleCond) globalData.setParameterByRow(nameParameter, row, newValue);
		}
	}
	
}
