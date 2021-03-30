package dav.transversal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import dxc.dataSheets.DS_SettingsAppium;
import dxc.util.DXCUtil;

public class DavUtil {

  	/**
  	 * Solicita el ambiente de la aplicación Empresarial que se probará. 
  	 */
  	public static String seleccionAmbiente() {
          String[] ambientes = {DatosEmpresarial.AMB_PROYECTOS, DatosEmpresarial.AMB_CONTENCION, DatosEmpresarial.AMB_OBSOLESCENCIA};
          int posAmbiente = 0;
          do {
          	posAmbiente = JOptionPane.showOptionDialog(null, "Ambiente en donde se ejecuta", "AMBIENTE", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, ambientes, ambientes[0]);
          } while(posAmbiente < 0); //Es -1 si cierra el Dialog
          return ambientes[posAmbiente];
      } 
       
    /**
     * Solicita el dispositivo a usar para abrir las aplicaciones móviles.
     */
  	public static String seleccionDispositivo() throws Exception {
          String[] devices = new DS_SettingsAppium(DS_SettingsAppium.DEVICE).getDevices();
          String device;
          do {
          	device = (String) JOptionPane.showInputDialog(null, "Dispositivo a usar?", "DISPOSITIVO", JOptionPane.QUESTION_MESSAGE, null, devices, devices[0]);
  		} while (device == null); //Es null si cancela
          return device;
      }
      
  	/**
  	 * Solicita el ambiente de la App, este dato se concatenar� al package de la aplicaci�n para AppPersonas.
  	 */
  	public static String ingresarAmbienteApp() {
        String numAmbiente;
        do {
          	numAmbiente = JOptionPane.showInputDialog(null, "Indicar el ambiente", "AMBIENTE", JOptionPane.INFORMATION_MESSAGE);
        } while (numAmbiente == null); //Es null si cancela
        return numAmbiente;
  	}
  	
  	/**
  	 * Indica si el tiempo de actividad en determinado canal ha expirado. Bas�ndose en la diferencia existente entre la
  	 * hora actual y la hora en que se realiz� el logueo.
  	 * @param canal - Canal por el que se est� preguntando, posibles valores: "AppEmpresas" / "AppPersonas" / "Pyme"
  	 *                / "Davicom"
  	 */
  	public static boolean tiempoActividadExpirado(String canal) {

		Date tiempoLog = null;       // ALMACENAR� LA HORA EN QUE SE REALIZ� EL LOGUEO
		int minsActividad = 0;       // ALMACENAR� LOS MINUTOS DE ACTIVIDAD
		Date tiempoAct = new Date(); // HORA ACTUAL
		switch (canal) {
			case "AppEmpresas":
				tiempoLog = DatosEmpresarial.FECHAHORALOG_APP;
				minsActividad = DatosEmpresarial.TIME_ACT_APP;
				break;
				
			case "Pyme":
				tiempoLog = DatosEmpresarial.FECHAHORALOG_WEB;
				minsActividad = DatosEmpresarial.TIME_ACT_PYMES;
				break;
		}
		// CALCULA LOS MINUTOS QUE HAN TRANSCURRIDO
		long difMinutes = TimeUnit.MILLISECONDS.toMinutes(tiempoAct.getTime() - tiempoLog.getTime());
		return (difMinutes >= minsActividad);
	}
  	
  //-------------------------------------------------------
  	/**
  	 * M�todo que configurar el archivo "hosts" para garantizar que se abra en Davicom el ambiente requerido.
  	 * @param datosHostAUsar - Array que contiene datos del ambiente 0-ip, 1-url, 2-nombre ambiente
  	 * @param datosHostComm - Array que contiene en una matriz los datos de los ambiente que NO se usar�n
  	 */
  	public static void configurarHostDavicom(String[] datosHostAUsar, String[][] datosHostComm) {
		String archHostOri  = "C:\\Windows\\System32\\drivers\\etc\\hosts";
		String dirTemp = DXCUtil.DIR_DXC_TEMP;
		if (!DXCUtil.directoryExist(dirTemp)) new File(dirTemp).mkdir();
		
		String archHostCopy = dirTemp + "hosts";
		String supuestaIp;
		int longNoUsar = datosHostComm.length;
		try {			
			FileReader fr = new FileReader(archHostOri);
			BufferedReader br = new BufferedReader(fr);
			
			FileWriter fw = new FileWriter(archHostCopy);
			PrintWriter pw = new PrintWriter(fw);

			String linea = br.readLine();
			boolean estaComm, hayCambio = false, hostAmbOK = false;
			while (linea!= null) {
				if (linea.length() >= 16) {
					estaComm = ( linea.substring(0, 1).equals("#") ); // la variable tomar� un true o false si est� comentada la linea
					supuestaIp = linea.substring(0, 16); //retorna una cadena con los primeros  16 caracteres '#000.000.000.000'
					if (supuestaIp.contains(datosHostAUsar[0])) { 
						if (!hostAmbOK) { // NO HA ENCONTRADO EL AMBIENTE
							if (estaComm) {
								linea = datosHostAUsar[0] + "\t\t" + datosHostAUsar[1] + "\t\t#" + datosHostAUsar[2];
								hayCambio = true;
								
							}
							hostAmbOK = true;
						}
						// SI YA SE HAB�A ENCONTRADO LA IP DEL AMBIENTE Y APARECE OTRA VEZ SIN COMENTARIO, SE DEBE COMENTAREAR
						else if (!estaComm) {
							linea = "#" + linea;
							hayCambio = true;
						}
					}
					else if (!estaComm) {
						for (int i = 0; i < longNoUsar; i++) {
							if (supuestaIp.contains(datosHostComm[i][0])) {
								linea = "#" + linea;
								hayCambio = true;
							}
						}
					}
				}
				pw.println(linea);
				linea = br.readLine();
			}
			// SI NO ENCONTR� LA IP DEL AMBIENTE A USAR, SE DEBE INCLUIR
			if (!hostAmbOK) {
				pw.println(datosHostAUsar[0] + "\t\t" + datosHostAUsar[1] + "\t\t#" + datosHostAUsar[2]);
				hayCambio = true;
			}
			
			fw.close();
			fr.close();
			
			// SI HUBO CAMBIO DEL ARCHIVO, SE DEBE COPIAR EN EL HOST DE DRIVERS DEL SISTEMA
			if (hayCambio) DXCUtil.copyFile(archHostCopy, archHostOri);
			
		} catch(IOException e) {			
			e.printStackTrace();			
		}
	}
  	
  	
  	
  	
  	
  	
  	
  	

}
