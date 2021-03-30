package dxc.util;

import java.util.HashMap;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;

import dxc.util.ExcelFile;

/**
 * Clase que maneja la información para el manejo de datos en un libro de Excel.
 * Se debe contar con el recurso (path y nombre de archivo) y la hoja en donde se encuentran los datos.
 * @author DXC-szea
 */
public class DataSheet {

	private String source;
	protected String nbSheet; //Nombre de la hoja con los datos
	private ExcelFile excelFile;
	private int rowHeader;  //Row del Excel en donde se encuentra el nombre de los parámetros
	private int colFinal;   //Columna del Excel donde se encuentra el �ltimo par�metro
	private int rowFinal;   //Row final del Excel donde hay informaci�n
	private HashMap<String,Integer> parameters; //key=nombre par�metro, value=columna en excel
	private String[] keyHeader;
	 
	public DataSheet() throws Exception { 
		this.rowHeader = 1;
		// TODO para sobreescribir NO usar directamente
	}
	
	public DataSheet(String source) throws Exception {
		this.source = source;
		this.rowHeader = 1;
		// TODO para sobreescribir NO usar directamente
	}
	
	public DataSheet(String source, int rowHeader) throws Exception {
		this.source = source;
		this.rowHeader = rowHeader;
		// TODO para sobreescribir NO usar directamente
	}
	
	public DataSheet(String source, String nbSheet) throws Exception {
		this.rowHeader = 1;
		this.nbSheet   = nbSheet;
		this.iniciarDataSheet(source);
	}

	public DataSheet(String source, String nbSheet, int rowHeader) throws Exception {
		this.rowHeader = rowHeader;
		this.nbSheet   = nbSheet;
		this.iniciarDataSheet(source);
	}

	/**
	 * Se encarga de iniciar la hoja de datos: abre el archivo fuente, selecciona la hoja del Excel donde est� la data,
	 * carga los par�metros (header) con los que cuenta, carga el rowFinal y la columnaFinal.
	 * Es requerido que ya est� cargado:
	 * - El nombre de la hoja en donde est�n los datos.
	 * - Row Header  
	 */
	protected void iniciarDataSheet(String source) throws Exception {
		this.source = source;
		this.excelFile = new ExcelFile(this.source, ExcelFile.OPEN);
		this.excelFile.selectSheet(this.nbSheet);
		
		if (this.excelFile.getSheet() == null)
			throw new Exception ("DataSheetERROR -- En el Excel se espera una hoja con nombre [" + this.nbSheet + "] y NO se encuentra...");

		this.loadNameParameters();
		this.rowFinal = this.excelFile.getLastRow();
		this.colFinal = this.excelFile.getLastColumn(this.rowHeader);
	}
	
	private void loadNameParameters() throws Exception {
		//Debe cargar los par�metros existentes en el HashMap [parameters]:
		//Key=nombre del par�metro, value=columna donde est�
		int colIni = this.excelFile.getFirstColumn(this.rowHeader);
		this.colFinal = this.excelFile.getLastColumn(this.rowHeader);
		String nameParam = "";
		parameters = new HashMap<String,Integer>();
		for (int col = colIni; col <= this.colFinal; col++) {
			nameParam = this.excelFile.getStringCellValue(this.rowHeader, col).trim();
			if (!nameParam.equals("_")) {
				if (parameters.containsKey(nameParam))
					throw new Exception ("DataDrivenERROR -- Data Sheet invalid, parameter [" + nameParam + "] is duplicated...");
				//Si no se ha almacenado el par�metro, se almacena:
				parameters.put(nameParam, col);
			}
		}
		
	}
	
	public int getLastRow() {
		return this.rowFinal;
	}

	/**
	 * Indica cu�l es la �ltima fila existente en el DataSheet, bas�ndose en los par�metros requeridos (uno o varios).
	 * Esto debido a que el Excel puede indicar una fila final que no es real, porque en alg�n momento se ingresaron
	 * datos en el archivo y luego los borraron, si hay par�metros que indican la existencia del registro, esos son los
	 * que deben ser recibidos en [nbParameters] (no necesariamente TODOS los par�metros).
	 */
	public int getLastRow(int filaInicial, String... nbParameters) throws Exception {
    	int filaFinal = this.getLastRow();
    	boolean hayDatos;
    	do {
    		hayDatos = true; // SE ASUME QUE S� HAY DATOS EN LA FILA
        	for (String nbParam : nbParameters) {
        		hayDatos = hayDatos && !this.getParameterByRow(nbParam, filaFinal).trim().isEmpty();
        		if (!hayDatos) break; // PARA QUE TERMINE EL CICLO
			}
        	if (!hayDatos) filaFinal--; // PARA EVALUAR LA ANTERIOR FILA
		} while (!hayDatos && filaFinal >= filaInicial);
    	return filaFinal;
    }

