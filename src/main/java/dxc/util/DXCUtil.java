package dxc.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Collator;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class DXCUtil {

//=======================================================================================================================
/*
 PARA CONVERTIR A UN String[] los Object[] porque si se hace un Cast sale excepción, se hace: 
 Arrays.copyOf(arrObj, arrObj.length, String[].class)
 */
//=======================================================================================================================
	// TODO >> MANEJO DE UTILITARIOS EN GENERAL:
	public static String PATH_RESOURCES = ""; // EN EJECUCIÓN JAR PONER ESTE VALOR "/resources";
	
	/**
	 * Retorna el ícono de DXC en una Image.
	 */
	public static Image getDxcIconImage() {
		//return new ImageIcon(DXCUtil.getResource("Icons/IMGdxc.png")).getImage();
		return Toolkit.getDefaultToolkit().getImage(DXCUtil.getResource("Icons/IMGdxc.png"));
	}
	/**
	 * Retorna java.net.URL correspondiente al recurso [pathResource], el cual se encuentra en los resources.<br>
	 * Se recibe por par�metro el archivo con ruta y extensi�n. p.e: Icons/image.png
	 */
	public static URL getResource(String pathResource) {
		String resource = PATH_RESOURCES + "/" + pathResource;
		return DXCUtil.class.getResource(resource);
	}
	
	/**
	 * Retorna InputStream correspondiente al recurso [pathResource], el cual se encuentra en los resources.<br>
	 * Se recibe por par�metro el archivo con ruta y extensi�n. p.e: DatosTxt/archivo.txt
	 */
	public static InputStream getResourceAsStream(String pathResource) {
		String resource = PATH_RESOURCES + "/" + pathResource;
		return DXCUtil.class.getResourceAsStream(resource);
	}

	/**
	 * Retorna el caracter que usa la m�quina local como separador de decimales.
	 */
	public static char getDecimalSeparator() {
		return new DecimalFormat().getDecimalFormatSymbols().getDecimalSeparator();
	}
	
	/**
	 * Retorna el caracter que usa la m�quina local como separador de miles.
	 */
	public static char getMilSeparator() {
		char milSep = '.';
		if (getDecimalSeparator() == '.') milSep = ',';
		return milSep;
	}
	
	/**
	 * Captura de pantalla para la depuraci�n, se usa Robot para capturar la captura de pantalla.
	 * No se podr� usar en una m�quina remota y no podr� usarse en segundo plano. Debe mantener el objeto sobre 
	 * el que se desea el pintScreen encima de todas las dem�s ventanas.
	 * @param nbPathFile : Nombre del archivo donde se guardar�, con ruta y extensi�n (debe ser de imagen).
	 */
	public static void printScreen(String nbPathFile) {
        try {
            BufferedImage image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            ImageIO.write(image, "png", new File(nbPathFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
	/**
	 * Hace una espera de los segundos indicados.
	 * @param segundos
	 */
	public static void wait(int segundos) {
		try {
			Thread.sleep(segundos*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * M�todo que retorna la IP de la m�quina donde se est� ejecutando.
	 */
	public static String getIP() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostAddress();
	}

	/**
	 * M�todo que retorna el HOSTNAME de la m�quina donde se est� ejecutando.
	 */
	public static String getHostName() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostName();
	}
	
	/**
	 * Muestra en pantalla en un Frame, la informaci�n de la [exception], de igual manera lo escribe en el archivo LOG
	 * ubicado en [DXCUtil.LOG_FILE]
	 */
	public static void showExceptionInFrame(Exception exception) {
		  
		String message = exception.toString();
		for (StackTraceElement ste : exception.getStackTrace()) {
			message += "\n\tat " + ste.toString();
		}
		DXCUtil.showMessageInFrame("SE PRESENT� ERROR...", message);
	}
	
	/**
	 * Muestra en pantalla en un Frame, el mensaje indicado por [message], de igual manera lo escribe en el archivo LOG
	 * ubicado en [DXCUtil.LOG_FILE]
	 * @param title - T�tulo que lleva el frame
	 * @param message - Mensaje a presentar
	 */
	public static void showMessageInFrame(String title, String message) {
		  
		writeMessageInlog(message); // ESCRIBE EN EL LOG
		
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setTitle(title);
		f.setBackground(Color.LIGHT_GRAY);
		
		JTextArea textArea = new JTextArea();
		textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
		textArea.setText("\n" + message);
		textArea.setEditable(false);
		textArea.setBackground(Color.LIGHT_GRAY);
			
		f.add(new JScrollPane(textArea));
		f.setSize(new Dimension(350, 300));
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}
	
	/**
	 * Escribe en el archivo LOG ubicado en [DXCUtil.LOG_FILE] el [message]
	 */
	public static final String DIR_DXC_TEMP = "C:\\temp\\";
	public static final String LOG_FILE = DIR_DXC_TEMP + "DXC_SeleniumLog.txt";
	public static void writeMessageInlog(String message) {
		  
		  // SI LA CARPETA NO EXISTE, SE CREA:
		if (!DXCUtil.directoryExist(DIR_DXC_TEMP)) new File(DIR_DXC_TEMP).mkdir();
		  
		File archivo = new File(LOG_FILE);
		try {
			if (!archivo.exists()) archivo.createNewFile(); // SI NO EXISTE SE CREA
			FileWriter fw = new FileWriter(archivo, true);  // EL [true] ES PARA UBICAR EL ARCHIVO AL FINAL
			BufferedWriter bw = new BufferedWriter(fw);     // DA UN MEJOR PERFORMANCE A LA ESCRITURA
			bw.write("\n*** " + DXCUtil.dateToString("dd/mm/yyyy") + " - " + DXCUtil.hourToString("HH:mm:ss") + " >>> ");
			bw.write(message + "\n");
			bw.close();
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}
	
//=======================================================================================================================
	// TODO >> MANEJO DE STRINGS:
	private static final Pattern ACCENTS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	
	/**
	 * Retorna el [text] sin acentos.
	 */
	public static String removeAccents(String text) {
	    return ACCENTS_PATTERN.matcher(Normalizer.normalize(text, Normalizer.Form.NFD)).replaceAll("");
	}

	/**
	 * Indica si [text1] y [text2] son iguales ignorando si est�n en may�sculas, min�sculas y as� mismo tildes. 
	 */
	public static boolean equalsIgnoreCaseAndAccents(String text1, String text2) {
        final Collator instance = Collator.getInstance();
        // ESTA ESTRATEGIA IGNORA ACENTOS Y EL CASE
        instance.setStrength(Collator.NO_DECOMPOSITION);
        // [compare] DA 0 SI SON IGUALES
        return (instance.compare(text1, text2) == 0);
	}

	/**
	 * Indica si [text] est� contenido en [textComplete] ignorando si est�n en may�sculas, min�sculas y as� mismo tildes.
	 */
	public static boolean containsIgnoreCaseAndAccents(String textComplete, String text) {
		String hsToCompare = removeAccents(textComplete).toLowerCase();
	    String nToCompare  = removeAccents(text).toLowerCase();
	    return hsToCompare.contains(nToCompare);
	}
	
	/**
	 * Retorna el String que corresponde a los [lenght] caracteres que est�n a la derecha de [texto].<br>
	 * Si la longitud de [texto] es menor a [lenght] el retorno es [texto].<br>
	 * <p><b>right("corona", 4)</b> Retorna "rona"<br>
	 * <b>right("corona", 8)</b> Retorna "corona"
	 */
	public static String right(String texto, int lenght) {
		String valRetorno = texto;
		if (texto.length() > lenght)
			valRetorno = texto.substring(texto.length() - lenght);
		return valRetorno;
	}

	/**
	 * Retorna el String que corresponde a los [lenght] caracteres que est�n a la izquierda de [texto].<br>
	 * Si la longitud de [texto] es menor a [lenght] el retorno es [texto].<br>
	 * <p><b>left("corona", 4)</b> Retorna "coro"<br>
	 * <b>right("corona", 8)</b> Retorna "corona"
	 */
	public static String left(String value, int lenght) {
		String valRetorno = value;
		if (value.length() > lenght)
			valRetorno = value.substring(0, lenght);
		return valRetorno;
	}

	/**
	 * Indica si [texto] representa un valor entero.
	 */
	public static boolean isInteger(String texto){
		try {
			Long.parseLong(texto); //Se usa Long para que acepte ENTEROS grandes
			return true;
		} catch (NumberFormatException nfe){
			return false;
		}
	}

	/**
	 * Indica si [texto] representa un valor num�rico.
	 */
	public static boolean isNumeric(String texto){
		try {
			Double.parseDouble(texto); //Se usa Double para que acepte ENTEROS y DECIMALES grandes
			return true;
		} catch (NumberFormatException nfe){
			return false;
		}
	}
	
	/**
	 * Indica si [caracter] representa un valor num�rico.
	 */
	public static boolean isNumeric(char caracter){
		return DXCUtil.isNumeric(String.valueOf(caracter));
	}
	
	/**
	 * Repite el "charRepeat" de tal manera que retorne un String de tama�o "longitud".
	 */
	public static String repeat(char charRepeat, int longitud) {
		String textoRetorno = "";
		for (int i = 0; i < longitud; i++) {
			textoRetorno += charRepeat;
		}
		//String relleno = String.valueOf(charRepeat);
		//String textoRetorno = relleno.repeat(longitud);
		return textoRetorno;
	}
	
	/**
	 * Retorna un String de tama�o [longitud] a partir de [texto], cortando o rellenando a la izquierda:<br>
	 * - Si el tama�o de [texto] es igual a [longitud] se retorna [texto].<br>
	 * - Si el tama�o de [texto] es menor a [longitud] se completa [texto] con [charRelleno] a la izquierda.<br>
	 * - Si el tama�o de [texto] es mayor a [longitud] se corta la parte izquierda de [texto].
	 */
	public static String leftComplete(String texto, int longitud, char charRelleno) {
		String textoRetorno = texto;
		if (texto.length() < longitud) {
			textoRetorno = DXCUtil.repeat(charRelleno, longitud - texto.length()) + texto;
		} else if (texto.length() > longitud) {
			textoRetorno = texto.substring(texto.length()-longitud, texto.length());
		}
		return textoRetorno;
	}
	
	/**
	 * Retorna un String de tama�o [longitud] a partir de [texto], cortando o rellenando a la derecha:<br>
	 * - Si el tama�o de [texto] es igual a [longitud] se retorna [texto].<br>
	 * - Si el tama�o de [texto] es menor a [longitud] se completa [texto] con [charRelleno] a la derecha.<br>
	 * - Si el tama�o de [texto] es mayor a [longitud] se corta la parte derecha de [texto].
	 */
	public static String rightComplete(String texto, int longitud, char charRelleno) {
		String textoRetorno = texto;
		if (texto.length() < longitud) {
			textoRetorno = texto + DXCUtil.repeat(charRelleno, longitud - texto.length());
		} else if (texto.length() > longitud) {
			textoRetorno = texto.substring(0, longitud);
		}
		return textoRetorno;
	}
	
	/**
	 * M�todo que remueve de la derecha del "texto", el caracter "charRemove". Antes se hace el [trim] de "texto".
	 */
	public static String trimRight(String texto, char charRemove) {
		String textoTemp = texto.trim();
		int numBorrar = 0, longTexto = textoTemp.length();
	    
		for (int pos = longTexto-1; pos >= 0; pos--) {
			if (textoTemp.charAt(pos) == charRemove) numBorrar++;
			else break; // Cuando ya no encuentre el "charRemove" no busca m�s
		}
	    return DXCUtil.left(textoTemp, longTexto-numBorrar);
	}
	
	/**
	 * M�todo que remueve de la izquierda del "texto", el caracter "charRemove". Antes se hace el [trim] de "texto".
	 */
	public static String trimLeft(String texto, char charRemove) {
		String textoTemp = texto.trim();
		int numBorrar = 0, longTexto = textoTemp.length();
	    
		for (int pos = 0; pos < longTexto; pos++) {
			if (textoTemp.charAt(pos) == charRemove) numBorrar++;
			else break; // Cuando ya no encuentre el "charRemove" no busca m�s
		}
	    return DXCUtil.right(textoTemp, longTexto-numBorrar);
	}
	
	/**
	 * Este m�todo revisa el "textoCompleto", buscando "texto1" y "texto2", retorna el texto que se encuentra entre estos
	 * Strings, quita los espacios sobrantes de inicio y fin del texto extraido.<br><br>
	 * Tener en cuenta:<br>
	 * - Si no encuentra el "texto1" retorna lo que va antes de "texto2".<br>
	 * - Si no encuentra "texto2" retorna lo que va despu�s de "texto1".<br>
	 * - Si no encuentra ni "texto1" ni "texto2" retorna "textoCompleto".<br>
	 * - El texto retornado se encuentra en MAY�SCULAS, ya que todo el texto se pone as� para comparar.
	 */
	public static String getTextoEntre(String textoCompleto, String texto1, String texto2) {
		
		String[] arrayTexto1, arrayTexto2;
		String newT1 = DXCUtil.getDatoSplit(texto1);
		String newT2 = DXCUtil.getDatoSplit(texto2);
		arrayTexto1 = textoCompleto.toUpperCase().split(newT1.toUpperCase());
		if (arrayTexto1.length == 1) // NO ENCONTR� EL [texto1]
			arrayTexto2 = arrayTexto1[0].split(newT2.toUpperCase());
		else // ENCONTR� EL [texto1]
			arrayTexto2 = arrayTexto1[1].split(newT2.toUpperCase());
		return arrayTexto2[0].trim();
	}
	/**
	 * En el Split se pueden enviar expresiones regulares, las expresiones regulares pueden usar caracteres especiales,
	 * por ende si el dato para hacer Split contiene estos caracteres debe ser incluido dentro de llaves [] para que se
	 * tome el dato como es. 
	 */
	public static String getDatoSplit(String datoSplit) {
		String datoRetorno = datoSplit;
		if (datoRetorno.contains("|")) datoRetorno = datoRetorno.replace("|", "[|]");
		if (datoRetorno.contains(".")) datoRetorno = datoRetorno.replace(".", "[.]");
		if (datoRetorno.contains("*")) datoRetorno = datoRetorno.replace("*", "[*]");
		return datoRetorno;
	}
	
	/**
	 * M�todo que limpia el [texto] removiendo todos los caracteres [charQuitar] y adicional deja el retorno sin
	 * espacios al inicio, al final y entre palabras deja 1 �nico espacio.
	 */
	public static String limpiarTexto(String texto, char charQuitar) {
	    String valRetorno = texto;
	    String quitar = String.valueOf(charQuitar);
	    if (!quitar.isEmpty()) valRetorno = texto.replace(quitar, "");
	    return DXCUtil.totalTrim(valRetorno);
	}
	
	/**
	 * M�todo que remueve de inicio y fin los espacios, y adicional en el texto entre palabra y palabra deja un �nico
	 * espacio en caso que hayan separaciones de m�s de 1 espacio.
	 */
	public static String totalTrim(String texto) {
		String valRetorno = texto.trim();
		do { // Limpiar la parte interna de la l�nea dejando s�lo un espacio entre dato y dato
			valRetorno = valRetorno.replace("  ", " "); // Remueve el doble espacio y lo vuelve 1
		} while (valRetorno.contains("  "));
	    return valRetorno;
	}
	
	/**
	 * Transforma [expresionATransformar] al valor num�rico que contenga en double.<br>
	 * Para que sea negativo el primer caracter debe ser "-".
	 * @param expresionATransformar - Cadena con la expresi�n a transformar, puede tener s�mbolos raros.
	 * @param numDecimales - N�mero de decimales con los que cuenta el dato.
	 */
	public static double toDouble(String expresionATransformar, int numDecimales) {
		return Double.valueOf(toNumberInString(expresionATransformar, numDecimales));
	}

	/**
	 * Transforma [expresionATransformar] al valor num�rico que contenga en int.<br>
	 * Para que sea negativo el primer caracter debe ser "-".
	 * @param expresionATransformar - Cadena con la expresi�n a transformar, puede tener s�mbolos raros.
	 * @param numDecimales - N�mero de decimales con los que cuenta el dato.
	 */
	public static int toInt(String expresionATransformar, int numDecimales) {
		return Integer.valueOf(toNumberInString(expresionATransformar, numDecimales));
	}

	/**
	 * Transforma [expresionATransformar] a un String que sea valor num�rico que pueda ser formateado con Double.valueOf()<br>
	 * Para que sea negativo el primer caracter debe ser "-".
	 * @param expresionATransformar - Cadena con la expresi�n a transformar, puede tener s�mbolos raros.
	 * @param numDecimales - N�mero de decimales con los que cuenta el dato.
	 */
	public static String toNumberInString(String expresionATransformar, int numDecimales) {
		String valor = "";
		int inicio = 0;
		String expresion = expresionATransformar.trim();
		if (expresion.charAt(0) == '-') {
			valor = "-";
			inicio = 1;
		}
		char digito;
		for (int cont = inicio; cont < expresion.length(); cont++) {
			digito = expresion.charAt(cont);
			if (DXCUtil.isNumeric(digito)) valor += digito;
		}
		
		String valRetorno = valor;
		if (numDecimales > 0)
			valRetorno = left(valor, valor.length()-numDecimales) + "." + right(valor, numDecimales);
		return valRetorno;
	}
	
	/**
	 * Retorna en formato currency [$ ###,###.##] el dato [number] con la cantidad de decimales indicada.
	 */
	public static String formatCurrency(double number, int numDecimales) {
		String decimalFormat = "$ #,###";
		if (numDecimales > 0) decimalFormat += "." + repeat('0', numDecimales);
		DecimalFormat formato = new DecimalFormat(decimalFormat);
		return formato.format(number);
	}

	/**
	 * Retorna en formato num�rico [###,###.##] el dato [number] con la cantidad de decimales indicada.
	 */
	public static String formatNumber(double number, int numDecimales) {
		String decimalFormat = "#,###";
		if (numDecimales > 0) decimalFormat += "." + repeat('0', numDecimales);
		DecimalFormat formato = new DecimalFormat(decimalFormat);
		return formato.format(number);
	}

//=======================================================================================================================
	// TODO >> MANEJO DE NUM�ROS:
	
	/**
	 * Retorna un n�mero entero aleatorio entre [min] y [max].
	 */
	public static int enteroRandom(int min, int max) {
		Random aleatorio = new Random();
		return ( min + aleatorio.nextInt( max + 1 - min ) );
	}
	
//=======================================================================================================================
	// TODO >> MANEJO DE ARRAYS:
	
	/**
	 * Transforma a String el [array] separando cada elemento por [delimiter].
	 */
	public static String arrayToString(String[] array, String delimiter) {
		String sep = delimiter, valRetorno = "";
		int fin = array.length - 1;
		for (int i = 0; i <= fin; i++) {
			if (i == fin) sep = "";
			valRetorno += (array[i] + sep);
		}
		return valRetorno;
	}
	
	/**
	 * Transforma a String el [array] separando cada elemento por [delimiter].
	 */
	public static String arrayToString(int[] array, String delimiter) {
		String sep = delimiter, valRetorno = "";
		int fin = array.length - 1;
		for (int i = 0; i <= fin; i++) {
			if (i == fin) sep = "";
			valRetorno += (String.valueOf(array[i]) + sep);
		}
		return valRetorno;
	}
	
	/**
	 * Determina si el [item] es igual a alguno de los �tems existentes en el [array].<br>
	 * La comparaci�n es exacta.<br>
	 * Se usaba Arrays.binarySearch(array, item) pero no funcion� con este caso:
	 * String[] x = {"id", "test-id", "status"}; DXCUtil.itemInArray("status", x);
	 */
	public static boolean itemInArray(String item, String[] array) {
	    boolean existe = false; // EN CASO QUE [array] NO TENGA ELEMENTOS
	    for (int posArr = 0; posArr < array.length; posArr++) {
	    	existe = item.equals(array[posArr]);
			if (existe) break; // TERMINA EL CICLO
		}
		return existe;
	}
	
	/**
	 * Determina si el [item] es igual a alguno de los �tems existentes en el [array].<br>
	 * Se usaba Arrays.binarySearch(array, item) pero no funcion� con este caso:
	 * String[] x = {"id", "test-id", "status"}; DXCUtil.itemInArray("status", x);
	 */
	public static boolean itemInArray(int item, int[] array) {
	    boolean existe = false; // EN CASO QUE [array] NO TENGA ELEMENTOS
	    for (int posArr = 0; posArr < array.length; posArr++) {
	    	existe = ( item == array[posArr] );
			if (existe) break; // TERMINA EL CICLO
		}
		return existe;
	}
	
	/**
	 * Determina la posici�n en la que se encuentra el [item] dentro del [array]. La comparaci�n es exacta.<br>
	 * Si el item NO se encuentra el retorno es [-1]<br>
	 * Se usaba Arrays.binarySearch(array, item) pero no funcion� con este caso:
	 * String[] x = {"id", "test-id", "status"}; DXCUtil.itemInArray("status", x);
	 */
	public static int posItemInArray(String item, String[] array) {
	    int posItem = -1; // EN CASO QUE NO EST�
	    for (int posArr = 0; posArr < array.length; posArr++) {
	    	if (item.equals(array[posArr])) {
	    		posItem = posArr;
	    		break; // TERMINA EL CICLO
	    	}
		}
		return posItem;
	}
	
	/**
	 * Determina si alguno de los elementos del [array] contiene el [item].<br>
	 * Se ignoran acentos y si est� en may�sculas o min�sculas.
	 */
	public static boolean anyArrayItemContainsItem(String[] array, String item) {
	    boolean arrayItemContains = false; // EN CASO QUE [array] NO TENGA ELEMENTOS
	    for (int posArr = 0; posArr < array.length; posArr++) {
	    	arrayItemContains = DXCUtil.containsIgnoreCaseAndAccents(array[posArr], item);
			if (arrayItemContains) break; // TERMINA EL CICLO
		}
		return arrayItemContains;
	}
	
	/**
	 * Determina si el [item] contiene alguno de los elementos del [array].
	 */
	public static boolean itemContainsAnyArrayItem(String item, String[] array) {
	    boolean itemContains = false; // EN CASO QUE [array] NO TENGA ELEMENTOS
	    for (int posArr = 0; posArr < array.length; posArr++) {
	    	itemContains = item.contains(array[posArr]);
			if (itemContains) break; // TERMINA EL CICLO
		}
		return itemContains;
	}
	
	/**
	 * Si el [item] contiene alguno de los elementos del [array], retorna la posici�n del [array] en donde est� el
	 * elemento contenido. Si no hay elementso contenidos en [item[ el retorno es [-1].
	 */
	public static int itemContainsAnyArrayItemPos(String item, String[] array) {
	    int posItemContained = -1; // EN CASO QUE [array] NO TENGA ELEMENTOS
	    for (int posArr = 0; posArr < array.length; posArr++) {
	    	if (item.contains(array[posArr])) {
	    		posItemContained = posArr;
	    		break; // TERMINA EL CICLO
	    	}
		}
		return posItemContained;
	}
	
	/**
	 * Une los elementos de los arreglos recibidos en un s�lo Array. Cada elemento de los arreglos son [String].
	 */
	public static String[] joinArrays(String[]... arrays) {
		
		int tamFinal = 0;
		for (String[] array : arrays) {
			tamFinal += array.length;
		}
		String[] arrRetorno = new String[tamFinal];
		int posArr = 0;
		for (String[] array : arrays) {
			for (String item : array) {
				arrRetorno[posArr++] = item;
			}
		}
		return arrRetorno;
	}
	
	/**
	 * Une los elementos de los arreglos recibidos en un s�lo Array. Cada elemento de los arreglos son [int].
	 */
	public static int[] joinArrays(int[]... arrays) {
		
		int tamFinal = 0;
		for (int[] array : arrays) {
			tamFinal += array.length;
		}
		int[] arrRetorno = new int[tamFinal];
		int posArr = 0;
		for (int[] array : arrays) {
			for (int item : array) {
				arrRetorno[posArr++] = item;
			}
		}
		return arrRetorno;
	}
//=======================================================================================================================
	// TODO >> MANEJO DE FILES:
	public static final String ORDER_BYNAME = "BY_NAME";
    public static final String ORDER_BYLASTMODIFIED = "BY_LAST_MODIFIED";
    
    /**
     * Indica si el directorio con nombre [nbDirectory] existe y adem�s garantizando que sea folder.
     */
	public static boolean directoryExist(String nbDirectory) {
		File directory = new File(nbDirectory);
		boolean existe = false;
		// SI EXISTE, SE CORROBORA QUE SEA DIRECTORY
		if (directory.exists()) existe = directory.isDirectory();
		return existe;
	}
	
	/**
	 * Copia el archivo [nbFileSource] a [nbFileDestination], si el destino existe, lo reemplaza.
	 * @param nbFileSource - Nombre del archivo origen
	 * @param nbFileDestination - Nombre del archivo destino
	 * @throws IOException
	 */
	public static void copyFile(String nbFileSource, String nbFileDestination) throws IOException {
		Path origenPath  = Paths.get(nbFileSource);
        Path destinoPath = Paths.get(nbFileDestination);
        // SOBREESCRIBIR EL ARCHIVO DE DESTINO SI EXISTEY LO COPIA
        Files.copy(origenPath, destinoPath, StandardCopyOption.REPLACE_EXISTING);
	}
	
	/**
	 * Mueve el archivo [nbFileSource] a [nbFileDestination], si el destino existe, lo reemplaza.
	 * @param nbFileSource - Nombre del archivo origen
	 * @param nbFileDestination - Nombre del archivo destino
	 * @throws IOException
	 */
	public static void moveFile(String nbFileSource, String nbFileDestination) throws IOException {
		Path origenPath  = Paths.get(nbFileSource);
        Path destinoPath = Paths.get(nbFileDestination);
        // SOBREESCRIBIR EL ARCHIVO DE DESTINO SI EXISTEY LO MUEVE
        Files.move(origenPath, destinoPath, StandardCopyOption.REPLACE_EXISTING);
	}

	/**
	 * Retorna un arreglo de los directorio y archivos existentes en el directorio [nbDirectory] ordenados seg�n lo
	 * indicado en [order] y ascendente o descendentemente seg�n lo indicado por el par�metro [ascendente].
	 * @param nbDirectory Nombre del directorio.
	 * @param order pueden ser los valores [ORDER_BYNAME] o [ORDER_BYLASTMODIFIED]
	 * @param orderAsc true hace ordenamiento ascendente, false hace ordenamiento descendente.
	 */
    public static File[] getFilesOrderBy(String nbDirectory, String order, boolean orderAsc) {
    	File directory = new File(nbDirectory);
    	File[] files = directory.listFiles();
    	switch (order) {
		case DXCUtil.ORDER_BYNAME:
			// USA EL [toLowerCase] PORQUE SIN ESTO ORDENA Y TIENE EN CUENTA LAS MAY�SCULAS COMO MENORES QUE LAS MIN�SCULAS
			if (orderAsc) Arrays.sort(files, Comparator.comparing(File::getName, Comparator.comparing(String::toLowerCase)));
			else Arrays.sort(files, Comparator.comparing(File::getName, Comparator.comparing(String::toLowerCase)).reversed());
			break;
		case DXCUtil.ORDER_BYLASTMODIFIED:
			if (orderAsc) Arrays.sort(files, Comparator.comparingLong(File::lastModified));
			else Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
    		break;
		}
        return files;
    }
    
    /**
     * Retorna [true] si el archivo con nombre [fileName] est� abierto, por ende est� bloqueado. En caso contrario
     * retorna [false].
     * Pre-requisito: El archivo existe
     */
    @SuppressWarnings({ "unused", "resource" })
	public static boolean fileIsLocked(String fileName) {
    	boolean isLocked;
    	try {
    		FileChannel channel = new RandomAccessFile(new File(fileName), "rw").getChannel();
    		isLocked = false;
		} catch (Exception e) {
			isLocked = true;
		}
    	return isLocked;
    }
    
    /**
     * Retorna la extensi�n del nombre del archivo recibido. Lo devuelve en may�sculas.
     */
    public static String fileGetExtension(String fileName) {
    	String extension = "";
    	int i = fileName.lastIndexOf('.');
		if (i >= 0) extension = fileName.substring(i+1).toUpperCase();
		return extension;
    }
    
    /**
     * Indica si el archivo [fileName] es una IMAGEN.<br>
     * Si no existe el archivo, lo toma como que NO es imagen.
     */
    public static boolean isImage(String fileName) {
    	return isImage(new File(fileName));
    }

    /**
     * Indica si el archivo [file] es una IMAGEN.<br>
     * Si no existe el archivo, lo toma como que NO es imagen.
     */
    public static boolean isImage(File file) {
    	try {
    		String mimetype = Files.probeContentType(file.toPath());
    		// [mimetype] ES ALGO COMO "image/png"
    		return (mimetype != null && mimetype.split("/")[0].toUpperCase().equals("IMAGE"));
			
		} catch (Exception e) {
			return false;
		}
    }

//=======================================================================================================================
	// TODO >> MANEJO DE FECHAS Y TIEMPOS:
	
	/**
	 * Retorna la hora actual en el formato indicado.
	 * @param format Formato requerido = "HHH:mm:ss", "hh:mm:ss", "HH:mm", "hh:mm" 
	 */
	public static String hourToString(String format) {
		Date dtFechaActual = new Date();
		DateFormat dfLocal = new SimpleDateFormat(format);
		return ( dfLocal.format(dtFechaActual) );
	}
	
	/**
	 * Retorna la fecha actual en el formato indicado. Para que traiga el mes actual el formato debe ir con M may�scula.
	 * @param format Formato requerido = "yyyy/MM/dd", "yyyyMMdd" 
	 * @throws Exception  
	 */
	public static String dateToString(String format) throws Exception {
		Calendar today = Calendar.getInstance();
		return dateToString(today, format);
	}
	
	/**
	 * Retorna la fecha [date] en el formato indicado. Para que traiga el mes actual el formato debe ir con M may�scula.
	 * @param format Formato requerido = "yyyy/MM/dd", "yyyyMMdd" 
	 * @throws Exception 
	 */
	public static String dateToString(Calendar dateCalendar, String format) throws Exception {

		String sep = "", stringRet;
		String uCaseFormat = format.toUpperCase().trim();
    	String dia = leftComplete(String.valueOf(dateCalendar.get(Calendar.DATE)), 2, '0');
    	String mes = leftComplete(String.valueOf(dateCalendar.get(Calendar.MONTH) + 1), 2, '0'); // VIENE DESDE 0
    	String ano = String.valueOf(dateCalendar.get(Calendar.YEAR));

		switch (uCaseFormat) {
		case "DD/MM/YYYY": case "DD-MM-YYYY":
			sep = format.substring(2, 3);
			stringRet = dia + sep + mes + sep + ano;
			break;

		case "MM/DD/YYYY": case "MM-DD-YYYY":
			sep = format.substring(2, 3);
			stringRet = mes + sep + dia + sep + ano;
			break;
			
		case "YYYY/MM/DD": case "YYYY-MM-DD":
			sep = format.substring(4, 5);
			stringRet = ano + sep + mes + sep + dia;
			break;
			
		case "YYYY/DD/MM": case "YYYY-DD-MM":
			sep = format.substring(4, 5);
			stringRet = ano + sep + dia + sep + mes;
			break;
			
		case "DDMMYYYY":
			stringRet = dia + mes + ano;
			break;
			
		case "MMDDYYYY":
			stringRet = mes + dia + ano;
			break;
			
		case "YYYYMMDD":
			stringRet = ano + mes + dia;
			break;
			
		case "D-M-YY":
			stringRet = trimLeft(dia, '0') + "-" + trimLeft(mes, '0') + "-" + right(ano, 2);
			break;
			
		case "M-D":
			stringRet = trimLeft(mes, '0') + "-" + trimLeft(dia, '0');
			break;
			
		default:
			throw new Exception("Formato de fecha [" + format + "] NO contemplado");
		}
		return stringRet;
	}
	
	/**
	 * Retorna el objeto [Calendar] correspondiente a [dateString], se asume que la fecha est� en formato "dd/mm/yyyy". 
	 */
	public static Calendar stringToCalendar(String dateString) throws Exception {
		return stringToCalendar(dateString, "dd/mm/yyyy");
	}
	
	/**
	 * Retorna el objeto [Calendar] correspondiente a [dateString], el formato de la fecha corresponde a [formatDate].
	 */
	public static Calendar stringToCalendar(String dateString, String formatDate) throws Exception {
		
		String format = formatDate.toUpperCase().trim();
		int day, month, year;
		switch (format) {
		case "DD/MM/YYYY": case "DD-MM-YYYY":
			day   = Integer.valueOf(dateString.substring(0, 2));
			month = Integer.valueOf(dateString.substring(3, 5));
			year  = Integer.valueOf(dateString.substring(6, 10));
			break;

		case "MM/DD/YYYY": case "MM-DD-YYYY":
			day   = Integer.valueOf(dateString.substring(3, 5));
			month = Integer.valueOf(dateString.substring(0, 2));
			year  = Integer.valueOf(dateString.substring(6, 10));
			break;
			
		case "YYYY/MM/DD": case "YYYY-MM-DD":
			day   = Integer.valueOf(dateString.substring(8, 10));
			month = Integer.valueOf(dateString.substring(5, 7));
			year  = Integer.valueOf(dateString.substring(0, 4));
			break;
			
		case "YYYY/DD/MM": case "YYYY-DD-MM":
			day   = Integer.valueOf(dateString.substring(5, 7));
			month = Integer.valueOf(dateString.substring(8, 10));
			year  = Integer.valueOf(dateString.substring(0, 4));
			break;
			
		case "DDMMYYYY":
			day   = Integer.valueOf(dateString.substring(0, 2));
			month = Integer.valueOf(dateString.substring(2, 4));
			year  = Integer.valueOf(dateString.substring(4, 8));
			break;
			
		case "MMDDYYYY":
			day   = Integer.valueOf(dateString.substring(2, 4));
			month = Integer.valueOf(dateString.substring(0, 2));
			year  = Integer.valueOf(dateString.substring(4, 8));
			break;
			
		default:
			throw new Exception("Formato de fecha [" + formatDate + "] NO contemplado");
		}
		Calendar dateCal = Calendar.getInstance();
		dateCal.set(year, month-1, day);
		return dateCal;
	}
	
//=======================================================================================================================
	// TODO >> MANEJO DE HASHMAP:
	
	/**
	 * Retorna los values del HashMap [datos] en un String, separados por [delimiter]
	 * @param datos - HashMap que contiene los datos
	 * @param delimiter - String que separa cada value del HashMap.
	 */
	public static String hashMapValuesToString(HashMap<String, String> datos, String delimiter) {
		String valRetorno = "", sep = delimiter;
		Collection<String> values = datos.values();
		int cont = 0, numValues = values.size();
		
		for (String value : values) {
			cont++;
			if (cont == numValues) sep = ""; // EN EL �LTIMO YA NO PONE SEPARADOR
			valRetorno += value + sep;
		}
		return valRetorno;
	}

	/**
	 * Retorna los Keys del HashMap [datos] en un String, separados por [delimiter]
	 * @param datos - HashMap que contiene los datos
	 * @param delimiter - String que separa cada key del HashMap.
	 */
	public static String hashMapKeysToString(HashMap<String, String> datos, String delimiter) {
		String valRetorno = "", sep = delimiter;
		Set<String> keys = datos.keySet();
		int cont = 0, numValues = keys.size();
		
		for (String key : keys) {
			cont++;
			if (cont == numValues) sep = ""; // EN EL �LTIMO YA NO PONE SEPARADOR
			valRetorno += key + sep;
		}
		return valRetorno;
	}
	
}
