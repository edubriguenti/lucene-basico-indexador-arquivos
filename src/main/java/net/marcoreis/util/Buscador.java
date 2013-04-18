package net.marcoreis.util;

import java.io.*;

import javax.swing.*;

import org.apache.log4j.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryParser.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import org.apache.lucene.util.*;

/**
* Classe responsável por recuperar documentos através de um parâmetro
* 
*/
public class Buscador {
  private static Logger logger = Logger.getLogger(Buscador.class);
  private String diretorioDoIndice = System.getProperty("user.home")
      + "/indice-lucene";

  /**
  *
  */
  public void buscaComParser(String parametro) {
    try {
      //{1}
      Directory diretorio = new SimpleFSDirectory(new File(diretorioDoIndice));
      //{2}
      IndexReader leitor = IndexReader.open(diretorio);
      IndexSearcher buscador = new IndexSearcher(leitor);
      //{3}
      Analyzer analisador = new StandardAnalyzer(Version.LUCENE_36);
      //{4}
      QueryParser parser = new QueryParser(Version.LUCENE_36, "Texto",
          analisador);
      Query consulta = parser.parse(parametro);
      //{5}
      long inicio = System.currentTimeMillis();
      TopDocs resultado = buscador.search(consulta, 100);
      long fim = System.currentTimeMillis();
      int totalDeOcorrencias = resultado.totalHits;
      logger.info("Total de documentos encontrados:" + totalDeOcorrencias);
      logger.info("Tempo total para busca: " + (fim - inicio) + "ms");
      //{6}
      for (ScoreDoc sd : resultado.scoreDocs) {
        Document documento = buscador.doc(sd.doc);
        logger.info("Caminho:" + documento.get("Caminho"));
        logger.info("Última modificação:" + documento.get("UltimaModificacao"));
        logger.info("Score:" + sd.score);
        logger.info("--------");
      }
      buscador.close();
    } catch (Exception e) {
      logger.error(e);
    }
  }

  public static void main(String[] args) {
    Buscador b = new Buscador();
    String parametro = JOptionPane.showInputDialog("Consulta");
    b.buscaComParser(parametro);
  }
}