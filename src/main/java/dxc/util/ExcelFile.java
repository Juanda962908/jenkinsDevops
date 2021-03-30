package dxc.util;
/**
 * Esta clase requiere las librerías de Apache POI, para el manejo de Excel.
 * Descargar las 2 dependencias de la URL (son dos, por XLS y XLSX) ->
 * [https://mvnrepository.com/artifact/org.apache.poi/]
 * Los índices de filas y columnas en los Excel empiezan en cero (0), por eso cuando se reciben fila y columna en los
 * diferentes métodos, se debe restar 1.
 * @author DXC-szea
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;

//import org.apache.poi.hssf.usermodel.HSSFHyperlink;
// HSSF (.xls) y XSSF (.xlsx) = Fue introducido en POI 3.5
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelFile {
	
	public static final int OPEN   = 0;
	public static final int CREATE = 1;

	protected String filePath;
	protected String extension; // SE SETEA EN MAY�SCULA
	protected File file;
	protected FileInputStream fileInput;
	protected FileOutputStream fileOutput;
	protected Workbook workBook;
	protected Sheet sheet;
	protected boolean isNew;
	protected boolean saved = false;
	
	public ExcelFile(String filePath, int typeOpen) {
		
		this.filePath = filePath;
		this.file = new File(this.filePath);
		this.extension = DXCUtil.fileGetExtension(this.filePath);
		
		try {
			switch (typeOpen) {
			case OPEN:
				this.openFile();
				break;
			case CREATE:
				this.openNewFile();
				break;
			default:
				throw new Exception ("Tipo apertura [" + typeOpen + "] NO contemplado");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Sheet getSheet() {
		return this.sheet;
	}
	
	public Workbook getWorkBook() {
		return this.workBook;
	}
	
	// ABRE UN ARCHIVO DE EXCEL EXISTENTE
	private void openFile() {
		this.isNew = false; //Indica que el excel S� existe
		try {
			this.fileInput = new FileInputStream(this.file);
			// CARGA EN EL LIBRO DE TRABAJO EL EXCEL, SEG�N SI ES XLSX o XLS:
			if (this.extension.equals("XLSX")) this.workBook = new XSSFWorkbook(this.fileInput);
			else if (this.extension.equals("XLS")) this.workBook = new HSSFWorkbook(this.fileInput);
			
		} catch (FileNotFoundException e) {
			System.err.println("DXCError -- ARCHIVO [" + this.filePath + "] NO EXISTE...");
			e.printStackTrace();
			System.exit(0);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	// ABRE UN ARCHIVO DE EXCEL NUEVO, SI YA EXISTE, SE BORRA EL EXISTENTE
	private void openNewFile() {
		this.isNew = true; //Indica que el excel NO existe, es nuevo
		try {
			if (this.file.exists()) {// si el archivo existe se elimina
				this.file.delete();
				System.out.println("DXCWarning -- ARCHIVO [" + this.filePath + "] EXISTE, SE ELIMINA...");
			}
			this.fileOutput = new FileOutputStream(this.file);
			// CREA EL LIBRO DE TRABAJO, SEG�N SI ES XLSX o XLS:
			if (this.extension.equals("XLSX")) this.workBook = new XSSFWorkbook();
			else if (this.extension.equals("XLS")) this.workBook = new HSSFWorkbook();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// CIERRA EL ARCHIVO DE EXCEL QUE EST� CARGADO, SI EL ARCHIVO ERA NUEVO Y NUNCA SE SALV�, SE ELIMINA 
	public void closeFile() {
		try {
			if (this.fileInput != null)  this.fileInput.close();
			if (this.fileOutput != null) this.fileOutput.close();
			this.workBook.close();
			//Si se trata de un Excel nuevo que NO se guard� => es temporal, se borra:
			if (this.isNew && !this.saved) this.file.delete();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// SALVA EL ARCHIVO DE EXCEL QUE EST� CARGADO
	public void saveFile() throws Exception {
		this.saved = true; //Se marca que se salv�
		//Si NO se ha creado [fileOutput] se crea:
		this.fileOutput = new FileOutputStream(this.file);
		this.workBook.write(this.fileOutput);
		this.fileOutput.flush();
	}
	
	// SALVA Y CIERRA EL ARCHIVO DE EXCEL QUE EST� CARGADO 
	public void saveAndCloseFile() throws Exception {
		this.saveFile();
		this.closeFile();
	}
	
	// CREA UN NUEVO SHEET CON EL NOMBRE DADO, NO DEBE EXISTIR UNA HOJA CON ESE NOMBRE
	public void createSheet(String sheetName) {
		this.sheet = this.workBook.createSheet(sheetName);
		this.workBook.setActiveSheet(this.getWorkBook().getSheetIndex(sheetName));
	}

	// SELECCIONA EL SHEET RESPECTIVO
	public void selectSheet(String sheetName) {
		this.sheet = this.workBook.getSheet(sheetName);
		this.workBook.setActiveSheet(this.getWorkBook().getSheetIndex(sheetName));
	}
	
	// SELECCIONA EL SHEET CORRESPONDIENTE AL INDEX INGRESADO
	public void selectSheet(int sheetIndex) {
		this.sheet = this.workBook.getSheetAt(sheetIndex);
		this.workBook.setActiveSheet(sheetIndex);
	}
	
	// RETORNA EL VALOR DE LA CELDA INDICADA EN FORMATO STRING
	public String getStringCellValue(int rowNumber, int colNumber) {
		String valRetorno = "";
		Row row = this.sheet.getRow(rowNumber-1);
		if (row != null) { // Si NO retorna null es porque el Row ya se ha creado y existe
			Cell cell = row.getCell(colNumber-1);
			if (cell != null) {
				if (cell.getCellType().equals(CellType.NUMERIC)) {
					DecimalFormat formato = new DecimalFormat("####.##");
					valRetorno = formato.format(cell.getNumericCellValue());
				} else
					valRetorno = cell.getStringCellValue();
			}
		}
		return valRetorno;
	}
	
	// RETORNA EL VALOR DE LA CELDA INDICADA EN FORMATO NUM�RICO
	public double getNumberCellValue(int rowNumber, int colNumber) {
		double valRetorno = 0;
		Row row = this.sheet.getRow(rowNumber-1);
		if (row != null) { // Si NO retorna null es porque el Row ya se ha creado y existe
			Cell cell = row.getCell(colNumber-1);
			valRetorno = cell.getNumericCellValue();
		}
		return valRetorno;
	}
	
	// RETORNA EL VALOR DE LA CELDA INDICADA EN FORMATO DATE
	public Date getDateCellValue(int rowNumber, int colNumber) {
		Date valRetorno = new Date();
		Row row = this.sheet.getRow(rowNumber-1);
		if (row != null) { // Si NO retorna null es porque el Row ya se ha creado y existe
			Cell cell = row.getCell(colNumber-1);
			valRetorno = cell.getDateCellValue();
		}
		return valRetorno;
	}

	// Retorna la celda [Cell] correspondiente al Row y Column indicados, para que se pueda modificar.
	public Cell getCellToModify(int rowNumber, int colNumber) {
		Row row = this.sheet.getRow(rowNumber-1);
		// Si retorna null es porque el Row NO se ha creado, se debe crear para poder escribir all�
		if (row == null) row = this.sheet.createRow(rowNumber-1);
		
		Cell cell = row.getCell(colNumber-1);
		if (cell == null) cell = row.createCell(colNumber-1);
		return cell;
	}
	
	// HACE UN HIPERV�NCULO EN EL VALOR DE LA CELDA CON FILA [rowNumber] Y COLUMMA [colNumber], EL HIPERV�NCULO
	// DIRECIONA A LA HOJA 'sheetName'. LA CELDA DEBE CONTENER TEXTO Y LA HOJA DEBE EXISTIR
	public void setHyperlinkCell(int rowNumber, int colNumber, String sheetName) {
		
		Font hlinkfont = this.workBook.createFont();
		hlinkfont.setUnderline(Font.U_SINGLE_ACCOUNTING);
		hlinkfont.setColor(IndexedColors.BLUE.index);
        CellStyle hlinkstyle = this.workBook.createCellStyle();
        hlinkstyle.setFont(hlinkfont);
        
		Cell cell = this.getCellToModify(rowNumber, colNumber);
		Hyperlink link = (Hyperlink)this.workBook.getCreationHelper().createHyperlink(HyperlinkType.DOCUMENT);
		link.setAddress("'" + sheetName + "'!A1");
		cell.setHyperlink(link);
		cell.setCellStyle(hlinkstyle);
	}
	
	// ESCRIBE EN EL VALOR DE LA CELDA EL DATO INDICADO EN FORMATO STRING
	public void setStringCellValue(int rowNumber, int colNumber, String dato) {
		Cell cell = this.getCellToModify(rowNumber, colNumber);
		cell.setCellValue(dato); //se a�ade el dato
	}
	
	// ESCRIBE EN EL VALOR DE LA CELDA EL DATO INDICADO EN FORMATO NUM�RICO
	public void setNumberCellValue(int rowNumber, int colNumber, double dato) {
		Cell cell = this.getCellToModify(rowNumber, colNumber);
		cell.setCellValue(dato); //se a�ade el dato
	}
	
	// ESCRIBE EN EL VALOR DE LA CELDA EL DATO INDICADO EN FORMATO DATE
	public void setDateCellValue(int rowNumber, int colNumber, Date dato) {
		Cell cell= this.getCellToModify(rowNumber, colNumber);
		cell.setCellValue(dato); //se a�ade el dato
	}
	
	/* EJEMPLO DE STYLE...
	 * CellStyle style = libro.createCellStyle();
	 * Font font = libro.createFont();
	 * font.setBold(true);
	 * style.setFont(font);
	 */
	
	// SETEA EL [style] EN LA CELDA CORRESPONDIENTE A [rowNumber, colNumber]
	public void setCellStyle(int rowNumber, int colNumber, CellStyle style) {
		Cell cell = this.getCellToModify(rowNumber, colNumber);
		cell.setCellStyle(style);
	}

	/**
	 * 
	 * @param rows Puede venir de diferentes maneras: el n�mero de 1 columna, varias columnas separadas por "," o
	 * columna inicial ":" columna final.
	 * @param columns Puede venir de diferentes maneras: el n�mero de 1 columna, varias columnas separadas por "," o
	 * columna inicial ":" columna final.
	 * @param style
	 */
	public void setCellStyle(String rows, String columns, CellStyle style) {
		// ALISTA ARRAY DE FILAS A CONTEMPLAR: 
		String[] arrFils = {rows}; // SE ASUME QUE S�LO VIENE UNA FILA 
		if (rows.contains(",")) arrFils = rows.split(","); // VIENEN LAS FILAS SEPARADAS POR ","
		else if (rows.contains(":")) { // VIENE UN RANGO DE FILAS
			String[] arrTemp = rows.split(":");
			int ini = Integer.valueOf(arrTemp[0]);
			int fin = Integer.valueOf(arrTemp[1]);
			arrFils = new String[1+fin-ini];
			for (int i = ini; i <= fin; i++) {
				arrFils[i-ini] = String.valueOf(i);
			}
		}
		// ALISTA ARRAY DE COLUMNAS A CONTEMPLAR:
		String[] arrCols = {columns}; // SE ASUME QUE S�LO VIENE UNA COLUMNA 
		if (columns.contains(",")) arrCols = columns.split(","); // VIENEN LAS COLUMNAS SEPARADAS POR ","
		else if (columns.contains(":")) { // VIENE UN RANGO DE COLUMNAS
			String[] arrTemp = columns.split(":");
			int ini = Integer.valueOf(arrTemp[0]);
			int fin = Integer.valueOf(arrTemp[1]);
			arrCols = new String[1+fin-ini];
			for (int i = ini; i <= fin; i++) {
				arrCols[i-ini] = String.valueOf(i);
			}
		}
		// RECORRE LAS FILAS Y LAS COLUMNAS A LAS QUE SE LES APLICAR� EL STYLE
		int row, column;
		for (int i = 0; i < arrFils.length; i++) {
			row = Integer.valueOf(arrFils[i]);
			for (int j = 0; j < arrCols.length; j++) {
				column = Integer.valueOf(arrCols[j]);
				this.setCellStyle(row, column, style);
			}
		}
	}
	
	/**
	 * Pone el tama�o indicado por [width] en las columnas indicadas en [columns].
	 * @param columns Puede venir de diferentes maneras: el n�mero de 1 columna, varias columnas separadas por "," o
	 * columna inicial ":" columna final.
	 * @param width Ancho de la columna
	 */
	public void setColumnsWidth(String columns, int width) {
		String[] arrCols = {columns}; // SE ASUME QUE S�LO VIENE UNA COLUMNA 
		if (columns.contains(",")) arrCols = columns.split(","); // VIENEN LAS COLUMNAS SEPARADAS POR ","
		else if (columns.contains(":")) { // VIENE UN RANGO DE COLUMNAS
			String[] arrTemp = columns.split(":");
			int ini = Integer.valueOf(arrTemp[0]);
			int fin = Integer.valueOf(arrTemp[1]);
			arrCols = new String[1+fin-ini];
			for (int i = ini; i <= fin; i++) {
				arrCols[i-ini] = String.valueOf(i);
			}
		}
		// RECORRE EL ARREGLO DE COLUMNAS PARA CONVERTIR CADA DATO EN ENTERO Y TOMARLO COMO INDEX:
		int columnIndex;
		for (int i = 0; i < arrCols.length; i++) {
			columnIndex = Integer.valueOf(arrCols[i]);
			this.sheet.setColumnWidth(columnIndex-1, width);
		}
	}
	
	// RETORNA LA PRIMERA FILA CON INFORMACI�N EN LA HOJA DE EXCEL (index inicial = 0) se debe sumar 1.
	public int getFirstRow() {
		int valRet = this.sheet.getFirstRowNum();
		return (valRet + 1);
	}
	
	// RETORNA LA �LTIMA FILA CON INFORMACI�N EN LA HOJA DE EXCEL (index inicial = 0) se debe sumar 1.
	public int getLastRow() {
		int valRet = this.sheet.getLastRowNum();
		return (valRet + 1);
	}
	
	// RETORNA LA PRIMERA COLUMNA CON INFORMACI�N, QUE CORRESPONDA AL ROW [rowNumber] EN LA HOJA DE EXCEL
	public int getFirstColumn(int rowNumber) {
		int firstColumn = 0;
		Row row = sheet.getRow(rowNumber-1);
		if (row != null) firstColumn = 1 + row.getFirstCellNum();
		return firstColumn;
	}
	
	// RETORNA LA �LTIMA COLUMNA CON INFORMACI�N, QUE CORRESPONDA AL ROW [rowNumber] EN LA HOJA DE EXCEL
	public int getLastColumn(int rowNumber) {
		int lastColumn = 0;
		Row row = sheet.getRow(rowNumber-1);
		if (row != null) lastColumn = row.getLastCellNum();
		return lastColumn;
	}
	
	// RETORNA EL NOMBRE DEL SHEET QUE EST� ACTUALMENTE SELECCIONADO
	public String getCurrentSheetName() {
		return this.sheet.getSheetName();
	}
	
    /**
     * Copia la hoja que est� seteada actualmente en la instancia de esta clase a otra hoja cuyo nombre se modifica a
     * [sheetDestino] si �ste es <> "". Deja seleccionada la nueva hoja.
     */
    public void copySheetTo(String sheetDestino) {
    	int indexSheet = this.getWorkBook().getActiveSheetIndex();
    	Sheet newSheet = this.getWorkBook().cloneSheet(indexSheet);
    	int indexNewSheet = this.getWorkBook().getSheetIndex(newSheet.getSheetName());
    	// CAMBIA EL NOMBRE DE LA HOJA
    	if (!sheetDestino.equals("")) this.getWorkBook().setSheetName(indexNewSheet, sheetDestino);
		this.selectSheet(indexNewSheet); // DEJA LA NUEVA HOJA ACTIVA
    }

/*
	// RETORNA EN UN HashMap LA INFORMACI�N DE EN QU� COLUMNA SE ENCUENTRA EL ENCABEZADO
	// Key = NOMBRE DEL ENCABEZADO / Value = COLUMNA EN EL EXCEL EN DONDE EST� ESE ENCABEZADO 
	public HashMap<String,Integer> readNameParameters(int rowHeader) throws Exception {
		
		int colIni = this.getFirstColumn(rowHeader);
		int colFinal = this.getLastColumn(rowHeader);
		String nameParam = "";
		HashMap<String,Integer> parameters = new HashMap<String,Integer>();
		for (int col = colIni; col <= colFinal; col++) {
			nameParam = this.getStringCellValue(rowHeader, col).trim();
			if (!nameParam.equals("_")) {
				if (parameters.containsKey(nameParam))
					throw new Exception ("ExcelFileERROR -- parameter [" + nameParam + "] is duplicated...");
				//Si no se ha almacenado el par�metro, se almacena:
				parameters.put(nameParam, col);
			}
		}
		return parameters;
	}
*/
}
