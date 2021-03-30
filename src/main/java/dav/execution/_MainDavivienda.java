package dav.execution;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;

import dxc.util.DXCUtil;
import launchTest.empresarial.web.LogueoPyme;

public class _MainDavivienda {

	public static void main(String[] args) {
		DXCUtil.PATH_RESOURCES = "/resources";
		TestListenerAdapter tla = new TestListenerAdapter();
		TestNG testng = new TestNG();
		
		testng.setTestClasses(new Class[] { LogueoPyme.class });
		testng.addListener(tla);
		testng.run();
	}

}