	public int getLastColumn() {
		return this.colFinal;
	}
	
	public ExcelFile getExcelFile() {
		return this.excelFile;
	}
	
	protected void setKeyHeader(String[] keyData) {
		this.keyHeader = keyData;
	}

	/**
	 * Retorna el par�metro del DataSheet que se encuentra en el Row que se recibe por par�metro.
	 */
	public String getParameterByRow(String nameParameter, int rowNumber) throws Exception {
		int colExcel = this.getColumnaParameter(nameParameter); // SI NO EXISTE GENERA EXCEPCI�N	
		return this.excelFile.getStringCellValue(rowNumber, colExcel);
	}
	
	/**
	 * Almacena en el par�metro del DataSheet que se encuentra en el Row que se recibe por par�metro, el valor indicado. 
	 */
	public void setParameterByRow(String nameParameter, int rowNumber, String value) throws Exception {
		int colNumber = this.getColumnaParameter(nameParameter); // SI NO EXISTE GENERA EXCEPCI�N
		this.excelFile.setStringCellValue(rowNumber, colNumber, value);
		this.excelFile.saveFile(); // PARA QUE VAYA SALVANDO LA HOJA DE DATOS
	}
	
	/**
	 * Almacena en el par�metro del DataSheet que se encuentra en el Row que se recibe por par�metro, el valor indicado.<br>
	 * Adicional le da el [color] indicado a la celda.
	 * @param nameParameter - Nombre del par�metro en donde se ubicar� la celda (para ubicar la columna).
	 * @param rowNumber - Fila de la hoja de datos.
	 * @param color - Index del color a darle a la celda (p.e: IndexedColors.YELLOW.index)
	 * @param value - Valor a almacenar.
	 */
	public void setColorParameterByRow(String nameParameter, int rowNumber, short color, String value) throws Exception {
        CellStyle cellheader = this.excelFile.getWorkBook().createCellStyle();
        cellheader.setFillForegroundColor(color);
        cellheader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellheader.setWrapText(false);

		int colNumber = this.getColumnaParameter(nameParameter); // SI NO EXISTE GENERA EXCEPCI�N
		this.excelFile.setStringCellValue(rowNumber, colNumber, value);
        this.excelFile.setCellStyle(rowNumber, colNumber, cellheader);
        this.excelFile.saveFile(); // PARA QUE VAYA SALVANDO LA HOJA DE DATOS
	}
	
	/**
	 * Le da el [color] indicado a la celda del par�metro del DataSheet que se encuentra en el Row que se recibe por
	 * par�metro<br>
	 * @param nameParameter - Nombre del par�metro en donde se ubicar� la celda (para ubicar la columna).
	 * @param rowNumber - Fila de la hoja de datos.
	 * @param color - Index del color a darle a la celda (p.e: IndexedColors.YELLOW.index)
	 */
	public void setColorParameterByRow(String nameParameter, int rowNumber, short color) throws Exception {
        CellStyle cellheader = this.excelFile.getWorkBook().createCellStyle();
        cellheader.setFillForegroundColor(color);
        cellheader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellheader.setWrapText(false);

		int colNumber = this.getColumnaParameter(nameParameter); // SI NO EXISTE GENERA EXCEPCI�N
        this.excelFile.setCellStyle(rowNumber, colNumber, cellheader);
        this.excelFile.saveFile(); // PARA QUE VAYA SALVANDO LA HOJA DE DATOS
	}
	
	/**
	 * Adicionar los par�metros indicados a la hoja de datos actual. 
	 */
	public void addParameters(String... nameParameters) throws Exception {
		
		for (String nameParam : nameParameters) {
			//Si en el HashMap donde est�n los par�metros ya contiene el par�metro, se genera excepci�n, sino se adiciona:
			if (parameters.containsKey(nameParam))
				throw new Exception ("DataDrivenERROR -- method 'addParameter' : parameter [" + nameParam + "] already exists...");
			
			this.colFinal++; //Incrementa la columna final
			this.excelFile.setStringCellValue(this.rowHeader, this.colFinal, nameParam); //Lo almacena a la hoja de Excel
			parameters.put(nameParam, this.colFinal); //Lo adiciona al HashMap de par�metros
		}
		this.excelFile.saveFile(); // PARA QUE VAYA SALVANDO LA HOJA DE DATOS
	}
	
