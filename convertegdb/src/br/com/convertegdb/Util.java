/**
 * 
 */
package br.com.convertegdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * @author Giuliano Rosa da Silva
 * @email giulianors@gmail.com
 *
 */
public class Util {
	private int tamBuffer = 30;
	private String driverName = "org.firebirdsql.jdbc.FBDriver";
	private String databaseUrl;

	private String database;
	private String user;
	private String password;
	private String host;
	private boolean erro;
	private String mensagem;
	
	private MyLogHandler myloghandler;

	java.sql.Driver d = null;
	java.sql.Connection conn = null;
	java.sql.Statement s = null;
	java.sql.ResultSet rs = null;

	FileOutputStream fos;
	ZipOutputStream zipOut;

	List<String> tabelas;
	
	public MyLogHandler getMyloghandler() {
		return myloghandler;
	}

	public void setMyloghandler(MyLogHandler myloghandler) {
		this.myloghandler = myloghandler;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	private String configura() {
		return String.format("jdbc:firebirdsql:%s/3050:%s", this.host, this.database);
	}

	public boolean Inicia() {

		if (this.database == null || this.database.isEmpty()) {
			erro = true; //
			mensagem = "Database não informado.";			
			Logger.getLogger("AnotherName").log(Level.SEVERE, mensagem);
			return false;
		}

		this.databaseUrl = this.configura();

		erro = false;

		Properties props = new Properties();
		props.setProperty("user", this.user);
		props.setProperty("password", this.password);
		props.setProperty("encoding", "UTF8");

		try {
			Class.forName(driverName);

			conn = DriverManager.getConnection(this.databaseUrl, props);
		} catch (Exception c) {
			Logger.getLogger("AnotherName").log(Level.SEVERE, c.getMessage());
			c.printStackTrace();			
			return false;
		}
		
		Logger.getLogger("AnotherName").log(Level.INFO, "Conexão aberta.");

		return true;
	}

	public boolean listaTabelas() {
		String sql;
		tabelas = new ArrayList<String>();

		sql = "SELECT RDB$RELATION_NAME";
		sql += " FROM RDB$RELATIONS";
		sql += " WHERE ((RDB$SYSTEM_FLAG = 0)";
		sql += " OR (RDB$SYSTEM_FLAG IS NULL))";
		sql += " AND (RDB$VIEW_SOURCE IS NULL)";
		sql += " ORDER BY RDB$RELATION_NAME";

		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				tabelas.add(rs.getString(1).trim());
			}
			return true;
		} catch (Exception e) {			
			e.printStackTrace();
			Logger.getLogger("AnotherName").log(Level.SEVERE, e.getMessage());
			return false;
		}

		// Now that we have a connection, let's try to get some meta data...
//	      try {
//	        java.sql.DatabaseMetaData dbMetaData = conn.getMetaData ();
//
//	        // What are the views defined on this database?
//	        //java.sql.ResultSet tables = dbMetaData.getTables (null, null, "%", new String[] {"VIEW"});
//	        java.sql.ResultSet tables = dbMetaData.getTables (null, null, "%", null);
//	        while (tables.next ()) {
//	          System.out.println (tables.getString ("TABLE_NAME") + " is a view.");
//	        }
//	        tables.close ();
//	        return true;
//	      }
//	      catch (java.sql.SQLException e) {
//	        System.out.println ("Unable to extract database meta data.");
//	        e.printStackTrace();
//	        return false;
//	        // What the heck, who needs meta data anyway ;-(, let's continue on...
//	      }

	}

	private boolean criaZip(String nomezip) throws FileNotFoundException {
		fos = new FileOutputStream(nomezip);
		if (fos != null) {
			zipOut = new ZipOutputStream(fos);
			return true;
		} else
			return false;
	}

	private boolean fechaZip() throws IOException {
		if (zipOut != null) {
			zipOut.close();
			fos.close();
			return true;
		} else
			return false;
	}

	private boolean adicionaZip(String nomearq) throws IOException {
		if (fos != null) {
			File fileToZip = new File(nomearq);
			FileInputStream fis = new FileInputStream(fileToZip);
			ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
			zipOut.putNextEntry(zipEntry);
			byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zipOut.write(bytes, 0, length);
			}
			fis.close();
			return true;
		} else
			return false;
	}

	public boolean salvaArquivos() throws SQLException, IOException {
		String sql;
		String saida = "";
		String nomearq = "";
		int total;
		int nr_linhas = 0;

		if (criaZip(String.format("%s.zip", this.database.substring(0, this.database.length() - 4)))) {

			for (String s : tabelas) {
				sql = String.format("SELECT * FROM %s", s);

				nomearq = this.database;
				nomearq = String.format("%s_%s.csv", nomearq.substring(0, nomearq.length() - 4), s);

				FileOutputStream arqsaida = new FileOutputStream(nomearq);
				System.out.println("Arquivo " + s);

				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);

				ResultSetMetaData rsMetaData = rs.getMetaData();

				total = rsMetaData.getColumnCount();
				for (int i = 1; i <= total; i++) {
					saida += rsMetaData.getColumnName(i) + ";";
				}
				saida = saida.substring(0, saida.length() - 1) + "\n";

				arqsaida.write(saida.getBytes());
				saida = "";

				while (rs.next()) {
					for (int i = 1; i <= total; i++) {
						if (rs.getObject(i) == null) {
							saida += "\"\";";
						} else {
							saida += "\"" + rs.getObject(i).toString().trim() + "\";";
						}
					}
					saida = saida.substring(0, saida.length() - 1) + "\n";

					nr_linhas += 1;
					if (nr_linhas % tamBuffer == 0) {
						arqsaida.write(saida.getBytes());
						saida = "";
					}
				}
				if (saida.length() > 0) {
					arqsaida.write(saida.getBytes());
					saida = "";
				}
				arqsaida.close();

				if (adicionaZip(nomearq)) {
					File f = new File(nomearq);
					f.delete();
				}
			}
			fechaZip();
		} else
			return false;

		return true;
	}

	public String[] retornaLista(String diretorio) {
		File directoryPath = new File(diretorio);
		FilenameFilter textFilefilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				String lowercaseName = name.toLowerCase();
				if (lowercaseName.endsWith(".gdb")) {
					return true;
				} else {
					return false;
				}
			}
		};

		String[] textFilesList = directoryPath.list(textFilefilter);

		return textFilesList;
	}	
}
