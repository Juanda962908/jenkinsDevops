package dav.transversal;

import java.util.ArrayList;
import java.util.List;

import dxc.util.DXCUtil;
import dxc.util.DataSheet;

public class DS_DaviviendaUrls extends DataSheet {

	private static final String SOURCE = "./src/main/resources/DataSheet/DS_DaviviendaUrls.xlsx";

	public DS_DaviviendaUrls() throws Exception {
		
		super(); // INICIA EN 1 EL [RowHeader] 
		this.nbSheet = "URL"; // OJO NOMBRE DE LA HOJA
		this.iniciarDataSheet(SOURCE); // CARGA EL ARCHIVO FUENTE QUE CONTIENE LA HOJA DE DATOS

		// CARGA LOS NOMBRES DEL HEADER QUE IDENTIFICA DE FORMA ÚNICA CADA REGISTRO
		this.setKeyHeader(new String[]{"ambiente", "portal"});
	}
	
	/**
	 * Método que retorna la URL correspondiente al ambiente y portal recibido:
	 * Se requiere el nombre del ambiente y el del portal, tal como está almacenado en el DataSheet "DS_DaviviendaUrls"
	 */
	public String getUrl(String ambiente, String portal) throws Exception {
		
		String[] valueData = {ambiente, portal};
		int rowDevice = this.getRowKeyData(valueData);
		if (rowDevice == 0)
			throw new Exception ("DS_DaviviendaUrlsERROR -- No se encuentra la URL para [" + DXCUtil.arrayToString(valueData, "-") + "]...");
		
		String url = this.getExcelFile().getStringCellValue(rowDevice, this.getColumnaParameter("url"));
		return url;
	}

	/**
	 * M�todo que retorna la URL correspondiente al portal recibido:
	 * Se requiere el nombre del portal, tal como est� almacenado en el DataSheet "DS_DaviviendaUrls", se asume que ambiente va VAC�O "".
	 */
	public String getUrl(String portal) throws Exception {
		return getUrl("", portal);
	}

	/**
	 * M�todo que retorna los datos de ambiente, url e ip de un portal recibido por par�metro.
	 * En donde: 0-ambiente, 1-url, 2-ip
	 * Se requiere el nombre del portal, tal como est� almacenado en el DataSheet "DS_DaviviendaUrls", se asume que ambiente va VAC�O "".
	 */
	public String[][] getDatosXPortal(String portal) throws Exception {
		
		// Recorre desde la fila 2 hasta el final de filas, hasta encontrar los rows
		String datoDataSh;
		List<Integer> rows = new ArrayList<Integer>();
		int filaFin = this.getExcelFile().getLastRow();
		for (int fila = 2; fila <= filaFin; fila++) {
			datoDataSh = this.getParameterByRow("portal", fila).trim();
			if (datoDataSh.equals(portal)) rows.add(fila);
		}
		
		String[][] datosRet = new String[rows.size()][3];
		int pos = 0;
		for (Integer row : rows) {
			datosRet[pos][0] = this.getParameterByRow("ambiente", row).trim();
			datosRet[pos][1] = this.getParameterByRow("url", row).trim();
			datosRet[pos][2] = this.getParameterByRow("ip", row).trim();
			pos++;
		}
		return datosRet;
	}

}