	/**
	 * Salva la informaci�n del actual DataDriven. Y libera el archivo de Excel. 
	 */
	public void liberarData() {
		this.excelFile.closeFile();//ZEA saveAndCloseFile();
	}
	
	/**
	 * Retorna el n�mero de la columna en el Excel, en donde se encuentra el par�metro [nbParameter]
	 */
	public int getColumnaParameter(String nbParameter) throws Exception {
		if (!this.parameterExist(nbParameter))
			throw new Exception ("DataSheetERROR -- Parameter [" + nbParameter + "] inexisting");
		return (int)this.parameters.get(nbParameter);
	}

	/**
	 * Indica si el par�metro [nbParameter] se encuentra en el DataSheet.
	 */
	public boolean parameterExist(String nbParameter) {
		return this.parameters.containsKey(nbParameter);
	}
	
	/**
	 * Indica si TODOS los par�metros [nbParameters] se encuentran en el DataSheet.
	 */
	public boolean parametersExist(String... nbParameters) {
		boolean todoExiste = true;
		for (String nbParameter : nbParameters) {
			todoExiste = todoExiste & this.parameterExist(nbParameter);
			if (!todoExiste) break; // TERMINA EN CICLO
		}
		return todoExiste;
	}

	/**
	 * Retorna el n�mero de la fila del Excel del DataSheet, en el que se encuentra el registro cuyos datos clave
	 * coincidan de forma exacta con los recibidos en [valueData] esta informaci�n debe venir completa y en el mismo orden
	 * en que se expres� los encabezados [keyHeader].
	 * Si no se encuentra el retorno es [0].
	 */
	public int getRowKeyData(String[] valueData) throws Exception {

		int totalKeyHeader = this.keyHeader.length;
		if (totalKeyHeader == 0)
			throw new Exception ("DataSheetERROR -- No se tienen identificado los KeyHeader...");
		if (valueData.length != totalKeyHeader)
			throw new Exception ("DataSheetERROR -- Se esperaban [" + totalKeyHeader + "] KeyHeader...");
		
		// Arma array con las columnas donde est�n los encabezados clave:
		int[] colsKeyHeader = new int[totalKeyHeader];
		String nbHeader;
		for (int pos = 0; pos < totalKeyHeader; pos++) {
			nbHeader = this.keyHeader[pos];
			colsKeyHeader[pos] = this.getColumnaParameter(nbHeader);
		}
		
		// Recorre desde la fila 2 hasta el final de filas, hasta encontrar los datos
		int filaFin = this.excelFile.getLastRow();
		int rowRetorno = 0;
		String datoBuscar, datoDataSh;
		boolean encontroDato;
		for (int fila = 2; fila <= filaFin; fila++) {
			encontroDato = true; //Asume que s� se encuentra el dato
			for (int pos = 0; pos < totalKeyHeader; pos++) {
				datoBuscar = valueData[pos];
				datoDataSh = this.excelFile.getStringCellValue(fila, colsKeyHeader[pos]).trim();
				encontroDato = encontroDato && datoDataSh.equals(datoBuscar);
				if (!encontroDato) break; //Termina el ciclo interno, porque si el dato no coincide no hay que seguir
			}
			if (encontroDato) {
				rowRetorno = fila;
				break; //Termina el ciclo porque se encontr� la coincidencia
			}
		}
		return rowRetorno;
	}

	/**
	 * Valida que los [nbParameters] existan en la hoja de datos, si NO existen todos, genera una excepci�n indicando
	 * cu�les faltan.
	 */
	public void validarParameters(String... nbParameters) throws Exception {
		String listaParams = "", sep = "";
		for (String nbParameter : nbParameters) {
			if (!this.parameterExist(nbParameter)) {
				if (!listaParams.equals("")) sep = ", ";
				listaParams += sep + nbParameter;
			}
		}
		if (!listaParams.equals(""))
			throw new Exception ("DataSheetERROR -- La hoja de datos no contiene los par�metros esperados.\n\tFaltan >>> " + listaParams);
	}

}
