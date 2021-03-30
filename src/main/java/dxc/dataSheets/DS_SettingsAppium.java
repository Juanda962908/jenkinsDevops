package dxc.dataSheets;

import dxc.util.DataSheet;

public class DS_SettingsAppium extends DataSheet {

	public static final String DEVICE = "Devices";
	public static final String APPLICATION = "Applications";
	private static final String SOURCE = "./src/main/resources/DataSheet/DS_SettingsAppium.xlsx";
	//Nombre de los parámetros que identifican de forma única el Device

	public DS_SettingsAppium(String nbSetting) throws Exception {
		
		super(); // INICIA EN 1 EL [RowHeader] 
		if (!nbSetting.equals(DEVICE) && !nbSetting.equals(APPLICATION))
			throw new Exception ("DS_SettingsAppiumERROR -- Data Sheet [" + nbSetting + "] doesn't exist...");
		
		this.nbSheet = nbSetting;
		this.iniciarDataSheet(SOURCE); //Carga el archivo fuente que contiene la hoja de datos

		// Carga los nombres del header que identifica de forma única cada registro
		if (nbSetting.equals(DEVICE))
			this.setKeyHeader(new String[]{"name"});
		else if (nbSetting.equals(APPLICATION))
			this.setKeyHeader(new String[]{"nameApplication"});
		
	}
	
	/**
	 * M�todo que retorna un String[] que contiene la informaci�n de configuraci�n del Device en el siguiente orden:
	 * {0-deviceName, 1-udid, 2-platformName, 3-platformVersion}
	 * Se requiere el nombre del device, tal como est� almacenado en el DataSheet "DS_SettingsAppium"
	 */
	public String[] getSettingsDevice(String name) throws Exception {
		
		String[] valueData = {name};
		int rowDevice = this.getRowKeyData(valueData);
		if (rowDevice == 0)
			throw new Exception ("DS_SettingsAppiumERROR -- No se encuentra el Device con nombre [" + name + "]...");
		
		String[] datosDevice = new String[4];
		datosDevice[0] = this.getExcelFile().getStringCellValue(rowDevice, this.getColumnaParameter("deviceName"));
		datosDevice[1] = this.getExcelFile().getStringCellValue(rowDevice, this.getColumnaParameter("udid"));
		datosDevice[2] = this.getExcelFile().getStringCellValue(rowDevice, this.getColumnaParameter("platformName"));
		datosDevice[3] = this.getExcelFile().getStringCellValue(rowDevice, this.getColumnaParameter("platformVersion"));
		
		return datosDevice;
	}
	
	/**
	 * M�todo que retorna un String[] que contiene la informaci�n de configuraci�n de la aplicaci�n que se cargar� en el
	 * Device, en el siguiente orden: {0-appPackage, 1-appActivity}
	 * Se requiere el nombre de la aplicaci�n, tal como est� almacenado en el DataSheet "DS_SettingsAppium"
	 */
	public String[] getSettingsApp(String appName) throws Exception {
		
		String[] valueData = {appName};
		int rowDevice = this.getRowKeyData(valueData);
		if (rowDevice == 0)
			throw new Exception ("DS_SettingsAppiumERROR -- No se encuentra la aplicaci�n [" + appName + "]...");
		
		String[] datosApp = new String[2];
		datosApp[0] = this.getExcelFile().getStringCellValue(rowDevice, this.getColumnaParameter("appPackage"));
		datosApp[1] = this.getExcelFile().getStringCellValue(rowDevice, this.getColumnaParameter("appActivity"));
		
		return datosApp;
	}

	/**
	 * M�todo que retorna un String[] los Devices existentes en el DataSheet
	 */
	public String[] getDevices() throws Exception {
		
		int filaFin  = this.getLastRow();
		int colDevic = this.getColumnaParameter("name");
		String[] devices = new String[filaFin-1];
		for (int fila = 2; fila <= filaFin; fila++) {
			devices[fila-2] = this.getExcelFile().getStringCellValue(fila, colDevic);
		}
		return devices;
	}
	
}
