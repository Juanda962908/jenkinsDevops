package dxc.execution;

import dxc.util.DataSheet;

/**
 * Clase que maneja la información para el manejo de datos principal en un libro de Excel.
 * Para el manejo de la data siempre se contará con la hoja principal que corresponderá a un sheet en un Excel, esta
 * hoja debe tener el nombre "Data" de forma obligatoria.
 * @author DXC-szea
 */
public class DataDriven extends DataSheet {


	private static final String MAIN_SHEET = "Data"; // NOMBRE DE LA HOJA DEL DATASHEET QUE TIENE LOS DATOS
	private int currentRow; // ROW QUE SE EST� EJECUTANDO ACTUALMENTE
	
	/**
	 * Constructor que crea el DataSheet indicando la ruta del archivo fuente. Asume que el RowHeader es la fila [1].
	 */
	public DataDriven(String source) throws Exception {
		super(source, MAIN_SHEET);
	}

	/**
	 * Constructor que crea el DataSheet indicando la ruta del archivo fuente y el rowHeader.
	 */
	public DataDriven(String source, int rowHeader) throws Exception {
		super(source, MAIN_SHEET, rowHeader);
	}

	/**
	 * Retorna la fila del DataSheet que se est� teniendo como la fila actual. 
	 */
	public int getCurrentRow() {
		return this.currentRow;
	}

	public void setCurrentRow(int currentRow) {
		this.currentRow = currentRow;
	} 
	/**
	 * Retorna el par�metro del DataSheet que se encuentra en el Row actual. 
	 */
	public String getParameter(String nameParameter) throws Exception { 
		return this.getParameterByRow(nameParameter, this.currentRow); 
	}
	
	/**
	 * Almacena en el par�metro del DataSheet que se encuentra en el Row actual, el valor indicado. 
	 */
	public void setParameter(String nameParameter, String value) throws Exception {
		this.setParameterByRow(nameParameter, this.currentRow, value);
	}
	
}
