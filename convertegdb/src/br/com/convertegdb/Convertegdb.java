package br.com.convertegdb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * @author Giuliano Rosa da Silva
 * @email giulianors@gmail.com
 *
 */
public class Convertegdb {

	/**
	 * @param args
	 */

	private static void iniciaLog() {
		String h = MyLogHandler.class.getCanonicalName();
		StringBuilder sb = new StringBuilder();
		sb.append(".level=ALL\n");
		sb.append("handlers=").append(h).append('\n');

		try {
			LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(sb.toString().getBytes("UTF-8")));
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Util util = new Util();
		util.setHost("localhost");
		util.setUser("sysdba");
		util.setPassword("masterkey");

		iniciaLog();

		if (args.length != 1) {
			System.out.println("Uso: java -jar Convertegdb.jar caminho_diretorio_gdb");
		}

		String[] lista = util.retornaLista(args[0]);

		for (int i = 0; i < lista.length; i++) {
			util.setDatabase(String.format("%s/%s", args[0], lista[i]));

			Logger.getLogger("AnotherName").log(Level.INFO, "Iniciado " + lista[i]);

			try {
				if (util.Inicia()) {
					if (util.listaTabelas()) {
						util.salvaArquivos();
						Logger.getLogger("AnotherName").log(Level.INFO, "Finalizado " + lista[i]);
					}
				}
			} catch (Exception e) {
				Logger.getLogger("SameName").log(Level.SEVERE, e.getMessage());
			}
		}
		Logger.getLogger("AnotherName").log(Level.INFO, "Finalizado");
	}
}
